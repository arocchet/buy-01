#!/bin/bash

# SonarQube Analysis Script for Buy01 E-commerce Platform

set -e

echo "🔍 Starting SonarQube analysis for Buy01 E-commerce Platform..."

# Check if SonarQube token exists
if [ ! -f .sonarqube-token ]; then
    echo "❌ SonarQube token not found. Please run setup first: ./scripts/sonarqube-setup.sh"
    exit 1
fi

SONAR_TOKEN=$(cat .sonarqube-token)
SONAR_HOST_URL="http://localhost:9000"

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

    # Download SonarQube Scanner if not present
    if [ ! -f "sonar-scanner/bin/sonar-scanner" ]; then
        echo "📥 Downloading SonarQube Scanner..."
        mkdir -p tools
        cd tools
        wget -q https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-5.0.1.3006-linux.zip
        unzip -q sonar-scanner-cli-5.0.1.3006-linux.zip
        mv sonar-scanner-5.0.1.3006-linux sonar-scanner
        cd ..
        echo "✅ SonarQube Scanner downloaded"
    fi

    # Set JAVA_HOME if not set
    if [ -z "$JAVA_HOME" ]; then
        export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
    fi

    # Run analysis using Docker (easier approach)
    docker run --rm \
        --network host \
        -v "$(pwd):/usr/src" \
        sonarsource/sonar-scanner-cli:latest \
        -Dsonar.projectKey=buy01-ecommerce \
        -Dsonar.sources=/usr/src \
        -Dsonar.host.url=$SONAR_HOST_URL \
        -Dsonar.login=$SONAR_TOKEN \
        -Dsonar.java.binaries=/usr/src/microservices-architecture/*/target/classes \
        -Dsonar.exclusions="**/node_modules/**,**/target/**,**/*.min.js,**/vendor/**" \
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