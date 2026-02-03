# Configuration Slack pour Buy01 CI/CD

## Configuration du Webhook Slack dans Jenkins

Pour recevoir les notifications Slack, configurez la variable d'environnement dans Jenkins :

### 1. Dans Jenkins (http://localhost:8090)

1. **Manage Jenkins** → **Configure System**
2. **Global Properties** → **Environment Variables**
3. **Ajouter** :
   - **Name** : `SLACK_WEBHOOK_URL`
   - **Value** : `https://hooks.slack.com/services/VOTRE_WORKSPACE/VOTRE_CHANNEL/VOTRE_TOKEN`

### 2. Types de notifications

- 🚀 **Début de déploiement** : Notifie quand le déploiement commence
- ✅ **Succès** : Déploiement réussi avec liens vers l'application
- ❌ **Échec** : Erreur avec lien vers les logs Jenkins
- ⚠️ **Instable** : Tests échoués mais déploiement effectué

### 3. Format des messages

Les notifications incluent :
- 🎯 Environnement (dev/staging/production)
- 📊 Numéro de build
- 🔗 Liens vers Jenkins et l'application
- 📋 Détails du statut

### 4. Canal Slack

Par défaut : `#deployments`

Pour changer, configurez aussi `SLACK_CHANNEL` dans Jenkins.

## Test des notifications

```bash
# Tester les notifications
export SLACK_WEBHOOK_URL="votre_webhook_url"
./scripts/send-notification.sh --test
```

---

**Note** : Le webhook est configuré dans Jenkins pour éviter d'exposer les secrets dans le code.