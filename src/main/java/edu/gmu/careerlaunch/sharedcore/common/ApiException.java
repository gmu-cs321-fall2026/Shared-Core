package edu.gmu.careerlaunch.sharedcore.common;

import org.springframework.http.HttpStatus;

/**
 * Throw this from any code to send back a standard error.
 * Example: throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid email or password", null);
 * GlobalExceptionHandler turns it into {"error", "code", "detail"} JSON.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final Object detail;

    public ApiException(HttpStatus status, String code, String error, Object detail) {
        super(error);
        this.status = status;
        this.code = code;
        this.detail = detail;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public Object getDetail() { return detail; }
}
