package com.example.notification.config;

import com.example.notification.service.AuthTokenService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    private final AuthTokenService tokenService;

    public FeignConfig(AuthTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String token = tokenService.getAccessToken();
                if (token != null) {
                    template.header("Authorization", "Bearer " + token);
                }
            }
        };
    }
}
