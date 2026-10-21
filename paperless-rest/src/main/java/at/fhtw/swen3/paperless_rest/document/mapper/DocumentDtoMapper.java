package at.fhtw.swen3.paperless_rest.document.mapper;

import at.fhtw.swen3.paperless_rest.document.dto.DocumentDto;
import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.web.multipart.MultipartFile;

@Mapper(componentModel = "spring")
public interface DocumentDtoMapper {

    DocumentDto toDto(DocumentModel model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "filename", source = "originalFilename")
    DocumentModel toModel(MultipartFile file);
}