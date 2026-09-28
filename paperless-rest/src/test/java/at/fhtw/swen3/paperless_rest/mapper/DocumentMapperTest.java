package at.fhtw.swen3.paperless_rest.mapper;

import at.fhtw.swen3.paperless_rest.models.DocumentBl;
import at.fhtw.swen3.paperless_rest.entity.Document;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    private final DocumentMapper mapper = new DocumentMapper();

    @Test
    void mapsBlToDal() {
        UUID id = UUID.randomUUID();
        DocumentBl bl = new DocumentBl();
        bl.setId(id);
        bl.setFilename("HelloWorld.pdf");
        bl.setContentType("application/pdf");
        bl.setSize(42L);
        bl.setUploadedAt(Instant.parse("2026-01-01T10:00:00Z"));

        Document dal = mapper.toDal(bl);

        assertThat(dal.getId()).isEqualTo(id);
        assertThat(dal.getFilename()).isEqualTo("HelloWorld.pdf");
        assertThat(dal.getContentType()).isEqualTo("application/pdf");
        assertThat(dal.getSize()).isEqualTo(42L);
        assertThat(dal.getUploadedAt()).isEqualTo(Instant.parse("2026-01-01T10:00:00Z"));
    }

    @Test
    void mapsDalToBl() {
        UUID id = UUID.randomUUID();
        Document dal = new Document();
        dal.setId(id);
        dal.setFilename("Existing.pdf");
        dal.setContentType("application/pdf");
        dal.setSize(99L);
        dal.setUploadedAt(Instant.parse("2026-02-01T10:00:00Z"));

        DocumentBl bl = mapper.toBl(dal);

        assertThat(bl.getId()).isEqualTo(id);
        assertThat(bl.getFilename()).isEqualTo("Existing.pdf");
        assertThat(bl.getContentType()).isEqualTo("application/pdf");
        assertThat(bl.getSize()).isEqualTo(99L);
        assertThat(bl.getUploadedAt()).isEqualTo(Instant.parse("2026-02-01T10:00:00Z"));
    }
}
