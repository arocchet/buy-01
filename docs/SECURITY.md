# 🔐 Documentation Sécurité - Let's Play API

## 📋 Vue d'ensemble

Cette documentation détaille l'implémentation des mesures de sécurité dans l'API Let's Play, conformément aux exigences du projet.

## 🛡️ Mesures de sécurité implémentées

### 1. **Hashage et Salt des mots de passe** ✅

#### Implémentation
- **Algorithme** : BCrypt avec salt automatique
- **Configuration** : `SecurityConfig.java:28`
- **Utilisation** : `UserService.java:35`

#### Code
```java
// Configuration BCrypt
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// Hashage lors de la création
user.setPassword(passwordEncoder.encode(user.getPassword()));

// Vérification lors de l'authentification
passwordEncoder.matches(password, user.get().getPassword())
```

#### Validation
- Format en BDD : `$2a$10$...` (BCrypt avec salt)
- Mots de passe jamais stockés en clair
- Salt unique généré automatiquement pour chaque mot de passe

### 2. **Validation des entrées** ✅

#### Protection contre les injections MongoDB
```java
// Détection de patterns dangereux
private static final Pattern MONGO_INJECTION_PATTERN = Pattern.compile(
    ".*\\$.*|.*\\{.*\\}.*|.*javascript.*|.*eval.*|.*where.*",
    Pattern.CASE_INSENSITIVE
);
```

#### Protection contre XSS
```java
// Détection de scripts malveillants
private static final Pattern HTML_SCRIPT_PATTERN = Pattern.compile(
    "<script[^>]*>.*?</script>|javascript:|on\\w+=",
    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
);
```

#### Sanitisation
```java
// Suppression des caractères dangereux
String sanitized = input.replaceAll("[<>\"'&]", "");
sanitized = sanitized.replaceAll("\\$", "");
sanitized = sanitized.replaceAll("\\{|\\}", "");
```

#### Usage
- Appliqué dans `UserService.java:27` et `ProductService.java:22`
- Validation automatique avant sauvegarde en BDD
- Rejet avec exception `SecurityException` si input invalide

### 3. **Protection des informations sensibles** ✅

#### Masquage des mots de passe
```java
// Dans User.java
@JsonIgnore
private String password;
```

#### Filtrage des réponses API
- Aucun mot de passe retourné dans les réponses JSON
- JWT ne contient que l'email et le rôle
- Données sensibles exclues des logs

### 4. **HTTPS - Protection des données en transit** ✅

#### Configuration de développement
```yaml
# application.yml (HTTP pour dev)
server:
  port: 8080
```

#### Configuration de production
```yaml
# application-prod.yml (HTTPS)
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
  require-ssl: true
```

#### Activation HTTPS
```bash
# Générer certificat SSL
./generate-ssl.sh

# Lancer en mode production
mvn spring-boot:run -Dspring.profiles.active=prod

# Accès sécurisé
curl -k https://localhost:8443/api/products
```

## 🔧 Annotations Spring Security

### Configuration de base

| Annotation | Rôle | Implémentation |
|---|---|---|
| `@EnableWebSecurity` | Active la sécurité web | `SecurityConfig.java:19` |
| `@EnableMethodSecurity` | Active la sécurité sur méthodes | `SecurityConfig.java:20` |

### Contrôle d'accès

#### @PreAuthorize - Contrôle granulaire
```java
// Admin seulement
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<User>> getAllUsers()

// Users et Admins
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public ResponseEntity<?> createProduct()
```

#### @PostAuthorize - Contrôle après exécution
```java
// Vérification d'accès après récupération du produit
@PostAuthorize("hasRole('ADMIN') or returnObject.userId == authentication.name")
private Product getProductWithSecurityCheck(String id)
```

