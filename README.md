# Let's Play - Spring Boot CRUD API

Une API RESTful CRUD développée avec Spring Boot et MongoDB pour la gestion d'utilisateurs et de produits avec authentification JWT.

## Fonctionnalités

- **Gestion des utilisateurs** : CRUD complet avec rôles (admin/user)
- **Gestion des produits** : CRUD complet avec propriété par utilisateur
- **Authentification JWT** : Authentification basée sur des tokens
- **Sécurité avancée** : Hashage des mots de passe, validation des entrées, prévention des injections
- **Gestion d'erreurs** : Gestion globale des exceptions avec codes HTTP appropriés

## Prérequis

- Java 17 ou supérieur
- Maven 3.6+
- MongoDB 4.0+ en cours d'exécution

## Installation et Exécution

1. **Clonez le projet** :
```bash
git clone <repository-url>
cd lets-play
```

2. **Démarrez MongoDB** :
```bash
# Sur macOS avec Homebrew
brew services start mongodb-community

# Sur Linux/Ubuntu
sudo systemctl start mongod

# Ou démarrez MongoDB manuellement
mongod
```

3. **Exécutez l'application** :
```bash
# Utilisation du script fourni
./run.sh

# Ou manuellement
./mvnw clean spring-boot:run
```

L'application sera disponible sur `http://localhost:8080`

## Endpoints API

### Authentification
- `POST /api/auth/register` - Inscription d'un nouvel utilisateur
- `POST /api/auth/login` - Connexion et récupération du token JWT

### Utilisateurs (nécessite le rôle ADMIN)
- `GET /api/users` - Récupérer tous les utilisateurs
- `GET /api/users/{id}` - Récupérer un utilisateur par ID
- `PUT /api/users/{id}` - Mettre à jour un utilisateur
- `DELETE /api/users/{id}` - Supprimer un utilisateur

### Produits
- `GET /api/products` - Récupérer tous les produits (accessible sans authentification)
- `GET /api/products/{id}` - Récupérer un produit par ID
- `POST /api/products` - Créer un nouveau produit (authentification requise)
- `PUT /api/products/{id}` - Mettre à jour un produit (propriétaire ou admin uniquement)
- `DELETE /api/products/{id}` - Supprimer un produit (propriétaire ou admin uniquement)
- `GET /api/products/user/{userId}` - Récupérer les produits d'un utilisateur
- `GET /api/products/search?name={name}` - Rechercher des produits par nom

## Exemples d'utilisation

### 1. Inscription d'un utilisateur
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "role": "user"
  }'
```

### 2. Connexion
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### 3. Création d'un produit (avec token JWT)
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99
  }'
```

### 4. Récupération des produits
```bash
curl -X GET http://localhost:8080/api/products
```

## Modèle de données

### User
```json
{
  "id": "string",
  "name": "string",
  "email": "string",
  "role": "user|admin"
}
```

### Product
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "price": "number",
  "userId": "string"
}
```

## Sécurité

- **Mots de passe** : Hashés avec BCrypt
- **JWT** : Tokens avec expiration (24h par défaut)
- **Validation des entrées** : Protection contre les injections MongoDB et XSS
- **Autorisations** : Contrôle d'accès basé sur les rôles
- **CORS** : Configuré pour accepter toutes les origines (à adapter pour la production)

## Configuration

Modifiez `src/main/resources/application.yml` pour personnaliser :
- URI MongoDB
- Secret JWT
- Durée d'expiration des tokens
- Port du serveur
- Niveaux de logging

## Tests

Les endpoints peuvent être testés avec curl, Postman, ou tout autre client HTTP.

## Structure du projet

```
src/
├── main/
│   ├── java/com/letsplay/letsplay/
│   │   ├── config/          # Configuration de sécurité
│   │   ├── controller/      # Contrôleurs REST
│   │   ├── dto/             # Objets de transfert de données
│   │   ├── exception/       # Gestion des exceptions
│   │   ├── model/           # Entités MongoDB
│   │   ├── repository/      # Interfaces de repository
│   │   ├── security/        # Utilitaires de sécurité et JWT
│   │   └── service/         # Services métier
│   └── resources/
│       └── application.yml  # Configuration de l'application
└── test/                    # Tests unitaires et d'intégration
```