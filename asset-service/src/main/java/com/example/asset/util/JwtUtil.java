package com.example.asset.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class JwtUtil {
    private JwtUtil() {}

    public static Optional<String> getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            return Optional.ofNullable(jwt.getToken().getSubject());
        }
        return Optional.empty();
    }

    public static Optional<String> getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            Object u = jwt.getToken().getClaim("preferred_username");
            if (u==null) u = jwt.getToken().getClaim("username");
            return Optional.ofNullable(u!=null?u.toString():null);
        }
        return Optional.empty();
    }

    public static List<String> getRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            // Try to get roles from JWT claims
            Object rolesObj = jwt.getToken().getClaim("roles");
            if (rolesObj instanceof List) {
                return ((List<?>) rolesObj).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
            // Fallback: get from authorities
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }
        // Fallback: get from authorities
        if (auth != null) {
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