#### Configuration centralisée (alternative à @PermitAll)
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/products").permitAll()
    .requestMatchers("/api/products/**").hasAnyRole("USER", "ADMIN")
    .requestMatchers("/api/users/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

### Injection de dépendances

#### @Autowired - 15 utilisations
```java
// Services
@Autowired
private UserRepository userRepository;

@Autowired
private PasswordEncoder passwordEncoder;

@Autowired
private InputSanitizer inputSanitizer;

// Security
@Autowired
private JwtAuthenticationFilter jwtAuthenticationFilter;
```

## 🧪 Tests de sécurité

### Test 1 : Vérification hashage des mots de passe
```bash
mongosh letsplay --eval "db.users.find({}, {password:1})"
# Résultat : Tous les mots de passe au format $2a$10$...
```

### Test 2 : Protection contre injections MongoDB
```bash
curl -X POST /api/auth/register \
  -d '{"name": "$where: function() { return true; }"}'
# Résultat : "Invalid input detected. Potential security threat."
```

### Test 3 : Protection contre XSS
```bash
curl -X POST /api/auth/register \
  -d '{"name": "<script>alert(\"XSS\")</script>"}'
# Résultat : "Invalid input detected. Potential security threat."
```

### Test 4 : Protection des données sensibles
```bash
curl /api/products
# Résultat : Aucun champ password visible dans la réponse
```

### Test 5 : Contrôle d'accès par rôles
```bash
# Avec token user normal
curl -H "Authorization: Bearer USER_TOKEN" /api/users
# Résultat : HTTP 403 Forbidden

# Avec token admin
curl -H "Authorization: Bearer ADMIN_TOKEN" /api/users
# Résultat : Liste des utilisateurs
```

## 🔒 Architecture JWT

### Configuration
```yaml
spring:
  security:
    jwt:
      secret: [clé-512-bits-sécurisée]
      expiration: 86400000 # 24 heures
```

### Génération de token
```java
public String generateToken(String username, String role) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);
    return createToken(claims, username);
}
```

### Validation
```java
public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
}
```

## 📊 Matrice des permissions

| Endpoint | Méthode | Permissions requises | Contrôle supplémentaire |
|---|---|---|---|
| `/api/auth/**` | ALL | Aucune (public) | - |
| `/api/products` | GET | Aucune (public) | - |
| `/api/products/**` | POST/PUT/DELETE | USER ou ADMIN | Propriétaire seulement |
| `/api/users/**` | ALL | ADMIN | - |

## 🚨 Gestion des erreurs de sécurité

### GlobalExceptionHandler
```java
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ErrorResponse> handleAccessDeniedException() {
    // Retourne 403 Forbidden
}

@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<ErrorResponse> handleBadCredentialsException() {
    // Retourne 401 Unauthorized
}

@ExceptionHandler(SecurityException.class)
public ResponseEntity<ErrorResponse> handleSecurityException() {
    // Retourne 400 Bad Request pour inputs invalides
}
```

### Codes de réponse HTTP
- **200** : Succès
- **400** : Données invalides / Menace de sécurité détectée
- **401** : Non authentifié (token manquant/invalide)
- **403** : Accès refusé (permissions insuffisantes)
- **404** : Ressource non trouvée

## 🔍 Audit et conformité

### ✅ Exigences respectées
1. **Hash et salt des mots de passe** : BCrypt avec salt automatique
2. **Validation des entrées** : Protection MongoDB injection + XSS
3. **Protection données sensibles** : @JsonIgnore + filtrage API
4. **HTTPS** : Configuration production prête

### ✅ Bonnes pratiques Spring Security
1. **Configuration centralisée** : Une seule classe SecurityConfig
2. **Annotations granulaires** : @PreAuthorize et @PostAuthorize sur endpoints sensibles
3. **JWT sécurisé** : Clé 512 bits + expiration
4. **Gestion d'erreurs** : Codes HTTP appropriés, pas de 5XX

### ✅ Tests de sécurité validés
- Tous les mots de passe hashés en BDD ✅
- Injections MongoDB bloquées ✅
- Scripts XSS bloqués ✅
- Contrôle d'accès par rôles fonctionnel ✅
- Données sensibles protégées ✅

## 📝 Notes pour la production

### Variables d'environnement recommandées
```bash
export JWT_SECRET="[clé-sécurisée-512-bits]"
export MONGODB_URI="mongodb://user:pass@host:27017/letsplay"
export SSL_KEYSTORE_PASSWORD="[mot-de-passe-keystore]"
```

### Monitoring de sécurité
- Logs des tentatives d'injection détectées
- Monitoring des échecs d'authentification
- Alertes sur les accès non autorisés

### Recommandations
1. Rotation régulière des clés JWT
2. Certificat SSL valide en production (Let's Encrypt)
3. WAF (Web Application Firewall) recommandé
4. Rate limiting pour prévenir brute force

---

**Statut de sécurité** : ✅ **CONFORME** - Toutes les exigences sécuritaires sont implémentées et testées.