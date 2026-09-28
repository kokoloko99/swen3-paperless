package at.fhtw.swen3.paperless_rest.controller;

import at.fhtw.swen3.paperless_rest.models.CommentBl;
import at.fhtw.swen3.paperless_rest.dto.CommentDto;
import at.fhtw.swen3.paperless_rest.dto.CreateCommentRequest;
import at.fhtw.swen3.paperless_rest.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents/{documentId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> addComment(@PathVariable UUID documentId,
                                                  @RequestBody CreateCommentRequest request) {
        CommentBl comment = new CommentBl();
        comment.setDocumentId(documentId);
        comment.setAuthor(request.author());
        comment.setText(request.text());

        CommentBl saved = commentService.addComment(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable UUID documentId) {
        List<CommentDto> comments = commentService.getComments(documentId).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID documentId, @PathVariable UUID commentId) {
        commentService.deleteComment(documentId, commentId);
        return ResponseEntity.noContent().build();
    }

    private CommentDto toDto(CommentBl comment) {
        return new CommentDto(
                comment.getId(),
                comment.getDocumentId(),
                comment.getAuthor(),
                comment.getText(),
                comment.getCreatedAt()
        );
    }
}
