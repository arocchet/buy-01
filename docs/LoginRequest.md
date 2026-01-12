# Documentation Technique : LoginRequest.java

## Analyse du Modèle de Données

### Structure de la Classe
La classe `LoginRequest` (Ligne 1) est un DTO (Data Transfer Object) conçu pour encapsuler les données d'authentification utilisateur. Elle implémente un modèle simple avec deux propriétés principales :

1. **Email** (Ligne 5-6)
   - Validé par `@NotBlank` (Ligne 5) pour garantir la présence d'une valeur
   - Validé par `@Email` (Ligne 6) pour vérifier le format d'adresse email
   - Message d'erreur personnalisé pour chaque validation

2. **Password** (Ligne 9)
   - Validé uniquement par `@NotBlank` (Ligne 9)
   - Message d'erreur personnalisé pour l'absence de mot de passe

### Constructeurs
- **Constructeur par défaut** (Ligne 12) : Permet l'instanciation sans paramètres
- **Constructeur avec paramètres** (Ligne 14-15) : Initialise les deux propriétés

### Méthodes d'Accès
- **Getters et Setters** (Ligne 17-24) :
  - `getEmail()`/`setEmail()` pour la gestion de l'adresse email
  - `getPassword()`/`setPassword()` pour la gestion du mot de passe

## Validation des Données

### Règles de Validation
| Champ | Annotation | Message d'Erreur |
|-------|------------|------------------|
| email | @NotBlank | "Email is mandatory" |
| email | @Email | "Email should be valid" |
| password | @NotBlank | "Password is mandatory" |

### Implémentation
Les validations sont appliquées via les annotations Jakarta Validation :
- `@NotBlank` : Vérifie que la chaîne n'est pas nulle, vide ou composée uniquement d'espaces
- `@Email` : Vérifie le format standard d'une adresse email

## Architecture et Intégration

### Rôle dans l'Application
Ce DTO sert d'interface entre :
1. La couche présentation (frontend) pour la collecte des données
2. La couche service pour le traitement de l'authentification

### Flux de Données
1. **Entrée** : Données saisies par l'utilisateur
2. **Validation** : Vérification des contraintes via les annotations
3. **Transfert** : Transmission vers les services d'authentification

## Bonnes Pratiques Implémentées

1. **Encapsulation** : Toutes les propriétés sont privées avec accès contrôlé
2. **Validation** : Intégration des règles de validation au niveau du modèle
3. **Constructeurs** : Présence des deux types de constructeurs (par défaut et paramétré)
4. **Documentation** : Messages d'erreur explicites pour les validations

## Résumé Technique

**Fonction Principale**: Modèle de transfert de données pour l'authentification utilisateur avec validation intégrée.

**Technologies Clés**: Java, Jakarta Validation API

**Complexité**: Simple - Structure linéaire avec validation standardisée

**Points Clés**:
- Validation des champs email et mot de passe
- Structure DTO standard avec getters/setters
- Intégration des annotations de validation

**Impact Projet**: Composant critique pour le flux d'authentification, garantissant la qualité des données avant traitement.