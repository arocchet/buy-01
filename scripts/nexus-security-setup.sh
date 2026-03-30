#!/usr/bin/env bash

set -euo pipefail

NEXUS_URL="${NEXUS_URL:-http://localhost:8081}"
NEXUS_ADMIN_USER="${NEXUS_ADMIN_USER:-admin}"
NEXUS_ADMIN_PASSWORD="${NEXUS_ADMIN_PASSWORD:-admin123}"

CI_USER="${CI_USER:-ci-deployer}"
CI_PASSWORD="${CI_PASSWORD:-ChangeMe-StrongPassword!}"

auth_args=(-u "${NEXUS_ADMIN_USER}:${NEXUS_ADMIN_PASSWORD}" -H "Content-Type: application/json")

role_exists() {
  curl -fsS "${auth_args[@]}" "${NEXUS_URL}/service/rest/v1/security/roles" \
    | grep -q '"id"[[:space:]]*:[[:space:]]*"nx-ci-deployer"'
}

user_exists() {
  curl -fsS "${auth_args[@]}" "${NEXUS_URL}/service/rest/v1/security/users" \
    | grep -q "\"userId\"[[:space:]]*:[[:space:]]*\"${CI_USER}\""
}

if role_exists; then
  echo "[SKIP] Role nx-ci-deployer already exists"
else
  echo "[CREATE] Role nx-ci-deployer"
  curl -fsS "${auth_args[@]}" -X POST "${NEXUS_URL}/service/rest/v1/security/roles" -d '{
    "id": "nx-ci-deployer",
    "name": "CI Deployer",
    "description": "Deploy snapshots/releases and push Docker images",
    "privileges": [
      "nx-repository-view-maven2-maven-releases-add",
      "nx-repository-view-maven2-maven-releases-edit",
      "nx-repository-view-maven2-maven-snapshots-add",
      "nx-repository-view-maven2-maven-snapshots-edit",
      "nx-repository-view-maven2-maven-public-read",
      "nx-repository-view-docker-docker-hosted-add",
      "nx-repository-view-docker-docker-hosted-edit",
      "nx-repository-view-docker-docker-group-read"
    ],
    "roles": []
  }' >/dev/null
fi

if user_exists; then
  echo "[SKIP] User ${CI_USER} already exists"
else
  echo "[CREATE] User ${CI_USER}"
  curl -fsS "${auth_args[@]}" -X POST "${NEXUS_URL}/service/rest/v1/security/users" -d "{
    \"userId\": \"${CI_USER}\",
    \"firstName\": \"CI\",
    \"lastName\": \"Deployer\",
    \"emailAddress\": \"ci-deployer@example.local\",
    \"password\": \"${CI_PASSWORD}\",
    \"status\": \"active\",
    \"roles\": [\"nx-ci-deployer\"]
  }" >/dev/null
fi

echo "Nexus RBAC setup complete."
