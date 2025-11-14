# Documentation Technique Complète : ProductController.java

## Vue d'Ensemble

Le fichier `ProductController.java` constitue le cœur de l'API de gestion des produits dans l'application Let's Play. Ce contrôleur REST implémente toutes les opérations CRUD (Create, Read, Update, Delete) pour les produits, avec une gestion fine des autorisations et une intégration avec le système d'authentification.

## Architecture API

### Modèle Architectural
- **Pattern RESTful** : Implémentation stricte des conventions REST
- **Couches séparées** : Contrôleur (API) / Service (logique métier) / Modèle (entités)
- **Sécurité intégrée** : Gestion des rôles et permissions via Spring Security

### Configuration
- **Base URL** : `/api/products`
- **CORS** : Configuration permissive (`@CrossOrigin(origins = "*", maxAge = 3600)`)
- **Validation** : Utilisation de `@Valid` pour les DTOs

## Endpoints API

### 1. Récupération des Produits

#### GET /api/products
```java
@GetMapping
public ResponseEntity<List<Product>> getAllProducts() {
    List<Product> products = productService.getAllProducts();
    return ResponseEntity.ok(products);
}
```
- **Description** : Récupère la liste complète des produits
- **Authentification** : Non requise
- **Réponse** : Liste de tous les produits disponibles

#### GET /api/products/{id}
```java
@GetMapping("/{id}")
public ResponseEntity<?> getProductById(@PathVariable String id) {
    try {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error retrieving product: " + e.getMessage());
    }
}
```
- **Description** : Récupère un produit spécifique par son ID
- **Authentification** : Non requise
- **Gestion des erreurs** : Retourne 404 si produit non trouvé

### 2. Gestion des Produits (CRUD)

#### POST /api/products
```java
@PostMapping
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest productRequest) {
    // Implémentation complète...
}
```
- **Description** : Crée un nouveau produit
- **Authentification** : Requise (rôles USER ou ADMIN)
- **Validation** : Utilisation de `@Valid` sur le DTO
- **Logique métier** : Associe automatiquement le produit à l'utilisateur authentifié

#### PUT /api/products/{id}
```java
@PutMapping("/{id}")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest productRequest) {
    // Implémentation complète...
}
```
- **Description** : Met à jour un produit existant
- **Authentification** : Requise (rôles USER ou ADMIN)
- **Contrôle d'accès** : Vérifie que l'utilisateur est propriétaire du produit ou est admin
- **Validation** : Utilisation de `@Valid` sur le DTO

#### DELETE /api/products/{id}
```java
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public ResponseEntity<?> deleteProduct(@PathVariable String id) {
    // Implémentation complète...
}
```
- **Description** : Supprime un produit
- **Authentification** : Requise (rôles USER ou ADMIN)
- **Contrôle d'accès** : Vérifie que l'utilisateur est propriétaire du produit ou est admin

### 3. Fonctionnalités Avancées

#### GET /api/products/user/{userId}
```java
@GetMapping("/user/{userId}")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public ResponseEntity<List<Product>> getProductsByUserId(@PathVariable String userId) {
    List<Product> products = productService.getProductsByUserId(userId);
    return ResponseEntity.ok(products);
}
```
- **Description** : Récupère tous les produits d'un utilisateur spécifique
- **Authentification** : Requise (rôles USER ou ADMIN)

#### GET /api/products/search
```java
@GetMapping("/search")
public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
    List<Product> products = productService.searchProductsByName(name);
    return ResponseEntity.ok(products);
}
```
- **Description** : Recherche des produits par nom
- **Authentification** : Non requise
- **Paramètre** : `name` (obligatoire)

## Implémentation Technique

### Gestion de l'Authentification
```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
String userEmail = authentication.getName();
```
- **Mécanisme** : Utilisation du contexte de sécurité Spring
- **Récupération** : Email de l'utilisateur authentifié
- **Validation** : Vérification de l'existence de l'utilisateur

### Contrôle d'Accès
```java
if (!user.getRole().equals("admin") && !productService.isProductOwner(id, user.getId())) {
    return ResponseEntity.status(403).body("Access denied: You can only modify your own products");
}
```
- **Logique** : Vérification du rôle et de la propriété
- **Réponse** : 403 Forbidden si accès non autorisé

