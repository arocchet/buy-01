#!/bin/bash

# Buy01 - Stop all services

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

VOLUMES=false
for arg in "$@"; do
  case $arg in
    --volumes|-v) VOLUMES=true ;;
    --help|-h)
      echo "Usage: ./stop.sh [options]"
      echo "  -v, --volumes   Also remove persistent volumes (MongoDB data, media uploads)"
      exit 0
      ;;
  esac
done

echo -e "${YELLOW}🛑 Stopping Buy01 services...${NC}"

cd "$(dirname "$0")/microservices-architecture/docker-compose"

if [ "$VOLUMES" = true ]; then
    echo -e "${YELLOW}⚠️  Removing volumes (all data will be lost)...${NC}"
    docker compose down -v
else
    docker compose down
fi

echo -e "${GREEN}✅ All services stopped.${NC}"
if [ "$VOLUMES" = false ]; then
    echo "   Tip: run './stop.sh --volumes' to also wipe MongoDB data and media uploads."
fi
