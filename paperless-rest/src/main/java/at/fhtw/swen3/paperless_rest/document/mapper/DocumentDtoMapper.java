package at.fhtw.swen3.paperless_rest.document.mapper;

import at.fhtw.swen3.paperless_rest.document.dto.DocumentDto;
import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.web.multipart.MultipartFile;

// Übersetzt zwischen Upload-Datei, internem Modell und Antwortdaten.
@Mapper(componentModel = "spring")
public interface DocumentDtoMapper {

    // Wandelt die Metadaten in die Antwort für den Browser um.
    DocumentDto toDto(DocumentModel model);

    // Übernimmt Dateiname, Typ und Größe der Upload-Datei in das Modell.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "filename", source = "originalFilename")
    DocumentModel toModel(MultipartFile file);
}