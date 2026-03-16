#!/bin/bash

# Buy01 - E-commerce Platform Startup Script

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Flags
BUILD=false
FRONTEND=false

# Parse arguments
for arg in "$@"; do
  case $arg in
    --build|-b)   BUILD=true ;;
    --frontend|-f) FRONTEND=true ;;
    --help|-h)
      echo "Usage: ./run.sh [options]"
      echo "  -b, --build      Rebuild all Docker images before starting"
      echo "  -f, --frontend   Also start the Angular frontend (dev server)"
      echo "  -h, --help       Show this help message"
      exit 0
      ;;
  esac
done

echo -e "${BLUE}╔══════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   🛒  Buy01 E-commerce Platform      ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════╝${NC}"
echo ""

# Navigate to docker-compose directory
cd "$(dirname "$0")/microservices-architecture/docker-compose"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker Desktop first.${NC}"
    exit 1
fi

# Build images if requested or if --build flag is set
if [ "$BUILD" = true ]; then
    echo -e "${YELLOW}📦 Building Docker images (this may take a few minutes)...${NC}"
    docker compose build --no-cache \
        api-gateway \
        user-service \
        product-service \
        media-service \
        order-service
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Docker build failed. Check the errors above.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ All images built successfully!${NC}"
    echo ""
fi

echo -e "${YELLOW}🐳 Starting services...${NC}"
docker compose up -d

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to start services. Check docker compose logs for details.${NC}"
    exit 1
fi

# Wait for services to be healthy
echo -e "${YELLOW}⏳ Waiting for services to be ready...${NC}"
SERVICES=("mongodb" "kafka" "zookeeper")
for svc in "${SERVICES[@]}"; do
    echo -n "   Waiting for $svc... "
    for i in $(seq 1 30); do
        STATUS=$(docker inspect --format='{{.State.Health.Status}}' "$svc" 2>/dev/null)
        if [ "$STATUS" = "healthy" ]; then
            echo -e "${GREEN}healthy${NC}"
            break
        fi
        sleep 2
        if [ $i -eq 30 ]; then
            echo -e "${YELLOW}timeout (may still be starting)${NC}"
        fi
    done
done

echo ""
echo -e "${GREEN}✅ Buy01 platform is up!${NC}"
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "  ${GREEN}Microservices${NC}"
echo "  API Gateway:      https://localhost:8080"
echo "  User Service:     http://localhost:8081"
echo "  Product Service:  http://localhost:8082"
echo "  Media Service:    http://localhost:8083"
echo "  Order Service:    http://localhost:8084"
echo ""
echo -e "  ${GREEN}Infrastructure${NC}"
echo "  MongoDB:          mongodb://localhost:27017"
echo "  Kafka:            localhost:9092"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "  ${YELLOW}Credentials:${NC} admin / password123"
echo -e "  ${YELLOW}API Docs:${NC}     https://localhost:8080/api"
echo ""

# Start frontend if flag is set, otherwise ask
if [ "$FRONTEND" = false ]; then
    read -p "Do you want to start the Angular frontend? (y/n) " -n 1 -r
    echo
    [[ $REPLY =~ ^[Yy]$ ]] && FRONTEND=true
fi

if [ "$FRONTEND" = true ]; then
    cd "../../frontend"

    if [ ! -d "node_modules" ]; then
        echo -e "${YELLOW}📥 Installing frontend dependencies...${NC}"
        npm install
        if [ $? -ne 0 ]; then
            echo -e "${RED}❌ npm install failed.${NC}"
            exit 1
        fi
    fi

    echo -e "${YELLOW}🌐 Starting Angular frontend on http://localhost:4200 ...${NC}"
    npm start
fi
