package at.fhtw.swen3.paperless_rest.repository;

import at.fhtw.swen3.paperless_rest.entity.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class DocumentRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private DocumentRepository documentRepository;

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

    @Test
    void returnsEmptyWhenDocumentDoesNotExist() {
        Optional<Document> found = documentRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

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
