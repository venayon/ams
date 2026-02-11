# Test Suite Documentation

## Overview

This project includes comprehensive unit and integration tests using modern testing practices.

## Test Technologies

- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework for unit tests
- **Testcontainers**: Real MongoDB instances for integration tests
- **Spring Boot Test**: Testing support for Spring applications
- **MockMvc**: REST API testing

## Test Structure

```
src/test/java/
├── config/
│   ├── TestContainersConfiguration.java    # Testcontainers setup
│   └── ProxyPatternIntegrationTest.java    # Proxy pattern verification
├── service/
│   └── AwardServiceTest.java               # Unit tests for service layer
├── facade/
│   └── FeatureToggleServiceTest.java       # Unit tests for toggle service
├── repository/
│   ├── OldFlowRepositoryIntegrationTest.java
│   └── NewFlowRepositoryIntegrationTest.java
└── controller/
    ├── AwardControllerIntegrationTest.java
    └── FeatureToggleControllerIntegrationTest.java
```

## Test Categories

### Unit Tests
- **AwardServiceTest**: Tests business logic in isolation with mocked dependencies
- **FeatureToggleServiceTest**: Tests feature toggle management logic

### Integration Tests
- **Repository Tests**: Test MongoDB operations with real database
- **Controller Tests**: Test REST API endpoints end-to-end
- **ProxyPatternIntegrationTest**: Verifies the proxy pattern configuration

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AwardServiceTest
```

### Run Integration Tests Only
```bash
mvn test -Dtest=*IntegrationTest
```

### Run Unit Tests Only
```bash
mvn test -Dtest=*Test
```

### Run with Coverage
```bash
mvn clean test jacoco:report
```

## Testcontainers Setup

### Prerequisites
- Docker must be installed and running
- Docker daemon must be accessible

### How It Works

1. **Automatic Container Management**
   - Testcontainers automatically downloads MongoDB Docker image
   - Starts container before tests
   - Stops and removes container after tests

2. **Dynamic Configuration**
   - MongoDB URI is configured dynamically
   - Each test gets a clean database state

3. **Container Reuse**
   - Containers are reused across test runs for performance
   - Set `mongoDBContainer.withReuse(true)` in TestContainersConfiguration

### Docker Requirements

```bash
# Verify Docker is running
docker ps

# Pull MongoDB image (optional - automatic)
docker pull mongo:7.0
```

## Test Coverage

### Unit Tests Cover:
✅ Business logic validation
✅ Error handling
✅ Edge cases
✅ State management
✅ Method behavior

### Integration Tests Cover:
✅ Database operations
✅ REST API endpoints
✅ Feature toggle switching
✅ Proxy pattern behavior
✅ End-to-end workflows

## Test Data Management

### BeforeEach Setup
- Clean database state
- Create test fixtures
- Initialize feature toggles

### AfterEach Cleanup
- Delete test data
- Reset database state

## Example Test Execution

```bash
# Terminal output
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.award.service.AwardServiceTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Running com.example.award.controller.AwardControllerIntegrationTest
2024-02-10 Creating container for image: mongo:7.0
2024-02-10 Container mongo:7.0 is starting
2024-02-10 Container mongo:7.0 started in PT2.134S
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## Troubleshooting

### Docker Not Running
```
Error: Could not find a valid Docker environment
Solution: Start Docker daemon
```

### Port Conflicts
```
Error: Port 27017 already in use
Solution: Testcontainers uses random ports automatically
```

### Slow Tests
```
Issue: First test run downloads Docker images
Solution: 
- Pre-pull images: docker pull mongo:7.0
- Enable container reuse
```

## Best Practices

1. **Isolation**: Each test is independent
2. **Clean State**: Database cleaned before each test
3. **Descriptive Names**: Test methods describe what they test
4. **Arrange-Act-Assert**: Clear test structure
5. **Mock External Dependencies**: Only in unit tests
6. **Real Database**: For integration tests

## CI/CD Integration

The tests are designed to run in CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Run tests
  run: mvn test
```

The Testcontainers will automatically work in CI environments that support Docker.
