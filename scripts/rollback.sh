#!/bin/bash

# Buy01 Rollback Script
# This script provides automated rollback capabilities for the Buy01 platform

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BACKUP_DIR="./deployments/backups"
COMPOSE_DIR="./microservices-architecture/docker-compose"
LOG_FILE="./logs/rollback.log"

# Create logs directory if it doesn't exist
mkdir -p logs

# Logging function
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
    log "$message"
}

# Function to show usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Rollback Options:
    -b BUILD_NUMBER    Rollback to specific build number
    -l                 List available backups
    -p                 Rollback to previous deployment
    -e ENVIRONMENT     Target environment (dev|staging|production)
    -h                 Show this help message

Examples:
    $0 -l                           # List available backups
    $0 -b 123 -e staging           # Rollback to build 123 in staging
    $0 -p -e production            # Rollback to previous deployment in production

EOF
}

# Function to list available backups
list_backups() {
    print_status "$BLUE" "📋 Available Backups:"

    if [ ! -d "$BACKUP_DIR" ]; then
        print_status "$RED" "❌ No backup directory found at $BACKUP_DIR"
        return 1
    fi

    echo ""
    echo "Build Number | Date Created     | Environment | Status"
    echo "-------------|------------------|-------------|--------"

    for backup in $(ls -1 "$BACKUP_DIR" 2>/dev/null | sort -nr); do
        if [ -d "$BACKUP_DIR/$backup" ]; then
            local date_created=$(stat -c %y "$BACKUP_DIR/$backup" 2>/dev/null | cut -d' ' -f1,2 | cut -d'.' -f1 2>/dev/null || stat -f %Sm "$BACKUP_DIR/$backup" 2>/dev/null)
            local env_file=""
            local status="✅ Available"

            # Try to determine environment from backup
            if [ -f "$BACKUP_DIR/$backup/docker-compose.prod.yml" ]; then
                env_file="Production"
            elif [ -f "$BACKUP_DIR/$backup/docker-compose.staging.yml" ]; then
                env_file="Staging"
            elif [ -f "$BACKUP_DIR/$backup/docker-compose.dev.yml" ]; then
                env_file="Development"
            else
                env_file="Unknown"
                status="⚠️  Incomplete"
            fi

            printf "%-12s | %-16s | %-11s | %s\n" "$backup" "$date_created" "$env_file" "$status"
        fi
    done
    echo ""
}

# Function to get the previous build number
get_previous_build() {
    local current_deployment_file="$COMPOSE_DIR/.current_deployment"

    if [ -f "$current_deployment_file" ]; then
        local current_build=$(cat "$current_deployment_file")
        local previous_build=$(ls -1 "$BACKUP_DIR" 2>/dev/null | sort -nr | grep -A1 "^$current_build$" | tail -1)

        if [ ! -z "$previous_build" ] && [ "$previous_build" != "$current_build" ]; then
            echo "$previous_build"
        else
            print_status "$RED" "❌ No previous deployment found"
            return 1
        fi
    else
        # If no current deployment file, get the latest backup
        local latest_backup=$(ls -1 "$BACKUP_DIR" 2>/dev/null | sort -nr | head -1)
        if [ ! -z "$latest_backup" ]; then
            echo "$latest_backup"
        else
            print_status "$RED" "❌ No backups available"
            return 1
        fi
    fi
}

# Function to validate backup
validate_backup() {
    local build_number=$1
    local backup_path="$BACKUP_DIR/$build_number"

    if [ ! -d "$backup_path" ]; then
        print_status "$RED" "❌ Backup $build_number not found"
        return 1
    fi

    # Check for required files
    local required_files=("docker-compose.yml")

    for file in "${required_files[@]}"; do
        if [ ! -f "$backup_path/$file" ]; then
            print_status "$RED" "❌ Required file $file not found in backup $build_number"
            return 1
        fi
    done

    print_status "$GREEN" "✅ Backup $build_number validation passed"
    return 0
}

