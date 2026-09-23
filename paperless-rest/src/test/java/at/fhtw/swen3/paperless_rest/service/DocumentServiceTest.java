package at.fhtw.swen3.paperless_rest.service;

import at.fhtw.swen3.paperless_rest.entity.Document;
import at.fhtw.swen3.paperless_rest.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.repository.DocumentRepository;
import at.fhtw.swen3.paperless_rest.dto.DocumentDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    private DocumentService documentService;

    @Test
    void uploadDocumentPersistsMetadataFromFile() {
        documentService = new DocumentService(documentRepository);
        MockMultipartFile file = new MockMultipartFile(
                "file", "HelloWorld.pdf", "application/pdf", "content".getBytes());

        Document saved = new Document();
        saved.setId(1L);
        saved.setFilename("HelloWorld.pdf");
        saved.setContentType("application/pdf");
        saved.setSize(file.getSize());
        when(documentRepository.save(org.mockito.ArgumentMatchers.any(Document.class))).thenReturn(saved);

        DocumentDto dto = documentService.uploadDocument(file);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());
        Document toBeSaved = captor.getValue();

        assertThat(toBeSaved.getFilename()).isEqualTo("HelloWorld.pdf");
        assertThat(toBeSaved.getContentType()).isEqualTo("application/pdf");
        assertThat(toBeSaved.getUploadedAt()).isNotNull();

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.filename()).isEqualTo("HelloWorld.pdf");
    }

    @Test
    void getDocumentReturnsDtoWhenFound() {
        documentService = new DocumentService(documentRepository);
        Document document = new Document();
        document.setId(42L);
        document.setFilename("Existing.pdf");
        document.setContentType("application/pdf");
        document.setSize(10L);
        when(documentRepository.findById(42L)).thenReturn(Optional.of(document));

        DocumentDto dto = documentService.getDocument(42L);

        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.filename()).isEqualTo("Existing.pdf");
    }

    @Test
    void getDocumentThrowsWhenNotFound() {
        documentService = new DocumentService(documentRepository);
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocument(99L))
                .isInstanceOf(DocumentNotFoundException.class);
    }
}
