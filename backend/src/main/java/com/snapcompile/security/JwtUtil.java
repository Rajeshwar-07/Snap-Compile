package com.snapcompile.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JwtUtil - Handles JWT token creation and validation
 *
 * JWT Flow:
 * 1. User logs in → we create a token with their email inside
 * 2. Frontend stores token in localStorage
 * 3. Every API request → frontend sends token in Authorization header
 * 4. We validate the token and extract user email
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;  // 86400000 ms = 24 hours

    // Create signing key from secret string
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate a JWT token for a user
     * @param email - user's email (stored inside token)
     * @return JWT token string
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)                                           // Store email in token
                .setIssuedAt(new Date())                                     // Token creation time
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))  // Expiry
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)         // Sign with secret
                .compact();
    }

    /**
     * Extract email from JWT token
     * @param token - JWT string from Authorization header
     * @return email stored inside the token
     */
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Check if token is valid and not expired
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
