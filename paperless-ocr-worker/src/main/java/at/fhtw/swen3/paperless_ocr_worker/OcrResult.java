package at.fhtw.swen3.paperless_ocr_worker;
public record OcrResult(String id, String filename, String contentType, String text) {}
