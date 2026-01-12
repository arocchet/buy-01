# 🚀 Comment tester le pipeline CI/CD Buy01

## 📋 Méthodes de déclenchement du pipeline

### 1. 🔄 **Déclenchement automatique (recommandé)**

Avec la configuration actuelle, le pipeline se déclenche automatiquement :

```groovy
triggers {
    pollSCM('H/2 * * * *')  // Vérifie les changements toutes les 2 min
    cron('@daily')          // Build quotidien automatique
}
```

**🧪 Test du déclenchement automatique :**

```bash
# 1. Faire un changement dans le code
echo "# Test pipeline auto-trigger $(date)" >> README.md

# 2. Commit et push
git add README.md
git commit -m "Test: trigger automatique pipeline"
git push origin main

# 3. Attendre 2 minutes maximum
# 4. Vérifier sur Jenkins: http://localhost:8090
```

### 2. 🖱️ **Déclenchement manuel Jenkins UI**

**Étapes :**
1. Aller sur http://localhost:8090
2. Cliquer sur le job "buy-01-CI-CD"
3. Cliquer "Build with Parameters"
4. Sélectionner :
   - Environment: `dev`
   - RUN_TESTS: `true`
   - DEPLOY: `true`
   - BRANCH: `main`
5. Cliquer "Build"

### 3. ⚡ **Déclenchement via API Jenkins**

```bash
# Trigger avec curl (si Jenkins sans auth)
curl -X POST "http://localhost:8090/job/buy-01-CI-CD/buildWithParameters" \
  -d "ENVIRONMENT=dev&RUN_TESTS=true&DEPLOY=true&BRANCH=main"
```

---

## 📱 **Monitoring en temps réel**

### **Slack Notifications**
Avec votre webhook configuré, vous recevez automatiquement :
- ✅ **Démarrage du build**
- 🧪 **Résultats des tests**
- 🚀 **Status du déploiement**
- ❌ **Alertes en cas d'échec**

### **Jenkins Dashboard**
- **Console Output** : Logs en temps réel
- **Test Results** : Rapports JUnit + Karma
- **Artifacts** : JAR files et build artifacts

---

## 🧪 **Scénarios de test complets**

### **Test 1 : Build réussi**
```bash
# Modification mineure
echo "// Amélioration $(date)" >> frontend/src/styles.css
git add . && git commit -m "feat: amélioration CSS"
git push

# Résultat attendu dans Slack :
# ✅ Build #X réussi - Déployé en dev
```

### **Test 2 : Simulation d'échec de test**
```bash
# Créer un test qui échoue temporairement
echo 'it("should fail", () => { expect(true).toBe(false); });' >> frontend/src/app/test-fail.spec.ts
git add . && git commit -m "test: simulation d'échec"
git push

# Résultat attendu :
# ❌ Build #X échoué - Tests frontend
# 🔄 Rollback automatique activé
```

### **Test 3 : Déploiement multi-environnement**
```bash
# Via Jenkins UI : Build with Parameters
# Environment = staging
# Résultat : Déploiement avec health checks + Nginx
```

---

## 🔍 **Vérification du pipeline**

### **1. Pipeline démarré ?**
```bash
# Vérifier les logs Jenkins
curl -s http://localhost:8090/job/buy-01-CI-CD/lastBuild/api/json | jq '.building'
# true = en cours, false = terminé
```

### **2. Notifications fonctionnelles ?**
```bash
# Test manuel Slack
export SLACK_WEBHOOK_URL="YOUR_SLACK_WEBHOOK_URL"
./scripts/send-notification.sh "🧪 Test manuel - $(date)"
```

### **3. Health checks OK ?**
```bash
# Vérifier les services après déploiement
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Product Service
curl http://localhost:8083/actuator/health  # Media Service
```

---

## 🚨 **Dépannage**

### **Pipeline ne se déclenche pas ?**

1. **Vérifier la configuration Jenkins :**
```bash
# Job configuré avec Git SCM ?
curl -s http://localhost:8090/job/buy-01-CI-CD/config.xml | grep -i "git\|scm"
```

2. **Vérifier les permissions Git :**
```bash
# Jenkins peut accéder au repo ?
ls -la /Users/pierrecaboor/IdeaProjects/buy-01/.git
```

3. **Forcer un poll manuel :**
   - Jenkins UI → Job → "Poll SCM" dans le menu

### **Build échoue ?**

1. **Consulter les logs :**
   - Jenkins UI → Build → "Console Output"

2. **Vérifier les dépendances :**
```bash
# Docker running ?
docker ps

# Services accessibles ?
curl http://localhost:8090  # Jenkins
```

3. **Rollback si nécessaire :**
```bash
./scripts/rollback.sh -l              # Lister sauvegardes
./scripts/rollback.sh -p -e dev       # Rollback précédent
```

---

## 📊 **Indicateurs de succès**

### ✅ **Pipeline fonctionnel :**
- [ ] Jenkins accessible sur http://localhost:8090
- [ ] Job "buy-01-CI-CD" visible
- [ ] Git commits déclenchent builds (2 min max)
- [ ] Notifications Slack reçues
- [ ] Tests exécutés automatiquement
- [ ] Déploiement multi-environnement
- [ ] Health checks validés
- [ ] Rollback disponible

### 📈 **Métriques à surveiller :**
- **Build time** : < 10 minutes typique
- **Test coverage** : > 80% backend, > 80% frontend
- **Deployment time** : < 5 minutes per environment
- **Rollback time** : < 2 minutes

---

## 🎯 **Workflow complet de test**

```bash
# 1. Démarrer l'environnement
./jenkins/start-jenkins.sh
./run.sh  # Services Buy01

# 2. Configurer le job Jenkins (une seule fois)
# Via UI : Pipeline script from SCM + Git repo + Jenkinsfile

# 3. Configurer variables d'environnement (une seule fois)
# SLACK_WEBHOOK_URL, SLACK_CHANNEL dans Jenkins

# 4. Tester le pipeline
echo "Test $(date)" >> README.md
git add . && git commit -m "test: pipeline trigger"
git push

# 5. Surveiller dans Slack + Jenkins UI
```

---

**🎉 Votre pipeline CI/CD Buy01 est maintenant prêt pour des tests complets avec déclenchement automatique !**

**Prochaine étape :** Faire votre premier commit pour voir le pipeline s'exécuter automatiquement.