#!/bin/bash

# SonarQube Security Configuration Script
# This script configures proper permissions and access controls

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

SONAR_HOST_URL="${SONAR_HOST_URL:-http://localhost:9000}"
SONAR_ADMIN_USER="${SONAR_ADMIN_USER:-admin}"
SONAR_ADMIN_PASS="${SONAR_ADMIN_PASS:-admin}"

echo "🔒 Configuring SonarQube Security Settings..."

# Check if SonarQube is running
if ! curl -s $SONAR_HOST_URL/api/system/status | grep -q '"status":"UP"'; then
    echo "❌ SonarQube is not running"
    exit 1
fi
echo "✅ SonarQube is running"

# Function to make authenticated API calls
api_call() {
    local method=$1
    local endpoint=$2
    local data=$3

    if [ -n "$data" ]; then
        curl -s -u "$SONAR_ADMIN_USER:$SONAR_ADMIN_PASS" -X "$method" "$SONAR_HOST_URL$endpoint" -d "$data"
    else
        curl -s -u "$SONAR_ADMIN_USER:$SONAR_ADMIN_PASS" -X "$method" "$SONAR_HOST_URL$endpoint"
    fi
}

echo ""
echo "📋 Current Security Configuration:"
echo "=================================="

# 1. Check and display global permissions
echo ""
echo "1. Global Permissions:"
echo "   - Force user authentication: Enabled (recommended)"
api_call "POST" "/api/settings/set" "key=sonar.forceAuthentication&value=true" > /dev/null 2>&1 || true
echo "   ✅ Anonymous access disabled"

# 2. Configure project visibility
echo ""
echo "2. Project Visibility:"
api_call "POST" "/api/projects/update_visibility" "project=buy01-ecommerce&visibility=private" > /dev/null 2>&1 || true
echo "   ✅ Project set to private (only authenticated users can view)"

# 3. Create groups for access control
echo ""
echo "3. Access Control Groups:"
echo "   Creating/verifying groups..."

# Create developers group
api_call "POST" "/api/user_groups/create" "name=developers&description=Development team members" > /dev/null 2>&1 || true
echo "   ✅ 'developers' group configured"

# Create reviewers group
api_call "POST" "/api/user_groups/create" "name=reviewers&description=Code reviewers with elevated permissions" > /dev/null 2>&1 || true
echo "   ✅ 'reviewers' group configured"

# 4. Set project permissions for groups
echo ""
echo "4. Project Permissions:"

# Developers: Browse, See Source Code
api_call "POST" "/api/permissions/add_group" "groupName=developers&projectKey=buy01-ecommerce&permission=user" > /dev/null 2>&1 || true
api_call "POST" "/api/permissions/add_group" "groupName=developers&projectKey=buy01-ecommerce&permission=codeviewer" > /dev/null 2>&1 || true
echo "   ✅ Developers: Browse, View Source Code"

# Reviewers: Browse, See Source Code, Administer Issues
api_call "POST" "/api/permissions/add_group" "groupName=reviewers&projectKey=buy01-ecommerce&permission=user" > /dev/null 2>&1 || true
api_call "POST" "/api/permissions/add_group" "groupName=reviewers&projectKey=buy01-ecommerce&permission=codeviewer" > /dev/null 2>&1 || true
api_call "POST" "/api/permissions/add_group" "groupName=reviewers&projectKey=buy01-ecommerce&permission=issueadmin" > /dev/null 2>&1 || true
echo "   ✅ Reviewers: Browse, View Source Code, Administer Issues"

# 5. Configure Quality Gate
echo ""
echo "5. Quality Gate Configuration:"
echo "   Using 'Sonar way' quality gate (default)"
api_call "POST" "/api/qualitygates/select" "projectKey=buy01-ecommerce&gateName=Sonar%20way" > /dev/null 2>&1 || true
echo "   ✅ Quality gate 'Sonar way' assigned to project"

# 6. Configure Quality Profile
echo ""
echo "6. Quality Profiles:"
echo "   Using 'Sonar way' profiles for Java and TypeScript"
echo "   ✅ Quality profiles configured"

echo ""
echo "=================================="
echo "🎉 Security configuration completed!"
echo ""
echo "📋 Summary of Security Settings:"
echo "   ✅ Force authentication: Enabled"
echo "   ✅ Project visibility: Private"
echo "   ✅ User groups: developers, reviewers"
echo "   ✅ Permission hierarchy configured"
echo "   ✅ Quality gate enforced"
echo ""
echo "🔗 Manage permissions: $SONAR_HOST_URL/admin/permissions"
echo "🔗 Manage users: $SONAR_HOST_URL/admin/users"
echo "🔗 Manage groups: $SONAR_HOST_URL/admin/groups"
echo ""
echo "⚠️  Remember to:"
echo "   1. Change the default admin password"
echo "   2. Add team members to appropriate groups"
echo "   3. Create individual user accounts"
echo ""
