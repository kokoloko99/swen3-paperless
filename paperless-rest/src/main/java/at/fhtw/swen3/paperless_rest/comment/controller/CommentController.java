package at.fhtw.swen3.paperless_rest.comment.controller;

import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import at.fhtw.swen3.paperless_rest.comment.dto.CommentDto;
import at.fhtw.swen3.paperless_rest.comment.dto.CreateCommentRequest;
import at.fhtw.swen3.paperless_rest.comment.mapper.CommentDtoMapper;
import at.fhtw.swen3.paperless_rest.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
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
@RequestMapping("/api/v1/documents/{documentId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentDtoMapper commentDtoMapper;
    private final Logger logger;

    @PostMapping
    public ResponseEntity<CommentDto> addComment(@PathVariable UUID documentId,
                                                  @RequestBody CreateCommentRequest request) {
        logger.info("Adding comment to document {}", documentId);
        CommentModel saved = commentService.addComment(commentDtoMapper.toModel(documentId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(commentDtoMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable UUID documentId) {
        logger.debug("Getting comments for document {}", documentId);
        return ResponseEntity.ok(commentDtoMapper.toDtos(commentService.getComments(documentId)));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID documentId, @PathVariable UUID commentId) {
        logger.info("Deleting comment {} from document {}", commentId, documentId);
        commentService.deleteComment(documentId, commentId);
        return ResponseEntity.noContent().build();
    }
}
