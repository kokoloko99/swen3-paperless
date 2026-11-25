package at.fhtw.swen3.paperless_rest.document.service;

// Beschreibt einen Versandfehler, ohne den Service an RabbitMQ zu binden.
public class OcrPublishException extends RuntimeException {
    public OcrPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
