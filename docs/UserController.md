# Documentation Technique Complète : UserController.java

## Vue d'Ensemble du Contrôleur

Le fichier `UserController.java` est un contrôleur REST Spring Boot qui gère les opérations CRUD (Create, Read, Update, Delete) pour les utilisateurs dans l'application Let's Play. Ce contrôleur expose des endpoints API sécurisés pour la gestion des utilisateurs avec des rôles d'administration.

## Architecture et Structure

### Organisation du Code

```java
package com.letsplay.letsplay.controller;

import com.letsplay.letsplay.model.User;
import com.letsplay.letsplay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    // Implémentation des méthodes
}
```

### Dépendances et Injections

- **Service Injecté** (Ligne 15):
  ```java
  @Autowired
  private UserService userService;
  ```
  - Injection automatique du service `UserService` pour déléguer la logique métier
  - Suivi du principe de séparation des responsabilités (contrôleur vs service)

### Configuration des Endpoints

- **Mapping Global** (Ligne 13):
  ```java
  @RequestMapping("/api/users")
  ```
  - Préfixe commun pour tous les endpoints du contrôleur
  - Conforme aux bonnes pratiques REST (pluriel pour les collections)

- **CORS Configuration** (Ligne 14):
  ```java
  @CrossOrigin(origins = "*", maxAge = 3600)
  ```
  - Autorise les requêtes cross-origin depuis n'importe quelle origine
  - Cache des prérequêtes CORS pendant 1 heure (3600 secondes)

## Implémentation des Endpoints

### 1. Récupération de Tous les Utilisateurs

```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<User>> getAllUsers() {
    List<User> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
}
```

- **Méthode HTTP**: GET
- **Endpoint**: `/api/users`
- **Sécurité**: Requiert le rôle ADMIN
- **Comportement**:
  - Appelle `userService.getAllUsers()`
  - Retourne la liste des utilisateurs avec un code HTTP 200 (OK)
- **Validation**:
  - Pas de validation explicite (tous les utilisateurs sont retournés)
  - La sécurité est gérée par Spring Security

### 2. Récupération d'un Utilisateur par ID

```java
@GetMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getUserById(@PathVariable String id) {
    try {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error retrieving user: " + e.getMessage());
    }
}
```

- **Méthode HTTP**: GET
- **Endpoint**: `/api/users/{id}`
- **Paramètres**:
  - `id`: Identifiant de l'utilisateur (String)
- **Sécurité**: Requiert le rôle ADMIN
- **Comportement**:
  - Appelle `userService.getUserById(id)`
  - Gestion des cas:
    - Utilisateur trouvé → Retourne l'utilisateur (200 OK)
    - Utilisateur non trouvé → Retourne 404 Not Found
    - Erreur → Retourne 400 Bad Request avec le message d'erreur
- **Gestion des Erreurs**:
  - Bloc try-catch pour capturer les exceptions
  - Retourne un message d'erreur explicite

### 3. Mise à Jour d'un Utilisateur

```java
@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> updateUser(@PathVariable String id, @Valid @RequestBody User userDetails) {
    try {
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
```

- **Méthode HTTP**: PUT
- **Endpoint**: `/api/users/{id}`
- **Paramètres**:
  - `id`: Identifiant de l'utilisateur (String)
  - `userDetails`: Objet User contenant les détails à mettre à jour (validé)
- **Sécurité**: Requiert le rôle ADMIN
- **Validation**:
  - `@Valid` sur le corps de la requête pour validation des données
- **Comportement**:
  - Appelle `userService.updateUser(id, userDetails)`
  - Retourne l'utilisateur mis à jour (200 OK)
  - Gestion des erreurs:
    - Capture les `RuntimeException`
    - Retourne 400 Bad Request avec le message d'erreur

### 4. Suppression d'un Utilisateur

```java
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> deleteUser(@PathVariable String id) {
    try {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
```

- **Méthode HTTP**: DELETE
- **Endpoint**: `/api/users/{id}`
- **Paramètres**:
  - `id`: Identifiant de l'utilisateur à supprimer (String)
- **Sécurité**: Requiert le rôle ADMIN
- **Comportement**:
  - Appelle `userService.deleteUser(id)`
  - Retourne 200 OK en cas de succès
  - Gestion des erreurs:
    - Capture les `RuntimeException`
    - Retourne 400 Bad Request avec le message d'erreur

## Analyse des Bonnes Pratiques

### Sécurité

- **Contrôle d'Accès**:
  - `@PreAuthorize("hasRole('ADMIN')")` sur tous les endpoints
  - Assure que seul un utilisateur avec le rôle ADMIN peut accéder aux endpoints
- **Validation des Données**:
  - `@Valid` sur les objets de requête pour validation automatique
  - Utilisation de Jakarta Validation pour la validation des entrées

### Gestion des Erreurs

- **Approche Uniforme**:
  - Utilisation de `ResponseEntity<?>` pour retourner différents codes HTTP
  - Gestion centralisée des erreurs avec try-catch
- **Messages d'Erreur**:
  - Retourne des messages d'erreur explicites
  - Codes HTTP appropriés (400, 404, 200)

### Architecture

- **Séparation des Responsabilités**:
  - Contrôleur ne contient que la logique de routage
  - Délégation de la logique métier au service `UserService`
- **RESTful Design**:
  - Endpoints conformes aux conventions REST
  - Utilisation appropriée des méthodes HTTP (GET, PUT, DELETE)

## Diagramme des Flux

```
Client → [UserController] → [UserService] → [Repository] → Database
       ↑               ↑
       ← Response      ← Data
```

## Résumé Technique

**Fonction Principale**: Gestion des opérations CRUD pour les utilisateurs avec des contrôles d'accès administratifs.

**Technologies Clés**: Spring Boot, Spring Security, Jakarta Validation, REST.

**Complexité**: Moyenne - Implémentation standard avec gestion d'erreurs et validation.

**Points Clés**:
- Contrôle d'accès strict avec `@PreAuthorize`
- Validation des données en entrée
- Gestion centralisée des erreurs
- Architecture propre avec séparation des responsabilités

**Impact Projet**: Composant critique pour la gestion des utilisateurs dans l'application Let's Play, assurant la sécurité et l'intégrité des données utilisateur.