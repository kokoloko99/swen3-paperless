package at.fhtw.swen3.paperless_ocr_worker;
import java.io.IOException;
import java.util.function.Function;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class ElasticsearchDocumentIndexerTest {
    ElasticsearchClient client=mock(ElasticsearchClient.class);
    ElasticsearchIndicesClient indices=mock(ElasticsearchIndicesClient.class);
    IndexedDocument doc=new IndexedDocument("id","scan.pdf","application/pdf","Invoice 123");
    DocumentIndexer indexer=new ElasticsearchDocumentIndexer(client,"paperless-documents");
    @Test void createsMappingAndIndexesUsingStableId() throws Exception {
        when(client.indices()).thenReturn(indices);
        when(indices.exists(any(Function.class))).thenReturn(new BooleanResponse(false));
        indexer.index(doc);
        var create=org.mockito.ArgumentCaptor.forClass(Function.class);
        verify(indices).create(create.capture());
        CreateIndexRequest req=(CreateIndexRequest)((co.elastic.clients.util.ObjectBuilder)
                create.getValue().apply(new CreateIndexRequest.Builder())).build();
        assertThat(req.mappings().properties().get("text").isText()).isTrue();
        assertThat(req.mappings().properties().get("id").isKeyword()).isTrue();
        var request=org.mockito.ArgumentCaptor.forClass(IndexRequest.class);
        verify(client).index(request.capture());
        assertThat(request.getValue().id()).isEqualTo("id");
        assertThat(request.getValue().index()).isEqualTo("paperless-documents");
        assertThat(request.getValue().document()).isEqualTo(doc);
    }
    @Test void existingIndexIsReused() throws Exception {
        when(client.indices()).thenReturn(indices);
        when(indices.exists(any(Function.class))).thenReturn(new BooleanResponse(true));
        indexer.index(doc); verify(indices,never()).create(any(Function.class));
    }
    @Test void wrapsClientFailure() throws Exception {
        when(client.indices()).thenReturn(indices);
        when(indices.exists(any(Function.class))).thenThrow(new IOException("offline"));
        assertThatThrownBy(() -> indexer.index(doc)).isInstanceOf(ProcessingException.class)
            .hasCauseInstanceOf(IOException.class);
        verify(client,never()).index(any(IndexRequest.class));
    }
}
