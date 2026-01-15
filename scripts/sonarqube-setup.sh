#!/bin/bash

# SonarQube Setup Script for Buy01 E-commerce Platform

set -e

echo "🚀 Starting SonarQube setup for Buy01 E-commerce Platform..."

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo "❌ Docker is not running. Please start Docker and try again."
        exit 1
    fi
    echo "✅ Docker is running"
}

# Function to start SonarQube
start_sonarqube() {
    echo "🐳 Starting SonarQube with Docker Compose..."
    docker-compose -f docker-compose.sonarqube.yml up -d

    echo "⏳ Waiting for SonarQube to be ready..."
    timeout=300
    counter=0

    while [ $counter -lt $timeout ]; do
        if curl -s http://localhost:9000/api/system/status | grep -q '"status":"UP"'; then
            echo "✅ SonarQube is ready!"
            break
        fi
        echo "⏳ SonarQube is starting... ($counter/$timeout seconds)"
        sleep 5
        counter=$((counter + 5))
    done

    if [ $counter -ge $timeout ]; then
        echo "❌ SonarQube failed to start within $timeout seconds"
        exit 1
    fi
}

# Function to configure SonarQube project
configure_project() {
    echo "🔧 Configuring SonarQube project..."

    # Wait a bit more for SonarQube to be fully ready
    sleep 10

    # Create project using REST API (default admin/admin credentials)
    curl -u admin:admin -X POST "http://localhost:9000/api/projects/create" \
        -d "project=buy01-ecommerce&name=Buy01%20E-commerce%20Platform"

    echo "✅ Project configured successfully"
}

# Function to generate authentication token
generate_token() {
    echo "🔑 Generating authentication token..."

    TOKEN_RESPONSE=$(curl -s -u admin:admin -X POST "http://localhost:9000/api/user_tokens/generate" \
        -d "name=ci-token")

    TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

    if [ -n "$TOKEN" ]; then
        echo "✅ Token generated successfully"
        echo "📝 Save this token for CI/CD: $TOKEN"
        echo "$TOKEN" > .sonarqube-token
        echo "💾 Token saved to .sonarqube-token file"
    else
        echo "⚠️ Could not generate token automatically. Please generate manually from SonarQube UI"
    fi
}

# Main execution
main() {
    check_docker
    start_sonarqube
    configure_project
    generate_token

    echo ""
    echo "🎉 SonarQube setup completed successfully!"
    echo ""
    echo "📋 Next steps:"
    echo "1. Open SonarQube UI: http://localhost:9000"
    echo "2. Login with admin/admin (change password on first login)"
    echo "3. Review project settings in SonarQube UI"
    echo "4. Run analysis with: ./scripts/run-sonar-analysis.sh"
    echo ""
    echo "🔗 Useful links:"
    echo "- SonarQube UI: http://localhost:9000"
    echo "- Project dashboard: http://localhost:9000/dashboard?id=buy01-ecommerce"
    echo ""
}

# Run main function
main