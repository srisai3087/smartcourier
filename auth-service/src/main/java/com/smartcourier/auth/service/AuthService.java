package com.smartcourier.auth.service;

import com.smartcourier.auth.dto.AuthResponse;
import com.smartcourier.auth.dto.LoginRequest;
import com.smartcourier.auth.dto.SignupRequest;

/**
 * AuthService - Service interface defining the authentication contract.
 *
 * Following the industry-standard Service + ServiceImpl pattern:
 *  - Interface defines WHAT the service does (contract)
 *  - AuthServiceImpl defines HOW it does it (implementation)
 *
 * Benefits:
 *  - Easy to mock in unit tests (Mockito mocks interfaces)
 *  - Allows swapping implementations without changing controllers
 *  - Promotes programming to abstraction (SOLID principle)
 */
public interface AuthService {

    /**
     * Registers a new user with ROLE_CUSTOMER and returns JWT tokens.
     *
     * @param request - validated signup payload
     * @return AuthResponse with accessToken, refreshToken, and role
     */
    AuthResponse signup(SignupRequest request);

    /**
     * Authenticates user credentials and returns JWT tokens.
     *
     * @param request - email and password
     * @return AuthResponse with accessToken, refreshToken, and role
     */
    AuthResponse login(LoginRequest request);
}
