# Award Management System - Quick Start Guide

## 📦 What's Included

This ZIP file contains a complete, production-ready Spring Boot application with:

- ✅ **19 Java Classes** (Domain, Repository, Service, Controller)
- ✅ **45+ Unit & Integration Tests** with Testcontainers
- ✅ **Comprehensive Documentation** (README, Architecture, Testing, Proxy Pattern)
- ✅ **Postman Collection** for API testing
- ✅ **Maven Build Configuration** with all dependencies
- ✅ **Docker-ready** with Testcontainers support

## 🚀 Quick Start (5 Minutes)

### Step 1: Prerequisites

Ensure you have:
```bash
# Java 17 or higher
java -version

# Maven 3.6+
mvn -version

# MongoDB (running on localhost:27017)
# OR use Docker:
docker run -d -p 27017:27017 --name mongodb mongo:7.0

# Docker (for tests)
docker ps
```

### Step 2: Extract and Build

```bash
# Extract the ZIP
unzip award-management-system.zip
cd award-management-system

# Build the project
mvn clean install

# This will:
# - Compile all Java code
# - Download dependencies
# - Run 45+ tests with Testcontainers
# - Package the application
```

### Step 3: Run the Application

```bash
# Option 1: Using the run script
chmod +x run.sh
./run.sh

# Option 2: Using Maven
mvn spring-boot:run

# Option 3: Using the JAR
java -jar target/award-management-system-1.0.0.jar
```

The application will start on `http://localhost:8080`

### Step 4: Test the API

```bash
# Create a feature toggle (using OldFlow initially)
curl -X POST http://localhost:8080/api/feature-toggles \
  -H "Content-Type: application/json" \
  -d '{
    "featureName": "use-new-flow",
    "enabled": false,
    "description": "Toggle between old and new flow"
  }'

# Create an award (goes to OldFlow)
curl -X POST http://localhost:8080/api/awards \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Employee of the Month",
    "description": "Best performer",
    "category": "PERFORMANCE"
  }'

# Enable NewFlow
curl -X POST http://localhost:8080/api/feature-toggles/use-new-flow/enable

# Create another award (goes to NewFlow)
curl -X POST http://localhost:8080/api/awards \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Innovation Award",
    "description": "Best innovation",
    "category": "INNOVATION"
  }'

# Get all awards
curl http://localhost:8080/api/awards
```

## 🧪 Running Tests

### Run All Tests (45+ tests)
```bash
mvn test
```

**Note:** Tests use Testcontainers which requires Docker to be running.

### First Test Run
The first time you run tests, Docker will download the MongoDB image (~200MB). Subsequent runs are fast.

```bash
# Pre-download the image (optional)
docker pull mongo:7.0
```

### Test Output
```
[INFO] Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## 📁 Project Structure

```
award-management-system/
├── src/main/java/
│   └── com/example/award/
│       ├── domain/              # Award, FeatureToggle entities
│       ├── repository/          # OldFlow, NewFlow repositories
│       ├── facade/              # AwardFacade, FeatureToggleService
│       ├── proxy/               # AwardRepoProxy (Traffic Cop)
│       ├── service/             # AwardService (Business Logic)
│       ├── controller/          # REST Controllers
│       └── config/              # Configuration classes
├── src/test/java/               # 45+ unit & integration tests
├── src/main/resources/          # application.properties
├── pom.xml                      # Maven configuration
├── README.md                    # Full documentation
├── ARCHITECTURE.md              # Architecture details
├── PROXY_CONFIGURATION.md       # Proxy pattern explanation
├── TESTING.md                   # Test documentation
└── postman_collection.json      # API testing collection
```

## 🎯 Key Features

### 1. Transparent Proxy Pattern
Service layer code doesn't change when switching flows:

```java
@Service
public class AwardService {
    @Autowired
    public AwardService(OldFlow repository) {
        // AwardRepoProxy is injected automatically!
        // Service doesn't know about the proxy
    }
}
```

### 2. Runtime Flow Switching
No code deployment needed:

```bash
# Switch to NewFlow
curl -X POST http://localhost:8080/api/feature-toggles/use-new-flow/enable

# Switch to OldFlow  
curl -X POST http://localhost:8080/api/feature-toggles/use-new-flow/disable
```

### 3. Comprehensive Testing
- **Unit Tests**: Mockito-based tests for service logic
- **Integration Tests**: Testcontainers with real MongoDB
- **API Tests**: Full REST endpoint testing

## 📚 Documentation

- **[README.md](README.md)** - Complete project documentation
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture and design patterns
- **[PROXY_CONFIGURATION.md](PROXY_CONFIGURATION.md)** - Detailed proxy pattern explanation
- **[TESTING.md](TESTING.md)** - Test suite documentation

## 🔧 Configuration

### MongoDB Connection
Edit `src/main/resources/application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/award_db
```

### Application Port
```properties
server.port=8080
```

## 📮 API Documentation

Import `postman_collection.json` into Postman for complete API documentation with example requests.

### Main Endpoints

**Awards:**
- `POST /api/awards` - Create award
- `GET /api/awards` - Get all awards
- `GET /api/awards/{id}` - Get award by ID
- `PUT /api/awards/{id}` - Update award
- `DELETE /api/awards/{id}` - Delete award

**Feature Toggles:**
- `POST /api/feature-toggles` - Create/update toggle
- `GET /api/feature-toggles` - Get all toggles
- `POST /api/feature-toggles/{name}/enable` - Enable feature
- `POST /api/feature-toggles/{name}/disable` - Disable feature

## 🐛 Troubleshooting

### MongoDB Connection Error
```
Error: MongoTimeoutException
Solution: 
1. Ensure MongoDB is running: docker ps
2. Start MongoDB: docker run -d -p 27017:27017 --name mongodb mongo:7.0
```

### Tests Fail - Docker Not Running
```
Error: Could not find a valid Docker environment
Solution: Start Docker Desktop or Docker daemon
```

### Port 8080 Already in Use
```
Error: Port 8080 is already in use
Solution: 
1. Stop conflicting application
2. Or change port in application.properties
```

## 💡 Next Steps

1. **Explore the Code**
   - Start with `AwardService.java` - the business logic
   - Check `AwardRepoProxy.java` - the traffic cop
   - Review `AwardFacade.java` - the decision maker

2. **Run the Tests**
   - See how the proxy pattern works in `ProxyPatternIntegrationTest.java`
   - Check repository tests for MongoDB operations

3. **Customize**
   - Add more repository methods
   - Extend the domain model
   - Add new endpoints

## 📞 Support

For issues or questions:
1. Check the documentation files (README, ARCHITECTURE, etc.)
2. Review the test cases for examples
3. Examine the Postman collection for API usage

## ✅ Verification Checklist

- [ ] Java 17+ installed
- [ ] Maven 3.6+ installed
- [ ] Docker running (for tests)
- [ ] MongoDB running (for application)
- [ ] Project builds: `mvn clean install`
- [ ] Tests pass: `mvn test`
- [ ] Application starts: `mvn spring-boot:run`
- [ ] API responds: `curl http://localhost:8080/api/awards`

---

**You're all set! The application is ready to run.** 🎉
