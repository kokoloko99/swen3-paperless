package at.fhtw.swen3.paperless_rest.comment.persistence.entity;

import at.fhtw.swen3.paperless_rest.document.persistence.entity.Document;

import at.fhtw.swen3.paperless_rest.common.persistence.entity.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment extends AuditableEntity {

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    private String author;

    private String text;
}
