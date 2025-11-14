# Documentation Technique Complète : JwtUtil.java

## Vue d'Ensemble

Le fichier `JwtUtil.java` est un composant Spring Boot spécialisé dans la gestion des tokens JWT (JSON Web Tokens) pour l'authentification et l'autorisation. Ce composant utilise la bibliothèque JJWT pour créer, valider et extraire des informations des tokens JWT.

## Architecture et Structure

### Structure du Composant

```java
@Component
public class JwtUtil {
    // Configuration et méthodes
}
```

- **Annotation** : `@Component` (Ligne 17) marque cette classe comme un composant Spring géré par le conteneur IoC.
- **Configuration** : Utilise des propriétés injectées via `@Value` pour le secret et la durée d'expiration.

### Configuration

| Propriété | Type | Source | Description |
|-----------|------|--------|-------------|
| `secret` | String | `@Value("${spring.security.jwt.secret}")` | Clé secrète pour signer les tokens JWT |
| `expiration` | Long | `@Value("${spring.security.jwt.expiration}")` | Durée de validité du token en millisecondes |

## Fonctionnalités Principales

### Gestion des Tokens JWT

#### 1. Extraction d'Informations

```java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}

public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
}

public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
}
```

- **extractUsername** : Extrait le sujet (username) du token (Ligne 24)
- **extractExpiration** : Récupère la date d'expiration (Ligne 30)
- **extractClaim** : Méthode générique pour extraire n'importe quelle revendication (Ligne 38)

#### 2. Validation des Tokens

```java
public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
}

private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
}
```

- **validateToken** : Vérifie si le token est valide pour un utilisateur donné (Ligne 78)
- **isTokenExpired** : Vérifie si le token a expiré (Ligne 50)

#### 3. Création de Tokens

```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userDetails.getUsername());
}

public String generateToken(String username, String role) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);
    return createToken(claims, username);
}

private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
}
```

- **generateToken** : Crée un token pour un utilisateur (Ligne 62)
- **generateToken avec rôle** : Crée un token avec une revendication de rôle (Ligne 68)
- **createToken** : Méthode interne pour construire le token JWT (Ligne 72)

#### 4. Gestion des Rôles

```java
public String extractRole(String token) {
    Claims claims = extractAllClaims(token);
    return claims.get("role", String.class);
}
```

- **extractRole** : Récupère le rôle associé au token (Ligne 85)

## Flux de Données et Interactions

### Flux de Création de Token

1. **Initialisation** : Création d'une map de claims (Ligne 63 ou 69)
2. **Construction** : Utilisation de Jwts.builder() pour construire le token (Ligne 72)
3. **Signature** : Signature avec la clé secrète (Ligne 77)
4. **Génération** : Retour du token compacté (Ligne 78)

### Flux de Validation

1. **Extraction** : Récupération du username et vérification de l'expiration (Ligne 79-80)
2. **Comparaison** : Vérification que le username correspond à celui de l'utilisateur (Ligne 80)

## Sécurité et Bonnes Pratiques

### Gestion des Clés

```java
private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
}
```

- Utilisation de HMAC-SHA pour la signature des tokens
- Clé secrète injectée depuis la configuration

### Algorithme de Signature

- Utilisation de `SignatureAlgorithm.HS512` (Ligne 77) pour une sécurité élevée

### Gestion des Claims

- Stockage des claims dans une map (Ligne 63, 69)
- Ajout de claims personnalisés comme le rôle (Ligne 69)

## Intégration avec Spring Security

Bien que le fichier ne montre pas directement l'intégration avec Spring Security, on peut supposer (HYPOTHÈSE) que :

1. Ce composant est utilisé par un `JwtAuthenticationFilter` pour valider les tokens
2. Les tokens sont générés après une authentification réussie
3. Le rôle extrait est utilisé pour l'autorisation

## Points d'Extension

1. **Ajout de Claims Personnalisés** : La structure permet d'ajouter facilement de nouvelles revendications
2. **Support Multi-Algorithmes** : Possibilité d'étendre pour supporter plusieurs algorithmes de signature
3. **Gestion des Tokens Rafraîchis** : Ajout de méthodes pour gérer les tokens de rafraîchissement

## Exemples d'Utilisation (HYPOTHÈSE)

Bien qu'aucun exemple concret ne soit fourni dans le fichier, voici comment ce composant pourrait être utilisé :

```java
// Génération d'un token
String token = jwtUtil.generateToken(userDetails);

// Validation d'un token
boolean isValid = jwtUtil.validateToken(token, userDetails);

// Extraction d'informations
String username = jwtUtil.extractUsername(token);
String role = jwtUtil.extractRole(token);
```

## Résumé Technique

**Fonction Principale**: Gestion complète des tokens JWT pour l'authentification et l'autorisation dans une application Spring Boot.

**Technologies Clés**: Spring Boot, JJWT (JSON Web Token), HMAC-SHA512.

**Complexité**: Moyenne - Le composant est bien structuré mais nécessite une compréhension des concepts JWT et Spring Security.

**Points Clés**:
- Utilisation de la bibliothèque JJWT pour la gestion des tokens
- Support des rôles via des claims personnalisés
- Validation complète des tokens avec vérification de l'expiration
- Architecture modulaire permettant des extensions futures

**Impact Projet**: Composant critique pour la sécurité de l'application, centralisant toute la logique JWT.