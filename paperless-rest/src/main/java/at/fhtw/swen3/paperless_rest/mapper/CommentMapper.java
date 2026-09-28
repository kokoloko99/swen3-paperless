package at.fhtw.swen3.paperless_rest.mapper;

import at.fhtw.swen3.paperless_rest.models.CommentBl;
import at.fhtw.swen3.paperless_rest.entity.Comment;
import at.fhtw.swen3.paperless_rest.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public Comment toDal(CommentBl bl, Document document) {
        Comment dal = new Comment();
        dal.setId(bl.getId());
        dal.setDocument(document);
        dal.setAuthor(bl.getAuthor());
        dal.setText(bl.getText());
        dal.setCreatedAt(bl.getCreatedAt());
        return dal;
    }

    public CommentBl toBl(Comment dal) {
        CommentBl bl = new CommentBl();
        bl.setId(dal.getId());
        bl.setDocumentId(dal.getDocument().getId());
        bl.setAuthor(dal.getAuthor());
        bl.setText(dal.getText());
        bl.setCreatedAt(dal.getCreatedAt());
        return bl;
    }
}
