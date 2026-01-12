#!/bin/bash

# Test des scénarios d'erreur pour l'audit

set -e

echo "🧪 Test des scénarios d'erreur du pipeline..."

# Test 1: Simulation d'échec de notification
echo "1. Test d'échec de notification..."
export SLACK_WEBHOOK_URL="https://invalid-webhook-url"
if ! ./scripts/send-notification.sh "Test d'échec intentionnel" 2>/dev/null; then
    echo "✅ Gestion d'erreur de notification OK"
else
    echo "❌ La gestion d'erreur ne fonctionne pas"
fi

# Reset webhook
export SLACK_WEBHOOK_URL="YOUR_SLACK_WEBHOOK_URL"

# Test 2: Test de rollback sans sauvegardes
echo "2. Test de rollback sans sauvegardes..."
if ! ./scripts/rollback.sh -b 999 -e dev 2>/dev/null; then
    echo "✅ Gestion d'erreur de rollback OK"
else
    echo "❌ Le rollback devrait échouer sans sauvegarde"
fi

echo "✅ Tests d'erreur terminés"