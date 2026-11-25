package at.fhtw.swen3.paperless_rest.document.exception;

// Meldet, dass der Upload nicht für die OCR-Verarbeitung übergeben werden konnte.
public class OcrSubmissionException extends RuntimeException {
    public OcrSubmissionException(Throwable cause) {
        super("Document upload could not be submitted for OCR", cause);
    }
}
