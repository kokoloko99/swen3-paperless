package at.fhtw.swen3.paperless_rest.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class DocumentBl {

    private UUID id;
    private String filename;
    private String contentType;
    private long size;
    private Instant uploadedAt;
}
