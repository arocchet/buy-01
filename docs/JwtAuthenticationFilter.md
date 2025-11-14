# Documentation Technique : JwtAuthenticationFilter.java

## Vue d'Ensemble

Le fichier `JwtAuthenticationFilter.java` implémente un filtre d'authentification JWT (JSON Web Token) pour une application Spring Security. Ce composant est responsable de l'extraction, de la validation et de l'application des tokens JWT dans le contexte de sécurité de l'application.

## Structure du Code

```java
package com.letsplay.letsplay.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // ... implémentation ...
}
```

## Dépendances et Annotations

- **Annotations** :
  - `@Component` (Ligne 15) : Indique que cette classe est un composant Spring géré par le conteneur.
  - `@Autowired` (Lignes 17-18) : Injection de dépendances pour `JwtUtil` et `CustomUserDetailsService`.

- **Dépendances** :
  - `JwtUtil` : Service utilitaire pour manipuler les tokens JWT.
  - `CustomUserDetailsService` : Service personnalisé pour charger les détails utilisateur.

## Méthode Principale : `doFilterInternal`

```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                              FilterChain filterChain) throws ServletException, IOException {
    // ... implémentation ...
}
```

### Flux de Traitement

1. **Extraction du Header d'Authorization** (Ligne 21) :
   ```java
   final String authorizationHeader = request.getHeader("Authorization");
   ```

2. **Validation du Format JWT** (Ligne 24-27) :
   ```java
   if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
       jwt = authorizationHeader.substring(7);
       try {
           username = jwtUtil.extractUsername(jwt);
       } catch (Exception e) {
           logger.error("JWT token extraction failed", e);
       }
   }
   ```

3. **Authentification de l'Utilisateur** (Ligne 30-42) :
   ```java
   if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
       UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

       if (jwtUtil.validateToken(jwt, userDetails)) {
           UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                   new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
           usernamePasswordAuthenticationToken
                   .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
           SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
       }
   }
   ```

4. **Poursuite de la Chaîne de Filtres** (Ligne 44) :
   ```java
   filterChain.doFilter(request, response);
   ```

## Gestion des Erreurs

- **Extraction du Username** (Ligne 26) :
  ```java
  try {
      username = jwtUtil.extractUsername(jwt);
  } catch (Exception e) {
      logger.error("JWT token extraction failed", e);
  }
  ```

- **Validation du Token** (Ligne 36) :
  ```java
  if (jwtUtil.validateToken(jwt, userDetails)) {
      // ... authentification ...
  }
  ```

## Intégration avec Spring Security

- **Contexte de Sécurité** (Ligne 39-41) :
  ```java
  SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
  ```

- **Détails de l'Authentification** (Ligne 40) :
  ```java
  usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
  ```

## Résumé Technique

**Fonction Principale** : Filtre d'authentification JWT pour valider et appliquer les tokens d'authentification dans le contexte de sécurité Spring.

**Technologies Clés** : Spring Security, JWT, Servlet Filters.

**Complexité** : Moyenne. Le code gère plusieurs étapes critiques (extraction, validation, authentification) avec une logique conditionnelle bien structurée.

**Points Clés** :
- Extraction et validation des tokens JWT.
- Intégration avec le contexte de sécurité Spring.
- Gestion des erreurs lors de l'extraction du username.

**Impact Projet** : Composant essentiel pour la sécurité de l'application, assurant l'authentification des utilisateurs via JWT.