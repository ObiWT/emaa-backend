# 1Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Skopíruj POM a zdrojový kód
COPY pom.xml .
COPY src ./src

# Maven build – jOOQ codegen sa spustí, použije ENV premenné
RUN mvn clean package -DskipTests

# 2Runtime stage (ľahký obraz)
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Skopíruj WAR z build stage
COPY --from=build /app/target/emaa-system-0.0.1-SNAPSHOT.war /app/app.war

EXPOSE 8080

# Spustenie WAR na porte z ENV (Render)
ENTRYPOINT ["sh", "-c", "java -jar app.war --server.port=$PORT"]
