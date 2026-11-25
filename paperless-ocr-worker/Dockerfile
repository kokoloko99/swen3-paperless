# Baut den OCR-Worker mit Java 21.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
# Reuse the project's Gradle wrapper; build from the repository root context.
COPY paperless-rest/gradlew ./
COPY paperless-rest/gradle gradle
COPY paperless-ocr-worker/build.gradle paperless-ocr-worker/settings.gradle ./
COPY paperless-ocr-worker/src src
# Bereinigt Windows-Zeilenenden und erstellt die ausfÃ¼hrbare Worker-Anwendung.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar --no-daemon

# EnthÃ¤lt nur die Java-Laufzeit und den fertigen Worker.
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-eng tesseract-ocr-deu poppler-utils && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
