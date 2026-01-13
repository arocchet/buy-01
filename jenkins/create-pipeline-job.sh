#!/bin/bash

# Script pour créer automatiquement le job Pipeline Jenkins
# Ce job utilisera le Jenkinsfile de la branche cicd-production

set -e

JENKINS_URL="http://localhost:8090"
JOB_NAME="Buy01-Pipeline"
REPO_URL="https://github.com/arocchet/buy-01.git"
BRANCH="cicd-production"

echo "🚀 Création du job Pipeline Jenkins pour Buy01..."

# Configuration XML du job Pipeline
cat > pipeline-job-config.xml << EOF
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job">
  <actions/>
  <description>Pipeline CI/CD automatique pour la plateforme e-commerce Buy01</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
      <triggers>
        <hudson.triggers.SCMTrigger>
          <spec>H/2 * * * *</spec>
          <ignorePostCommitHooks>false</ignorePostCommitHooks>
        </hudson.triggers.SCMTrigger>
      </triggers>
    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
    <hudson.model.ParametersDefinitionProperty>
      <parameterDefinitions>
        <hudson.model.ChoiceParameterDefinition>
          <name>ENVIRONMENT</name>
          <description>Environnement de déploiement</description>
          <choices>
            <string>development</string>
            <string>staging</string>
            <string>production</string>
          </choices>
          <defaultValue>development</defaultValue>
        </hudson.model.ChoiceParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>RUN_TESTS</name>
          <description>Exécuter les tests automatisés</description>
          <defaultValue>true</defaultValue>
        </hudson.model.BooleanParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>DEPLOY</name>
          <description>Déployer automatiquement après build</description>
          <defaultValue>true</defaultValue>
        </hudson.model.BooleanParameterDefinition>
      </parameterDefinitions>
    </hudson.model.ParametersDefinitionProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps">
    <scm class="hudson.plugins.git.GitSCM" plugin="git">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>$REPO_URL</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/$BRANCH</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
EOF

echo "📝 Configuration XML générée"

# Créer le job via l'API Jenkins
echo "🔨 Création du job '$JOB_NAME'..."
curl -s -X POST \
  "$JENKINS_URL/createItem?name=$JOB_NAME" \
  -H "Content-Type: application/xml" \
  --data-binary @pipeline-job-config.xml

if [ $? -eq 0 ]; then
    echo "✅ Job '$JOB_NAME' créé avec succès!"
    echo ""
    echo "🔗 Accès: $JENKINS_URL/job/$JOB_NAME/"
    echo "🎯 Branche surveillée: $BRANCH"
    echo "⏰ Polling SCM: Toutes les 2 minutes"
    echo ""
    echo "🚀 Le job va automatiquement se déclencher à chaque push!"
else
    echo "❌ Erreur lors de la création du job"
    exit 1
fi

# Déclencher immédiatement le premier build
echo "🧪 Déclenchement du premier build pour test..."
curl -s -X POST "$JENKINS_URL/job/$JOB_NAME/build"

echo ""
echo "🎉 Pipeline configurée! Surveillance active de la branche $BRANCH"

# Nettoyage
rm -f pipeline-job-config.xml

echo ""
echo "📋 Prochaines étapes:"
echo "1. Accédez à $JENKINS_URL/job/$JOB_NAME/"
echo "2. Vérifiez que le build initial se lance"
echo "3. Faites un commit/push pour tester l'auto-trigger"