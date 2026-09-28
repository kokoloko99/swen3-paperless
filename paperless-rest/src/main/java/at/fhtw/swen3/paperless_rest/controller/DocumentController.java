package at.fhtw.swen3.paperless_rest.controller;

import at.fhtw.swen3.paperless_rest.models.DocumentBl;
import at.fhtw.swen3.paperless_rest.dto.DocumentDto;
import at.fhtw.swen3.paperless_rest.service.DocumentService;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> upload(@RequestParam("file") MultipartFile file) {
        DocumentBl document = new DocumentBl();
        document.setFilename(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setSize(file.getSize());

        DocumentBl saved = documentService.uploadDocument(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(toDto(documentService.getDocument(id)));
    }

    private DocumentDto toDto(DocumentBl document) {
        return new DocumentDto(
                document.getId(),
                document.getFilename(),
                document.getContentType(),
                document.getSize(),
                document.getUploadedAt()
        );
    }
}
