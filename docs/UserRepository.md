# Définitions de Types : src/main/java/com/letsplay/letsplay/repository/UserRepository.java

## Vue d'ensemble
Ce fichier définit une interface de repository pour la gestion des utilisateurs dans une application Spring Boot utilisant MongoDB comme base de données. Il étend les fonctionnalités de base de Spring Data MongoRepository pour fournir des opérations spécifiques aux utilisateurs.

## Définitions de Types

### Interfaces

| Interface | Propriétés Clés | Objectif | Héritage |
|-----------|----------------|---------|-------------|
| `UserRepository` | `findByEmail()`, `existsByEmail()` | Gestion des opérations CRUD et requêtes personnalisées pour les utilisateurs | `MongoRepository<User, String>` |

**Détails de l'interface** (Ligne 1-13):
```java
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

- **Annotation `@Repository`** (Ligne 1): Indique que cette interface est un composant de repository Spring, permettant l'injection de dépendances et la gestion des exceptions.
- **Héritage de `MongoRepository`** (Ligne 1): Fournit les opérations CRUD de base (save, findAll, delete, etc.) pour l'entité `User` avec `String` comme type d'ID.
- **Méthodes personnalisées**:
  - `findByEmail()` (Ligne 3): Méthode de requête personnalisée qui retourne un `Optional<User>` pour un email donné.
  - `existsByEmail()` (Ligne 4): Méthode de requête personnalisée qui vérifie l'existence d'un utilisateur avec un email donné.

### Types Alias et Génériques

| Type | Définition | Contexte d'utilisation |
|------|------------|------------------------|
| `Optional<User>` | Retourne un utilisateur ou vide | Utilisé pour `findByEmail()` pour gérer les cas où l'utilisateur n'existe pas |
| `boolean` | Retourne true/false | Utilisé pour `existsByEmail()` pour vérifier l'existence d'un utilisateur |

### Relations entre Types

- **`UserRepository`** dépend de l'entité `User` (importée depuis `com.letsplay.letsplay.model.User`) pour définir le type d'entité géré.
- **`MongoRepository`** fournit l'implémentation de base des opérations CRUD, tandis que `UserRepository` étend ces fonctionnalités avec des méthodes personnalisées.

## Exemples d'Utilisation

**Exemple 1: Recherche d'un utilisateur par email**
```java
Optional<User> user = userRepository.findByEmail("test@example.com");
if (user.isPresent()) {
    // Traitement de l'utilisateur trouvé
} else {
    // Gestion du cas où l'utilisateur n'existe pas
}
```

**Exemple 2: Vérification de l'existence d'un utilisateur**
```java
boolean exists = userRepository.existsByEmail("test@example.com");
if (exists) {
    // L'utilisateur existe
} else {
    // L'utilisateur n'existe pas
}
```

## Résumé Technique

**Fonction Principale**: Fournir une interface pour les opérations de base et personnalisées sur les utilisateurs dans une base de données MongoDB.

**Technologies Clés**: Spring Data MongoDB, Spring Framework, Java.

**Complexité**: Simple. Le fichier est concis et utilise des fonctionnalités standard de Spring Data pour les repositories.

**Points Clés**:
- Utilisation de `MongoRepository` pour les opérations CRUD de base.
- Méthodes personnalisées pour des requêtes spécifiques (recherche par email, vérification d'existence).
- Retourne des `Optional` pour gérer les cas où les utilisateurs n'existent pas.

**Impact Projet**: Composant essentiel pour la gestion des utilisateurs, servant de couche d'accès aux données pour les services métier.