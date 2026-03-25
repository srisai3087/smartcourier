package com.smartcourier.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AuthServiceApplication - Microservice Entry Point
 *
 * Responsibilities:
 *  - User registration (CUSTOMER / ADMIN roles)
 *  - Login and JWT access token generation
 *  - Refresh token management
 *
 * Spring Modules Used:
 *  - Spring Boot Web (REST controllers)
 *  - Spring Security (password encoding, auth config)
 *  - Spring Data JPA (user/role persistence)
 *  - Spring Validation (request DTO validation)
 *  - SpringDoc OpenAPI (Swagger UI)
 */
@SpringBootApplication
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
