 Architecture Spring Boot + MongoDB avec authentification JWT

  Fonctionnalités implémentées :

  - Modèles de données : User et Product avec validation
  - CRUD complet pour users et products
  - Authentification JWT avec rôles (admin/user)
  - Sécurité avancée : hashage des mots de passe, validation d'entrées, protection contre les injections
  - Gestion d'erreurs globale : codes HTTP appropriés, pas de 5XX
  - Autorisations : contrôle d'accès basé sur les rôles

  Structure créée :

  src/main/java/com/letsplay/letsplay/
  ├── config/SecurityConfig.java
  ├── controller/
  │   ├── AuthController.java
  │   ├── UserController.java
  │   └── ProductController.java
  ├── dto/
  │   ├── LoginRequest.java
  │   └── JwtResponse.java
  ├── exception/
  │   ├── GlobalExceptionHandler.java
  │   ├── ErrorResponse.java
  │   └── custom exceptions
  ├── model/
  │   ├── User.java
  │   └── Product.java
  ├── repository/
  │   ├── UserRepository.java
  │   └── ProductRepository.java
  ├── security/
  │   ├── JwtUtil.java
  │   ├── CustomUserDetailsService.java
  │   ├── JwtAuthenticationFilter.java
  │   └── InputSanitizer.java
  └── service/
      ├── UserService.java
      └── ProductService.java

  API Endpoints :

  - Auth : /api/auth/register, /api/auth/login
  - Users : CRUD complet (admin seulement)
  - Products : CRUD avec GET public, modifications pour propriétaires/admins

  Sécurité :

  - Mots de passe hashés avec BCrypt
  - Validation d'entrées contre injections MongoDB/XSS
  - JWT avec expiration 24h
  - Contrôle d'accès par rôles

  Pour tester l'application :
  1. Démarrer MongoDB
  2. Exécuter ./run.sh ou mvn spring-boot:run
  3. L'API sera disponible sur http://localhost:8080


  brew install mongodb-community