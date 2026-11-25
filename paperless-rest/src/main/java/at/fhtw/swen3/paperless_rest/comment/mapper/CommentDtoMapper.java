package at.fhtw.swen3.paperless_rest.comment.mapper;

import at.fhtw.swen3.paperless_rest.comment.dto.CommentDto;
import at.fhtw.swen3.paperless_rest.comment.dto.CreateCommentRequest;
import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

// Übersetzt zwischen den API-Daten und dem internen Kommentarmodell.
@Mapper(componentModel = "spring")
public interface CommentDtoMapper {

    // Wandelt einen Kommentar in die Daten für die HTTP-Antwort um.
    CommentDto toDto(CommentModel model);

    // Wandelt eine Liste von Kommentaren in Antwortdaten um.
    List<CommentDto> toDtos(List<CommentModel> models);

    // Fasst die Dokument-ID und die Anfragedaten zu einem Kommentarmodell zusammen.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "documentId", source = "documentId")
    CommentModel toModel(UUID documentId, CreateCommentRequest request);
}