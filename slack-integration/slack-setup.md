# Configuration Slack pour Buy01 CI/CD

## 1. Configuration de l'app Slack "Buy 01"

### Étapes dans Slack API Dashboard

1. **Accéder à votre app**: https://api.slack.com/apps
2. **Sélectionner**: "Buy 01" app

### 2. Configurer les Incoming Webhooks

1. **Features > Incoming Webhooks**
2. **Activer**: "Activate Incoming Webhooks" = ON
3. **Add New Webhook to Workspace**
4. **Sélectionner**: Canal de destination (ex: #deployments, #ci-cd, #buy01)
5. **Copier**: L'URL du webhook généré

### 3. Permissions OAuth (Optionnel pour fonctionnalités avancées)

**OAuth & Permissions > Scopes > Bot Token Scopes:**
```
chat:write          # Envoyer des messages
chat:write.public   # Écrire dans les canaux publics
channels:read       # Lire les informations des canaux
groups:read         # Lire les informations des groupes privés
```

### 4. Configuration des variables d'environnement

**Pour Jenkins:**
```bash
# Dans Jenkins > Manage Jenkins > Configure System > Global Properties
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/T.../B.../...
SLACK_CHANNEL=#deployments
SLACK_BOT_TOKEN=xoxb-... (si vous utilisez l'API)
```

**Pour les scripts locaux:**
```bash
# Dans votre ~/.bashrc ou ~/.zshrc
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/T.../B.../..."
export SLACK_CHANNEL="#deployments"
```

## 2. Test de la configuration

### Test avec curl
```bash
curl -X POST \
  -H 'Content-type: application/json' \
  --data '{"text":"🧪 Test de l'\''app Buy01 - CI/CD Pipeline configuré!"}' \
  YOUR_WEBHOOK_URL
```

### Test avec notre script
```bash
# Test basique
./scripts/send-notification.sh --test

# Test avec message personnalisé
./scripts/send-notification.sh "🚀 Déploiement réussi de Buy01 en production!"
```

## 3. Messages enrichis pour Buy01

### Format des notifications
- **Build Success**: ✅ avec couleur verte
- **Build Failed**: ❌ avec couleur rouge
- **Deployment Warning**: ⚠️ avec couleur orange
- **Rollback**: 🔄 avec détails de la version

### Exemple de payload Slack
```json
{
    "channel": "#deployments",
    "username": "Buy01 CI/CD",
    "icon_emoji": ":shopping_cart:",
    "attachments": [
        {
            "color": "#36a64f",
            "title": "✅ Buy01 - Déploiement réussi",
            "text": "La plateforme e-commerce Buy01 a été déployée avec succès!",
            "fields": [
                {
                    "title": "Environnement",
                    "value": "Production",
                    "short": true
                },
                {
                    "title": "Version",
                    "value": "v1.2.3 (build #42)",
                    "short": true
                }
            ],
            "actions": [
                {
                    "type": "button",
                    "text": "Voir le build",
                    "url": "http://jenkins:8090/job/Buy01-CI-CD/42/"
                },
                {
                    "type": "button",
                    "text": "Accéder au site",
                    "url": "https://buy01.com"
                }
            ]
        }
    ]
}
```