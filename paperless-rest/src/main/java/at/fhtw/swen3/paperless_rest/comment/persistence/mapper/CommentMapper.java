package at.fhtw.swen3.paperless_rest.comment.persistence.mapper;

import at.fhtw.swen3.paperless_rest.comment.persistence.entity.Comment;
import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "document.id", source = "documentId")
    @Mapping(target = "updatedAt", ignore = true)
    Comment toDal(CommentModel model);

    @Mapping(target = "documentId", source = "document.id")
    CommentModel toModel(Comment dal);
}