package at.fhtw.swen3.paperless_rest.comment.persistence.repository;

import at.fhtw.swen3.paperless_rest.comment.persistence.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByDocument_Id(UUID documentId);
}
