package at.fhtw.swen3.paperless_rest.dto;

import java.time.Instant;

public record DocumentDto(
        Long id,
        String filename,
        String contentType,
        long size,
        Instant uploadedAt
) {
}
