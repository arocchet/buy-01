# ---------- BUILD ----------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Dépendances d'abord (meilleur cache)
COPY pom.xml .
RUN apt-get update \
 && apt-get install -y maven \
 && mvn -B dependency:go-offline

# Code source
COPY src ./src

# Build
RUN mvn clean package -DskipTests


# ---------- RUNTIME ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Dossiers nécessaires à l'app
RUN mkdir -p /app/uploads /app/uploads/avatars

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
