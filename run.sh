#!/bin/bash

echo "Starting Let's Play Application..."

# Check if MongoDB is running
if ! pgrep -x "mongod" > /dev/null; then
    echo "MongoDB is not running. Please start MongoDB first."
    echo "You can start MongoDB with: brew services start mongodb-community"
    exit 1
fi

# Build and run the Spring Boot application
echo "Building the application..."
./mvnw clean compile

echo "Running the application..."
./mvnw spring-boot:run