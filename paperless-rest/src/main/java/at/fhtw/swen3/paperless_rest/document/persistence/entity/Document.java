package at.fhtw.swen3.paperless_rest.document.persistence.entity;

import at.fhtw.swen3.paperless_rest.common.persistence.entity.AuditableEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Document extends AuditableEntity {

    private String filename;

    private String contentType;

    private long size;

    private Instant uploadedAt;
}
