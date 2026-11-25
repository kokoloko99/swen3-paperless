package at.fhtw.swen3.paperless_rest.comment.persistence.mapper;

import at.fhtw.swen3.paperless_rest.comment.persistence.entity.Comment;
import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Übersetzt zwischen dem internen Modell und der Datenbank-Entity.
@Mapper(componentModel = "spring")
public interface CommentMapper {

    // Wandelt das Modell in eine Entity mit einer Dokumentzuordnung um.
    @Mapping(target = "document.id", source = "documentId")
    @Mapping(target = "updatedAt", ignore = true)
    Comment toDal(CommentModel model);

    // Wandelt die Entity in ein Modell mit der Dokument-ID um.
    @Mapping(target = "documentId", source = "document.id")
    CommentModel toModel(Comment dal);
}