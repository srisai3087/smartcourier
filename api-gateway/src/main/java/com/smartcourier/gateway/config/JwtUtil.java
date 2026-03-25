package com.smartcourier.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JwtUtil - Gateway-level JWT utility
 *
 * Responsible only for VALIDATING tokens (not generating).
 * Token generation happens in Auth Service.
 * The same jwt.secret must be used in both services.
 */
@Component
public class JwtUtil {

    // Loaded from application.yml - must match Auth Service secret
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Validates the JWT token signature and expiry.
     * Throws JwtException if token is invalid or expired.
     */
    public Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the userId claim from the token payload.
     * This is forwarded downstream as X-User-Id header.
     */
    public String extractUserId(Claims claims) {
        return claims.get("userId", Long.class).toString();
    }

    /**
     * Extracts the user role (ROLE_CUSTOMER / ROLE_ADMIN).
     * This is forwarded downstream as X-User-Role header.
     */
    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }
}
