package at.fhtw.swen3.paperless_rest.document.persistence;

import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;

import java.util.Optional;
import java.util.UUID;

public interface DocumentPersistence {

    DocumentModel save(DocumentModel document);

    Optional<DocumentModel> findById(UUID id);

    boolean existsById(UUID id);
}