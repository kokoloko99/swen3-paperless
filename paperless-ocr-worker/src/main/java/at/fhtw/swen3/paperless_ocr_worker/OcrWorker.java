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
    private final OcrResultPublisher publisher;

    public OcrWorker(DocumentStorage storage, TextExtractor extractor, OcrResultPublisher publisher) {
        this.storage = storage;
        this.extractor = extractor;
        this.publisher = publisher;
    }

    @RabbitListener(queues = "${paperless.ocr.queue}")
    public void process(Message message) {
        var properties = message.getMessageProperties();
        String id;
        String filename;
        String type;
        try {
            type = (String) properties.getHeaders().get("originalContentType");
            id = UUID.fromString((String) properties.getHeaders().get("documentId")).toString();
            filename = (String) properties.getHeaders().get("filename");
            if (!"text/plain".equals(properties.getContentType()) || filename == null || filename.isBlank() || message.getBody().length == 0
                    || !"1".equals(properties.getHeaders().get("schemaVersion"))
                    || !id.equals(new String(message.getBody(), java.nio.charset.StandardCharsets.UTF_8))
                    || !("application/pdf".equals(type) || "image/png".equals(type)
                    || "image/jpeg".equals(type) || "image/tiff".equals(type))) {
                throw new IllegalArgumentException("Missing file or unsupported OCR format");
            }
        } catch (RuntimeException ex) {
            throw new AmqpRejectAndDontRequeueException("Invalid OCR message", ex);
        }
        byte[] original = storage.load(id);
        if (original.length == 0) throw new ProcessingException("Original document is empty", null);
        String text = extractor.extract(original, type);
        publisher.publish(new OcrResult(id, filename, type, text));
        logger.info("OCR document {} loaded from MinIO, recognized and submitted for indexing ({} characters)", id, text.length());
    }
}
