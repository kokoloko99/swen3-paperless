package at.fhtw.swen3.paperless_indexing_worker;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CharacterCodingException;
import java.util.UUID;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;

@Component
public class IndexingWorker {
    private final DocumentIndexer indexer;
    public IndexingWorker(DocumentIndexer indexer) { this.indexer = indexer; }
    @RabbitListener(queues = "${paperless.indexing.queue}")
    public void process(Message message) {
        IndexedDocument document;
        try {
            var p = message.getMessageProperties();
            String id = UUID.fromString((String) p.getHeaders().get("documentId")).toString();
            String filename = (String) p.getHeaders().get("filename");
            String type = (String) p.getHeaders().get("originalContentType");
            if (filename == null || filename.isBlank() || type == null || type.isBlank()
                    || !"text/plain".equals(p.getContentType()) || !"1".equals(p.getHeaders().get("schemaVersion"))) {
                throw new IllegalArgumentException("Invalid OCR result metadata");
            }
            String text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(message.getBody())).toString();
            document = new IndexedDocument(id, filename, type, text);
        } catch (RuntimeException | CharacterCodingException ex) {
            throw new AmqpRejectAndDontRequeueException("Invalid indexing message", ex);
        }
        indexer.index(document);
        LoggerFactory.getLogger(getClass()).info("Document {} indexed ({} characters)", document.id(), document.text().length());
    }
}
