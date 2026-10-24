package com.snapcompile.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * JwtAuthFilter - Runs on every HTTP request
 *
 * It checks the Authorization header for a JWT token.
 * If valid, it sets the user as authenticated for this request.
 *
 * Flow:
 * Request → Filter reads "Authorization: Bearer <token>"
 *         → Validate token
 *         → Load user from DB
 *         → Set authentication in SecurityContext
 *         → Controller runs with authenticated user
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Get Authorization header - format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
        String authHeader = request.getHeader("Authorization");

        // If no Authorization header or it doesn't start with "Bearer ", skip
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token part (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // Extract email from token
        String email = jwtUtil.extractEmail(token);

        // If email found and user not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load full user details from DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Validate token against user details
            if (jwtUtil.isTokenValid(token)) {
                // Create authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authenticated user in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue to next filter / controller
        filterChain.doFilter(request, response);
    }
}
