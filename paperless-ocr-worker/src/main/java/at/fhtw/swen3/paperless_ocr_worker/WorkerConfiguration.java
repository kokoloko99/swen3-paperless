package at.fhtw.swen3.paperless_ocr_worker;

import io.minio.MinioClient;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerConfiguration {
    @Bean
    MinioClient minioClient(@Value("${paperless.minio.url}") String url,
                           @Value("${paperless.minio.access-key}") String accessKey,
                           @Value("${paperless.minio.secret-key}") String secretKey) {
        return MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
    }

    @Bean
    Declarables workerQueues(@Value("${paperless.ocr.queue}") String queue,
            @Value("${paperless.indexing.queue}") String indexingQueue) {
        // Default exchange routes rejected messages to the durable dead-letter queue.
        return new Declarables(QueueBuilder.durable(queue).deadLetterExchange("").deadLetterRoutingKey(queue + ".dead").build(),
                QueueBuilder.durable(queue + ".dead").build(),
                QueueBuilder.durable(indexingQueue).deadLetterExchange("").deadLetterRoutingKey(indexingQueue + ".dead").build(),
                QueueBuilder.durable(indexingQueue + ".dead").build());
    }
}
