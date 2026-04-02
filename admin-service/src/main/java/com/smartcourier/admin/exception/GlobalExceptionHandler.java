package com.smartcourier.admin.exception;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest; import java.time.LocalDateTime;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest req) {
        return ResponseEntity.status(404).body(ErrorResponse.builder().timestamp(LocalDateTime.now())
            .status(404).error("Not Found").message(ex.getMessage())
            .path(req.getDescription(false).replace("uri=","")).build());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest req) {
        return ResponseEntity.status(500).body(ErrorResponse.builder().timestamp(LocalDateTime.now())
            .status(500).error("Internal Server Error").message("Unexpected error occurred.")
            .path(req.getDescription(false).replace("uri=","")).build());
    }
}
