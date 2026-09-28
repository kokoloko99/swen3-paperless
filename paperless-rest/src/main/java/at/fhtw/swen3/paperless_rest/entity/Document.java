package at.fhtw.swen3.paperless_rest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @UuidGenerator
    private UUID id;

    private String filename;

    private String contentType;

    private long size;

    private Instant uploadedAt;
}
