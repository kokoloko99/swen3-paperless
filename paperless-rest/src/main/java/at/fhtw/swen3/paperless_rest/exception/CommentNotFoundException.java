package at.fhtw.swen3.paperless_rest.exception;

import java.util.UUID;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(UUID id) {
        super("Comment not found: " + id);
    }
}
