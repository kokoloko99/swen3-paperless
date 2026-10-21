package at.fhtw.swen3.paperless_rest.comment.persistence;

import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentPersistence {

    CommentModel save(CommentModel comment);

    Optional<CommentModel> findById(UUID id);

    List<CommentModel> findByDocumentId(UUID documentId);

    void deleteById(UUID id);
}