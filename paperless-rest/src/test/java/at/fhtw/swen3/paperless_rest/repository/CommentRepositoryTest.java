package at.fhtw.swen3.paperless_rest.repository;

import at.fhtw.swen3.paperless_rest.entity.Comment;
import at.fhtw.swen3.paperless_rest.entity.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class CommentRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    private Document persistDocument() {
        Document document = new Document();
        document.setFilename("HelloWorld.pdf");
        document.setContentType("application/pdf");
        document.setSize(123L);
        document.setUploadedAt(Instant.now());
        return documentRepository.save(document);
    }

    @Test
    void savesAndFindsCommentsByDocumentId() {
        Document document = persistDocument();

        Comment comment = new Comment();
        comment.setDocument(document);
        comment.setAuthor("Alice");
        comment.setText("Looks good");
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);

        List<Comment> found = commentRepository.findByDocument_Id(document.getId());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getAuthor()).isEqualTo("Alice");
        assertThat(found.get(0).getText()).isEqualTo("Looks good");
    }

    @Test
    void returnsEmptyListWhenDocumentHasNoComments() {
        Document document = persistDocument();

        List<Comment> found = commentRepository.findByDocument_Id(document.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void deletesComment() {
        Document document = persistDocument();
        Comment comment = new Comment();
        comment.setDocument(document);
        comment.setAuthor("Alice");
        comment.setText("Temporary");
        comment.setCreatedAt(Instant.now());
        Comment saved = commentRepository.save(comment);

        commentRepository.delete(saved);

        assertThat(commentRepository.findById(saved.getId())).isEmpty();
    }
}
