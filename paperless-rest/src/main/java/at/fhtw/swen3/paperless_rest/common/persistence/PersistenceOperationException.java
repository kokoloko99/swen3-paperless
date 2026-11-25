package at.fhtw.swen3.paperless_rest.common.persistence;

// Beschreibt einen fehlgeschlagenen Datenbankzugriff und behält seine Ursache.
public class PersistenceOperationException extends RuntimeException {
    public PersistenceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
