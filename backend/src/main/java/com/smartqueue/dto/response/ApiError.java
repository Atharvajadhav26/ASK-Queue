package com.smartqueue.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public class ApiError {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> details;

    public ApiError() {}

    public ApiError(int status, String error, String message, LocalDateTime timestamp, Map<String, String> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.details = details;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Map<String, String> getDetails() { return details; }
    public void setDetails(Map<String, String> details) { this.details = details; }
}
