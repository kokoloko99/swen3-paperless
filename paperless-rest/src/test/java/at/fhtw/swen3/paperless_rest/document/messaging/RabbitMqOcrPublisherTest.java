package at.fhtw.swen3.paperless_rest.document.messaging;

import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RabbitMqOcrPublisherTest {
    @Test
    void translatesBrokerFailureAndPreservesCause() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        var failure = new org.springframework.amqp.AmqpConnectException(new java.net.ConnectException("refused"));
        when(template.invoke(any())).thenThrow(failure);
        DocumentModel document = new DocumentModel();
        document.setId(UUID.randomUUID());

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                new RabbitMqOcrPublisher(template, "paperless.ocr").publish(document, new byte[]{1}))
                .isInstanceOf(OcrMessagingException.class)
                .hasCause(failure);
    }

    @Test
    void publishesOriginalBytesWithMetadataAndWaitsForConfirmation() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        RabbitOperations operations = mock(RabbitOperations.class);
        when(template.invoke(any())).thenAnswer(invocation -> {
            RabbitOperations.OperationsCallback<Object> callback = invocation.getArgument(0);
            return callback.doInRabbit(operations);
        });
        DocumentModel document = new DocumentModel();
        document.setId(UUID.randomUUID());
        document.setFilename("scan.pdf");
        document.setContentType("application/pdf");
        byte[] bytes = new byte[]{37, 80, 68, 70};

        new RabbitMqOcrPublisher(template, "paperless.ocr").publish(document, bytes);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(operations).send(eq(""), eq("paperless.ocr"), captor.capture());
        Message message = captor.getValue();
        assertThat(message.getBody()).isEqualTo(bytes);
        assertThat(message.getMessageProperties().getContentType()).isEqualTo("application/pdf");
        assertThat(message.getMessageProperties().getHeaders())
                .containsEntry("documentId", document.getId().toString())
                .containsEntry("filename", "scan.pdf");
        assertThat(message.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
        verify(operations).waitForConfirmsOrDie(5000);
    }
}