### Gestion des Erreurs
```java
try {
    // Logique métier
} catch (RuntimeException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
}
```
- **Approche** : Gestion centralisée des exceptions
- **Réponse** : Message d'erreur détaillé en cas d'échec

## Intégration avec les Services

### ProductService
```java
@Autowired
private ProductService productService;
```
- **Rôle** : Délégation de la logique métier
- **Méthodes utilisées** :
  - `getAllProducts()`
  - `getProductById(String id)`
  - `createProduct(Product product)`
  - `updateProduct(String id, Product productDetails)`
  - `deleteProduct(String id)`
  - `getProductsByUserId(String userId)`
  - `searchProductsByName(String name)`
  - `isProductOwner(String productId, String userId)`

### UserService
```java
@Autowired
private UserService userService;
```
- **Rôle** : Interaction avec le système d'utilisateurs
- **Méthodes utilisées** :
  - `getUserByEmail(String email)`

## Modèles Utilisés

### ProductRequest (DTO)
```java
@Valid @RequestBody ProductRequest productRequest
```
- **Rôle** : Transport des données de produit
- **Champs** :
  - `name` (String)
  - `description` (String)
  - `price` (Double)

### Product (Entité)
```java
Product product = new Product();
product.setName(productRequest.getName());
product.setDescription(productRequest.getDescription());
product.setPrice(productRequest.getPrice());
product.setUserId(userOptional.get().getId());
```
- **Champs** :
  - `id` (String)
  - `name` (String)
  - `description` (String)
  - `price` (Double)
  - `userId` (String)

### User (Entité)
```java
Optional<User> userOptional = userService.getUserByEmail(userEmail);
User user = userOptional.get();
```
- **Champs utilisés** :
  - `id` (String)
  - `role` (String)

## Sécurité

### Annotation @PreAuthorize
```java
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
```
- **Rôle** : Contrôle d'accès basé sur les rôles
- **Endpoints protégés** :
  - POST /api/products
  - PUT /api/products/{id}
  - DELETE /api/products/{id}
  - GET /api/products/user/{userId}

### Gestion des Rôles
```java
if (!user.getRole().equals("admin") && !productService.isProductOwner(id, user.getId())) {
    // Accès refusé
}
```
- **Logique** :
  - Les admins ont accès à tous les produits
  - Les utilisateurs normaux ne peuvent accéder qu'à leurs propres produits

## Validation des Données

### Validation des DTOs
```java
@Valid @RequestBody ProductRequest productRequest
```
- **Mécanisme** : Utilisation de l'annotation `@Valid`
- **Validation** :
  - Vérification des contraintes définies dans le DTO
  - Retourne 400 Bad Request en cas d'erreur

## Gestion des Réponses

### Réponses Standard
```java
return ResponseEntity.ok(product);
```
- **Code HTTP** : 200 OK
- **Format** : JSON

### Réponses d'Erreur
```java
return ResponseEntity.badRequest().body("Error message");
```
- **Code HTTP** : 400 Bad Request
- **Format** : Message d'erreur en texte

### Réponses Not Found
```java
return ResponseEntity.notFound().build();
```
- **Code HTTP** : 404 Not Found
- **Format** : Corps vide

## Résumé Technique

**Fonction Principale** : Contrôleur REST pour la gestion des produits avec gestion fine des autorisations et intégration avec le système d'authentification.

**Technologies Clés** : Spring Boot, Spring Security, Spring Web, Jakarta Validation, Java 17+.

**Complexité** : Élevée - Implémente une logique métier complexe avec gestion des rôles, validation des données et intégration avec plusieurs services.

**Points Clés** :
- Gestion complète du cycle de vie des produits (CRUD)
- Contrôle d'accès basé sur les rôles et la propriété des produits
- Validation des données en entrée
- Gestion centralisée des erreurs
- Intégration avec les services métier

**Impact Projet** : Composant central de l'application, assurant l'interaction entre les utilisateurs et les produits avec une sécurité renforcée.