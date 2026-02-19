# Award Management System

A Spring Boot application demonstrating the Proxy and Facade design patterns with feature toggle functionality for seamless migration between old and new data flows.

## 📚 Documentation

- **[MIGRATION_STRATEGY.md](MIGRATION_STRATEGY.md)** - Comprehensive guide to Proxy + Facade migration strategy, advantages, drawbacks, and best practices
- **[FEATURE_TOGGLE_CONFIGURATION.md](FEATURE_TOGGLE_CONFIGURATION.md)** - Feature toggle configuration guide (YAML + DB fallback)
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detailed system architecture and component responsibilities
- **[PROXY_CONFIGURATION.md](PROXY_CONFIGURATION.md)** - Spring bean configuration and dependency injection details

## Architecture Overview

```
Service Layer (Your Code)
    ↓
AwardRepoProxy ← "Traffic cop"
    ↓
AwardFacade ← "Decision maker"
    ↓
Checks Feature Toggle
    ↓
    ├─→ OldFlow (MongoRepository)
    └─→ NewFlow (MongoRepository)
```

## Design Patterns Implemented

### 1. Proxy Pattern - AwardRepoProxy
- Acts as a "traffic cop"
- Implements the OldFlow interface
- Marked with `@Primary` annotation - **this is key!**
- Spring automatically injects this proxy when service layer asks for `OldFlow`
- Provides cross-cutting concerns (logging, validation)
- Delegates to AwardFacade

**Key Configuration:**
```java
@Repository
@Primary  // Makes Spring inject this instead of actual AwardRepo
@Qualifier("awardRepoProxy")
public class AwardRepoProxy implements OldFlow {
    // Service layer gets this proxy transparently!
}
```

### 2. Facade Pattern - AwardFacade
- Acts as a "decision maker"
- Checks feature toggle to determine which flow to use
- Abstracts complexity of choosing between OldFlow and NewFlow

### 3. Feature Toggle Pattern - FeatureToggle
- Controls which repository implementation is used
- **Primary source**: `application.yml` (simple, clean configuration)
- **Fallback**: MongoDB database (for runtime changes)
- Priority: YAML → Database → false (default)

## Components

### Domain Layer
- **Award**: Entity representing an award
- **FeatureToggle**: Entity for feature flag management

### Repository Layer
- **OldFlow**: MongoDB repository for legacy award processing
- **NewFlow**: MongoDB repository for new award processing
- **FeatureToggleRepository**: Repository for feature toggle management

### Facade Layer
- **AwardFacade**: Decides which repository to use based on feature toggle
- **FeatureToggleService**: Service for managing feature toggles

### Proxy Layer
- **AwardRepoProxy**: Proxy for additional cross-cutting concerns

### Service Layer
- **AwardService**: Business logic for award management

### Controller Layer
- **AwardController**: REST endpoints for award operations
- **FeatureToggleController**: REST endpoints for feature toggle management

## API Endpoints

### Award Management

#### Create Award
```
POST /api/awards
Content-Type: application/json

{
  "name": "Best Employee Award",
  "description": "Award for outstanding performance",
  "category": "PERFORMANCE"
}
```

#### Get All Awards
```
GET /api/awards
```

#### Get Award by ID
```
GET /api/awards/{id}
```

#### Update Award
```
PUT /api/awards/{id}
Content-Type: application/json

{
  "name": "Updated Award Name",
  "status": "APPROVED"
}
```

#### Delete Award
```
DELETE /api/awards/{id}
```

#### Award to Recipient
```
POST /api/awards/{id}/award?recipientId={recipientId}
```

#### Get Awards by Recipient
```
GET /api/awards/recipient/{recipientId}
```

#### Get Awards by Category
```
GET /api/awards/category/{category}
```

#### Get Awards by Status
```
GET /api/awards/status/{status}
```

#### Approve Award
```
POST /api/awards/{id}/approve
```

#### Reject Award
```
POST /api/awards/{id}/reject
```

### Feature Toggle Management

#### Get All Feature Toggles
```
GET /api/feature-toggles
```

#### Get Feature Toggle
```
GET /api/feature-toggles/{featureName}
```

#### Check if Feature is Enabled
```
GET /api/feature-toggles/{featureName}/enabled
```

#### Create/Update Feature Toggle
```
POST /api/feature-toggles
Content-Type: application/json

{
  "featureName": "use-new-flow",
  "enabled": true,
  "description": "Enable new award processing flow"
}
```

#### Enable Feature
```
POST /api/feature-toggles/{featureName}/enable?description={description}
```

#### Disable Feature
```
POST /api/feature-toggles/{featureName}/disable
```

