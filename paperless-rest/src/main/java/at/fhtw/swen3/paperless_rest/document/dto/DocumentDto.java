package at.fhtw.swen3.paperless_rest.document.dto;

import java.time.Instant;
import java.util.UUID;

// Enthält die Dokumentdaten für die Antwort an den Browser.
public record DocumentDto(
        UUID id,
        String filename,
        String contentType,
        long size,
        Instant uploadedAt
) {
}
