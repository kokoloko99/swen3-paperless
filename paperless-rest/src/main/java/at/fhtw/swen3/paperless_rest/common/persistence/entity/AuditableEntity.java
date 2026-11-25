package at.fhtw.swen3.paperless_rest.common.persistence.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

// Ergänzt Datenbankeinträge um den Erstellungs- und Änderungszeitpunkt.
@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntity extends BaseEntity {

    // Hibernate setzt den Zeitpunkt beim Erstellen des Datenbankeintrags.
    @CreationTimestamp
    private Instant createdAt;

    // Hibernate aktualisiert den Zeitpunkt bei Änderungen am Datenbankeintrag.
    @UpdateTimestamp
    private Instant updatedAt;
}
