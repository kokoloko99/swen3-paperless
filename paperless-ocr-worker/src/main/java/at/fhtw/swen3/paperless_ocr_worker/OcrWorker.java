package at.fhtw.swen3.paperless_ocr_worker;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OcrWorker {
    private static final Logger logger = LoggerFactory.getLogger(OcrWorker.class);
    private final DocumentStorage storage;
    private final TextExtractor extractor;
    private final DocumentIndexer indexer;

    public OcrWorker(DocumentStorage storage, TextExtractor extractor, DocumentIndexer indexer) {
        this.storage = storage;
        this.extractor = extractor;
        this.indexer = indexer;
    }

    @RabbitListener(queues = "${paperless.ocr.queue}")
    public void process(Message message) {
        var properties = message.getMessageProperties();
        String id;
        String filename;
        String type = properties.getContentType();
        try {
            id = UUID.fromString((String) properties.getHeaders().get("documentId")).toString();
            filename = (String) properties.getHeaders().get("filename");
            if (filename == null || filename.isBlank() || message.getBody().length == 0
                    || !("application/pdf".equals(type) || "image/png".equals(type)
                    || "image/jpeg".equals(type) || "image/tiff".equals(type))) {
                throw new IllegalArgumentException("Missing file or unsupported OCR format");
            }
        } catch (RuntimeException ex) {
            throw new AmqpRejectAndDontRequeueException("Invalid OCR message", ex);
        }
        storage.store(id, message.getBody(), type);
        String text = extractor.extract(message.getBody(), type);
        indexer.index(new IndexedDocument(id, filename, type, text));
        logger.info("OCR document {} stored and indexed ({} characters)", id, text.length());
    }
}
