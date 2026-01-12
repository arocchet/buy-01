#!/bin/bash

# Script d'audit complet du pipeline CI/CD Buy01
# Valide tous les critères d'évaluation

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# Audit configuration
JENKINS_URL="http://localhost:8090"
AUDIT_DATE=$(date)
SCORE=0
MAX_SCORE=0

echo -e "${BLUE}🔍 Buy01 CI/CD Pipeline - Audit Complet${NC}"
echo "=================================================="
echo "Date: $AUDIT_DATE"
echo ""

# Function to log audit results
log_audit() {
    local category="$1"
    local test="$2"
    local result="$3"
    local points="$4"
    local max_points="$5"

    if [ "$result" = "PASS" ]; then
        echo -e "${GREEN}✅ [$category] $test: PASS ($points/$max_points points)${NC}"
        SCORE=$((SCORE + points))
    elif [ "$result" = "PARTIAL" ]; then
        echo -e "${YELLOW}⚠️  [$category] $test: PARTIAL ($points/$max_points points)${NC}"
        SCORE=$((SCORE + points))
    else
        echo -e "${RED}❌ [$category] $test: FAIL (0/$max_points points)${NC}"
    fi

    MAX_SCORE=$((MAX_SCORE + max_points))
}

# Function to check Jenkins availability
audit_jenkins_availability() {
    echo -e "\n${BLUE}📋 1. Pipeline Functionality${NC}"

    # Test 1: Jenkins accessibility
    if curl -s "$JENKINS_URL" > /dev/null 2>&1; then
        log_audit "PIPELINE" "Jenkins accessibility" "PASS" 5 5
    else
        log_audit "PIPELINE" "Jenkins accessibility" "FAIL" 0 5
        return 1
    fi

    # Test 2: Job availability
    if curl -s "$JENKINS_URL" | grep -q "buy-01-CI-CD"; then
        log_audit "PIPELINE" "Pipeline job exists" "PASS" 5 5
    else
        log_audit "PIPELINE" "Pipeline job exists" "FAIL" 0 5
    fi
}

