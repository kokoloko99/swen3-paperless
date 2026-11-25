package at.fhtw.swen3.paperless_ocr_worker;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class RabbitMqOcrResultPublisherTest {
    @Test void publishesPersistentUtf8ResultAndWaitsForConfirmation() {
        RabbitTemplate template=mock(RabbitTemplate.class);RabbitOperations ops=mock(RabbitOperations.class);
        when(template.invoke(any())).thenAnswer(i -> ((RabbitOperations.OperationsCallback<?>)i.getArgument(0)).doInRabbit(ops));
        new RabbitMqOcrResultPublisher(template,"index").publish(new OcrResult("id","scan.pdf","application/pdf","Rechnung"));
        var captor=org.mockito.ArgumentCaptor.forClass(Message.class);
        verify(ops).send(eq(""),eq("index"),captor.capture());verify(ops).waitForConfirmsOrDie(5000);
        var m=captor.getValue();assertThat(new String(m.getBody(),java.nio.charset.StandardCharsets.UTF_8)).isEqualTo("Rechnung");
        assertThat(m.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
        assertThat(m.getMessageProperties().getHeaders()).containsEntry("originalContentType","application/pdf").containsEntry("schemaVersion","1");
    }
    @Test void wrapsBrokerFailure() {
        RabbitTemplate template=mock(RabbitTemplate.class);
        when(template.invoke(any())).thenThrow(new org.springframework.amqp.AmqpException("offline"));
        assertThatThrownBy(()->new RabbitMqOcrResultPublisher(template,"index").publish(new OcrResult("id","scan.pdf","application/pdf","text")))
            .isInstanceOf(ProcessingException.class);
    }
}
