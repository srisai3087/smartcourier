package com.smartcourier.auth.serviceimpl;

import com.smartcourier.auth.dto.AuthResponse;
import com.smartcourier.auth.dto.LoginRequest;
import com.smartcourier.auth.dto.SignupRequest;
import com.smartcourier.auth.entity.Role;
import com.smartcourier.auth.entity.User;
import com.smartcourier.auth.exception.ResourceNotFoundException;
import com.smartcourier.auth.repository.RoleRepository;
import com.smartcourier.auth.repository.UserRepository;
import com.smartcourier.auth.service.AuthService;
import com.smartcourier.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * AuthServiceImpl - Concrete implementation of AuthService.
 *
 * Placed in 'serviceimpl' package (industry standard).
 * The controller depends on AuthService interface, NOT this class directly.
 *
 * Spring Modules Used:
 *  - @Service: marks as Spring-managed bean (component scan picks it up)
 *  - @Transactional: wraps database operations in a transaction
 *  - @RequiredArgsConstructor: Lombok generates constructor-based DI
 *  - PasswordEncoder: Spring Security BCrypt hashing
 */
@Service
@Slf4j                       // Lombok: generates log field for logging
@RequiredArgsConstructor     // Lombok: generates constructor for all final fields (constructor injection)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Signup Flow:
     *  1. Check if email already registered
     *  2. Hash the password with BCrypt
     *  3. Assign ROLE_CUSTOMER
     *  4. Persist user to auth_db
     *  5. Generate JWT tokens and return
     */
    @Override
    @Transactional  // Ensures the user save and role assignment are atomic
    public AuthResponse signup(SignupRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Prevent duplicate registrations
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }

        // Fetch the default customer role (must exist in roles table)
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_CUSTOMER not found. Please seed the roles table."));

        // Build user entity - password hashed by BCrypt (strength 10 by default)
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .enabled(true)
                .roles(Set.of(customerRole))
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        // Generate JWT with userId and role claims
        String accessToken = jwtService.generateToken(
                savedUser.getId(), savedUser.getEmail(), "ROLE_CUSTOMER");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(UUID.randomUUID().toString()) // Simplified; store in DB for production
                .role("ROLE_CUSTOMER")
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .build();
    }

    /**
     * Login Flow:
     *  1. Find user by email (throws 404 if not found)
     *  2. Verify password using BCrypt matches()
     *  3. Check account is enabled
     *  4. Generate and return JWT tokens
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // BCrypt verify: compares raw password against stored hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.getEnabled()) {
            throw new BadCredentialsException("Account is disabled. Contact support.");
        }

        // Extract primary role (user may have multiple; take first)
        String role = user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("ROLE_CUSTOMER");

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), role);

        log.info("Login successful for userId: {}", user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(UUID.randomUUID().toString())
                .role(role)
                .userId(user.getId())
                .fullName(user.getFullName())
                .build();
    }
}
