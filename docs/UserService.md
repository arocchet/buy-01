# Documentation Technique Complète : UserService.java

## Vue d'Ensemble

Le fichier `UserService.java` implémente un service de gestion des utilisateurs pour l'application Let's Play. Ce service encapsule toute la logique métier liée aux utilisateurs, incluant la création, la récupération, la mise à jour et la suppression des utilisateurs, ainsi que l'authentification.

## Structure du Service

### Dépendances

```java
// Ligne 5-12: Déclarations des dépendances
@Autowired
private UserRepository userRepository;

@Autowired
private PasswordEncoder passwordEncoder;

@Autowired
private InputSanitizer inputSanitizer;
```

- **UserRepository**: Interface pour les opérations CRUD sur les utilisateurs
- **PasswordEncoder**: Service de hachage des mots de passe
- **InputSanitizer**: Service de validation et nettoyage des entrées utilisateur

### Méthodes Principales

#### 1. Création d'Utilisateur

```java
// Ligne 14-33: Méthode createUser
public User createUser(User user) {
    // Validation des entrées
    inputSanitizer.validateUserInput(user.getName(), user.getEmail(), user.getRole());

    // Vérification de l'unicité de l'email
    if (userRepository.existsByEmail(user.getEmail())) {
        throw new RuntimeException("Email already exists");
    }

    // Nettoyage et hachage des données
    user.setName(inputSanitizer.sanitize(user.getName()));
    user.setPassword(passwordEncoder.encode(user.getPassword()));

    // Définition du rôle par défaut
    if (user.getRole() == null || user.getRole().isEmpty()) {
        user.setRole("user");
    }

    return userRepository.save(user);
}
```

#### 2. Récupération des Utilisateurs

```java
// Ligne 35-42: Méthodes de récupération
public List<User> getAllUsers() {
    return userRepository.findAll();
}

public Optional<User> getUserById(String id) {
    return userRepository.findById(id);
}

public Optional<User> getUserByEmail(String email) {
    return userRepository.findByEmail(email);
}
```

#### 3. Mise à Jour d'Utilisateur

```java
// Ligne 44-68: Méthode updateUser
public User updateUser(String id, User userDetails) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

    // Mise à jour conditionnelle des champs
    if (userDetails.getName() != null) {
        user.setName(userDetails.getName());
    }
    if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
        if (userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        user.setEmail(userDetails.getEmail());
    }
    if (userDetails.getPassword() != null) {
        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
    }
    if (userDetails.getRole() != null) {
        user.setRole(userDetails.getRole());
    }

    return userRepository.save(user);
}
```

#### 4. Suppression d'Utilisateur

```java
// Ligne 70-76: Méthode deleteUser
@SuppressWarnings("null")
public void deleteUser(String id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    userRepository.delete(user);
}
```

#### 5. Authentification

```java
// Ligne 78-84: Méthode authenticateUser
public boolean authenticateUser(String email, String password) {
    Optional<User> user = userRepository.findByEmail(email);
    return user.isPresent() && passwordEncoder.matches(password, user.get().getPassword());
}
```

## Gestion des Erreurs

Le service implémente une gestion des erreurs explicite :

- **Email existant**: Lève une exception lors de la création ou mise à jour avec un email déjà utilisé
- **Utilisateur non trouvé**: Lève une exception lors des opérations de mise à jour ou suppression
- **Validation des entrées**: Utilise InputSanitizer pour valider les données avant traitement

## Sécurité

- **Hachage des mots de passe**: Utilisation de PasswordEncoder pour le hachage sécurisé
- **Validation des entrées**: Nettoyage et validation via InputSanitizer
- **Authentification**: Méthode dédiée pour vérifier les identifiants

## Flux de Données

1. **Création**:
   Entrée → Validation → Nettoyage → Hachage → Sauvegarde

2. **Mise à jour**:
   Récupération → Validation des modifications → Mise à jour → Sauvegarde

3. **Authentification**:
   Récupération → Vérification des identifiants

## Résumé Technique

**Fonction Principale**: Service central de gestion des utilisateurs avec opérations CRUD et authentification

**Technologies Clés**: Spring Framework, Spring Security, JPA Repository

**Complexité**: Moyenne - Implémentation complète avec gestion des erreurs et validation

**Points Clés**:
- Gestion complète du cycle de vie des utilisateurs
- Sécurité renforcée par hachage des mots de passe
- Validation des entrées utilisateur
- Gestion explicite des erreurs

**Impact Projet**: Composant critique pour l'authentification et la gestion des utilisateurs dans l'application Let's Play