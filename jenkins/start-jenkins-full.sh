#!/bin/bash

# Script pour démarrer Jenkins avec Maven, Node.js et Docker
# pour la pipeline Buy01 CI/CD complète

set -e

echo "🚀 Création de Jenkins avec Maven, Node.js et Docker..."

# Créer un Dockerfile personnalisé pour Jenkins
cat > Dockerfile.jenkins << 'EOF'
FROM jenkins/jenkins:lts

# Passer en root pour installer les outils
USER root

# Installer Docker, Maven et Node.js
RUN apt-get update && apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    maven \
    wget

# Installer Docker CLI
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
RUN echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
RUN apt-get update && apt-get install -y docker-ce-cli

# Installer Node.js 18
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
RUN apt-get install -y nodejs

# Installer Angular CLI globalement
RUN npm install -g @angular/cli

# Vérifier les installations
RUN java -version
RUN mvn -version
RUN node -version
RUN npm -version
RUN docker --version

# Retourner à l'utilisateur jenkins
USER jenkins

# Installer les plugins Jenkins essentiels
RUN jenkins-plugin-cli --plugins \
    git \
    workflow-aggregator \
    pipeline-stage-view \
    build-timeout \
    credentials-binding \
    timestamper \
    ws-cleanup \
    ant \
    gradle \
    pipeline-github-lib \
    pipeline-stage-view \
    ssh-slaves \
    matrix-auth \
    pam-auth \
    ldap \
    email-ext \
    mailer
EOF

echo "🔨 Construction de l'image Jenkins personnalisée..."
docker build -f Dockerfile.jenkins -t jenkins-buy01-custom .

echo "🗑️ Nettoyage de l'ancien conteneur..."
docker stop jenkins-buy01 2>/dev/null || true
docker rm jenkins-buy01 2>/dev/null || true

echo "🚀 Démarrage de Jenkins avec tous les outils..."
docker run -d \
  --name jenkins-buy01 \
  --restart=unless-stopped \
  -p 8090:8080 \
  -p 50000:50000 \
  -v jenkins-data:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$HOME"/.m2:/var/jenkins_home/.m2 \
  jenkins-buy01-custom

echo "⏳ Attente du démarrage de Jenkins..."
sleep 45

echo "🔍 Vérification des outils installés..."
docker exec jenkins-buy01 java -version
docker exec jenkins-buy01 mvn -version
docker exec jenkins-buy01 node -version
docker exec jenkins-buy01 docker --version

echo "🎉 Jenkins avec Maven, Node.js et Docker est prêt!"
echo "🔗 Accès: http://localhost:8090"

# Nettoyer le Dockerfile temporaire
rm -f Dockerfile.jenkins

echo ""
echo "✅ Tous les outils sont maintenant disponibles pour la vraie pipeline !"