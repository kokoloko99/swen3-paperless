package at.fhtw.swen3.paperless_rest.document.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// Enthält die Dokumentdaten, mit denen der Service arbeitet.
@Getter
@Setter
@NoArgsConstructor
public class DocumentModel {

    private UUID id;
    private String filename;
    private String contentType;
    private long size;
    private Instant uploadedAt;
}
