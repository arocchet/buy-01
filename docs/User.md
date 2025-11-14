# Documentation Technique : Modèle User

## Vue d'Ensemble du Modèle

Le fichier `User.java` définit un modèle de données pour la gestion des utilisateurs dans une application Spring Boot utilisant MongoDB comme base de données. Ce modèle représente les informations essentielles d'un utilisateur avec des contraintes de validation et des annotations spécifiques à MongoDB.

## Analyse Approfondie du Modèle

### Structure de Base

```java
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @NotBlank(message = "Name is mandatory")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @JsonIgnore
    private String password;

    @NotBlank(message = "Role is mandatory")
    @Pattern(regexp = "^(admin|user)$", message = "Role must be either 'admin' or 'user'")
    private String role = "user";
}
```

### Champs et Validations

| Champ | Type | Validations | Annotation MongoDB | Description |
|-------|------|-------------|-------------------|-------------|
| id | String | - | @Id | Identifiant unique généré par MongoDB |
| name | String | @NotBlank, @Size(min=2, max=100) | - | Nom de l'utilisateur (2-100 caractères) |
| email | String | @NotBlank, @Email, @Indexed(unique=true) | @Indexed | Adresse email unique et valide |
| password | String | @NotBlank, @Size(min=8) | @JsonIgnore | Mot de passe (8+ caractères, ignoré en JSON) |
| role | String | @NotBlank, @Pattern("admin|user") | - | Rôle utilisateur (admin ou user, par défaut "user") |

### Constructeurs

```java
public User() {} // Constructeur par défaut

public User(String name, String email, String password, String role) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.role = role;
}
```

### Méthodes d'Accès

```java
// Getters et Setters pour chaque champ
public String getId() { return id; }
public void setId(String id) { this.id = id; }
public String getName() { return name; }
public void setName(String name) { this.name = name; }
public String getEmail() { return email; }
public void setEmail(String email) { this.email = email; }
public String getPassword() { return password; }
public void setPassword(String password) { this.password = password; }
public String getRole() { return role; }
public void setRole(String role) { this.role = role; }
```

## Analyse des Annotations

### Annotations MongoDB

- `@Document(collection = "users")` : Indique que cette classe représente une collection MongoDB nommée "users"
- `@Id` : Marque le champ comme identifiant unique (généré automatiquement par MongoDB)
- `@Indexed(unique = true)` : Crée un index unique sur le champ email pour des requêtes optimisées

### Annotations de Validation

- `@NotBlank` : Valide que le champ n'est pas vide ou null
- `@Size` : Valide la taille minimale/maximale d'une chaîne
- `@Email` : Valide le format d'une adresse email
- `@Pattern` : Valide que la valeur correspond à une expression régulière
- `@JsonIgnore` : Empêche le champ d'être sérialisé en JSON (sécurité pour le mot de passe)

## Schéma de la Collection MongoDB

```json
{
  "_id": "ObjectId",
  "name": "string (2-100 chars)",
  "email": "string (unique, valid email)",
  "password": "string (8+ chars)",
  "role": "string (admin|user)"
}
```

## Cas d'Utilisation Typiques

### Création d'un Utilisateur

```java
User newUser = new User(
    "Jean Dupont",
    "jean.dupont@example.com",
    "securePassword123",
    "user"
);
userRepository.save(newUser);
```

### Récupération d'un Utilisateur

```java
Optional<User> user = userRepository.findById(userId);
if (user.isPresent()) {
    // Traitement de l'utilisateur
}
```

### Mise à jour d'un Utilisateur

```java
User existingUser = userRepository.findById(userId).orElseThrow();
existingUser.setName("Nouveau Nom");
existingUser.setEmail("nouvel.email@example.com");
userRepository.save(existingUser);
```

## Sécurité et Bonnes Pratiques

1. **Protection des données sensibles** :
   - Le mot de passe est marqué avec `@JsonIgnore` pour éviter la sérialisation
   - Le mot de passe a une contrainte de taille minimale (8 caractères)

2. **Gestion des rôles** :
   - Validation stricte des rôles avec `@Pattern("admin|user")`
   - Valeur par défaut "user" pour une sécurité accrue

3. **Intégrité des données** :
   - Email unique grâce à `@Indexed(unique = true)`
   - Validation des champs obligatoires avec `@NotBlank`

## Résumé Technique

**Fonction Principale**: Modèle de données pour la gestion des utilisateurs avec validation et intégration MongoDB.

**Technologies Clés**: Spring Data MongoDB, Jakarta Validation API, Jackson.

**Complexité**: Moyenne - Le modèle est simple mais intègre des validations avancées et des annotations spécifiques à MongoDB.

**Points Clés**:
- Validation complète des champs utilisateur
- Intégration avec MongoDB via Spring Data
- Sécurité des données sensibles (mot de passe)
- Gestion des rôles avec validation

**Impact Projet**: Composant fondamental pour l'authentification et la gestion des utilisateurs dans l'application.