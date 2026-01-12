# Définitions de Types : src/main/java/com/letsplay/letsplay/exception/BadRequestException.java

## Vue d'ensemble
Ce fichier définit une exception personnalisée `BadRequestException` qui étend `RuntimeException`. Elle est conçue pour gérer les erreurs de requête invalide dans l'application.

## Définitions de Types

### Classe Exception
| Classe | Propriétés | Héritage | Lignes |
|--------|------------|----------|--------|
| `BadRequestException` | - | `RuntimeException` | 1-11 |

#### Constructeurs
1. **Constructeur avec message** (Ligne 4-6)
   ```java
   public BadRequestException(String message) {
       super(message);
   }
   ```
   - Crée une exception avec un message d'erreur personnalisé
   - Transmet le message à la classe parente `RuntimeException`

2. **Constructeur avec message et cause** (Ligne 8-11)
   ```java
   public BadRequestException(String message, Throwable cause) {
       super(message, cause);
   }
   ```
   - Crée une exception avec un message et une cause (exception sous-jacente)
   - Transmet les deux paramètres à la classe parente `RuntimeException`

## Relations de Types
- La classe `BadRequestException` hérite de `RuntimeException`
- Elle est conçue pour être utilisée comme exception non vérifiée
- Probablement utilisée pour signaler des erreurs de validation de requête

## Exemples d'utilisation (hypothétiques)
```java
// Exemple d'utilisation probable (non présent dans le fichier)
try {
    // Code de validation de requête
    if (!requeteValide) {
        throw new BadRequestException("Requête invalide: " + messageErreur);
    }
} catch (BadRequestException e) {
    // Gestion de l'erreur
    logger.error("Erreur de requête: " + e.getMessage());
    throw e;
}
```

## Résumé Technique

**Fonction Principale**: Gestion des erreurs de requête invalide dans l'application

**Technologies Clés**: Java, Exceptions personnalisées

**Complexité**: Simple - La classe est minimaliste avec seulement deux constructeurs

**Points Clés**:
- Exception non vérifiée (héritage de RuntimeException)
- Deux constructeurs pour différentes situations d'erreur
- Conception simple et directe pour la gestion des erreurs

**Impact Projet**: Composant essentiel pour la gestion des erreurs HTTP 400 (Bad Request) dans l'application