    # Buy01 CI/CD Pipeline

🚀 **Pipeline CI/CD complet avec Jenkins pour la plateforme e-commerce Buy01**

## Vue d'ensemble

Ce projet implémente un pipeline CI/CD complet utilisant Jenkins pour automatiser le build, les tests et le déploiement de la plateforme Buy01. Le pipeline supporte les déploiements multi-environnements avec des stratégies de rollback automatique.

## Architecture CI/CD

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│   Developer     │    │     Jenkins      │    │   Target Env        │
│   Commits       │───▶│    Pipeline      │───▶│   (Dev/Staging/     │
│                 │    │                  │    │    Production)      │
└─────────────────┘    └──────────────────┘    └─────────────────────┘
                              │
                              ▼
                       ┌──────────────────┐
                       │   Notifications  │
                       │   (Email/Slack)  │
                       └──────────────────┘
```

## Fonctionnalités principales

### ✅ **Build automatisé**
- Compilation parallèle des microservices Java/Spring Boot
- Build du frontend Angular avec optimisations
- Création d'images Docker multi-stage

### 🧪 **Tests automatisés**
- **Backend**: Tests JUnit avec couverture JaCoCo
- **Frontend**: Tests Karma/Jasmine avec couverture
- **Sécurité**: Scan OWASP des dépendances
- Échec du pipeline si les tests échouent

### 🚀 **Déploiements multi-environnements**
- **Development**: Mode debug, MongoDB Express, logs détaillés
- **Staging**: Health checks, proxy Nginx, validation complète
- **Production**: Réplicas, monitoring Prometheus/Grafana, load balancer

### 🔄 **Stratégie de rollback**
- Sauvegarde automatique avant déploiement
- Rollback automatique en cas d'échec
- Script de rollback manuel avec interface conviviale

### 📢 **Notifications intelligentes**
- **Email**: Notifications HTML riches avec détails du build
- **Slack**: Messages enrichis avec boutons d'action
- Notifications conditionnelles selon le statut

### ⚙️ **Builds paramétrés (Bonus)**
- Choix de l'environnement cible
- Activation/désactivation des tests
- Sélection de la branche Git
- Contrôle du déploiement

## Démarrage rapide

### 1. Lancer Jenkins
```bash
cd jenkins
./start-jenkins.sh
```

### 2. Configuration initiale
- **Accès**: http://localhost:8090
- **Mot de passe initial**: Affiché par le script de démarrage
- **Plugins**: Installation automatique des plugins requis

### 3. Créer le job pipeline
1. **New Item** → **Pipeline** → "Buy01-CI-CD"
2. **Pipeline script from SCM** → Git → URL du repo
3. **Script Path**: `Jenkinsfile`

### 4. Configuration des variables d'environnement
```bash
# Email
EMAIL_FROM=noreply@buy01.com
EMAIL_TO=team@buy01.com
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# Slack
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/...
SLACK_CHANNEL=#deployments

# Docker
DOCKER_REGISTRY=localhost:5000
```

## Utilisation du pipeline

### Builds automatiques
- **Déclenchement**: Push sur les branches surveillées
- **Webhook**: Configuration GitHub/GitLab
- **Polling**: Vérification toutes les 5 minutes

### Builds manuels
```bash
# Interface Jenkins
1. Aller sur le job "Buy01-CI-CD"
2. "Build with Parameters"
3. Sélectionner les options désirées
4. Cliquer "Build"
```

### Paramètres disponibles
- **Environment**: `dev` | `staging` | `production`
- **Run Tests**: Exécuter les tests automatisés
- **Deploy**: Déployer après le build
- **Branch**: Branche Git à construire

## Gestion des rollbacks

### Rollback automatique
Le pipeline effectue un rollback automatique si :
- Les health checks échouent
- Les smoke tests échouent
- Une erreur critique survient

### Rollback manuel
```bash
# Lister les sauvegardes disponibles
./scripts/rollback.sh -l

# Rollback vers un build spécifique
./scripts/rollback.sh -b 123 -e production

