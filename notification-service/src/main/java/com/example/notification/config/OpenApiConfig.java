package com.example.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ✅ OpenAPI Configuration for Notification Service
 * Configures Swagger/OpenAPI documentation with security schemes and API information.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("""
                                Notification Service API
                                
                                Comprehensive notification management including:
                                - Email notifications
                                - SMS notifications
                                - WhatsApp notifications
                                - In-app notifications
                                - Notification templates
                                - Notification history and tracking
                                
                                ## Authentication
                                Most endpoints require authentication using Bearer tokens. 
                                Obtain a token from the Auth Service `/api/auth/login` endpoint.
                                
                                ## Features
                                - **Multiple Channels**: Email, SMS, WhatsApp, In-app
                                - **Template Management**: Create and manage notification templates
                                - **History Tracking**: Track notification delivery status
                                - **Bulk Notifications**: Send notifications to multiple recipients
                                - **Template Variables**: Dynamic content in notifications
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.example.com/notification-service")
                                .description("Production server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from Auth Service /api/auth/login endpoint")));
    }
}
