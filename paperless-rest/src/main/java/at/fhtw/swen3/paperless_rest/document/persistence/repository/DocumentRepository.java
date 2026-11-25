package at.fhtw.swen3.paperless_rest.document.persistence.repository;

import at.fhtw.swen3.paperless_rest.document.persistence.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Spring Data stellt hier die Datenbankzugriffe für Dokumente bereit.
public interface DocumentRepository extends JpaRepository<Document, UUID> {
}
