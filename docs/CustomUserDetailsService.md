# Documentation Technique: CustomUserDetailsService

## Vue d'Ensemble

Le fichier `CustomUserDetailsService.java` implémente un service personnalisé pour la gestion des utilisateurs dans le contexte de Spring Security. Ce composant joue un rôle crucial dans l'authentification et l'autorisation des utilisateurs.

## Architecture et Structure

### Structure de Classe

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    // ...
}
```

- **Ligne 1**: Annotation `@Service` marque cette classe comme un composant Spring géré par le conteneur IoC
- **Ligne 2**: Implémente l'interface `UserDetailsService` de Spring Security pour fournir des fonctionnalités d'authentification

### Dépendances

```java
@Autowired
private UserRepository userRepository;
```

- **Ligne 6**: Injection de dépendance du repository utilisateur via `@Autowired`
- **Ligne 7**: Déclaration du repository pour accéder aux données utilisateur

## Fonctionnalités Principales

### Méthode loadUserByUsername

```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

    return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            getAuthorities(user.getRole())
    );
}
```

- **Ligne 10-16**: Implémentation de la méthode obligatoire de `UserDetailsService`
- **Ligne 11**: Récupération de l'utilisateur via son email
- **Ligne 12**: Gestion de l'exception si l'utilisateur n'est pas trouvé
- **Ligne 14-16**: Création d'un objet `UserDetails` Spring Security avec:
  - Email comme identifiant
  - Mot de passe de l'utilisateur
  - Autorisations dérivées du rôle

### Méthode getAuthorities

```java
private Collection<? extends GrantedAuthority> getAuthorities(String role) {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
}
```

- **Ligne 18-21**: Méthode privée pour convertir un rôle utilisateur en autorisations Spring Security
- **Ligne 20**: Création d'une liste contenant une seule autorité avec le préfixe "ROLE_"
- **Ligne 21**: Conversion du rôle en majuscules pour respecter la convention Spring Security

## Flux de Données

1. **Entrée**: Email de l'utilisateur (String)
2. **Traitement**:
   - Recherche de l'utilisateur dans la base de données
   - Conversion du rôle en autorité Spring Security
   - Création d'un objet `UserDetails`
3. **Sortie**: Objet `UserDetails` contenant les informations d'authentification

## Gestion des Erreurs

- **Ligne 12**: Lancement d'une `UsernameNotFoundException` si l'utilisateur n'est pas trouvé
- **Ligne 10**: La méthode déclare qu'elle peut lancer cette exception

## Intégration avec Spring Security

Ce service est intégré dans le flux d'authentification Spring Security via:
1. L'implémentation de l'interface `UserDetailsService`
2. La création d'objets `UserDetails` compatibles avec Spring Security
3. La conversion des rôles en autorités Spring Security

## Configuration Requise

Pour que ce service fonctionne correctement, le projet doit avoir:
- Une configuration Spring Security appropriée
- Un `UserRepository` correctement implémenté avec la méthode `findByEmail`
- Un modèle `User` avec les champs `email`, `password` et `role`

## Résumé Technique

**Fonction Principale**: Service d'authentification personnalisé pour Spring Security qui charge les utilisateurs à partir de la base de données et convertit leurs rôles en autorités Spring Security.

**Technologies Clés**: Spring Security, Spring Data JPA, Java

**Complexité**: Moyenne - Le code est simple mais nécessite une bonne compréhension de Spring Security et de son modèle d'authentification.

**Points Clés**:
- Implémente l'interface `UserDetailsService` de Spring Security
- Convertit les rôles utilisateur en autorités Spring Security
- Gère les erreurs d'utilisateur non trouvé
- Utilise l'injection de dépendance pour accéder au repository

**Impact Projet**: Composant critique pour l'authentification et l'autorisation dans l'application, essentiel pour la sécurité globale du système.