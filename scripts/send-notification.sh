#!/bin/bash

# Buy01 Notification Script
# Sends notifications via email and Slack

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration (these should be set as environment variables)
EMAIL_SMTP_SERVER="${EMAIL_SMTP_SERVER:-smtp.gmail.com}"
EMAIL_SMTP_PORT="${EMAIL_SMTP_PORT:-587}"
EMAIL_FROM="${EMAIL_FROM:-noreply@buy01.com}"
EMAIL_TO="${EMAIL_TO:-team@buy01.com}"
EMAIL_USERNAME="${EMAIL_USERNAME:-}"
EMAIL_PASSWORD="${EMAIL_PASSWORD:-}"

SLACK_WEBHOOK_URL="${SLACK_WEBHOOK_URL:-}"
SLACK_CHANNEL="${SLACK_CHANNEL:-#social}"

# Project information
PROJECT_NAME="Buy01 E-commerce Platform"
BUILD_URL="${BUILD_URL:-}"
BUILD_NUMBER="${BUILD_NUMBER:-}"
GIT_COMMIT="${GIT_COMMIT:-}"
ENVIRONMENT="${ENVIRONMENT:-}"

# Buy01 specific configuration
BUY01_TEMPLATES_FILE="${BUY01_TEMPLATES_FILE:-$(dirname "$0")/../slack-integration/buy01-slack-templates.json}"
APP_URL_PRODUCTION="https://buy01.com"
APP_URL_STAGING="https://staging.buy01.com"
APP_URL_DEVELOPMENT="https://dev.buy01.com"

