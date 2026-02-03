#!/bin/bash

# Script pour configurer automatiquement le job Jenkins Buy01-CI-CD

set -e

JENKINS_URL="http://localhost:8090"
JOB_NAME="buy-01-CI-CD"
REPO_URL="file:///Users/pierrecaboor/IdeaProjects/buy-01"

echo "🔧 Configuration du job Jenkins Buy01-CI-CD..."

# Vérifier que Jenkins est accessible
if ! curl -s "$JENKINS_URL" > /dev/null; then
    echo "❌ Jenkins n'est pas accessible à $JENKINS_URL"
    exit 1
fi

echo "✅ Jenkins accessible"

# Configuration du job via l'API Jenkins (si l'API est disponible sans auth)
cat << EOF > jenkins-job-config.xml
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <actions/>
  <description>Pipeline CI/CD pour la plateforme e-commerce Buy01</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
      <triggers>
        <hudson.triggers.SCMTrigger>
          <spec>H/5 * * * *</spec>
          <ignorePostCommitHooks>false</ignorePostCommitHooks>
        </hudson.triggers.SCMTrigger>
      </triggers>
    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
    <hudson.model.ParametersDefinitionProperty>
      <parameterDefinitions>
        <hudson.model.ChoiceParameterDefinition>
          <name>ENVIRONMENT</name>
          <description>Target deployment environment</description>
          <choices class="java.util.Arrays\$ArrayList">
            <a class="string-array">
              <string>dev</string>
              <string>staging</string>
              <string>production</string>
            </a>
          </choices>
        </hudson.model.ChoiceParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>RUN_TESTS</name>
          <description>Run automated tests</description>
          <defaultValue>true</defaultValue>
        </hudson.model.BooleanParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>DEPLOY</name>
          <description>Deploy after successful build</description>
          <defaultValue>true</defaultValue>
        </hudson.model.BooleanParameterDefinition>
        <hudson.model.StringParameterDefinition>
          <name>BRANCH</name>
          <description>Git branch to build</description>
          <defaultValue>main</defaultValue>
        </hudson.model.StringParameterDefinition>
      </parameterDefinitions>
    </hudson.model.ParametersDefinitionProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.87">
    <scm class="hudson.plugins.git.GitSCM" plugin="git@4.8.2">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>$REPO_URL</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="list"/>
      <extensions/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
EOF

echo "📄 Configuration XML créée"

echo ""
echo "🎯 Étapes pour configurer manuellement le job dans Jenkins :"
echo ""
echo "1. 🌐 Ouvrir Jenkins : $JENKINS_URL"
echo "2. 🔍 Cliquer sur le job '$JOB_NAME'"
echo "3. ⚙️  Cliquer sur 'Configure' dans le menu de gauche"
echo "4. 📋 Dans 'Pipeline' section :"
echo "   - Definition: 'Pipeline script from SCM'"
echo "   - SCM: 'Git'"
echo "   - Repository URL: '$REPO_URL'"
echo "   - Branch: '*/main'"
echo "   - Script Path: 'Jenkinsfile'"
echo "5. 💾 Cliquer 'Save'"
echo "6. 🚀 Cliquer 'Build with Parameters'"
echo ""
echo "⚙️  Variables d'environnement à ajouter dans Manage Jenkins > Configure System :"
echo "SLACK_WEBHOOK_URL=YOUR_SLACK_WEBHOOK_URL
echo "SLACK_CHANNEL=#deployments"
echo ""
echo "🔧 Fichier de configuration XML disponible : jenkins-job-config.xml"