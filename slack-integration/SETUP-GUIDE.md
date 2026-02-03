# Guide d'installation Slack pour Buy01

## 🚀 Configuration rapide de l'app Slack "Buy 01"

### 1. Configuration dans Slack API Dashboard

1. **Accédez à**: https://api.slack.com/apps
2. **Sélectionnez**: Votre app "Buy 01"

#### Activer les Incoming Webhooks
1. **Features > Incoming Webhooks**
2. **Activez**: "Activate Incoming Webhooks"
3. **"Add New Webhook to Workspace"**
4. **Sélectionnez** le canal (recommandé: #deployments ou #buy01)
5. **Copiez** l'URL du webhook (commençant par `https://hooks.slack.com/services/...`)

#### Permissions supplémentaires (optionnel)
**OAuth & Permissions > Bot Token Scopes:**
- `chat:write` - Envoyer des messages
- `chat:write.public` - Écrire dans les canaux publics

### 2. Configuration automatique avec notre script

```bash
cd slack-integration
./configure-slack.sh
```

Le script va :
- ✅ Demander l'URL du webhook
- ✅ Configurer le canal de destination
- ✅ Tester la connexion
- ✅ Créer les fichiers de configuration
- ✅ Mettre à jour les scripts

### 3. Configuration manuelle

#### Variables d'environnement Jenkins
**Manage Jenkins > Configure System > Global Properties:**

```
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/T.../B.../...
SLACK_CHANNEL=#deployments
SLACK_USERNAME=Buy01 CI/CD
SLACK_ICON_EMOJI=:shopping_cart:
```

#### Variables locales (optionnel)
Ajoutez dans `~/.bashrc` ou `~/.zshrc`:

```bash
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/..."
export SLACK_CHANNEL="#deployments"
```

### 4. Test de la configuration

#### Test rapide
```bash
./scripts/send-notification.sh --test
```

#### Test avec curl
```bash
curl -X POST \
  -H 'Content-type: application/json' \
  --data '{"text":"🧪 Test Buy01 CI/CD Pipeline"}' \
  YOUR_WEBHOOK_URL
```

#### Test d'un déploiement
```bash
./scripts/send-notification.sh "🚀 Déploiement réussi de Buy01 en production!"
```

## 📱 Canaux recommandés

### Structure des canaux
- **#deployments** - Notifications CI/CD principales
- **#buy01-alerts** - Alertes et erreurs critiques
- **#buy01-dev** - Notifications développement
- **#buy01-general** - Discussions générales du projet

### Configuration multi-canaux
Dans le Jenkinsfile, vous pouvez personnaliser selon l'environnement :

```groovy
script {
    def channel = params.ENVIRONMENT == "production" ? "#buy01-alerts" : "#deployments"
    env.SLACK_CHANNEL = channel
}
```

## 🎨 Messages personnalisés Buy01

### Types de notifications
- **✅ Déploiement réussi** - Vert avec détails complets
- **❌ Déploiement échoué** - Rouge avec logs d'erreur
- **🔄 Rollback** - Orange avec info de restauration
- **🧪 Résultats de tests** - Bleu avec métriques
- **🚧 Maintenance** - Jaune avec durée estimée

### Exemple de message enrichi
```json
{
  "attachments": [{
    "color": "#36a64f",
    "title": "🛒 Buy01 - Déploiement réussi",
    "fields": [
      {"title": "🏪 Environnement", "value": "Production"},
      {"title": "📦 Build", "value": "#42"},
      {"title": "🧪 Tests", "value": "✅ 247/247 passés"}
    ],
    "actions": [
      {"text": "🌐 Accéder à Buy01", "url": "https://buy01.com"},
      {"text": "🔍 Voir le build", "url": "http://jenkins:8090/job/42/"}
    ]
  }]
}
```

## 🔧 Intégration Jenkins

### Dans le Jenkinsfile
Les notifications sont automatiquement envoyées dans les sections :
- `post { success }` - Déploiement réussi
- `post { failure }` - Échec avec rollback
- `post { unstable }` - Tests partiellement échoués

### Notifications personnalisées
```groovy
script {
    sh '''
        ./scripts/send-notification.sh \
            "🚀 Buy01 v${BUILD_NUMBER} déployé en ${ENVIRONMENT}"
    '''
}
```

## 📊 Monitoring intégré

### Liens automatiques
- **Dashboard Jenkins** - Lien direct vers le build
- **Application** - Accès à l'env déployé selon l'environnement
- **Logs** - Console de build pour débuggage
- **Grafana** - Métriques de l'application (production)

### Health checks dans Slack
```bash
# Envoi automatique si service down
if ! curl -f http://localhost:8080/health; then
    ./scripts/send-notification.sh \
        "🚨 Service Buy01 indisponible en ${ENVIRONMENT}"
fi
```

## 🎯 Meilleures pratiques

### Fréquence des notifications
- **Production**: Toutes les notifications
- **Staging**: Déploiements + échecs uniquement
- **Development**: Échecs uniquement

### Format des messages
- **Émojis** pour identification rapide
- **Couleurs** selon la criticité
- **Boutons d'action** pour accès rapide
- **Métadonnées** complètes dans les threads

### Gestion des alertes
- **@channel** uniquement pour production critique
- **Threads** pour les détails techniques
- **Réactions** pour accuser réception

## 🛠️ Dépannage

### Webhook ne fonctionne pas
```bash
# Tester avec curl
curl -v -X POST YOUR_WEBHOOK_URL \
  -H 'Content-type: application/json' \
  -d '{"text":"Test"}'

# Vérifier les permissions de l'app Slack
# Régénérer le webhook si nécessaire
```

### Messages non reçus
- Vérifier que le canal existe
- Confirmer les permissions de l'app
- Vérifier les variables d'environnement Jenkins

### Format incorrect
- Valider le JSON avec un validateur
- Tester les templates avec des données statiques
- Vérifier l'encoding des caractères spéciaux

---

**🎉 Votre app Slack "Buy 01" est maintenant prête à recevoir toutes les notifications CI/CD de la plateforme e-commerce !**

Pour toute question, consultez la [documentation Slack API](https://api.slack.com/messaging/webhooks) ou les logs Jenkins.