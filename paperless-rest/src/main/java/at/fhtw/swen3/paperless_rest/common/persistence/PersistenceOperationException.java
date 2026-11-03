package at.fhtw.swen3.paperless_rest.common.persistence;

/** Persistence boundary: prevents repository-specific exceptions leaking out. */
public class PersistenceOperationException extends RuntimeException {
    public PersistenceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
