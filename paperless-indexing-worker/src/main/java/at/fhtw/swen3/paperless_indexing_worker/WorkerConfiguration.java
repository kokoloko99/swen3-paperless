package at.fhtw.swen3.paperless_indexing_worker;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class WorkerConfiguration {
    @Bean(destroyMethod = "close")
    ElasticsearchClient elasticsearchClient(@Value("${paperless.elasticsearch.url}") String url) {
        return ElasticsearchClient.of(b -> b.host(url));
    }
    @Bean
    Declarables indexingQueues(@Value("${paperless.indexing.queue}") String queue) {
        return new Declarables(QueueBuilder.durable(queue).deadLetterExchange("").deadLetterRoutingKey(queue + ".dead").build(),
                QueueBuilder.durable(queue + ".dead").build());
    }
}
