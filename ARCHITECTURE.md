# Award Management System - Architecture Documentation

## System Architecture

The Award Management System implements a layered architecture with design patterns that enable seamless migration between old and new data processing flows.

### Key Configuration: Transparent Proxy Pattern

The system uses Spring's `@Primary` annotation to implement transparent proxying:

1. **Service Layer** injects `OldFlow` interface
2. **AwardRepoProxy** implements `OldFlow` and is marked `@Primary`
3. Spring automatically injects the proxy instead of the actual repository
4. **No code changes** required in service layer!

```java
// In AwardService.java
@Autowired
public AwardService(OldFlow awardRepository) {
    // Spring injects AwardRepoProxy here (due to @Primary)
    // Service code doesn't know it's using a proxy!
}

// In AwardRepoProxy.java
@Repository
@Primary  // This makes Spring inject this instead of the actual OldFlow
@Qualifier("awardRepoProxy")
public class AwardRepoProxy implements OldFlow {
    // Delegates to AwardFacade
}

// Actual repositories use specific qualifiers
@Repository
@Qualifier("oldFlow")
public interface OldFlow extends MongoRepository<Award, String> { }

@Repository
@Qualifier("newFlow")
public interface NewFlow extends MongoRepository<Award, String> { }
```

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     REST Controller Layer                    │
│  ┌──────────────────────┐  ┌──────────────────────────────┐ │
│  │  AwardController     │  │  FeatureToggleController     │ │
│  │  /api/awards         │  │  /api/feature-toggles        │ │
│  └──────────────────────┘  └──────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                       Service Layer                          │
│                    (Your Business Logic)                     │
│  ┌──────────────────────────────────────────────────────────┤
│  │  AwardService                                             │
│  │  - createAward()       - approveAward()                  │
│  │  - updateAward()       - rejectAward()                   │
│  │  - awardToRecipient()  - getAwardsByStatus()            │
│  └──────────────────────────────────────────────────────────┤
└─────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                    Proxy Layer (Traffic Cop)                 │
│  ┌──────────────────────────────────────────────────────────┤
│  │  AwardRepoProxy                                           │
│  │  - Logging                                                │
│  │  - Validation                                             │
│  │  - Cross-cutting concerns                                │
│  └──────────────────────────────────────────────────────────┤
└─────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                  Facade Layer (Decision Maker)               │
│  ┌──────────────────────────────────────────────────────────┤
│  │  AwardFacade                                              │
│  │  ┌────────────────────────────────────────────┐          │
│  │  │  1. Checks FeatureToggle:                  │          │
│  │  │     "use-new-flow" enabled?                │          │
│  │  │                                            │          │
│  │  │  2. Routes request to:                    │          │
│  │  │     ├─ OldFlow (if disabled)              │          │
│  │  │     └─ NewFlow (if enabled)               │          │
│  │  └────────────────────────────────────────────┘          │
│  └──────────────────────────────────────────────────────────┤
└─────────────────────────────────────────────────────────────┘
                   ↓                    ↓
        ┌──────────────────┐  ┌──────────────────┐
        │                  │  │                  │
        │     OldFlow      │  │     NewFlow      │
        │  (Repository)    │  │  (Repository)    │
        │                  │  │                  │
        └──────────────────┘  └──────────────────┘
                   ↓                    ↓
        ┌──────────────────────────────────────┐
        │                                      │
        │        MongoDB Database              │
        │        Collection: awards            │
        │                                      │
        └──────────────────────────────────────┘

        ┌──────────────────────────────────────┐
        │      Feature Toggle Service          │
        │                                      │
        │  ┌────────────────────────────────┐  │
        │  │  FeatureToggleRepository       │  │
        │  └────────────────────────────────┘  │
        │              ↓                       │
        │  ┌────────────────────────────────┐  │
        │  │     MongoDB Database           │  │
        │  │  Collection: feature_toggles   │  │
        │  └────────────────────────────────┘  │
        └──────────────────────────────────────┘
```

## Design Patterns

### 1. Proxy Pattern (AwardRepoProxy)

**Role**: "Traffic Cop"

**Responsibilities**:
- Intercepts all repository calls
- Provides cross-cutting concerns:
  - Logging: Logs every operation with appropriate level
  - Validation: Validates inputs before processing
  - Error handling: Provides consistent error messages
- Delegates actual work to AwardFacade

**Example Flow**:
```java
User Request → AwardRepoProxy.save()
              ↓
              Validate award (not null, has name)
              ↓
              Log: "Saving award request received"
              ↓
              Delegate to AwardFacade.save()
              ↓
              Log: "Award saved successfully"
              ↓
              Return saved award
```

### 2. Facade Pattern (AwardFacade)

**Role**: "Decision Maker"

**Responsibilities**:
- Determines which flow to use based on feature toggle
- Abstracts complexity of multiple repository implementations
- Provides single interface to underlying repositories
- Checks feature toggle state for each operation

**Decision Logic**:
```java
boolean useNewFlow = featureToggleService.isFeatureEnabled("use-new-flow");

if (useNewFlow) {
    return newFlow.save(award);  // Use enhanced processing
} else {
    return oldFlow.save(award);  // Use legacy processing
}
```

### 3. Feature Toggle Pattern

**Role**: Runtime Configuration Control

**Responsibilities**:
- Stores feature flags in database
- Allows runtime switching without code deployment
- Enables gradual rollout strategies
- Supports A/B testing and canary releases

**Toggle States**:
- **Enabled (true)**: Routes all requests to NewFlow
- **Disabled (false)**: Routes all requests to OldFlow

## Data Flow Examples

### Example 1: Create Award (OldFlow)

```
1. POST /api/awards
   ↓
