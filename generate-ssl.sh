#!/bin/bash

echo "Génération des certificats SSL auto-signés pour Buy-01..."
echo "Utilisation de Docker pour exécuter keytool..."
echo ""

# Désactiver la conversion de path de MINGW/Git Bash
export MSYS_NO_PATHCONV=1

# Services
SERVICES=("api-gateway" "user-service" "product-service" "media-service")

# Répertoire de base pour les microservices
BASE_DIR="microservices-architecture"

# Mot de passe du keystore
KEYSTORE_PASS="changeit"

# Vérifier que Docker est disponible
if ! command -v docker &> /dev/null; then
    echo "❌ ERREUR: Docker n'est pas installé ou n'est pas dans le PATH"
    exit 1
fi

# Obtenir le chemin Windows absolu du projet
WIN_PWD=$(pwd -W 2>/dev/null || pwd)

# Pour chaque service, créer un keystore
for SERVICE in "${SERVICES[@]}"; do
    RESOURCES_DIR="$BASE_DIR/$SERVICE/src/main/resources"
    KEYSTORE_PATH="$RESOURCES_DIR/keystore.p12"
    
    echo "=== Génération du certificat pour $SERVICE ==="
    
    # Créer le répertoire des ressources s'il n'existe pas
    mkdir -p "$RESOURCES_DIR"
    
    # Supprimer l'ancien keystore s'il existe
    if [ -f "$KEYSTORE_PATH" ]; then
        rm "$KEYSTORE_PATH"
        echo "  ⚠️  Ancien keystore supprimé"
    fi
    
    # Chemin complet Windows pour le montage Docker
    FULL_PATH="$WIN_PWD/$RESOURCES_DIR"
    
    # Générer un keystore PKCS12 avec certificat auto-signé via Docker
    docker run --rm -v "$FULL_PATH:/cert" eclipse-temurin:21-jdk \
        keytool -genkeypair \
        -alias "$SERVICE" \
        -keyalg RSA \
        -keysize 2048 \
        -storetype PKCS12 \
        -keystore /cert/keystore.p12 \
        -validity 365 \
        -storepass "$KEYSTORE_PASS" \
        -keypass "$KEYSTORE_PASS" \
        -dname "CN=localhost, OU=$SERVICE, O=Buy-01, L=Paris, ST=IDF, C=FR" \
        -ext "SAN=dns:localhost,dns:$SERVICE,ip:127.0.0.1"
    
    if [ $? -eq 0 ]; then
        echo "  ✅ Certificat généré: $KEYSTORE_PATH"
    else
        echo "  ❌ Erreur lors de la génération du certificat pour $SERVICE"
    fi
    echo ""
done

echo "==========================================="
echo "✅ Tous les certificats SSL ont été générés"
echo "==========================================="
echo ""
echo "🔐 Mot de passe des keystores: $KEYSTORE_PASS"
echo ""
echo "⚠️  Votre navigateur affichera un avertissement (certificat auto-signé)"