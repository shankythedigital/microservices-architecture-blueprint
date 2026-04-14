package com.example.helpdesk.config;

import com.example.common.config.KeeplyCorsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS for Flutter web / local dev; policy is defined in {@link KeeplyCorsConfiguration}.
 * Replaces {@code allowedOrigins("*")} which is invalid for credentialed requests (e.g. JWT).
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", KeeplyCorsConfiguration.devLocalAndFlutterWeb());
        return source;
    }
}