# Rollback vers le déploiement précédent
./scripts/rollback.sh -p -e staging
```

## Structure des tests

### Backend (JUnit + JaCoCo)
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
</plugin>
```

### Frontend (Karma + Jasmine)
```json
// package.json
{
  "scripts": {
    "test:ci": "ng test --browsers=ChromeHeadless --watch=false --code-coverage"
  }
}
```

### Couverture de code
- **Objectif Backend**: 80% lignes, 70% branches
- **Objectif Frontend**: 80% statements, fonctions, lignes
- **Rapports**: Intégrés dans Jenkins avec graphiques

## Environnements de déploiement

### Development
```yaml
# docker-compose.dev.yml
services:
  user-service:
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - LOGGING_LEVEL_ROOT=DEBUG
  mongo-express:  # Interface MongoDB
    ports: ["8889:8081"]
```

### Staging
```yaml
# docker-compose.staging.yml
services:
  nginx:  # Reverse proxy
    ports: ["80:80", "443:443"]
  # Health checks activés
```

### Production
```yaml
# docker-compose.prod.yml
services:
  user-service:
    deploy:
      replicas: 2
      resources:
        limits: {memory: 1G, cpus: '0.5'}
  prometheus:  # Monitoring
  grafana:     # Dashboards
```

## Monitoring et observabilité

### Health Checks
- **Endpoints**: `/actuator/health` pour tous les services
- **Fréquence**: Vérification toutes les 30s
- **Timeout**: 10s avec 3 tentatives

### Monitoring Production
- **Prometheus**: http://localhost:9090 - Métriques système
- **Grafana**: http://localhost:3000 - Dashboards visuels
- **Logs centralisés**: Agrégation dans `./logs/`

### Alertes
- Email automatique si un service devient indisponible
- Notifications Slack pour les déploiements
- Dashboard temps réel sur Grafana

## Sécurité

### Scan des vulnérabilités
```xml
<!-- OWASP Dependency Check -->
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
    </configuration>
</plugin>
```

### Configuration sécurisée
- Mots de passe stockés dans Jenkins Credentials
- Certificats SSL pour HTTPS
- Headers de sécurité dans Nginx
- Isolation des conteneurs Docker

## Optimisations performance

### Builds parallèles
- 4 microservices buildent simultanément
- Tests frontend/backend en parallèle
- Images Docker avec cache multi-stage

### Cache et optimisations
- Cache Maven local persistant
- Cache node_modules avec volumes
- Optimisation des images Docker
- Compression gzip dans Nginx

## Dépannage

### Problèmes fréquents

**Jenkins ne démarre pas**
```bash
docker logs jenkins-buy01
docker system df  # Vérifier l'espace disque
```

**Tests échouent**
```bash
# Backend
cd microservices-architecture/user-service && mvn test

# Frontend
cd frontend && npm test
```

**Déploiement échoue**
```bash
# Vérifier les services
curl http://localhost:8080/actuator/health
docker-compose logs

# Rollback manuel
./scripts/rollback.sh -p -e staging
```

### Logs utiles
- **Jenkins**: `docker logs jenkins-buy01`
- **Applications**: `./logs/*.log`
- **Nginx**: `./nginx/logs/`

## Documentation complète

- **Setup Jenkins**: [jenkins/jenkins-setup.md](jenkins/jenkins-setup.md)
- **Scripts**: Voir le dossier `scripts/`
- **Configuration Docker**: `microservices-architecture/docker-compose/`

## Support et maintenance

### Tâches régulières
- Mise à jour des plugins Jenkins (mensuel)
- Nettoyage des artifacts anciens (automatique)
- Revue des rapports de sécurité
- Monitoring de l'utilisation disque

### Backup automatique
- Configuration Jenkins sauvegardée
- Artifacts conservés 30 jours
- État des déploiements trackés
- Base de données sauvegardée

---

**🎯 Ce pipeline CI/CD offre une solution complète, sécurisée et scalable pour le déploiement automatisé de la plateforme Buy01.**