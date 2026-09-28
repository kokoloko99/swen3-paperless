package at.fhtw.swen3.paperless_rest.controller;

import at.fhtw.swen3.paperless_rest.models.DocumentBl;
import at.fhtw.swen3.paperless_rest.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final UUID MISSING_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    private static DocumentBl sampleDocument() {
        DocumentBl document = new DocumentBl();
        document.setId(DOCUMENT_ID);
        document.setFilename("HelloWorld.pdf");
        document.setContentType("application/pdf");
        document.setSize(7L);
        document.setUploadedAt(Instant.parse("2026-01-01T10:00:00Z"));
        return document;
    }

    @Test
    void uploadReturnsCreatedWithDocumentMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "HelloWorld.pdf", "application/pdf", "content".getBytes());
        when(documentService.uploadDocument(any())).thenReturn(sampleDocument());

        mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(DOCUMENT_ID.toString()))
                .andExpect(jsonPath("$.filename").value("HelloWorld.pdf"));
    }

    @Test
    void getReturnsDocumentWhenFound() throws Exception {
        when(documentService.getDocument(eq(DOCUMENT_ID))).thenReturn(sampleDocument());

        mockMvc.perform(get("/api/documents/{id}", DOCUMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("HelloWorld.pdf"));
    }

    @Test
    void getReturnsNotFoundWhenMissing() throws Exception {
        when(documentService.getDocument(eq(MISSING_ID))).thenThrow(new DocumentNotFoundException(MISSING_ID));

        mockMvc.perform(get("/api/documents/{id}", MISSING_ID))
                .andExpect(status().isNotFound());
    }
}