#### Toggle Feature
```
POST /api/feature-toggles/{featureName}/toggle
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MongoDB 4.4+ (running on localhost:27017)

## Setup and Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd award-management-system
   ```

2. **Start MongoDB**
   ```bash
   # Using Docker
   docker run -d -p 27017:27017 --name mongodb mongo:latest
   
   # Or install MongoDB locally
   ```

3. **Configure MongoDB Connection**
   Edit `application.properties`:
   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/award_db
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## Usage Example

### 1. Configure Feature Toggle in application.yml (Recommended)

Edit `src/main/resources/application.yml`:

```yaml
feature:
  toggle:
    db-fallback-enabled: true
    flags:
      use-new-flow: false  # Start with old flow
```

### 2. Create an Award (using OldFlow)

```bash
curl -X POST http://localhost:8080/api/awards \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Employee of the Month",
    "description": "Best performer in January",
    "category": "PERFORMANCE"
  }'
```

### 3. Enable New Flow

**Option A: Update YAML** (requires restart)
```yaml
feature:
  toggle:
    flags:
      use-new-flow: true  # Change to true
```

**Option B: Use REST API** (runtime change, saves to DB)
```bash
curl -X POST http://localhost:8080/api/feature-toggles/use-new-flow/enable
```
*Note: If feature exists in YAML, YAML value takes precedence when reading*

### 4. Create Another Award (using NewFlow)

```bash
curl -X POST http://localhost:8080/api/awards \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Innovation Award",
    "description": "Most innovative solution",
    "category": "INNOVATION"
  }'
```

## Testing

Run the tests with:
```bash
mvn test
```

### Test Suite

The project includes comprehensive testing:

- **Unit Tests**: Service and facade layer logic with Mockito
- **Integration Tests**: REST API and repository tests with Testcontainers
- **45+ Test Cases**: Covering all major functionality

**Prerequisites for Tests:**
- Docker must be running (for Testcontainers)

See [TESTING.md](TESTING.md) for detailed test documentation.

### Test Coverage

- ✅ Service Layer (AwardService, FeatureToggleService)
- ✅ Repository Layer (OldFlow, NewFlow)
- ✅ Controller Layer (REST APIs)
- ✅ Proxy Pattern Verification
- ✅ Feature Toggle Switching

## Project Structure

```
award-management-system/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── award/
│       │               ├── AwardManagementApplication.java
│       │               ├── controller/
│       │               │   ├── AwardController.java
│       │               │   └── FeatureToggleController.java
│       │               ├── service/
│       │               │   └── AwardService.java
│       │               ├── proxy/
│       │               │   └── AwardRepoProxy.java
│       │               ├── facade/
│       │               │   ├── AwardFacade.java
│       │               │   └── FeatureToggleService.java
│       │               ├── repository/
│       │               │   ├── OldFlow.java
│       │               │   ├── NewFlow.java
│       │               │   └── FeatureToggleRepository.java
│       │               └── domain/
│       │                   ├── Award.java
│       │                   └── FeatureToggle.java
│       └── resources/
│           └── application.properties
└── pom.xml
```

## Key Features

- **Feature Toggle**: Runtime switching between OldFlow and NewFlow
- **Zero Downtime Migration**: Switch flows without restarting the application
- **Logging**: Comprehensive logging at each layer
- **Validation**: Input validation in the proxy layer
- **REST API**: Complete CRUD operations for awards
- **MongoDB Integration**: Persistent storage with MongoDB
- **Design Patterns**: Proxy, Facade, and Feature Toggle patterns

## Configuration

### Feature Toggle Configuration

Feature toggles are managed through **application.yml** (primary) with optional database fallback:

#### Simple YAML Configuration (Recommended)

Edit `src/main/resources/application.yml`:

```yaml
feature:
  toggle:
    # Enable/disable DB fallback (if true, checks DB when not found in YAML)
    db-fallback-enabled: true
    
    # Feature flags defined in YAML (simpler and cleaner)
    flags:
      use-new-flow: false
      # Add more feature flags here:
      # new-feature: true
      # another-feature: false
```

**Priority Order:**
1. **application.yml** - Checked first (simplest, cleanest)
2. **Database** - Checked if `db-fallback-enabled: true` and not in YAML
3. **false** - Default if not found anywhere

#### Database Configuration (Optional)

For runtime changes, use REST API or direct MongoDB updates:

```bash
# Enable feature via REST API (saves to DB)
curl -X POST http://localhost:8080/api/feature-toggles/use-new-flow/enable

# Note: If feature exists in YAML, YAML value takes precedence when reading
```

**When to use YAML vs Database:**
- **YAML**: Static flags, environment-specific configs, simple flags
- **Database**: Runtime changes, dynamic flags, A/B testing

## License

MIT License
