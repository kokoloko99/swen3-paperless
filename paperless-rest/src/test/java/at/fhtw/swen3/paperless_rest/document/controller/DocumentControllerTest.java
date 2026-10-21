package at.fhtw.swen3.paperless_rest.document.controller;

import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import at.fhtw.swen3.paperless_rest.document.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.document.exception.InvalidDocumentException;
import at.fhtw.swen3.paperless_rest.document.service.DocumentService;
import at.fhtw.swen3.paperless_rest.document.mapper.DocumentDtoMapperImpl;
import at.fhtw.swen3.paperless_rest.common.security.SecurityConfig;
import at.fhtw.swen3.paperless_rest.common.logging.LoggingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
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
@Import({DocumentDtoMapperImpl.class, SecurityConfig.class, LoggingConfiguration.class})
@WithMockUser
class DocumentControllerTest {

    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final UUID MISSING_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private DocumentService documentService;

    private static DocumentModel sampleDocument() {
        DocumentModel document = new DocumentModel();
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

        mockMvc.perform(multipart("/api/v1/documents").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(DOCUMENT_ID.toString()))
                .andExpect(jsonPath("$.filename").value("HelloWorld.pdf"));
    }

    @Test
    void uploadReturnsBadRequestWhenDocumentMetadataIsInvalid() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "application/pdf", new byte[0]);
        when(documentService.uploadDocument(any()))
                .thenThrow(new InvalidDocumentException("Document filename must not be blank"));

        mockMvc.perform(multipart("/api/v1/documents").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReturnsDocumentWhenFound() throws Exception {
        when(documentService.getDocument(eq(DOCUMENT_ID))).thenReturn(sampleDocument());

        mockMvc.perform(get("/api/v1/documents/{id}", DOCUMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("HelloWorld.pdf"));
    }

    @Test
    void getReturnsNotFoundWhenMissing() throws Exception {
        when(documentService.getDocument(eq(MISSING_ID))).thenThrow(new DocumentNotFoundException(MISSING_ID));

        mockMvc.perform(get("/api/v1/documents/{id}", MISSING_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReturnsBadRequestWhenIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/documents/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/documents/{id}", DOCUMENT_ID))
                .andExpect(status().isUnauthorized());
    }
}