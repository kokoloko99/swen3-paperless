package at.fhtw.swen3.paperless_rest.comment.dto;

public record CreateCommentRequest(
        String author,
        String text
) {
}
