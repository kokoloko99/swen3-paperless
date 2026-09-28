package at.fhtw.swen3.paperless_rest.service;

import at.fhtw.swen3.paperless_rest.models.DocumentBl;
import at.fhtw.swen3.paperless_rest.entity.Document;
import at.fhtw.swen3.paperless_rest.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.mapper.DocumentMapper;
import at.fhtw.swen3.paperless_rest.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    @Override
    public DocumentBl uploadDocument(DocumentBl document) {
        document.setUploadedAt(Instant.now());

        Document dal = documentMapper.toDal(document);
        Document saved = documentRepository.save(dal);
        return documentMapper.toBl(saved);
    }

    @Override
    public DocumentBl getDocument(UUID id) {
        Document dal = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
        return documentMapper.toBl(dal);
    }
}
