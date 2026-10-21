package at.fhtw.swen3.paperless_rest.comment.persistence;

import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import at.fhtw.swen3.paperless_rest.comment.persistence.mapper.CommentMapper;
import at.fhtw.swen3.paperless_rest.comment.persistence.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

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
        CommentModel saved = commentMapper.toModel(commentRepository.save(commentMapper.toDal(comment)));
        logger.debug("Persisted comment {} for document {}", saved.getId(), saved.getDocumentId());
        return saved;
    }

    @Override
    public Optional<CommentModel> findById(UUID id) {
        Optional<CommentModel> comment = commentRepository.findById(id).map(commentMapper::toModel);
        logger.debug("Comment {} found: {}", id, comment.isPresent());
        return comment;
    }

    @Override
    public List<CommentModel> findByDocumentId(UUID documentId) {
        List<CommentModel> comments = commentRepository.findByDocument_Id(documentId).stream()
                .map(commentMapper::toModel)
                .toList();
        logger.debug("Loaded {} comments for document {}", comments.size(), documentId);
        return comments;
    }

    @Override
    public void deleteById(UUID id) {
        commentRepository.deleteById(id);
        logger.debug("Deleted comment {} from persistence", id);
    }
}