2. AwardController.createAward()
   ↓
3. AwardService.createAward()
   - Sets creation timestamp
   - Sets status to "PENDING"
   ↓
4. AwardRepoProxy.save()
   - Validates award
   - Logs operation
   ↓
5. AwardFacade.save()
   - Checks toggle: use-new-flow = false
   - Routes to OldFlow
   ↓
6. OldFlow.save() (MongoRepository)
   ↓
7. MongoDB: Insert into awards collection
   ↓
8. Return saved Award to user
```

### Example 2: Switch to NewFlow

```
1. POST /api/feature-toggles/use-new-flow/enable
   ↓
2. FeatureToggleController.enableFeature()
   ↓
3. FeatureToggleService.enableFeature()
   ↓
4. Update MongoDB: feature_toggles.use-new-flow = true
   ↓
5. All subsequent requests now use NewFlow
```

### Example 3: Create Award (NewFlow)

```
1. POST /api/awards
   ↓
2. [Same as Example 1 until step 5]
   ↓
5. AwardFacade.save()
   - Checks toggle: use-new-flow = true
   - Routes to NewFlow
   ↓
6. NewFlow.save() (MongoRepository with enhanced queries)
   ↓
7. MongoDB: Insert into awards collection
   ↓
8. Return saved Award to user
```

## Component Responsibilities

### Controllers
- **AwardController**: HTTP endpoints for award CRUD operations
- **FeatureToggleController**: HTTP endpoints for toggle management

### Service Layer
- **AwardService**: Business logic for award lifecycle
  - Award creation and updates
  - Status management (approve/reject)
  - Awarding to recipients

### Proxy Layer
- **AwardRepoProxy**: Cross-cutting concerns wrapper
  - Input validation
  - Operation logging
  - Consistent error handling

### Facade Layer
- **AwardFacade**: Repository selection logic
  - Feature toggle checking
  - Flow routing
  - Repository abstraction
- **FeatureToggleService**: Toggle management
  - Toggle CRUD operations
  - State checking

### Repository Layer
- **OldFlow**: Legacy data access (extends MongoRepository)
- **NewFlow**: Enhanced data access (extends MongoRepository)
- **FeatureToggleRepository**: Toggle persistence

### Domain Layer
- **Award**: Award entity with full lifecycle properties
- **FeatureToggle**: Feature flag configuration entity

## Migration Strategy

### Phase 1: Setup (Current State)
```
All traffic → OldFlow
Feature toggle: use-new-flow = false
```

### Phase 2: Testing
```
Test traffic → NewFlow (manual testing)
Production traffic → OldFlow
Feature toggle: use-new-flow = false
```

### Phase 3: Canary Release
```
10% traffic → NewFlow
90% traffic → OldFlow
Feature toggle: Conditional routing based on user/criteria
```

### Phase 4: Full Migration
```
All traffic → NewFlow
OldFlow maintained for rollback
Feature toggle: use-new-flow = true
```

### Phase 5: Cleanup
```
All traffic → NewFlow
Remove OldFlow code
Remove feature toggle
```

## Key Benefits

1. **Zero Downtime Migration**: Switch flows without restarting
2. **Easy Rollback**: Toggle off if issues occur
3. **Gradual Rollout**: Test with subset of users first
4. **Clean Separation**: Clear boundaries between components
5. **Testability**: Each layer can be tested independently
6. **Maintainability**: Well-organized, single-responsibility components
7. **Extensibility**: Easy to add new flows or features

## Configuration

### MongoDB Collections

**awards**:
```json
{
  "_id": "ObjectId",
  "name": "String",
  "description": "String",
  "category": "String",
  "recipientId": "String",
  "awardedDate": "ISODate",
  "status": "String",
  "createdBy": "String",
  "createdAt": "ISODate",
  "updatedAt": "ISODate"
}
```

**feature_toggles**:
```json
{
  "_id": "ObjectId",
  "featureName": "String",
  "enabled": "Boolean",
  "description": "String",
  "createdAt": "ISODate",
  "updatedAt": "ISODate",
  "updatedBy": "String"
}
```

## Logging Strategy

Each layer logs at appropriate levels:

- **DEBUG**: Method entry/exit, flow selection
- **INFO**: Business operations, toggle changes
- **WARN**: Validation failures, missing toggles
- **ERROR**: System failures, database errors

Example log flow:
```
INFO  AwardController: Create award request received
DEBUG AwardService: Creating new award: Employee of the Month
INFO  AwardRepoProxy: Saving award request received
DEBUG AwardRepoProxy: Award validation passed
DEBUG AwardFacade: Using New Flow for award processing
INFO  AwardFacade: Saving award: Employee of the Month using NewFlow
INFO  AwardRepoProxy: Award saved successfully with ID: 507f1f77bcf86cd799439011
INFO  AwardService: Award created successfully with ID: 507f1f77bcf86cd799439011
```

## Future Enhancements

1. **Metrics Collection**: Track performance of each flow
2. **A/B Testing**: Route based on user segments
3. **Audit Logging**: Track all state changes
4. **Caching**: Add caching layer in proxy
5. **Circuit Breaker**: Add resilience patterns
6. **Event Sourcing**: Publish events for each operation
