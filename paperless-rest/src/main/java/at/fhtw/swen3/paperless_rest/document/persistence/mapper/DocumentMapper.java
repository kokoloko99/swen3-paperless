package at.fhtw.swen3.paperless_rest.document.persistence.mapper;

import at.fhtw.swen3.paperless_rest.document.persistence.entity.Document;
import at.fhtw.swen3.paperless_rest.document.models.DocumentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Übersetzt zwischen Dokumentmodell und Datenbank-Entity.
@Mapper(componentModel = "spring")
public interface DocumentMapper {

    // Überträgt die Metadaten aus dem Modell in eine Entity.
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Document toDal(DocumentModel model);

    // Überträgt die gespeicherten Metadaten in ein Modell.
    DocumentModel toModel(Document dal);
}