# Function to send email notification
send_email() {
    local subject="$1"
    local message="$2"
    local priority="${3:-normal}"

    if [ -z "$EMAIL_USERNAME" ] || [ -z "$EMAIL_PASSWORD" ]; then
        echo -e "${YELLOW}⚠️  Email credentials not configured, skipping email notification${NC}"
        return 0
    fi

    local html_message=$(cat << EOF
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>$subject</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .header { background-color: #f8f9fa; padding: 20px; border-bottom: 3px solid #007bff; }
        .content { padding: 20px; }
        .footer { background-color: #f8f9fa; padding: 15px; margin-top: 20px; border-top: 1px solid #dee2e6; }
        .success { color: #28a745; }
        .error { color: #dc3545; }
        .warning { color: #ffc107; }
        .info { color: #17a2b8; }
        .badge { padding: 3px 8px; border-radius: 3px; font-size: 12px; font-weight: bold; }
        .badge-success { background-color: #d4edda; color: #155724; }
        .badge-error { background-color: #f8d7da; color: #721c24; }
        .badge-warning { background-color: #fff3cd; color: #856404; }
        .details { background-color: #f8f9fa; padding: 15px; border-left: 4px solid #007bff; margin: 15px 0; }
    </style>
</head>
<body>
    <div class="header">
        <h2>$PROJECT_NAME</h2>
        <p>Deployment Notification</p>
    </div>
    <div class="content">
        <h3>$subject</h3>
        <div class="details">
            $message
        </div>
        <table style="border-collapse: collapse; width: 100%; margin-top: 20px;">
            <tr><td style="padding: 5px; border: 1px solid #ddd;"><strong>Project:</strong></td><td style="padding: 5px; border: 1px solid #ddd;">$PROJECT_NAME</td></tr>
            <tr><td style="padding: 5px; border: 1px solid #ddd;"><strong>Environment:</strong></td><td style="padding: 5px; border: 1px solid #ddd;">$ENVIRONMENT</td></tr>
            <tr><td style="padding: 5px; border: 1px solid #ddd;"><strong>Build Number:</strong></td><td style="padding: 5px; border: 1px solid #ddd;">$BUILD_NUMBER</td></tr>
            <tr><td style="padding: 5px; border: 1px solid #ddd;"><strong>Git Commit:</strong></td><td style="padding: 5px; border: 1px solid #ddd;">$GIT_COMMIT</td></tr>
            <tr><td style="padding: 5px; border: 1px solid #ddd;"><strong>Timestamp:</strong></td><td style="padding: 5px; border: 1px solid #ddd;">$(date)</td></tr>
        </table>
    </div>
    <div class="footer">
        <p>This is an automated notification from the Buy01 CI/CD pipeline.</p>
        <p><a href="$BUILD_URL">View Build Details</a></p>
    </div>
</body>
</html>
EOF
)

    # Create temporary file for email content
    local temp_file=$(mktemp)
    echo "To: $EMAIL_TO" > "$temp_file"
    echo "From: $EMAIL_FROM" >> "$temp_file"
    echo "Subject: $subject" >> "$temp_file"
    echo "Content-Type: text/html; charset=UTF-8" >> "$temp_file"
    echo "" >> "$temp_file"
    echo "$html_message" >> "$temp_file"

    # Send email using curl
    if curl -s --url "smtps://$EMAIL_SMTP_SERVER:$EMAIL_SMTP_PORT" \
        --ssl-reqd \
        --mail-from "$EMAIL_FROM" \
        --mail-rcpt "$EMAIL_TO" \
        --upload-file "$temp_file" \
        --user "$EMAIL_USERNAME:$EMAIL_PASSWORD" > /dev/null; then
        echo -e "${GREEN}✅ Email sent successfully to $EMAIL_TO${NC}"
    else
        echo -e "${RED}❌ Failed to send email${NC}"
    fi

    # Cleanup
    rm -f "$temp_file"
}

# Function to send Slack notification
send_slack() {
    local title="$1"
    local message="$2"
    local color="${3:-good}"

    if [ -z "$SLACK_WEBHOOK_URL" ]; then
        echo -e "${YELLOW}⚠️  Slack webhook URL not configured, skipping Slack notification${NC}"
        return 0
    fi

    # Determine emoji and color based on message type
    local emoji="🚀"
    case "$color" in
        "good") emoji="✅"; color="#36a64f" ;;
        "warning") emoji="⚠️"; color="#ff9900" ;;
        "danger") emoji="❌"; color="#ff0000" ;;
        *) emoji="ℹ️"; color="#439fe0" ;;
    esac

    # Create Slack payload using new Block Kit format
    local payload=$(cat << EOF
{
    "channel": "$SLACK_CHANNEL",
    "username": "Buy01 CI/CD",
    "icon_emoji": ":rocket:",
    "text": "$emoji $title",
    "blocks": [
        {
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "*$emoji $title*\n\n$message"
            }
        },
        {
            "type": "section",
            "fields": [
                {
                    "type": "mrkdwn",
                    "text": "*Environment:*\n$ENVIRONMENT"
                },
                {
                    "type": "mrkdwn",
                    "text": "*Build:*\n#$BUILD_NUMBER"
                },
                {
                    "type": "mrkdwn",
                    "text": "*Commit:*\n$GIT_COMMIT"
                },
                {
                    "type": "mrkdwn",
                    "text": "*Time:*\n$(date '+%H:%M:%S')"
                }
            ]
        }
    ]
}
EOF
)

    # Send to Slack
    # Debug: Show payload being sent
    echo -e "${BLUE}🔍 Debug - Webhook URL: ${SLACK_WEBHOOK_URL:0:50}...${NC}"
    echo -e "${BLUE}🔍 Debug - Payload (first 300 chars):${NC}"
    echo "$payload" | head -c 300
    echo ""

    # Send to Slack
    local response=$(curl -s -X POST \
        -H 'Content-type: application/json' \
        --data "$payload" \
        "$SLACK_WEBHOOK_URL")

    echo -e "${BLUE}🔍 Debug - Slack response: $response${NC}"

    if [ "$response" = "ok" ]; then
        echo -e "${GREEN}✅ Slack notification sent successfully${NC}"
    else
        echo -e "${RED}❌ Failed to send Slack notification. Response: $response${NC}"
    fi
}

# Function to determine notification type based on keywords
determine_notification_type() {
    local message="$1"

    if echo "$message" | grep -qi "success\|completed\|deployed\|passed"; then
        echo "success"
    elif echo "$message" | grep -qi "failed\|error\|failure"; then
        echo "error"
    elif echo "$message" | grep -qi "warning\|unstable"; then
        echo "warning"
    else
        echo "info"
    fi
}

# Function to send comprehensive notification
send_notification() {
    local message="$1"
    local notification_type=$(determine_notification_type "$message")

    echo -e "${BLUE}📢 Sending notifications...${NC}"

    case "$notification_type" in
        "success")
            local subject="✅ $PROJECT_NAME - Deployment Success"
            local slack_color="good"
            ;;
        "error")
            local subject="❌ $PROJECT_NAME - Deployment Failed"
            local slack_color="danger"
            ;;
        "warning")
            local subject="⚠️ $PROJECT_NAME - Deployment Warning"
            local slack_color="warning"
            ;;
        *)
            local subject="ℹ️ $PROJECT_NAME - Deployment Info"
            local slack_color="#439fe0"
            ;;
    esac

    # Send email notification
    send_email "$subject" "$message" "$notification_type"

    # Send Slack notification
    send_slack "$subject" "$message" "$slack_color"

    echo -e "${GREEN}📨 Notification process completed${NC}"
}

# Function to send test notification
send_test_notification() {
    echo -e "${BLUE}🧪 Sending test notifications...${NC}"

    local test_message="This is a test notification from the Buy01 CI/CD pipeline. All systems are operational."

    send_notification "$test_message"
}

# Function to show usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS] [MESSAGE]

Send notifications via email and Slack for CI/CD events.

Options:
    -t, --test          Send a test notification
    -e, --email-only    Send email notification only
    -s, --slack-only    Send Slack notification only
    -h, --help          Show this help message

Environment Variables:
    EMAIL_SMTP_SERVER   SMTP server (default: smtp.gmail.com)
    EMAIL_SMTP_PORT     SMTP port (default: 587)
    EMAIL_FROM          From email address
    EMAIL_TO            To email address
    EMAIL_USERNAME      SMTP username
    EMAIL_PASSWORD      SMTP password
    SLACK_WEBHOOK_URL   Slack incoming webhook URL
    SLACK_CHANNEL       Slack channel (default: #deployments)

Examples:
    $0 "Build completed successfully"
    $0 --test
    $0 --email-only "Deployment failed in staging"

EOF
}

# Main script logic
main() {
    local email_only=false
    local slack_only=false
    local test_mode=false
    local message=""

    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -t|--test)
                test_mode=true
                shift
                ;;
            -e|--email-only)
                email_only=true
                shift
                ;;
            -s|--slack-only)
                slack_only=true
                shift
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            -*)
                echo -e "${RED}Unknown option: $1${NC}"
                usage
                exit 1
                ;;
            *)
                message="$1"
                shift
                ;;
        esac
    done

    # Handle test mode
    if [ "$test_mode" = true ]; then
        send_test_notification
        exit 0
    fi

    # Validate message
    if [ -z "$message" ]; then
        echo -e "${RED}❌ Message is required${NC}"
        usage
        exit 1
    fi

    # Send notifications based on options
    if [ "$email_only" = true ]; then
        local subject=$(determine_notification_type "$message" | sed 's/success/✅/; s/error/❌/; s/warning/⚠️/; s/info/ℹ️/')
        send_email "$subject $PROJECT_NAME - Notification" "$message"
    elif [ "$slack_only" = true ]; then
        local color=$(determine_notification_type "$message")
        case "$color" in
            "success") color="good" ;;
            "error") color="danger" ;;
            "warning") color="warning" ;;
            *) color="#439fe0" ;;
        esac
        send_slack "$PROJECT_NAME - Notification" "$message" "$color"
    else
        send_notification "$message"
    fi
}

# Run main function with all arguments
main "$@"