# Swagger/OpenAPI Troubleshooting Guide

This guide helps resolve common issues with Swagger/OpenAPI integration.

## Common Issues and Solutions

### 1. Swagger UI Returns 404

**Symptoms:**
- Accessing `/swagger-ui.html` returns 404
- Swagger UI page doesn't load

**Solutions:**

1. **Check Dependencies:**
   ```xml
   <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
       <version>2.3.0</version>
   </dependency>
   ```

2. **Verify Security Configuration:**
   Ensure SecurityConfig allows Swagger paths:
   ```java
   .requestMatchers(
       "/swagger-ui/**",
       "/swagger-ui.html",
       "/v3/api-docs/**",
       "/api-docs/**",
       "/swagger-resources/**",
       "/webjars/**"
   ).permitAll()
   ```

3. **Check Application Properties:**
   ```yaml
   springdoc:
     swagger-ui:
       path: /swagger-ui.html
       enabled: true
   ```

4. **Verify Port:**
   - Auth Service: http://localhost:8081/swagger-ui.html
   - Notification Service: http://localhost:8082/swagger-ui.html
   - Asset Service: http://localhost:8083/swagger-ui.html

### 2. Duplicate Dependencies

**Symptoms:**
- Build warnings about duplicate dependencies
- Class conflicts

**Solution:**
Remove duplicate springdoc dependencies. Keep only:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Do NOT include:**
- `springdoc-openapi-starter-webflux-ui` (for WebMVC applications)
- Multiple versions of the same dependency

### 3. API Documentation Not Showing

**Symptoms:**
- Swagger UI loads but shows "No operations defined"
- Endpoints not appearing

**Solutions:**

1. **Check OpenApiConfig Bean:**
   Ensure `OpenApiConfig` class is annotated with `@Configuration` and has `@Bean` method.

2. **Verify Controller Annotations:**
   Controllers should be annotated with `@RestController` or `@Controller`.

3. **Check Package Scanning:**
   Ensure controllers are in scanned packages:
   ```java
   @SpringBootApplication(scanBasePackages = {"com.example.service", "com.example.common"})
   ```

4. **Verify Path Mappings:**
   Check that paths match the `paths-to-match` in `application.yml`:
   ```yaml
   springdoc:
     group-configs:
       - group: 'service-name'
         paths-to-match: '/api/**'
   ```

### 4. Authentication Not Working in Swagger

**Symptoms:**
- "Authorize" button doesn't work
- 401 errors when testing endpoints

**Solutions:**

1. **Click "Authorize" Button:**
   - Enter token in format: `Bearer <your-token>`
   - Do NOT include quotes around the token

2. **Get Valid Token:**
   ```bash
   # Register user
   curl -X POST http://localhost:8081/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"test","password":"Test123!","projectType":"ECOM"}'
   
   # Login to get token
   curl -X POST http://localhost:8081/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"loginType":"PASSWORD","username":"test","password":"Test123!"}'
   ```

3. **Check Token Expiry:**
   Tokens expire after configured time. Get a new token if expired.

4. **Verify Security Config:**
   Ensure endpoints require authentication:
   ```java
   .requestMatchers("/api/**").authenticated()
   ```

### 5. CORS Issues

**Symptoms:**
- Swagger UI can't make requests
- CORS errors in browser console

**Solutions:**

1. **Enable CORS in SecurityConfig:**
   ```java
   http.cors(customizer -> {});
   ```

2. **Add CORS Configuration:**
   ```java
   @Bean
   public CorsConfigurationSource corsConfigurationSource() {
       CorsConfiguration configuration = new CorsConfiguration();
       configuration.setAllowedOrigins(Arrays.asList("*"));
       configuration.setAllowedMethods(Arrays.asList("*"));
       configuration.setAllowedHeaders(Arrays.asList("*"));
       UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
       source.registerCorsConfiguration("/**", configuration);
       return source;
   }
   ```

