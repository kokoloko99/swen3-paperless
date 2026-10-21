package at.fhtw.swen3.paperless_rest.comment.service;

import at.fhtw.swen3.paperless_rest.comment.exception.InvalidCommentException;
import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import at.fhtw.swen3.paperless_rest.comment.persistence.CommentPersistence;
import at.fhtw.swen3.paperless_rest.comment.validation.CommentValidator;
import org.slf4j.LoggerFactory;
import at.fhtw.swen3.paperless_rest.document.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.document.persistence.DocumentPersistence;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final UUID OTHER_DOCUMENT_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();

    @Mock
    private CommentPersistence commentPersistence;

    @Mock
    private DocumentPersistence documentPersistence;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(
                commentPersistence,
                documentPersistence,
                new CommentValidator(LoggerFactory.getLogger(CommentValidator.class)),
                LoggerFactory.getLogger(CommentService.class));
    }

    private CommentModel validComment() {
        CommentModel comment = new CommentModel();
        comment.setDocumentId(DOCUMENT_ID);
        comment.setAuthor("Alice");
        comment.setText("Looks good");
        return comment;
    }

    private CommentModel storedComment(UUID documentId) {
        CommentModel comment = new CommentModel();
        comment.setId(COMMENT_ID);
        comment.setDocumentId(documentId);
        comment.setAuthor("Alice");
        comment.setText("Looks good");
        return comment;
    }

    @Test
    void addCommentPersistsWhenDocumentExists() {
        when(documentPersistence.existsById(DOCUMENT_ID)).thenReturn(true);
        when(commentPersistence.save(any(CommentModel.class))).thenAnswer(invocation -> {
            CommentModel comment = invocation.getArgument(0);
            comment.setId(COMMENT_ID);
            return comment;
        });

        CommentModel result = commentService.addComment(validComment());

        assertThat(result.getId()).isEqualTo(COMMENT_ID);
        assertThat(result.getDocumentId()).isEqualTo(DOCUMENT_ID);
        assertThat(result.getAuthor()).isEqualTo("Alice");
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void addCommentThrowsWhenDocumentMissing() {
        when(documentPersistence.existsById(DOCUMENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> commentService.addComment(validComment()))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void addCommentThrowsWhenTextBlank() {
        CommentModel comment = validComment();
        comment.setText("  ");

        assertThatThrownBy(() -> commentService.addComment(comment))
                .isInstanceOf(InvalidCommentException.class);
    }

    @Test
    void addCommentThrowsWhenAuthorBlank() {
        CommentModel comment = validComment();
        comment.setAuthor("");

        assertThatThrownBy(() -> commentService.addComment(comment))
                .isInstanceOf(InvalidCommentException.class);
    }

    @Test
    void addCommentThrowsWhenTextTooLong() {
        CommentModel comment = validComment();
        comment.setText("x".repeat(1001));

        assertThatThrownBy(() -> commentService.addComment(comment))
                .isInstanceOf(InvalidCommentException.class);
    }

    @Test
    void getCommentsReturnsCommentsWhenDocumentExists() {
        when(documentPersistence.existsById(DOCUMENT_ID)).thenReturn(true);
        when(commentPersistence.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of(storedComment(DOCUMENT_ID)));

        List<CommentModel> result = commentService.getComments(DOCUMENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("Alice");
    }

    @Test
    void getCommentsThrowsWhenDocumentMissing() {
        when(documentPersistence.existsById(DOCUMENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> commentService.getComments(DOCUMENT_ID))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void deleteCommentRemovesWhenBelongsToDocument() {
        when(commentPersistence.findById(COMMENT_ID)).thenReturn(Optional.of(storedComment(DOCUMENT_ID)));

        commentService.deleteComment(DOCUMENT_ID, COMMENT_ID);

        verify(commentPersistence).deleteById(COMMENT_ID);
    }

    @Test
    void deleteCommentDoesNothingWhenCommentMissing() {
        when(commentPersistence.findById(COMMENT_ID)).thenReturn(Optional.empty());

        commentService.deleteComment(DOCUMENT_ID, COMMENT_ID);

        verify(commentPersistence, never()).deleteById(COMMENT_ID);
    }

    @Test
    void deleteCommentDoesNothingWhenCommentBelongsToDifferentDocument() {
        when(commentPersistence.findById(COMMENT_ID)).thenReturn(Optional.of(storedComment(OTHER_DOCUMENT_ID)));

        commentService.deleteComment(DOCUMENT_ID, COMMENT_ID);

        verify(commentPersistence, never()).deleteById(COMMENT_ID);
    }
}
