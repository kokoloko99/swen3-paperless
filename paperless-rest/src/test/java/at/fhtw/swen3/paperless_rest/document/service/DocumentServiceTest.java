package at.fhtw.swen3.paperless_rest.document.service;

import at.fhtw.swen3.paperless_rest.document.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import at.fhtw.swen3.paperless_rest.document.persistence.DocumentPersistence;
import at.fhtw.swen3.paperless_rest.document.exception.InvalidDocumentException;
import at.fhtw.swen3.paperless_rest.document.validation.DocumentValidator;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.BeforeEach;
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
    private DocumentPersistence documentPersistence;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(
                documentPersistence,
                new DocumentValidator(LoggerFactory.getLogger(DocumentValidator.class)),
                LoggerFactory.getLogger(DocumentService.class));
    }

    @Test
    void uploadDocumentSetsUploadTimeAndPersists() {
        DocumentModel toUpload = new DocumentModel();
        toUpload.setFilename("HelloWorld.pdf");
        toUpload.setContentType("application/pdf");
        toUpload.setSize(7L);

        UUID savedId = UUID.randomUUID();
        when(documentPersistence.save(any(DocumentModel.class))).thenAnswer(invocation -> {
            DocumentModel model = invocation.getArgument(0);
            model.setId(savedId);
            return model;
        });

        DocumentModel result = documentService.uploadDocument(toUpload);

        ArgumentCaptor<DocumentModel> captor = ArgumentCaptor.forClass(DocumentModel.class);
        verify(documentPersistence).save(captor.capture());
        assertThat(captor.getValue().getFilename()).isEqualTo("HelloWorld.pdf");
        assertThat(captor.getValue().getUploadedAt()).isNotNull();
        assertThat(result.getId()).isEqualTo(savedId);
    }

    @Test
    void uploadDocumentRejectsInvalidMetadata() {
        DocumentModel invalid = new DocumentModel();
        invalid.setFilename(" ");
        invalid.setContentType("application/pdf");
        invalid.setSize(1L);

        assertThatThrownBy(() -> documentService.uploadDocument(invalid))
                .isInstanceOf(InvalidDocumentException.class);
    }

    @Test
    void getDocumentReturnsModelWhenFound() {
        UUID id = UUID.randomUUID();
        DocumentModel document = new DocumentModel();
        document.setId(id);
        document.setFilename("Existing.pdf");
        when(documentPersistence.findById(id)).thenReturn(Optional.of(document));

        DocumentModel result = documentService.getDocument(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getFilename()).isEqualTo("Existing.pdf");
    }

    @Test
    void getDocumentThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(documentPersistence.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocument(id))
                .isInstanceOf(DocumentNotFoundException.class);
    }
}