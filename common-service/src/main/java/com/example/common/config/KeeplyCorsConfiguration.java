package com.example.common.config;

import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

/**
 * Shared CORS policy for Keeply microservices consumed by {@code keeply_app} (Flutter web/mobile)
 * and browsers. Values follow the Fetch spec: with {@code Allow-Credentials: true}, origins must
 * not be {@code *}, and {@code Access-Control-Expose-Headers} must list real response header names
 * (not preflight request header names).
 *
 * <p>Patterns cover: local loopback, HTTPS local, typical LAN (192.168.*), and Android emulator
 * host bridge (10.0.2.2) / common private 10.x dev networks so Flutter web opened from a device or
 * another PC can still call APIs during development.
 */
public final class KeeplyCorsConfiguration {

    private KeeplyCorsConfiguration() {}

    /**
     * Local dev + Flutter web (random ports). Uses origin patterns so reflected
     * {@code Access-Control-Allow-Origin} is valid with credentials.
     */
    public static CorsConfiguration devLocalAndFlutterWeb() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://[::1]:*",
                "https://localhost:*",
                "https://127.0.0.1:*",
                "https://[::1]:*",
                // LAN / device browser hitting host machine APIs
                "http://192.168.*:*",
                "https://192.168.*:*",
                // Android emulator browser → host; common 10.0.x dev LAN
                "http://10.0.2.2:*",
                "https://10.0.2.2:*",
                "http://10.0.*:*",
                "https://10.0.*:*"
        ));
        c.setAllowedMethods(Arrays.asList(
                "GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        // Explicit list: wildcard request headers are invalid with credentials in strict clients.
        c.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Accept-Language",
                "Accept-Encoding",
                "Origin",
                "User-Agent",
                "X-Requested-With",
                "Cache-Control",
                "Pragma",
                "If-None-Match",
                "If-Modified-Since",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        c.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Content-Disposition",
                "Content-Length",
                "ETag",
                "X-Total-Count",
                "X-Request-Id",
                "X-Correlation-Id"
        ));
        c.setAllowCredentials(true);
        c.setMaxAge(3600L);
        return c;
    }
}
