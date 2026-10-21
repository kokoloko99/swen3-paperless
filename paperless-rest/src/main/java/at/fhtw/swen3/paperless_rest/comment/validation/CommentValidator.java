package at.fhtw.swen3.paperless_rest.comment.validation;

import at.fhtw.swen3.paperless_rest.comment.exception.InvalidCommentException;
import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentValidator {

    private static final int MAX_TEXT_LENGTH = 1000;
    private final Logger logger;

    public void validate(CommentModel comment) {
        if (comment == null) {
            reject("Comment must not be null");
        }
        if (comment.getDocumentId() == null) {
            reject("Comment document ID must not be null");
        }
        if (comment.getAuthor() == null || comment.getAuthor().isBlank()) {
            reject("Comment author must not be blank");
        }
        if (comment.getText() == null || comment.getText().isBlank()) {
            reject("Comment text must not be blank");
        }
        if (comment.getText().length() > MAX_TEXT_LENGTH) {
            reject("Comment text must not exceed " + MAX_TEXT_LENGTH + " characters");
        }
    }

    private void reject(String message) {
        logger.warn("Comment validation failed: {}", message);
        throw new InvalidCommentException(message);
    }
}
