package com.smartcourier.gateway.filter;

import com.smartcourier.gateway.config.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JwtAuthFilter - Spring Cloud Gateway Custom Filter
 *
 * Intercepts ALL protected routes to:
 *  1. Check Authorization header exists
 *  2. Validate JWT signature + expiry
 *  3. Extract userId and role from token claims
 *  4. Forward X-User-Id and X-User-Role headers to downstream services
 *
 * Downstream services TRUST these headers without re-validating JWT.
 * This works because downstream services are on internal network only.
 *
 * Spring Module: Spring Cloud Gateway (WebFlux reactive filter)
 */
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    // Public paths that skip JWT validation (loaded from application.yml)
    @Value("${jwt.public-paths}")
    private String[] publicPaths;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Skip JWT check for public endpoints (login, signup)
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            // Reject immediately if Authorization header is missing
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // Validate Bearer token format
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7); // Strip "Bearer " prefix

            try {
                // Validate JWT - throws JwtException if invalid or expired
                Claims claims = jwtUtil.validateToken(token);

                // Mutate request: add user context headers for downstream services
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", jwtUtil.extractUserId(claims))
                        .header("X-User-Role", jwtUtil.extractRole(claims))
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (JwtException e) {
                // Token is expired, tampered, or invalid
                return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Checks if the incoming path is in the public (no-auth) list.
     */
    private boolean isPublicPath(String path) {
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes an error response and completes the reactive chain.
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    // Required config class for AbstractGatewayFilterFactory
    public static class Config {
        // Configuration properties can be added here if needed
    }
}
