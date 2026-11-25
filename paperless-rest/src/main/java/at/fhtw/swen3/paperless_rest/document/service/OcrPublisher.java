package at.fhtw.swen3.paperless_rest.document.service;

import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;

// Legt fest, wie der Service ein Dokument zur OCR-Verarbeitung übergibt.
public interface OcrPublisher {
    /** @throws OcrPublishException wenn die Datei nicht versendet werden kann */
// Speichert das Original vor der Verarbeitung bzw. uebergibt nur die Dokumentreferenz an RabbitMQ.
    void publish(DocumentModel document);
}
