package at.fhtw.swen3.paperless_rest.service;

import at.fhtw.swen3.paperless_rest.models.CommentBl;
import at.fhtw.swen3.paperless_rest.entity.Comment;
import at.fhtw.swen3.paperless_rest.entity.Document;
import at.fhtw.swen3.paperless_rest.exception.CommentNotFoundException;
import at.fhtw.swen3.paperless_rest.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.exception.InvalidCommentException;
import at.fhtw.swen3.paperless_rest.mapper.CommentMapper;
import at.fhtw.swen3.paperless_rest.repository.CommentRepository;
import at.fhtw.swen3.paperless_rest.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final int MAX_TEXT_LENGTH = 1000;

    private final CommentRepository commentRepository;
    private final DocumentRepository documentRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentBl addComment(CommentBl comment) {
        validate(comment);

        Document document = documentRepository.findById(comment.getDocumentId())
                .orElseThrow(() -> new DocumentNotFoundException(comment.getDocumentId()));

        comment.setCreatedAt(Instant.now());

        Comment dal = commentMapper.toDal(comment, document);
        Comment saved = commentRepository.save(dal);
        return commentMapper.toBl(saved);
    }

    @Override
    public List<CommentBl> getComments(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new DocumentNotFoundException(documentId);
        }

        return commentRepository.findByDocument_Id(documentId).stream()
                .map(commentMapper::toBl)
                .toList();
    }

    @Override
    public void deleteComment(UUID documentId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getDocument().getId().equals(documentId)) {
            throw new CommentNotFoundException(commentId);
        }

        commentRepository.delete(comment);
    }

    private void validate(CommentBl comment) {
        if (comment.getAuthor() == null || comment.getAuthor().isBlank()) {
            throw new InvalidCommentException("Comment author must not be blank");
        }
        if (comment.getText() == null || comment.getText().isBlank()) {
            throw new InvalidCommentException("Comment text must not be blank");
        }
        if (comment.getText().length() > MAX_TEXT_LENGTH) {
            throw new InvalidCommentException("Comment text must not exceed " + MAX_TEXT_LENGTH + " characters");
        }
    }
}
