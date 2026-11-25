package at.fhtw.swen3.paperless_rest.comment.exception;

// Meldet, dass die Daten eines Kommentars ungültig sind.
public class InvalidCommentException extends RuntimeException {

    public InvalidCommentException(String message) {
        super(message);
    }
}
