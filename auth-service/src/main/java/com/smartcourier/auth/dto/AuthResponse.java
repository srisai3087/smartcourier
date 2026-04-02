package com.smartcourier.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
