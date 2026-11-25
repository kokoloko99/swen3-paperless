package at.fhtw.swen3.paperless_ocr_worker;
public interface TextExtractor {
    String extract(byte[] content, String contentType);
}
