package at.fhtw.swen3.paperless_indexing_worker;
public record IndexedDocument(String id, String filename, String contentType, String text) {}
