# ---------- BUILD ----------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY pom.xml .
RUN apt-get update \
 && apt-get install -y maven \
 && mvn -B dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


# ---------- RUNTIME ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
