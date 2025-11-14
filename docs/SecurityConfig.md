# Documentation Technique : Configuration de Sécurité

## Vue d'Ensemble

Le fichier `SecurityConfig.java` est une configuration centrale de sécurité pour l'application Let's Play. Il implémente une architecture de sécurité basée sur JWT (JSON Web Tokens) avec des règles d'autorisation granulaires.

## Configuration Principale

### 1. Configuration de Base (Lignes 1-15)
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
```
- **@Configuration** : Indique que cette classe contient des beans de configuration
- **@EnableWebSecurity** : Active la sécurité web Spring
- **@EnableMethodSecurity** : Permet l'annotation de sécurité au niveau des méthodes

### 2. Dépendances (Ligne 17)
```java
@Autowired
private JwtAuthenticationFilter jwtAuthenticationFilter;
```
- Injection du filtre d'authentification JWT personnalisé

## Beans de Configuration

### 1. Encodage de Mot de Passe (Lignes 19-22)
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
- **Fonction** : Fournit un encodeur de mot de passe BCrypt
- **Utilisation** : Pour le hachage sécurisé des mots de passe

### 2. Gestionnaire d'Authentification (Lignes 24-26)
```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```
- **Fonction** : Crée un gestionnaire d'authentification
- **Paramètre** : Configuration d'authentification injectée

## Configuration de la Chaîne de Filtres

### 1. Configuration HTTP (Lignes 28-48)
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/products").permitAll()
            .requestMatchers("/api/products/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/api/users/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

#### Règles d'Autorisation :
- **/api/auth/** : Accès public (Ligne 34)
- **/api/products** : Accès public (Ligne 35)
- **/api/products/** : Requiert rôle USER ou ADMIN (Ligne 36)
- **/api/users/** : Requiert rôle ADMIN (Ligne 37)
- **Autres requêtes** : Requiert authentification (Ligne 38)

#### Paramètres de Sécurité :
- **CSRF désactivé** (Ligne 31) : Pour les API REST
- **Session stateless** (Ligne 40) : Pas de gestion de session
- **Filtre JWT ajouté** (Ligne 42) : Avant le filtre d'authentification standard

## Diagramme de Flux de Sécurité

```
[Client] → [JwtAuthenticationFilter] → [SecurityFilterChain] → [Endpoint]
```

## Bonnes Pratiques Implémentées

1. **Sécurité Stateless** : Utilisation de JWT pour une architecture sans session
2. **Règles d'Autorisation Granulaires** : Contrôle d'accès par rôle
3. **Protection CSRF** : Désactivée pour les API (bonne pratique pour REST)
4. **Hachage Sécurisé** : Utilisation de BCrypt pour les mots de passe

## Résumé Technique

**Fonction Principale**: Configuration centrale de sécurité pour l'application Let's Play avec gestion JWT et autorisation basée sur les rôles.

**Technologies Clés**: Spring Security, JWT, BCrypt

**Complexité**: Moyenne - Configuration standard mais avec des règles d'autorisation spécifiques

**Points Clés**:
- Architecture stateless avec JWT
- Règles d'autorisation granulaires
- Désactivation de CSRF pour les API
- Hachage sécurisé des mots de passe

**Impact Projet**: Composant critique pour la sécurité de l'application, gérant l'authentification et l'autorisation de toutes les requêtes.