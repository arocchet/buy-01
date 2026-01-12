pipeline {
    agent any

    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'staging', 'production'],
            description: 'Target deployment environment'
        )
        booleanParam(
            name: 'RUN_TESTS',
            defaultValue: true,
            description: 'Run automated tests'
        )
        booleanParam(
            name: 'DEPLOY',
            defaultValue: true,
            description: 'Deploy after successful build'
        )
        string(
            name: 'BRANCH',
            defaultValue: 'cicd-production',
            description: 'Git branch to build'
        )
        string(
            name: 'GIT_URL',
            defaultValue: 'https://github.com/arocchet/buy-01.git',
            description: 'Git repository URL'
        )
    }

    triggers {
        // Poll SCM every 2 minutes for changes
        pollSCM('H/2 * * * *')

        // Optional: Cron trigger for nightly builds
        cron('@daily')
    }


    environment {
        DOCKER_REGISTRY = 'localhost:5000'
        APP_NAME = 'buy01'
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "🔄 Using workspace files (already checked out)"
                }
            }
        }

        stage('Build Info') {
            steps {
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: "git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()
                    
                    echo "🏗️ Build Information:"
                    echo "Environment: ${params.ENVIRONMENT}"
                    echo "Branch: ${params.BRANCH}"
                    echo "Commit: ${env.GIT_COMMIT_SHORT}"
                    echo "Build Number: ${env.BUILD_NUMBER}"
                }
            }
        }

        stage('Backend - Build & Test') {
            parallel {
                stage('User Service') {
                    steps {
                        dir('microservices-architecture/user-service') {
                            script {
                                echo "🔨 Building User Service..."
                                sh 'mvn clean compile'

                                if (params.RUN_TESTS) {
                                    echo "🧪 Testing User Service..."
                                    sh 'mvn test'
                                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                                    publishCoverage adapters: [
                                        jacocoAdapter('target/site/jacoco/jacoco.xml')
                                    ]
                                }

                                echo "📦 Packaging User Service..."
                                sh 'mvn package -DskipTests'
                            }
                        }
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: 'microservices-architecture/user-service/target/*.jar', allowEmptyArchive: true
                        }
                    }
                }

                stage('Product Service') {
                    steps {
                        dir('microservices-architecture/product-service') {
                            script {
                                echo "🔨 Building Product Service..."
                                sh 'mvn clean compile'

                                if (params.RUN_TESTS) {
                                    echo "🧪 Testing Product Service..."
                                    sh 'mvn test'
                                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                                }

                                echo "📦 Packaging Product Service..."
                                sh 'mvn package -DskipTests'
                            }
                        }
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: 'microservices-architecture/product-service/target/*.jar', allowEmptyArchive: true
                        }
                    }
                }

                stage('Media Service') {
                    steps {
                        dir('microservices-architecture/media-service') {
                            script {
                                echo "🔨 Building Media Service..."
                                sh 'mvn clean compile'

                                if (params.RUN_TESTS) {
                                    echo "🧪 Testing Media Service..."
                                    sh 'mvn test'
                                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                                }

                                echo "📦 Packaging Media Service..."
                                sh 'mvn package -DskipTests'
                            }
                        }
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: 'microservices-architecture/media-service/target/*.jar', allowEmptyArchive: true
                        }
                    }
                }

                stage('API Gateway') {
                    steps {
                        dir('microservices-architecture/api-gateway') {
                            script {
                                echo "🔨 Building API Gateway..."
                                sh 'mvn clean compile'

                                if (params.RUN_TESTS) {
                                    echo "🧪 Testing API Gateway..."
                                    sh 'mvn test'
                                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                                }

                                echo "📦 Packaging API Gateway..."
                                sh 'mvn package -DskipTests'
                            }
                        }
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: 'microservices-architecture/api-gateway/target/*.jar', allowEmptyArchive: true
                        }
                    }
                }
            }
        }

        stage('Frontend - Build & Test') {
            steps {
                dir('frontend') {
                    script {
                        echo "🔨 Building Angular Frontend..."
                        sh 'npm ci'

                        if (params.RUN_TESTS) {
                            echo "🧪 Testing Frontend..."
                            sh 'npm run test -- --browsers=ChromeHeadless --watch=false --code-coverage'
                            publishTestResults testResultsPattern: 'coverage/lcov.info'
                            publishCoverage adapters: [
                                lcovAdapter('coverage/lcov.info')
                            ]
                        }

                        echo "📦 Building Frontend for ${params.ENVIRONMENT}..."
                        sh "npm run build -- --configuration=${params.ENVIRONMENT}"
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'frontend/dist/**/*', allowEmptyArchive: true
                }
            }
        }

        stage('Docker Build') {
            when {
                expression { params.DEPLOY }
            }
            parallel {
                stage('Build Backend Images') {
                    steps {
                        script {
                            echo "🐳 Building Docker images for backend services..."

                            def services = ['user-service', 'product-service', 'media-service', 'api-gateway']
                            services.each { service ->
                                echo "Building ${service}..."
                                sh """
                                    cd microservices-architecture/${service}
                                    docker build -t ${env.DOCKER_REGISTRY}/${env.APP_NAME}-${service}:${env.BUILD_NUMBER} .
                                    docker tag ${env.DOCKER_REGISTRY}/${env.APP_NAME}-${service}:${env.BUILD_NUMBER} ${env.DOCKER_REGISTRY}/${env.APP_NAME}-${service}:latest
                                """
                            }
                        }
                    }
                }

                stage('Build Frontend Image') {
                    steps {
                        dir('frontend') {
                            script {
                                echo "🐳 Building Frontend Docker image..."
                                sh """
                                    docker build -t ${env.DOCKER_REGISTRY}/${env.APP_NAME}-frontend:${env.BUILD_NUMBER} .
                                    docker tag ${env.DOCKER_REGISTRY}/${env.APP_NAME}-frontend:${env.BUILD_NUMBER} ${env.DOCKER_REGISTRY}/${env.APP_NAME}-frontend:latest
                                """
                            }
                        }
                    }
                }
            }
        }

        stage('Security Scan') {
            when {
                expression { params.RUN_TESTS }
            }
            parallel {
                stage('Backend Security') {
                    steps {
                        script {
                            echo "🔒 Running security scans on backend..."
                            // OWASP dependency check for Java services
                            sh '''
                                cd microservices-architecture/user-service
                                mvn org.owasp:dependency-check-maven:check
                            '''
                        }
                    }
                }

                stage('Frontend Security') {
                    steps {
                        dir('frontend') {
                            script {
                                echo "🔒 Running npm audit on frontend..."
                                sh 'npm audit --audit-level moderate || true'
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy to Environment') {
            when {
                expression { params.DEPLOY }
            }
            steps {
                script {
                    echo "🚀 Deploying to ${params.ENVIRONMENT} environment..."

                    // Create backup before deployment
                    sh '''
                        echo "📋 Creating backup of current deployment..."
                        mkdir -p deployments/backups/${BUILD_NUMBER}
                        cp -r microservices-architecture/docker-compose deployments/backups/${BUILD_NUMBER}/
                    '''

                    // Deploy based on environment
                    switch(params.ENVIRONMENT) {
                        case 'dev':
                            sh '''
                                echo "🔧 Deploying to Development..."
                                cd microservices-architecture/docker-compose
                                docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
                            '''
                            break
                        case 'staging':
                            sh '''
                                echo "🎭 Deploying to Staging..."
                                cd microservices-architecture/docker-compose
                                docker-compose -f docker-compose.yml -f docker-compose.staging.yml up -d
                            '''
                            break
                        case 'production':
                            sh '''
                                echo "🏭 Deploying to Production..."
                                cd microservices-architecture/docker-compose
                                docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
                            '''
                            break
                    }
                }
            }
        }

        stage('Health Check') {
            when {
                expression { params.DEPLOY }
            }
            steps {
                script {
                    echo "🏥 Running health checks..."

                    def services = [
                        'API Gateway': 'http://localhost:8080/actuator/health',
                        'User Service': 'http://localhost:8081/actuator/health',
                        'Product Service': 'http://localhost:8082/actuator/health',
                        'Media Service': 'http://localhost:8083/actuator/health'
                    ]

                    services.each { name, url ->
                        retry(5) {
                            sleep(10)
                            sh "curl -f ${url} || exit 1"
                            echo "✅ ${name} is healthy"
                        }
                    }
                }
            }
        }

        stage('Smoke Tests') {
            when {
                expression { params.DEPLOY && params.RUN_TESTS }
            }
            steps {
                script {
                    echo "💨 Running smoke tests..."
                    sh '''
                        # Test API Gateway
                        curl -f http://localhost:8080/actuator/info

                        # Test basic endpoints
                        curl -f http://localhost:8080/api/products

                        echo "✅ Smoke tests passed"
                    '''
                }
            }
        }
    }

    post {
        always {
            script {
                echo "🧹 Cleaning up workspace..."
                deleteDir()
            }
        }
        success {
            script {
                echo "✅ Build completed successfully!"

                // Send success notification (commented out - no email server)
                echo "✅ Email: Build Success - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            }
        }
        failure {
            script {
                echo "❌ Build failed!"

                // Rollback if deployment failed
                if (params.DEPLOY) {
                    echo "🔄 Initiating rollback..."
                    sh '''
                        if [ -d "deployments/backups/${BUILD_NUMBER}" ]; then
                            echo "📁 Restoring from backup..."
                            cp -r deployments/backups/${BUILD_NUMBER}/* microservices-architecture/docker-compose/
                            cd microservices-architecture/docker-compose
                            docker-compose up -d
                            echo "✅ Rollback completed"
                        fi
                    '''
                }

                // Send failure notification (commented out - no email server)
                echo "❌ Email: Build Failed - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            }
        }
        unstable {
            script {
                echo "⚠️ Build unstable (some tests failed)"

                echo "⚠️ Email: Build Unstable - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            }
        }
    }
}