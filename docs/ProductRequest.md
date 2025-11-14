# Documentation Technique: ProductRequest.java

## Vue d'Ensemble
Le fichier `ProductRequest.java` représente un Data Transfer Object (DTO) utilisé pour encapsuler les données d'un produit dans le système. Il implémente des contraintes de validation pour garantir l'intégrité des données transmises.

## Structure du Code

### Package et Imports
```java
package com.letsplay.letsplay.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
```

### Classe ProductRequest
```java
public class ProductRequest {
    // Déclarations des champs avec annotations de validation
    // Constructeurs
    // Getters et Setters
}
```

## Analyse des Champs et Validations

### Champ `name`
```java
@NotBlank(message = "Product name is mandatory")
@Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
private String name;
```
- **Validation**:
  - `@NotBlank`: Le nom du produit est obligatoire
  - `@Size`: Doit contenir entre 2 et 200 caractères

### Champ `description`
```java
@Size(max = 1000, message = "Description cannot exceed 1000 characters")
private String description;
```
- **Validation**:
  - `@Size`: La description ne peut pas dépasser 1000 caractères

### Champ `price`
```java
@NotNull(message = "Price is mandatory")
@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
private Double price;
```
- **Validation**:
  - `@NotNull`: Le prix est obligatoire
  - `@DecimalMin`: Doit être supérieur à 0 (exclusif)

## Constructeurs

### Constructeur par défaut
```java
public ProductRequest() {}
```
- Initialise un objet vide

### Constructeur avec paramètres
```java
public ProductRequest(String name, String description, Double price) {
    this.name = name;
    this.description = description;
    this.price = price;
}
```
- Initialise un objet avec les valeurs fournies

## Méthodes d'Accès

### Getters et Setters
```java
public String getName() { return name; }
public void setName(String name) { this.name = name; }

public String getDescription() { return description; }
public void setDescription(String description) { this.description = description; }

public Double getPrice() { return price; }
public void setPrice(Double price) { this.price = price; }
```
- Méthodes standard pour l'accès et la modification des champs

## Architecture et Design

### Pattern Utilisé
- **DTO (Data Transfer Object)**:
  - Objet simple contenant des données
  - Pas de logique métier
  - Utilisé pour la transmission de données entre couches

### Validation
- Utilisation des annotations de validation de Jakarta EE
- Validation côté client avant envoi au serveur

### Couplage
- Couplage faible avec les autres composants
- Peut être utilisé par n'importe quelle couche nécessitant des données produit

## Sécurité

### Protection contre les injections
- Les validations empêchent les valeurs nulles ou vides
- Les contraintes de taille limitent les attaques par injection

## Performance

### Optimisations
- Structure légère sans surcharge
- Validation simple et efficace

## Maintenance

### Extensibilité
- Facile à étendre avec de nouveaux champs
- Les validations peuvent être ajoutées/modifiées sans impact sur le reste du système

## Résumé Technique

**Fonction Principale**: Encapsulation et validation des données produit pour la transmission entre couches.

**Technologies Clés**: Java, Jakarta Validation API

**Complexité**: Simple - Structure standard de DTO avec validations basiques

**Points Clés**:
- Validation des données avant transmission
- Structure légère et extensible
- Conformité aux bonnes pratiques DTO

**Impact Projet**: Composant essentiel pour la transmission sécurisée des données produit dans l'application.