#!/bin/bash

echo "Génération du certificat SSL auto-signé pour Let's Play..."

# Créer le répertoire des ressources s'il n'existe pas
mkdir -p src/main/resources

# Générer un keystore PKCS12 avec certificat auto-signé
keytool -genkeypair \
    -alias letsplay \
    -keyalg RSA \
    -keysize 2048 \
    -storetype PKCS12 \
    -keystore src/main/resources/keystore.p12 \
    -validity 365 \
    -storepass changeit \
    -keypass changeit \
    -dname "CN=localhost, OU=Let's Play, O=Let's Play Inc, L=Paris, ST=IDF, C=FR" \
    -ext SAN=dns:localhost,ip:127.0.0.1

echo "✅ Certificat SSL généré dans src/main/resources/keystore.p12"
echo "🔐 Mot de passe du keystore: changeit"
echo ""
echo "Pour utiliser HTTPS en production:"
echo "mvn spring-boot:run -Dspring.profiles.active=prod"
echo ""
echo "L'application sera disponible sur: https://localhost:8443"
echo "⚠️  Votre navigateur affichera un avertissement (certificat auto-signé)"