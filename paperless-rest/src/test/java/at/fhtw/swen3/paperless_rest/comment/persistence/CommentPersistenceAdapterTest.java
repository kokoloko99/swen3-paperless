package at.fhtw.swen3.paperless_rest.comment.persistence;

import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import at.fhtw.swen3.paperless_rest.comment.persistence.entity.Comment;
import at.fhtw.swen3.paperless_rest.comment.persistence.mapper.CommentMapper;
import at.fhtw.swen3.paperless_rest.comment.persistence.repository.CommentRepository;
import at.fhtw.swen3.paperless_rest.document.persistence.entity.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentPersistenceAdapterTest {

    private static final UUID DOCUMENT_ID = UUID.randomUUID();

    @Mock
    private CommentRepository commentRepository;

    private CommentPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CommentPersistenceAdapter(
                commentRepository,
                Mappers.getMapper(CommentMapper.class),
                LoggerFactory.getLogger(CommentPersistenceAdapter.class));
    }

    private Comment entity(UUID id) {
        Document document = new Document();
        document.setId(DOCUMENT_ID);
        Comment comment = new Comment();
        comment.setId(id);
        comment.setDocument(document);
        comment.setAuthor("Alice");
        comment.setText("Hi");
        return comment;
    }

    @Test
    void saveMapsModelToEntityAndBack() {
        UUID id = UUID.randomUUID();
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(id);
            return comment;
        });
        CommentModel model = new CommentModel();
        model.setDocumentId(DOCUMENT_ID);
        model.setAuthor("Alice");

        CommentModel saved = adapter.save(model);

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getDocumentId()).isEqualTo(DOCUMENT_ID);
    }

    @Test
    void findByIdMapsEntityToModel() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findById(id)).thenReturn(Optional.of(entity(id)));

        assertThat(adapter.findById(id)).get().extracting(CommentModel::getDocumentId).isEqualTo(DOCUMENT_ID);
    }

    @Test
    void findByDocumentIdMapsAll() {
        when(commentRepository.findByDocument_Id(DOCUMENT_ID)).thenReturn(List.of(entity(UUID.randomUUID())));

        List<CommentModel> result = adapter.findByDocumentId(DOCUMENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("Alice");
    }

    @Test
    void deleteByIdDelegates() {
        UUID id = UUID.randomUUID();

        adapter.deleteById(id);

        verify(commentRepository).deleteById(id);
    }
}