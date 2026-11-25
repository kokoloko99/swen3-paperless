package at.fhtw.swen3.paperless_indexing_worker;

import java.io.IOException;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchDocumentIndexer implements DocumentIndexer {
    private final ElasticsearchClient client;
    private final String index;
    public ElasticsearchDocumentIndexer(ElasticsearchClient client,
            @Value("${paperless.elasticsearch.index}") String index) {
        this.client = client;
        this.index = index;
    }
    @Override
    public void index(IndexedDocument document) {
        try {
            // Explicit mapping avoids accidental date detection in filenames or OCR output.
            if (!client.indices().exists(b -> b.index(index)).value()) {
                try {
                    client.indices().create(b -> b.index(index).mappings(m -> m
                            .properties("id", f -> f.keyword(k -> k))
                            .properties("filename", f -> f.text(t -> t))
                            .properties("contentType", f -> f.keyword(k -> k))
                            .properties("text", f -> f.text(t -> t))));
                } catch (ElasticsearchException ex) {
                    if (!"resource_already_exists_exception".equals(ex.error().type())) throw ex;
                }
            }
            client.index(IndexRequest.of(b -> b.index(index).id(document.id()).document(document)));
        } catch (IOException | ElasticsearchException ex) {
            throw new ProcessingException("Could not index OCR document", ex);
        }
    }
}
