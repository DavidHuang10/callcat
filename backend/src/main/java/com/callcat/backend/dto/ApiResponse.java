package com.callcat.backend.dto;

public class ApiResponse {
    
    private String message;
    private boolean success;
    
    public ApiResponse(String message) {
        this.message = message;
        this.success = true;
    }
    
    public ApiResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
    
    public static ApiResponse success(String message) {
        return new ApiResponse(message, true);
    }
    
    public static ApiResponse error(String message) {
        return new ApiResponse(message, false);
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}