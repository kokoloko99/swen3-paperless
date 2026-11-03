package at.fhtw.swen3.paperless_rest.document.service;

import at.fhtw.swen3.paperless_rest.document.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.document.exception.OcrSubmissionException;
import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import at.fhtw.swen3.paperless_rest.document.persistence.DocumentPersistence;
import at.fhtw.swen3.paperless_rest.document.validation.DocumentValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentPersistence documentPersistence;
    private final DocumentValidator documentValidator;
    private final OcrPublisher ocrPublisher;
    private final Logger logger;

    @Transactional
    public DocumentModel uploadDocument(DocumentModel document, byte[] content) {
        documentValidator.validate(document);
        document.setUploadedAt(Instant.now());
        DocumentModel saved = documentPersistence.save(document);
        logger.debug("Document {} metadata saved in upload transaction; submitting for OCR", saved.getId());
        try {
            ocrPublisher.publish(saved, content);
        } catch (OcrPublishException ex) {
            throw new OcrSubmissionException(ex);
        }
        logger.debug("Document {} submitted for OCR; upload transaction is ready to commit", saved.getId());
        return saved;
    }

    public DocumentModel getDocument(UUID id) {
        return documentPersistence.findById(id).orElseThrow(() -> {
            logger.warn("Document {} was not found", id);
            return new DocumentNotFoundException(id);
        });
    }
}
