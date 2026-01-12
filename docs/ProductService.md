# Documentation Technique - ProductService.java

## Vue d'ensemble
Le fichier `ProductService.java` implémente un service métier pour la gestion des produits dans l'application. Il encapsule les opérations CRUD (Create, Read, Update, Delete) et des fonctionnalités spécifiques aux produits, avec une intégration de sécurité via un sanitizer d'entrées.

## Structure du Service

### Dépendances
```java
// Ligne 4-10: Déclarations des dépendances
@Autowired
private ProductRepository productRepository;

@Autowired
private InputSanitizer inputSanitizer;
```

### Méthodes Principales

#### 1. Création de Produit
```java
// Ligne 12-20: Méthode createProduct
public Product createProduct(Product product) {
    inputSanitizer.validateUserInput(product.getName(), product.getDescription());
    product.setName(inputSanitizer.sanitize(product.getName()));
    product.setDescription(inputSanitizer.sanitize(product.getDescription()));
    return productRepository.save(product);
}
```
- **Fonction**: Crée un nouveau produit après validation et sanitization des entrées
- **Sécurité**: Utilise `InputSanitizer` pour nettoyer les champs sensibles
- **Validation**: Vérifie les entrées avant création

#### 2. Récupération de Produits
```java
// Ligne 22-25: Méthode getAllProducts
public List<Product> getAllProducts() {
    return productRepository.findAll();
}
```
- **Fonction**: Récupère tous les produits disponibles
- **Performance**: Appel direct au repository sans traitement supplémentaire

#### 3. Récupération par ID
```java
// Ligne 27-30: Méthode getProductById
public Optional<Product> getProductById(String id) {
    return productRepository.findById(id);
}
```
- **Fonction**: Récupère un produit spécifique par son ID
- **Retour**: Utilise `Optional` pour gérer les cas où le produit n'existe pas

#### 4. Récupération par Utilisateur
```java
// Ligne 32-35: Méthode getProductsByUserId
public List<Product> getProductsByUserId(String userId) {
    return productRepository.findByUserId(userId);
}
```
- **Fonction**: Récupère tous les produits associés à un utilisateur spécifique
- **Relation**: Utilise la relation entre produits et utilisateurs

#### 5. Recherche par Nom
```java
// Ligne 37-40: Méthode searchProductsByName
public List<Product> searchProductsByName(String name) {
    return productRepository.findByNameContainingIgnoreCase(name);
}
```
- **Fonction**: Recherche des produits par nom (insensible à la casse)
- **Algorithme**: Utilise une recherche partielle dans le nom

#### 6. Mise à Jour de Produit
```java
// Ligne 42-58: Méthode updateProduct
public Product updateProduct(String id, Product productDetails) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

    if (productDetails.getName() != null) {
        product.setName(productDetails.getName());
    }
    if (productDetails.getDescription() != null) {
        product.setDescription(productDetails.getDescription());
    }
    if (productDetails.getPrice() != null) {
        product.setPrice(productDetails.getPrice());
    }

    return productRepository.save(product);
}
```
- **Fonction**: Met à jour partiellement un produit existant
- **Partial Update**: Ne modifie que les champs fournis
- **Validation**: Vérifie l'existence du produit avant mise à jour

#### 7. Suppression de Produit
```java
// Ligne 60-66: Méthode deleteProduct
public void deleteProduct(String id) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    productRepository.delete(product);
}
```
- **Fonction**: Supprime un produit du système
- **Sécurité**: Vérifie l'existence avant suppression

#### 8. Vérification de Propriétaire
```java
// Ligne 68-73: Méthode isProductOwner
public boolean isProductOwner(String productId, String userId) {
    Optional<Product> product = productRepository.findById(productId);
    return product.isPresent() && product.get().getUserId().equals(userId);
}
```
- **Fonction**: Vérifie si un utilisateur est le propriétaire d'un produit
- **Sécurité**: Utilisé pour les contrôles d'accès

## Modèle de Données

### Classe Product
```java
// Référence à la classe Product (importée ligne 4)
com.letsplay.letsplay.model.Product
```
- **Champs principaux**:
  - `id`: Identifiant unique
  - `name`: Nom du produit
  - `description`: Description
  - `price`: Prix
  - `userId`: Identifiant de l'utilisateur propriétaire

## Sécurité

### Sanitization des Entrées
```java
// Ligne 14-16: Sanitization des champs
inputSanitizer.validateUserInput(product.getName(), product.getDescription());
product.setName(inputSanitizer.sanitize(product.getName()));
product.setDescription(inputSanitizer.sanitize(product.getDescription()));
```
- **Protection**: Nettoyage des champs sensibles avant stockage
- **Validation**: Vérification des entrées avant traitement

## Gestion des Erreurs

### Exceptions
```java
// Ligne 44-45: Gestion d'erreur
.orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
```
- **Cas d'erreur**: Produit non trouvé
- **Type**: `RuntimeException` avec message descriptif

## Résumé Technique

**Fonction Principale**: Service métier central pour la gestion des produits avec des fonctionnalités CRUD complètes et des contrôles de sécurité.

**Technologies Clés**: Spring Framework, JPA Repository, Java 8+ features (Optional, Streams)

**Complexité**: Moyenne - Implémentation standard avec quelques considérations de sécurité supplémentaires.

**Points Clés**:
- Intégration complète avec le repository
- Sanitization des entrées pour la sécurité
- Gestion des erreurs explicite
- Contrôle d'accès via vérification de propriétaire

**Impact Projet**: Composant essentiel de l'architecture, servant d'intermédiaire entre le contrôleur et le repository, avec des responsabilités étendues en matière de sécurité et de validation.