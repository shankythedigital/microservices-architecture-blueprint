package com.example.asset.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Optional;

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

    public static String getUserIdOrThrow() {
        return getUserId().orElseThrow(() -> new RuntimeException("No authenticated user"));
    }
    public static String getUsernameOrThrow() {
        return getUsername().orElse("system");
    }
}
