#!/bin/bash

# SonarQube Analysis Script for Buy01 E-commerce Platform

set -e

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Project root is one level up from scripts directory
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🔍 Starting SonarQube analysis for Buy01 E-commerce Platform..."
echo "📁 Project root: $PROJECT_ROOT"

# Change to project root directory
cd "$PROJECT_ROOT"

# Load token from environment variable or file
if [ -z "$SONAR_TOKEN" ]; then
    if [ -f "$PROJECT_ROOT/.sonarqube-token" ]; then
        SONAR_TOKEN=$(cat "$PROJECT_ROOT/.sonarqube-token")
    else
        echo "❌ SONAR_TOKEN not set. Either:"
        echo "   - Set SONAR_TOKEN environment variable"
        echo "   - Run ./scripts/sonarqube-setup.sh to generate a token"
        exit 1
    fi
fi

SONAR_HOST_URL="${SONAR_HOST_URL:-http://localhost:9000}"

# Function to check if SonarQube is running
check_sonarqube() {
    if ! curl -s $SONAR_HOST_URL/api/system/status > /dev/null; then
        echo "❌ SonarQube is not running. Please start it first: docker-compose -f docker-compose.sonarqube.yml up -d"
        exit 1
    fi
    echo "✅ SonarQube is running"
}

# Function to build Java projects
build_java_projects() {
    echo "🔨 Building Java microservices..."

    for service in api-gateway user-service product-service media-service; do
        if [ -d "microservices-architecture/$service" ]; then
            echo "📦 Building $service..."
            cd "microservices-architecture/$service"

            # Clean and compile
            if [ -f "pom.xml" ]; then
                mvn clean compile test-compile -DskipTests=true
            fi

            cd ../..
        fi
    done
    echo "✅ Java microservices built successfully"
}

# Function to build frontend (if applicable)
build_frontend() {
    if [ -d "frontend" ]; then
        echo "🎨 Building frontend..."
        cd frontend

        # Install dependencies if needed
        if [ -f "package.json" ] && [ ! -d "node_modules" ]; then
            npm install
        fi

        # Build frontend
        if [ -f "package.json" ]; then
            npm run build 2>/dev/null || echo "⚠️ Frontend build failed or no build script found"
        fi

        cd ..
        echo "✅ Frontend processed"
    fi
}

# Function to run SonarQube analysis
run_analysis() {
    echo "🔍 Running SonarQube analysis..."

    # Run analysis using Docker sonar-scanner
    # Note: Using --platform linux/amd64 for Apple Silicon compatibility
    docker run --rm \
        --platform linux/amd64 \
        --network host \
        -v "$(pwd):/usr/src" \
        -w /usr/src \
        sonarsource/sonar-scanner-cli:latest \
        -Dsonar.projectKey=buy01-ecommerce \
        -Dsonar.projectName="Buy01 E-commerce Platform" \
        -Dsonar.sources=microservices-architecture/api-gateway/src/main/java,microservices-architecture/user-service/src/main/java,microservices-architecture/product-service/src/main/java,microservices-architecture/media-service/src/main/java,frontend/src \
        -Dsonar.host.url=$SONAR_HOST_URL \
        -Dsonar.token=$SONAR_TOKEN \
        -Dsonar.java.binaries=microservices-architecture/api-gateway/target/classes,microservices-architecture/user-service/target/classes,microservices-architecture/product-service/target/classes,microservices-architecture/media-service/target/classes \
        -Dsonar.exclusions="**/node_modules/**,**/target/**,**/*.min.js,**/vendor/**,**/dist/**" \
        -Dsonar.java.source=17
}

# Main execution
main() {
    check_sonarqube
    build_java_projects
    build_frontend
    run_analysis

    echo ""
    echo "🎉 SonarQube analysis completed!"
    echo ""
    echo "📊 View results:"
    echo "- Dashboard: $SONAR_HOST_URL/dashboard?id=buy01-ecommerce"
    echo "- Issues: $SONAR_HOST_URL/project/issues?id=buy01-ecommerce"
    echo "- Security: $SONAR_HOST_URL/project/security_hotspots?id=buy01-ecommerce"
    echo ""
}

# Run main function
main