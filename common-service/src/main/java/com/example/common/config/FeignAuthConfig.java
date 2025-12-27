package com.example.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Optional: adds a static token from ENV to outgoing Feign requests when no request-scope header exists.
 */
@Configuration
public class FeignAuthConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            String token = System.getenv().getOrDefault("FEIGN_ACCESS_TOKEN", "");
            if (!token.isEmpty()) {
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}
