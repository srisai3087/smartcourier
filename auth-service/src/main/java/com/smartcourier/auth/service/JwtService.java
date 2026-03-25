package com.smartcourier.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtService - Handles JWT token lifecycle.
 * <p>
 * Responsibilities:
 * 1. Generate access tokens with user claims embedded
 * 2. Validate token signature and expiry
 * 3. Extract claims (userId, role, email) from tokens
 * <p>
 * Spring Module: No Spring module - plain service using JJWT library.
 * Secret key must match the one configured in api-gateway/application.yml.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationSeconds;

    /**
     * Generates a signed JWT access token for the authenticated user.
     * <p>
     * Claims embedded in the token:
     * - sub: user email (standard JWT subject)
     * - userId: database PK (used by downstream services)
     * - role: ROLE_CUSTOMER or ROLE_ADMIN
     * - iat: issued-at timestamp
     * - exp: expiry timestamp
     */
    public String generateToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(email)                        // Standard JWT subject claim
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                .signWith(getSigningKey())             // HS256 signing
                .compact();
    }

    /**
     * Validates token and returns all claims.
     * Throws JwtException if token is tampered or expired.
     */
    public Claims validateAndExtract(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Builds the HMAC-SHA256 signing key from the configured secret.
     * Key must be at least 256 bits (32 chars).
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
