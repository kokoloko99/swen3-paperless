package at.fhtw.swen3.paperless_rest.comment.service;

import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import at.fhtw.swen3.paperless_rest.document.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.comment.persistence.CommentPersistence;
import at.fhtw.swen3.paperless_rest.comment.validation.CommentValidator;
import at.fhtw.swen3.paperless_rest.document.persistence.DocumentPersistence;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentPersistence commentPersistence;
    private final DocumentPersistence documentPersistence;
    private final CommentValidator commentValidator;
    private final Logger logger;

    public CommentModel addComment(CommentModel comment) {
        commentValidator.validate(comment);

        if (!documentPersistence.existsById(comment.getDocumentId())) {
            logger.warn("Cannot add comment: document {} was not found", comment.getDocumentId());
            throw new DocumentNotFoundException(comment.getDocumentId());
        }

        comment.setCreatedAt(Instant.now());

        CommentModel saved = commentPersistence.save(comment);
        logger.info("Created comment {} for document {}", saved.getId(), saved.getDocumentId());
        return saved;
    }

    public List<CommentModel> getComments(UUID documentId) {
        if (!documentPersistence.existsById(documentId)) {
            logger.warn("Cannot get comments: document {} was not found", documentId);
            throw new DocumentNotFoundException(documentId);
        }

        List<CommentModel> comments = commentPersistence.findByDocumentId(documentId);
        logger.debug("Found {} comments for document {}", comments.size(), documentId);
        return comments;
    }

    public void deleteComment(UUID documentId, UUID commentId) {
        commentPersistence.findById(commentId)
                .filter(comment -> documentId.equals(comment.getDocumentId()))
                .ifPresentOrElse(comment -> {
                    commentPersistence.deleteById(commentId);
                    logger.info("Deleted comment {} from document {}", commentId, documentId);
                }, () -> logger.debug("Comment {} was not found for document {}", commentId, documentId));
    }
}
