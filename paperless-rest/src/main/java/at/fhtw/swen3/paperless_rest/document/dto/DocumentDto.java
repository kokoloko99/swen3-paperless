package at.fhtw.swen3.paperless_rest.document.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentDto(
        UUID id,
        String filename,
        String contentType,
        long size,
        Instant uploadedAt
) {
}
