package at.fhtw.swen3.paperless_rest.common.exception;

import at.fhtw.swen3.paperless_rest.document.exception.DocumentNotFoundException;
import at.fhtw.swen3.paperless_rest.document.exception.InvalidDocumentException;
import at.fhtw.swen3.paperless_rest.comment.exception.InvalidCommentException;
import at.fhtw.swen3.paperless_rest.common.persistence.PersistenceOperationException;
import at.fhtw.swen3.paperless_rest.document.exception.OcrSubmissionException;
import at.fhtw.swen3.paperless_rest.document.controller.DocumentReadException;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Logger logger;

    @ExceptionHandler(OcrSubmissionException.class)
    public ResponseEntity<String> handleOcrSubmission(OcrSubmissionException ex) {
        logger.error("OCR submission failed", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("OCR service unavailable. Please try again later.");
    }

    // Transaction creation/commit can fail outside the persistence adapter methods.
    @ExceptionHandler({PersistenceOperationException.class, DataAccessException.class, TransactionException.class})
    public ResponseEntity<String> handlePersistence(Exception ex) {
        logger.error("Database operation failed", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Document storage unavailable. Please try again later.");
    }

    @ExceptionHandler(DocumentReadException.class)
    public ResponseEntity<String> handleDocumentRead(DocumentReadException ex) {
        logger.error("Uploaded file could not be read", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Could not read the uploaded file.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        logger.warn("Upload exceeded the configured size limit");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("Uploaded file exceeds the allowed size.");
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<String> handleDocumentNotFound(DocumentNotFoundException ex) {
        logger.warn("Document request failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidCommentException.class)
    public ResponseEntity<String> handleInvalidComment(InvalidCommentException ex) {
        logger.warn("Comment request failed validation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidDocumentException.class)
    public ResponseEntity<String> handleInvalidDocument(InvalidDocumentException ex) {
        logger.warn("Document request failed validation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<String> handleBadRequest(Exception ex) {
        logger.warn("Request was invalid: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResource(NoResourceFoundException ex) {
        logger.debug("Resource was not found: {}", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpected(Exception ex) {
        logger.error("Unexpected request failure", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }
}
