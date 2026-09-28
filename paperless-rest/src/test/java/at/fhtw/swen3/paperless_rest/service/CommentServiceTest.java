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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final UUID OTHER_DOCUMENT_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private DocumentRepository documentRepository;

    private final CommentMapper commentMapper = new CommentMapper();

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(commentRepository, documentRepository, commentMapper);
    }

    private CommentBl validComment() {
        CommentBl comment = new CommentBl();
        comment.setDocumentId(DOCUMENT_ID);
        comment.setAuthor("Alice");
        comment.setText("Looks good");
        return comment;
    }

    @Test
    void addCommentPersistsWhenDocumentExists() {
        Document document = new Document();
        document.setId(DOCUMENT_ID);
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(COMMENT_ID);
            return comment;
        });

        CommentBl result = commentService.addComment(validComment());

        assertThat(result.getId()).isEqualTo(COMMENT_ID);
        assertThat(result.getDocumentId()).isEqualTo(DOCUMENT_ID);
        assertThat(result.getAuthor()).isEqualTo("Alice");
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void addCommentThrowsWhenDocumentMissing() {
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(validComment()))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void addCommentThrowsWhenTextBlank() {
        CommentBl comment = validComment();
        comment.setText("  ");

        assertThatThrownBy(() -> commentService.addComment(comment))
                .isInstanceOf(InvalidCommentException.class);
    }

    @Test
    void addCommentThrowsWhenAuthorBlank() {
        CommentBl comment = validComment();
        comment.setAuthor("");

        assertThatThrownBy(() -> commentService.addComment(comment))
                .isInstanceOf(InvalidCommentException.class);
    }

    @Test
    void addCommentThrowsWhenTextTooLong() {
        CommentBl comment = validComment();
        comment.setText("x".repeat(1001));

        assertThatThrownBy(() -> commentService.addComment(comment))
                .isInstanceOf(InvalidCommentException.class);
    }

    @Test
    void getCommentsReturnsMappedListWhenDocumentExists() {
        when(documentRepository.existsById(DOCUMENT_ID)).thenReturn(true);
        Document document = new Document();
        document.setId(DOCUMENT_ID);
        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setDocument(document);
        comment.setAuthor("Alice");
        comment.setText("Looks good");
        when(commentRepository.findByDocument_Id(DOCUMENT_ID)).thenReturn(List.of(comment));

        List<CommentBl> result = commentService.getComments(DOCUMENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("Alice");
    }

    @Test
    void getCommentsThrowsWhenDocumentMissing() {
        when(documentRepository.existsById(DOCUMENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> commentService.getComments(DOCUMENT_ID))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void deleteCommentRemovesWhenBelongsToDocument() {
        Document document = new Document();
        document.setId(DOCUMENT_ID);
        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setDocument(document);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

        commentService.deleteComment(DOCUMENT_ID, COMMENT_ID);
    }

    @Test
    void deleteCommentThrowsWhenCommentMissing() {
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(DOCUMENT_ID, COMMENT_ID))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void deleteCommentThrowsWhenCommentBelongsToDifferentDocument() {
        Document document = new Document();
        document.setId(OTHER_DOCUMENT_ID);
        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setDocument(document);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(DOCUMENT_ID, COMMENT_ID))
                .isInstanceOf(CommentNotFoundException.class);
    }
}
