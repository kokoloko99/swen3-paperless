package at.fhtw.swen3.paperless_rest.document.persistence;

import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import at.fhtw.swen3.paperless_rest.document.persistence.entity.Document;
import at.fhtw.swen3.paperless_rest.document.persistence.mapper.DocumentMapper;
import at.fhtw.swen3.paperless_rest.document.persistence.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentPersistenceAdapterTest {

    @Mock
    private DocumentRepository documentRepository;

    private DocumentPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DocumentPersistenceAdapter(
                documentRepository,
                Mappers.getMapper(DocumentMapper.class),
                LoggerFactory.getLogger(DocumentPersistenceAdapter.class));
    }

    @Test
    void saveMapsModelToEntityAndBack() {
        UUID id = UUID.randomUUID();
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            document.setId(id);
            return document;
        });
        DocumentModel model = new DocumentModel();
        model.setFilename("a.pdf");

        DocumentModel saved = adapter.save(model);

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getFilename()).isEqualTo("a.pdf");
    }

    @Test
    void findByIdMapsEntityToModel() {
        UUID id = UUID.randomUUID();
        Document document = new Document();
        document.setId(id);
        document.setFilename("a.pdf");
        when(documentRepository.findById(id)).thenReturn(Optional.of(document));

        assertThat(adapter.findById(id)).get().extracting(DocumentModel::getFilename).isEqualTo("a.pdf");
    }

    @Test
    void existsByIdDelegates() {
        UUID id = UUID.randomUUID();
        when(documentRepository.existsById(id)).thenReturn(true);

        assertThat(adapter.existsById(id)).isTrue();
    }
}