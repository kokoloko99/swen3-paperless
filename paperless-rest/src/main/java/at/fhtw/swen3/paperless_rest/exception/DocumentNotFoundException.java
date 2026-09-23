package at.fhtw.swen3.paperless_rest.exception;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(Long id) {
        super("Document not found: " + id);
    }
}
