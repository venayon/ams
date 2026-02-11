# Proxy Configuration - How It Works

## Spring Bean Configuration

```
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Application Context                    │
│                                                                  │
│  Bean Name: "awardService"                                      │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  @Service                                               │    │
│  │  public class AwardService {                           │    │
│  │      @Autowired                                        │    │
│  │      public AwardService(OldFlow awardRepository) {    │    │
│  │          // Spring sees: "I need an OldFlow bean"     │    │
│  │          // Spring finds TWO beans implementing OldFlow:│    │
│  │          //   1. awardRepoProxy (@Primary)            │    │
│  │          //   2. oldFlow                               │    │
│  │          // Spring chooses: awardRepoProxy (due to @Primary)│
│  │      }                                                 │    │
│  │  }                                                     │    │
│  └────────────────────────────────────────────────────────┘    │
│                          ↓ receives                             │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Bean Name: "awardRepoProxy" (@Primary)                │    │
│  │  ┌──────────────────────────────────────────────────┐  │    │
│  │  │  @Repository                                      │  │    │
│  │  │  @Primary  ← KEY ANNOTATION!                     │  │    │
│  │  │  @Qualifier("awardRepoProxy")                    │  │    │
│  │  │  public class AwardRepoProxy implements OldFlow { │  │    │
│  │  │      @Autowired                                   │  │    │
│  │  │      public AwardRepoProxy(AwardFacade facade) { │  │    │
│  │  │          // Injects awardFacade                  │  │    │
│  │  │      }                                            │  │    │
│  │  │                                                   │  │    │
│  │  │      public Award save(Award award) {            │  │    │
│  │  │          // Validation & logging                 │  │    │
│  │  │          return facade.save(award);              │  │    │
│  │  │      }                                            │  │    │
│  │  │  }                                                │  │    │
│  │  └──────────────────────────────────────────────────┘  │    │
│  └────────────────────────────────────────────────────────┘    │
│                          ↓ delegates to                         │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Bean Name: "awardFacade"                              │    │
│  │  ┌──────────────────────────────────────────────────┐  │    │
│  │  │  @Component                                       │  │    │
│  │  │  public class AwardFacade {                      │  │    │
│  │  │      @Autowired                                   │  │    │
│  │  │      public AwardFacade(                         │  │    │
│  │  │          @Qualifier("oldFlow") OldFlow oldRepo, │  │    │
│  │  │          @Qualifier("newFlow") NewFlow newRepo, │  │    │
│  │  │          FeatureToggleService toggleService) {   │  │    │
│  │  │          // Explicitly requests specific beans   │  │    │
│  │  │      }                                            │  │    │
│  │  │                                                   │  │    │
│  │  │      private MongoRepository determineFlow() {   │  │    │
│  │  │          if (toggleService.isEnabled("use-new")) │  │    │
│  │  │              return newRepo; // Bean: newFlow    │  │    │
│  │  │          else                                     │  │    │
│  │  │              return oldRepo; // Bean: oldFlow    │  │    │
│  │  │      }                                            │  │    │
│  │  │  }                                                │  │    │
│  │  └──────────────────────────────────────────────────┘  │    │
│  └────────────────────────────────────────────────────────┘    │
│                   ↓ routes to (based on toggle)                │
│        ┌──────────────────────┬──────────────────────┐         │
│        ↓                      ↓                       │         │
│  ┌──────────────┐      ┌──────────────┐              │         │
│  │ Bean: oldFlow│      │ Bean: newFlow│              │         │
│  │              │      │              │              │         │
│  │ @Repository  │      │ @Repository  │              │         │
│  │ @Qualifier   │      │ @Qualifier   │              │         │
│  │ ("oldFlow")  │      │ ("newFlow")  │              │         │
│  │              │      │              │              │         │
│  │ OldFlow      │      │ NewFlow      │              │         │
│  │ (interface)  │      │ (interface)  │              │         │
│  │ extends      │      │ extends      │              │         │
│  │ MongoRepo    │      │ MongoRepo    │              │         │
│  └──────────────┘      └──────────────┘              │         │
│                                                       │         │
└───────────────────────────────────────────────────────┘         
```

## Bean Resolution Process

When Spring initializes `AwardService`:

```
Step 1: @Autowired OldFlow parameter needed
        ↓
Step 2: Find all beans of type OldFlow
        Found:
        - awardRepoProxy (type: AwardRepoProxy implements OldFlow, @Primary: YES)
        - oldFlow (type: OldFlow, @Primary: NO, @Qualifier: "oldFlow")
        ↓
Step 3: Multiple candidates found, check for @Primary
        ↓
Step 4: Select awardRepoProxy (has @Primary annotation)
        ↓
Step 5: Inject awardRepoProxy into AwardService
```

When Spring initializes `AwardFacade`:

```
Step 1: @Qualifier("oldFlow") OldFlow parameter needed
        ↓
Step 2: Find bean with qualifier "oldFlow"
        ↓
Step 3: Found: oldFlow bean (the actual repository)
        ↓
Step 4: @Qualifier("newFlow") NewFlow parameter needed
        ↓
Step 5: Find bean with qualifier "newFlow"
        ↓
Step 6: Found: newFlow bean (the actual repository)
        ↓
Step 7: Inject both actual repositories into AwardFacade
```

## Key Points

1. **@Primary Annotation**: Makes AwardRepoProxy the default when OldFlow is requested
2. **@Qualifier Annotation**: Allows explicit selection of specific beans
3. **Service Layer**: Knows nothing about the proxy - clean separation!
4. **Facade Layer**: Uses @Qualifier to get actual repository beans
5. **Runtime Switching**: Feature toggle controls which repository the facade uses

## Code Comparison

### Without Proxy Pattern (Bad):
```java
@Service
public class AwardService {
    @Autowired
    private AwardFacade facade;  // Service directly depends on facade
    
    public Award create(Award award) {
        // Business logic mixed with flow selection
        if (featureToggle.isEnabled()) {
            return newFlow.save(award);
        } else {
            return oldFlow.save(award);
        }
    }
}
```

### With Proxy Pattern (Good):
```java
@Service
public class AwardService {
    @Autowired
    private OldFlow repository;  // Service depends on interface
                                // Gets proxy automatically!
    
    public Award create(Award award) {
        // Clean business logic
        // Flow selection happens transparently
        return repository.save(award);
    }
}
```

## Benefits

✅ **No Service Layer Changes**: Service code remains unchanged
✅ **Transparent Proxying**: Proxy is invisible to service layer
✅ **Easy Testing**: Can mock OldFlow interface in tests
✅ **Clean Architecture**: Each layer has single responsibility
✅ **Runtime Flexibility**: Switch flows without code deployment
