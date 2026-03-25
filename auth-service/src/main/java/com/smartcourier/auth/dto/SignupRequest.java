package com.smartcourier.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * SignupRequest DTO - Data Transfer Object for user registration.
 *
 * @Valid on the controller method triggers these Bean Validation constraints.
 * Spring Module: Spring Validation (spring-boot-starter-validation)
 */
@Data
public class SignupRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Optional - used for SMS notifications in future
    private String phone;
}
