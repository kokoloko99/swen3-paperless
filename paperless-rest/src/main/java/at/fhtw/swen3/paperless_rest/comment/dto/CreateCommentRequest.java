package at.fhtw.swen3.paperless_rest.comment.dto;

// Enthält Autor und Text aus der Anfrage für einen neuen Kommentar.
public record CreateCommentRequest(
        String author,
        String text
) {
}
