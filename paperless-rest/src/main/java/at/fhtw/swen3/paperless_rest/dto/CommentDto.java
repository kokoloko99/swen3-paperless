package at.fhtw.swen3.paperless_rest.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID documentId,
        String author,
        String text,
        Instant createdAt
) {
}
