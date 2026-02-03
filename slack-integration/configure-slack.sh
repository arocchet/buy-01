#!/bin/bash

# Configuration script for Buy01 Slack integration
# This script helps configure Slack notifications for the CI/CD pipeline

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}🛠️  Configuration Slack pour Buy01 CI/CD${NC}"
echo ""

# Function to validate webhook URL
validate_webhook_url() {
    local url="$1"
    if [[ ! "$url" =~ ^https://hooks\.slack\.com/services/.* ]]; then
        echo -e "${RED}❌ URL de webhook Slack invalide${NC}"
        return 1
    fi
    return 0
}

# Function to test Slack webhook
test_slack_webhook() {
    local webhook_url="$1"
    local channel="$2"

    echo -e "${YELLOW}🧪 Test de connexion Slack...${NC}"

    local test_payload=$(cat << EOF
{
    "channel": "$channel",
    "username": "Buy01 CI/CD",
    "icon_emoji": ":shopping_cart:",
    "text": "🧪 Test de configuration - App Buy01 connectée avec succès!",
    "attachments": [
        {
            "color": "#36a64f",
            "title": "✅ Configuration Slack réussie",
            "text": "L'app Buy01 peut maintenant envoyer des notifications CI/CD",
            "footer": "Buy01 E-commerce Platform",
            "ts": $(date +%s)
        }
    ]
}
EOF
)

    if curl -s -X POST \
        -H 'Content-type: application/json' \
        --data "$test_payload" \
        "$webhook_url" | grep -q "ok"; then
        echo -e "${GREEN}✅ Test Slack réussi! Vérifiez le canal $channel${NC}"
        return 0
    else
        echo -e "${RED}❌ Test Slack échoué${NC}"
        return 1
    fi
}

# Function to create Jenkins credentials
create_jenkins_credentials() {
    local webhook_url="$1"
    local channel="$2"

    echo -e "${BLUE}📝 Création des credentials Jenkins...${NC}"

    cat > jenkins-slack-config.xml << EOF
<com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>
  <scope>GLOBAL</scope>
  <id>slack-webhook</id>
  <description>Slack Webhook for Buy01 CI/CD</description>
  <username>buy01-cicd</username>
  <password>$webhook_url</password>
</com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>
EOF

    echo "Fichier jenkins-slack-config.xml créé"
    echo "Importez-le dans Jenkins > Manage Jenkins > Manage Credentials"
}

# Interactive configuration
echo -e "${YELLOW}📋 Configuration interactive Slack${NC}"
echo ""

# Get webhook URL
while true; do
    echo -n "Entrez l'URL du webhook Slack: "
    read -r webhook_url

    if validate_webhook_url "$webhook_url"; then
        break
    fi
    echo "Veuillez entrer une URL valide (https://hooks.slack.com/services/...)"
done

# Get channel
echo -n "Canal Slack (défaut: #deployments): "
read -r channel
channel=${channel:-"#deployments"}

# Test the configuration
if test_slack_webhook "$webhook_url" "$channel"; then
    # Create environment file
    cat > .env.slack << EOF
# Slack Configuration for Buy01 CI/CD
SLACK_WEBHOOK_URL=$webhook_url
SLACK_CHANNEL=$channel
SLACK_USERNAME=Buy01 CI/CD
SLACK_ICON_EMOJI=:shopping_cart:
EOF

    # Create Jenkins environment variables
    cat > jenkins-env-vars.txt << EOF
# Add these to Jenkins > Manage Jenkins > Configure System > Global Properties

SLACK_WEBHOOK_URL=$webhook_url
SLACK_CHANNEL=$channel
SLACK_USERNAME=Buy01 CI/CD
SLACK_ICON_EMOJI=:shopping_cart:
EOF

    # Update notification script with Buy01 specific settings
    sed -i.bak "s|SLACK_CHANNEL=\${SLACK_CHANNEL:-#deployments}|SLACK_CHANNEL=\${SLACK_CHANNEL:-$channel}|" ../scripts/send-notification.sh

    echo ""
    echo -e "${GREEN}🎉 Configuration Slack terminée!${NC}"
    echo ""
    echo "Fichiers créés:"
    echo "- .env.slack (variables d'environnement)"
    echo "- jenkins-env-vars.txt (variables Jenkins)"
    echo ""
    echo -e "${BLUE}Prochaines étapes:${NC}"
    echo "1. Ajouter les variables dans Jenkins (voir jenkins-env-vars.txt)"
    echo "2. Redémarrer Jenkins si nécessaire"
    echo "3. Tester avec: ../scripts/send-notification.sh --test"

    # Optional: Create Jenkins credentials
    echo ""
    echo -n "Créer les credentials Jenkins automatiquement? (y/N): "
    read -r create_creds
    if [[ "$create_creds" =~ ^[Yy]$ ]]; then
        create_jenkins_credentials "$webhook_url" "$channel"
    fi

else
    echo -e "${RED}❌ Configuration échouée${NC}"
    exit 1
fi