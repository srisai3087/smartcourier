package com.smartcourier.delivery.exception;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "Not Found", ex.getMessage(), req));
    }
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleTransition(InvalidStatusTransitionException ex, WebRequest req) {
        return ResponseEntity.status(422).body(build(422, "Unprocessable Entity", ex.getMessage(), req));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String,String> fields = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e -> fields.put(((FieldError)e).getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("status",400,"error","Validation Failed","errors",fields));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest req) {
        return ResponseEntity.status(500).body(build(500, "Internal Server Error", "Unexpected error occurred.", req));
    }
    private ErrorResponse build(int status, String error, String msg, WebRequest req) {
        return ErrorResponse.builder().timestamp(LocalDateTime.now()).status(status)
            .error(error).message(msg).path(req.getDescription(false).replace("uri=","")).build();
    }
}
