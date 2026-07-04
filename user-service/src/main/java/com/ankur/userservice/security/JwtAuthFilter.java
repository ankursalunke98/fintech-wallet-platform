package com.ankur.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Get Authorization header from REQUEST (not response)
        String header = request.getHeader("Authorization");

        // Step 2: If no header or doesn't start with "Bearer " → pass through
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Strip "Bearer " prefix (7 characters) to get raw token
        String token = header.substring(7);

        // Step 4: Extract username FROM THE TOKEN using JwtService
        String username = jwtService.extractUsername(token);

        // Step 5: If username found AND nobody authenticated yet this request
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user from DB
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            // Validate token against user
            if (jwtService.isTokenValid(token, userDetails)) {

                // Create authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Add request details to auth token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // Set in SecurityContext — marks this request as authenticated
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }

        // Step 6: Always continue the filter chain
        filterChain.doFilter(request, response);
    }
}