package at.fhtw.swen3.paperless_ocr_worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Startet den OCR-Worker als eigene Spring-Boot-Anwendung.
@SpringBootApplication
public class OcrWorkerApplication {
    // Startet den Worker, der Nachrichten aus RabbitMQ empfängt.
    public static void main(String[] args) {
        SpringApplication.run(OcrWorkerApplication.class, args);
    }
}
