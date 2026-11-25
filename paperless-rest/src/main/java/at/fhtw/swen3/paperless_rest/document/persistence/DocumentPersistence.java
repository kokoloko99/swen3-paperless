package at.fhtw.swen3.paperless_rest.document.persistence;

import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;

import java.util.Optional;
import java.util.UUID;

// Legt die vom Service benötigten Datenbankoperationen für Dokumente fest.
public interface DocumentPersistence {

    // Speichert die Metadaten eines Dokuments.
    DocumentModel save(DocumentModel document);

    // Sucht die Metadaten anhand der Dokument-ID.
    Optional<DocumentModel> findById(UUID id);

    // Prüft, ob ein Dokument mit dieser ID existiert.
    boolean existsById(UUID id);

    // Löscht das Dokument; die Datenbank entfernt seine Kommentare automatisch.
    void deleteById(UUID id);
}
