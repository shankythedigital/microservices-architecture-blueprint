package com.example.asset.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuditInterceptor auditInterceptor;

    @Autowired
    public WebMvcConfig(AuditInterceptor auditInterceptor){ this.auditInterceptor = auditInterceptor; }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**","/error");
    }
}
