package at.fhtw.swen3.paperless_rest.document.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {
    @Bean
    public Queue ocrQueue(@Value("${paperless.ocr.queue}") String queueName) {
        return new Queue(queueName, true);
    }
}
