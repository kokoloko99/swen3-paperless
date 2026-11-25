package at.fhtw.swen3.paperless_rest.document.messaging;

import at.fhtw.swen3.paperless_rest.document.service.OcrPublishException;

// Übersetzt einen RabbitMQ-Fehler in die Fehlerart unserer Versandschnittstelle.
public class OcrMessagingException extends OcrPublishException {
    public OcrMessagingException(Throwable cause) {
        super("Could not publish the document to the OCR queue", cause);
    }
}
