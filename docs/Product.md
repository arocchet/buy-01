# Documentation Technique : Modèle Product (MongoDB)

## Vue d'ensemble du modèle

Le fichier `Product.java` définit un modèle de données pour une entité "Produit" dans une application utilisant MongoDB comme base de données. Ce modèle est annoté avec des contraintes de validation et des annotations spécifiques à Spring Data MongoDB.

## Analyse détaillée du modèle

### Annotations et configuration

```java
@Document(collection = "products")
public class Product {
    // ...
}
```
- **Ligne 13**: Annotation `@Document` indique que cette classe représente une collection MongoDB nommée "products".
- **Ligne 14-25**: Importations des annotations de validation Jakarta EE et des classes Java nécessaires.

### Structure du modèle

#### Champs principaux

| Champ | Type | Annotations | Description |
|-------|------|-------------|-------------|
| id | String | `@Id` | Identifiant unique généré par MongoDB |
| name | String | `@NotBlank`, `@Size` | Nom du produit (2-200 caractères) |
| description | String | `@Size` | Description du produit (max 1000 caractères) |
| price | Double | `@NotNull`, `@DecimalMin` | Prix du produit (doit être > 0) |
| userId | String | `@NotBlank` | Identifiant de l'utilisateur propriétaire |

#### Constructeurs

```java
public Product() {} // Constructeur par défaut

public Product(String name, String description, Double price, String userId) {
    this.name = name;
    this.description = description;
    this.price = price;
    this.userId = userId;
}
```
- **Ligne 27-29**: Constructeur vide requis par Spring Data
- **Ligne 31-37**: Constructeur avec paramètres pour les champs principaux

#### Getters et Setters

```java
public String getId() { return id; }
public void setId(String id) { this.id = id; }
// ... (getters/setters pour tous les champs)
```
- **Ligne 39-78**: Méthodes d'accès standard pour tous les champs

### Validation des données

#### Contraintes sur les champs

| Champ | Contraintes | Message d'erreur |
|-------|-------------|-----------------|
| name | `@NotBlank` | "Product name is mandatory" |
| name | `@Size(min=2, max=200)` | "Product name must be between 2 and 200 characters" |
| description | `@Size(max=1000)` | "Description cannot exceed 1000 characters" |
| price | `@NotNull` | "Price is mandatory" |
| price | `@DecimalMin(value="0.0", inclusive=false)` | "Price must be greater than 0" |
| userId | `@NotBlank` | "User ID is mandatory" |

### Utilisation du modèle

#### Création d'un produit

```java
Product produit = new Product(
    "Nouveau produit",
    "Description détaillée du produit",
    19.99,
    "user123"
);
```

#### Mise à jour d'un produit

```java
produit.setName("Nouveau nom");
produit.setPrice(24.99);
```

#### Accès aux propriétés

```java
String nom = produit.getName();
Double prix = produit.getPrice();
```

## Architecture et intégration

### Intégration avec Spring Data MongoDB

Le modèle `Product` est conçu pour être utilisé avec Spring Data MongoDB :

1. **Annotation `@Document`** : Lie la classe à une collection MongoDB
2. **Annotation `@Id`** : Indique le champ utilisé comme identifiant unique
3. **Annotations de validation** : Assurent la validité des données avant persistance

### Relations avec d'autres modèles

Bien que le fichier ne montre pas explicitement de relations, on peut supposer (HYPOTHÈSE) que :

- Le champ `userId` fait référence à un utilisateur dans une autre collection
- Le modèle pourrait être utilisé dans des relations avec d'autres entités comme `Order` ou `Category`

## Bonnes pratiques implémentées

1. **Validation des données** : Utilisation des annotations Jakarta Validation
2. **Encapsulation** : Getters et setters pour tous les champs
3. **Constructeurs** : Constructeur vide et constructeur avec paramètres
4. **Documentation implicite** : Annotations fournissent des informations claires sur les contraintes

## Résumé Technique

**Fonction Principale**: Modèle de données pour la gestion des produits dans une application e-commerce utilisant MongoDB.

**Technologies Clés**: Spring Data MongoDB, Jakarta Validation, Java POJO.

**Complexité**: Moyenne - Le modèle est simple mais intègre des contraintes de validation et des annotations spécifiques à MongoDB.

**Points Clés**:
- Utilisation de MongoDB comme base de données NoSQL
- Validation des données au niveau du modèle
- Structure POJO standard avec getters/setters
- Intégration avec Spring Data pour la persistance

**Impact Projet**: Composant fondamental pour la gestion des produits dans l'application, servant de base pour les opérations CRUD et les relations avec d'autres entités.