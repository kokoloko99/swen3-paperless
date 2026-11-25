package at.fhtw.swen3.paperless_rest.document.messaging;

import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import at.fhtw.swen3.paperless_rest.document.service.OcrPublisher;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Speichert das Original vor der Verarbeitung bzw. uebergibt nur die Dokumentreferenz an RabbitMQ.
@Component
public class RabbitMqOcrPublisher implements OcrPublisher {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqOcrPublisher.class);
    private final RabbitTemplate rabbitTemplate;
    private final String queueName;

    public RabbitMqOcrPublisher(RabbitTemplate rabbitTemplate,
                               @Value("${paperless.ocr.queue}") String queueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueName = queueName;
    }

// Speichert das Original vor der Verarbeitung bzw. uebergibt nur die Dokumentreferenz an RabbitMQ.
    @Override
    public void publish(DocumentModel document) {
        MessageProperties properties = new MessageProperties();
        properties.setHeader("schemaVersion", "1");
        properties.setMessageId(document.getId().toString());
        properties.setContentType("text/plain");
        properties.setContentEncoding("UTF-8");
        properties.setHeader("originalContentType", document.getContentType());
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        properties.setHeader("documentId", document.getId().toString());
        properties.setHeader("filename", document.getFilename());
        Message message = new Message(document.getId().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8), properties);

        logger.debug("Publishing document {} to OCR queue {} ({} bytes)",
                document.getId(), queueName, message.getBody().length);
        try {
            rabbitTemplate.invoke(operations -> {
                // Der Standard-Exchange leitet die Nachricht an die Queue mit diesem Namen.
                operations.send("", queueName, message);
                operations.waitForConfirmsOrDie(5000);
                return null;
            });
            logger.info("RabbitMQ confirmed document {} in OCR queue {} ({} bytes)",
                    document.getId(), queueName, message.getBody().length);
        } catch (AmqpException ex) {
            // Der zentrale Fehlerhandler protokolliert die vollständige Ursache.
            logger.warn("OCR publish failed for document {} in queue {}", document.getId(), queueName);
            throw new OcrMessagingException(ex);
        }
    }
}
