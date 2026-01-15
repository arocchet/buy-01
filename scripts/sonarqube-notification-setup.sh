#!/bin/bash

# SonarQube Notification Setup Script for Buy01 E-commerce Platform

set -e

echo "📧 Setting up SonarQube notifications..."

# Configuration variables
SONAR_HOST_URL="${SONAR_HOST_URL:-http://localhost:9000}"
SONAR_TOKEN="${SONAR_TOKEN:-$(cat .sonarqube-token 2>/dev/null || echo '')}"
WEBHOOK_URL="${WEBHOOK_URL:-${SLACK_WEBHOOK_URL}}"

# Function to check SonarQube connectivity
check_sonarqube() {
    if ! curl -s "$SONAR_HOST_URL/api/system/status" > /dev/null; then
        echo "❌ SonarQube is not accessible at $SONAR_HOST_URL"
        echo "Please start SonarQube first: docker-compose -f docker-compose.sonarqube.yml up -d"
        exit 1
    fi
    echo "✅ SonarQube is accessible"
}

# Function to configure webhooks in SonarQube
setup_webhooks() {
    if [ -z "$SONAR_TOKEN" ]; then
        echo "❌ SONAR_TOKEN not found. Please set SONAR_TOKEN environment variable or run setup script first."
        exit 1
    fi

    echo "🔗 Setting up SonarQube webhooks..."

    # Create webhook for quality gate changes
    if [ -n "$WEBHOOK_URL" ]; then
        echo "📡 Creating Quality Gate webhook..."

        curl -s -u "$SONAR_TOKEN:" -X POST "$SONAR_HOST_URL/api/webhooks/create" \
            -d "name=Quality Gate Slack Notification" \
            -d "url=$WEBHOOK_URL" \
            -d "project=buy01-ecommerce" || {
                echo "⚠️ Webhook creation failed (it might already exist)"
            }

        echo "✅ Quality Gate webhook configured"
    else
        echo "⚠️ No webhook URL provided, skipping webhook setup"
    fi
}

# Function to configure email notifications
setup_email_notifications() {
    echo "📧 Email notification configuration..."
    echo "To configure email notifications:"
    echo "1. Go to SonarQube UI: $SONAR_HOST_URL"
    echo "2. Navigate to Administration > Configuration > General Settings"
    echo "3. Configure Email settings:"
    echo "   - SMTP server host"
    echo "   - SMTP server port"
    echo "   - Security protocol (SSL/TLS)"
    echo "   - Authentication credentials"
    echo "4. Test email configuration"
    echo "5. Set up user notification preferences in their profiles"
}

# Function to create notification rules
create_notification_rules() {
    echo "🔔 Creating notification rules..."

    cat > sonarqube-notification-rules.json << 'EOF'
{
  "notification_rules": {
    "quality_gate_failed": {
      "channels": ["slack", "email"],
      "severity": "high",
      "message_template": "Quality Gate Failed for {project} on {branch}"
    },
    "new_security_hotspots": {
      "channels": ["slack", "email"],
      "severity": "critical",
      "message_template": "Security hotspots detected in {project}"
    },
    "coverage_decreased": {
      "channels": ["slack"],
      "severity": "medium",
      "threshold": "5%",
      "message_template": "Code coverage decreased by {percentage} in {project}"
    },
    "new_bugs": {
      "channels": ["slack"],
      "severity": "medium",
      "message_template": "{count} new bugs found in {project}"
    }
  }
}
EOF

    echo "✅ Notification rules template created: sonarqube-notification-rules.json"
}

# Function to setup GitHub repository secrets
setup_github_secrets() {
    echo "🔐 GitHub Secrets Configuration Guide"
    echo "Please configure the following secrets in your GitHub repository:"
    echo ""
    echo "Repository Settings > Secrets and Variables > Actions > New repository secret"
    echo ""
    echo "Required secrets:"
    echo "• SONAR_TOKEN: $SONAR_TOKEN"
    echo "• SONAR_HOST_URL: $SONAR_HOST_URL"
    echo "• SLACK_WEBHOOK_URL: Your Slack webhook URL"
    echo ""
    echo "Optional secrets for email notifications:"
    echo "• SMTP_SERVER: Your SMTP server address"
    echo "• SMTP_PORT: SMTP server port (587, 465, etc.)"
    echo "• SMTP_USERNAME: SMTP authentication username"
    echo "• SMTP_PASSWORD: SMTP authentication password"
    echo "• TEAM_EMAIL: Team notification email address"
    echo ""
    echo "📝 Create these secrets to enable automated notifications."
}

