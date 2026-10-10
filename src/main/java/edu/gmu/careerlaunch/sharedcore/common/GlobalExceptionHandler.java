package edu.gmu.careerlaunch.sharedcore.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Catches errors from ANY controller and returns them in the standard
 * {"error", "code", "detail"} format, so the service never crashes
 * or sends back Java stack traces.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errors our own code throws on purpose (401, 403, ...). */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> apiError(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiError(ex.getMessage(), ex.getCode(), ex.getDetail()));
    }

    /** A required field is missing or invalid, e.g. no email. Lists each bad field. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidFields(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("Invalid input", "VALIDATION_ERROR", fields));
    }

    /** The request body isn't valid JSON (or is missing). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadableBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("Invalid input", "VALIDATION_ERROR", "Request body must be valid JSON"));
    }

    /** The database can't be reached (stopped, wrong password, wrong host). */
    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ApiError> databaseDown(DataAccessResourceFailureException ex) {
        log.error("Database unavailable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("Database unavailable", "SERVICE_DOWN", "Could not reach the database"));
    }

    /** Someone called a URL that doesn't exist. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> notFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("Not found", "NOT_FOUND", "No endpoint at this URL"));
    }

    /** Anything else we didn't expect. Log the details, but don't show them to the caller. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("Something went wrong", "INTERNAL_ERROR", null));
    }
}
