package at.fhtw.swen3.paperless_rest.document.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Konfiguriert die eigene RabbitMQ-Queue für OCR-Aufträge.
@Configuration
public class RabbitMqConfiguration {
    // Legt eine dauerhafte Queue an, die einen Neustart von RabbitMQ übersteht.
    @Bean
    public Queue ocrQueue(@Value("${paperless.ocr.queue}") String queueName) {
        return org.springframework.amqp.core.QueueBuilder.durable(queueName)
                .deadLetterExchange("").deadLetterRoutingKey(queueName + ".dead").build();
    }
}
