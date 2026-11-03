package at.fhtw.swen3.paperless_rest.document.messaging;

import at.fhtw.swen3.paperless_rest.document.service.OcrPublishException;

public class OcrMessagingException extends OcrPublishException {
    public OcrMessagingException(Throwable cause) {
        super("Could not publish the document to the OCR queue", cause);
    }
}
