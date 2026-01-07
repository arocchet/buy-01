1. Inscription (Register)

  ```POST http://localhost:8080/api/auth/register```

  Content-Type: application/json

  Body (raw JSON):

  ```
  {
      "name": "Test User",
      "email": "test@postman.com",
      "password": "password123",
      "role": "user"
  }
  ```


  2. Connexion (Login)

  ```POST http://localhost:8080/api/auth/login```

  Content-Type: application/json

  Body (raw JSON):

  ```
  {
      "email": "john@example.com",
      "password": "password123"
  }
  ```

  3. Créer un produit (Auth requise)

  ```POST http://localhost:8080/api/products```
  Authorization: Bearer {{auth_token}}
  Content-Type: application/json

  Body (raw JSON):

 ```
  {
      "name": "MacBook Pro",
      "description": "Laptop professionnel pour 
  développement",
      "price": 2499.99
  }
```

  4. Récupérer tous les produits (Public)

  ```GET http://localhost:8080/api/products```

  5. Récupérer un produit par ID

  ```GET http://localhost:8080/api/products/6913138a```

  2a52051a7f143659

  6. Récupérer tous les utilisateurs (Admin 
  seulement)

  ```GET http://localhost:8080/api/users```

  Authorization: Bearer {{auth_token}}
  Note : Ceci échouera avec un 403 car votre 
  token est pour un user normal

  7. Mettre à jour un produit

  ```PUT http://localhost:8080/api/products/6913138a```

  Authorization: Bearer {{auth_token}}
  Content-Type: application/json

  Body (raw JSON):

 ```
  {
      "name": "MacBook Pro Updated",
      "description": "Laptop mis à jour",
      "price": 2299.99
  }
```

  8. Supprimer un produit

  ```DELETE http://localhost:8080/api/products/6913138a2a52051a7f143659```

  Authorization: Bearer {{auth_token}}