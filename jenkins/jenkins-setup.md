# Jenkins CI/CD Setup Guide for Buy01

## Quick Start

1. **Start Jenkins:**
   ```bash
   cd jenkins
   ./start-jenkins.sh
   ```

2. **Access Jenkins:**
   - URL: http://localhost:8090
   - Get initial password: `docker exec jenkins-buy01 cat /var/jenkins_home/secrets/initialAdminPassword`

## Initial Configuration

### 1. Jenkins Setup
1. Access Jenkins at http://localhost:8090
2. Use the initial admin password displayed by the startup script
3. Install suggested plugins + additional plugins from `jenkins-plugins.txt`
4. Create admin user

### 2. Required Tools Configuration
Go to **Manage Jenkins > Global Tool Configuration**:

- **Maven**: Add Maven 3.8.x
- **Node.js**: Add Node.js 18.x
- **Docker**: Ensure Docker is available

### 3. Environment Variables
Set in **Manage Jenkins > Configure System > Global properties**:

```
DOCKER_REGISTRY=localhost:5000
EMAIL_SMTP_SERVER=smtp.gmail.com
EMAIL_FROM=noreply@buy01.com
EMAIL_TO=team@buy01.com
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/...
```

### 4. Email Configuration
In **Manage Jenkins > Configure System > E-mail Notification**:
- SMTP server: smtp.gmail.com
- Use SMTP Authentication: Yes
- Username: your-email@gmail.com
- Password: your-app-password
- Use SSL: Yes
- SMTP Port: 465

### 5. Slack Configuration
In **Manage Jenkins > Configure System > Slack**:
- Workspace: your-workspace
- Credential: Add Slack token
- Default channel: #deployments

## Creating the Pipeline Job

### 1. New Pipeline Job
1. **New Item** → **Pipeline** → Name: "Buy01-CI-CD"
2. **Pipeline > Definition**: Pipeline script from SCM
3. **SCM**: Git
4. **Repository URL**: Your repository URL
5. **Script Path**: Jenkinsfile

### 2. Build Triggers
- **Poll SCM**: `H/5 * * * *` (every 5 minutes)
- **GitHub hook trigger**: Enable for webhook support

### 3. Build Parameters
The pipeline includes these parameters:
- **ENVIRONMENT**: dev/staging/production
- **RUN_TESTS**: boolean
- **DEPLOY**: boolean
- **BRANCH**: git branch

## Pipeline Features

### Automated Testing
- **Backend**: JUnit tests with JaCoCo coverage
- **Frontend**: Karma/Jasmine tests with coverage
- **Security**: OWASP dependency check

### Multi-Environment Deployment
- **Development**: Full debugging, MongoDB Express
- **Staging**: Health checks, Nginx proxy
- **Production**: Replicas, monitoring, load balancer

### Rollback Strategy
Automatic rollback on failure:
```bash
# Manual rollback
./scripts/rollback.sh -b 123 -e production
./scripts/rollback.sh -p -e staging  # Previous deployment
./scripts/rollback.sh -l  # List backups
```

### Notifications
- **Email**: HTML notifications with build details
- **Slack**: Rich notifications with buttons

## Monitoring and Health Checks

### Actuator Endpoints
- Health: `/actuator/health`
- Info: `/actuator/info`
- Metrics: `/actuator/metrics`

### Production Monitoring
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)

## Security Best Practices

### 1. Secrets Management
Use Jenkins Credentials:
- Database passwords
- JWT secrets
- SMTP passwords
- Slack tokens

### 2. OWASP Security
- Dependency vulnerability scanning
- Security headers in Nginx
- Container security scanning

### 3. Access Control
- Role-based access control
- SSH keys for Git access
- Secure Jenkins configuration

## Troubleshooting

### Common Issues

1. **Jenkins Won't Start**
   ```bash
   docker logs jenkins-buy01
   docker exec -it jenkins-buy01 bash
   ```

2. **Build Failures**
   - Check Maven/Node.js versions
   - Verify Docker daemon access
   - Check workspace permissions

3. **Test Failures**
   ```bash
   # Backend tests
   cd microservices-architecture/user-service
   mvn test

   # Frontend tests
   cd frontend
   npm test
   ```

4. **Deployment Issues**
   ```bash
   # Check service health
   curl http://localhost:8080/actuator/health

   # View logs
   docker-compose logs
   ```

### Performance Optimization

1. **Parallel Builds**
   - Backend services build in parallel
   - Frontend builds independently
   - Test stages run concurrently

2. **Caching**
   - Maven local repository
   - Node.js modules
   - Docker layer caching

3. **Resource Management**
   - Memory limits for containers
   - CPU allocation
   - Build agent scaling

## Advanced Features

### 1. Multi-Branch Pipelines
Support for feature branch builds:
```groovy
// In Jenkinsfile
when {
    anyOf {
        branch 'main'
        branch 'develop'
        branch 'feature/*'
    }
}
```

### 2. Blue-Green Deployment
Zero-downtime deployments:
```bash
# Deploy to blue environment
docker-compose -f docker-compose.blue.yml up -d

# Health check and switch traffic
# Cleanup green environment
```

### 3. GitOps Integration
Automatic deployment on Git changes:
- Webhook configuration
- Automated testing
- Progressive deployment

## Maintenance

### Regular Tasks
- Update Jenkins plugins monthly
- Review security reports
- Clean up old builds and artifacts
- Monitor disk usage
- Update base images

### Backup Strategy
- Jenkins configuration backup
- Build artifacts retention (30 days)
- Database backups
- Deployment state tracking

## Support

### Logs Location
- Jenkins: `docker logs jenkins-buy01`
- Application: `./logs/`
- Nginx: `./nginx/logs/`

### Useful Commands
```bash
# Restart Jenkins
docker restart jenkins-buy01

# Clean workspace
docker system prune -f

# View pipeline logs
docker logs -f jenkins-buy01

# Access Jenkins shell
docker exec -it jenkins-buy01 bash
```