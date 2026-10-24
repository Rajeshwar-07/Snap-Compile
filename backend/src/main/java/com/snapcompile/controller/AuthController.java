package com.snapcompile.controller;

import com.snapcompile.dto.AuthResponse;
import com.snapcompile.dto.LoginRequest;
import com.snapcompile.dto.RegisterRequest;
import com.snapcompile.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - Handles login and register API endpoints
 *
 * Base URL: /api/auth
 * These endpoints are PUBLIC (no JWT needed)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Register a new user
     *
     * Request body: { "username": "john", "email": "john@gmail.com", "password": "123456" }
     * Response: { "token": "eyJ...", "username": "john", "email": "john@gmail.com", "userId": 1 }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/login
     * Login existing user
     *
     * Request body: { "email": "john@gmail.com", "password": "123456" }
     * Response: { "token": "eyJ...", "username": "john", "email": "john@gmail.com", "userId": 1 }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
