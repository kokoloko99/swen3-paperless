package at.fhtw.swen3.paperless_rest.document.exception;

public class OcrSubmissionException extends RuntimeException {
    public OcrSubmissionException(Throwable cause) {
        super("Document upload could not be submitted for OCR", cause);
    }
}
