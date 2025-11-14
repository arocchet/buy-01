# Documentation Technique : ErrorResponse.java

## Vue d'Ensemble

Le fichier `ErrorResponse.java` définit une classe Java servant de modèle standardisé pour les réponses d'erreur dans une application. Cette classe encapsule les informations essentielles pour la gestion des erreurs, incluant le timestamp, le code d'état, le message d'erreur, le chemin de la requête et les erreurs de validation.

## Structure de la Classe

### Attributs Principaux

| Attribut | Type | Description | Ligne |
|----------|------|-------------|-------|
| timestamp | LocalDateTime | Horodatage de l'erreur | 8 |
| status | int | Code HTTP de l'erreur | 9 |
| error | String | Type d'erreur | 10 |
| message | String | Message descriptif de l'erreur | 11 |
| path | String | Chemin de la requête ayant provoqué l'erreur | 12 |
| validationErrors | List<String> | Liste des erreurs de validation | 13 |

### Constructeurs

1. **Constructeur par défaut** (Ligne 15-17)
   - Initialise le timestamp avec l'heure actuelle
   - Appelé implicitement par le second constructeur

2. **Constructeur principal** (Ligne 19-24)
   - Initialise tous les attributs principaux
   - Appelle le constructeur par défaut pour le timestamp

### Méthodes d'Accès

| Méthode | Description | Ligne |
|---------|-------------|-------|
| getTimestamp() | Récupère l'horodatage de l'erreur | 26-28 |
| setTimestamp() | Définit l'horodatage de l'erreur | 30-32 |
| getStatus() | Récupère le code d'état HTTP | 34-36 |
| setStatus() | Définit le code d'état HTTP | 38-40 |
| getError() | Récupère le type d'erreur | 42-44 |
| setError() | Définit le type d'erreur | 46-48 |
| getMessage() | Récupère le message d'erreur | 50-52 |
| setMessage() | Définit le message d'erreur | 54-56 |
| getPath() | Récupère le chemin de la requête | 58-60 |
| setPath() | Définit le chemin de la requête | 62-64 |
| getValidationErrors() | Récupère la liste des erreurs de validation | 66-68 |
| setValidationErrors() | Définit la liste des erreurs de validation | 70-72 |

## Analyse Technique

### Design Pattern

La classe `ErrorResponse` suit le pattern **Data Transfer Object (DTO)** :
- Contient uniquement des attributs et des méthodes d'accès
- Pas de logique métier
- Conçu pour transporter des données entre couches

### Gestion des Erreurs

- **Timestamp automatique** : Initialisé à la création de l'objet
- **Validation centralisée** : Gestion des erreurs de validation via la liste `validationErrors`
- **Structure standardisée** : Format cohérent pour toutes les réponses d'erreur

### Points Forts

1. **Extensibilité** : La liste `validationErrors` permet d'ajouter plusieurs erreurs de validation
2. **Standardisation** : Format uniforme pour toutes les réponses d'erreur
3. **Conformité REST** : Structure compatible avec les bonnes pratiques REST

## Utilisation Typique

Bien que le fichier ne contienne pas d'exemples d'utilisation, voici comment cette classe serait typiquement utilisée dans une application :

```java
// Création d'une réponse d'erreur
ErrorResponse errorResponse = new ErrorResponse(
    400,
    "Bad Request",
    "Paramètres de requête invalides",
    "/api/users"
);

// Ajout d'erreurs de validation
List<String> validationErrors = new ArrayList<>();
validationErrors.add("Le champ 'email' est requis");
validationErrors.add("Le champ 'password' doit contenir au moins 8 caractères");
errorResponse.setValidationErrors(validationErrors);

// Retour de la réponse dans un contrôleur
return ResponseEntity.status(errorResponse.getStatus())
    .body(errorResponse);
```

## Résumé Technique

**Fonction Principale**: Modèle standardisé pour les réponses d'erreur dans une application Java, encapsulant toutes les informations nécessaires pour une gestion cohérente des erreurs.

**Technologies Clés**: Java, LocalDateTime, List

**Complexité**: Moyenne - La classe est simple mais doit être utilisée correctement pour maintenir la cohérence des réponses d'erreur dans toute l'application.

**Points Clés**:
- Structure standardisée pour les réponses d'erreur
- Gestion centralisée des erreurs de validation
- Conception orientée données (DTO)

**Impact Projet**: Composant critique pour la gestion des erreurs, assurant une expérience utilisateur cohérente et une maintenance simplifiée des réponses d'erreur dans toute l'application.