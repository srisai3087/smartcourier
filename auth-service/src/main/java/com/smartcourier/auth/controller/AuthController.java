package com.smartcourier.auth.controller;

import com.smartcourier.auth.dto.AuthResponse;
import com.smartcourier.auth.dto.LoginRequest;
import com.smartcourier.auth.dto.SignupRequest;
import com.smartcourier.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST Controller for authentication endpoints.
 *
 * All routes are PUBLIC (no JWT required).
 * These endpoints are accessible via: /gateway/auth/** through the API Gateway.
 *
 * Spring Modules Used:
 *  - @RestController: combines @Controller + @ResponseBody (returns JSON)
 *  - @RequestMapping: maps HTTP paths to handler methods
 *  - @Valid: triggers Bean Validation on @RequestBody
 *  - ResponseEntity: gives full control over HTTP status codes and headers
 *
 * Swagger UI: http://localhost:8081/swagger-ui.html
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login APIs")
public class AuthController {

    // Depends on interface, not implementation (SOLID - Dependency Inversion)
    private final AuthService authService;

    /**
     * POST /auth/signup
     * Register a new user. Assigns ROLE_CUSTOMER by default.
     * Returns 201 Created with JWT tokens.
     */
    @PostMapping("/signup")
    @Operation(summary = "Register new user", description = "Creates a new CUSTOMER account and returns JWT tokens")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /auth/login
     * Authenticate with email and password.
     * Returns 200 OK with JWT tokens on success.
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Validates credentials and returns JWT access + refresh tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /auth/health
     * Simple health check endpoint. No auth required.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}
