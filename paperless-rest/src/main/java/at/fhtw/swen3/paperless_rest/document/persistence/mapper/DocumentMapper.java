package at.fhtw.swen3.paperless_rest.document.persistence.mapper;

import at.fhtw.swen3.paperless_rest.document.persistence.entity.Document;
import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Document toDal(DocumentModel model);

    DocumentModel toModel(Document dal);
}