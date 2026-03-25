package com.smartcourier.auth.exception;

/**
 * ResourceNotFoundException - Thrown when a requested entity is not found.
 * Maps to HTTP 404 Not Found in GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
