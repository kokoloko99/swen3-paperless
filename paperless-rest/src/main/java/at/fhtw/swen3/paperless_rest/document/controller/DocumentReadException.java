package at.fhtw.swen3.paperless_rest.document.controller;

// Meldet, dass der Inhalt einer hochgeladenen Datei nicht gelesen werden konnte.
public class DocumentReadException extends RuntimeException {
    public DocumentReadException(Throwable cause) {
        super("Could not read the uploaded file", cause);
    }
}
