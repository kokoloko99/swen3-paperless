package at.fhtw.swen3.paperless_rest.service;

import at.fhtw.swen3.paperless_rest.dto.DocumentDto;
import at.fhtw.swen3.paperless_rest.entity.Document;
import at.fhtw.swen3.paperless_rest.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentDto uploadDocument(MultipartFile file) {
        Document document = new Document();
        document.setFilename(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setSize(file.getSize());
        document.setUploadedAt(Instant.now());

        Document saved = documentRepository.save(document);
        return toDto(saved);
    }

    public DocumentDto getDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
        return toDto(document);
    }

    private DocumentDto toDto(Document document) {
        return new DocumentDto(
                document.getId(),
                document.getFilename(),
                document.getContentType(),
                document.getSize(),
                document.getUploadedAt()
        );
    }
}
