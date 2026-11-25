package at.fhtw.swen3.paperless_rest.document.exception;

// Meldet ungültige Angaben zu einem Dokument.
public class InvalidDocumentException extends RuntimeException {

    public InvalidDocumentException(String message) {
        super(message);
    }
}
