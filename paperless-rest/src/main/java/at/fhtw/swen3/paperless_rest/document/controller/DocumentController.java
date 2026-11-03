package at.fhtw.swen3.paperless_rest.document.controller;

import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import at.fhtw.swen3.paperless_rest.document.dto.DocumentDto;
import at.fhtw.swen3.paperless_rest.document.mapper.DocumentDtoMapper;
import at.fhtw.swen3.paperless_rest.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentDtoMapper documentDtoMapper;
    private final Logger logger;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> upload(@RequestParam("file") MultipartFile file) {
        logger.info("Document upload received: {} ({} bytes, type {})",
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException ex) {
            throw new DocumentReadException(ex);
        }
        DocumentModel saved = documentService.uploadDocument(documentDtoMapper.toModel(file), content);
        // The transactional service proxy has committed before returning here.
        logger.info("Document upload completed: document {} ({}) stored and submitted for OCR",
                saved.getId(), saved.getFilename());
        return ResponseEntity.status(HttpStatus.CREATED).body(documentDtoMapper.toDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> get(@PathVariable UUID id) {
        logger.debug("Getting document {}", id);
        return ResponseEntity.ok(documentDtoMapper.toDto(documentService.getDocument(id)));
    }
}
