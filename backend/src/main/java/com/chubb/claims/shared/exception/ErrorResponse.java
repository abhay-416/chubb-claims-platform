package com.chubb.claims.shared.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> validationErrors;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, Map<String, String> validationErrors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.validationErrors = validationErrors;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, String> getValidationErrors() { return validationErrors; }
}
