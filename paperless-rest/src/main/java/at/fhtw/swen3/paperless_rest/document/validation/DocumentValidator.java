package at.fhtw.swen3.paperless_rest.document.validation;

import at.fhtw.swen3.paperless_rest.document.exception.InvalidDocumentException;
import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentValidator {

    private final Logger logger;

    public void validate(DocumentModel document) {
        if (document == null) {
            reject("Document must not be null");
        }
        if (document.getFilename() == null || document.getFilename().isBlank()) {
            reject("Document filename must not be blank");
        }
        if (document.getContentType() == null || document.getContentType().isBlank()) {
            reject("Document content type must not be blank");
        }
        if (document.getSize() <= 0) {
            reject("Document size must be greater than zero");
        }
    }

    private void reject(String message) {
        logger.warn("Document validation failed: {}", message);
        throw new InvalidDocumentException(message);
    }
}
