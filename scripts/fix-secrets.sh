#!/bin/bash

# Script pour nettoyer les secrets du repository

set -e

echo "🔒 Nettoyage des secrets exposés..."

# Supprimer les fichiers contenant des secrets
echo "Suppression des fichiers avec secrets..."
rm -f slack-integration/.env.slack
rm -f slack-integration/jenkins-env-vars.txt
rm -f slack-integration/jenkins-slack-config.xml

# Nettoyer les scripts avec l'URL en dur
echo "Nettoyage des scripts..."

# setup-jenkins-job.sh
sed -i '' 's|SLACK_WEBHOOK_URL=https://hooks.slack.com/services/.*|SLACK_WEBHOOK_URL=YOUR_SLACK_WEBHOOK_URL|g' scripts/setup-jenkins-job.sh

# test-error-scenarios.sh
sed -i '' 's|export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/.*"|export SLACK_WEBHOOK_URL="YOUR_SLACK_WEBHOOK_URL"|g' scripts/test-error-scenarios.sh

echo "✅ Secrets supprimés des fichiers"

# Créer des templates sécurisés
echo "Création de templates sécurisés..."

cat > slack-integration/.env.slack.template << 'EOF'
# Slack Configuration for Buy01 CI/CD
# Copy to .env.slack and fill with your values
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/YOUR_TEAM_ID/YOUR_CHANNEL_ID/YOUR_TOKEN
SLACK_CHANNEL=#deployments
SLACK_USERNAME=Buy01 CI/CD
SLACK_ICON_EMOJI=:shopping_cart:
EOF

cat > slack-integration/jenkins-env-vars.template << 'EOF'
# Add these to Jenkins > Manage Jenkins > Configure System > Global Properties
# Replace with your actual webhook URL

SLACK_WEBHOOK_URL=https://hooks.slack.com/services/YOUR_TEAM_ID/YOUR_CHANNEL_ID/YOUR_TOKEN
SLACK_CHANNEL=#deployments
SLACK_USERNAME=Buy01 CI/CD
SLACK_ICON_EMOJI=:shopping_cart:
EOF

# Ajouter .env.slack au .gitignore
if ! grep -q "\.env\.slack" .gitignore 2>/dev/null; then
    echo "" >> .gitignore
    echo "# Slack secrets" >> .gitignore
    echo ".env.slack" >> .gitignore
    echo "jenkins-env-vars.txt" >> .gitignore
    echo "jenkins-slack-config.xml" >> .gitignore
fi

echo "✅ Templates sécurisés créés"
echo "✅ .gitignore mis à jour"