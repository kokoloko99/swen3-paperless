package at.fhtw.swen3.paperless_rest.comment.exception;

public class InvalidCommentException extends RuntimeException {

    public InvalidCommentException(String message) {
        super(message);
    }
}
