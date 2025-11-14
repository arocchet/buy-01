# Définitions de Types : src/main/java/com/letsplay/letsplay/exception/ResourceNotFoundException.java

## Vue d'ensemble
Ce fichier définit une exception personnalisée `ResourceNotFoundException` qui étend `RuntimeException`. Elle est conçue pour gérer les cas où une ressource demandée n'est pas trouvée dans l'application.

## Définition de Type

### Classe Exception
| Classe | Propriétés | Héritage | Lignes |
|--------|------------|----------|--------|
| `ResourceNotFoundException` | - | `RuntimeException` | 1-11 |

```java
package com.letsplay.letsplay.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## Constructeurs

### Constructeur avec message
```java
public ResourceNotFoundException(String message) {
    super(message);
}
```
- **Ligne 4-6**
- Permet de créer une exception avec un message personnalisé
- Transmet le message à la classe parente `RuntimeException`

### Constructeur avec message et cause
```java
public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
}
```
- **Ligne 7-10**
- Permet de créer une exception avec un message et une cause (exception imbriquée)
- Transmet à la fois le message et la cause à `RuntimeException`

## Utilisation Typique

Cette exception est probablement utilisée dans les couches de service ou de contrôleur pour signaler qu'une ressource (comme un utilisateur, un jeu, etc.) n'a pas été trouvée dans la base de données ou le système.

## Bonnes Pratiques

1. **Gestion des erreurs** : Utiliser cette exception pour les cas où une ressource attendue est introuvable
2. **Messages clairs** : Fournir des messages d'erreur explicites pour faciliter le débogage
3. **Propagation** : Laisser cette exception se propager jusqu'à un contrôleur qui peut la transformer en réponse HTTP 404

## Résumé Technique

**Fonction Principale**: Gestion des erreurs de ressource introuvable dans l'application

**Technologies Clés**: Java, Exceptions personnalisées

**Complexité**: Simple - Structure standard d'exception avec deux constructeurs

**Points Clés**:
- Exception non vérifiée (héritant de RuntimeException)
- Deux constructeurs pour différentes situations d'erreur
- Conception minimaliste et efficace

**Impact Projet**: Composant critique pour la gestion des erreurs 404 dans l'application, assurant une communication claire des erreurs aux couches supérieures.