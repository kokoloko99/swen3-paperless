package at.fhtw.swen3.paperless_ocr_worker;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class OcrWorkerTest {
    DocumentStorage storage = mock(DocumentStorage.class);
    TextExtractor extractor = mock(TextExtractor.class);
    OcrResultPublisher publisher = mock(OcrResultPublisher.class);
    OcrWorker worker = new OcrWorker(storage, extractor, publisher);
    String id = UUID.randomUUID().toString();
    byte[] original = new byte[]{1,2};
    @BeforeEach void prepareOriginal() { when(storage.load(id)).thenReturn(original); }
    Message message() {
        var props = new MessageProperties();
        props.setHeader("documentId", id); props.setHeader("filename", "scan.pdf");
        props.setContentType("text/plain");
        props.setHeader("originalContentType", "application/pdf");
        props.setHeader("schemaVersion", "1");
        return new Message(id.getBytes(java.nio.charset.StandardCharsets.UTF_8), props);
    }
    @Test void loadsExtractsAndPublishesInOrder() {
        Message m = message(); when(extractor.extract(original, "application/pdf")).thenReturn("Invoice");
        worker.process(m);
        var order = inOrder(storage, extractor, publisher);
        order.verify(storage).load(id);
        order.verify(extractor).extract(original, "application/pdf");
        order.verify(publisher).publish(new OcrResult(id, "scan.pdf", "application/pdf", "Invoice"));
    }
    @Test void emptyOriginalStopsOcr() {
        when(storage.load(id)).thenReturn(new byte[0]);
        assertThatThrownBy(() -> worker.process(message())).isInstanceOf(ProcessingException.class);
        verifyNoInteractions(extractor, publisher);
    }
    @Test void mismatchingReferenceIsRejected() {
        Message m = new Message(UUID.randomUUID().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8),message().getMessageProperties());
        assertThatThrownBy(() -> worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(storage, extractor, publisher);
    }
    @Test void invalidIdIsRejected() {
        Message m = message(); m.getMessageProperties().setHeader("documentId", "bad");
        assertThatThrownBy(() -> worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(storage, extractor, publisher);
    }
    @Test void missingFilenameIsRejected() {
        Message m = message(); m.getMessageProperties().getHeaders().remove("filename");
        assertThatThrownBy(() -> worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(storage, extractor, publisher);
    }
    @Test void unsupportedTypeIsRejected() {
        Message m = message(); m.getMessageProperties().setHeader("originalContentType", "text/plain");
        assertThatThrownBy(() -> worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
    @Test void emptyReferenceIsRejected() {
        Message m = new Message(new byte[0], message().getMessageProperties());
        assertThatThrownBy(() -> worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
    @Test void storageFailureStopsProcessing() {
        when(storage.load(id)).thenThrow(new ProcessingException("storage", null));
        assertThatThrownBy(() -> worker.process(message())).isInstanceOf(ProcessingException.class);
        verifyNoInteractions(extractor, publisher);
    }
    @Test void ocrFailurePreventsResultPublication() {
        when(extractor.extract(any(), anyString())).thenThrow(new ProcessingException("ocr", null));
        assertThatThrownBy(() -> worker.process(message())).isInstanceOf(ProcessingException.class);
        verifyNoInteractions(publisher);
    }
    @Test void resultPublicationFailurePropagatesForRetry() {
        when(extractor.extract(any(), anyString())).thenReturn("text");
        doThrow(new ProcessingException("index", null)).when(publisher).publish(any());
        assertThatThrownBy(() -> worker.process(message())).isInstanceOf(ProcessingException.class);
    }
    @Test void redeliveryUsesSameDocumentKey() {
        when(extractor.extract(any(), anyString())).thenReturn("");
        Message m=message(); worker.process(m); worker.process(m);
        verify(publisher, times(2)).publish(new OcrResult(id,"scan.pdf","application/pdf",""));
    }
}
