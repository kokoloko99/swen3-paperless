# Baut die Java-Anwendung mit Java 21 und Gradle.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY src src
# Bereinigt Windows-Zeilenenden und erstellt die ausführbare Anwendung ohne Testlauf.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

# Startet die fertige Anwendung mit der kleineren Java-Laufzeit.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
# Führt die gebaute Spring-Boot-Anwendung aus.
ENTRYPOINT ["java", "-jar", "app.jar"]