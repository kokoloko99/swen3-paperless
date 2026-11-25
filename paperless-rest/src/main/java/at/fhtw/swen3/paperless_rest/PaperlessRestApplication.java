package at.fhtw.swen3.paperless_rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Startet den REST-Server und lädt die Spring-Konfiguration.
@SpringBootApplication
public class PaperlessRestApplication {

    // Startet die Spring-Boot-Anwendung.
	public static void main(String[] args) {
		SpringApplication.run(PaperlessRestApplication.class, args);
	}

}
