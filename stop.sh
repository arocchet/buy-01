#!/bin/bash

# Buy01 - Stop all services

echo "🛑 Stopping Buy01 services..."

cd "$(dirname "$0")/microservices-architecture/docker-compose"

docker-compose down

echo "✅ All services stopped."