# Function to test notifications
test_notifications() {
    echo "🧪 Testing notification setup..."

    if [ -n "$WEBHOOK_URL" ]; then
        echo "📡 Testing Slack webhook..."

        # Test Slack webhook with a sample message
        curl -X POST "$WEBHOOK_URL" \
            -H 'Content-type: application/json' \
            --data '{
                "text": "🧪 Test notification from SonarQube setup script",
                "attachments": [
                    {
                        "color": "good",
                        "fields": [
                            {
                                "title": "Project",
                                "value": "Buy01 E-commerce Platform",
                                "short": true
                            },
                            {
                                "title": "Status",
                                "value": "✅ Notification system is working",
                                "short": true
                            }
                        ]
                    }
                ]
            }' && echo "✅ Slack notification test sent successfully" || echo "❌ Slack notification test failed"
    else
        echo "⚠️ No Slack webhook URL configured, skipping test"
    fi
}

# Function to create monitoring dashboard
create_monitoring_dashboard() {
    echo "📊 Creating monitoring dashboard configuration..."

    cat > monitoring-dashboard-config.yml << 'EOF'
# SonarQube Monitoring Dashboard Configuration
dashboard:
  title: "Buy01 E-commerce - Code Quality Dashboard"

  panels:
    - title: "Quality Gate Status"
      type: "status"
      query: "quality_gate_status"

    - title: "Code Coverage Trend"
      type: "line_chart"
      query: "coverage_percentage"
      time_range: "30d"

    - title: "Security Hotspots"
      type: "gauge"
      query: "security_hotspots_count"

    - title: "Technical Debt"
      type: "metric"
      query: "technical_debt_hours"

    - title: "Bugs by Severity"
      type: "pie_chart"
      query: "bugs_by_severity"

  alerts:
    - name: "Quality Gate Failed"
      condition: "quality_gate_status != 'OK'"
      channels: ["slack", "email"]

    - name: "Coverage Drop"
      condition: "coverage_percentage < 80"
      channels: ["slack"]

    - name: "High Security Risk"
      condition: "security_hotspots_count > 5"
      channels: ["slack", "email"]
      priority: "high"
EOF

    echo "✅ Monitoring dashboard configuration created: monitoring-dashboard-config.yml"
}

# Main execution
main() {
    echo "🚀 Starting SonarQube notification setup..."
    echo ""

    check_sonarqube
    setup_webhooks
    setup_email_notifications
    create_notification_rules
    setup_github_secrets
    test_notifications
    create_monitoring_dashboard

    echo ""
    echo "🎉 SonarQube notification setup completed!"
    echo ""
    echo "📋 Next steps:"
    echo "1. Configure GitHub secrets as listed above"
    echo "2. Set up SMTP configuration in SonarQube UI"
    echo "3. Configure user notification preferences"
    echo "4. Test notifications by triggering a build"
    echo ""
    echo "🔗 Useful links:"
    echo "- SonarQube UI: $SONAR_HOST_URL"
    echo "- Webhook settings: $SONAR_HOST_URL/project/webhooks?id=buy01-ecommerce"
    echo "- Email settings: $SONAR_HOST_URL/admin/settings?category=email"
    echo ""
}

# Show help
show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --webhook-url URL    Set Slack webhook URL"
    echo "  --sonar-token TOKEN  Set SonarQube token"
    echo "  --test-only          Only run notification tests"
    echo "  --help               Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  SONAR_HOST_URL       SonarQube server URL (default: http://localhost:9000)"
    echo "  SONAR_TOKEN          SonarQube authentication token"
    echo "  SLACK_WEBHOOK_URL    Slack webhook URL for notifications"
    echo ""
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --webhook-url)
            WEBHOOK_URL="$2"
            shift 2
            ;;
        --sonar-token)
            SONAR_TOKEN="$2"
            shift 2
            ;;
        --test-only)
            test_notifications
            exit 0
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Run main function
main