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

// Prüft den Persistenzadapter mit einem simulierten Repository und echtem Mapper.
@ExtendWith(MockitoExtension.class)
class DocumentPersistenceAdapterTest {

    @Mock
    private DocumentRepository documentRepository;

    private DocumentPersistenceAdapter adapter;

    // Prüft, dass der Adapter den Löschauftrag an das Repository weitergibt.
    @Test
    void deleteDelegatesToRepository() {
        UUID id = UUID.randomUUID();
        adapter.deleteById(id);
        org.mockito.Mockito.verify(documentRepository).deleteById(id);
    }

    // Erstellt den Adapter vor jedem Test ohne echte Datenbank.
    @BeforeEach
    void setUp() {
        adapter = new DocumentPersistenceAdapter(
                documentRepository,
                Mappers.getMapper(DocumentMapper.class),
                LoggerFactory.getLogger(DocumentPersistenceAdapter.class));
    }

    // Prüft die Umwandlung beim Speichern und die zurückgegebenen Daten.
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

    // Prüft die Umwandlung eines gefundenen Datenbankeintrags in ein Modell.
    @Test
    void findByIdMapsEntityToModel() {
        UUID id = UUID.randomUUID();
        Document document = new Document();
        document.setId(id);
        document.setFilename("a.pdf");
        when(documentRepository.findById(id)).thenReturn(Optional.of(document));

        assertThat(adapter.findById(id)).get().extracting(DocumentModel::getFilename).isEqualTo("a.pdf");
    }

    // Prüft die Übersetzung eines Datenbankfehlers und den Erhalt seiner Ursache.
    @Test
    void translatesDatabaseFailureAndPreservesCause() {
        var failure = new org.springframework.dao.DataAccessResourceFailureException("Database offline");
        when(documentRepository.findById(any())).thenThrow(failure);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> adapter.findById(UUID.randomUUID()))
                .isInstanceOf(at.fhtw.swen3.paperless_rest.common.persistence.PersistenceOperationException.class)
                .hasCause(failure);
    }

    // Prüft, dass die Existenzprüfung an das Repository weitergegeben wird.
    @Test
    void existsByIdDelegates() {
        UUID id = UUID.randomUUID();
        when(documentRepository.existsById(id)).thenReturn(true);

        assertThat(adapter.existsById(id)).isTrue();
    }
}
