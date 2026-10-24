package com.snapcompile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO returned after successful login/register
 * Contains JWT token and basic user info
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;        // JWT token - frontend stores this in localStorage
    private String username;
    private String email;
    private Long userId;
}
