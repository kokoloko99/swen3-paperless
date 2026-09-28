package at.fhtw.swen3.paperless_rest.mapper;

import at.fhtw.swen3.paperless_rest.models.DocumentBl;
import at.fhtw.swen3.paperless_rest.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public Document toDal(DocumentBl bl) {
        Document dal = new Document();
        dal.setId(bl.getId());
        dal.setFilename(bl.getFilename());
        dal.setContentType(bl.getContentType());
        dal.setSize(bl.getSize());
        dal.setUploadedAt(bl.getUploadedAt());
        return dal;
    }

    public DocumentBl toBl(Document dal) {
        DocumentBl bl = new DocumentBl();
        bl.setId(dal.getId());
        bl.setFilename(dal.getFilename());
        bl.setContentType(dal.getContentType());
        bl.setSize(dal.getSize());
        bl.setUploadedAt(dal.getUploadedAt());
        return bl;
    }
}
