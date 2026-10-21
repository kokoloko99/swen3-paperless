package at.fhtw.swen3.paperless_rest.comment.controller;

import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import at.fhtw.swen3.paperless_rest.comment.dto.CreateCommentRequest;
import at.fhtw.swen3.paperless_rest.document.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.comment.exception.InvalidCommentException;
import at.fhtw.swen3.paperless_rest.comment.service.CommentService;
import tools.jackson.databind.ObjectMapper;
import at.fhtw.swen3.paperless_rest.comment.mapper.CommentDtoMapperImpl;
import at.fhtw.swen3.paperless_rest.common.security.SecurityConfig;
import at.fhtw.swen3.paperless_rest.common.logging.LoggingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import({CommentDtoMapperImpl.class, SecurityConfig.class, LoggingConfiguration.class})
@WithMockUser
class CommentControllerTest {

    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    private static CommentModel sampleComment() {
        CommentModel comment = new CommentModel();
        comment.setId(COMMENT_ID);
        comment.setDocumentId(DOCUMENT_ID);
        comment.setAuthor("Alice");
        comment.setText("Looks good");
        comment.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
        return comment;
    }

    @Test
    void addCommentReturnsCreated() throws Exception {
        when(commentService.addComment(any())).thenReturn(sampleComment());
        CreateCommentRequest request = new CreateCommentRequest("Alice", "Looks good");

        mockMvc.perform(post("/api/v1/documents/{documentId}/comments", DOCUMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(COMMENT_ID.toString()))
                .andExpect(jsonPath("$.author").value("Alice"));
    }

    @Test
    void addCommentReturnsBadRequestWhenInvalid() throws Exception {
        when(commentService.addComment(any())).thenThrow(new InvalidCommentException("Comment text must not be blank"));
        CreateCommentRequest request = new CreateCommentRequest("Alice", "");

        mockMvc.perform(post("/api/v1/documents/{documentId}/comments", DOCUMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCommentReturnsNotFoundWhenDocumentMissing() throws Exception {
        when(commentService.addComment(any())).thenThrow(new DocumentNotFoundException(DOCUMENT_ID));
        CreateCommentRequest request = new CreateCommentRequest("Alice", "Looks good");

        mockMvc.perform(post("/api/v1/documents/{documentId}/comments", DOCUMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCommentsReturnsList() throws Exception {
        when(commentService.getComments(eq(DOCUMENT_ID))).thenReturn(List.of(sampleComment()));

        mockMvc.perform(get("/api/v1/documents/{documentId}/comments", DOCUMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author").value("Alice"));
    }

    @Test
    void getCommentsReturnsNotFoundWhenDocumentIsMissing() throws Exception {
        when(commentService.getComments(eq(DOCUMENT_ID))).thenThrow(new DocumentNotFoundException(DOCUMENT_ID));

        mockMvc.perform(get("/api/v1/documents/{documentId}/comments", DOCUMENT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCommentReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/documents/{documentId}/comments/{commentId}", DOCUMENT_ID, COMMENT_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCommentReturnsBadRequestWhenIdIsInvalid() throws Exception {
        mockMvc.perform(delete("/api/v1/documents/{documentId}/comments/{commentId}", DOCUMENT_ID, "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
