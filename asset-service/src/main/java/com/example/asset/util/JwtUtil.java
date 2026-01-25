package com.example.asset.util;

import io.jsonwebtoken.Claims;
import com.example.common.security.JwtVerifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class JwtUtil {
    private static JwtVerifier jwtVerifier;
    
    private JwtUtil() {}
    
    // Set JwtVerifier via static method (called from configuration)
    public static void setJwtVerifier(JwtVerifier verifier) {
        jwtVerifier = verifier;
    }

    public static Optional<String> getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            return Optional.ofNullable(jwt.getToken().getSubject());
        }
        // Handle UsernamePasswordAuthenticationToken (set by JwtAuthFilter)
        if (auth instanceof UsernamePasswordAuthenticationToken upat) {
            Object principal = upat.getPrincipal();
            if (principal instanceof String) {
                return Optional.of((String) principal);
            }
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
        // Handle UsernamePasswordAuthenticationToken (set by JwtAuthFilter)
        if (auth instanceof UsernamePasswordAuthenticationToken upat) {
            Object credentials = upat.getCredentials();
            if (credentials instanceof String token && jwtVerifier != null) {
                try {
                    Claims claims = jwtVerifier.parseToken(token).getBody();
                    Object u = claims.get("preferred_username");
                    if (u == null) u = claims.get("username");
                    return Optional.ofNullable(u != null ? u.toString() : null);
                } catch (Exception e) {
                    // If token parsing fails, return empty
                    return Optional.empty();
                }
            }
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
