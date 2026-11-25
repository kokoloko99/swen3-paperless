package at.fhtw.swen3.paperless_rest.document.persistence.repository;

import at.fhtw.swen3.paperless_rest.document.persistence.entity.Document;
import at.fhtw.swen3.paperless_rest.comment.persistence.entity.Comment;
import at.fhtw.swen3.paperless_rest.comment.persistence.repository.CommentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Prüft echte Datenbankzugriffe mit PostgreSQL in einem Testcontainer.
@DataJpaTest
@Testcontainers
class DocumentRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager entityManager;

    // Prüft das echte Datenbank-Cascade; andere Dokumente und Kommentare bleiben erhalten.
    @Test
    void deletingDocumentRemovesOnlyItsComments() {
        Document deleted = new Document();
        deleted.setFilename("delete.pdf");
        deleted.setSize(1);
        documentRepository.save(deleted);
        Document retained = new Document();
        retained.setFilename("keep.pdf");
        retained.setSize(1);
        documentRepository.save(retained);

        Comment first = new Comment();
        first.setDocument(deleted);
        first.setAuthor("Alice");
        first.setText("First");
        commentRepository.save(first);
        Comment second = new Comment();
        second.setDocument(deleted);
        second.setAuthor("Bob");
        second.setText("Second");
        commentRepository.save(second);
        Comment other = new Comment();
        other.setDocument(retained);
        other.setAuthor("Alice");
        other.setText("Keep");
        commentRepository.saveAndFlush(other);

        UUID deletedId = deleted.getId();
        UUID retainedId = retained.getId();
        UUID otherCommentId = other.getId();
        // Der Löschaufruf erhält wie eine neue HTTP-Anfrage einen frischen Kontext.
        entityManager.clear();
        documentRepository.deleteById(deletedId);
        documentRepository.flush();
        entityManager.clear();

        assertThat(documentRepository.findById(deletedId)).isEmpty();
        assertThat(commentRepository.findByDocument_Id(deletedId)).isEmpty();
        assertThat(documentRepository.existsById(retainedId)).isTrue();
        assertThat(commentRepository.findById(otherCommentId)).isPresent();
        // Wiederholtes Löschen eines bereits fehlenden Dokuments ist erlaubt.
        documentRepository.deleteById(deletedId);
        documentRepository.flush();
    }

    // Prüft, dass ein gespeichertes Dokument über seine ID gefunden wird.
    @Test
    void savesAndFindsDocumentById() {
        Document document = new Document();
        document.setFilename("HelloWorld.pdf");
        document.setContentType("application/pdf");
        document.setSize(1234L);
        document.setUploadedAt(Instant.parse("2026-01-01T10:00:00Z"));

        Document saved = documentRepository.save(document);

        Optional<Document> found = documentRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFilename()).isEqualTo("HelloWorld.pdf");
        assertThat(found.get().getContentType()).isEqualTo("application/pdf");
        assertThat(found.get().getSize()).isEqualTo(1234L);
    }

    // Prüft, dass eine unbekannte ID kein Dokument liefert.
    @Test
    void returnsEmptyWhenDocumentDoesNotExist() {
        Optional<Document> found = documentRepository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    // Prüft, dass ein gelöschtes Dokument nicht mehr gefunden wird.
    @Test
    void deletesDocument() {
        Document document = new Document();
        document.setFilename("ToDelete.pdf");
        document.setContentType("application/pdf");
        document.setSize(1L);
        document.setUploadedAt(Instant.now());
        Document saved = documentRepository.save(document);

        documentRepository.deleteById(saved.getId());

        assertThat(documentRepository.findById(saved.getId())).isEmpty();
    }
}
