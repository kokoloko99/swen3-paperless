package at.fhtw.swen3.paperless_rest.service;

import at.fhtw.swen3.paperless_rest.models.DocumentBl;
import at.fhtw.swen3.paperless_rest.entity.Document;
import at.fhtw.swen3.paperless_rest.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.mapper.DocumentMapper;
import at.fhtw.swen3.paperless_rest.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    private final DocumentMapper documentMapper = new DocumentMapper();

    private DocumentService documentService;

    @Test
    void uploadDocumentPersistsMetadataFromBlEntity() {
        documentService = new DocumentServiceImpl(documentRepository, documentMapper);

        DocumentBl toUpload = new DocumentBl();
        toUpload.setFilename("HelloWorld.pdf");
        toUpload.setContentType("application/pdf");
        toUpload.setSize(7L);

        UUID savedId = UUID.randomUUID();
        Document saved = new Document();
        saved.setId(savedId);
        saved.setFilename("HelloWorld.pdf");
        saved.setContentType("application/pdf");
        saved.setSize(7L);
        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        DocumentBl result = documentService.uploadDocument(toUpload);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());
        Document toBeSaved = captor.getValue();

        assertThat(toBeSaved.getFilename()).isEqualTo("HelloWorld.pdf");
        assertThat(toBeSaved.getContentType()).isEqualTo("application/pdf");
        assertThat(toUpload.getUploadedAt()).isNotNull();

        assertThat(result.getId()).isEqualTo(savedId);
        assertThat(result.getFilename()).isEqualTo("HelloWorld.pdf");
    }

    @Test
    void getDocumentReturnsBlEntityWhenFound() {
        documentService = new DocumentServiceImpl(documentRepository, documentMapper);
        UUID id = UUID.randomUUID();
        Document document = new Document();
        document.setId(id);
        document.setFilename("Existing.pdf");
        document.setContentType("application/pdf");
        document.setSize(10L);
        when(documentRepository.findById(id)).thenReturn(Optional.of(document));

        DocumentBl result = documentService.getDocument(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getFilename()).isEqualTo("Existing.pdf");
    }

    @Test
    void getDocumentThrowsWhenNotFound() {
        documentService = new DocumentServiceImpl(documentRepository, documentMapper);
        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocument(id))
                .isInstanceOf(DocumentNotFoundException.class);
    }
}
