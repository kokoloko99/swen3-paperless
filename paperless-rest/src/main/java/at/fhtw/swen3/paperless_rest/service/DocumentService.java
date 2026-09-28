package at.fhtw.swen3.paperless_rest.service;

import at.fhtw.swen3.paperless_rest.models.DocumentBl;

import java.util.UUID;

public interface DocumentService {

    DocumentBl uploadDocument(DocumentBl document);

    DocumentBl getDocument(UUID id);
}
