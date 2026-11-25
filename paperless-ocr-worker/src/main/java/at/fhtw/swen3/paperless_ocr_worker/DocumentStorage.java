package at.fhtw.swen3.paperless_ocr_worker;
public interface DocumentStorage {
    byte[] load(String id);
}
