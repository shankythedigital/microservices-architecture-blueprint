package com.example.asset.config;

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
 * ✅ OpenAPI Configuration for Asset Service
 * Configures Swagger/OpenAPI documentation with security schemes and API information.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI assetServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Asset Management Service API")
                        .description("""
                                Asset Management Service API
                                
                                Comprehensive asset lifecycle management including:
                                - Asset CRUD operations
                                - Master data management (Categories, Makes, Models, Vendors, etc.)
                                - User-Asset linking
                                - Compliance and validation
                                - Document management
                                - Warranty and AMC management
                                - Audit logging
                                
                                ## Authentication
                                Most endpoints require authentication using Bearer tokens. 
                                Obtain a token from the Auth Service `/api/auth/login` endpoint.
                                
                                ## Features
                                - **Bulk Operations**: Support for bulk create via JSON and Excel upload
                                - **Compliance**: Automated compliance checking and validation
                                - **Document Management**: Upload, download, and manage documents
                                - **User Assignment**: Link assets and components to users
                                - **Master Data**: Comprehensive master data management
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
                                .url("http://localhost:8083")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.example.com/asset-service")
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

