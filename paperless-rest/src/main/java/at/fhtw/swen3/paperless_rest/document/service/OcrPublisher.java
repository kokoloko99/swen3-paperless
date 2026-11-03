package at.fhtw.swen3.paperless_rest.document.service;

import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;

public interface OcrPublisher {
    /** @throws OcrPublishException if the document cannot be published */
    void publish(DocumentModel document, byte[] content);
}
