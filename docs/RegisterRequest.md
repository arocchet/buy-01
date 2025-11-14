# Documentation Technique : RegisterRequest.java

## Vue d'ensemble
Le fichier `RegisterRequest.java` est un Data Transfer Object (DTO) utilisé pour encapsuler les données nécessaires à l'enregistrement d'un utilisateur dans le système. Il implémente des contraintes de validation pour garantir l'intégrité des données avant leur traitement.

## Structure du DTO

### Champs et contraintes de validation

| Champ       | Type    | Contraintes                                                                 | Valeur par défaut | Description                     |
|-------------|---------|---------------------------------------------------------------------------|-------------------|---------------------------------|
| name        | String  | `@NotBlank`, `@Size(min=2, max=100)`                                     | -                 | Nom de l'utilisateur           |
| email       | String  | `@NotBlank`, `@Email`                                                    | -                 | Adresse email                  |
| password    | String  | `@NotBlank`, `@Size(min=8)`                                               | -                 | Mot de passe                   |
| role        | String  | `@Pattern(regexp="^(admin|user)$")`                                       | "user"            | Rôle de l'utilisateur (admin/user) |

### Constructeurs

- **Constructeur par défaut** (Ligne 13) : Initialise un objet vide
- **Constructeur avec paramètres** (Ligne 15) : Initialise tous les champs avec les valeurs fournies

### Méthodes d'accès

| Méthode               | Description                          |
|-----------------------|--------------------------------------|
| `getName()`/`setName()` | Accès/Modification du champ name     |
| `getEmail()`/`setEmail()` | Accès/Modification du champ email   |
| `getPassword()`/`setPassword()` | Accès/Modification du champ password |
| `getRole()`/`setRole()` | Accès/Modification du champ role    |

## Analyse des contraintes de validation

1. **Nom (name)** :
   - Doit être non vide (`@NotBlank`)
   - Doit avoir entre 2 et 100 caractères (`@Size`)

2. **Email (email)** :
   - Doit être non vide (`@NotBlank`)
   - Doit respecter le format d'une adresse email valide (`@Email`)

3. **Mot de passe (password)** :
   - Doit être non vide (`@NotBlank`)
   - Doit contenir au moins 8 caractères (`@Size`)

4. **Rôle (role)** :
   - Doit être soit "admin" soit "user" (`@Pattern`)
   - Valeur par défaut : "user"

## Architecture et intégration

### Rôle dans l'application

Ce DTO sert d'interface entre la couche présentation et la couche métier pour :
- La validation des données d'enregistrement
- Le transport des informations utilisateur
- La transformation des données avant traitement

### Intégration avec le système

1. **Validation** : Les annotations de validation sont traitées par le framework (probablement Spring) avant le traitement des données
2. **Sérialisation** : Peut être converti en JSON/XML pour les API REST
3. **Désérialisation** : Peut être construit à partir de requêtes HTTP entrantes

## Bonnes pratiques implémentées

1. **Validation côté client** : Les contraintes sont définies au niveau du DTO
2. **Sécurité** : Mot de passe avec longueur minimale
3. **Typage strict** : Utilisation de types primitifs appropriés
4. **Documentation implicite** : Les annotations fournissent une documentation claire des contraintes

## Résumé Technique

**Fonction Principale**: DTO pour la gestion des données d'enregistrement utilisateur avec validation intégrée

**Technologies Clés**: Java, Jakarta Validation API

**Complexité**: Simple - Structure standard de DTO avec validation de base

**Points Clés**:
- Validation des données avant traitement
- Structure claire avec getters/setters
- Contraintes explicites via annotations
- Valeur par défaut pour le rôle utilisateur

**Impact Projet**: Composant critique pour la gestion des utilisateurs, assurant la qualité des données en entrée du système