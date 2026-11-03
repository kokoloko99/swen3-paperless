FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
# Reuse the project's Gradle wrapper; build from the repository root context.
COPY paperless-rest/gradlew ./
COPY paperless-rest/gradle gradle
COPY paperless-ocr-worker/build.gradle paperless-ocr-worker/settings.gradle ./
COPY paperless-ocr-worker/src src
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
