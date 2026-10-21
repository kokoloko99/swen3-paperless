package at.fhtw.swen3.paperless_rest.document.service;

import at.fhtw.swen3.paperless_rest.document.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import at.fhtw.swen3.paperless_rest.document.persistence.DocumentPersistence;
import at.fhtw.swen3.paperless_rest.document.validation.DocumentValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentPersistence documentPersistence;
    private final DocumentValidator documentValidator;
    private final Logger logger;

    public DocumentModel uploadDocument(DocumentModel document) {
        documentValidator.validate(document);
        document.setUploadedAt(Instant.now());
        DocumentModel saved = documentPersistence.save(document);
        logger.info("Uploaded document {} ({})", saved.getId(), saved.getFilename());
        return saved;
    }

    public DocumentModel getDocument(UUID id) {
        return documentPersistence.findById(id).orElseThrow(() -> {
            logger.warn("Document {} was not found", id);
            return new DocumentNotFoundException(id);
        });
    }
}