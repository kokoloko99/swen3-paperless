package at.fhtw.swen3.paperless_ocr_worker;
import java.nio.charset.StandardCharsets;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class RabbitMqOcrResultPublisher implements OcrResultPublisher {
    private final RabbitTemplate template;
    private final String queue;
    public RabbitMqOcrResultPublisher(RabbitTemplate template, @Value("${paperless.indexing.queue}") String queue) {
        this.template = template; this.queue = queue;
    }
    @Override
    public void publish(OcrResult result) {
        var p = new MessageProperties();
        p.setMessageId(result.id()); p.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        p.setContentType("text/plain"); p.setContentEncoding("UTF-8");
        p.setHeader("documentId", result.id()); p.setHeader("filename", result.filename());
        p.setHeader("originalContentType", result.contentType()); p.setHeader("schemaVersion", "1");
        var message = new Message(result.text().getBytes(StandardCharsets.UTF_8), p);
        try {
            template.invoke(operations -> {
                operations.send("", queue, message);
                operations.waitForConfirmsOrDie(5000);
                return null;
            });
        } catch (AmqpException ex) {
            throw new ProcessingException("Could not publish OCR result", ex);
        }
    }
}
