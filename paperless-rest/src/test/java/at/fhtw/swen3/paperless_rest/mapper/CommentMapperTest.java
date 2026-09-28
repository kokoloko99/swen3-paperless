package at.fhtw.swen3.paperless_rest.mapper;

import at.fhtw.swen3.paperless_rest.models.CommentBl;
import at.fhtw.swen3.paperless_rest.entity.Comment;
import at.fhtw.swen3.paperless_rest.entity.Document;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    private final CommentMapper mapper = new CommentMapper();

    @Test
    void mapsBlToDal() {
        UUID commentId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        CommentBl bl = new CommentBl();
        bl.setId(commentId);
        bl.setDocumentId(documentId);
        bl.setAuthor("Alice");
        bl.setText("Looks good");
        bl.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));

        Document document = new Document();
        document.setId(documentId);

        Comment dal = mapper.toDal(bl, document);

        assertThat(dal.getId()).isEqualTo(commentId);
        assertThat(dal.getDocument()).isSameAs(document);
        assertThat(dal.getAuthor()).isEqualTo("Alice");
        assertThat(dal.getText()).isEqualTo("Looks good");
        assertThat(dal.getCreatedAt()).isEqualTo(Instant.parse("2026-01-01T10:00:00Z"));
    }

    @Test
    void mapsDalToBl() {
        UUID commentId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        Document document = new Document();
        document.setId(documentId);

        Comment dal = new Comment();
        dal.setId(commentId);
        dal.setDocument(document);
        dal.setAuthor("Bob");
        dal.setText("Needs review");
        dal.setCreatedAt(Instant.parse("2026-02-01T10:00:00Z"));

        CommentBl bl = mapper.toBl(dal);

        assertThat(bl.getId()).isEqualTo(commentId);
        assertThat(bl.getDocumentId()).isEqualTo(documentId);
        assertThat(bl.getAuthor()).isEqualTo("Bob");
        assertThat(bl.getText()).isEqualTo("Needs review");
        assertThat(bl.getCreatedAt()).isEqualTo(Instant.parse("2026-02-01T10:00:00Z"));
    }
}
