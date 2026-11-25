package at.fhtw.swen3.paperless_rest.comment.validation;

import at.fhtw.swen3.paperless_rest.comment.exception.InvalidCommentException;
import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Prüft die Kommentarregeln direkt ohne simulierte Abhängigkeiten.
class CommentValidatorTest {

    private final CommentValidator validator = new CommentValidator(LoggerFactory.getLogger(CommentValidator.class));

    private CommentModel validComment() {
        CommentModel comment = new CommentModel();
        comment.setDocumentId(UUID.randomUUID());
        comment.setAuthor("Alice");
        comment.setText("Looks good");
        return comment;
    }

    // Prüft, dass gültige Kommentardaten akzeptiert werden.
    @Test
    void acceptsValidComment() {
        assertThatCode(() -> validator.validate(validComment())).doesNotThrowAnyException();
    }

    // Prüft, dass ein fehlendes Kommentarobjekt abgelehnt wird.
    @Test
    void rejectsNullComment() {
        assertThatThrownBy(() -> validator.validate(null)).isInstanceOf(InvalidCommentException.class);
    }

    // Prüft, dass eine fehlende Dokument-ID abgelehnt wird.
    @Test
    void rejectsMissingDocumentId() {
        CommentModel comment = validComment();
        comment.setDocumentId(null);
        assertThatThrownBy(() -> validator.validate(comment)).isInstanceOf(InvalidCommentException.class);
    }

    // Prüft, dass ein fehlender oder leerer Autor abgelehnt wird.
    @Test
    void rejectsMissingOrBlankAuthor() {
        CommentModel missingAuthor = validComment();
        missingAuthor.setAuthor(null);
        assertThatThrownBy(() -> validator.validate(missingAuthor)).isInstanceOf(InvalidCommentException.class);

        CommentModel blankAuthor = validComment();
        blankAuthor.setAuthor(" ");
        assertThatThrownBy(() -> validator.validate(blankAuthor)).isInstanceOf(InvalidCommentException.class);
    }

    // Prüft, dass ein fehlender oder leerer Text abgelehnt wird.
    @Test
    void rejectsMissingOrBlankText() {
        CommentModel missingText = validComment();
        missingText.setText(null);
        assertThatThrownBy(() -> validator.validate(missingText)).isInstanceOf(InvalidCommentException.class);

        CommentModel blankText = validComment();
        blankText.setText(" ");
        assertThatThrownBy(() -> validator.validate(blankText)).isInstanceOf(InvalidCommentException.class);
    }

    // Prüft, dass die Textgrenze von 1000 Zeichen eingehalten wird.
    @Test
    void rejectsTextLongerThanOneThousandCharacters() {
        CommentModel comment = validComment();
        comment.setText("x".repeat(1001));
        assertThatThrownBy(() -> validator.validate(comment)).isInstanceOf(InvalidCommentException.class);
    }
}
