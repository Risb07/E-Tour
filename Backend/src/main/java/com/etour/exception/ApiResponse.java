package com.etour.exception;
import java.time.LocalDateTime;

public class ApiResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private String path;


    public ApiResponse(
            LocalDateTime timestamp,
            int status,
            String message,
            String path) {

        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.path = path;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }


    public int getStatus() {
        return status;
    }


    public String getMessage() {
        return message;
    }


    public String getPath() {
        return path;
    }
}