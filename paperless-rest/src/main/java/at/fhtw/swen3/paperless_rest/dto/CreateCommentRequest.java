package at.fhtw.swen3.paperless_rest.dto;

public record CreateCommentRequest(
        String author,
        String text
) {
}
