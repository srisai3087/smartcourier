package com.smartcourier.auth;

import com.smartcourier.auth.dto.AuthResponse;
import com.smartcourier.auth.dto.LoginRequest;
import com.smartcourier.auth.dto.SignupRequest;
import com.smartcourier.auth.entity.Role;
import com.smartcourier.auth.entity.User;
import com.smartcourier.auth.repository.RoleRepository;
import com.smartcourier.auth.repository.UserRepository;
import com.smartcourier.auth.service.JwtService;
import com.smartcourier.auth.serviceimpl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role customerRole;
    private User existingUser;

    @BeforeEach
    void setUp() {
        customerRole = new Role(1L, "ROLE_CUSTOMER");

        existingUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .password("$2a$10$hashedPassword")
                .enabled(true)
                .roles(Set.of(customerRole))
                .build();
    }

    // -----------------------------------------------------------------------
    // SIGNUP TESTS
    // -----------------------------------------------------------------------

    @Test
    void signup_shouldReturnAuthResponse_whenEmailIsNew() {
        // ARRANGE
        SignupRequest request = new SignupRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(jwtService.generateToken(anyLong(), anyString(), anyString())).thenReturn("jwt-token");

        // ACT
        AuthResponse response = authService.signup(request);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("ROLE_CUSTOMER");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signup_shouldThrowException_whenEmailAlreadyExists() {
        // ARRANGE
        SignupRequest request = new SignupRequest();
        request.setEmail("john@example.com");
        request.setPassword("pass");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    // -----------------------------------------------------------------------
    // LOGIN TESTS
    // -----------------------------------------------------------------------

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsValid() {
        // ARRANGE
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("rawPassword");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("rawPassword", existingUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(anyLong(), anyString(), anyString())).thenReturn("valid-jwt");

        // ACT
        AuthResponse response = authService.login(request);

        // ASSERT
        assertThat(response.getAccessToken()).isEqualTo("valid-jwt");
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void login_shouldThrowBadCredentials_whenPasswordWrong() {
        // ARRANGE
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrongPass");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPass", existingUser.getPassword())).thenReturn(false);

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_shouldThrowBadCredentials_whenEmailNotFound() {
        // ARRANGE
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("pass");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
