package at.fhtw.swen3.paperless_rest.document.exception;

import java.util.UUID;

// Meldet, dass ein Dokument mit dieser ID nicht gefunden wurde.
public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(UUID id) {
        super("Document not found: " + id);
    }
}
