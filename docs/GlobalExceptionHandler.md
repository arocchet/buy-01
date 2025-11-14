# Documentation Technique : GlobalExceptionHandler.java

## Vue d'ensemble
Le fichier `GlobalExceptionHandler.java` est un gestionnaire d'exceptions global pour une application Spring Boot. Il centralise la gestion des erreurs en fournissant des réponses structurées et cohérentes pour différentes exceptions.

## Structure et Fonctionnalités

### Classe Principale
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Implémentation des gestionnaires d'exceptions
}
```

### Gestionnaires d'Exceptions

#### 1. Gestion des exceptions de ressource non trouvée
```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
    // Implémentation
}
```
- **Ligne 15-22**: Gère les exceptions `ResourceNotFoundException` avec un code HTTP 404
- **Paramètres**: Reçoit l'exception et le contexte de la requête
- **Réponse**: Retourne un objet `ErrorResponse` avec les détails de l'erreur

#### 2. Gestion des mauvaises requêtes
```java
@ExceptionHandler(BadRequestException.class)
public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
    // Implémentation
}
```
- **Ligne 24-31**: Gère les exceptions `BadRequestException` avec un code HTTP 400
- **Cas d'utilisation**: Erreurs de validation de requête

#### 3. Gestion des erreurs de validation
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
    // Implémentation
}
```
- **Ligne 33-47**: Gère les exceptions de validation des arguments
- **Transformation**: Convertit les erreurs de validation en liste de messages
- **Exemple de transformation**:
  ```java
  List<String> validationErrors = ex.getBindingResult()
          .getFieldErrors()
          .stream()
          .map(error -> error.getField() + ": " + error.getDefaultMessage())
          .collect(Collectors.toList());
  ```

#### 4. Gestion des violations de contraintes
```java
@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
    // Implémentation
}
```
- **Ligne 49-63**: Gère les exceptions de validation au niveau des contraintes
- **Transformation similaire** à `MethodArgumentNotValidException` mais pour les contraintes Jakarta Validation

#### 5. Gestion des doublons de clé
```java
@ExceptionHandler(DuplicateKeyException.class)
public ResponseEntity<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException ex, WebRequest request) {
    // Implémentation
}
```
- **Ligne 65-77**: Gère les exceptions de duplication de clé
- **Cas particulier**: Détection spécifique des erreurs d'email dupliqué

#### 6. Gestion des accès refusés
```java
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
    // Implémentation
}
```
- **Ligne 79-85**: Gère les exceptions d'accès refusé avec code HTTP 403

#### 7. Gestion des mauvaises identifiants
```java
@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
    // Implémentation
}
```
- **Ligne 87-93**: Gère les exceptions d'authentification échouée avec code HTTP 401

#### 8. Gestion des exceptions runtime
```java
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
    // Implémentation
}
```
- **Ligne 95-101**: Gestionnaire générique pour les exceptions runtime

#### 9. Gestion des exceptions génériques
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
    // Implémentation
}
```
- **Ligne 103-110**: Gestionnaire de dernier recours pour toutes les exceptions non capturées

## Modèle de Réponse d'Erreur

### Classe ErrorResponse
Bien que non visible dans ce fichier, la classe `ErrorResponse` est utilisée pour structurer les réponses d'erreur. Elle contient probablement les champs suivants (déduits de l'utilisation):

```java
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> validationErrors;

    // Getters et setters
}
```

## Flux de Gestion des Erreurs

1. **Détection**: Une exception est levée dans l'application
2. **Capture**: Le gestionnaire approprié est sélectionné via l'annotation `@ExceptionHandler`
3. **Transformation**: L'exception est transformée en réponse structurée
4. **Enrichissement**: Ajout du contexte de la requête
5. **Retour**: Réponse HTTP avec le code approprié

## Bonnes Pratiques Implémentées

1. **Centralisation**: Toutes les exceptions sont gérées en un seul endroit
2. **Consistance**: Format de réponse uniforme pour toutes les erreurs
3. **Détails**: Inclusion du contexte de la requête dans les réponses
4. **Spécialisation**: Gestionnaires spécifiques pour différents types d'erreurs
5. **Extensibilité**: Facilité d'ajout de nouveaux gestionnaires

## Résumé Technique

**Fonction Principale**: Centraliser et standardiser la gestion des exceptions dans l'application

**Technologies Clés**: Spring Boot, Spring Web, Jakarta Validation

**Complexité**: Moyenne à élevée - Implémente une logique de gestion d'erreurs sophistiquée mais bien structurée

**Points Clés**:
- Utilisation de `@RestControllerAdvice` pour une gestion globale
- Transformation des erreurs en réponses structurées
- Gestion spécifique des erreurs de validation
- Inclusion du contexte de requête dans les réponses

**Impact Projet**: Composant critique pour la cohérence des réponses d'erreur et l'expérience utilisateur, particulièrement important pour les API REST.