package at.fhtw.swen3.paperless_indexing_worker;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import org.springframework.amqp.core.*;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class IndexingWorkerTest {
    DocumentIndexer indexer=mock(DocumentIndexer.class);
    IndexingWorker worker=new IndexingWorker(indexer);
    String id=UUID.randomUUID().toString();
    Message message(String text) {
        var p=new MessageProperties();p.setContentType("text/plain");
        p.setHeader("documentId",id);p.setHeader("filename","scan.pdf");
        p.setHeader("originalContentType","application/pdf");p.setHeader("schemaVersion","1");
        return new Message(text.getBytes(StandardCharsets.UTF_8),p);
    }
    @Test void indexesRecognizedTextUsingStableId() {
        Message m=message("Rechnung 123");worker.process(m);worker.process(m);
        verify(indexer,times(2)).index(new IndexedDocument(id,"scan.pdf","application/pdf","Rechnung 123"));
    }
    @Test void allowsEmptyTextForBlankPages() {
        worker.process(message(""));verify(indexer).index(new IndexedDocument(id,"scan.pdf","application/pdf",""));
    }
    @Test void rejectsMalformedId() {
        Message m=message("text");m.getMessageProperties().setHeader("documentId","bad");
        assertThatThrownBy(()->worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(indexer);
    }
    @Test void rejectsMissingMetadata() {
        Message m=message("text");m.getMessageProperties().getHeaders().remove("filename");
        assertThatThrownBy(()->worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(indexer);
    }
    @Test void rejectsWrongSchema() {
        Message m=message("text");m.getMessageProperties().setHeader("schemaVersion","99");
        assertThatThrownBy(()->worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
    @Test void rejectsInvalidUtf8() {
        Message m=new Message(new byte[]{(byte)0xff},message("").getMessageProperties());
        assertThatThrownBy(()->worker.process(m)).isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
    @Test void failurePropagatesSoMessageIsNotAcknowledged() {
        var failure=new ProcessingException("offline",null);doThrow(failure).when(indexer).index(any());
        assertThatThrownBy(()->worker.process(message("text"))).isSameAs(failure);
    }
}