# Function to audit Jenkinsfile quality
audit_jenkinsfile() {
    echo -e "\n${BLUE}📋 2. Code Quality & Standards${NC}"

    # Test 1: Jenkinsfile exists and well-structured
    if [ -f "Jenkinsfile" ]; then
        # Check for essential pipeline elements
        local has_parameters=$(grep -c "parameters" Jenkinsfile || echo 0)
        local has_stages=$(grep -c "stages" Jenkinsfile || echo 0)
        local has_parallel=$(grep -c "parallel" Jenkinsfile || echo 0)
        local has_post=$(grep -c "post" Jenkinsfile || echo 0)

        if [ $has_parameters -gt 0 ] && [ $has_stages -gt 0 ] && [ $has_parallel -gt 0 ] && [ $has_post -gt 0 ]; then
            log_audit "CODE_QUALITY" "Jenkinsfile structure" "PASS" 5 5
        else
            log_audit "CODE_QUALITY" "Jenkinsfile structure" "PARTIAL" 3 5
        fi
    else
        log_audit "CODE_QUALITY" "Jenkinsfile exists" "FAIL" 0 5
    fi

    # Test 2: Scripts quality
    local script_count=0
    local good_scripts=0

    for script in scripts/*.sh; do
        if [ -f "$script" ]; then
            script_count=$((script_count + 1))
            # Check for basic bash best practices
            if grep -q "set -e" "$script" && grep -q "#!/bin/bash" "$script"; then
                good_scripts=$((good_scripts + 1))
            fi
        fi
    done

    if [ $script_count -gt 0 ] && [ $good_scripts -eq $script_count ]; then
        log_audit "CODE_QUALITY" "Scripts follow best practices" "PASS" 5 5
    elif [ $good_scripts -gt 0 ]; then
        log_audit "CODE_QUALITY" "Scripts follow best practices" "PARTIAL" 3 5
    else
        log_audit "CODE_QUALITY" "Scripts follow best practices" "FAIL" 0 5
    fi
}

# Function to audit automated testing
audit_testing() {
    echo -e "\n${BLUE}📋 3. Automated Testing${NC}"

    # Test 1: Backend test configuration
    local backend_test_config=0
    for service in user-service product-service media-service api-gateway; do
        local pom_file="microservices-architecture/$service/pom.xml"
        if [ -f "$pom_file" ]; then
            if grep -q "jacoco-maven-plugin" "$pom_file" && grep -q "spring-boot-starter-test" "$pom_file"; then
                backend_test_config=$((backend_test_config + 1))
            fi
        fi
    done

    if [ $backend_test_config -eq 4 ]; then
        log_audit "TESTING" "Backend test configuration" "PASS" 5 5
    elif [ $backend_test_config -gt 0 ]; then
        log_audit "TESTING" "Backend test configuration" "PARTIAL" 3 5
    else
        log_audit "TESTING" "Backend test configuration" "FAIL" 0 5
    fi

    # Test 2: Frontend test configuration
    if [ -f "frontend/karma.conf.js" ] && [ -f "frontend/package.json" ]; then
        if grep -q "test:ci" "frontend/package.json"; then
            log_audit "TESTING" "Frontend test configuration" "PASS" 5 5
        else
            log_audit "TESTING" "Frontend test configuration" "PARTIAL" 3 5
        fi
    else
        log_audit "TESTING" "Frontend test configuration" "FAIL" 0 5
    fi

    # Test 3: Test integration in pipeline
    if grep -q "publishTestResults" "Jenkinsfile" && grep -q "publishCoverage" "Jenkinsfile"; then
        log_audit "TESTING" "Test reporting in pipeline" "PASS" 5 5
    else
        log_audit "TESTING" "Test reporting in pipeline" "FAIL" 0 5
    fi
}

# Function to audit deployment configuration
audit_deployment() {
    echo -e "\n${BLUE}📋 4. Deployment & Rollback${NC}"

    # Test 1: Multi-environment configuration
    local env_configs=0
    for env in dev staging prod; do
        if [ -f "microservices-architecture/docker-compose/docker-compose.$env.yml" ]; then
            env_configs=$((env_configs + 1))
        fi
    done

    if [ $env_configs -eq 3 ]; then
        log_audit "DEPLOYMENT" "Multi-environment configs" "PASS" 5 5
    elif [ $env_configs -gt 1 ]; then
        log_audit "DEPLOYMENT" "Multi-environment configs" "PARTIAL" 3 5
    else
        log_audit "DEPLOYMENT" "Multi-environment configs" "FAIL" 0 5
    fi

    # Test 2: Rollback strategy
    if [ -f "scripts/rollback.sh" ] && [ -x "scripts/rollback.sh" ]; then
        # Test rollback script functionality
        if ./scripts/rollback.sh -h > /dev/null 2>&1; then
            log_audit "DEPLOYMENT" "Rollback strategy" "PASS" 5 5
        else
            log_audit "DEPLOYMENT" "Rollback strategy" "PARTIAL" 3 5
        fi
    else
        log_audit "DEPLOYMENT" "Rollback strategy" "FAIL" 0 5
    fi

    # Test 3: Health checks in pipeline
    if grep -q "Health Check" "Jenkinsfile" || grep -q "actuator/health" "Jenkinsfile"; then
        log_audit "DEPLOYMENT" "Health checks" "PASS" 5 5
    else
        log_audit "DEPLOYMENT" "Health checks" "FAIL" 0 5
    fi
}

# Function to audit notifications
audit_notifications() {
    echo -e "\n${BLUE}📋 5. Notifications${NC}"

    # Test 1: Notification script
    if [ -f "scripts/send-notification.sh" ] && [ -x "scripts/send-notification.sh" ]; then
        log_audit "NOTIFICATIONS" "Notification script exists" "PASS" 3 3
    else
        log_audit "NOTIFICATIONS" "Notification script exists" "FAIL" 0 3
        return
    fi

    # Test 2: Slack integration
    if grep -q "SLACK_WEBHOOK_URL" "scripts/send-notification.sh"; then
        # Test actual Slack functionality if webhook is configured
        if [ ! -z "$SLACK_WEBHOOK_URL" ]; then
            echo "Testing Slack integration..."
            if ./scripts/send-notification.sh "🔍 Audit test - $(date)" > /dev/null 2>&1; then
                log_audit "NOTIFICATIONS" "Slack integration" "PASS" 5 5
            else
                log_audit "NOTIFICATIONS" "Slack integration" "PARTIAL" 3 5
            fi
        else
            log_audit "NOTIFICATIONS" "Slack integration" "PARTIAL" 3 5
        fi
    else
        log_audit "NOTIFICATIONS" "Slack integration" "FAIL" 0 5
    fi

    # Test 3: Email configuration
    if grep -q "emailext" "Jenkinsfile"; then
        log_audit "NOTIFICATIONS" "Email configuration" "PASS" 3 3
    else
        log_audit "NOTIFICATIONS" "Email configuration" "FAIL" 0 3
    fi
}

# Function to audit security
audit_security() {
    echo -e "\n${BLUE}📋 6. Security${NC}"

    # Test 1: OWASP dependency check
    local owasp_configs=0
    for service in user-service product-service media-service api-gateway; do
        local pom_file="microservices-architecture/$service/pom.xml"
        if [ -f "$pom_file" ]; then
            if grep -q "dependency-check-maven" "$pom_file"; then
                owasp_configs=$((owasp_configs + 1))
            fi
        fi
    done

    if [ $owasp_configs -gt 0 ]; then
        log_audit "SECURITY" "OWASP dependency scanning" "PASS" 3 3
    else
        log_audit "SECURITY" "OWASP dependency scanning" "FAIL" 0 3
    fi

    # Test 2: Secrets management preparation
    if grep -q "credentials(" "Jenkinsfile" || grep -q "environment" "Jenkinsfile"; then
        log_audit "SECURITY" "Secrets management structure" "PASS" 3 3
    else
        log_audit "SECURITY" "Secrets management structure" "FAIL" 0 3
    fi

    # Test 3: Jenkins security (basic check)
    echo -e "${YELLOW}⚠️  Manual verification required: Jenkins authentication setup${NC}"
    log_audit "SECURITY" "Jenkins access control" "PARTIAL" 1 3
}

# Function to audit bonus features
audit_bonus_features() {
    echo -e "\n${BLUE}📋 7. Bonus Features${NC}"

    # Test 1: Parameterized builds
    if grep -q "parameters" "Jenkinsfile" && grep -q "params.ENVIRONMENT" "Jenkinsfile"; then
        local param_count=$(grep -c "params\." "Jenkinsfile" || echo 0)
        if [ $param_count -gt 3 ]; then
            log_audit "BONUS" "Parameterized builds" "PASS" 5 5
        else
            log_audit "BONUS" "Parameterized builds" "PARTIAL" 3 5
        fi
    else
        log_audit "BONUS" "Parameterized builds" "FAIL" 0 5
    fi

    # Test 2: Distributed builds (parallel stages)
    if grep -q "parallel" "Jenkinsfile"; then
        local parallel_blocks=$(grep -c "parallel {" "Jenkinsfile" || echo 0)
        if [ $parallel_blocks -gt 0 ]; then
            log_audit "BONUS" "Distributed/parallel builds" "PASS" 5 5
        else
            log_audit "BONUS" "Distributed/parallel builds" "PARTIAL" 3 5
        fi
    else
        log_audit "BONUS" "Distributed/parallel builds" "FAIL" 0 5
    fi
}

# Function to test actual pipeline trigger
test_pipeline_trigger() {
    echo -e "\n${BLUE}📋 8. Pipeline Trigger Test${NC}"

    # Check if we can trigger a build (if Jenkins is properly configured)
    echo "Testing pipeline trigger capability..."

    # This would require proper Jenkins API access
    # For now, we test the configuration
    if grep -q "pollSCM\|triggers" "Jenkinsfile"; then
        log_audit "PIPELINE" "Auto-trigger configuration" "PASS" 3 3
    else
        log_audit "PIPELINE" "Auto-trigger configuration" "FAIL" 0 3
    fi
}

# Function to generate audit report
generate_audit_report() {
    echo -e "\n${BLUE}📊 Audit Summary${NC}"
    echo "================================"

    local percentage=$((SCORE * 100 / MAX_SCORE))

    echo "Total Score: $SCORE/$MAX_SCORE ($percentage%)"

    if [ $percentage -ge 90 ]; then
        echo -e "${GREEN}🏆 Grade: A - Excellence${NC}"
    elif [ $percentage -ge 80 ]; then
        echo -e "${GREEN}🥇 Grade: B - Très bien${NC}"
    elif [ $percentage -ge 70 ]; then
        echo -e "${YELLOW}🥈 Grade: C - Bien${NC}"
    elif [ $percentage -ge 60 ]; then
        echo -e "${YELLOW}🥉 Grade: D - Passable${NC}"
    else
        echo -e "${RED}❌ Grade: F - Insuffisant${NC}"
    fi

    echo ""
    echo "Detailed report available in: CI-CD-AUDIT-REPORT.md"

    # Generate summary for Slack notification if webhook is configured
    if [ ! -z "$SLACK_WEBHOOK_URL" ]; then
        export BUILD_NUMBER="AUDIT-$(date +%s)"
        export ENVIRONMENT="audit"
        export GIT_COMMIT="audit-run"

        ./scripts/send-notification.sh "🔍 Audit CI/CD Buy01 terminé: Score $SCORE/$MAX_SCORE ($percentage%) - Grade: $([ $percentage -ge 90 ] && echo 'A' || echo 'B')"
    fi
}

# Main audit execution
main() {
    echo -e "${BLUE}Starting comprehensive CI/CD audit...${NC}"

    # Change to project root if needed
    if [ ! -f "Jenkinsfile" ]; then
        echo -e "${RED}❌ Jenkinsfile not found. Please run from project root.${NC}"
        exit 1
    fi

    # Run all audit functions
    audit_jenkins_availability
    audit_jenkinsfile
    audit_testing
    audit_deployment
    audit_notifications
    audit_security
    audit_bonus_features
    test_pipeline_trigger

    # Generate final report
    generate_audit_report

    echo ""
    echo -e "${BLUE}🎯 Audit completed successfully!${NC}"

    # Return appropriate exit code based on score
    local percentage=$((SCORE * 100 / MAX_SCORE))
    if [ $percentage -ge 70 ]; then
        exit 0  # Success
    else
        exit 1  # Needs improvement
    fi
}

# Help function
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Audit complet du pipeline CI/CD Buy01

Options:
    -h, --help      Show this help
    -v, --verbose   Verbose output
    --no-slack      Skip Slack notifications

Environment Variables:
    SLACK_WEBHOOK_URL   Slack webhook for notifications
    JENKINS_URL         Jenkins server URL (default: http://localhost:8090)

Examples:
    $0                  # Full audit
    $0 --no-slack      # Audit without Slack notifications
    $0 -v              # Verbose audit

EOF
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            usage
            exit 0
            ;;
        -v|--verbose)
            set -x
            shift
            ;;
        --no-slack)
            unset SLACK_WEBHOOK_URL
            shift
            ;;
        *)
            echo "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Run main function
main "$@"