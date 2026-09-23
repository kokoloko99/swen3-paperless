package at.fhtw.swen3.paperless_rest.controller;

import at.fhtw.swen3.paperless_rest.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.dto.DocumentDto;
import at.fhtw.swen3.paperless_rest.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Test
    void uploadReturnsCreatedWithDocumentMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "HelloWorld.pdf", "application/pdf", "content".getBytes());
        DocumentDto dto = new DocumentDto(1L, "HelloWorld.pdf", "application/pdf", 7L, Instant.parse("2026-01-01T10:00:00Z"));
        when(documentService.uploadDocument(any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.filename").value("HelloWorld.pdf"));
    }

    @Test
    void getReturnsDocumentWhenFound() throws Exception {
        DocumentDto dto = new DocumentDto(1L, "HelloWorld.pdf", "application/pdf", 7L, Instant.parse("2026-01-01T10:00:00Z"));
        when(documentService.getDocument(eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("HelloWorld.pdf"));
    }

    @Test
    void getReturnsNotFoundWhenMissing() throws Exception {
        when(documentService.getDocument(eq(99L))).thenThrow(new DocumentNotFoundException(99L));

        mockMvc.perform(get("/api/documents/99"))
                .andExpect(status().isNotFound());
    }
}
