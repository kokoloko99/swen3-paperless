package at.fhtw.swen3.paperless_rest.comment.persistence.mapper;

import org.mapstruct.factory.Mappers;

import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import at.fhtw.swen3.paperless_rest.comment.persistence.entity.Comment;
import at.fhtw.swen3.paperless_rest.document.persistence.entity.Document;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    private final CommentMapper mapper = Mappers.getMapper(CommentMapper.class);

    @Test
    void mapsModelToDal() {
        UUID commentId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        CommentModel model = new CommentModel();
        model.setId(commentId);
        model.setDocumentId(documentId);
        model.setAuthor("Alice");
        model.setText("Looks good");
        model.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));

        Comment dal = mapper.toDal(model);

        assertThat(dal.getId()).isEqualTo(commentId);
        assertThat(dal.getDocument().getId()).isEqualTo(documentId);
        assertThat(dal.getAuthor()).isEqualTo("Alice");
        assertThat(dal.getText()).isEqualTo("Looks good");
        assertThat(dal.getCreatedAt()).isEqualTo(Instant.parse("2026-01-01T10:00:00Z"));
    }

    @Test
    void mapsDalToModel() {
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

        CommentModel model = mapper.toModel(dal);

        assertThat(model.getId()).isEqualTo(commentId);
        assertThat(model.getDocumentId()).isEqualTo(documentId);
        assertThat(model.getAuthor()).isEqualTo("Bob");
        assertThat(model.getText()).isEqualTo("Needs review");
        assertThat(model.getCreatedAt()).isEqualTo(Instant.parse("2026-02-01T10:00:00Z"));
    }
}
