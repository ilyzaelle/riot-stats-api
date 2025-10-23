# --- Étape de build ---
FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

# Copier le code source
COPY . .

# Compiler et packager l’application
RUN mvn clean package -DskipTests

# --- Étape de runtime ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copier le JAR généré depuis la première étape
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Lancer l'application Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
