# Documentation Technique : InputSanitizer.java

## Architecture du Composant

Le fichier `InputSanitizer.java` est un composant de sécurité Spring Boot responsable de la validation et de la purification des entrées utilisateur. Il implémente des mécanismes de protection contre les injections SQL et les attaques XSS.

### Structure du Composant

```java
package com.letsplay.letsplay.security;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class InputSanitizer {
    // Déclarations des patterns de sécurité
    // Méthodes de validation et purification
}
```

## Analyse des Dépendances

### Imports

| Import | Usage | Version |
|--------|-------|---------|
| `java.util.regex.Pattern` | Gestion des expressions régulières pour la validation | Java Standard |
| `org.springframework.stereotype.Component` | Annotation Spring pour l'injection de dépendances | Spring Framework |

## Analyse des Patterns de Sécurité

### Patterns Détectés

1. **Pattern d'injection MongoDB** (Ligne 10-12)
   ```java
   private static final Pattern MONGO_INJECTION_PATTERN = Pattern.compile(
           ".*\\$.*|.*\\{.*\\}.*|.*javascript.*|.*eval.*|.*where.*",
           Pattern.CASE_INSENSITIVE
   );
   ```

2. **Pattern d'injection HTML/JS** (Ligne 15-17)
   ```java
   private static final Pattern HTML_SCRIPT_PATTERN = Pattern.compile(
           "<script[^>]*>.*?</script>|javascript:|on\\w+=",
           Pattern.CASE_INSENSITIVE | Pattern.DOTALL
   );
   ```

## Méthodes Principales

### 1. `isValidInput(String input)`

**Ligne 19-27**
```java
public boolean isValidInput(String input) {
    if (input == null) {
        return true;
    }

    return !MONGO_INJECTION_PATTERN.matcher(input).matches()
            && !HTML_SCRIPT_PATTERN.matcher(input).matches();
}
```

**Fonctionnalité**:
- Vérifie si une entrée contient des motifs potentiellement dangereux
- Utilise les deux patterns définis pour détecter les injections
- Retourne `true` pour les entrées null (considérées comme sûres)

### 2. `sanitize(String input)`

**Ligne 29-39**
```java
public String sanitize(String input) {
    if (input == null) {
        return null;
    }

    String sanitized = input.replaceAll("[<>\"'&]", "");

    sanitized = sanitized.replaceAll("\\$", "");
    sanitized = sanitized.replaceAll("\\{|\\}", "");

    return sanitized.trim();
}
```

**Fonctionnalité**:
- Purifie une entrée en supprimant les caractères spéciaux
- Supprime les caractères HTML (`<`, `>`, `"`, `'`, `&`)
- Supprime les caractères MongoDB spéciaux (`$`, `{`, `}`)
- Retourne une chaîne nettoyée et trimée

### 3. `validateUserInput(String... inputs)`

**Ligne 41-48**
```java
public void validateUserInput(String... inputs) {
    for (String input : inputs) {
        if (!isValidInput(input)) {
            throw new SecurityException("Invalid input detected. Potential security threat.");
        }
    }
}
```

**Fonctionnalité**:
- Valide plusieurs entrées utilisateur
- Lance une exception `SecurityException` si une entrée est jugée dangereuse
- Utilise la méthode `isValidInput` pour chaque entrée

## Gestion des Erreurs

### Exception Personnalisée

```java
throw new SecurityException("Invalid input detected. Potential security threat.");
```

**Caractéristiques**:
- Exception lancée lors de la détection d'une entrée potentiellement dangereuse
- Message clair indiquant le problème de sécurité
- Doit être gérée par les couches appelantes

## Flux de Données

### Diagramme de Flux

```
Entrée Utilisateur → Validation (isValidInput) →
   ├── Valide → Utilisation normale
   └── Invalide → Exception SecurityException

Entrée Utilisateur → Purification (sanitize) →
   → Retourne chaîne nettoyée
```

## Intégration avec Spring

### Annotation @Component

```java
@Component
public class InputSanitizer {
    // ...
}
```

**Impact**:
- Le composant est automatiquement détecté et injecté par Spring
- Peut être utilisé via injection de dépendances dans d'autres composants
- Géré par le conteneur Spring pour le cycle de vie

## Bonnes Pratiques Implémentées

1. **Validation avant purification**:
   - La validation (`isValidInput`) est effectuée avant la purification (`sanitize`)
   - Évite de traiter des entrées potentiellement dangereuses

2. **Gestion des entrées null**:
   - Les entrées null sont traitées explicitement
   - Évite les NullPointerException

3. **Expressions régulières robustes**:
   - Patterns insensibles à la casse
   - Utilisation de `DOTALL` pour les balises HTML multi-lignes

4. **Purification complète**:
   - Suppression des caractères HTML
   - Suppression des caractères MongoDB spéciaux

## Limites et Considérations

1. **Expressions régulières**:
   - Les patterns peuvent être améliorés pour couvrir plus de cas d'injection
   - Risque de faux positifs/négatifs

2. **Performance**:
   - Les opérations de remplacement de chaînes peuvent être coûteuses
   - À considérer pour des entrées très volumineuses

3. **Cas d'utilisation**:
   - Conçu pour les entrées utilisateur standard
   - Peut nécessiter des adaptations pour des formats spécifiques

## Résumé Technique

**Fonction Principale**: Protection contre les injections SQL et XSS en validant et purifiant les entrées utilisateur.

**Technologies Clés**: Java, Spring Framework, Expressions régulières.

**Complexité**: Moyenne - Implémente des mécanismes de sécurité standard avec une logique claire mais nécessite une compréhension des attaques courantes.

**Points Clés**:
- Validation des entrées via expressions régulières
- Purification des chaînes en supprimant les caractères dangereux
- Intégration avec Spring via l'annotation @Component
- Gestion explicite des entrées null

**Impact Projet**: Composant critique pour la sécurité de l'application, utilisé probablement dans les couches de contrôleur ou service pour valider les entrées avant traitement.