# Function to create current state backup before rollback
create_pre_rollback_backup() {
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_path="$BACKUP_DIR/pre_rollback_$timestamp"

    print_status "$YELLOW" "📦 Creating pre-rollback backup..."

    mkdir -p "$backup_path"
    cp -r "$COMPOSE_DIR"/* "$backup_path/" 2>/dev/null || true

    print_status "$GREEN" "✅ Pre-rollback backup created at $backup_path"
}

# Function to stop current services
stop_services() {
    local environment=$1

    print_status "$YELLOW" "🛑 Stopping current services..."

    cd "$COMPOSE_DIR"

    case $environment in
        "dev")
            docker-compose -f docker-compose.yml -f docker-compose.dev.yml down
            ;;
        "staging")
            docker-compose -f docker-compose.yml -f docker-compose.staging.yml down
            ;;
        "production")
            docker-compose -f docker-compose.yml -f docker-compose.prod.yml down
            ;;
        *)
            docker-compose down
            ;;
    esac

    print_status "$GREEN" "✅ Services stopped"
    cd - > /dev/null
}

# Function to restore backup
restore_backup() {
    local build_number=$1
    local backup_path="$BACKUP_DIR/$build_number"

    print_status "$YELLOW" "🔄 Restoring backup $build_number..."

    # Copy backup files to compose directory
    cp -r "$backup_path"/* "$COMPOSE_DIR/"

    print_status "$GREEN" "✅ Backup $build_number restored"
}

# Function to start services
start_services() {
    local environment=$1

    print_status "$YELLOW" "🚀 Starting services for $environment environment..."

    cd "$COMPOSE_DIR"

    case $environment in
        "dev")
            docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
            ;;
        "staging")
            docker-compose -f docker-compose.yml -f docker-compose.staging.yml up -d
            ;;
        "production")
            docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
            ;;
        *)
            docker-compose up -d
            ;;
    esac

    cd - > /dev/null
    print_status "$GREEN" "✅ Services started"
}

# Function to verify rollback
verify_rollback() {
    local environment=$1

    print_status "$YELLOW" "🏥 Verifying rollback..."

    # Health check endpoints
    local services=(
        "API Gateway:http://localhost:8080/actuator/health"
        "User Service:http://localhost:8081/actuator/health"
        "Product Service:http://localhost:8082/actuator/health"
        "Media Service:http://localhost:8083/actuator/health"
    )

    sleep 30  # Wait for services to start

    local failed_checks=0

    for service_info in "${services[@]}"; do
        local service_name=$(echo "$service_info" | cut -d':' -f1)
        local health_url=$(echo "$service_info" | cut -d':' -f2-)

        print_status "$BLUE" "Checking $service_name..."

        local max_attempts=5
        local attempt=1

        while [ $attempt -le $max_attempts ]; do
            if curl -f "$health_url" > /dev/null 2>&1; then
                print_status "$GREEN" "✅ $service_name is healthy"
                break
            else
                if [ $attempt -eq $max_attempts ]; then
                    print_status "$RED" "❌ $service_name health check failed"
                    ((failed_checks++))
                else
                    print_status "$YELLOW" "⏳ $service_name not ready, retrying... ($attempt/$max_attempts)"
                    sleep 10
                fi
            fi
            ((attempt++))
        done
    done

    if [ $failed_checks -eq 0 ]; then
        print_status "$GREEN" "✅ Rollback verification passed - All services are healthy"
        return 0
    else
        print_status "$RED" "❌ Rollback verification failed - $failed_checks services are unhealthy"
        return 1
    fi
}

# Function to update deployment tracking
update_deployment_tracking() {
    local build_number=$1
    echo "$build_number" > "$COMPOSE_DIR/.current_deployment"
    print_status "$GREEN" "✅ Deployment tracking updated to build $build_number"
}

# Function to perform rollback
perform_rollback() {
    local build_number=$1
    local environment=$2

    print_status "$BLUE" "🔄 Starting rollback to build $build_number in $environment environment"

    # Validate backup
    if ! validate_backup "$build_number"; then
        return 1
    fi

    # Create pre-rollback backup
    create_pre_rollback_backup

    # Stop current services
    stop_services "$environment"

    # Restore backup
    restore_backup "$build_number"

    # Start services
    start_services "$environment"

    # Verify rollback
    if verify_rollback "$environment"; then
        update_deployment_tracking "$build_number"
        print_status "$GREEN" "🎉 Rollback completed successfully!"

        # Send notification (if notification script exists)
        if [ -f "./scripts/send-notification.sh" ]; then
            ./scripts/send-notification.sh "✅ Rollback completed successfully to build $build_number in $environment"
        fi

        return 0
    else
        print_status "$RED" "❌ Rollback verification failed"
        return 1
    fi
}

# Main script logic
main() {
    local build_number=""
    local environment="dev"
    local list_backups_flag=false
    local previous_deployment=false

    # Parse command line arguments
    while getopts "b:e:lph" opt; do
        case $opt in
            b) build_number="$OPTARG" ;;
            e) environment="$OPTARG" ;;
            l) list_backups_flag=true ;;
            p) previous_deployment=true ;;
            h) usage; exit 0 ;;
            *) usage; exit 1 ;;
        esac
    done

    # Validate environment
    if [[ ! "$environment" =~ ^(dev|staging|production)$ ]]; then
        print_status "$RED" "❌ Invalid environment: $environment"
        print_status "$YELLOW" "Valid environments: dev, staging, production"
        exit 1
    fi

    # Handle list backups
    if [ "$list_backups_flag" = true ]; then
        list_backups
        exit 0
    fi

    # Handle previous deployment rollback
    if [ "$previous_deployment" = true ]; then
        build_number=$(get_previous_build)
        if [ $? -ne 0 ]; then
            exit 1
        fi
        print_status "$BLUE" "🔍 Found previous deployment: build $build_number"
    fi

    # Validate build number
    if [ -z "$build_number" ]; then
        print_status "$RED" "❌ Build number is required"
        usage
        exit 1
    fi

    # Confirm rollback
    print_status "$YELLOW" "⚠️  You are about to rollback to build $build_number in $environment environment"
    echo -n "Are you sure you want to continue? (y/N): "
    read -r confirmation

    if [[ ! "$confirmation" =~ ^[Yy]$ ]]; then
        print_status "$BLUE" "ℹ️  Rollback cancelled"
        exit 0
    fi

    # Perform rollback
    if perform_rollback "$build_number" "$environment"; then
        exit 0
    else
        exit 1
    fi
}

# Run main function with all arguments
main "$@"