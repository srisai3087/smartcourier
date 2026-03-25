package com.smartcourier.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponse DTO - Returned after successful signup or login.
 *
 * Contains:
 *  - accessToken: JWT (1hr validity) - sent with every API request
 *  - refreshToken: UUID (7-day validity) - used to get new access token
 *  - role: ROLE_CUSTOMER or ROLE_ADMIN - used by frontend for routing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;   // JWT Bearer token
    private String refreshToken;  // UUID refresh token
    private String role;          // e.g. ROLE_CUSTOMER
    private Long userId;          // Used by frontend for scoped API calls
    private String fullName;      // For display in UI dashboard
}
