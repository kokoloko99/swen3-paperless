package at.fhtw.swen3.paperless_rest.common.persistence.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

// Stellt die gemeinsame, automatisch erzeugte ID für Datenbankeinträge bereit.
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    // Verwendet eine automatisch erzeugte UUID als eindeutigen Primärschlüssel.
    @Id
    @UuidGenerator
    private UUID id;
}
