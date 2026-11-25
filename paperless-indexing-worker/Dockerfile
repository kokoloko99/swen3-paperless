# Baut den eigenstaendigen Indexing-Worker mit Java 21.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
# Reuse the project's Gradle wrapper; build from the repository root context.
COPY paperless-rest/gradlew ./
COPY paperless-rest/gradle gradle
COPY paperless-indexing-worker/build.gradle paperless-indexing-worker/settings.gradle ./
COPY paperless-indexing-worker/src src
# Bereinigt Windows-Zeilenenden und erstellt die ausfÃ¼hrbare Worker-Anwendung.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar --no-daemon

# EnthÃ¤lt nur die Java-Laufzeit und den fertigen Worker.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
