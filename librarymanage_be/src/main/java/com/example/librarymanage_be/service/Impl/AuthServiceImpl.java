package com.example.librarymanage_be.service.Impl;

import com.example.librarymanage_be.dto.request.LoginRequest;
import com.example.librarymanage_be.dto.request.RegisterRequest;
import com.example.librarymanage_be.dto.response.AuthResponse;
import com.example.librarymanage_be.entity.Role;
import com.example.librarymanage_be.entity.User;
import com.example.librarymanage_be.repo.RoleRepository;
import com.example.librarymanage_be.repo.UserRepository;
import com.example.librarymanage_be.service.AuthService;
import com.example.librarymanage_be.service.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(email);
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.getRoles().add(getDefaultRole());

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser.getEmail());
        return new AuthResponse(token, "Bearer", savedUser.getUserId(), savedUser.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, "Bearer", user.getUserId(), user.getEmail());
    }

    private Role getDefaultRole() {
        return roleRepository.findByRoleName(DEFAULT_ROLE)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(DEFAULT_ROLE);
                    return roleRepository.save(role);
                });
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        return email.trim().toLowerCase();
    }
}
