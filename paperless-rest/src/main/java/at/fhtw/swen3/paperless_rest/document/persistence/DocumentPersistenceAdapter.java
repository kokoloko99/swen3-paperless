package at.fhtw.swen3.paperless_rest.document.persistence;

import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import at.fhtw.swen3.paperless_rest.document.persistence.mapper.DocumentMapper;
import at.fhtw.swen3.paperless_rest.document.persistence.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DocumentPersistenceAdapter implements DocumentPersistence {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final Logger logger;

    @Override
    public DocumentModel save(DocumentModel document) {
        DocumentModel saved = documentMapper.toModel(documentRepository.save(documentMapper.toDal(document)));
        logger.debug("Persisted document {}", saved.getId());
        return saved;
    }

    @Override
    public Optional<DocumentModel> findById(UUID id) {
        Optional<DocumentModel> document = documentRepository.findById(id).map(documentMapper::toModel);
        logger.debug("Document {} found: {}", id, document.isPresent());
        return document;
    }

    @Override
    public boolean existsById(UUID id) {
        boolean exists = documentRepository.existsById(id);
        logger.debug("Document {} exists: {}", id, exists);
        return exists;
    }
}