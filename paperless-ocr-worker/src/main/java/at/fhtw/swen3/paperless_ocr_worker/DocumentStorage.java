package at.fhtw.swen3.paperless_ocr_worker;
public interface DocumentStorage {
    void store(String id, byte[] content, String contentType);
}
