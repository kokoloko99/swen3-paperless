package at.fhtw.swen3.paperless_rest.comment.persistence;

import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Legt fest, welche Datenbankoperationen der Service für Kommentare braucht.
public interface CommentPersistence {

    // Speichert einen Kommentar und gibt ihn zurück.
    CommentModel save(CommentModel comment);

    // Sucht einen Kommentar anhand seiner ID.
    Optional<CommentModel> findById(UUID id);

    // Lädt alle Kommentare zu einem Dokument.
    List<CommentModel> findByDocumentId(UUID documentId);

    // Löscht einen Kommentar anhand seiner ID.
    void deleteById(UUID id);
}