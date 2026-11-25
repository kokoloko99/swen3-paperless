package at.fhtw.swen3.paperless_ocr_worker;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import static org.assertj.core.api.Assertions.*;
class WorkerConfigurationTest {
    @Test void declaresDurableQueueWithDeadLetterRoute() {
        var queues = new WorkerConfiguration().workerQueues("paperless.ocr.v5", "paperless.indexing.v1").getDeclarablesByType(Queue.class);
        assertThat(queues).hasSize(4);
        assertThat(queues.getFirst().isDurable()).isTrue();
        assertThat(queues.getFirst().getArguments()).containsEntry("x-dead-letter-exchange", "")
            .containsEntry("x-dead-letter-routing-key", "paperless.ocr.v5.dead");
        assertThat(queues.get(1).getName()).isEqualTo("paperless.ocr.v5.dead");
        assertThat(queues.get(1).isDurable()).isTrue();
    }
}
