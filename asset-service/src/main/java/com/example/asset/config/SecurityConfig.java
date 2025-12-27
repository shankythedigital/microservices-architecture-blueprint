package com.example.asset.config;
import com.example.common.security.JwtAuthFilter;
import com.example.common.security.JwtVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ✅ Security configuration for Notification Service.
 * Uses shared JwtVerifier (from common module) to validate tokens
 * using either RSA public key or HMAC secret.
 */
@Configuration
public class SecurityConfig {

    private final JwtVerifier jwtVerifier;

    public SecurityConfig(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Custom JWT validation filter
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtVerifier);

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Asset API endpoints require authentication
                .requestMatchers("/api/asset/**").authenticated()
                // Permit actuator/health or any public endpoints if needed
                .requestMatchers("/actuator/**").permitAll()
                // Swagger/OpenAPI endpoints
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                .anyRequest().permitAll()
            )

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


