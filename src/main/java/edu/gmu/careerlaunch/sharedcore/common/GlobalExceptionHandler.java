package edu.gmu.careerlaunch.sharedcore.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
