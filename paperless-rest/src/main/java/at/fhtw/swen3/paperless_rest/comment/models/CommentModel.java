package at.fhtw.swen3.paperless_rest.comment.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CommentModel {

    private UUID id;
    private UUID documentId;
    private String author;
    private String text;
    private Instant createdAt;
}
