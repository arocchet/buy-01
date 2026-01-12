#!/bin/bash

echo "🚀 Starting Jenkins for Buy01 CI/CD..."

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

# Navigate to jenkins directory
cd "$(dirname "$0")"

echo -e "${YELLOW}📦 Starting Jenkins container...${NC}"
docker-compose up -d

echo -e "${GREEN}✅ Jenkins started successfully!${NC}"
echo ""
echo "Jenkins is available at: http://localhost:8090"
echo ""
echo "To get the initial admin password, run:"
echo "docker exec jenkins-buy01 cat /var/jenkins_home/secrets/initialAdminPassword"
echo ""
echo "Waiting for Jenkins to be ready..."
sleep 30

# Get initial admin password
echo -e "${YELLOW}📋 Initial Admin Password:${NC}"
docker exec jenkins-buy01 cat /var/jenkins_home/secrets/initialAdminPassword