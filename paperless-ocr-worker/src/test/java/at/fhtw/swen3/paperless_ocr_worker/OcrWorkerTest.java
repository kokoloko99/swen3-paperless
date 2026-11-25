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
    DocumentIndexer indexer = mock(DocumentIndexer.class);
    OcrWorker worker = new OcrWorker(storage, extractor, indexer);
    String id = UUID.randomUUID().toString();
    Message message() {
        var props = new MessageProperties();
        props.setHeader("documentId", id); props.setHeader("filename", "scan.pdf");
        props.setContentType("application/pdf");
        return new Message(new byte[]{1,2}, props);
    }
    @Test void storesExtractsAndIndexesInOrder() {
        Message m = message(); when(extractor.extract(m.getBody(), "application/pdf")).thenReturn("Invoice");
        worker.process(m);
        var order = inOrder(storage, extractor, indexer);
        order.verify(storage).store(id, m.getBody(), "application/pdf");
        order.verify(extractor).extract(m.getBody(), "application/pdf");
        order.verify(indexer).index(new IndexedDocument(id, "scan.pdf", "application/pdf", "Invoice"));
    }
    @Test void invalidIdIsRejected() {
        Message m = message(); m.getMessageProperties().setHeader("documentId", "bad");
        assertThatThrownBy(() -> worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(storage, extractor, indexer);
    }
    @Test void missingFilenameIsRejected() {
        Message m = message(); m.getMessageProperties().getHeaders().remove("filename");
        assertThatThrownBy(() -> worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(storage, extractor, indexer);
    }
    @Test void unsupportedTypeIsRejected() {
        Message m = message(); m.getMessageProperties().setContentType("text/plain");
        assertThatThrownBy(() -> worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
    @Test void emptyFileIsRejected() {
        Message m = new Message(new byte[0], message().getMessageProperties());
        assertThatThrownBy(() -> worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
    @Test void storageFailureStopsProcessing() {
        doThrow(new ProcessingException("storage", null)).when(storage).store(anyString(), any(), anyString());
        assertThatThrownBy(() -> worker.process(message())).isInstanceOf(ProcessingException.class);
        verifyNoInteractions(extractor, indexer);
    }
    @Test void ocrFailurePreventsIndexing() {
        when(extractor.extract(any(), anyString())).thenThrow(new ProcessingException("ocr", null));
        assertThatThrownBy(() -> worker.process(message())).isInstanceOf(ProcessingException.class);
        verifyNoInteractions(indexer);
    }
    @Test void indexingFailurePropagatesForRetry() {
        when(extractor.extract(any(), anyString())).thenReturn("text");
        doThrow(new ProcessingException("index", null)).when(indexer).index(any());
        assertThatThrownBy(() -> worker.process(message())).isInstanceOf(ProcessingException.class);
    }
    @Test void redeliveryUsesSameDocumentKey() {
        when(extractor.extract(any(), anyString())).thenReturn("");
        Message m=message(); worker.process(m); worker.process(m);
        verify(indexer, times(2)).index(new IndexedDocument(id,"scan.pdf","application/pdf",""));
    }
}
