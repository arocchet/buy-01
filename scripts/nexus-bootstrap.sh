#!/usr/bin/env bash

set -euo pipefail

NEXUS_URL="${NEXUS_URL:-http://localhost:8081}"
NEXUS_USER="${NEXUS_USER:-admin}"
NEXUS_PASSWORD="${NEXUS_PASSWORD:-admin123}"

api_post() {
  local endpoint="$1"
  local payload="$2"

  curl -fsS -u "${NEXUS_USER}:${NEXUS_PASSWORD}" \
    -H "Content-Type: application/json" \
    -X POST "${NEXUS_URL}${endpoint}" \
    -d "${payload}" >/dev/null
}

repo_exists() {
  local name="$1"
  curl -fsS -u "${NEXUS_USER}:${NEXUS_PASSWORD}" \
    "${NEXUS_URL}/service/rest/v1/repositories" \
    | grep -q "\"name\"[[:space:]]*:[[:space:]]*\"${name}\""
}

create_if_missing() {
  local name="$1"
  local endpoint="$2"
  local payload="$3"

  if repo_exists "${name}"; then
    echo "[SKIP] Repository ${name} already exists"
    return
  fi

  echo "[CREATE] ${name}"
  api_post "${endpoint}" "${payload}"
}

echo "Bootstrap Nexus repositories on ${NEXUS_URL}"

# Hosted repositories for Maven artifacts (JAR/WAR).
create_if_missing "maven-releases" "/service/rest/v1/repositories/maven/hosted" '{
  "name": "maven-releases",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true,
    "writePolicy": "ALLOW_ONCE"
  },
  "maven": {
    "versionPolicy": "RELEASE",
    "layoutPolicy": "STRICT"
  }
}'

create_if_missing "maven-snapshots" "/service/rest/v1/repositories/maven/hosted" '{
  "name": "maven-snapshots",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true,
    "writePolicy": "ALLOW"
  },
  "maven": {
    "versionPolicy": "SNAPSHOT",
    "layoutPolicy": "STRICT"
  }
}'

create_if_missing "maven-central" "/service/rest/v1/repositories/maven/proxy" '{
  "name": "maven-central",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true
  },
  "proxy": {
    "remoteUrl": "https://repo1.maven.org/maven2/",
    "contentMaxAge": 1440,
    "metadataMaxAge": 1440
  },
  "negativeCache": {
    "enabled": true,
    "timeToLive": 1440
  },
  "httpClient": {
    "blocked": false,
    "autoBlock": true
  },
  "maven": {
    "versionPolicy": "MIXED",
    "layoutPolicy": "STRICT"
  }
}'

create_if_missing "maven-public" "/service/rest/v1/repositories/maven/group" '{
  "name": "maven-public",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true
  },
  "group": {
    "memberNames": ["maven-releases", "maven-snapshots", "maven-central"]
  }
}'

# Docker repositories.
create_if_missing "docker-hosted" "/service/rest/v1/repositories/docker/hosted" '{
  "name": "docker-hosted",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true,
    "writePolicy": "ALLOW"
  },
  "docker": {
    "v1Enabled": false,
    "forceBasicAuth": true,
    "httpPort": 8085
  }
}'

create_if_missing "docker-hub" "/service/rest/v1/repositories/docker/proxy" '{
  "name": "docker-hub",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true
  },
  "proxy": {
    "remoteUrl": "https://registry-1.docker.io",
    "contentMaxAge": 1440,
    "metadataMaxAge": 1440
  },
  "negativeCache": {
    "enabled": true,
    "timeToLive": 1440
  },
  "httpClient": {
    "blocked": false,
    "autoBlock": true
  },
  "docker": {
    "v1Enabled": false,
    "forceBasicAuth": true
  },
  "dockerProxy": {
    "indexType": "HUB"
  }
}'

create_if_missing "docker-group" "/service/rest/v1/repositories/docker/group" '{
  "name": "docker-group",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true
  },
  "group": {
    "memberNames": ["docker-hosted", "docker-hub"]
  },
  "docker": {
    "v1Enabled": false,
    "forceBasicAuth": true,
    "httpPort": 8086
  }
}'

echo "Nexus bootstrap complete."
