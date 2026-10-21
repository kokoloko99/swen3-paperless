package at.fhtw.swen3.paperless_rest.document.validation;

import at.fhtw.swen3.paperless_rest.document.exception.InvalidDocumentException;
import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentValidatorTest {

    private final DocumentValidator validator = new DocumentValidator(LoggerFactory.getLogger(DocumentValidator.class));

    private DocumentModel validDocument() {
        DocumentModel document = new DocumentModel();
        document.setFilename("paper.pdf");
        document.setContentType("application/pdf");
        document.setSize(1L);
        return document;
    }

    @Test
    void acceptsValidDocument() {
        assertThatCode(() -> validator.validate(validDocument())).doesNotThrowAnyException();
    }

    @Test
    void rejectsNullDocument() {
        assertThatThrownBy(() -> validator.validate(null)).isInstanceOf(InvalidDocumentException.class);
    }

    @Test
    void rejectsMissingOrBlankFilename() {
        DocumentModel missingFilename = validDocument();
        missingFilename.setFilename(null);
        assertThatThrownBy(() -> validator.validate(missingFilename)).isInstanceOf(InvalidDocumentException.class);

        DocumentModel blankFilename = validDocument();
        blankFilename.setFilename(" ");
        assertThatThrownBy(() -> validator.validate(blankFilename)).isInstanceOf(InvalidDocumentException.class);
    }

    @Test
    void rejectsMissingOrBlankContentType() {
        DocumentModel missingType = validDocument();
        missingType.setContentType(null);
        assertThatThrownBy(() -> validator.validate(missingType)).isInstanceOf(InvalidDocumentException.class);

        DocumentModel blankType = validDocument();
        blankType.setContentType(" ");
        assertThatThrownBy(() -> validator.validate(blankType)).isInstanceOf(InvalidDocumentException.class);
    }

    @Test
    void rejectsZeroOrNegativeFileSize() {
        DocumentModel empty = validDocument();
        empty.setSize(0L);
        assertThatThrownBy(() -> validator.validate(empty)).isInstanceOf(InvalidDocumentException.class);

        DocumentModel negative = validDocument();
        negative.setSize(-1L);
        assertThatThrownBy(() -> validator.validate(negative)).isInstanceOf(InvalidDocumentException.class);
    }
}
