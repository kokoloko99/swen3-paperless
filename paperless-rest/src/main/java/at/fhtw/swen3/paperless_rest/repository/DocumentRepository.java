package at.fhtw.swen3.paperless_rest.repository;

import at.fhtw.swen3.paperless_rest.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
