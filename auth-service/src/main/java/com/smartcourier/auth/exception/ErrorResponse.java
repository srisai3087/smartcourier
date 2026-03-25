package com.smartcourier.auth.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ErrorResponse - Standard error payload returned by GlobalExceptionHandler.
 *
 * All error responses follow this consistent structure so the frontend
 * and API consumers can reliably parse error details.
 */
@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
