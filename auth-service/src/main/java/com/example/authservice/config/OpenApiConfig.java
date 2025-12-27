package com.example.authservice.config;

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
 * ✅ OpenAPI Configuration for Auth Service
 * Configures Swagger/OpenAPI documentation with security schemes and API information.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Authentication & Authorization Service API")
                        .description("""
                                Authentication & Authorization Service API
                                
                                Comprehensive authentication and authorization including:
                                - User registration and login
                                - JWT token generation and validation
                                - Token refresh
                                - User profile management
                                - Admin user management
                                - Project type management
                                - OAuth2 integration
                                
                                ## Authentication
                                Most endpoints require authentication using Bearer tokens. 
                                Obtain a token from the `/api/auth/login` endpoint.
                                
                                ## Features
                                - **Multiple Login Methods**: Username/password, email/password, phone/OTP
                                - **JWT Tokens**: Access and refresh token support
                                - **User Management**: Complete CRUD operations for users
                                - **Admin Operations**: Admin-only endpoints for user management
                                - **Project Types**: Management of project types
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
                                .url("http://localhost:8081")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.example.com/auth-service")
                                .description("Production server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /api/auth/login endpoint")));
    }
}
