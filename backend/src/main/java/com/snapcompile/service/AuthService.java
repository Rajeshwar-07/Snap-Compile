package com.snapcompile.service;

import com.snapcompile.dto.AuthResponse;
import com.snapcompile.dto.LoginRequest;
import com.snapcompile.dto.RegisterRequest;
import com.snapcompile.model.User;
import com.snapcompile.repository.UserRepository;
import com.snapcompile.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService - Handles user registration and login logic
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user
     * Steps:
     * 1. Check if email/username already exists
     * 2. Hash the password using BCrypt
     * 3. Save user to database
     * 4. Return JWT token + user info
     */
    public AuthResponse register(RegisterRequest request) {

        // Check email not already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Check username not already taken
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Build and save new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // Hash password
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT token for the new user
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return new AuthResponse(token, savedUser.getUsername(), savedUser.getEmail(), savedUser.getId());
    }

    /**
     * Login an existing user
     * Steps:
     * 1. Spring Security authenticates email + password
     * 2. If valid, generate JWT token
     * 3. Return token + user info
     */
    public AuthResponse login(LoginRequest request) {

        // This throws an exception if credentials are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Credentials are correct - load user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate token
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getId());
    }
}
