package at.fhtw.swen3.paperless_rest.comment.persistence.repository;

import at.fhtw.swen3.paperless_rest.comment.persistence.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Spring Data stellt hier die üblichen Datenbankoperationen bereit.
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Spring Data sucht anhand des Methodennamens alle Kommentare zu dieser Dokument-ID.
    List<Comment> findByDocument_Id(UUID documentId);
}
