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

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {

        log.info("Registering user: {}", request.getEmail());

        //duplicate check
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }


        String inputRole = request.getRole();

        final String roleName = (inputRole == null ||
                (!inputRole.equals("ROLE_ADMIN") && !inputRole.equals("ROLE_CUSTOMER")))
                ? "ROLE_CUSTOMER"
                : inputRole;

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        // create user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .enabled(true)
                .roles(Set.of(role))
                .build();

        User savedUser = userRepository.save(user);

        // generate JWT
        String token = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                roleName
        );

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(UUID.randomUUID().toString())
                .role(roleName)
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.getEnabled()) {
            throw new BadCredentialsException("Account disabled");
        }

        // get role
        String role = user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("ROLE_CUSTOMER");

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                role
        );

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(UUID.randomUUID().toString())
                .role(role)
                .userId(user.getId())
                .fullName(user.getFullName())
                .build();
    }
}