### 6. Version Conflicts

**Symptoms:**
- ClassNotFoundException
- Method not found errors
- Build failures

**Solution:**
Use consistent version across all services:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### 7. Wrong Server URLs

**Symptoms:**
- Swagger UI shows wrong base URL
- Requests go to wrong server

**Solution:**
Update OpenApiConfig with correct port:
```java
.servers(List.of(
    new Server()
        .url("http://localhost:8081")  // Correct port for auth-service
        .description("Local development server")
))
```

**Correct Ports:**
- Auth Service: 8081
- Notification Service: 8082
- Asset Service: 8083

### 8. Missing Request/Response Schemas

**Symptoms:**
- Endpoints show but no request/response models
- "No schema" messages

**Solutions:**

1. **Add @Schema Annotations:**
   ```java
   @Schema(description = "User registration request")
   public class RegisterRequest {
       @Schema(description = "Username", example = "john.doe", required = true)
       private String username;
   }
   ```

2. **Use @Operation Annotations:**
   ```java
   @Operation(summary = "Register user", description = "Create a new user account")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "User created"),
       @ApiResponse(responseCode = "400", description = "Invalid input")
   })
   @PostMapping("/register")
   public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
       // ...
   }
   ```

### 9. Swagger UI Not Loading Resources

**Symptoms:**
- Swagger UI page loads but CSS/JS missing
- Blank page or broken UI

**Solutions:**

1. **Check Webjars:**
   SpringDoc includes webjars automatically. If missing, check:
   ```xml
   <dependency>
       <groupId>org.webjars</groupId>
       <artifactId>swagger-ui</artifactId>
   </dependency>
   ```

2. **Verify Security Config:**
   Ensure `/webjars/**` is permitted:
   ```java
   .requestMatchers("/webjars/**").permitAll()
   ```

3. **Check Browser Console:**
   Look for 404 errors on webjars resources.

### 10. Production Deployment Issues

**Symptoms:**
- Swagger works locally but not in production
- Security concerns about exposing API docs

**Solutions:**

1. **Disable in Production:**
   ```yaml
   # application-prod.yml
   springdoc:
     api-docs:
       enabled: false
     swagger-ui:
       enabled: false
   ```

2. **Conditional Configuration:**
   ```java
   @ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
   @Configuration
   public class OpenApiConfig {
       // ...
   }
   ```

3. **IP Whitelist:**
   Restrict access to Swagger UI by IP in SecurityConfig.

## Debugging Steps

1. **Check Application Logs:**
   Look for springdoc initialization messages:
   ```
   o.s.d.web.servlet.DocumentationPluginsBootstrapper : Context refreshed
   ```

2. **Verify Endpoints:**
   Test OpenAPI JSON directly:
   ```bash
   curl http://localhost:8081/v3/api-docs
   ```

3. **Check Dependencies:**
   ```bash
   mvn dependency:tree | grep springdoc
   ```

4. **Verify Configuration:**
   ```bash
   # Check if OpenApiConfig bean is created
   # Look for "Creating OpenAPI bean" in logs
   ```

5. **Test Security:**
   ```bash
   # Test if Swagger paths are accessible
   curl http://localhost:8081/swagger-ui.html
   ```

## Quick Checklist

- [ ] springdoc-openapi dependency added (version 2.3.0)
- [ ] OpenApiConfig class created with @Configuration
- [ ] SecurityConfig allows Swagger paths
- [ ] application.yml has springdoc configuration
- [ ] Correct port in OpenApiConfig servers
- [ ] No duplicate dependencies
- [ ] Controllers are in scanned packages
- [ ] CORS enabled if needed
- [ ] Valid JWT token for testing

## Getting Help

If issues persist:

1. Check service logs for errors
2. Verify all configuration files
3. Test endpoints directly (Postman/curl)
4. Compare with working service configuration
5. Check SpringDoc documentation: https://springdoc.org/

## Additional Resources

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
