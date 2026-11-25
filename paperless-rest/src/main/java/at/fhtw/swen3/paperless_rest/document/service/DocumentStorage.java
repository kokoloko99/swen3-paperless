package at.fhtw.swen3.paperless_rest.document.service;
public interface DocumentStorage {
    void store(String id, byte[] content, String contentType);
}
