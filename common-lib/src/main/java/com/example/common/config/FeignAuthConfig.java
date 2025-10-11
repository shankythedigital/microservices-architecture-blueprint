
package com.example.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignAuthConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            String token = System.getenv().getOrDefault("ACCESS_TOKEN", "");
            if (!token.isEmpty()) {
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}

