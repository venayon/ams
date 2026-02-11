# Award Management System

A Spring Boot application demonstrating the Proxy and Facade design patterns with feature toggle functionality for seamless migration between old and new data flows.

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
@Primary  // Makes Spring inject this instead of actual OldFlow
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
- Enables/disables new flow without code changes
- Stored in MongoDB for runtime configuration

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

### 1. Create the Feature Toggle

```bash
curl -X POST http://localhost:8080/api/feature-toggles \
  -H "Content-Type: application/json" \
  -d '{
    "featureName": "use-new-flow",
    "enabled": false,
    "description": "Toggle between old and new award processing flow"
  }'
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

```bash
curl -X POST http://localhost:8080/api/feature-toggles/use-new-flow/enable
```

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

The feature toggle can be managed through:
1. REST API endpoints
2. Direct MongoDB updates
3. Application startup initialization

## License

MIT License
