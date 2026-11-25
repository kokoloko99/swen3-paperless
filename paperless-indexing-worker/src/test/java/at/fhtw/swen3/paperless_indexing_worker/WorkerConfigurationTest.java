package at.fhtw.swen3.paperless_indexing_worker;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import static org.assertj.core.api.Assertions.*;
class WorkerConfigurationTest {
    @Test void declaresDurableIndexingQueueAndDeadLetterQueue() {
        var queues=new WorkerConfiguration().indexingQueues("paperless.indexing.v1").getDeclarablesByType(Queue.class);
        assertThat(queues).hasSize(2);
        assertThat(queues.getFirst().getArguments()).containsEntry("x-dead-letter-routing-key","paperless.indexing.v1.dead");
        assertThat(queues.get(1).isDurable()).isTrue();
    }
}
