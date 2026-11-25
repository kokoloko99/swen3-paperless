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

// Prüft den Upload-Ablauf mit simulierter Persistenz und simuliertem OCR-Versand.
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentPersistence documentPersistence;

    @Mock
    private OcrPublisher ocrPublisher;

    private DocumentService documentService;

    // Prüft, dass das Löschen an die Persistenz geht und keinen OCR-Auftrag auslöst.
    @Test
    void deleteDocumentDelegatesToPersistence() {
        UUID id = UUID.randomUUID();
        documentService.deleteDocument(id);
        verify(documentPersistence).deleteById(id);
        org.mockito.Mockito.verifyNoInteractions(ocrPublisher);
    }

    // Erstellt den Service vor jedem Test mit kontrollierten Ersatz-Abhängigkeiten.
    @BeforeEach
    void setUp() {
        documentService = new DocumentService(
                documentPersistence,
                new DocumentValidator(LoggerFactory.getLogger(DocumentValidator.class)),
                ocrPublisher,
                LoggerFactory.getLogger(DocumentService.class));
    }

    // Prüft Zeitstempel, Speicherung und Übergabe des Dateiinhalts an den Publisher.
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

        DocumentModel result = documentService.uploadDocument(toUpload, new byte[]{1, 2, 3});

        ArgumentCaptor<DocumentModel> captor = ArgumentCaptor.forClass(DocumentModel.class);
        verify(documentPersistence).save(captor.capture());
        assertThat(captor.getValue().getFilename()).isEqualTo("HelloWorld.pdf");
        assertThat(captor.getValue().getUploadedAt()).isNotNull();
        assertThat(result.getId()).isEqualTo(savedId);
        verify(ocrPublisher).publish(result, new byte[]{1, 2, 3});
    }

    // Prüft, dass ungültige Angaben weder gespeichert noch versendet werden.
    @Test
    void uploadDocumentRejectsInvalidMetadata() {
        DocumentModel invalid = new DocumentModel();
        invalid.setFilename(" ");
        invalid.setContentType("application/pdf");
        invalid.setSize(1L);

        assertThatThrownBy(() -> documentService.uploadDocument(invalid, new byte[0]))
                .isInstanceOf(InvalidDocumentException.class);
        org.mockito.Mockito.verifyNoInteractions(ocrPublisher, documentPersistence);
    }

    // Prüft, dass ein Versandfehler als OcrSubmissionException weitergegeben wird.
    @Test
    void uploadFailsWhenOcrPublishingFails() {
        DocumentModel document = new DocumentModel();
        document.setFilename("scan.pdf");
        document.setContentType("application/pdf");
        document.setSize(1);
        when(documentPersistence.save(any())).thenReturn(document);
        OcrPublishException failure = new OcrPublishException("Publisher unavailable", new RuntimeException("connection refused"));
        org.mockito.Mockito.doThrow(failure)
                .when(ocrPublisher).publish(any(), any(byte[].class));

        assertThatThrownBy(() -> documentService.uploadDocument(document, new byte[]{1}))
                .isInstanceOf(at.fhtw.swen3.paperless_rest.document.exception.OcrSubmissionException.class)
                .hasCause(failure);
    }

    // Prüft, dass ein vorhandenes Dokument zurückgegeben wird.
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

    // Prüft die Exception für ein fehlendes Dokument.
    @Test
    void getDocumentThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(documentPersistence.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocument(id))
                .isInstanceOf(DocumentNotFoundException.class);
    }
}
