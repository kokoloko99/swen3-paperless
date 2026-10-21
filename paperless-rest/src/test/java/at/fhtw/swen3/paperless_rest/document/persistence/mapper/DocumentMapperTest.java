package at.fhtw.swen3.paperless_rest.document.persistence.mapper;

import org.mapstruct.factory.Mappers;

import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import at.fhtw.swen3.paperless_rest.document.persistence.entity.Document;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    private final DocumentMapper mapper = Mappers.getMapper(DocumentMapper.class);

    @Test
    void mapsModelToDal() {
        UUID id = UUID.randomUUID();
        DocumentModel model = new DocumentModel();
        model.setId(id);
        model.setFilename("HelloWorld.pdf");
        model.setContentType("application/pdf");
        model.setSize(42L);
        model.setUploadedAt(Instant.parse("2026-01-01T10:00:00Z"));

        Document dal = mapper.toDal(model);

        assertThat(dal.getId()).isEqualTo(id);
        assertThat(dal.getFilename()).isEqualTo("HelloWorld.pdf");
        assertThat(dal.getContentType()).isEqualTo("application/pdf");
        assertThat(dal.getSize()).isEqualTo(42L);
        assertThat(dal.getUploadedAt()).isEqualTo(Instant.parse("2026-01-01T10:00:00Z"));
    }

    @Test
    void mapsDalToModel() {
        UUID id = UUID.randomUUID();
        Document dal = new Document();
        dal.setId(id);
        dal.setFilename("Existing.pdf");
        dal.setContentType("application/pdf");
        dal.setSize(99L);
        dal.setUploadedAt(Instant.parse("2026-02-01T10:00:00Z"));

        DocumentModel model = mapper.toModel(dal);

        assertThat(model.getId()).isEqualTo(id);
        assertThat(model.getFilename()).isEqualTo("Existing.pdf");
        assertThat(model.getContentType()).isEqualTo("application/pdf");
        assertThat(model.getSize()).isEqualTo(99L);
        assertThat(model.getUploadedAt()).isEqualTo(Instant.parse("2026-02-01T10:00:00Z"));
    }
}
