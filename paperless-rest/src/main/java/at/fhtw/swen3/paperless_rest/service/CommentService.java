package at.fhtw.swen3.paperless_rest.service;

import at.fhtw.swen3.paperless_rest.models.CommentBl;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    CommentBl addComment(CommentBl comment);

    List<CommentBl> getComments(UUID documentId);

    void deleteComment(UUID documentId, UUID commentId);
}
