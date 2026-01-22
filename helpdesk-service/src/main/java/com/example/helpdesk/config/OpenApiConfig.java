package com.example.helpdesk.config;

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
 * ✅ OpenAPI Configuration for Helpdesk Service
 * Configures Swagger/OpenAPI documentation with security schemes and API information.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI helpdeskServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Helpdesk Service API")
                        .description("""
                                Helpdesk Service API
                                
                                Comprehensive helpdesk management including:
                                - Issue tracking and resolution
                                - FAQ management
                                - Query handling
                                - Chatbot integration
                                - Service knowledge base
                                
                                ## Authentication
                                Most endpoints require authentication using Bearer tokens. 
                                Obtain a token from the Auth Service `/api/auth/login` endpoint.
                                
                                ## Features
                                - **Issue Management**: Raise, track, and resolve issues across all services
                                - **FAQs**: Manage and search frequently asked questions
                                - **Queries**: Handle user queries with automated responses
                                - **Chatbot**: AI-powered chatbot for instant support
                                - **Knowledge Base**: Comprehensive knowledge about auth-service, notification-service, asset-service
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
                                .url("http://localhost:8084")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.example.com/helpdesk-service")
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

