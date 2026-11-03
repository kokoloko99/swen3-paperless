package at.fhtw.swen3.paperless_rest.document.service;

/** Failure contract of the OCR publishing port, independent of RabbitMQ. */
public class OcrPublishException extends RuntimeException {
    public OcrPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
