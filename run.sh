#!/bin/bash

echo "=================================================="
echo "Award Management System - Quick Start Script"
echo "=================================================="
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

echo "✅ Maven version: $(mvn -version | head -n 1)"
echo ""

# Check if MongoDB is running
if ! command -v mongo &> /dev/null && ! command -v mongosh &> /dev/null; then
    echo "⚠️  MongoDB client not found. Please ensure MongoDB is running on localhost:27017"
else
    echo "✅ MongoDB client found"
fi

echo ""
echo "Building the application..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "Starting the application..."
    echo "The application will be available at: http://localhost:8080"
    echo ""
    echo "API Documentation:"
    echo "  - Awards API: http://localhost:8080/api/awards"
    echo "  - Feature Toggles API: http://localhost:8080/api/feature-toggles"
    echo ""
    mvn spring-boot:run
else
    echo ""
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi
