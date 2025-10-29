# Používame oficiálny JDK 21 obraz
FROM openjdk:21-jdk-slim

# Nastavíme pracovný adresár
WORKDIR /app

# Kopírujeme WAR súbor z buildu do kontajnera
COPY target/emaa-system-0.0.1-SNAPSHOT.war /app/app.war

# Otvoríme port 8080 (Spring Boot default)
EXPOSE 8080

# Spustíme aplikáciu
#ENTRYPOINT ["java", "-jar", "app.war"]
ENTRYPOINT ["sh", "-c", "java -jar app.war --server.port=$PORT"]
