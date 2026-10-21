package at.fhtw.swen3.paperless_rest.comment.mapper;

import at.fhtw.swen3.paperless_rest.comment.dto.CommentDto;
import at.fhtw.swen3.paperless_rest.comment.dto.CreateCommentRequest;
import at.fhtw.swen3.paperless_rest.comment.models.CommentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CommentDtoMapper {

    CommentDto toDto(CommentModel model);

    List<CommentDto> toDtos(List<CommentModel> models);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "documentId", source = "documentId")
    CommentModel toModel(UUID documentId, CreateCommentRequest request);
}