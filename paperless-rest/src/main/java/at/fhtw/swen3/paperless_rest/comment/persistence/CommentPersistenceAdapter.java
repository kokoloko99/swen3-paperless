package at.fhtw.swen3.paperless_rest.comment.persistence;

import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import at.fhtw.swen3.paperless_rest.comment.persistence.mapper.CommentMapper;
import at.fhtw.swen3.paperless_rest.comment.persistence.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataAccessException;
import at.fhtw.swen3.paperless_rest.common.persistence.PersistenceOperationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentPersistence {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final Logger logger;

    @Override
    public CommentModel save(CommentModel comment) {
        try {
            CommentModel saved = commentMapper.toModel(commentRepository.save(commentMapper.toDal(comment)));
            logger.debug("Persisted comment {} for document {}", saved.getId(), saved.getDocumentId());
            return saved;
        } catch (DataAccessException ex) {
            throw new PersistenceOperationException("Could not save comment", ex);
        }
    }

    @Override
    public Optional<CommentModel> findById(UUID id) {
        try {
            Optional<CommentModel> comment = commentRepository.findById(id).map(commentMapper::toModel);
            logger.debug("Comment {} found: {}", id, comment.isPresent());
            return comment;
        } catch (DataAccessException ex) {
            throw new PersistenceOperationException("Could not load comment", ex);
        }
    }

    @Override
    public List<CommentModel> findByDocumentId(UUID documentId) {
        try {
            List<CommentModel> comments = commentRepository.findByDocument_Id(documentId).stream()
                    .map(commentMapper::toModel)
                    .toList();
            logger.debug("Loaded {} comments for document {}", comments.size(), documentId);
            return comments;
        } catch (DataAccessException ex) {
            throw new PersistenceOperationException("Could not load comments", ex);
        }
    }

    @Override
    public void deleteById(UUID id) {
        try {
            commentRepository.deleteById(id);
            logger.debug("Deleted comment {} from persistence", id);
        } catch (DataAccessException ex) {
            throw new PersistenceOperationException("Could not delete comment", ex);
        }
    }
}
