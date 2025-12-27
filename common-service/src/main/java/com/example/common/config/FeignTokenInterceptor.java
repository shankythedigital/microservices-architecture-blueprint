package com.example.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * When services call other services using Feign, this interceptor relays the incoming Authorization header.
 * To use: annotate Feign clients with @Import(FeignTokenInterceptor.class) or add to component scan.
 */
@Configuration
public class FeignTokenInterceptor {
    @Bean
    public RequestInterceptor tokenRelay() {
        return (RequestTemplate template) -> {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String auth = attrs.getRequest().getHeader("Authorization");
                if (auth != null && !auth.isEmpty()) {
                    template.header("Authorization", auth);
                }
            }
        };
    }
}
