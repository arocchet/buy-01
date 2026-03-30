# Nexus Project Guide (Buy-01)

This guide implements the Nexus assignment requirements in the current workspace and can be reused for Buy-02 with the same approach.

## 1) Prerequisites

- Java: 11+ (project currently uses Java 17, which satisfies the requirement)
- Maven: 3.8+ (compatible with Java 11+)
- Docker and Docker Compose
- Jenkins (optional for CI pipeline execution)

## 2) Setup Nexus Repository Manager (Non-Root)

### Start Nexus

Use the dedicated compose file:

```bash
docker compose -f nexus/docker-compose.nexus.yml up -d
```

Why this satisfies the requirement:

- The container runs as user `200` (`nexus` user inside the image), not `root`.
- Persistent storage is mounted to `nexus_data`.

### Retrieve initial admin password

```bash
docker exec nexus cat /nexus-data/admin.password
```

Login at `http://localhost:8081`, then change the password.

## 3) Create Repositories for JAR/WAR and Docker

Run the bootstrap script:

```bash
chmod +x scripts/nexus-bootstrap.sh
NEXUS_URL=http://localhost:8081 \
NEXUS_USER=admin \
NEXUS_PASSWORD='<your-password>' \
./scripts/nexus-bootstrap.sh
```

Repositories created:

- Maven hosted: `maven-releases`, `maven-snapshots` (store JAR/WAR)
- Maven proxy: `maven-central`
- Maven group: `maven-public`
- Docker hosted: `docker-hosted` (port 8085)
- Docker proxy: `docker-hub`
- Docker group: `docker-group` (port 8086)

## 4) Sample Web Application as Maven Project

The microservices in this workspace already use Maven:

- `microservices-architecture/user-service`
- `microservices-architecture/product-service`
- `microservices-architecture/media-service`
- `microservices-architecture/api-gateway`
- `microservices-architecture/order-service`

To apply the same to Buy-02, copy `.mvn/settings-nexus.xml` and the Jenkins Nexus stages, then adapt service paths.

## 5) Artifact Publishing (Maven -> Nexus)

Pipeline updates in `Jenkinsfile` add:

- `PUBLISH_ARTIFACTS` parameter
- Dynamic artifact version generation: `1.0.<build>-<short-sha>`
- Publish stage using Maven `deploy` with `altDeploymentRepository`
- Release repo for `main`, snapshot repo for other branches

Required Jenkins credentials:

- `nexus-username` (Secret text)
- `nexus-password` (Secret text)

Manual publish example:

```bash
export NEXUS_USER=ci-deployer
export NEXUS_PASSWORD='<password>'
export NEXUS_MAVEN_PUBLIC_URL='http://localhost:8081/repository/maven-public/'

cd microservices-architecture/product-service
mvn -B -s ../../.mvn/settings-nexus.xml \
  -DskipTests \
  -DaltDeploymentRepository=nexus-snapshots::default::http://localhost:8081/repository/maven-snapshots/ \
  deploy
```

## 6) Dependency Management Through Nexus Proxy

`.mvn/settings-nexus.xml` configures:

- Mirror of `*` to `maven-public`
- Repository and plugin repository to `maven-public`
- Credentials for release/snapshot deployment

This ensures external dependencies are fetched through Nexus instead of directly from Maven Central.

## 7) Versioning and Version Retrieval

Versioning strategy:

- CI version string: `1.0.<jenkins-build>-<git-sha>`
- Maven artifacts routed to snapshots or releases based on branch
- Docker images tagged with both `${ARTIFACT_VERSION}` and `latest`

Retrieve versions from Nexus UI:

1. Browse -> `maven-releases` or `maven-snapshots`
2. Search by `groupId`/`artifactId`
3. View and download specific versions

Retrieve versions with REST API:

```bash
curl -u admin:'<password>' \
  'http://localhost:8081/service/rest/v1/search?repository=maven-snapshots&group=com.letsplay&name=product-service'
```

## 8) Docker Integration

The pipeline stage `Docker Build & Publish to Nexus` now:

- Builds service images
- Authenticates to `localhost:8085`
- Pushes `${ARTIFACT_VERSION}` and `latest` tags

Manual Docker push example:

```bash
docker login localhost:8085
docker build -t localhost:8085/buy01/product-service:1.0.101-a1b2c3d microservices-architecture/product-service
docker push localhost:8085/buy01/product-service:1.0.101-a1b2c3d
```

## 9) Continuous Integration

The Jenkins pipeline now includes artifact publication on push (when `PUBLISH_ARTIFACTS=true`) in addition to build/test.

CI behavior:

- Build and test services
- Deploy Maven artifacts to Nexus repositories
- Build and push Docker images to Nexus Docker hosted repo

## 10) Security and Access Control (Bonus)

Use script:

```bash
chmod +x scripts/nexus-security-setup.sh
NEXUS_URL=http://localhost:8081 \
NEXUS_ADMIN_USER=admin \
NEXUS_ADMIN_PASSWORD='<admin-password>' \
CI_USER=ci-deployer \
CI_PASSWORD='<strong-password>' \
./scripts/nexus-security-setup.sh
```

This creates:

- Role: `nx-ci-deployer`
- User: `ci-deployer`
- Repository-level privileges for Maven and Docker publication

## 11) Documentation Screenshots Checklist

Capture these screenshots for evaluator submission:

1. Nexus login and admin password change page
2. Repositories list showing maven/docker hosted/proxy/group
3. Jenkins build with successful Maven publish stage
4. Jenkins build with successful Docker publish stage
5. Nexus browse view with multiple versions of an artifact
6. Nexus security role and user configuration

## 12) Requirement-to-Implementation Mapping

- Setup Nexus non-root user: `nexus/docker-compose.nexus.yml`
- Repositories JAR/WAR/Docker: `scripts/nexus-bootstrap.sh`
- Maven publish: `Jenkinsfile` publish stage + `.mvn/settings-nexus.xml`
- Dependency proxying: `.mvn/settings-nexus.xml` mirror/profile
- Versioning and retrieval: `Jenkinsfile` artifact version + section 7 commands
- Docker integration: `Jenkinsfile` docker publish stage
- CI integration: `Jenkinsfile`
- Security and RBAC bonus: `scripts/nexus-security-setup.sh`
