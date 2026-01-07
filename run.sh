#!/bin/bash

# Buy01 - E-commerce Platform Startup Script

echo "🚀 Starting Buy01 E-commerce Platform..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Navigate to docker-compose directory
cd "$(dirname "$0")/microservices-architecture/docker-compose"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

echo -e "${YELLOW}📦 Building Docker images...${NC}"
docker-compose build

echo -e "${YELLOW}🐳 Starting services...${NC}"
docker-compose up -d

echo -e "${GREEN}✅ Backend services started!${NC}"
echo ""
echo "Services running:"
echo "  - API Gateway:     http://localhost:8080"
echo "  - User Service:    http://localhost:8081"
echo "  - Product Service: http://localhost:8082"
echo "  - Media Service:   http://localhost:8083"
echo "  - MongoDB:         http://localhost:27017"
echo "  - Kafka:           http://localhost:9092"
echo ""

# Ask if user wants to start frontend
read -p "Do you want to start the Angular frontend? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    cd "../../frontend"
    
    # Check if node_modules exists
    if [ ! -d "node_modules" ]; then
        echo -e "${YELLOW}📥 Installing frontend dependencies...${NC}"
        npm install
    fi
    
    echo -e "${YELLOW}🌐 Starting Angular frontend...${NC}"
    npm start
fi
