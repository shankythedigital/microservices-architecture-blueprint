package com.example.helpdesk.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class JwtUtil {
    private JwtUtil() {}

    public static Optional<String> getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Optional.empty();
        }
        // Handle JwtAuthenticationToken (OAuth2)
        if (auth instanceof JwtAuthenticationToken jwt) {
            return Optional.ofNullable(jwt.getToken().getSubject());
        }
        // Handle UsernamePasswordAuthenticationToken (from common JwtAuthFilter)
        if (auth instanceof UsernamePasswordAuthenticationToken) {
            Object principal = auth.getPrincipal();
            if (principal instanceof String) {
                return Optional.of((String) principal);
            }
            if (principal != null) {
                return Optional.of(principal.toString());
            }
        }
        // Fallback: try to get principal as string
        if (auth.getPrincipal() != null) {
            return Optional.of(auth.getPrincipal().toString());
        }
        return Optional.empty();
    }

    public static Optional<String> getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Optional.empty();
        }
        // Handle JwtAuthenticationToken (OAuth2)
        if (auth instanceof JwtAuthenticationToken jwt) {
            Object u = jwt.getToken().getClaim("preferred_username");
            if (u==null) u = jwt.getToken().getClaim("username");
            return Optional.ofNullable(u!=null?u.toString():null);
        }
        // Handle UsernamePasswordAuthenticationToken (from common JwtAuthFilter)
        if (auth instanceof UsernamePasswordAuthenticationToken) {
            // For UsernamePasswordAuthenticationToken, principal is typically the userId
            // Try to get username from credentials or return principal as fallback
            Object principal = auth.getPrincipal();
            if (principal != null) {
                return Optional.of(principal.toString());
            }
        }
        // Fallback: try to get principal as string
        if (auth.getPrincipal() != null) {
            return Optional.of(auth.getPrincipal().toString());
        }
        return Optional.empty();
    }

    public static List<String> getRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return List.of();
        }
        // Handle JwtAuthenticationToken (OAuth2)
        if (auth instanceof JwtAuthenticationToken jwt) {
            Object rolesObj = jwt.getToken().getClaim("roles");
            if (rolesObj instanceof List) {
                return ((List<?>) rolesObj).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }
        // Handle UsernamePasswordAuthenticationToken (from common JwtAuthFilter)
        // Get roles from authorities
        if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public static boolean isAdmin() {
        List<String> roles = getRoles();
        return roles.stream()
                .anyMatch(role -> role.equalsIgnoreCase("ROLE_ADMIN") || 
                                 role.equalsIgnoreCase("ADMIN"));
    }

    public static String getUserIdOrThrow() {
        return getUserId().orElseThrow(() -> new RuntimeException("No authenticated user"));
    }
    
    public static String getUsernameOrThrow() {
        return getUsername().orElse("system");
    }
}

