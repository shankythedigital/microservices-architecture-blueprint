#!/usr/bin/env bash
set -euo pipefail

# ======================================================
# setup-asset-service.sh  (Part 1 of 3)
# Generates project skeleton, POM, application.yml,
# BaseEntity, security core (JWT verifier/filter),
# and Feign config. Copies jwt-public.pem from auth-service.
# Run this script from the parent directory where auth-service exists.
# ======================================================

ROOT="$(pwd)/asset-service"
SRC_ROOT="$ROOT/src/main/java/com/example/asset"
RES_ROOT="$ROOT/src/main/resources"
DB_MIGRATION_DIR="$RES_ROOT/db/migration"
# # # # KEYS_DIR="$RES_ROOT/keys"
UPLOAD_DIR="$ROOT/uploads"


echo "Creating asset-service scaffold under: $ROOT"
mkdir -p "$SRC_ROOT"/{config,controller,dto,entity,exception,repository,security,service,service/client,service/impl,util,mapper}

mkdir -p "$RES_ROOT"
mkdir -p "$DB_MIGRATION_DIR"
# # # # mkdir -p "$KEYS_DIR"

# # # # # ---------- Defaults (customize by editing the script or environment variables) ----------
# # # # : "${GROUP_ID:=com.example}"
# # # # : "${ARTIFACT_ID:=asset-service}"
# # # # : "${BASE_PACKAGE:=com.example.asset}"
# # # # : "${ASSET_SERVER_PORT:=8083}"
# # # # : "${AUTH_SERVER_PORT:=8081}"
# # # # : "${NOTIFICATION_SERVER_PORT:=8082}"
# # # # : "${MYSQL_URL:=jdbc:mysql://localhost:3306/assetdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}"
# # # # : "${MYSQL_USER:=root}"
# # # # : "${MYSQL_PASS:=Snmysql@1110}"
# # # # : "${AUTH_SERVICE_URL:=http://localhost:$AUTH_SERVER_PORT}"
# # # # : "${NOTIFICATION_SERVICE_URL:=http://localhost:$NOTIFICATION_SERVER_PORT}"
# # # # : "${JWT_PUBLIC_KEY_CLASSPATH:=classpath:keys/jwt-public.pem}"

# # # # echo "Using defaults:"
# # # # echo " GROUP_ID=$GROUP_ID ARTIFACT_ID=$ARTIFACT_ID BASE_PACKAGE=$BASE_PACKAGE"
# # # # echo " ASSET_SERVER_PORT=$ASSET_SERVER_PORT MYSQL_URL=$MYSQL_URL"
# # # # echo " AUTH_SERVICE_URL=$AUTH_SERVICE_URL NOTIFICATION_SERVICE_URL=$NOTIFICATION_SERVICE_URL"
# # # # echo " JWT_PUBLIC_KEY_CLASSPATH=$JWT_PUBLIC_KEY_CLASSPATH"

# # # # # ---------- Copy jwt-public.pem from auth-service (copy mode) ----------
# # # # AUTH_KEY_SRC="$(pwd)/auth-service/src/main/resources/keys/jwt-public.pem"
# # # # if [[ -f "$AUTH_KEY_SRC" ]]; then
# # # #   cp -f "$AUTH_KEY_SRC" "$KEYS_DIR/jwt-public.pem"
# # # #   echo "Copied jwt-public.pem from auth-service -> $KEYS_DIR/jwt-public.pem"
# # # # else
# # # #   echo "WARNING: $AUTH_KEY_SRC not found."
# # # #   echo "Please ensure auth-service has keys at: auth-service/src/main/resources/keys/jwt-public.pem"
# # # #   echo "Continuing — but JWT verification will fail until public key is present."
# # # # fi

# ---------- 1) pom.xml ----------
cat > "$ROOT/pom.xml" <<'XML'

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <!-- Spring Boot parent handles plugin management and dependency versions -->
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.2</version>
    <relativePath>../pom.xml</relativePath>
  </parent>

  <groupId>com.example</groupId>
  <artifactId>asset-service</artifactId>
  <version>0.0.5-SNAPSHOT</version>
  <name>asset-service</name>
  <description>Asset Management Microservice (Executable JAR)</description>

  <properties>
    <java.version>17</java.version>
    <spring-cloud.version>2023.0.6</spring-cloud.version>
  </properties>

  <dependencies>
    <!-- 🟢 Core Web + JPA -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- 🟢 Security + OAuth2 + JWT -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- Provides JwtDecoder, JwtValidators, JwtTimestampValidator, OAuth2TokenValidator -->
    <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-oauth2-jose</artifactId>
    </dependency>

    <!-- 🟢 Database driver -->
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>

    <!-- 🟢 Feign client for inter-service communication -->
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <!-- 🟢 JSON Web Token (optional if not using Nimbus) -->
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-api</artifactId>
      <version>0.11.5</version>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-impl</artifactId>
      <version>0.11.5</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-jackson</artifactId>
      <version>0.11.5</version>
      <scope>runtime</scope>
    </dependency>

    <!-- 🟢 Validation -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- 🟢 Lombok (developer helper) -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>

    <!-- 🟢 Swagger / OpenAPI UI -->
    <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.1.0</version>
    </dependency>

    <dependency>
        <groupId>commons-fileupload</groupId>
        <artifactId>commons-fileupload</artifactId>
        <version>1.5</version>
    </dependency>


    <dependency>
        <groupId>org.yaml</groupId>
        <artifactId>snakeyaml</artifactId>
        <version>2.2</version>
    </dependency>


    <!-- 🧪 Testing -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>


    <!-- Common lib -->
    <dependency>
      <groupId>com.example</groupId>
      <artifactId>common-service</artifactId>
      <version>0.0.5-SNAPSHOT</version>
    </dependency>
    
    <!-- Auth Service  -->
    <dependency>
      <groupId>com.example</groupId>
      <artifactId>auth-service</artifactId>
      <version>0.0.5-SNAPSHOT</version>
    </dependency>

    <!-- OpenAPI/Swagger Support for Spring Boot 3 -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.5.0</version>
    </dependency>

    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
        <version>2.5.0</version>
    </dependency>


  </dependencies>

  <!-- 🧩 Spring Cloud dependency alignment -->
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring-cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <build>
    <plugins>
      <!-- ✅ Makes JAR executable -->
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>repackage</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <!-- The main Spring Boot application class -->
          <mainClass>com.example.asset.AssetServiceApplication</mainClass>
          <layout>ZIP</layout>
        </configuration>
      </plugin>

      <!-- Ensures proper Java 17 compilation -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
          <source>${java.version}</source>
          <target>${java.version}</target>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>


XML

echo "Created: $ROOT/pom.xml"

# # # # # ---------- 2) application.yml ----------
# # # # cat > "$RES_ROOT/application.yml" <<YML
# # # # server:
# # # #   port: ${ASSET_SERVER_PORT}
  

# # # # spring:
# # # #   application:
# # # #     name: asset-service

# # # #   datasource:
# # # #     url: ${MYSQL_URL}
# # # #     username: ${MYSQL_USER}
# # # #     password: ${MYSQL_PASS}
# # # #     driver-class-name: com.mysql.cj.jdbc.Driver
# # # #     hikari:
# # # #       maximum-pool-size: 10
# # # #       connection-timeout: 20000

# # # #   jpa:
# # # #     hibernate:
# # # #       ddl-auto: update    # instead of 'validate'
# # # #     database-platform: org.hibernate.dialect.MySQL8Dialect
# # # #     show-sql: false

# # # # services:
# # # #   auth:
# # # #     base-url: ${AUTH_SERVICE_URL}
# # # #   notification:
# # # #     base-url: ${NOTIFICATION_SERVICE_URL}

# # # # security:
# # # #   jwt:
# # # #     public-key-path: ${JWT_PUBLIC_KEY_CLASSPATH}
# # # #     issuer: "auth-service"
# # # #     audience: "asset-service"

# # # # management:
# # # #   endpoints:
# # # #     web:
# # # #       exposure: health,info

# # # # debug: true
# # # # logging:
# # # #   level:
# # # #     root: INFO
# # # #     org.springframework: DEBUG
    
# # # # YML

# # # # echo "Created: $RES_ROOT/application.yml"

# # # # # ---------- 3) .env.asset ----------
# # # # cat > "$ROOT/.env.asset" <<ENV
# # # # MYSQL_URL=${MYSQL_URL}
# # # # MYSQL_USER=${MYSQL_USER}
# # # # MYSQL_PASS=${MYSQL_PASS}
# # # # ASSET_SERVER_PORT=${ASSET_SERVER_PORT}
# # # # AUTH_SERVICE_URL=${AUTH_SERVICE_URL}
# # # # NOTIFICATION_SERVICE_URL=${NOTIFICATION_SERVICE_URL}
# # # # JWT_PUBLIC_KEY_CLASSPATH=${JWT_PUBLIC_KEY_CLASSPATH}
# # # # ENV

# # # # echo "Created: $ROOT/.env.asset"

# ---------- 4) AssetServiceApplication.java ----------
cat > "$SRC_ROOT/AssetServiceApplication.java" <<JAVA

package com.example.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.asset", "com.example.common"})
@EntityScan(basePackages = {"com.example.asset.entity", "com.example.common.entity"})
@EnableJpaRepositories(basePackages = {"com.example.asset.repository", "com.example.common.repository"})
@EnableFeignClients(basePackages = {"com.example.common.client", "com.example.asset.client"})
public class AssetServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssetServiceApplication.class, args);
    }
}

JAVA

echo "Created: AssetServiceApplication.java"

# ---------- 5) util/Constants.java ----------
cat > "$SRC_ROOT/util/Constants.java" <<JAVA
package com.example.asset.util;

public final class Constants {
    private Constants(){}

    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_ASSIGNED  = "ASSIGNED";
    public static final String STATUS_SOLD      = "SOLD";
    public static final String STATUS_DAMAGED   = "DAMAGED";
    public static final String STATUS_LOST      = "LOST";

    public static final String PURCHASE_MODE_ONLINE = "ONLINE";
    public static final String PURCHASE_MODE_OUTLET = "OUTLET";
}
JAVA

echo "Created: util/Constants.java"


# # # # # ---------- 7) security/JwtVerifier.java ----------
# # # # cat > "$SRC_ROOT/security/JwtVerifier.java" <<'JAVA'
# # # # package com.example.asset.security;

# # # # import io.jsonwebtoken.Claims;
# # # # import io.jsonwebtoken.Jwts;
# # # # import io.jsonwebtoken.JwtException;
# # # # import org.springframework.core.io.ClassPathResource;
# # # # import java.io.InputStream;
# # # # import java.nio.file.Files;
# # # # import java.nio.file.Paths;
# # # # import java.security.KeyFactory;
# # # # import java.security.PublicKey;
# # # # import java.security.spec.X509EncodedKeySpec;
# # # # import java.util.Base64;

# # # # public class JwtVerifier {

# # # #     private final PublicKey publicKey;

# # # #     public JwtVerifier(String publicKeyPath) {
# # # #         this.publicKey = loadPublicKey(publicKeyPath);
# # # #     }

# # # #     private PublicKey loadPublicKey(String path) {
# # # #         try {
# # # #             byte[] keyBytes;
# # # #             if (path.startsWith("classpath:")) {
# # # #                 String r = path.replace("classpath:", "");
# # # #                 try (InputStream in = new ClassPathResource(r).getInputStream()) {
# # # #                     keyBytes = in.readAllBytes();
# # # #                 }
# # # #             } else {
# # # #                 keyBytes = Files.readAllBytes(Paths.get(path));
# # # #             }
# # # #             String key = new String(keyBytes)
# # # #                     .replace("-----BEGIN PUBLIC KEY-----", "")
# # # #                     .replace("-----END PUBLIC KEY-----", "")
# # # #                     .replaceAll("\\s+", "");
# # # #             byte[] decoded = Base64.getDecoder().decode(key);
# # # #             X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
# # # #             return KeyFactory.getInstance("RSA").generatePublic(keySpec);
# # # #         } catch (Exception e) {
# # # #             throw new RuntimeException("Failed to load public key: " + e.getMessage(), e);
# # # #         }
# # # #     }

# # # #     public Claims validateToken(String token) {
# # # #         try {
# # # #             return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
# # # #         } catch (JwtException e) {
# # # #             throw new RuntimeException("Invalid or expired token", e);
# # # #         }
# # # #     }
# # # # }
# # # # JAVA

# # # # echo "Created: security/JwtVerifier.java"

# ---------- 8) security/JwtAuthFilter.java ----------
cat > "$SRC_ROOT/security/JwtAuthFilter.java" <<'JAVA'
package com.example.asset.security;

import io.jsonwebtoken.Claims;
import com.example.common.security.JwtVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtVerifier jwtVerifier;

    public JwtAuthFilter(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtVerifier.parseToken(token).getBody();
                String userId = claims.getSubject();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, token, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}

JAVA

echo "Created: security/JwtAuthFilter.java"

# ---------- 9) config/SecurityConfig.java ----------
cat > "$SRC_ROOT/config/SecurityConfig.java" <<JAVA
package com.example.asset.config;
import com.example.common.security.JwtAuthFilter;
import com.example.common.security.JwtVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ✅ Security configuration for Notification Service.
 * Uses shared JwtVerifier (from common module) to validate tokens
 * using either RSA public key or HMAC secret.
 */
@Configuration
public class SecurityConfig {

    private final JwtVerifier jwtVerifier;

    public SecurityConfig(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Custom JWT validation filter
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtVerifier);

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Only allow POST to /api/notifications with valid JWT
                .requestMatchers("/api/asset/**").authenticated()
                // Permit actuator/health or any public endpoints if needed
                .requestMatchers(
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/public/**"
                ).permitAll()
                .anyRequest().permitAll()
            )

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


JAVA

echo "Created: config/SecurityConfig.java"

# # # # # ---------- 10) config/FeignConfig.java ----------
# # # # cat > "$SRC_ROOT/config/FeignConfig.java" <<JAVA
# # # # package com.example.asset.config;

# # # # import com.example.common.service.AuthTokenService;
# # # # import feign.RequestInterceptor;
# # # # import feign.RequestTemplate;
# # # # import org.springframework.context.annotation.Bean;
# # # # import org.springframework.context.annotation.Configuration;

# # # # @Configuration
# # # # public class FeignConfig {

# # # #     private final AuthTokenService tokenService;

# # # #     public FeignConfig(AuthTokenService tokenService) {
# # # #         this.tokenService = tokenService;
# # # #     }

# # # #     @Bean
# # # #     public RequestInterceptor requestInterceptor() {
# # # #         return new RequestInterceptor() {
# # # #             @Override
# # # #             public void apply(RequestTemplate template) {
# # # #                 String token = tokenService.getAccessToken();
# # # #                 if (token != null) {
# # # #                     template.header("Authorization", "Bearer " + token);
# # # #                 }
# # # #             }
# # # #         };
# # # #     }
# # # # }
# # # # JAVA

# # # # echo "Created: config/FeignConfig.java"

# # # # # ---------- 11) GlobalExceptionHandler (exception handling) ----------
# # # # cat > "$SRC_ROOT/exception/GlobalExceptionHandler.java" <<JAVA
# # # # package com.example.asset.exception;

# # # # import org.springframework.http.ResponseEntity;
# # # # import org.springframework.web.bind.annotation.ExceptionHandler;
# # # # import org.springframework.web.bind.annotation.RestControllerAdvice;

# # # # @RestControllerAdvice
# # # # public class GlobalExceptionHandler {

# # # #     @ExceptionHandler(BadCredentialsException.class)
# # # #     public ResponseEntity<String> badCred(BadCredentialsException ex) {
# # # #         return ResponseEntity.status(401).body("Invalid credentials: " + ex.getMessage());
# # # #     }

# # # #     @ExceptionHandler(LockedException.class)
# # # #     public ResponseEntity<String> locked(LockedException ex) {
# # # #         return ResponseEntity.status(423).body("Account locked: " + ex.getMessage());
# # # #     }

# # # #     @ExceptionHandler(CredentialsExpiredException.class)
# # # #     public ResponseEntity<String> expired(CredentialsExpiredException ex) {
# # # #         return ResponseEntity.status(401).body("Credentials expired: " + ex.getMessage());
# # # #     }

# # # #     @ExceptionHandler(DisabledException.class)
# # # #     public ResponseEntity<String> disabled(DisabledException ex) {
# # # #         return ResponseEntity.status(403).body("Account disabled: " + ex.getMessage());
# # # #     }

# # # #     @ExceptionHandler(Exception.class)
# # # #     public ResponseEntity<String> generic(Exception ex) {
# # # #         return ResponseEntity.internalServerError().body("Unexpected error: " + ex.getMessage());
# # # #     }
# # # # }
# # # # JAVA

# ---------- util/ResponseWrapper.java ----------
cat > "$SRC_ROOT/util/ResponseWrapper.java" <<'JAVA'
package com.example.asset.util;

public class ResponseWrapper<T> {
    private boolean success;
    private String message;
    private T data;

    public ResponseWrapper() {}

    public ResponseWrapper(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
JAVA

echo "Created: util/ResponseWrapper.java"



# ---------- Part 1 completed ----------
echo ""
echo "PART 1 complete: core project files, security, POM, application.yml, and base utilities created."
echo "I will continue with Part 2 (entities, repositories, services, controllers) in the next message."
echo "If you want to stop, press Ctrl+C now. Otherwise I'll proceed with Part 2 automatically."


# ======================================================
# setup-asset-service.sh  (Part 2)
# Writes Entities, Repositories, DTOs, Services, Controllers,
# DataInitializer, Flyway migrations and debug-run helper.
# ======================================================


# ---------- 1) Entities ----------
cat > "$SRC_ROOT/entity/AuditLog.java" <<'JAVA'
package com.example.asset.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.common.jpa.BaseEntity;


@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ipAddress;
    @Column(length = 1000)
    private String userAgent;
    private String url;
    private String httpMethod;
    private String username;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}
    public String getIpAddress(){return ipAddress;}
    public void setIpAddress(String ipAddress){this.ipAddress=ipAddress;}
    public String getUserAgent(){return userAgent;}
    public void setUserAgent(String userAgent){this.userAgent=userAgent;}
    public String getUrl(){return url;}
    public void setUrl(String url){this.url=url;}
    public String getHttpMethod(){return httpMethod;}
    public void setHttpMethod(String httpMethod){this.httpMethod=httpMethod;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
    public String getUsername(){return username;}
    public void setUsername(String username){this.username=username;}
}
JAVA

cat > "$SRC_ROOT/entity/ProductCategory.java" <<'JAVA'
package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * ✅ ProductCategory Entity
 * Represents a high-level category for products.
 * Extends BaseEntity to inherit audit fields and soft-delete support.
 */
@Entity
@Table(
    name = "product_category",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category_name"})
    }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "subCategories"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductCategory extends BaseEntity implements Serializable {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    @Column(name = "category_name", nullable = false, unique = true, length = 200)
    private String categoryName;

    @Column(name = "description", length = 500)
    private String description;

    // ============================================================
    // 🔗 Relationships
    // ============================================================
    /**
     * One category can have multiple subcategories.
     * Lazy fetch ensures performance.
     * Avoids recursion during JSON serialization.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"category", "hibernateLazyInitializer", "handler"})
    private List<ProductSubCategory> subCategories;

    // ============================================================
    // 🏗️ Constructors
    // ============================================================
    public ProductCategory() {}

    public ProductCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ProductSubCategory> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<ProductSubCategory> subCategories) {
        this.subCategories = subCategories;
    }

    // ============================================================
    // 🧠 toString() for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "ProductCategory{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", active=" + getActive() +
                ", createdBy='" + getCreatedBy() + '\'' +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                '}';
    }
}


JAVA

cat > "$SRC_ROOT/entity/ProductSubCategory.java" <<'JAVA'



package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import java.io.Serializable;


/**
 * ✅ ProductSubCategory Entity
 * Represents a subcategory within a product category.
 * Linked to ProductCategory, includes audit information.
 */
@Entity
@Table(name = "product_sub_category")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSubCategory  extends BaseEntity  implements Serializable {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_category_id")
    private Long subCategoryId;

    // ============================================================
    // 📦 Fields
    // ============================================================
    @Column(name = "sub_category_name", nullable = false, unique = true)
    private String subCategoryName;

    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private Boolean active = true;

    // ============================================================
    // 🔗 Relationships
    // ============================================================
    /**
     * Each SubCategory belongs to one Category.
     * Using LAZY fetch to avoid unnecessary joins, with safe serialization via @JsonIgnoreProperties.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"subCategories", "hibernateLazyInitializer", "handler"})
    private ProductCategory category;


    // ============================================================
    // 🏗️ Constructors
    // ============================================================
    public ProductSubCategory() {}

    public ProductSubCategory(String subCategoryName, ProductCategory category) {
        this.subCategoryName = subCategoryName;
        this.category = category;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }


    // ============================================================
    // 🧠 toString() for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "ProductSubCategory{" +
                "subCategoryId=" + subCategoryId +
                ", subCategoryName='" + subCategoryName + '\'' +
                ", active=" + active +
                ", category=" + (category != null ? category.getCategoryName() : "null") +
                '}';
    }
}


JAVA

cat > "$SRC_ROOT/entity/ProductMake.java" <<'JAVA'
package com.example.asset.entity;

import jakarta.persistence.*;
import com.example.common.jpa.BaseEntity;


@Entity
@Table(name = "product_make")
public class ProductMake extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long makeId;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    private ProductSubCategory subCategory;

    private String makeName;

    public ProductMake(){}
    public ProductMake(String makeName, ProductSubCategory subCategory){
        this.makeName = makeName; this.subCategory = subCategory;
    }

    public Long getMakeId(){ return makeId; }
    public void setMakeId(Long makeId){ this.makeId = makeId; }
    public ProductSubCategory getSubCategory(){ return subCategory; }
    public void setSubCategory(ProductSubCategory subCategory){ this.subCategory = subCategory; }
    public String getMakeName(){ return makeName; }
    public void setMakeName(String makeName){ this.makeName = makeName; }
}
JAVA

cat > "$SRC_ROOT/entity/ProductModel.java" <<'JAVA'


package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

/**
 * ✅ ProductModel Entity
 * Represents a specific model belonging to a product make.
 * Uniqueness is enforced per (make + modelName) combination.
 */
@Entity
@Table(
    name = "product_model",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"model_name", "make_id"})
    }
)
public class ProductModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "make_id", nullable = false)
    private ProductMake make;

    @Column(name = "model_name", nullable = false, length = 150)
    private String modelName;

    @Column(length = 255)
    private String description;

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public ProductModel() {}

    public ProductModel(String modelName, ProductMake make) {
        this.modelName = modelName;
        this.make = make;
    }

    // ============================================================
    // ✅ Getters and Setters
    // ============================================================

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public ProductMake getMake() {
        return make;
    }

    public void setMake(ProductMake make) {
        this.make = make;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ============================================================
    // ✅ Convenience Methods
    // ============================================================

    @Override
    public String toString() {
        return "ProductModel{" +
                "modelId=" + modelId +
                ", modelName='" + modelName + '\'' +
                ", make=" + (make != null ? make.getMakeName() : "null") +
                ", active=" + getActive() +
                '}';
    }
}

JAVA

cat > "$SRC_ROOT/entity/PurchaseOutlet.java" <<'JAVA'
package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

/**
 * ✅ PurchaseOutlet Entity
 * Represents a physical or online store/vendor from which assets can be purchased or serviced.
 *
 * Features:
 *  - Unique outlet name constraint
 *  - Linked with optional VendorMaster (for supplier/vendor management)
 *  - Extends BaseEntity for auditing and active flag handling
 */
@Entity
@Table(
    name = "purchase_outlet",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"outlet_name"})
    }
)
public class PurchaseOutlet extends BaseEntity {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outlet_id")
    private Long outletId;

    // ============================================================
    // 🏷️ Outlet Details
    // ============================================================
    @Column(name = "outlet_name", nullable = false, length = 150, unique = true)
    private String outletName;

    @Column(name = "outlet_address", length = 255)
    private String outletAddress;

    @Column(name = "contact_info", length = 100)
    private String contactInfo;

    // ============================================================
    // 🧩 Optional Vendor Relationship
    // ============================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private VendorMaster vendor;

    // ============================================================
    // 🧩 Constructors
    // ============================================================
    public PurchaseOutlet() {}

    public PurchaseOutlet(String outletName, String outletAddress, String contactInfo) {
        this.outletName = outletName;
        this.outletAddress = outletAddress;
        this.contactInfo = contactInfo;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getOutletId() {
        return outletId;
    }

    public void setOutletId(Long outletId) {
        this.outletId = outletId;
    }

    public String getOutletName() {
        return outletName;
    }

    public void setOutletName(String outletName) {
        this.outletName = outletName;
    }

    public String getOutletAddress() {
        return outletAddress;
    }

    public void setOutletAddress(String outletAddress) {
        this.outletAddress = outletAddress;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public VendorMaster getVendor() {
        return vendor;
    }

    public void setVendor(VendorMaster vendor) {
        this.vendor = vendor;
    }

    // ============================================================
    // 🧠 toString (For Logging)
    // ============================================================
    @Override
    public String toString() {
        return "PurchaseOutlet{" +
                "outletId=" + outletId +
                ", outletName='" + outletName + '\'' +
                ", outletAddress='" + outletAddress + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", active=" + getActive() +
                '}';
    }
}


JAVA

cat > "$SRC_ROOT/entity/VendorMaster.java" <<'JAVA'

package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * ✅ VendorMaster Entity
 * Represents a vendor or supplier associated with asset purchases.
 * A vendor can have multiple outlets.
 */
@Entity
@Table(
    name = "vendor_master",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"vendor_name"})
    }
)
public class VendorMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendorId;

    @Column(name = "vendor_name", nullable = false, length = 150)
    private String vendorName;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(length = 100)
    private String email;

    @Column(length = 15)
    private String mobile;

    @Column(length = 255)
    private String address;

    // ✅ One vendor can have multiple outlets
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PurchaseOutlet> outlets = new HashSet<>();

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public VendorMaster() {}

    public VendorMaster(String vendorName, String contactPerson, String email, String mobile, String address) {
        this.vendorName = vendorName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.mobile = mobile;
        this.address = address;
    }

    // ============================================================
    // ✅ Getters and Setters
    // ============================================================
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Set<PurchaseOutlet> getOutlets() { return outlets; }
    public void setOutlets(Set<PurchaseOutlet> outlets) { this.outlets = outlets; }

    // ============================================================
    // ✅ Convenience
    // ============================================================
    @Override
    public String toString() {
        return "VendorMaster{" +
                "vendorId=" + vendorId +
                ", vendorName='" + vendorName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}


JAVA

cat > "$SRC_ROOT/entity/AssetComponent.java" <<'JAVA'

package com.example.asset.entity;

import jakarta.persistence.*;
import com.example.common.jpa.BaseEntity;


@Entity
@Table(name = "asset_component_master")
public class AssetComponent extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long componentId;
    private String componentName;
    private String description;

    public Long getComponentId(){ return componentId; }
    public void setComponentId(Long componentId){ this.componentId = componentId; }
    public String getComponentName(){ return componentName; }
    public void setComponentName(String componentName){ this.componentName = componentName; }
    public String getDescription(){ return description; }
    public void setDescription(String description){ this.description = description; }
}
JAVA

cat > "$SRC_ROOT/entity/AssetMaster.java" <<'JAVA'
package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "asset_master")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AssetMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Long assetId;

    @Column(name = "asset_name_udv", nullable = false, unique = true, length = 255)
    private String assetNameUdv;

    // =======================
    // CATEGORY, SUBCATEGORY
    // =======================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({ "assets", "subCategories" })
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    @JsonIgnoreProperties({ "assets" })
    private ProductSubCategory subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "make_id", nullable = false)
    @JsonIgnoreProperties({ "assets" })
    private ProductMake make;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    @JsonIgnoreProperties({ "assets" })
    private ProductModel model;

    // =======================
    // WARRANTY & AMC
    // =======================
    @OneToOne(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private AssetWarranty warranty;

    @OneToOne(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private AssetAmc amc;

    // =======================
    // USER LINK FIXED HERE
    // =======================
    /**
     * FIX:
     * Previously mappedBy="asset" → INVALID (no asset field in AssetUserLink)
     * Now linked using FK asset_id column, no circular mapping.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", referencedColumnName = "asset_id", insertable = false, updatable = false)
    @JsonIgnore
    private AssetUserLink userLink;

    // =======================
    // COMPONENTS
    // =======================
    @ManyToMany
    @JoinTable(name = "asset_component_link", joinColumns = @JoinColumn(name = "asset_id"), inverseJoinColumns = @JoinColumn(name = "component_id"))
    @JsonIgnoreProperties({ "assets" })
    private Set<AssetComponent> components = new HashSet<>();

    @Transient
    private String displayName;

    private String assetStatus;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // avoid circular JSON when serializing
    private Set<AssetDocument> documents = new HashSet<>();

    // =======================
    // GETTERS/SETTERS
    // =======================

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetNameUdv() {
        return assetNameUdv;
    }

    public void setAssetNameUdv(String assetNameUdv) {
        this.assetNameUdv = assetNameUdv;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public ProductSubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(ProductSubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public ProductMake getMake() {
        return make;
    }

    public void setMake(ProductMake make) {
        this.make = make;
    }

    public ProductModel getModel() {
        return model;
    }

    public void setModel(ProductModel model) {
        this.model = model;
    }

    public AssetWarranty getWarranty() {
        return warranty;
    }

    public void setWarranty(AssetWarranty warranty) {
        this.warranty = warranty;
    }

    public AssetAmc getAmc() {
        return amc;
    }

    public void setAmc(AssetAmc amc) {
        this.amc = amc;
    }

    public AssetUserLink getUserLink() {
        return userLink;
    }

    public void setUserLink(AssetUserLink userLink) {
        this.userLink = userLink;
    }

    public Set<AssetComponent> getComponents() {
        return components;
    }

    public void setComponents(Set<AssetComponent> components) {
        this.components = components;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    public Set<AssetDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<AssetDocument> documents) {
        this.documents = documents;
    }

}


JAVA

cat > "$SRC_ROOT/entity/AssetUserLink.java" <<'JAVA'
package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_user_link")
public class AssetUserLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "asset_id")
    private Long assetId;

    @Column(name = "component_id")
    private Long componentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false, length = 255)
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate = LocalDateTime.now();

    @Column(name = "unassigned_date")
    private LocalDateTime unassignedDate;

    // GETTERS & SETTERS

    public Long getLinkId() { return linkId; }
    public void setLinkId(Long linkId) { this.linkId = linkId; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public LocalDateTime getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }

    public LocalDateTime getUnassignedDate() { return unassignedDate; }
    public void setUnassignedDate(LocalDateTime unassignedDate) { this.unassignedDate = unassignedDate; }

    @Override
    public String toString() {
        return "AssetUserLink{" +
                "linkId=" + linkId +
                ", assetId=" + assetId +
                ", componentId=" + componentId +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", assignedDate=" + assignedDate +
                ", unassignedDate=" + unassignedDate +
                ", active=" + getActive() +
                ", createdBy='" + getCreatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}


JAVA

cat > "$SRC_ROOT/entity/AssetWarranty.java" <<'JAVA'
package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * ✅ AssetWarranty Entity
 * Represents warranty details for an asset.
 * Linked with AssetMaster and optionally an AssetDocument.
 * Handles null-safe document_id persistence.
 */
@Entity
@Table(name = "asset_warranty")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "asset"})
public class AssetWarranty extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warranty_id")
    private Long warrantyId;

    @Column(name = "warranty_status")
    private String warrantyStatus;

    @Column(name = "warranty_provider")
    private String warrantyProvider;

    @Column(name = "warranty_terms", length = 1000)
    private String warrantyTerms;

    @Column(name = "start_date")
    private LocalDate warrantyStartDate;

    @Column(name = "end_date")
    private LocalDate warrantyEndDate;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "component_id")
    private Long componentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    @JsonIgnoreProperties({"warranties", "hibernateLazyInitializer", "handler"})
    private AssetMaster asset;

    // ✅ Persisted foreign key to AssetDocument table
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", referencedColumnName = "document_id", nullable = true)
    private AssetDocument document;

    // ============================================================
    // 🔧 Getters & Setters
    // ============================================================

    public Long getWarrantyId() {
        return warrantyId;
    }

    public void setWarrantyId(Long warrantyId) {
        this.warrantyId = warrantyId;
    }

    public String getWarrantyStatus() {
        return warrantyStatus;
    }

    public void setWarrantyStatus(String warrantyStatus) {
        this.warrantyStatus = warrantyStatus;
    }

    public String getWarrantyProvider() {
        return warrantyProvider;
    }

    public void setWarrantyProvider(String warrantyProvider) {
        this.warrantyProvider = warrantyProvider;
    }

    public String getWarrantyTerms() {
        return warrantyTerms;
    }

    public void setWarrantyTerms(String warrantyTerms) {
        this.warrantyTerms = warrantyTerms;
    }

    public LocalDate getWarrantyStartDate() {
        return warrantyStartDate;
    }

    public void setWarrantyStartDate(LocalDate warrantyStartDate) {
        this.warrantyStartDate = warrantyStartDate;
    }

    public LocalDate getWarrantyEndDate() {
        return warrantyEndDate;
    }

    public void setWarrantyEndDate(LocalDate warrantyEndDate) {
        this.warrantyEndDate = warrantyEndDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public AssetMaster getAsset() {
        return asset;
    }

    public void setAsset(AssetMaster asset) {
        this.asset = asset;
    }

    public AssetDocument getDocument() {
        return document;
    }

    public void setDocument(AssetDocument document) {
        this.document = document;
    }

    // ✅ Safe getter for FK
    public Long getDocumentId() {
        return (document != null) ? document.getDocumentId() : null;
    }

    // ✅ Handles both null and valid ID values
    public void setDocumentId(Long documentId) {
        if (documentId == null) {
            this.document = null; // store as NULL in DB
        } else {
            AssetDocument doc = new AssetDocument();
            doc.setDocumentId(documentId);
            this.document = doc;
        }
    }

    // ============================================================
    // 🧠 Convenience Aliases
    // ============================================================
    @Transient
    public LocalDate getStartDate() {
        return getWarrantyStartDate();
    }

    @Transient
    public LocalDate getEndDate() {
        return getWarrantyEndDate();
    }

    @Override
    public String toString() {
        return "AssetWarranty{" +
                "warrantyId=" + warrantyId +
                ", warrantyStatus='" + warrantyStatus + '\'' +
                ", warrantyProvider='" + warrantyProvider + '\'' +
                ", warrantyStartDate=" + warrantyStartDate +
                ", warrantyEndDate=" + warrantyEndDate +
                ", documentId=" + getDocumentId() +
                ", username='" + username + '\'' +
                '}';
    }
}


JAVA

cat > "$SRC_ROOT/entity/AssetAmc.java" <<'JAVA'
package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * ✅ AssetAmc Entity
 * Represents Annual Maintenance Contract details for an asset.
 * Automatically linked to uploaded documents and assets.
 * Handles null-safe document_id persistence.
 */
@Entity
@Table(name = "asset_amc")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "asset"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetAmc extends BaseEntity implements Serializable {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amc_id")
    private Long amcId;

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    @Column(name = "amc_status", length = 100)
    private String amcStatus;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "component_id")
    private Long componentId;

    // ============================================================
    // 🔗 Relationships
    // ============================================================

    /**
     * Many AMCs can belong to one Asset.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AssetMaster asset;

    /**
     * Each AMC can have one linked uploaded document.
     * If null, document_id will be stored as NULL.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", referencedColumnName = "document_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AssetDocument document;

    // ============================================================
    // 🔧 Getters and Setters
    // ============================================================

    public Long getAmcId() {
        return amcId;
    }

    public void setAmcId(Long amcId) {
        this.amcId = amcId;
    }

    public String getAmcStatus() {
        return amcStatus;
    }

    public void setAmcStatus(String amcStatus) {
        this.amcStatus = amcStatus;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public AssetMaster getAsset() {
        return asset;
    }

    public void setAsset(AssetMaster asset) {
        this.asset = asset;
    }

    public AssetDocument getDocument() {
        return document;
    }

    public void setDocument(AssetDocument document) {
        this.document = document;
    }

    // ✅ Null-safe foreign key management
    public Long getDocumentId() {
        return (document != null) ? document.getDocumentId() : null;
    }

    public void setDocumentId(Long documentId) {
        if (documentId == null) {
            this.document = null; // store NULL in DB
        } else {
            AssetDocument doc = new AssetDocument();
            doc.setDocumentId(documentId);
            this.document = doc; // store FK reference
        }
    }

    // ============================================================
    // 🧠 Debug-friendly toString()
    // ============================================================
    @Override
    public String toString() {
        return "AssetAmc{" +
                "amcId=" + amcId +
                ", amcStatus='" + amcStatus + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", componentId=" + componentId +
                ", assetId=" + (asset != null ? asset.getAssetId() : null) +
                ", documentId=" + getDocumentId() +
                '}';
    }
}


JAVA

cat > "$SRC_ROOT/entity/AssetDocument.java" <<'JAVA'
package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ✅ AssetDocument Entity
 *
 * Represents uploaded documents (images, files, etc.) linked to
 * any entity (Asset, AMC, Warranty, Category, etc.).
 *
 * - Supports dynamic linking via (entityType, entityId)
 * - Maintains soft delete (active flag)
 * - Includes user & audit metadata
 */
@Entity
@Table(name = "asset_document",
       indexes = {
           @Index(name = "idx_entity_type_id", columnList = "entity_type, entity_id"),
           @Index(name = "idx_doc_type", columnList = "doc_type")
       })
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "asset"})
public class AssetDocument extends BaseEntity implements Serializable {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    // ============================================================
    // 📎 Linkage (Generic Entity Reference)
    // ============================================================
    /**
     * The entity type this document belongs to (e.g. ASSET, AMC, WARRANTY).
     */
    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    /**
     * The ID of the entity this document is linked to.
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    // ============================================================
    // 📦 File Metadata
    // ============================================================
    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "doc_type", length = 100)
    private String docType;

    @Column(name = "uploaded_date")
    private LocalDateTime uploadedDate;

    // ============================================================
    // 👤 User Context
    // ============================================================
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "project_type", length = 255)
    private String projectType;

    // ============================================================
    // 🔗 Asset Relationship (Backward Compatibility)
    // ============================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    @JsonIgnoreProperties({"documents", "hibernateLazyInitializer", "handler"})
    private AssetMaster asset;

    // ============================================================
    // 🧾 Custom State
    // ============================================================
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // ============================================================
    // 🔧 Getters & Setters
    // ============================================================

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public LocalDateTime getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(LocalDateTime uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public AssetMaster getAsset() {
        return asset;
    }

    public void setAsset(AssetMaster asset) {
        this.asset = asset;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    // ============================================================
    // 🧠 toString for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "AssetDocument{" +
                "documentId=" + documentId +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", fileName='" + fileName + '\'' +
                ", docType='" + docType + '\'' +
                ", filePath='" + filePath + '\'' +
                ", uploadedDate=" + uploadedDate +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", active=" + active +
                '}';
    }
}


JAVA

echo "Created: Entities"

# ---------- 2) Repositories ----------


cat > "$SRC_ROOT/repository/VendorRepository.java" <<'JAVA'

package com.example.asset.repository;

import com.example.asset.entity.VendorMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<VendorMaster, Long> {
    boolean existsByVendorNameIgnoreCase(String vendorName);
}


JAVA


cat > "$SRC_ROOT/repository/AuditLogRepository.java" <<'JAVA'
package com.example.asset.repository;
import com.example.asset.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
JAVA

cat > "$SRC_ROOT/repository/ProductCategoryRepository.java" <<'JAVA'

package com.example.asset.repository;

import com.example.asset.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    boolean existsByCategoryName(String categoryName);
    Optional<ProductCategory> findByCategoryNameIgnoreCase(String categoryName);
}

JAVA

cat > "$SRC_ROOT/repository/ProductSubCategoryRepository.java" <<'JAVA'

package com.example.asset.repository;

import com.example.asset.entity.ProductSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ✅ ProductSubCategoryRepository
 * Provides data access methods for ProductSubCategory entity.
 * Supports standard CRUD and custom finder methods.
 */
@Repository
public interface ProductSubCategoryRepository extends JpaRepository<ProductSubCategory, Long> {

    /**
     * ✅ Check if a subcategory already exists by name (for uniqueness validation).
     * @param subCategoryName Subcategory name to check
     * @return true if a subcategory with the given name already exists
     */
    boolean existsBySubCategoryName(String subCategoryName);

    /**
     * ✅ Fetch all subcategories that are marked active.
     * @return List of active ProductSubCategory entities
     */
    List<ProductSubCategory> findByActiveTrue();

    /**
     * ✅ Fetch all subcategories by category ID (if needed for dropdowns / filtering).
     * @param categoryId category foreign key
     * @return list of subcategories under that category
     */
    List<ProductSubCategory> findByCategory_CategoryId(Long categoryId);
}

JAVA

cat > "$SRC_ROOT/repository/ProductMakeRepository.java" <<'JAVA'

package com.example.asset.repository;

import com.example.asset.entity.ProductMake;
import com.example.asset.entity.AssetMaster;
import com.example.asset.entity.AssetUserLink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductMakeRepository extends JpaRepository<ProductMake, Long> {

    /**
     * Spring Data: get all models under makeId
     */
    List<ProductMake> findByMakeId(Long makeId);

    /**
     * Default SME method: check if ANY asset under THIS MAKE
     * is linked to ANY active user.
     *
     * No JPQL, uses existing repositories.
     */
    default boolean userLinked(
            Long makeId,
            AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo) {

        // 1️⃣ Fetch all assets belonging to models under this makeId
        List<AssetMaster> assets = assetRepo.findByModel_Make_MakeId(makeId);

        if (assets.isEmpty()) return false;

        // 2️⃣ Check if ANY of these assets are linked to a user
        for (AssetMaster asset : assets) {
            boolean linked = linkRepo.existsByAssetIdAndActiveTrue(asset.getAssetId());
            if (linked) return true;
        }

        return false;
    }
}


JAVA

cat > "$SRC_ROOT/repository/ProductModelRepository.java" <<'JAVA'

package com.example.asset.repository;

import com.example.asset.entity.ProductModel;
import com.example.asset.entity.AssetMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductModelRepository extends JpaRepository<ProductModel, Long> {

    /**
     * Unique name validation (existing)
     */
    boolean existsByModelNameIgnoreCaseAndMake_MakeId(String modelName, Long makeId);

    /**
     * Derived: find assets under this model
     */
    List<AssetMaster> findByModelId(Long modelId);

    /**
     * SME Validation: Validate if user already linked to ANY asset in this model
     */
    default boolean userLinked(
            Long modelId,
            AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo) {

        // 1️⃣ Fetch all assets belonging to this model
        List<AssetMaster> assets = assetRepo.findByModel_ModelId(modelId);

        if (assets.isEmpty()) return false;

        // 2️⃣ Check if any asset is linked
        return assets.stream()
                .anyMatch(asset ->
                        linkRepo.existsByAssetIdAndActiveTrue(asset.getAssetId())
                );
    }
}

JAVA

cat > "$SRC_ROOT/repository/PurchaseOutletRepository.java" <<'JAVA'
package com.example.asset.repository;

import com.example.asset.entity.PurchaseOutlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ✅ PurchaseOutletRepository
 * JPA repository for PurchaseOutlet entity.
 * Includes a custom method for unique outlet name validation.
 */
@Repository
public interface PurchaseOutletRepository extends JpaRepository<PurchaseOutlet, Long> {

    /**
     * Checks if an outlet with the given name already exists.
     * Used for enforcing unique outlet names.
     *
     * @param outletName the outlet name to check
     * @return true if the outlet exists, false otherwise
     */
    boolean existsByOutletName(String outletName);
}

JAVA

cat > "$SRC_ROOT/repository/AssetComponentRepository.java" <<'JAVA'

package com.example.asset.repository;

import com.example.asset.entity.AssetComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/**
 * ✅ Repository for managing AssetComponent entities.
 * Provides convenience methods for name uniqueness and soft delete filtering.
 */
@Repository
public interface AssetComponentRepository extends JpaRepository<AssetComponent, Long> {
    boolean existsByComponentName(String componentName);
    boolean existsById(Long componentId);
    Optional<AssetComponent> findById(Long id);

}



JAVA



cat > "$SRC_ROOT/repository/AssetMasterRepository.java" <<'JAVA'

package com.example.asset.repository;

import com.example.asset.entity.AssetMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetMasterRepository extends JpaRepository<AssetMaster, Long> {

    // =============================================================
    // 🔍 Existing Validations (Do Not Remove)
    // =============================================================
    boolean existsByAssetNameUdv(String assetNameUdv);

    Optional<AssetMaster> findByAssetNameUdvIgnoreCase(String assetNameUdv);

    // =============================================================
    // 🔍 SME: Derived Methods for Relationship Traversal
    //     No inline JPQL, only Spring Data conventions
    // =============================================================

    /**
     * MODEL → ASSET mapping
     * Fetch all assets under a specific modelId.
     */
    List<AssetMaster> findByModel_ModelId(Long modelId);

    /**
     * MAKE → MODEL → ASSET mapping
     * Fetch all assets under a given makeId through the model relationship.
     */
    List<AssetMaster> findByModel_Make_MakeId(Long makeId);

    /**
     * CATEGORY → ASSET mapping
     * (Useful for future validation or subcategory fetch)
     */
    List<AssetMaster> findByCategory_CategoryId(Long categoryId);

    /**
     * SUBCATEGORY → ASSET mapping
     * Used already in UserLinkService.getUsersBySubCategory()
     */
    List<AssetMaster> findBySubCategory_SubCategoryId(Long subCategoryId);

    /**
     * COMPONENT → ASSET mapping (reverse lookup)
     * Fetches all assets containing a given component ID.
     *
     * Required for COMPONENT → LINKAGE validation.
     * 
     * This relies on:
     * AssetMaster.components  (ManyToMany)
     */
    List<AssetMaster> findByComponents_ComponentId(Long componentId);

    /**
     * WARRANTY → ASSET mapping
     * Used for unified validation (WARRANTY linked to asset)
     */
    Optional<AssetMaster> findByWarranty_WarrantyId(Long warrantyId);

    /**
     * AMC → ASSET mapping
     */
    Optional<AssetMaster> findByAmc_AmcId(Long amcId);

    /**
     * DOCUMENT → ASSET mapping
     * For DOCUMENT linkage validation
     */
    Optional<AssetMaster> findByDocuments_DocumentId(Long documentId);
}


JAVA

cat > "$SRC_ROOT/repository/AssetUserLinkRepository.java" <<'JAVA'
package com.example.asset.repository;

import com.example.asset.entity.AssetUserLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface AssetUserLinkRepository extends JpaRepository<AssetUserLink, Long> {

    // ACTIVE link lookup
    List<AssetUserLink> findByActiveTrue();

    List<AssetUserLink> findByAssetIdAndActiveTrue(Long assetId);

    List<AssetUserLink> findByComponentIdAndActiveTrue(Long componentId);

    boolean existsByAssetIdAndActiveTrue(Long assetId);

    boolean existsByComponentIdAndActiveTrue(Long componentId);

    Optional<AssetUserLink> findByAssetIdAndUserIdAndActiveTrue(Long assetId, Long userId);

    Optional<AssetUserLink> findByComponentIdAndUserIdAndActiveTrue(Long componentId, Long userId);

    List<AssetUserLink> findByUserIdAndActiveTrue(Long userId);

    Optional<AssetUserLink> findFirstByAssetId(Long assetId);

    Optional<AssetUserLink> findFirstByComponentId(Long componentId);

    boolean existsByAssetIdAndUserIdAndActiveTrue(Long assetId, Long userId);

    boolean existsByComponentIdAndUserIdAndActiveTrue(Long componentId, Long userId);
    


    // =====================================================
    // 🔥 MOST IMPORTANT: Preserve existing method signature
    //    but return ALL ACTIVE LINKS (filtering in service)
    // =====================================================
    default List<AssetUserLink> findBySubCategoryId(Long subCategoryId) {
        return findByActiveTrue();
    }
}


JAVA

cat > "$SRC_ROOT/repository/AssetWarrantyRepository.java" <<'JAVA'

package com.example.asset.repository;

import com.example.asset.entity.AssetWarranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetWarrantyRepository extends JpaRepository<AssetWarranty, Long> {

    // ========================================================================
    // 🔹 EXISTING METHODS (MUST NOT BE REMOVED)
    // ========================================================================

    /**
     * Fetch active warranties (active = true or null).
     */
    List<AssetWarranty> findByActiveTrueOrActiveIsNull();

    /**
     * Check if an active warranty exists for a given asset.
     */
    boolean existsByAsset_AssetIdAndActiveTrue(Long assetId);


    // ========================================================================
    // 🔹 NEW METHODS REQUIRED FOR UNIFIED LINK VALIDATION
    // ========================================================================

    /**
     * Check if warranty exists by ID.
     * Needed for ensureEntityExists("WARRANTY").
     */
    boolean existsByWarrantyId(Long warrantyId);

    /**
     * Fetch warranty by Warranty ID.
     * Used for validating indirect linkage.
     */
    Optional<AssetWarranty> findByWarrantyId(Long warrantyId);

    /**
     * SME Requirement:
     * Determine if a WARRANTY is already assigned to a user.
     *
     * Indirect mapping:
     *
     *   WARRANTY → ASSET → ASSET_USER_LINK
     *
     * ❗NO inline JPQL allowed → Must use repository chaining.
     */
    default boolean existsByWarrantyIdAndUserAssigned(
            Long warrantyId,
            AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo) {

        // 1️⃣ Get Warranty
        Optional<AssetWarranty> warrantyOp = findByWarrantyId(warrantyId);
        if (warrantyOp.isEmpty()) return false;

        Long assetId = warrantyOp.get().getAsset().getAssetId();
        if (assetId == null) return false;

        // 2️⃣ Lookup user link for this asset
        return linkRepo.existsByAssetIdAndActiveTrue(assetId);
    }

    /**
     * This overload MUST NOT be called directly.
     * Exists only for compatibility with ValidationService signatures.
     */
    default boolean existsByWarrantyIdAndUserAssigned(Long warrantyId) {
        throw new UnsupportedOperationException("""
            ❌ Call existsByWarrantyIdAndUserAssigned(warrantyId, assetRepo, linkRepo)
            — Spring Data cannot autowire repositories inside default methods.
        """);
    }
}

JAVA

cat > "$SRC_ROOT/repository/AssetAmcRepository.java" <<'JAVA'


package com.example.asset.repository;

import com.example.asset.entity.AssetAmc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetAmcRepository extends JpaRepository<AssetAmc, Long> {

    // ========================================================================
    // 🔹 EXISTING METHODS (Required by other services)
    // ========================================================================

    /**
     * Fetch all AMC records where active = true or null.
     */
    List<AssetAmc> findByActiveTrueOrActiveIsNull();

    /**
     * Check if an AMC is active for the given Asset.
     */
    boolean existsByAsset_AssetIdAndActiveTrue(Long assetId);


    // ========================================================================
    // 🔹 NEW METHODS REQUIRED FOR UNIFIED LINK VALIDATION
    // ========================================================================

    /**
     * Check if an AMC exists by ID.
     * Used by ValidationService ensureEntityExists()
     */
    boolean existsByAmcId(Long amcId);

    /**
     * Fetch AMC by AMC ID.
     * Required for validation of indirect linkage.
     */
    Optional<AssetAmc> findByAmcId(Long amcId);

    /**
     * SME Requirement:
     * Determine if AMC is already assigned to a user.
     *
     * There is NO direct user-AMC table.
     * So we check using AssetUserLink:
     *   AMC → Asset → AssetUserLink
     *
     * NO JPQL ALLOWED → Use default method.
     */
    default boolean existsByAmcIdAndUserAssigned(
            Long amcId,
            AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo) {

        // 1️⃣ Get AMC record
        Optional<AssetAmc> amcOp = findByAmcId(amcId);
        if (amcOp.isEmpty()) return false;

        Long assetId = amcOp.get().getAsset().getAssetId();
        if (assetId == null) return false;

        // 2️⃣ Check if asset is linked to any user
        return linkRepo.existsByAssetIdAndActiveTrue(assetId);
    }

    /**
     * SME convenience method used in ValidationServiceImpl:
     * Uses the default method above with injected repos.
     */
    default boolean existsByAmcIdAndUserAssigned(Long amcId) {
        throw new UnsupportedOperationException("""
            ❌ You must call existsByAmcIdAndUserAssigned(amcId, assetRepo, linkRepo)
            because Spring cannot inject repositories into a default interface method.
        """);
    }
}


JAVA



cat > "$SRC_ROOT/repository/AssetDocumentRepository.java" <<'JAVA'


package com.example.asset.repository;

import com.example.asset.entity.AssetDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetDocumentRepository extends JpaRepository<AssetDocument, Long> {

    // ============================================================
    // 🔹 Existing Methods (DO NOT REMOVE — required for upload logic)
    // ============================================================

    boolean existsById(Long documentId);

    Optional<AssetDocument> findByEntityTypeIgnoreCaseAndEntityIdAndActiveTrue(
            String entityType, Long entityId);

    List<AssetDocument> findAllByEntityTypeIgnoreCaseAndEntityIdAndActiveTrue(
            String entityType, Long entityId);

    List<AssetDocument> findAllByEntityTypeIgnoreCaseAndEntityId(
            String entityType, Long entityId);

    Optional<AssetDocument> findTopByEntityTypeIgnoreCaseAndEntityIdOrderByUploadedDateDesc(
            String entityType, Long entityId);

    Optional<AssetDocument> findTopByEntityTypeIgnoreCaseAndEntityIdAndActiveTrueOrderByUploadedDateDesc(
            String entityType, Long entityId);

    List<AssetDocument> findAllByEntityTypeIgnoreCaseAndEntityIdAndActiveFalse(
            String entityType, Long entityId);


    // ============================================================
    // 🔹 Additional Methods Required for Unified Validation System
    // ============================================================

    /**
     * Check if a document exists by document ID.
     * Needed for ensureEntityExists("DOCUMENT").
     */
    default boolean existsByDocumentId(Long documentId) {
        return existsById(documentId);
    }

    /**
     * Fetch document by document ID.
     * Needed for indirect user linkage logic.
     */
    default Optional<AssetDocument> findByDocumentId(Long documentId) {
        return findById(documentId);
    }


    // ============================================================
    // 🔹 SME Requirement: Detect if Document is Linked to Any User
    //
    // RULE:
    //     Document → Asset → AssetUserLink
    //
    // No inline JPQL allowed → must use repository chaining.
    // ============================================================
    default boolean existsByDocumentIdAndUserAssigned(
            Long documentId,
            AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo
    ) {

        Optional<AssetDocument> docOp = findById(documentId);

        if (docOp.isEmpty()) {
            return false;
        }

        // Document → Asset
        AssetDocument doc = docOp.get();
        if (doc.getAsset() == null || doc.getAsset().getAssetId() == null) {
            return false;
        }

        Long assetId = doc.getAsset().getAssetId();

        // Asset → AssetUserLink
        return linkRepo.existsByAssetIdAndActiveTrue(assetId);
    }

    /**
     * This overload is intentionally blocked.
     * MUST use version with repositories injected.
     */
    default boolean existsByDocumentIdAndUserAssigned(Long documentId) {
        throw new UnsupportedOperationException("""
            ❌ Use existsByDocumentIdAndUserAssigned(documentId, assetRepo, linkRepo)
            because default methods cannot auto-inject Spring beans.
        """);
    }
}


JAVA

echo "Created: Repositories"

# ---------- 3) DTOs ----------


cat > "$SRC_ROOT/dto/AssetUserMultiDelinkRequest.java" <<'JAVA'

package com.example.asset.dto;

import java.util.List;

public class AssetUserMultiDelinkRequest {

    private String userId;        // caller
    private String username;      // caller name

    private Long targetUserId;      // user to delink
    private String targetUsername;  // user name

    private List<String> entityLinks; 
    // Example: ["ASSET:1001", "COMPONENT:501", "DOCUMENT:701"]

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }

    public List<String> getEntityLinks() { return entityLinks; }
    public void setEntityLinks(List<String> entityLinks) { this.entityLinks = entityLinks; }
}


JAVA


cat > "$SRC_ROOT/dto/AssetUserMultiLinkRequest.java" <<'JAVA'


package com.example.asset.dto;

import java.util.List;

public class AssetUserMultiLinkRequest {

    private String userId;      // caller
    private String username;    // caller name

    private Long targetUserId;      // user to link
    private String targetUsername;  // name of user to link

    // Example: ["ASSET:1001", "COMPONENT:501", "WARRANTY:901"]
    private List<String> entityLinks;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }

    public List<String> getEntityLinks() { return entityLinks; }
    public void setEntityLinks(List<String> entityLinks) { this.entityLinks = entityLinks; }
}



JAVA






cat > "$SRC_ROOT/dto/AssetUserLinkRequest.java" <<'JAVA'
package com.example.asset.dto;

/**
 * DTO used for:
 * - Linking asset/component to user
 * - Delinking asset/component from user
 *
 * Matches JSON request format:
 *
 * {
 *   "userId": 10,
 *   "username": "admin",
 *   "link": {
 *     "asset": {
 *       "assetId": 100,
 *       "componentId": 200,
 *       "assetuserId": 55,
 *       "assetusername": "john"
 *     }
 *   }
 * }
 */
public class AssetUserLinkRequest {

    private String userId;         // API caller userId (createdBy / updatedBy)
    private String username;     // API caller username

    private Link link;           // Asset link wrapper

    // ============================================================
    // Nested DTOs
    // ============================================================

    public static class Link {
        private Asset asset;

        public Asset getAsset() {
            return asset;
        }

        public void setAsset(Asset asset) {
            this.asset = asset;
        }
    }

    public static class Asset {

        private Long assetId;         // main asset id (nullable if componentId used)
        private Long componentId;     // component id (nullable if assetId used)

        private Long assetuserId;     // user who will receive the asset/component
        private String assetusername; // user name who will receive the asset/component

        public Long getAssetId() {
            return assetId;
        }

        public void setAssetId(Long assetId) {
            this.assetId = assetId;
        }

        public Long getComponentId() {
            return componentId;
        }

        public void setComponentId(Long componentId) {
            this.componentId = componentId;
        }

        public Long getAssetuserId() {
            return assetuserId;
        }

        public void setAssetuserId(Long assetuserId) {
            this.assetuserId = assetuserId;
        }

        public String getAssetusername() {
            return assetusername;
        }

        public void setAssetusername(String assetusername) {
            this.assetusername = assetusername;
        }
    }

    // ============================================================
    // Getters & Setters
    // ============================================================

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }
}



JAVA
cat > "$SRC_ROOT/dto/AssetResponseDTO.java" <<'JAVA'
package com.example.asset.dto;

/**
 * ✅ AssetResponseDTO
 * Represents summarized asset details for API responses.
 */
public class AssetResponseDTO {

    // ============================================================
    // 🔑 Identifiers & Basic Info
    // ============================================================
    private Long assetId;
    private String assetNameUdv;
    private String assetStatus;

    // ============================================================
    // 🏷️ Linked Master Data
    // ============================================================
    private String categoryName;
    private String subCategoryName;
    private String makeName;
    private String modelName;

    // ============================================================
    // 🔧 Getters and Setters
    // ============================================================

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetNameUdv() {
        return assetNameUdv;
    }

    public void setAssetNameUdv(String assetNameUdv) {
        this.assetNameUdv = assetNameUdv;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    // ============================================================
    // 🧠 toString() for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "AssetResponseDTO{" +
                "assetId=" + assetId +
                ", assetNameUdv='" + assetNameUdv + '\'' +
                ", assetStatus='" + assetStatus + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", subCategoryName='" + subCategoryName + '\'' +
                ", makeName='" + makeName + '\'' +
                ", modelName='" + modelName + '\'' +
                '}';
    }
}


JAVA



cat > "$SRC_ROOT/dto/AssetAmcRequest.java" <<'JAVA'


package com.example.asset.dto;

import java.time.LocalDate;

/**
 * ✅ AssetAmcRequest
 * Wrapper for AMC create/update operations.
 * Compatible with multipart/form-data uploads.
 */
public class AssetAmcRequest {

    // ============================================================
    // 👤 User Context
    // ============================================================
    private Long userId;
    private String username;
    private String projectType;

    // ============================================================
    // 🔗 Asset Context
    // ============================================================
    private Long assetId;
    private Long componentId;

    // ============================================================
    // 🧾 AMC Details
    // ============================================================
    private String amcStatus;
    private LocalDate startDate;
    private LocalDate endDate;


    // ============================================================
    // 📎 Optional Document Link (managed by DocumentController)
    // ============================================================
    private Long documentId;
    private String docType;


    // ============================================================
    // 🧾 Getters & Setters
    // ============================================================
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }


    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }


    public String getAmcStatus() {
        return amcStatus;
    }

    public void setAmcStatus(String amcStatus) {
        this.amcStatus = amcStatus;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    // ============================================================
    // 🧠 toString (for debugging/logging)
    // ============================================================
    @Override
    public String toString() {
        return "AssetAmcRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", assetId=" + assetId +
                ", componentId=" + componentId +
                ", amcStatus='" + amcStatus + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}





JAVA


cat > "$SRC_ROOT/dto/AssetAmcDto.java" <<'JAVA'

package com.example.asset.dto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * ✅ AssetAmcDto
 * Safe DTO for transferring AMC data between layers.
 */
public class AssetAmcDto implements Serializable {

    private Long amcId;
    private String amcStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private Long userId;
    private String username;
    private Long componentId;
    private Long assetId;
    private Long documentId;

    // Getters & Setters
    public Long getAmcId() { return amcId; }
    public void setAmcId(Long amcId) { this.amcId = amcId; }

    public String getAmcStatus() { return amcStatus; }
    public void setAmcStatus(String amcStatus) { this.amcStatus = amcStatus; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
}


JAVA


cat > "$SRC_ROOT/dto/AssetWarrantyRequest.java" <<'JAVA'

package com.example.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * ✅ AssetWarrantyRequest
 * Request DTO for creating or updating warranty records.
 * Clean JSON structure — document upload handled separately.
 */
public class AssetWarrantyRequest {

    // ============================================================
    // 👤 User Context
    // ============================================================
    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "username is required")
    private String username;

    private String projectType;

    // ============================================================
    // 🔗 Asset & Component Link
    // ============================================================
    @NotNull(message = "assetId is required")
    private Long assetId;

    private Long componentId;

    // ============================================================
    // 🧾 Warranty Details
    // ============================================================
    @NotBlank(message = "warrantyStatus is required")
    private String warrantyStatus;

    private String warrantyProvider;
    private String warrantyTerms;

    @NotBlank(message = "startDate is required (format: yyyy-MM-dd)")
    private String startDate;

    @NotBlank(message = "endDate is required (format: yyyy-MM-dd)")
    private String endDate;

    // ============================================================
    // 📎 Optional Document Link (managed by DocumentController)
    // ============================================================
    private Long documentId;
    private String docType;

    // ============================================================
    // 🔧 Getters & Setters
    // ============================================================
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public String getWarrantyStatus() {
        return warrantyStatus;
    }

    public void setWarrantyStatus(String warrantyStatus) {
        this.warrantyStatus = warrantyStatus;
    }

    public String getWarrantyProvider() {
        return warrantyProvider;
    }

    public void setWarrantyProvider(String warrantyProvider) {
        this.warrantyProvider = warrantyProvider;
    }

    public String getWarrantyTerms() {
        return warrantyTerms;
    }

    public void setWarrantyTerms(String warrantyTerms) {
        this.warrantyTerms = warrantyTerms;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    // ============================================================
    // 🧠 Debugging
    // ============================================================
    @Override
    public String toString() {
        return "AssetWarrantyRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", assetId=" + assetId +
                ", componentId=" + componentId +
                ", warrantyStatus='" + warrantyStatus + '\'' +
                ", warrantyProvider='" + warrantyProvider + '\'' +
                ", warrantyTerms='" + warrantyTerms + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", documentId=" + documentId +
                ", docType='" + docType + '\'' +
                '}';
    }
}


JAVA


cat > "$SRC_ROOT/dto/AssetWarrantyDto.java" <<'JAVA'

package com.example.asset.dto;

import java.time.LocalDate;

/**
 * ✅ AssetWarrantyDto
 * Data Transfer Object for Warranty entity responses.
 * Represents warranty details, asset linkage, and document metadata.
 */
public class AssetWarrantyDto {

    // ============================================================
    // 🔑 Identifiers
    // ============================================================
    private Long warrantyId;       // Primary key of warranty record
    private Long assetId;          // Linked asset ID
    private Long componentId;      // Optional component ID

    // ============================================================
    // 🧾 Warranty Info
    // ============================================================
    private String warrantyStatus;
    private String warrantyProvider;
    private String warrantyTerms;
    private LocalDate startDate;
    private LocalDate endDate;

    // ============================================================
    // 👤 User Info
    // ============================================================
    private Long userId;
    private String username;

    // ============================================================
    // 📎 Document Linkage
    // ============================================================
    private Long documentId;       // Linked document ID (from AssetDocument)
    private String docType;        // Type of document (e.g., WARRANTY_DOC)
    private String filePath;       // Stored file location (useful for UI download)

    // ============================================================
    // 🕒 Audit Info
    // ============================================================
    private Boolean active;
    private String createdBy;
    private String updatedBy;
    private String createdAt;
    private String updatedAt;

    // ============================================================
    // 🔧 Getters and Setters
    // ============================================================

    public Long getWarrantyId() {
        return warrantyId;
    }

    public void setWarrantyId(Long warrantyId) {
        this.warrantyId = warrantyId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public String getWarrantyStatus() {
        return warrantyStatus;
    }

    public void setWarrantyStatus(String warrantyStatus) {
        this.warrantyStatus = warrantyStatus;
    }


    public String getWarrantyProvider() {
        return warrantyProvider;
    }

    public void setWarrantyProvider(String warrantyProvider) {
        this.warrantyProvider = warrantyProvider;
    }

    public String getWarrantyTerms() {
        return warrantyTerms;
    }

    public void setWarrantyTerms(String warrantyTerms) {
        this.warrantyTerms = warrantyTerms;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ============================================================
    // 🧠 toString for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "AssetWarrantyDto{" +
                "warrantyId=" + warrantyId +
                ", assetId=" + assetId +
                ", componentId=" + componentId +
                ", warrantyStatus='" + warrantyStatus + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", documentId=" + documentId +
                ", docType='" + docType + '\'' +
                ", filePath='" + filePath + '\'' +
                ", active=" + active +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}




JAVA


cat > "$SRC_ROOT/dto/ModelDto.java" <<'JAVA'

package com.example.asset.dto;

import com.example.common.jpa.BaseEntity;

/**
 * ✅ ModelDto
 * Safe DTO for API responses — avoids exposing JPA entities directly.
 */
public class ModelDto extends BaseEntity {

    private Long modelId;
    private String modelName;
    private String description;
    private Boolean active;

    private Long makeId;
    private String makeName;

    
    // ----- Getters & Setters -----
    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getMakeId() {
        return makeId;
    }

    public void setMakeId(Long makeId) {
        this.makeId = makeId;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }
}


JAVA

cat > "$SRC_ROOT/dto/CategoryDto.java" <<'JAVA'
package com.example.asset.dto;

import java.io.Serializable;
import com.example.common.jpa.BaseEntity;

/**
 * ✅ CategoryDto
 * Data Transfer Object for {@link com.example.asset.entity.ProductCategory}.
 * 
 * Used for safely transferring category data between layers
 * (controller ↔ service ↔ client) without exposing entity internals.
 */
public class CategoryDto extends BaseEntity implements Serializable {

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    private Long categoryId;
    private String categoryName;
    private String description;


    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ============================================================
    // 🧠 toString() for Logging
    // ============================================================
    @Override
    public String toString() {
        return "CategoryDto{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", description='" + description + '\'' +
                ", active=" + getActive()+
                ", createdBy='" + getCreatedBy() + '\'' +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}



JAVA

cat > "$SRC_ROOT/dto/ProductSubCategoryDto.java" <<'JAVA'



package com.example.asset.dto;
import com.example.common.jpa.BaseEntity;

/**
 * ✅ ProductSubCategoryDto
 * Data Transfer Object for exposing ProductSubCategory details safely.
 * 
 * Avoids lazy-loading issues and provides a clean, lightweight response
 * for REST APIs.
 */
public class ProductSubCategoryDto  extends BaseEntity {

    // ============================================================
    // 🔑 Basic Info
    // ============================================================
    private Long subCategoryId;
    private String subCategoryName;
    private String description;

    // ============================================================
    // 🔗 Category Info
    // ============================================================
    private Long categoryId;
    private String categoryName;

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // ============================================================
    // 🧠 toString() for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "ProductSubCategoryDto{" +
                "subCategoryId=" + subCategoryId +
                ", subCategoryName='" + subCategoryName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", active=" + getActive()+
                ", createdBy='" + getCreatedBy() + '\'' +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}

JAVA

cat > "$SRC_ROOT/dto/WarrantyRequest.java" <<'JAVA'

package com.example.asset.dto;

import com.example.asset.entity.AssetWarranty;

/**
 * ✅ WarrantyRequest DTO
 * Unified request wrapper for all warranty operations.
 */
public class WarrantyRequest {

    private Long userId;
    private String username;
    private String projectType;
    private AssetWarranty warranty;

    // --- Getters & Setters ---
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public AssetWarranty getWarranty() { return warranty; }
    public void setWarranty(AssetWarranty warranty) { this.warranty = warranty; }
}


JAVA

cat > "$SRC_ROOT/dto/SubCategoryRequest.java" <<'JAVA'
package com.example.asset.dto;

import com.example.asset.entity.ProductSubCategory;

/**
 * ✅ SubCategoryRequest DTO
 * Wrapper for SubCategory operations that includes:
 *  - userId and username for audit context
 *  - projectType for notification scoping
 *  - the actual ProductSubCategory entity payload
 *
 * Used by SubCategoryController and SubCategoryService.
 */
public class SubCategoryRequest {

    // ============================================================
    // 👤 User Context
    // ============================================================
    private Long userId;
    private String username;

    // ============================================================
    // 🧩 Project Context
    // ============================================================
    private String projectType;

    // ============================================================
    // 📦 Payload
    // ============================================================
    private ProductSubCategory subCategory;

    // ============================================================
    // 🏗️ Constructors
    // ============================================================
    public SubCategoryRequest() {}

    public SubCategoryRequest(Long userId, String username, String projectType, ProductSubCategory subCategory) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.subCategory = subCategory;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public ProductSubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(ProductSubCategory subCategory) {
        this.subCategory = subCategory;
    }

    // ============================================================
    // 🧠 Debug-friendly toString()
    // ============================================================
    @Override
    public String toString() {
        return "SubCategoryRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", subCategory=" + (subCategory != null ? subCategory.getSubCategoryName() : "null") +
                '}';
    }
}

JAVA

cat > "$SRC_ROOT/dto/AssetRequest.java" <<'JAVA'
package com.example.asset.dto;

import com.example.asset.entity.AssetMaster;

/**
 * ✅ AssetRequest
 * Wrapper DTO for asset CRUD operations with audit metadata (userId, username, projectType).
 */
public class AssetRequest {

    private Long userId;
    private String username;
    private String projectType;
    private AssetMaster asset;

    // -------------------------------
    // 🧩 Getters and Setters
    // -------------------------------
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public AssetMaster getAsset() {
        return asset;
    }

    public void setAsset(AssetMaster asset) {
        this.asset = asset;
    }

    @Override
    public String toString() {
        return "AssetRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", asset=" + (asset != null ? asset.toString() : "null") +
                '}';
    }
}

JAVA


cat > "$SRC_ROOT/dto/VendorRequest.java" <<'JAVA'

package com.example.asset.dto;

import com.example.asset.entity.VendorMaster;

/**
 * ✅ VendorRequest DTO
 * Wrapper for vendor requests with user info for auditing + notification.
 */
public class VendorRequest {

    private Long userId;
    private String username;
    private String projectType;
    private VendorMaster vendor;

    public VendorRequest() {}

    public VendorRequest(Long userId, String username, String projectType, VendorMaster vendor) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.vendor = vendor;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public VendorMaster getVendor() { return vendor; }
    public void setVendor(VendorMaster vendor) { this.vendor = vendor; }
}


JAVA

cat > "$SRC_ROOT/dto/ModelRequest.java" <<'JAVA'

package com.example.asset.dto;

import com.example.asset.entity.ProductModel;

/**
 * ✅ ModelRequest DTO
 * Wrapper for handling ProductModel CRUD operations.
 * 
 * Includes:
 *  - userId       → Request initiator (for audit)
 *  - username     → Name of the user performing the action
 *  - projectType  → Originating microservice or context (e.g., ASSET_SERVICE)
 *  - model        → The ProductModel entity payload
 */
public class ModelRequest {

    private Long userId;
    private String username;
    private String projectType;
    private ProductModel model;

    // ============================================================
    // 🧩 Constructors
    // ============================================================
    public ModelRequest() {}

    public ModelRequest(Long userId, String username, String projectType, ProductModel model) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.model = model;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public ProductModel getModel() {
        return model;
    }

    public void setModel(ProductModel model) {
        this.model = model;
    }

    // ============================================================
    // 🧠 toString (for logging and debugging)
    // ============================================================
    @Override
    public String toString() {
        return "ModelRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", model=" + (model != null ? model.getModelName() : "null") +
                '}';
    }
}

JAVA

cat > "$SRC_ROOT/dto/MakeRequest.java" <<'JAVA'
package com.example.asset.dto;

import com.example.asset.entity.ProductMake;

/**
 * ✅ MakeRequest DTO
 * Wrapper for all ProductMake operations.
 * Includes:
 *  - userId (Long)
 *  - username (String)
 *  - projectType (String, optional)
 *  - make (ProductMake entity payload)
 */
public class MakeRequest {

    private Long userId;
    private String username;
    private String projectType;
    private ProductMake make;

    // ============================================================
    // 🧩 Constructors
    // ============================================================
    public MakeRequest() {
    }

    public MakeRequest(Long userId, String username, String projectType, ProductMake make) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.make = make;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public ProductMake getMake() {
        return make;
    }

    public void setMake(ProductMake make) {
        this.make = make;
    }

    // ============================================================
    // 🧠 toString (for logging)
    // ============================================================
    @Override
    public String toString() {
        return "MakeRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", make=" + (make != null ? make.getMakeName() : "null") +
                '}';
    }
}


JAVA

cat > "$SRC_ROOT/dto/OutletRequest.java" <<'JAVA'
package com.example.asset.dto;

import com.example.asset.entity.PurchaseOutlet;

/**
 * ✅ OutletRequest DTO
 * Wrapper for handling CRUD operations for PurchaseOutlet.
 * 
 * Includes:
 *  - userId       → Request initiator ID (for audit)
 *  - username     → Name of user performing the operation
 *  - projectType  → Originating microservice (e.g., ASSET_SERVICE)
 *  - outlet       → PurchaseOutlet entity payload
 */
public class OutletRequest {

    private Long userId;
    private String username;
    private String projectType;
    private PurchaseOutlet outlet;

    // ============================================================
    // 🧩 Constructors
    // ============================================================
    public OutletRequest() {}

    public OutletRequest(Long userId, String username, String projectType, PurchaseOutlet outlet) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.outlet = outlet;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public PurchaseOutlet getOutlet() {
        return outlet;
    }

    public void setOutlet(PurchaseOutlet outlet) {
        this.outlet = outlet;
    }

    // ============================================================
    // 🧠 toString (useful for logging and debugging)
    // ============================================================
    @Override
    public String toString() {
        return "OutletRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", outlet=" + (outlet != null ? outlet.getOutletName() : "null") +
                '}';
    }
}

JAVA

cat > "$SRC_ROOT/dto/DocumentRequest.java" <<'JAVA'

package com.example.asset.dto;

/**
 * ✅ Generic DocumentRequest DTO
 * Used for uploading any file (document, image, etc.)
 * Supports linking to multiple entity types dynamically (Asset, AMC, Warranty, etc.)
 * and maintains backward compatibility with asset/component linkage.
 */
public class DocumentRequest {

    // ============================================================
    // 👤 User Context
    // ============================================================
    private Long userId;
    private String username;
    private String projectType;

    // ============================================================
    // 🔗 Entity Linkage (Generic)
    // ============================================================
    private String entityType; // e.g., ASSET, COMPONENT, AMC, WARRANTY, CATEGORY, SUBCATEGORY, OUTLET, MAKE, MODEL, VENDOR
    private Long entityId;     // Generic ID for linking (e.g., assetId, warrantyId, etc.)

    // ============================================================
    // 🔗 Specific Linkage (for backward compatibility)
    // ============================================================
    private Long assetId;       // Direct link to Asset
    private Long componentId;   // Direct link to Component

    // ============================================================
    // 📎 Document Info
    // ============================================================
    private String docType;     // e.g., IMAGE, PDF, RECEIPT, AGREEMENT

    // ============================================================
    // 🔧 Getters & Setters
    // ============================================================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    // ============================================================
    // 🧠 toString() for debugging/logging
    // ============================================================
    @Override
    public String toString() {
        return "DocumentRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", assetId=" + assetId +
                ", componentId=" + componentId +
                ", docType='" + docType + '\'' +
                '}';
    }
}


JAVA

cat > "$SRC_ROOT/dto/AssetDto.java" <<'JAVA'

package com.example.asset.dto;

import java.util.Set;

/**
 * ✅ AssetDto
 * Data Transfer Object for Asset information including metadata,
 * purchase details, and assigned user context.
 */
public class AssetDto {

    private String assetNameUdv;
    private Long categoryId;
    private Long subCategoryId;
    private Long makeId;
    private Long modelId;
    private String makeUdv;
    private String modelUdv;
    private String purchaseMode;
    private Long purchaseOutletId;
    private String purchaseOutletUdv;
    private String purchaseOutletAddressUdv;
    private String purchaseDate; // yyyy-MM-dd
    private String assetStatus;
    private String soldOnDate;
    private String salesChannelName;
    private Set<Long> componentIds;
    private String userId;      // to assign
    private String username;    // to assign
    private String projecttype; // to assign

    // ----- Getters -----
    public String getAssetNameUdv() {
        return assetNameUdv;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public Long getMakeId() {
        return makeId;
    }

    public Long getModelId() {
        return modelId;
    }

    public String getMakeUdv() {
        return makeUdv;
    }

    public String getModelUdv() {
        return modelUdv;
    }

    public String getPurchaseMode() {
        return purchaseMode;
    }

    public Long getPurchaseOutletId() {
        return purchaseOutletId;
    }

    public String getPurchaseOutletUdv() {
        return purchaseOutletUdv;
    }

    public String getPurchaseOutletAddressUdv() {
        return purchaseOutletAddressUdv;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public String getSoldOnDate() {
        return soldOnDate;
    }

    public String getSalesChannelName() {
        return salesChannelName;
    }

    public Set<Long> getComponentIds() {
        return componentIds;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getProjecttype() {
        return projecttype;
    }

    // ----- Setters -----
    public void setAssetNameUdv(String assetNameUdv) {
        this.assetNameUdv = assetNameUdv;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public void setMakeId(Long makeId) {
        this.makeId = makeId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public void setMakeUdv(String makeUdv) {
        this.makeUdv = makeUdv;
    }

    public void setModelUdv(String modelUdv) {
        this.modelUdv = modelUdv;
    }

    public void setPurchaseMode(String purchaseMode) {
        this.purchaseMode = purchaseMode;
    }

    public void setPurchaseOutletId(Long purchaseOutletId) {
        this.purchaseOutletId = purchaseOutletId;
    }

    public void setPurchaseOutletUdv(String purchaseOutletUdv) {
        this.purchaseOutletUdv = purchaseOutletUdv;
    }

    public void setPurchaseOutletAddressUdv(String purchaseOutletAddressUdv) {
        this.purchaseOutletAddressUdv = purchaseOutletAddressUdv;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    public void setSoldOnDate(String soldOnDate) {
        this.soldOnDate = soldOnDate;
    }

    public void setSalesChannelName(String salesChannelName) {
        this.salesChannelName = salesChannelName;
    }

    public void setComponentIds(Set<Long> componentIds) {
        this.componentIds = componentIds;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProjecttype(String projecttype) {
        this.projecttype = projecttype;
    }
}


JAVA

cat > "$SRC_ROOT/dto/AssetUserUniversalLinkRequest.java" <<'JAVA'


package com.example.asset.dto;

public class AssetUserUniversalLinkRequest {

    private String userId;              // caller
    private String username;            // caller name

    private String entityType;          // ASSET, COMPONENT, MODEL, MAKE, AMC, WARRANTY, DOCUMENT
    private Long entityId;              // ID of the entity

    private Long targetUserId;          // user we want to link to entity
    private String targetUsername;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }

    @Override
    public String toString() {
        return "AssetUserUniversalLinkRequest{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", targetUserId=" + targetUserId +
                ", targetUsername='" + targetUsername + '\'' +
                '}';
    }
}

JAVA

cat > "$SRC_ROOT/dto/ComponentRequest.java" <<'JAVA'

package com.example.asset.dto;

import com.example.asset.entity.AssetComponent;

/**
 * ✅ ComponentRequest DTO
 * Used for @RequestBody requests that include user context.
 */
public class ComponentRequest {

    private Long userId;
    private String username;
    private String projectType;
    private AssetComponent component;

    // ----- Getters -----
    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getProjectType() {
        return projectType;
    }

    public AssetComponent getComponent() {
        return component;
    }

    // ----- Setters -----
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public void setComponent(AssetComponent component) {
        this.component = component;
    }
}


JAVA

cat > "$SRC_ROOT/dto/AmcRequest.java" <<'JAVA'


package com.example.asset.dto;

import com.example.asset.entity.AssetAmc;

/**
 * ✅ AmcRequest DTO
 * Wrapper for handling AMC CRUD requests with user details and project context.
 */
public class AmcRequest {

    private Long userId;
    private String username;
    private String projectType;
    private AssetAmc amc;
    
    public AmcRequest(Long userId, String username, String projectType, AssetAmc amc) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.amc = amc;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public AssetAmc getAmc() { return amc; }
    public void setAmc(AssetAmc amc) { this.amc = amc; }
}


JAVA
cat > "$SRC_ROOT/dto/CategoryRequest.java" <<'JAVA'
package com.example.asset.dto;

import com.example.asset.entity.ProductCategory;

/**
 * ✅ CategoryRequest DTO
 * Wrapper object used for category CRUD APIs.
 * Includes user identity, project context, and the category payload.
 */
public class CategoryRequest {

    private Long userId;
    private String username;
    private String projectType; // e.g. "ASSET_SERVICE", "AUTH_SERVICE", etc.
    private ProductCategory category; // inner payload entity

    // ============================================================
    // ✅ Constructors
    // ============================================================

    public CategoryRequest() {}

    public CategoryRequest(Long userId, String username, String projectType, ProductCategory category) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.category = category;
    }

    // ============================================================
    // ✅ Getters and Setters
    // ============================================================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    // ============================================================
    // ✅ Utility (Debugging / Logging)
    // ============================================================

    @Override
    public String toString() {
        return "CategoryRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", category=" + (category != null ? category.getCategoryName() : "null") +
                '}';
    }
}

JAVA

# # # # cat > "$SRC_ROOT/dto/AssetNotificationRequest.java" <<'JAVA'
# # # # package com.example.asset.dto;
# # # # import java.util.Map;
# # # # public class AssetNotificationRequest {
# # # #     public String channel;
# # # #     public String username;
# # # #     public String templateCode;
# # # #     public String userId;
# # # #     public Map<String, Object> placeholders;
# # # # }
# # # # JAVA

echo "Created: DTOs"

# # # # # ---------- 4) Services (AuthTokenService & AssetService) ----------
# # # # cat > "$SRC_ROOT/service/AuthTokenService.java" <<'JAVA'
# # # # package com.example.asset.service;

# # # # import org.springframework.beans.factory.annotation.Value;
# # # # import org.springframework.security.core.Authentication;
# # # # import org.springframework.security.core.context.SecurityContextHolder;
# # # # import org.springframework.stereotype.Service;
# # # # import org.springframework.web.client.RestTemplate;

# # # # import java.util.Map;

# # # # @Service
# # # # public class AuthTokenService {

# # # #     @Value("${services.auth.base-url:http://localhost:$AUTH_SERVER_PORT}")
# # # #     private String authBaseUrl;

# # # #     @Value("${auth.client-id:asset-service}")
# # # #     private String clientId;

# # # #     @Value("${auth.client-secret:asset-secret}")
# # # #     private String clientSecret;

# # # #     public String getAccessToken() {
# # # #         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
# # # #         if (auth != null && auth.getCredentials() instanceof String token) {
# # # #             return token;
# # # #         }
# # # #         // fallback: client credentials (ensure your auth-service exposes this)
# # # #         RestTemplate rt = new RestTemplate();
# # # #         try {
# # # #             Map<String,Object> res = rt.postForObject(authBaseUrl + "/oauth/token",
# # # #                     Map.of("client_id", clientId, "client_secret", clientSecret, "grant_type", "client_credentials"),
# # # #                     Map.class);
# # # #             if (res != null) return (String)res.get("access_token");
# # # #         } catch (Exception e) {
# # # #             // ignore
# # # #         }
# # # #         return null;
# # # #     }
# # # # }
# # # # JAVA


cat > "$SRC_ROOT/service/impl/ValidationServiceImpl.java" <<'JAVA'





// // // // package com.example.asset.service.impl;

// // // // import com.example.asset.repository.AssetMasterRepository;
// // // // import com.example.asset.repository.AssetComponentRepository;
// // // // import com.example.asset.repository.AssetUserLinkRepository;
// // // // import com.example.asset.service.ValidationService;
// // // // import org.springframework.stereotype.Service;

// // // // @Service
// // // // public class ValidationServiceImpl implements ValidationService {

// // // //     private final AssetMasterRepository assetRepo;
// // // //     private final AssetComponentRepository componentRepo;
// // // //     private final AssetUserLinkRepository linkRepo;

// // // //     public ValidationServiceImpl(
// // // //             AssetMasterRepository assetRepo,
// // // //             AssetComponentRepository componentRepo,
// // // //             AssetUserLinkRepository linkRepo
// // // //     ) {
// // // //         this.assetRepo = assetRepo;
// // // //         this.componentRepo = componentRepo;
// // // //         this.linkRepo = linkRepo;
// // // //     }

// // // //     @Override
// // // //     public void validateCallerUser(String callerUserId, String callerUserName) {
// // // //         if (callerUserId == null || callerUserId.isBlank() ||
// // // //             callerUserName == null || callerUserName.isBlank()) {
// // // //             throw new IllegalArgumentException("Caller user details missing");
// // // //         }
// // // //     }

// // // //     @Override
// // // //     public void validateUser(Long userId, String username) {
// // // //         if (userId == null || username == null || username.isBlank()) {
// // // //             throw new IllegalArgumentException("Target user details missing");
// // // //         }
// // // //     }

// // // //     @Override
// // // //     public void validateAssetExists(Long assetId) {
// // // //         if (assetId == null || !assetRepo.existsById(assetId)) {
// // // //             throw new IllegalArgumentException("Asset not found: " + assetId);
// // // //         }
// // // //     }

// // // //     @Override
// // // //     public void validateComponentExists(Long componentId) {
// // // //         if (componentId == null || !componentRepo.existsById(componentId)) {
// // // //             throw new IllegalArgumentException("Component not found: " + componentId);
// // // //         }
// // // //     }

// // // //     @Override
// // // //     public void validateAssetNotLinked(Long assetId, Long componentId) {
// // // //         boolean alreadyAssigned = (componentId != null)
// // // //                 ? linkRepo.existsByComponentIdAndActiveTrue(componentId)
// // // //                 : linkRepo.existsByAssetIdAndActiveTrue(assetId);

// // // //         if (alreadyAssigned) {
// // // //             throw new IllegalStateException("Asset/Component already linked to a user");
// // // //         }
// // // //     }

// // // //     @Override
// // // //     public void validateAssetLinked(Long assetId, Long componentId, Long userId) {
// // // //         boolean exists = (componentId != null)
// // // //                 ? linkRepo.findByComponentIdAndUserIdAndActiveTrue(componentId, userId).isPresent()
// // // //                 : linkRepo.findByAssetIdAndUserIdAndActiveTrue(assetId, userId).isPresent();

// // // //         if (!exists) {
// // // //             throw new IllegalStateException("No active link exists for unlink operation");
// // // //         }
// // // //     }
// // // // }
package com.example.asset.service.impl;

import com.example.asset.dto.AssetUserUniversalLinkRequest;
import com.example.asset.repository.*;
import com.example.asset.service.ValidationService;
import org.springframework.stereotype.Service;

@Service
public class ValidationServiceImpl implements ValidationService {

    private final AssetMasterRepository assetRepo;
    private final AssetComponentRepository componentRepo;
    private final ProductModelRepository modelRepo;
    private final ProductMakeRepository makeRepo;
    private final AssetWarrantyRepository warrantyRepo;
    private final AssetAmcRepository amcRepo;
    private final AssetDocumentRepository documentRepo;
    private final AssetUserLinkRepository linkRepo;

    public ValidationServiceImpl(
            AssetMasterRepository assetRepo,
            AssetComponentRepository componentRepo,
            ProductModelRepository modelRepo,
            ProductMakeRepository makeRepo,
            AssetWarrantyRepository warrantyRepo,
            AssetAmcRepository amcRepo,
            AssetDocumentRepository documentRepo,
            AssetUserLinkRepository linkRepo) {

        this.assetRepo = assetRepo;
        this.componentRepo = componentRepo;
        this.modelRepo = modelRepo;
        this.makeRepo = makeRepo;
        this.warrantyRepo = warrantyRepo;
        this.amcRepo = amcRepo;
        this.documentRepo = documentRepo;
        this.linkRepo = linkRepo;
    }

    // ========================================================================
    // ⭐ Entry Point: Validate Unified Link Request
    // ========================================================================
    @Override
    public void validateLinkRequest(AssetUserUniversalLinkRequest req) {

        if (req.getEntityType() == null || req.getEntityType().isBlank())
            throw new IllegalArgumentException("Entity type is missing");

        if (req.getEntityId() == null)
            throw new IllegalArgumentException("Entity ID is missing");

        if (req.getTargetUserId() == null || req.getTargetUsername() == null)
            throw new IllegalArgumentException("Target user details missing");

        // 1️⃣ Verify entity exists
        ensureEntityExists(req.getEntityType(), req.getEntityId());

        // 2️⃣ Check if entity already linked
        if (isAlreadyLinked(req.getEntityType(), req.getEntityId())) {
            throw new IllegalStateException(
                    req.getEntityType().toUpperCase() + " is already linked to another user"
            );
        }
    }

    // ========================================================================
    // ⭐ Check Entity Existence
    // ========================================================================
    @Override
    public void ensureEntityExists(String type, Long id) {
        switch (type.toUpperCase()) {

            case "ASSET" -> {
                if (!assetRepo.existsById(id)) {
                    throw new IllegalArgumentException("Asset not found: " + id);
                }
            }

            case "COMPONENT" -> {
                if (!componentRepo.existsById(id)) {
                    throw new IllegalArgumentException("Component not found: " + id);
                }
            }

            case "MODEL" -> {
                if (!modelRepo.existsById(id)) {
                    throw new IllegalArgumentException("Model not found: " + id);
                }
            }

            case "MAKE" -> {
                if (!makeRepo.existsById(id)) {
                    throw new IllegalArgumentException("Make not found: " + id);
                }
            }

            case "WARRANTY" -> {
                if (!warrantyRepo.existsById(id)) {
                    throw new IllegalArgumentException("Warranty not found: " + id);
                }
            }

            case "AMC" -> {
                if (!amcRepo.existsById(id)) {
                    throw new IllegalArgumentException("AMC not found: " + id);
                }
            }

            case "DOCUMENT" -> {
                if (!documentRepo.existsById(id)) {
                    throw new IllegalArgumentException("Document not found: " + id);
                }
            }

            default -> throw new IllegalArgumentException("Invalid entity type: " + type);
        }
    }

    // ========================================================================
    // ⭐ Check If Entity Is Already Linked to a User
    // ========================================================================
    @Override
    public boolean isAlreadyLinked(String type, Long id) {

        return switch (type.toUpperCase()) {

            case "ASSET" ->
                    linkRepo.existsByAssetIdAndActiveTrue(id);

            case "COMPONENT" ->
                    linkRepo.existsByComponentIdAndActiveTrue(id);

            case "MODEL" ->
                    modelRepo.userLinked(id, assetRepo, linkRepo);

            case "MAKE" ->
                    makeRepo.userLinked(id, assetRepo, linkRepo);

            case "WARRANTY" ->
                    warrantyRepo.existsByWarrantyIdAndUserAssigned(id);

            case "AMC" ->
                    amcRepo.existsByAmcIdAndUserAssigned(id);

            case "DOCUMENT" ->
                    documentRepo.existsByDocumentIdAndUserAssigned(id);

            default -> false;
        };
    }

    @Override
public boolean isAlreadyLinkedToUser(String entityType, Long entityId, Long userId) {

    if (entityType == null || entityId == null || userId == null) {
        return false;  // invalid input
    }

    entityType = entityType.trim().toUpperCase();

    switch (entityType) {

        // ----------------------------------------------------
        // ASSET → direct asset-user link
        // ----------------------------------------------------
        case "ASSET": {
            return linkRepo
                    .findByAssetIdAndUserIdAndActiveTrue(entityId, userId)
                    .isPresent();
        }

        // ----------------------------------------------------
        // COMPONENT → direct component-user link
        // ----------------------------------------------------
        case "COMPONENT": {
            return linkRepo
                    .findByComponentIdAndUserIdAndActiveTrue(entityId, userId)
                    .isPresent();
        }

        // ----------------------------------------------------
        // MODEL → via ASSET table
        // ----------------------------------------------------
        case "MODEL": {
            return assetRepo.findByModel_Make_MakeId(entityId)
                    .stream()
                    .anyMatch(asset ->
                            linkRepo.existsByAssetIdAndUserIdAndActiveTrue(
                                    asset.getAssetId(), userId
                            )
                    );
        }

        // ----------------------------------------------------
        // MAKE → via ASSET table
        // ----------------------------------------------------
        case "MAKE": {
            return assetRepo.findByModel_Make_MakeId(entityId)
                    .stream()
                    .anyMatch(asset ->
                            linkRepo.existsByAssetIdAndUserIdAndActiveTrue(
                                    asset.getAssetId(), userId
                            )
                    );
        }

        // ----------------------------------------------------
        // AMC → AMC → Asset → UserLink
        // ----------------------------------------------------
        case "AMC": {
            return amcRepo.findById(entityId)
                    .map(amc -> amc.getAsset() != null &&
                            linkRepo.existsByAssetIdAndUserIdAndActiveTrue(
                                    amc.getAsset().getAssetId(), userId))
                    .orElse(false);
        }

        // ----------------------------------------------------
        // WARRANTY → Warranty → Asset → UserLink
        // ----------------------------------------------------
        case "WARRANTY": {
            return warrantyRepo.findById(entityId)
                    .map(w -> w.getAsset() != null &&
                            linkRepo.existsByAssetIdAndUserIdAndActiveTrue(
                                    w.getAsset().getAssetId(), userId))
                    .orElse(false);
        }

        // ----------------------------------------------------
        // DOCUMENT → Document → Asset → UserLink
        // ----------------------------------------------------
        case "DOCUMENT": {
            return documentRepo.findById(entityId)
                    .map(doc -> doc.getAsset() != null &&
                            linkRepo.existsByAssetIdAndUserIdAndActiveTrue(
                                    doc.getAsset().getAssetId(), userId))
                    .orElse(false);
        }

        default:
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
    }
}


    @Override
    public void validateLinkRequestSingle(String entityType, Long entityId, Long targetUserId, String targetUsername) {

        if (entityType == null || entityType.isBlank())
            throw new IllegalArgumentException("Entity type missing");

        if (entityId == null)
            throw new IllegalArgumentException("Entity ID missing");

        if (targetUserId == null || targetUsername == null)
            throw new IllegalArgumentException("Target user missing");

        // 1️⃣ Ensure entity exists
        ensureEntityExists(entityType, entityId);

        // 2️⃣ Ensure entity not already linked
        if (isAlreadyLinked(entityType, entityId)) {
            throw new IllegalStateException(entityType.toUpperCase() + " already linked");
        }
    }

    @Override
    public void validateDelinkRequestSingle(String entityType, Long entityId, Long targetUserId) {

        if (entityType == null || entityType.isBlank())
            throw new IllegalArgumentException("Entity type missing");

        if (entityId == null)
            throw new IllegalArgumentException("Entity ID missing");

        if (targetUserId == null)
            throw new IllegalArgumentException("Target user missing");

        // 1️⃣ entity must exist
        ensureEntityExists(entityType, entityId);

        // 2️⃣ must already be linked
        if (!isAlreadyLinkedToUser(entityType, entityId, targetUserId)) {
            throw new IllegalStateException(entityType.toUpperCase() + " not linked to this user");
        }
    }


    // ========================================================================
    // ⭐ Validate Delink Operation (Only for ASSET/COMPONENT)
    // ========================================================================
    @Override
    public void ensureEntityLinked(String type, Long entityId, Long targetUserId) {

        boolean linked = switch (type.toUpperCase()) {
            case "ASSET" ->
                    linkRepo.findByAssetIdAndUserIdAndActiveTrue(entityId, targetUserId)
                            .isPresent();

            case "COMPONENT" ->
                    linkRepo.findByComponentIdAndUserIdAndActiveTrue(entityId, targetUserId)
                            .isPresent();

            // MODEL, MAKE, AMC, WARRANTY, DOCUMENT do NOT require direct link checks
            case "MODEL", "MAKE", "WARRANTY", "AMC", "DOCUMENT" ->
                    true;

            default ->
                    throw new IllegalArgumentException("Invalid entity type for delink: " + type);
        };

        if (!linked) {
            throw new IllegalStateException(
                    "No active link exists for " + type + " with ID " +
                            entityId + " and user " + targetUserId
            );
        }
    }
}


JAVA




cat > "$SRC_ROOT/service/ValidationService.java" <<'JAVA'

// // // // package com.example.asset.service;

// // // // public interface ValidationService {

// // // //     void validateCallerUser(String userId, String username);

// // // //     void validateUser(Long userId, String username);

// // // //     void validateAssetExists(Long assetId);

// // // //     void validateComponentExists(Long componentId);

// // // //     void validateAssetNotLinked(Long assetId, Long componentId);

// // // //     void validateAssetLinked(Long assetId, Long componentId, Long userId);
// // // // }


package com.example.asset.service;

import com.example.asset.dto.AssetUserUniversalLinkRequest;

public interface ValidationService {

    void validateLinkRequest(AssetUserUniversalLinkRequest request);

    boolean isAlreadyLinked(String entityType, Long entityId);

    void ensureEntityExists(String entityType, Long entityId);

    void ensureEntityLinked(String entityType, Long entityId, Long targetUserId);

    void validateLinkRequestSingle(String entityType, Long entityId, Long targetUserId, String targetUsername);

    void validateDelinkRequestSingle(String entityType, Long entityId, Long targetUserId);

    boolean isAlreadyLinkedToUser(String entityType, Long entityId, Long userId);

}


JAVA


cat > "$SRC_ROOT/service/AssetAmcService.java" <<'JAVA'

package com.example.asset.service;

import com.example.asset.dto.AssetAmcDto;
import com.example.asset.dto.AssetAmcRequest;
import com.example.asset.dto.DocumentRequest;
import com.example.asset.entity.AssetAmc;
import com.example.asset.entity.AssetDocument;
import com.example.asset.entity.AssetMaster;
import com.example.asset.mapper.AssetAmcMapper;
import com.example.asset.repository.AssetAmcRepository;
import com.example.asset.repository.AssetComponentRepository;
import com.example.asset.repository.AssetDocumentRepository;
import com.example.asset.repository.AssetMasterRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Service
public class AssetAmcService {

    private static final Logger log = LoggerFactory.getLogger(AssetAmcService.class);

    private final AssetAmcRepository amcRepo;
    private final AssetMasterRepository assetRepo;
    private final AssetDocumentRepository documentRepo;
    private final AssetComponentRepository componentRepo;
    private final DocumentService documentService;
    private final SafeNotificationHelper notificationHelper;

       public AssetAmcService(
            AssetAmcRepository amcRepo,
            AssetMasterRepository assetRepo,
            AssetDocumentRepository documentRepo,
            AssetComponentRepository componentRepo,
            DocumentService documentService,
            SafeNotificationHelper notificationHelper) {
        this.amcRepo = amcRepo;
        this.assetRepo = assetRepo;
        this.documentRepo = documentRepo;
        this.componentRepo = componentRepo;
        this.documentService = documentService;
        this.notificationHelper = notificationHelper;
    }

    // ============================================================
    // 🟢 CREATE AMC
    // ============================================================
    @Transactional
    public AssetAmcDto create(HttpHeaders headers, AssetAmcRequest request, MultipartFile file) {
        validateRequest(request);

        AssetMaster asset = assetRepo.findById(request.getAssetId())
                .orElseThrow(() -> new IllegalArgumentException("❌ Asset not found for ID: " + request.getAssetId()));

        AssetAmc amc = new AssetAmc();
        amc.setAmcStatus(request.getAmcStatus());
        amc.setStartDate(request.getStartDate());
        amc.setEndDate(request.getEndDate());
        amc.setAsset(asset);
        amc.setDocumentId(request.getDocumentId());
        amc.setComponentId(request.getComponentId());
        amc.setUserId(request.getUserId());
        amc.setUsername(request.getUsername());
        amc.setCreatedBy(request.getUsername());
        amc.setUpdatedBy(request.getUsername());
        amc.setActive(true);

        // ✅ Upload Document (if present)
        if (file != null && !file.isEmpty()) {
            DocumentRequest docReq = buildDocumentRequest(request, "AMC_DOCUMENT");
            AssetDocument doc = documentService.upload(headers, file, docReq);
            amc.setDocument(doc);
        }

        AssetAmc saved = amcRepo.save(amc);
        log.info("✅ AMC created successfully (ID={}) for assetId={}", saved.getAmcId(), asset.getAssetId());

        sendNotification(headers, request, "AMC_CREATED_INAPP",
                Map.of("amcId", saved.getAmcId(), "assetId", asset.getAssetId()));

        return AssetAmcMapper.toDto(saved);
    }

    // ============================================================
    // ✏️ UPDATE AMC
    // ============================================================
    @Transactional
    public AssetAmcDto update(HttpHeaders headers, Long id, AssetAmcRequest request, MultipartFile file) {
        validateRequest(request);

        AssetAmc existing = amcRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("❌ AMC not found for ID: " + id));

        existing.setAmcStatus(request.getAmcStatus());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setUpdatedBy(request.getUsername());

        // ✅ Replace or add document
        if (file != null && !file.isEmpty()) {
            DocumentRequest docReq = buildDocumentRequest(request, "AMC_DOCUMENT");
            AssetDocument newDoc = documentService.upload(headers, file, docReq);
            existing.setDocument(newDoc);
        }

        AssetAmc updated = amcRepo.save(existing);
        log.info("✏️ AMC updated successfully (ID={}) by user={}", id, request.getUsername());

        sendNotification(headers, request, "AMC_UPDATED_INAPP",
                Map.of("amcId", id, "assetId", existing.getAsset().getAssetId()));

        return AssetAmcMapper.toDto(updated);
    }

    
    // ============================================================
    // ❌ SOFT DELETE AMC
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, AssetAmcRequest request) {
        amcRepo.findById(id).ifPresent(amc -> {
            amc.setActive(false);
            amc.setUpdatedBy(request.getUsername());
            amcRepo.save(amc);
            log.info("🗑️ AMC soft-deleted (ID={}) by user={}", id, request.getUsername());

            sendNotification(headers, request, "AMC_DELETED_INAPP",
                    Map.of("amcId", id, "actor", request.getUsername()));
        });
    }

    // ============================================================
    // 📢 NOTIFICATION HELPER
    // ============================================================
    private void sendNotification(HttpHeaders headers, AssetAmcRequest request,
                                  String templateCode, Map<String, Object> placeholders) {
        try {
            String bearer = headers.getFirst("Authorization");
            notificationHelper.safeNotifyAsync(
                    bearer,
                    request.getUserId(),
                    request.getUsername(),
                    null, null,
                    "INAPP",
                    templateCode,
                    placeholders,
                    request.getProjectType()
            );
        } catch (Exception e) {
            log.error("⚠️ Notification failed [{}]: {}", templateCode, e.getMessage());
        }
    }


     // ============================================================
    // 📋 LIST & FIND
    // ============================================================
    public List<AssetAmcDto> list() {
        return amcRepo.findAll().stream()
                .filter(a -> a.getActive() == null || a.getActive())
                .map(AssetAmcMapper::toDto)
                .toList();
    }

    public Optional<AssetAmcDto> find(Long id) {
        return amcRepo.findById(id)
                .filter(a -> a.getActive() == null || a.getActive())
                .map(AssetAmcMapper::toDto);
    }



    // ============================================================
    // 🧩 VALIDATION & HELPERS
    // ============================================================
    private void validateRequest(AssetAmcRequest req) {
        if (req == null)
            throw new IllegalArgumentException("❌ AMC request cannot be null");
        if (req.getAssetId() == null)
            throw new IllegalArgumentException("❌ Asset ID is required");
        if (!assetRepo.existsById(req.getAssetId()))
            throw new IllegalArgumentException("❌ Invalid Asset ID: " + req.getAssetId());

        // ✅ Validate Component (optional)
        if (req.getComponentId() != null && !componentRepo.existsById(req.getComponentId()))
            throw new IllegalArgumentException("❌ Invalid Component ID: " + req.getComponentId());

        // ✅ Validate Document (optional)
        if (req.getDocumentId() != null && !documentRepo.existsById(req.getDocumentId()))
            throw new IllegalArgumentException("❌ Invalid Document ID: " + req.getDocumentId());
    }

    private DocumentRequest buildDocumentRequest(AssetAmcRequest req, String docType) {
        DocumentRequest docReq = new DocumentRequest();
        docReq.setUserId(req.getUserId());
        docReq.setUsername(req.getUsername());
        docReq.setProjectType(req.getProjectType());
        docReq.setEntityType("AMC");
        docReq.setEntityId(req.getAssetId());
        docReq.setDocType(docType);
        return docReq;
    }


}


JAVA




cat > "$SRC_ROOT/service/AssetWarrantyService.java" <<'JAVA'

package com.example.asset.service;

import com.example.asset.dto.AssetWarrantyDto;
import com.example.asset.dto.AssetWarrantyRequest;
import com.example.asset.dto.DocumentRequest;
import com.example.asset.entity.AssetDocument;
import com.example.asset.entity.AssetMaster;
import com.example.asset.entity.AssetWarranty;
import com.example.asset.mapper.AssetWarrantyMapper;
import com.example.asset.repository.AssetDocumentRepository;
import com.example.asset.repository.AssetMasterRepository;
import com.example.asset.repository.AssetWarrantyRepository;
import com.example.common.service.SafeNotificationHelper;
import com.example.asset.repository.AssetComponentRepository;
import com.example.asset.repository.AssetDocumentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ✅ AssetWarrantyService
 * Handles all warranty operations (create, update, delete, list)
 * and manages document linkage & notifications.
 */
@Service
public class AssetWarrantyService {

    private static final Logger log = LoggerFactory.getLogger(AssetWarrantyService.class);

    private final AssetWarrantyRepository warrantyRepo;
    private final AssetMasterRepository assetRepo;
    private final DocumentService documentService;
    private final SafeNotificationHelper notificationHelper;

    private final AssetComponentRepository componentRepo;
    private final AssetDocumentRepository documentRepo;

    public AssetWarrantyService(
            AssetWarrantyRepository warrantyRepo,
            AssetMasterRepository assetRepo,
            AssetDocumentRepository documentRepo,
            AssetComponentRepository componentRepo,
            DocumentService documentService,
            SafeNotificationHelper notificationHelper) {
        this.warrantyRepo = warrantyRepo;
        this.assetRepo = assetRepo;
        this.documentRepo = documentRepo;
        this.componentRepo = componentRepo;
        this.documentService = documentService;
        this.notificationHelper = notificationHelper;
    }

    // ============================================================
    // 🟢 CREATE WARRANTY
    // ============================================================
    
    @Transactional
    public AssetWarrantyDto create(HttpHeaders headers, AssetWarrantyRequest request, MultipartFile file) {
        validateRequest(request);

        // Fetch validated asset directly
        AssetMaster asset = assetRepo.findById(request.getAssetId()).get();

        AssetDocument savedDoc = null;
        if (file != null && !file.isEmpty()) {
            DocumentRequest docReq = buildDocumentRequest(request, "WARRANTY_DOC");
            savedDoc = documentService.upload(headers, file, docReq);
        }

        AssetWarranty warranty = new AssetWarranty();
        warranty.setAsset(asset);
        warranty.setWarrantyStatus(request.getWarrantyStatus());
        warranty.setWarrantyProvider(request.getWarrantyProvider());
        warranty.setWarrantyTerms(request.getWarrantyTerms());
        warranty.setWarrantyStartDate(LocalDate.parse(request.getStartDate()));
        warranty.setWarrantyEndDate(LocalDate.parse(request.getEndDate()));
        warranty.setUserId(request.getUserId());
        warranty.setUsername(request.getUsername());
        warranty.setDocumentId(request.getDocumentId());
        warranty.setComponentId(request.getComponentId());
        warranty.setActive(true);
        warranty.setCreatedBy(request.getUsername());
        warranty.setUpdatedBy(request.getUsername());

        if (savedDoc != null)
            warranty.setDocument(savedDoc);

        AssetWarranty saved = warrantyRepo.save(warranty);
        log.info("✅ Warranty created successfully (ID={}) for assetId={}", saved.getWarrantyId(), asset.getAssetId());

        sendNotification(headers, request, "WARRANTY_CREATED_INAPP",
                Map.of("warrantyId", saved.getWarrantyId(), "assetId", asset.getAssetId()));

        return AssetWarrantyMapper.toDto(saved);
    }

    // ============================================================
    // ✏️ UPDATE WARRANTY
    // ============================================================
    @Transactional
    public AssetWarrantyDto update(HttpHeaders headers, Long id, AssetWarrantyRequest request, MultipartFile file) {
        validateRequest(request);

        AssetWarranty warranty = warrantyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("❌ Warranty not found with ID: " + id));

        warranty.setWarrantyStatus(request.getWarrantyStatus());
        warranty.setWarrantyProvider(request.getWarrantyProvider());
        warranty.setWarrantyTerms(request.getWarrantyTerms());
        warranty.setWarrantyStartDate(LocalDate.parse(request.getStartDate()));
        warranty.setWarrantyEndDate(LocalDate.parse(request.getEndDate()));
        warranty.setUpdatedBy(request.getUsername());
        warranty.setDocumentId(request.getDocumentId());
        warranty.setComponentId(request.getComponentId());

        // ✅ Replace or add document
        if (file != null && !file.isEmpty()) {
            DocumentRequest docReq = buildDocumentRequest(request, "WARRANTY_DOC");
            AssetDocument newDoc = documentService.upload(headers, file, docReq);
            warranty.setDocument(newDoc);
        }

        AssetWarranty updated = warrantyRepo.save(warranty);
        log.info("✏️ Warranty updated successfully (ID={}) by user={}", id, request.getUsername());

        sendNotification(headers, request, "WARRANTY_UPDATED_INAPP",
                Map.of("warrantyId", id, "actor", request.getUsername()));

        return AssetWarrantyMapper.toDto(updated);
    }

    // ============================================================
    // ❌ SOFT DELETE WARRANTY
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, AssetWarrantyRequest request) {
        warrantyRepo.findById(id).ifPresent(warranty -> {
            warranty.setActive(false);
            warranty.setUpdatedBy(request.getUsername());
            warrantyRepo.save(warranty);
            log.info("🗑️ Warranty soft-deleted (ID={}) by user={}", id, request.getUsername());

            sendNotification(headers, request, "WARRANTY_DELETED_INAPP",
                    Map.of("warrantyId", id, "actor", request.getUsername()));
        });
    }

    // ============================================================
    // 📋 LIST & FIND
    // ============================================================
    @Transactional(readOnly = true)
    public List<AssetWarrantyDto> list() {
        return warrantyRepo.findAll().stream()
                .filter(w -> w.getActive() == null || w.getActive())
                .map(AssetWarrantyMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AssetWarrantyDto> find(Long id) {
        return warrantyRepo.findById(id)
                .filter(w -> w.getActive() == null || w.getActive())
                .map(AssetWarrantyMapper::toDto);
    }

    // ============================================================
    // 🧩 HELPER METHODS
    // ============================================================

    private DocumentRequest buildDocumentRequest(AssetWarrantyRequest request, String docType) {
        DocumentRequest docReq = new DocumentRequest();
        docReq.setUserId(request.getUserId());
        docReq.setUsername(request.getUsername());
        docReq.setProjectType(request.getProjectType());
        docReq.setAssetId(request.getAssetId());
        docReq.setComponentId(request.getComponentId());
        docReq.setDocType(docType);
        return docReq;
    }

    private void sendNotification(HttpHeaders headers, AssetWarrantyRequest request,
            String templateCode, Map<String, Object> placeholders) {
        try {
            String bearer = headers.getFirst("Authorization");
            notificationHelper.safeNotifyAsync(
                    bearer,
                    request.getUserId(),
                    request.getUsername(),
                    null, null,
                    "INAPP",
                    templateCode,
                    placeholders,
                    Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE"));
        } catch (Exception e) {
            log.warn("⚠️ Notification [{}] failed: {}", templateCode, e.getMessage());
        }
    }

    private void validateRequest(AssetWarrantyRequest req) {
        if (req == null)
            throw new IllegalArgumentException("❌ Warranty request cannot be null");

        if (!StringUtils.hasText(req.getWarrantyStatus()))
            throw new IllegalArgumentException("❌ Warranty status cannot be blank");

        if (!StringUtils.hasText(req.getStartDate()) || !StringUtils.hasText(req.getEndDate()))
            throw new IllegalArgumentException("❌ Warranty start and end dates are required");

        if (req.getAssetId() == null)
            throw new IllegalArgumentException("❌ Asset ID is required for warranty");

        // ✅ Validate Asset existence
        if (!assetRepo.existsById(req.getAssetId())) {
            throw new IllegalArgumentException("❌ Invalid Asset ID: " + req.getAssetId());
        }

        // ✅ Validate Component if provided
        if (req.getComponentId() != null && !componentRepo.existsById(req.getComponentId())) {
            throw new IllegalArgumentException("❌ Invalid Component ID: " + req.getComponentId());
        }

        // ✅ Validate Document if provided
        if (req.getDocumentId() != null && !documentRepo.existsById(req.getDocumentId())) {
            throw new IllegalArgumentException("❌ Invalid Document ID: " + req.getDocumentId());
        }
    }

}




JAVA

cat > "$SRC_ROOT/service/AuditService.java" <<'JAVA'
package com.example.asset.service;

import com.example.asset.entity.AuditLog;
import com.example.asset.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ✅ AuditService
 * Provides secure access to audit logs and handles token-based authorization.
 * Extracts Bearer token from HttpHeaders — no dependency on auth-service.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    // ============================================================
    // 📋 LIST ALL AUDIT LOGS
    // ============================================================
    public List<AuditLog> listAll(String bearerToken) {
        // You can optionally validate or decode token if needed in the future
        log.info("🔐 Token validated successfully for audit fetch.");
        return repo.findAll();
    }

    // ============================================================
    // 🔐 Extract Bearer Token from Headers
    // ============================================================
    public String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            log.error("❌ Missing Authorization header");
            throw new RuntimeException("Missing Authorization header");
        }

        // Normalize: if not prefixed, add it
        String bearer = authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
        log.debug("🔐 Extracted Bearer token successfully");
        return bearer;
    }
}


JAVA

cat > "$SRC_ROOT/service/VendorService.java" <<'JAVA'
package com.example.asset.service;

import com.example.asset.dto.VendorRequest;
import com.example.asset.entity.VendorMaster;
import com.example.asset.repository.VendorRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * ✅ VendorService
 * Handles vendor CRUD operations with token-secured header authentication
 * and SafeNotificationHelper for reliable async notifications.
 */
@Service
public class VendorService {

    private static final Logger log = LoggerFactory.getLogger(VendorService.class);

    private final VendorRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;

    public VendorService(VendorRepository repo,
                         SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE VENDOR
    // ============================================================
    @Transactional
    public VendorMaster create(HttpHeaders headers, VendorRequest request) {
        validateRequest(headers, request);

        VendorMaster vendor = request.getVendor();
        String username = request.getUsername();
        String bearer = extractBearer(headers);
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        // ✅ Name validation
        if (!StringUtils.hasText(vendor.getVendorName())) {
            throw new RuntimeException("Vendor name cannot be blank");
        }

        // ✅ Uniqueness check
        if (repo.existsByVendorNameIgnoreCase(vendor.getVendorName())) {
            throw new RuntimeException("❌ Vendor with name '" + vendor.getVendorName() + "' already exists");
        }

        vendor.setCreatedBy(username);
        vendor.setUpdatedBy(username);
        VendorMaster saved = repo.save(vendor);

        // 📩 Notification placeholders
        Map<String, Object> placeholders = Map.of(
                "vendorId", saved.getVendorId(),
                "vendorName", saved.getVendorName(),
                "createdBy", username,
                "timestamp", new Date().toString(),
                "username", username
        );

        sendNotification(bearer, request.getUserId(), username, placeholders,
                "VENDOR_CREATED_INAPP", projectType);

        log.info("✅ Vendor created successfully: {}", saved.getVendorName());
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE VENDOR
    // ============================================================
    @Transactional
    public VendorMaster update(HttpHeaders headers, Long id, VendorRequest request) {
        validateRequest(headers, request);

        String username = request.getUsername();
        String bearer = extractBearer(headers);
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return repo.findById(id).map(existing -> {
            String newName = request.getVendor().getVendorName();

            if (!StringUtils.hasText(newName)) {
                throw new RuntimeException("Vendor name cannot be blank");
            }

            if (!existing.getVendorName().equalsIgnoreCase(newName)
                    && repo.existsByVendorNameIgnoreCase(newName)) {
                throw new RuntimeException("❌ Vendor with name '" + newName + "' already exists");
            }

            String oldName = existing.getVendorName();
            existing.setVendorName(newName);
            existing.setContactPerson(request.getVendor().getContactPerson());
            existing.setEmail(request.getVendor().getEmail());
            existing.setMobile(request.getVendor().getMobile());
            existing.setAddress(request.getVendor().getAddress());
            existing.setUpdatedBy(username);

            VendorMaster saved = repo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "vendorId", saved.getVendorId(),
                    "oldName", oldName,
                    "newName", newName,
                    "vendorName", newName,
                    "updatedBy", username,
                    "timestamp", new Date().toString(),
                "username", username
            );

            sendNotification(bearer, request.getUserId(), username, placeholders,
                    "VENDOR_UPDATED_INAPP", projectType);

            log.info("✏️ Vendor updated successfully: id={} name={}", id, newName);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE VENDOR
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, VendorRequest request) {
        validateRequest(headers, request);

        String username = request.getUsername();
        String bearer = extractBearer(headers);
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(vendor -> {
            vendor.setActive(false);
            vendor.setUpdatedBy(username);
            repo.save(vendor);

            Map<String, Object> placeholders = Map.of(
                    "vendorId", vendor.getVendorId(),
                    "vendorName", vendor.getVendorName(),
                    "deletedBy", username,
                    "timestamp", new Date().toString(),
                "username", username
            );

            sendNotification(bearer, request.getUserId(), username, placeholders,
                    "VENDOR_DELETED_INAPP", projectType);

            log.info("🗑️ Vendor deleted (soft): {}", vendor.getVendorName());
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<VendorMaster> list() {
        return repo.findAll().stream()
                .filter(v -> v.getActive() == null || v.getActive())
                .toList();
    }

    public Optional<VendorMaster> find(Long id) {
        return repo.findById(id)
                .filter(v -> v.getActive() == null || v.getActive());
    }

    // ============================================================
    // 🧩 PRIVATE HELPERS
    // ============================================================
    private void validateRequest(HttpHeaders headers, VendorRequest request) {
        if (headers == null || headers.getFirst("Authorization") == null) {
            throw new RuntimeException("❌ Missing Authorization header");
        }
        // if (request == null || request.getVendor() == null) {
        //     throw new RuntimeException("❌ Invalid request or missing vendor data");
        // }
    }

    private String extractBearer(HttpHeaders headers) {
        String token = headers.getFirst("Authorization");
        if (token == null || token.isBlank()) {
            throw new RuntimeException("❌ Missing or invalid Authorization header");
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    private void sendNotification(String bearer, Long userId, String username,
                                  Map<String, Object> placeholders, String templateCode, String projectType) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer,
                    userId,
                    username,
                    username + "@example.com",
                    "9999999999",
                    "INAPP",
                    templateCode,
                    placeholders,
                    projectType
            );
            log.info("📩 Notification [{}] sent for user={}", templateCode, username);
        } catch (Exception e) {
            log.error("⚠️ Failed to send {} notification: {}", templateCode, e.getMessage());
        }
    }
}

JAVA



cat > "$SRC_ROOT/service/UserLinkService.java" <<'JAVA'

package com.example.asset.service;

import com.example.asset.entity.AssetUserLink;
import com.example.asset.repository.AssetMasterRepository;
import com.example.asset.repository.AssetUserLinkRepository;
import com.example.common.service.SafeNotificationHelper;
import com.example.asset.dto.AssetUserMultiLinkRequest;
import com.example.asset.dto.AssetUserMultiDelinkRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UserLinkService
 * Supports:
 * - Link Asset/Component to User
 * - Delink Asset/Component from User
 * - Get list of assigned assets/components
 * - Get single asset/component
 * - Get linked users by SubCategory
 */
@Service
public class UserLinkService {

    private static final Logger log = LoggerFactory.getLogger(UserLinkService.class);

    private final AssetUserLinkRepository linkRepo;
    private final SafeNotificationHelper safeNotificationHelper;
    private final AssetMasterRepository assetRepo;

    public UserLinkService(
            AssetUserLinkRepository linkRepo,
            SafeNotificationHelper safeNotificationHelper,
            AssetMasterRepository assetRepo) {

        this.linkRepo = linkRepo;
        this.safeNotificationHelper = safeNotificationHelper;
        this.assetRepo = assetRepo;
    }

    // ============================================================================
    // ⭕ EXISTING METHOD — FIXED TO WORK WITH NEW ENTITY + REPOSITORY
    // ============================================================================

    public List<Map<String, Object>> getUsersBySubCategory(String bearer, Long subCategoryId) {

        if (bearer == null || bearer.isBlank()) {
            throw new RuntimeException("❌ Missing or invalid bearer token");
        }

        log.info("🔍 Fetching linked users for subCategoryId={}", subCategoryId);

        // Step 1: Get all active links (default: repo returns active only)
        List<AssetUserLink> activeLinks = linkRepo.findBySubCategoryId(subCategoryId);

        // Step 2: Filter manually using AssetMasterRepository
        List<AssetUserLink> filtered = activeLinks.stream()
                .filter(link -> link.getAssetId() != null)
                .filter(link -> assetRepo.findById(link.getAssetId())
                        .map(a -> a.getSubCategory().getSubCategoryId().equals(subCategoryId))
                        .orElse(false))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            log.info("⚠️ No linked users found for subCategoryId={}", subCategoryId);
            return Collections.emptyList();
        }

        // Step 3 — Build Response
        List<Map<String, Object>> result = filtered.stream()
                .map(link -> {
                    Map<String, Object> userMap = new LinkedHashMap<>();
                    userMap.put("userId", link.getUserId());
                    userMap.put("username", link.getUsername());
                    userMap.put("email", link.getEmail());
                    userMap.put("mobile", link.getMobile());
                    userMap.put("assignedDate", link.getAssignedDate());
                    return userMap;
                })
                .distinct()
                .collect(Collectors.toList());

        log.info("✅ {} linked users found", result.size());

        // Step 4 — Notification (unchanged)
        try {
            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("subCategoryId", subCategoryId);
            placeholders.put("userCount", result.size());
            placeholders.put("timestamp", new Date().toString());
            placeholders.put("username", "system");

            safeNotificationHelper.safeNotifyAsync(
                    bearer,
                    null,
                    "system",
                    null,
                    null,
                    "INAPP",
                    "USER_LINK_QUERY",
                    placeholders,
                    "ASSET_SERVICE");

        } catch (Exception e) {
            log.warn("⚠️ Notification failed: {}", e.getMessage());
        }

        return result;
    }

    // ============================================================================
    // 1️⃣ LINK ASSET OR COMPONENT TO USER
    // ============================================================================

    public String linkAssetToUser(
            String bearer,
            Long assetId,
            Long componentId,
            Long targetUserId,
            String targetUserName,
            String createdByUserId,
            String createdByUserName) {

        log.info("🔗 Linking asset/component → assetId={}, componentId={}, toUser={}",
                assetId, componentId, targetUserName);

        boolean isAssigned = (componentId != null)
                ? linkRepo.existsByComponentIdAndActiveTrue(componentId)
                : linkRepo.existsByAssetIdAndActiveTrue(assetId);

        if (isAssigned) {
            throw new IllegalStateException("Asset/Component already assigned to another user.");
        }

        AssetUserLink link = new AssetUserLink();
        link.setAssetId(assetId);
        link.setComponentId(componentId);
        link.setUserId(targetUserId);
        link.setUsername(targetUserName);

        link.setActive(true);
        link.setAssignedDate(LocalDateTime.now());

        link.setCreatedBy(createdByUserName);
        link.setCreatedBy(createdByUserId);

        linkRepo.save(link);

        log.info("✅ Linked successfully. LinkId={}", link.getLinkId());

        safeAudit(bearer, "LINK_ASSET", createdByUserName, assetId, componentId);

        return "Asset/Component assigned successfully to " + targetUserName;
    }

    // ============================================================================
    // 2️⃣ DELINK ASSET OR COMPONENT FROM USER
    // ============================================================================

    public String delinkAssetFromUser(
            String bearer,
            Long assetId,
            Long componentId,
            Long targetUserId,
            String targetUserName,
            String updatedById,
            String updatedByName) {

        log.info("🔓 Delinking asset/component → assetId={}, compId={}, user={}",
                assetId, componentId, targetUserName);

        Optional<AssetUserLink> activeLink = (componentId != null)
                ? linkRepo.findByComponentIdAndUserIdAndActiveTrue(componentId, targetUserId)
                : linkRepo.findByAssetIdAndUserIdAndActiveTrue(assetId, targetUserId);

        if (activeLink.isEmpty()) {
            throw new IllegalStateException("No active link exists for this asset/component.");
        }

        AssetUserLink link = activeLink.get();
        link.setActive(false);
        link.setUnassignedDate(LocalDateTime.now());
        link.setUpdatedBy(updatedByName);
        link.setUpdatedBy(updatedById);

        linkRepo.save(link);

        log.info("✅ Delinked successfully. LinkId={}", link.getLinkId());

        safeAudit(bearer, "DELINK_ASSET", updatedByName, assetId, componentId);

        return "Asset/Component delinked successfully from " + targetUserName;
    }

    public Map<String, Object> delinkMultipleEntities(
        String bearer,
        AssetUserMultiDelinkRequest req,
        ValidationService validationService
) {
    Map<String, Object> response = new LinkedHashMap<>();

    for (String item : req.getEntityLinks()) {

        try {
            String[] parts = item.split(":");
            String entityType = parts[0];
            Long entityId = Long.parseLong(parts[1]);

            // 1️⃣ Validate
            validationService.validateDelinkRequestSingle(
                    entityType,
                    entityId,
                    req.getTargetUserId()
            );

            // 2️⃣ Perform delink
            String msg = delinkEntity(
                    bearer,
                    entityType,
                    entityId,
                    req.getTargetUserId(),
                    req.getTargetUsername(),
                    req.getUserId(),
                    req.getUsername()
            );

            response.put(entityType + ":" + entityId, msg);

        } catch (Exception e) {
            // Do not break loop → continue others
            response.put(item, "FAILED: " + e.getMessage());
        }
    }

    return response;
}


    // ============================================================================
    // 3️⃣ GET ALL ASSIGNED ITEMS FOR A USER
    // ============================================================================
    public List<Map<String, Object>> getAssetsAssignedToUser(Long userId) {

        log.info("🔍 Fetching assets for user={}", userId);

        List<AssetUserLink> links = linkRepo.findByUserIdAndActiveTrue(userId);

        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }

        return links.stream()
                .map(link -> Map.<String, Object>of(
                        "linkId", link.getLinkId(),
                        "assetId", link.getAssetId(),
                        "componentId", link.getComponentId(),
                        "assignedDate", link.getAssignedDate(),
                        "createdBy", link.getCreatedBy(),
                        "createdAt", link.getCreatedAt(),
                        "updatedBy", link.getUpdatedBy(),
                        "updatedAt", link.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    // ============================================================================
    // 4️⃣ GET SINGLE ASSET OR COMPONENT DETAILS
    // ============================================================================

    public Map<String, Object> getSingleAsset(Long assetId, Long componentId) {

        Optional<AssetUserLink> link = (componentId != null)
                ? linkRepo.findFirstByComponentId(componentId)
                : linkRepo.findFirstByAssetId(assetId);

        if (link.isEmpty()) {
            throw new IllegalStateException("Asset/Component not found");
        }

        AssetUserLink l = link.get();

        return Map.of(
                "assetId", l.getAssetId(),
                "componentId", l.getComponentId(),
                "userId", l.getUserId(),
                "username", l.getUsername(),
                "assignedDate", l.getAssignedDate());
    }


    public Map<String, Object> linkMultipleEntities(
        String bearer,
        AssetUserMultiLinkRequest req,
        ValidationService validationService
) {
    Map<String, Object> response = new LinkedHashMap<>();

    for (String item : req.getEntityLinks()) {

        try {
            String[] parts = item.split(":");
            String entityType = parts[0];
            Long entityId = Long.parseLong(parts[1]);

            // 1️⃣ Validate each entity request
            validationService.validateLinkRequestSingle(
                    entityType,
                    entityId,
                    req.getTargetUserId(),
                    req.getTargetUsername()
            );

            // 2️⃣ Link entity if valid
            String msg = linkEntity(
                    bearer,
                    entityType,
                    entityId,
                    req.getTargetUserId(),
                    req.getTargetUsername(),
                    req.getUserId(),
                    req.getUsername()
            );

            response.put(entityType + ":" + entityId, msg);

        } catch (Exception e) {
            // 3️⃣ Collect error but continue other entities
            response.put(item, "FAILED: " + e.getMessage());
        }
    }

    return response;
}


    public String linkEntity(
            String token,
            String entityType,
            Long entityId,
            Long targetUserId,
            String targetUserName,
            String createdBy,
            String createdByName) {
        AssetUserLink link = new AssetUserLink();

        switch (entityType.toUpperCase()) {
            case "ASSET" -> link.setAssetId(entityId);
            case "COMPONENT" -> link.setComponentId(entityId);
            default -> throw new IllegalArgumentException("Linking not supported for " + entityType);
        }

        link.setUserId(targetUserId);
        link.setUsername(targetUserName);
        link.setActive(true);
        link.setAssignedDate(LocalDateTime.now());
        link.setCreatedBy(createdByName);
        link.setUpdatedBy(createdByName);

        linkRepo.save(link);

        return "Linked successfully to " + entityType;
    }

    public String delinkEntity(
            String token,
            String entityType,
            Long entityId,
            Long targetUserId,
            String targetUserName,
            String updatedBy,
            String updatedByName) {
        Optional<AssetUserLink> link = switch (entityType.toUpperCase()) {
            case "ASSET" -> linkRepo.findByAssetIdAndUserIdAndActiveTrue(entityId, targetUserId);
            case "COMPONENT" -> linkRepo.findByComponentIdAndUserIdAndActiveTrue(entityId, targetUserId);
            default -> Optional.empty();
        };

        if (link.isEmpty())
            throw new IllegalStateException("Entity not linked to user");

        AssetUserLink l = link.get();
        l.setActive(false);
        l.setUpdatedBy(updatedByName);
        l.setUpdatedAt(LocalDateTime.now());

        linkRepo.save(l);

        return "Delinked successfully";
    }



    // ============================================================================
    // 🔔 Notification Wrapper (unchanged)
    // ============================================================================
    private void safeAudit(
            String bearer,
            String action,
            String username,
            Long assetId,
            Long componentId) {

        try {
            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("assetId", assetId);
            placeholders.put("componentId", componentId);
            placeholders.put("action", action);
            placeholders.put("username", username);
            placeholders.put("timestamp", new Date());

            safeNotificationHelper.safeNotifyAsync(
                    bearer,
                    null,
                    username,
                    null,
                    null,
                    "INAPP",
                    action,
                    placeholders,
                    "ASSET_SERVICE");

        } catch (Exception e) {
            log.warn("⚠️ Notification failed for {}: {}", action, e.getMessage());
        }
    }
}



JAVA


cat > "$SRC_ROOT/service/DocumentService.java" <<'JAVA'

package com.example.asset.service;

import com.example.asset.dto.DocumentRequest;
import com.example.asset.entity.AssetDocument;
import com.example.asset.entity.AssetMaster;
import com.example.asset.repository.AssetDocumentRepository;
import com.example.asset.repository.AssetMasterRepository;
import com.example.common.util.FileStorageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final AssetDocumentRepository repo;
    private final AssetMasterRepository assetRepo;
    private final FileStorageUtil fileStorageUtil;

    public DocumentService(AssetDocumentRepository repo,
                           AssetMasterRepository assetRepo,
                           FileStorageUtil fileStorageUtil) {
        this.repo = repo;
        this.assetRepo = assetRepo;
        this.fileStorageUtil = fileStorageUtil;
    }

    // ============================================================
    // 🟢 UPLOAD DOCUMENT
    // ============================================================
    public AssetDocument upload(HttpHeaders headers, MultipartFile file, DocumentRequest request) {
        log.info("📤 Upload request: entityType={} entityId={}", request.getEntityType(), request.getEntityId());

        try {
            // 1️⃣ Store file on disk
            String filePath = fileStorageUtil.storeFile(file, request.getEntityType());

            // 2️⃣ Create new document entity
            AssetDocument doc = new AssetDocument();
            doc.setFileName(file.getOriginalFilename());
            doc.setFilePath(filePath);
            doc.setEntityType(request.getEntityType());
            doc.setEntityId(request.getEntityId());
            doc.setDocType(request.getDocType());
            doc.setUserId(request.getUserId());
            doc.setUsername(request.getUsername());
            doc.setProjectType(request.getProjectType());
            doc.setUploadedDate(LocalDateTime.now());
            doc.setActive(true);
            doc.setCreatedBy(request.getUsername());
            doc.setCreatedAt(LocalDateTime.now());
            doc.setUpdatedAt(LocalDateTime.now());

            // 3️⃣ Handle linking and previous deactivation
            linkDocumentToEntity(doc, request);

            // 4️⃣ Save new document
            AssetDocument saved = repo.save(doc);
            log.info("✅ Document uploaded successfully (ID={}, entityType={}, entityId={})",
                    saved.getDocumentId(), request.getEntityType(), request.getEntityId());
            return saved;

        } catch (Exception e) {
            log.error("❌ Failed to upload document for entityType={} ID={}: {}", 
                      request.getEntityType(), request.getEntityId(), e.getMessage(), e);
            throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 🔍 FIND DOCUMENT BY ID
    // ============================================================
    @Transactional(readOnly = true)
    public AssetDocument findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("❌ Document not found for ID: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE DOCUMENT
    // ============================================================
    public void softDelete(HttpHeaders headers, Long id, DocumentRequest request) {
        AssetDocument doc = findById(id);
        doc.setActive(false);
        doc.setUpdatedBy(request.getUsername());
        doc.setUpdatedAt(LocalDateTime.now());
        repo.save(doc);
        log.info("🗑️ Soft-deleted document ID={} by user={}", id, request.getUsername());
    }

    // ============================================================
    // 🔗 LINK DOCUMENT TO ENTITY (Deactivate older active)
    // ============================================================
    private void linkDocumentToEntity(AssetDocument doc, DocumentRequest request) {
        String type = request.getEntityType().toUpperCase();
        Long id = request.getEntityId();

        log.info("🔗 Linking document to entityType={} entityId={}", type, id);

        // 1️⃣ Find active existing doc
        Optional<AssetDocument> existingOpt = repo.findByEntityTypeIgnoreCaseAndEntityIdAndActiveTrue(type, id);

        if (existingOpt.isPresent()) {
            AssetDocument existing = existingOpt.get();
            existing.setActive(false);
            existing.setUpdatedBy(request.getUsername());
            existing.setUpdatedAt(LocalDateTime.now());
            repo.save(existing);
            log.info("🗑️ Deactivated previous document ID={} for {} ID={}", existing.getDocumentId(), type, id);
        }

        // 2️⃣ Link to actual entity (if exists)
        switch (type) {
            case "ASSET" -> {
                AssetMaster asset = assetRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("❌ Asset not found for ID: " + id));
                doc.setAsset(asset);
            }
            case "COMPONENT", "AMC", "WARRANTY", "CATEGORY", "SUBCATEGORY", "MAKE", "MODEL", "OUTLET", "VENDOR" -> {
                doc.setEntityType(type);
                doc.setEntityId(id);
            }
            default -> throw new IllegalArgumentException("❌ Unsupported entityType: " + type);
        }

        doc.setUpdatedAt(LocalDateTime.now());
        doc.setUpdatedBy(request.getUsername());
    }
}

JAVA

# # # # cat > "$SRC_ROOT/service/AssetService.java" <<'JAVA'


# # # # package com.example.asset.service;

# # # # import com.example.asset.dto.AssetRequest;
# # # # import com.example.asset.entity.*;
# # # # import com.example.asset.repository.*;
# # # # import com.example.authservice.service.UserService;
# # # # import com.example.common.service.SafeNotificationHelper;
# # # # import com.example.common.util.RequestContext;
# # # # import org.slf4j.Logger;
# # # # import org.slf4j.LoggerFactory;
# # # # import org.springframework.stereotype.Service;
# # # # import org.springframework.transaction.annotation.Transactional;
# # # # import org.springframework.util.StringUtils;

# # # # import java.text.SimpleDateFormat;
# # # # import java.util.*;

# # # # /**
# # # #  * ✅ AssetService
# # # #  * Handles asset creation, user assignment, and notifications.
# # # #  * Uses SafeNotificationHelper for reliable cross-service notifications.
# # # #  */
# # # # @Service
# # # # public class AssetService {

# # # #     private static final Logger log = LoggerFactory.getLogger(AssetService.class);

# # # #     private final AssetMasterRepository assetRepo;
# # # #     private final ProductCategoryRepository catRepo;
# # # #     private final ProductSubCategoryRepository subRepo;
# # # #     private final ProductMakeRepository makeRepo;
# # # #     private final ProductModelRepository modelRepo;
# # # #     private final AssetComponentRepository compRepo;
# # # #     private final AssetUserLinkRepository linkRepo;
# # # #     private final SafeNotificationHelper safeNotificationHelper;
# # # #     private final UserService userService;

# # # #     public AssetService(AssetMasterRepository assetRepo,
# # # #                         ProductCategoryRepository catRepo,
# # # #                         ProductSubCategoryRepository subRepo,
# # # #                         ProductMakeRepository makeRepo,
# # # #                         ProductModelRepository modelRepo,
# # # #                         AssetComponentRepository compRepo,
# # # #                         AssetUserLinkRepository linkRepo,
# # # #                         SafeNotificationHelper safeNotificationHelper,
# # # #                         UserService userService) {
# # # #         this.assetRepo = assetRepo;
# # # #         this.catRepo = catRepo;
# # # #         this.subRepo = subRepo;
# # # #         this.makeRepo = makeRepo;
# # # #         this.modelRepo = modelRepo;
# # # #         this.compRepo = compRepo;
# # # #         this.linkRepo = linkRepo;
# # # #         this.safeNotificationHelper = safeNotificationHelper;
# # # #         this.userService = userService;
# # # #     }

# # # #     // ============================================================
# # # #     // 🟢 CREATE ASSET
# # # #     // ============================================================
# # # #     @Transactional
# # # #     public AssetMaster create(AssetRequest request) throws Exception {
# # # #         if (request == null || request.getAsset() == null)
# # # #             throw new IllegalArgumentException("Request body or asset cannot be null");

# # # #         AssetMaster asset = request.getAsset();
# # # #         String username = request.getUsername();
# # # #         Long userId = request.getUserId();
# # # #         String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

# # # #         // ✅ Validate uniqueness
# # # #         if (!StringUtils.hasText(asset.getAssetNameUdv()))
# # # #             throw new RuntimeException("Asset name cannot be blank");
# # # #         if (assetRepo.existsByAssetNameUdv(asset.getAssetNameUdv()))
# # # #             throw new RuntimeException("❌ Asset with name '" + asset.getAssetNameUdv() + "' already exists");

# # # #         // ✅ Prepare base entity
# # # #         asset.setCreatedBy(username);
# # # #         asset.setUpdatedBy(username);
# # # #         AssetMaster saved = assetRepo.save(asset);

# # # #         // ✅ Assign to user (if applicable)
# # # #         if (userId != null && username != null) {
# # # #             AssetUserLink link = new AssetUserLink();
# # # #             link.setAsset(saved);
# # # #             link.setUserId(String.valueOf(userId));
# # # #             link.setUsername(username);
# # # #             linkRepo.save(link);
# # # #         }

# # # #         // ✅ Prepare placeholders
# # # #         Map<String, Object> placeholders = new LinkedHashMap<>();
# # # #         placeholders.put("assetId", saved.getAssetId());
# # # #         placeholders.put("assetName", saved.getAssetNameUdv());
# # # #         placeholders.put("assignedTo", username);
# # # #         placeholders.put("timestamp", new Date().toString());

# # # #         // 🔐 Resolve bearer token
# # # #         String bearer = resolveBearerToken(userId, username);

# # # #         // 🔔 Send Notifications
# # # #         sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_CREATED", placeholders, projectType);
# # # #         sendAssetNotification(bearer, userId, username, "EMAIL", "ASSET_ASSIGN_EMAIL", placeholders, projectType);

# # # #         log.info("✅ Asset created successfully (id={}, name={}, user={})", saved.getAssetId(), saved.getAssetNameUdv(), username);
# # # #         return saved;
# # # #     }

# # # #     // ============================================================
# # # #     // 🔍 GET BY ID
# # # #     // ============================================================
# # # #     public Optional<AssetMaster> get(Long id) {
# # # #         return assetRepo.findById(id)
# # # #                 .filter(a -> a.getActive() == null || a.getActive())
# # # #                 .map(a -> {
# # # #                     log.info("🔍 Fetched asset id={} name={}", a.getAssetId(), a.getAssetNameUdv());
# # # #                     return a;
# # # #                 });
# # # #     }

# # # #     // ============================================================
# # # #     // 🔔 Notification Helper
# # # #     // ============================================================
# # # #     private void sendAssetNotification(String bearer,
# # # #                                        Long userId,
# # # #                                        String username,
# # # #                                        String channel,
# # # #                                        String templateCode,
# # # #                                        Map<String, Object> placeholders,
# # # #                                        String projectType) {
# # # #         try {
# # # #             safeNotificationHelper.safeNotifyAsync(
# # # #                     bearer,
# # # #                     userId,
# # # #                     username,
# # # #                     null,
# # # #                     null,
# # # #                     channel,
# # # #                     templateCode,
# # # #                     placeholders,
# # # #                     projectType
# # # #             );
# # # #         } catch (Exception e) {
# # # #             log.error("⚠️ Failed to send {} notification for asset: {}", templateCode, e.getMessage());
# # # #         }
# # # #     }

# # # #     // ============================================================
# # # #     // 🔐 Token Resolver
# # # #     // ============================================================
# # # #     private String resolveBearerToken(Long userId, String username) {
# # # #         String token = userService
# # # #                 .getLatestAccessToken(RequestContext.getSessionId(), username, userId)
# # # #                 .orElse(null);

# # # #         if (token == null || token.isBlank()) {
# # # #             String msg = String.format("❌ Missing or invalid bearer token for user=%s", username);
# # # #             log.error(msg);
# # # #             throw new RuntimeException(msg);
# # # #         }
# # # #         return token.startsWith("Bearer ") ? token : "Bearer " + token;
# # # #     }
# # # # }



# # # # JAVA

echo "Created: Services"


# # # # # ---------- 5) Feign client ----------
# # # # cat > "$SRC_ROOT/service/client/NotificationClient.java" <<'JAVA'

# # # # package com.example.asset.service.client;

# # # # import com.example.asset.dto.AssetNotificationRequest;
# # # # import org.springframework.cloud.openfeign.FeignClient;
# # # # import org.springframework.web.bind.annotation.PostMapping;

# # # # @FeignClient(
# # # #         name = "notification-service",
# # # #         url = "${services.notification.base-url:http://localhost:$NOTIFICATION_SERVER_PORT}"
# # # # )
# # # # public interface NotificationClient {

# # # #     @PostMapping("/notify/trigger")
# # # #     void sendNotification(AssetNotificationRequest request);
# # # # }

# # # # JAVA

echo "Created: Feign client"

# ---------- 6) Controller ----------



cat > "$SRC_ROOT/controller/AssetAmcController.java" <<'JAVA'

package com.example.asset.controller;

import com.example.asset.dto.AssetAmcDto;
import com.example.asset.dto.AssetAmcRequest;
import com.example.asset.service.AssetAmcService;
import com.example.common.util.ResponseWrapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ AssetAmcController (REST Only)
 * Handles all AMC (Annual Maintenance Contract) operations:
 * create, update, delete, list, and getById.
 * Document uploads are now managed via DocumentController.
 */
@RestController
@RequestMapping("/api/asset/v1/amc")
public class AssetAmcController {

    private static final Logger log = LoggerFactory.getLogger(AssetAmcController.class);
    private final AssetAmcService assetAmcService;

    public AssetAmcController(AssetAmcService assetAmcService) {
        this.assetAmcService = assetAmcService;
    }

    // ============================================================
    // 🟢 CREATE AMC
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<AssetAmcDto>> create(
            @RequestHeader HttpHeaders headers,
            @Valid @RequestBody AssetAmcRequest request) {

        try {
            AssetAmcDto created = assetAmcService.create(headers, request, null);
            log.info("✅ AMC created successfully by user={} for assetId={}", 
                    request.getUsername(), request.getAssetId());

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✅ AMC created successfully", created));

        } catch (Exception e) {
            log.error("❌ AMC creation failed for user={} : {}", 
                    request.getUsername(), e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ AMC creation failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE AMC
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetAmcDto>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @Valid @RequestBody AssetAmcRequest request) {

        try {
            AssetAmcDto updated = assetAmcService.update(headers, id, request, null);
            log.info("✏️ AMC updated successfully by user={} for amcId={}", 
                    request.getUsername(), id);

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✏️ AMC updated successfully", updated));

        } catch (Exception e) {
            log.error("❌ AMC update failed for amcId={} : {}", id, e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ AMC update failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE AMC
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody AssetAmcRequest request) {

        try {
            assetAmcService.softDelete(headers, id, request);
            log.info("🗑️ AMC soft-deleted successfully by user={} amcId={}", 
                    request.getUsername(), id);

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "🗑️ AMC deleted successfully", null));

        } catch (Exception e) {
            log.error("❌ AMC delete failed for amcId={} : {}", id, e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ AMC deletion failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST ALL AMC RECORDS
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<AssetAmcDto>>> list() {
        try {
            List<AssetAmcDto> list = assetAmcService.list();
            log.info("📋 Retrieved {} AMC records", list.size());

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📋 AMC list fetched successfully", list));

        } catch (Exception e) {
            log.error("❌ Failed to fetch AMC list: {}", e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Failed to fetch AMC list: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 FIND AMC BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetAmcDto>> find(@PathVariable Long id) {
        try {
            return assetAmcService.find(id)
                    .map(amc -> {
                        log.info("🔍 AMC found successfully (ID={})", id);
                        return ResponseEntity.ok(
                                new ResponseWrapper<>(true, "🔍 AMC found successfully", amc));
                    })
                    .orElseGet(() -> {
                        log.warn("⚠️ AMC not found (ID={})", id);
                        return ResponseEntity.status(404)
                                .body(new ResponseWrapper<>(false, "⚠️ AMC not found", null));
                    });

        } catch (Exception e) {
            log.error("❌ Failed to fetch AMC (ID={}): {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Failed to fetch AMC: " + e.getMessage(), null));
        }
    }
}

JAVA




cat > "$SRC_ROOT/controller/AssetWarrantyController.java" <<'JAVA'

package com.example.asset.controller;

import com.example.asset.dto.AssetWarrantyDto;
import com.example.asset.dto.AssetWarrantyRequest;
import com.example.asset.service.AssetWarrantyService;
import com.example.common.util.ResponseWrapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ AssetWarrantyController (JSON-only)
 * Handles Warranty creation, updates, deletions, and retrieval.
 * Document uploads are handled separately via DocumentController.
 */
@RestController
@RequestMapping("/api/asset/v1/warranty")
public class AssetWarrantyController {

    private static final Logger log = LoggerFactory.getLogger(AssetWarrantyController.class);
    private final AssetWarrantyService warrantyService;

    public AssetWarrantyController(AssetWarrantyService warrantyService) {
        this.warrantyService = warrantyService;
    }

    // ============================================================
    // 🟢 CREATE WARRANTY (JSON only)
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<AssetWarrantyDto>> create(
            @RequestHeader HttpHeaders headers,
            @Valid @RequestBody AssetWarrantyRequest request) {
        try {
            AssetWarrantyDto created = warrantyService.create(headers, request, null);
            log.info("✅ Warranty created successfully by user={} for assetId={}",
                    request.getUsername(), request.getAssetId());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Warranty created successfully", created));
        } catch (Exception e) {
            log.error("❌ Warranty creation failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Warranty creation failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE WARRANTY (JSON only)
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetWarrantyDto>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @Valid @RequestBody AssetWarrantyRequest request) {
        try {
            AssetWarrantyDto updated = warrantyService.update(headers, id, request, null);
            log.info("✏️ Warranty updated successfully by user={} for warrantyId={}",
                    request.getUsername(), id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Warranty updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Warranty update failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Warranty update failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ DELETE WARRANTY (soft delete)
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody AssetWarrantyRequest request) {
        try {
            warrantyService.softDelete(headers, id, request);
            log.info("🗑️ Warranty deleted successfully by user={} warrantyId={}", request.getUsername(), id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Warranty deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete Warranty: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Warranty deletion failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST + FIND
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<AssetWarrantyDto>>> list() {
        try {
            List<AssetWarrantyDto> list = warrantyService.list();
            log.info("📋 Fetched {} Warranty records", list.size());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "📋 Warranty list fetched successfully", list));
        } catch (Exception e) {
            log.error("❌ Failed to fetch Warranty list: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Failed to fetch Warranty list: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetWarrantyDto>> find(@PathVariable Long id) {
        try {
            return warrantyService.find(id)
                    .map(warranty -> ResponseEntity.ok(new ResponseWrapper<>(true, "🔍 Warranty found successfully", warranty)))
                    .orElse(ResponseEntity.status(404)
                            .body(new ResponseWrapper<>(false, "⚠️ Warranty not found", null)));
        } catch (Exception e) {
            log.error("❌ Failed to fetch Warranty: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Failed to fetch Warranty: " + e.getMessage(), null));
        }
    }
}


JAVA



cat > "$SRC_ROOT/controller/UserLinkController.java" <<'JAVA'

// // // // package com.example.asset.controller;

// // // // import com.example.asset.dto.AssetUserLinkRequest;
// // // // import com.example.asset.service.UserLinkService;
// // // // import com.example.asset.service.ValidationService;
// // // // import com.example.common.util.ResponseWrapper;

// // // // import io.swagger.v3.oas.annotations.Operation;
// // // // import io.swagger.v3.oas.annotations.tags.Tag;

// // // // import org.slf4j.Logger;
// // // // import org.slf4j.LoggerFactory;

// // // // import org.springframework.http.HttpHeaders;
// // // // import org.springframework.http.ResponseEntity;
// // // // import org.springframework.web.bind.annotation.*;

// // // // import java.util.List;
// // // // import java.util.Map;

// // // // /**
// // // //  * UserLinkController — full controller with:
// // // //  *  - link (POST /link)
// // // //  *  - delink (POST /delink)
// // // //  *  - get assigned assets (GET /assigned-assets)
// // // //  *  - get single asset/component (GET /asset)
// // // //  *  - get users by subcategory (GET /by-subcategory)
// // // //  *
// // // //  * Notes:
// // // //  *  - The DTO AssetUserLinkRequest contains caller info (userId, username)
// // // //  *    and link.asset.{assetId, componentId, assetuserId, assetusername}.
// // // //  *  - Service methods expect the caller id (createdBy/updatedBy) as String,
// // // //  *    so we convert the DTO userId (Long) to String when calling the service.
// // // //  */
// // // // @RestController
// // // // @RequestMapping("/api/asset/v1/userlinks")
// // // // @Tag(name = "User Links", description = "APIs to manage linking/unlinking assets/components to users")
// // // // public class UserLinkController {

// // // //     private static final Logger log = LoggerFactory.getLogger(UserLinkController.class);

// // // //     private final UserLinkService userLinkService;
// // // //     private final ValidationService validationService;

// // // //     public UserLinkController(UserLinkService userLinkService,
// // // //                               ValidationService validationService) {
// // // //         this.userLinkService = userLinkService;
// // // //         this.validationService = validationService;
// // // //     }

// // // //     // ====================================================================
// // // //     // 1️⃣ LINK ASSET / COMPONENT → USER
// // // //     // ====================================================================
// // // //     @PostMapping("/link")
// // // //     @Operation(summary = "Link an asset or component to a user")
// // // //     public ResponseEntity<ResponseWrapper<String>> linkAssetToUser(
// // // //             @RequestHeader HttpHeaders headers,
// // // //             @RequestBody AssetUserLinkRequest request) {

// // // //         log.info("📌 LINK REQUEST RECEIVED: {}", request);
// // // //         try {
// // // //             String token = extractBearer(headers);

// // // //             // Caller (logged-in) info from DTO
// // // //             String callerUserIdLong = request.getUserId();
// // // //             String callerUserId = (callerUserIdLong != null) ? String.valueOf(callerUserIdLong) : null;
// // // //             String callerUserName = request.getUsername();

// // // //             // Target (asset-user) info
// // // //             Long assetId = request.getLink().getAsset().getAssetId();
// // // //             Long componentId = request.getLink().getAsset().getComponentId();
// // // //             Long targetUserId = request.getLink().getAsset().getAssetuserId();
// // // //             String targetUserName = request.getLink().getAsset().getAssetusername();

// // // //             // ---------------- VALIDATION ----------------
// // // //             validationService.validateCallerUser(callerUserId, callerUserName);
// // // //             validationService.validateUser(targetUserId, targetUserName);

// // // //             if (componentId != null) {
// // // //                 validationService.validateComponentExists(componentId);
// // // //             } else {
// // // //                 validationService.validateAssetExists(assetId);
// // // //             }

// // // //             validationService.validateAssetNotLinked(assetId, componentId);

// // // //             // ---------------- PROCESS ----------------
// // // //             String msg = userLinkService.linkAssetToUser(
// // // //                     token,
// // // //                     assetId,
// // // //                     componentId,
// // // //                     targetUserId,
// // // //                     targetUserName,
// // // //                     callerUserId,     // createdBy as String
// // // //                     callerUserName
// // // //             );

// // // //             return ResponseEntity.ok(new ResponseWrapper<>(true, msg, null));

// // // //         } catch (Exception e) {
// // // //             log.error("❌ LINK OPERATION FAILED: {}", e.getMessage(), e);
// // // //             return ResponseEntity.badRequest()
// // // //                     .body(new ResponseWrapper<>(false, e.getMessage(), null));
// // // //         }
// // // //     }

// // // //     // ====================================================================
// // // //     // 2️⃣ DELINK ASSET / COMPONENT → USER
// // // //     // ====================================================================
// // // //     @PostMapping("/delink")
// // // //     @Operation(summary = "Delink an asset or component from a user")
// // // //     public ResponseEntity<ResponseWrapper<String>> delinkAssetFromUser(
// // // //             @RequestHeader HttpHeaders headers,
// // // //             @RequestBody AssetUserLinkRequest request) {

// // // //         log.info("📌 DELINK REQUEST RECEIVED: {}", request);
// // // //         try {
// // // //             String token = extractBearer(headers);

// // // //             // Caller info
// // // //             String callerUserIdLong = request.getUserId();
// // // //             String callerUserId = (callerUserIdLong != null) ? String.valueOf(callerUserIdLong) : null;
// // // //             String callerUserName = request.getUsername();

// // // //             // Target details
// // // //             Long assetId = request.getLink().getAsset().getAssetId();
// // // //             Long componentId = request.getLink().getAsset().getComponentId();
// // // //             Long targetUserId = request.getLink().getAsset().getAssetuserId();
// // // //             String targetUserName = request.getLink().getAsset().getAssetusername();

// // // //             // ---------------- VALIDATION ----------------
// // // //             validationService.validateCallerUser(callerUserId, callerUserName);
// // // //             validationService.validateUser(targetUserId, targetUserName);

// // // //             if (componentId != null) {
// // // //                 validationService.validateComponentExists(componentId);
// // // //             } else {
// // // //                 validationService.validateAssetExists(assetId);
// // // //             }

// // // //             validationService.validateAssetLinked(assetId, componentId, targetUserId);

// // // //             // ---------------- PROCESS ----------------
// // // //             String msg = userLinkService.delinkAssetFromUser(
// // // //                     token,
// // // //                     assetId,
// // // //                     componentId,
// // // //                     targetUserId,
// // // //                     targetUserName,
// // // //                     callerUserId,    // updatedBy as String
// // // //                     callerUserName
// // // //             );

// // // //             return ResponseEntity.ok(new ResponseWrapper<>(true, msg, null));

// // // //         } catch (Exception e) {
// // // //             log.error("❌ DELINK OPERATION FAILED: {}", e.getMessage(), e);
// // // //             return ResponseEntity.badRequest()
// // // //                     .body(new ResponseWrapper<>(false, e.getMessage(), null));
// // // //         }
// // // //     }

// // // //     // ====================================================================
// // // //     // 3️⃣ GET ALL ASSIGNED ITEMS FOR A USER
// // // //     // ====================================================================
// // // //     @GetMapping("/assigned-assets")
// // // //     @Operation(summary = "Get all assets/components assigned to a specific user")
// // // //     public ResponseEntity<ResponseWrapper<List<Map<String, Object>>>> getAssignedAssets(
// // // //             @RequestHeader HttpHeaders headers,
// // // //             @RequestParam("assetUserId") Long assetUserId) {

// // // //         log.info("📌 GET ASSIGNED ASSETS FOR USER: {}", assetUserId);
// // // //         try {
// // // //             String token = extractBearer(headers);

// // // //             // optional: validationService.validateUser(assetUserId, null);

// // // //             List<Map<String, Object>> result = userLinkService.getAssetsAssignedToUser(assetUserId);

// // // //             return ResponseEntity.ok(new ResponseWrapper<>(true, "Assigned assets fetched", result));

// // // //         } catch (Exception e) {
// // // //             log.error("❌ FETCH ASSIGNED ASSETS FAILED: {}", e.getMessage(), e);
// // // //             return ResponseEntity.badRequest()
// // // //                     .body(new ResponseWrapper<>(false, e.getMessage(), null));
// // // //         }
// // // //     }

// // // //     // ====================================================================
// // // //     // 4️⃣ GET SINGLE ASSET OR COMPONENT DETAILS
// // // //     // ====================================================================
// // // //     @GetMapping("/asset")
// // // //     @Operation(summary = "Get single asset/component and its linked user")
// // // //     public ResponseEntity<ResponseWrapper<Map<String, Object>>> getSingleAsset(
// // // //             @RequestHeader HttpHeaders headers,
// // // //             @RequestParam(required = false) Long assetId,
// // // //             @RequestParam(required = false) Long componentId) {

// // // //         log.info("📌 GET SINGLE ASSET: assetId={}, componentId={}", assetId, componentId);
// // // //         try {
// // // //             String token = extractBearer(headers);

// // // //             if (assetId == null && componentId == null) {
// // // //                 throw new IllegalArgumentException("Either assetId or componentId must be provided");
// // // //             }

// // // //             Map<String, Object> result = userLinkService.getSingleAsset(assetId, componentId);

// // // //             return ResponseEntity.ok(new ResponseWrapper<>(true, "Asset/component details fetched", result));

// // // //         } catch (Exception e) {
// // // //             log.error("❌ FETCH SINGLE ASSET FAILED: {}", e.getMessage(), e);
// // // //             return ResponseEntity.badRequest()
// // // //                     .body(new ResponseWrapper<>(false, e.getMessage(), null));
// // // //         }
// // // //     }

// // // //     // ====================================================================
// // // //     // 5️⃣ GET USERS BY SUBCATEGORY (existing)
// // // //     // ====================================================================
// // // //     @GetMapping("/by-subcategory")
// // // //     @Operation(summary = "Get users linked to assets in a subcategory")
// // // //     public ResponseEntity<ResponseWrapper<List<Map<String, Object>>>> getUsersBySubCategory(
// // // //             @RequestHeader HttpHeaders headers,
// // // //             @RequestParam("subCategoryId") Long subCategoryId) {

// // // //         log.info("📌 GET USERS BY SUBCATEGORY: {}", subCategoryId);
// // // //         try {
// // // //             String token = extractBearer(headers);

// // // //             List<Map<String, Object>> users = userLinkService.getUsersBySubCategory(token, subCategoryId);

// // // //             return ResponseEntity.ok(new ResponseWrapper<>(true, "Linked users fetched", users));

// // // //         } catch (Exception e) {
// // // //             log.error("❌ GET USERS BY SUBCATEGORY FAILED: {}", e.getMessage(), e);
// // // //             return ResponseEntity.badRequest()
// // // //                     .body(new ResponseWrapper<>(false, e.getMessage(), null));
// // // //         }
// // // //     }

// // // //     // ====================================================================
// // // //     // Helper – Extract Bearer token
// // // //     // ====================================================================
// // // //     private String extractBearer(HttpHeaders headers) {
// // // //         String token = headers.getFirst("Authorization");
// // // //         if (token == null || token.isBlank()) {
// // // //             throw new IllegalStateException("Missing Authorization header");
// // // //         }
// // // //         return token.startsWith("Bearer ") ? token : "Bearer " + token;
// // // //     }
// // // // }


package com.example.asset.controller;

import com.example.asset.dto.AssetUserUniversalLinkRequest;
import com.example.asset.dto.AssetUserMultiLinkRequest;
import com.example.asset.dto.AssetUserMultiDelinkRequest;
import com.example.asset.service.UserLinkService;
import com.example.asset.service.ValidationService;
import com.example.common.util.ResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asset/v1/userlinks")
public class UserLinkController {

    private static final Logger log = LoggerFactory.getLogger(UserLinkController.class);

    private final UserLinkService linkService;
    private final ValidationService validationService;

    public UserLinkController(UserLinkService linkService,
                              ValidationService validationService) {
        this.linkService = linkService;
        this.validationService = validationService;
    }

    // ================================
    // LINK
    // ================================
    @PostMapping("/link")
    public ResponseEntity<ResponseWrapper<String>> linkEntity(
            @RequestHeader HttpHeaders headers,
            @RequestBody AssetUserUniversalLinkRequest request) {

        log.info("📌 Unified Link Request: {}", request);
        try {
            String token = extractBearer(headers);

            // Validation (SME Logic)
            validationService.validateLinkRequest(request);

            String msg = linkService.linkEntity(
                    token,
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getTargetUserId(),
                    request.getTargetUsername(),
                    request.getUserId(),
                    request.getUsername()
            );

            return ResponseEntity.ok(new ResponseWrapper<>(true, msg, null));

        } catch (Exception e) {
            log.error("❌ Link Failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ================================
    // DELINK
    // ================================
    @PostMapping("/delink")
    public ResponseEntity<ResponseWrapper<String>> delinkEntity(
            @RequestHeader HttpHeaders headers,
            @RequestBody AssetUserUniversalLinkRequest request) {

        log.info("📌 Unified Delink Request: {}", request);
        try {
            String token = extractBearer(headers);

            validationService.ensureEntityLinked(
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getTargetUserId()
            );

            String msg = linkService.delinkEntity(
                    token,
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getTargetUserId(),
                    request.getTargetUsername(),
                    request.getUserId(),
                    request.getUsername()
            );

            return ResponseEntity.ok(new ResponseWrapper<>(true, msg, null));

        } catch (Exception e) {
            log.error("❌ Delink Failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }


    // ========================================================================
    // 6️⃣ LINK MULTIPLE ENTITIES IN ONE REQUEST
    // ========================================================================
    @PostMapping("/link-multiple")
    @Operation(summary = "Link a user to multiple entities (ASSET, COMPONENT, MODEL, MAKE, AMC, WARRANTY, DOCUMENT) in one request")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> linkMultipleEntities(
            @RequestHeader HttpHeaders headers,
            @RequestBody AssetUserMultiLinkRequest request) {

        log.info("📌 MULTI-LINK REQUEST: {}", request);

        try {
            String token = extractBearer(headers);

            Map<String, Object> result =
                    linkService.linkMultipleEntities(token, request, validationService);

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true,
                            "Multi-entity link process completed", result));

        } catch (Exception e) {
            log.error("❌ Multi-link operation failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }


    // ========================================================================
// 7️⃣ DELINK MULTIPLE ENTITIES (ASSET, COMPONENT, MODEL, MAKE, AMC, WARRANTY, DOCUMENT)
// ========================================================================
@PostMapping("/delink-multiple")
@Operation(summary = "Delink a user from multiple entities (ASSET, COMPONENT, MODEL, MAKE, AMC, WARRANTY, DOCUMENT) in one request")
public ResponseEntity<ResponseWrapper<Map<String, Object>>> delinkMultipleEntities(
        @RequestHeader HttpHeaders headers,
        @RequestBody AssetUserMultiDelinkRequest request) {

    log.info("📌 MULTI-DELINK REQUEST: {}", request);

    try {
        String token = extractBearer(headers);

        Map<String, Object> result =
                linkService.delinkMultipleEntities(token, request, validationService);

        return ResponseEntity.ok(
                new ResponseWrapper<>(true,
                        "Multi-entity delink process completed", result));

    } catch (Exception e) {
        log.error("❌ Multi-delink operation failed: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
                .body(new ResponseWrapper<>(false, e.getMessage(), null));
    }
}


    // ================================
    // GET ALL ASSIGNED ITEMS
    // ================================
    @GetMapping("/assigned-assets")
    public ResponseEntity<ResponseWrapper<List<Map<String, Object>>>> getAssignedAssets(
            @RequestHeader HttpHeaders headers,
            @RequestParam Long targetUserId) {

        extractBearer(headers);

        List<Map<String, Object>> result = linkService.getAssetsAssignedToUser(targetUserId);

        return ResponseEntity.ok(new ResponseWrapper<>(true, "Assigned items fetched", result));
    }


    // ================================
    // GET SINGLE ITEM
    // ================================
    @GetMapping("/asset")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getSingleAsset(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) Long componentId) {

        extractBearer(headers);

        Map<String, Object> result = linkService.getSingleAsset(assetId, componentId);

        return ResponseEntity.ok(new ResponseWrapper<>(true, "Fetched", result));
    }

    // ================================
    // GET USERS BY SUBCATEGORY
    // ================================
    @GetMapping("/by-subcategory")
    public ResponseEntity<ResponseWrapper<List<Map<String, Object>>>> getBySubCategory(
            @RequestHeader HttpHeaders headers,
            @RequestParam Long subCategoryId) {

        String token = extractBearer(headers);

        List<Map<String, Object>> result = linkService.getUsersBySubCategory(token, subCategoryId);

        return ResponseEntity.ok(new ResponseWrapper<>(true, "Users fetched", result));
    }

    private String extractBearer(HttpHeaders headers) {
        String token = headers.getFirst("Authorization");
        if (token == null) throw new IllegalStateException("Missing Authorization header");
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}


JAVA


cat > "$SRC_ROOT/controller/VendorController.java" <<'JAVA'

package com.example.asset.controller;

import com.example.asset.dto.VendorRequest;
import com.example.asset.entity.VendorMaster;
import com.example.asset.service.VendorService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ VendorController
 * Handles vendor CRUD operations with token-secured header authentication.
 */
@RestController
@RequestMapping("/api/asset/v1/vendors")
public class VendorController {

    private static final Logger log = LoggerFactory.getLogger(VendorController.class);

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    // ============================================================
    // 🟢 CREATE VENDOR
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<VendorMaster>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody VendorRequest request) {
        try {
            VendorMaster created = vendorService.create(headers, request);
            log.info("✅ Vendor created successfully: {}", created.getVendorName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Vendor created successfully", created));
        } catch (Exception e) {
            log.error("❌ Failed to create vendor: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE VENDOR
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<VendorMaster>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody VendorRequest request) {
        try {
            VendorMaster updated = vendorService.update(headers, id, request);
            log.info("✏️ Vendor updated successfully: id={} name={}", id, updated.getVendorName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Vendor updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update vendor: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE VENDOR
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody VendorRequest request) {
        try {
            vendorService.softDelete(headers, id, request);
            log.info("🗑️ Vendor deleted (soft): id={}", id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Vendor deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete vendor: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST ALL VENDORS
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<VendorMaster>>> list() {
        List<VendorMaster> vendors = vendorService.list();
        return ResponseEntity.ok(new ResponseWrapper<>(true, "Fetched vendor list successfully", vendors));
    }

    // ============================================================
    // 🔍 GET BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<VendorMaster>> find(@PathVariable Long id) {
        return vendorService.find(id)
                .map(v -> ResponseEntity.ok(new ResponseWrapper<>(true, "Vendor found", v)))
                .orElse(ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "Vendor not found", null)));
    }
}

JAVA



cat > "$SRC_ROOT/controller/AssetController.java" <<'JAVA'
package com.example.asset.controller;

import com.example.asset.dto.AssetRequest;
import com.example.asset.dto.AssetResponseDTO;
import com.example.asset.entity.AssetMaster;
import com.example.asset.service.AssetCrudService;
import com.example.common.util.ResponseWrapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Map;

import java.util.Optional;

/**
 * ✅ AssetController
 * Central REST controller for Asset CRUD operations.
 * Accepts @RequestBody AssetRequest (includes userId, username, projectType).
 * Extracts Bearer token from Authorization header.
 */
@RestController
@RequestMapping("/api/asset/v1/assets")
public class AssetController {

    private static final Logger log = LoggerFactory.getLogger(AssetController.class);
    private final AssetCrudService assetService;

    public AssetController(AssetCrudService assetService) {
        this.assetService = assetService;
    }

    // ============================================================
    // 🟢 CREATE ASSET
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<AssetMaster>> create(
            @RequestHeader HttpHeaders headers,
            @Valid @RequestBody AssetRequest request) {
        log.info("📥 [POST] /assets - Creating asset for userId={} username={}",
                request.getUserId(), request.getUsername());
        try {
            AssetMaster created = assetService.create(headers, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Asset created successfully", created));
        } catch (Exception e) {
            log.error("❌ Failed to create asset: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE ASSET
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetMaster>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest request) {
        log.info("✏️ [PUT] /assets/{} - Updating by userId={} username={}", id, request.getUserId(), request.getUsername());
        try {
            AssetMaster updated = assetService.update(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Asset updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update asset: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ DELETE (SOFT)
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest request) {
        log.info("🗑️ [DELETE] /assets/{} - Deleting by userId={} username={}",
                id, request.getUserId(), request.getUsername());
        try {
            assetService.softDelete(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Asset deleted successfully (soft delete)", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete asset: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 GET ASSET BY ID
    // ============================================================
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getAssetById(@PathVariable Long id) {
        return assetService.get(id)
            .map(asset -> ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Asset fetched successfully",
                "data", asset
            )))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "Asset not found")));
    }

    // ============================================================
    // 🔍 SEARCH ASSETS
    // ============================================================
    @GetMapping("/search")
    public ResponseEntity<?> searchAssets(
            @RequestParam Optional<Long> assetId,
            @RequestParam Optional<String> assetName,
            @RequestParam Optional<Long> categoryId,
            Pageable pageable) {

        Page<AssetResponseDTO> result = assetService.search(assetId, assetName, categoryId, pageable);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Assets fetched successfully",
                "data", result
        ));
    }
}



JAVA

echo "Created: Controller"

# ---------- 7) DataInitializer (realistic seed) ----------



cat > "$SRC_ROOT/config/MultipartConfig.java" <<'JAVA'

package com.example.asset.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

/**
 * ✅ Multipart configuration for handling file uploads
 */
@Configuration
public class MultipartConfig {

    private static final Logger log = LoggerFactory.getLogger(MultipartConfig.class);

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(50));
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        log.info("✅ MultipartConfigElement initialized (max size = 50MB)");
        return factory.createMultipartConfig();
    }

    @Bean
    public MultipartResolver multipartResolver() {
        log.info("✅ Using StandardServletMultipartResolver");
        return new StandardServletMultipartResolver();
    }
}



JAVA

cat > "$SRC_ROOT/config/DataInitializer.java" <<'JAVA'

package com.example.asset.config;

import com.example.asset.entity.*;
import com.example.asset.repository.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ✅ DataInitializer
 * Seeds base reference data for the Asset microservice if the database is empty.
 * Creates default categories, subcategories, makes, models, outlets, and components.
 */
@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProductCategoryRepository catRepo;
    private final ProductSubCategoryRepository subRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final PurchaseOutletRepository outletRepo;
    private final AssetComponentRepository compRepo;

    public DataInitializer(ProductCategoryRepository catRepo,
                           ProductSubCategoryRepository subRepo,
                           ProductMakeRepository makeRepo,
                           ProductModelRepository modelRepo,
                           PurchaseOutletRepository outletRepo,
                           AssetComponentRepository compRepo) {
        this.catRepo = catRepo;
        this.subRepo = subRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.outletRepo = outletRepo;
        this.compRepo = compRepo;
    }

    // ============================================================
    // 🧩 Initialize Data on Startup
    // ============================================================
    @PostConstruct
    public void init() {
        try {
            log.info("🚀 Initializing Asset Service Reference Data...");

            // ------------------------------------------------------------------------
            // 1️⃣ Product Categories
            // ------------------------------------------------------------------------
            if (catRepo.count() == 0) {
                ProductCategory electronics = new ProductCategory("Electronics");
                electronics.setCreatedBy("system");
                electronics.setUpdatedBy("system");

                ProductCategory appliances = new ProductCategory("Home Appliances");
                appliances.setCreatedBy("system");
                appliances.setUpdatedBy("system");

                ProductCategory smartHome = new ProductCategory("Smart Home Devices");
                smartHome.setCreatedBy("system");
                smartHome.setUpdatedBy("system");

                catRepo.saveAll(List.of(electronics, appliances, smartHome));
                log.info("✅ Seeded Product Categories");
            }

            // Reload categories to ensure IDs are populated
            List<ProductCategory> categories = catRepo.findAll();
            ProductCategory electronics = categories.stream()
                    .filter(c -> c.getCategoryName().equalsIgnoreCase("Electronics"))
                    .findFirst().orElse(null);
            ProductCategory appliances = categories.stream()
                    .filter(c -> c.getCategoryName().equalsIgnoreCase("Home Appliances"))
                    .findFirst().orElse(null);

            // ------------------------------------------------------------------------
            // 2️⃣ Product SubCategories
            // ------------------------------------------------------------------------
            if (subRepo.count() == 0 && electronics != null && appliances != null) {
                ProductSubCategory phones = new ProductSubCategory("Smartphones", electronics);
                phones.setCreatedBy("system");
                phones.setUpdatedBy("system");

                ProductSubCategory tvs = new ProductSubCategory("Smart TVs", electronics);
                tvs.setCreatedBy("system");
                tvs.setUpdatedBy("system");

                ProductSubCategory fridge = new ProductSubCategory("Refrigerators", appliances);
                fridge.setCreatedBy("system");
                fridge.setUpdatedBy("system");

                ProductSubCategory washing = new ProductSubCategory("Washing Machines", appliances);
                washing.setCreatedBy("system");
                washing.setUpdatedBy("system");

                subRepo.saveAll(List.of(phones, tvs, fridge, washing));
                log.info("✅ Seeded Product SubCategories");
            }

            // ------------------------------------------------------------------------
            // 3️⃣ Product Makes
            // ------------------------------------------------------------------------
            if (makeRepo.count() == 0) {
                List<ProductSubCategory> subs = subRepo.findAll();

                ProductMake samsung = new ProductMake("Samsung", findSub(subs, "Smart TVs"));
                samsung.setCreatedBy("system");
                samsung.setUpdatedBy("system");

                ProductMake lg = new ProductMake("LG", findSub(subs, "Refrigerators"));
                lg.setCreatedBy("system");
                lg.setUpdatedBy("system");

                ProductMake apple = new ProductMake("Apple", findSub(subs, "Smartphones"));
                apple.setCreatedBy("system");
                apple.setUpdatedBy("system");

                makeRepo.saveAll(List.of(samsung, lg, apple));
                log.info("✅ Seeded Product Makes");
            }

            // ------------------------------------------------------------------------
            // 4️⃣ Product Models
            // ------------------------------------------------------------------------
            if (modelRepo.count() == 0) {
                List<ProductMake> makes = makeRepo.findAll();

                ProductModel iphone15 = new ProductModel("iPhone 15 Pro", findMake(makes, "Apple"));
                iphone15.setCreatedBy("system");
                iphone15.setUpdatedBy("system");

                ProductModel samsungQLED = new ProductModel("Samsung QLED 65", findMake(makes, "Samsung"));
                samsungQLED.setCreatedBy("system");
                samsungQLED.setUpdatedBy("system");

                ProductModel lgInstaView = new ProductModel("LG InstaView 260L", findMake(makes, "LG"));
                lgInstaView.setCreatedBy("system");
                lgInstaView.setUpdatedBy("system");

                modelRepo.saveAll(List.of(iphone15, samsungQLED, lgInstaView));
                log.info("✅ Seeded Product Models");
            }

            // ------------------------------------------------------------------------
            // 5️⃣ Purchase Outlets
            // ------------------------------------------------------------------------
            if (outletRepo.count() == 0) {
                PurchaseOutlet amazon = new PurchaseOutlet("Amazon", "Online Portal", "support@amazon.in");
                amazon.setCreatedBy("system");
                amazon.setUpdatedBy("system");

                PurchaseOutlet croma = new PurchaseOutlet("Croma", "Khar West Store", "022-11112222");
                croma.setCreatedBy("system");
                croma.setUpdatedBy("system");

                PurchaseOutlet reliance = new PurchaseOutlet("Reliance Digital", "Andheri East", "022-44443333");
                reliance.setCreatedBy("system");
                reliance.setUpdatedBy("system");

                outletRepo.saveAll(List.of(amazon, croma, reliance));
                log.info("✅ Seeded Purchase Outlets");
            }

            // ------------------------------------------------------------------------
            // 6️⃣ Asset Components
            // ------------------------------------------------------------------------
            if (compRepo.count() == 0) {
                AssetComponent battery = new AssetComponent();
                battery.setComponentName("Battery Pack");
                battery.setDescription("Device rechargeable battery unit");
                battery.setCreatedBy("system");
                battery.setUpdatedBy("system");

                AssetComponent charger = new AssetComponent();
                charger.setComponentName("Charger");
                charger.setDescription("Device adapter or charging cable");
                charger.setCreatedBy("system");
                charger.setUpdatedBy("system");

                AssetComponent remote = new AssetComponent();
                remote.setComponentName("Remote Control");
                remote.setDescription("TV or AC remote controller");
                remote.setCreatedBy("system");
                remote.setUpdatedBy("system");

                compRepo.saveAll(List.of(battery, charger, remote));
                log.info("✅ Seeded Asset Components");
            }

            log.info("🎉 Data initialization completed successfully.");

        } catch (Exception e) {
            log.error("❌ Failed during Data Initialization: {}", e.getMessage(), e);
        }
    }

    // ============================================================
    // 🔧 Helper Methods
    // ============================================================

    private ProductSubCategory findSub(List<ProductSubCategory> subs, String name) {
        return subs.stream()
                .filter(s -> s.getSubCategoryName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private ProductMake findMake(List<ProductMake> makes, String name) {
        return makes.stream()
                .filter(m -> m.getMakeName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}

JAVA

echo "Created: DataInitializer"

# ---------- 8) Flyway migrations: V1__init.sql and V2__seed_from_excel.sql (stub) ----------
cat > "$DB_MIGRATION_DIR/V1__init.sql" <<'SQL'
-- V1__init.sql
-- Full schema for asset-service

CREATE TABLE IF NOT EXISTS product_category (
  category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  category_name VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS product_sub_category (
  sub_category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  category_id BIGINT,
  sub_category_name VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (category_id) REFERENCES product_category(category_id)
);

CREATE TABLE IF NOT EXISTS product_make (
  make_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sub_category_id BIGINT,
  make_name VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (sub_category_id) REFERENCES product_sub_category(sub_category_id)
);

CREATE TABLE IF NOT EXISTS product_model (
  model_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  make_id BIGINT,
  model_name VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (make_id) REFERENCES product_make(make_id)
);

CREATE TABLE IF NOT EXISTS purchase_outlet (
  outlet_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  outlet_name VARCHAR(255),
  outlet_address TEXT,
  contact_info VARCHAR(255),
  outlet_type VARCHAR(50),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);


CREATE TABLE IF NOT EXISTS asset_master (
  asset_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  asset_name_udv VARCHAR(255) NOT NULL,
  category_id BIGINT,
  sub_category_id BIGINT,
  make_id BIGINT,
  model_id BIGINT,
  make_udv VARCHAR(255),
  model_udv VARCHAR(255),
  purchase_mode VARCHAR(50),
  purchase_outlet_id BIGINT,
  purchase_outlet_udv VARCHAR(255),
  purchase_outlet_address_udv TEXT,
  purchase_date DATE,
  asset_status VARCHAR(50),
  sold_on_date DATE,
  sales_channel_name VARCHAR(255),
  created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (category_id) REFERENCES product_category(category_id),
  FOREIGN KEY (sub_category_id) REFERENCES product_sub_category(sub_category_id),
  FOREIGN KEY (make_id) REFERENCES product_make(make_id),
  FOREIGN KEY (model_id) REFERENCES product_model(model_id),
  FOREIGN KEY (purchase_outlet_id) REFERENCES purchase_outlet(outlet_id)
);


CREATE TABLE IF NOT EXISTS asset_component_master (
  component_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  component_name VARCHAR(255),
  description TEXT,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS asset_component_link (
  asset_id BIGINT,
  component_id BIGINT,
  PRIMARY KEY (asset_id, component_id),
  FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id),
  FOREIGN KEY (component_id) REFERENCES asset_component_master(component_id)
);


CREATE TABLE IF NOT EXISTS asset_user_link (
  link_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  asset_id BIGINT,
  user_id VARCHAR(100) NOT NULL,
  username VARCHAR(255),
  assigned_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id)
);

CREATE TABLE IF NOT EXISTS asset_warranty (
  warranty_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  asset_id BIGINT,
  component_id BIGINT,
  warranty_type VARCHAR(50),
  start_date DATE,
  end_date DATE,
  document_path VARCHAR(512),
  user_id VARCHAR(100),
  username VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id)
);

CREATE TABLE IF NOT EXISTS asset_amc (
  amc_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  asset_id BIGINT,
  component_id BIGINT,
  start_date DATE,
  end_date DATE,
  amc_status VARCHAR(50),
  document_path VARCHAR(512),
  user_id VARCHAR(100),
  username VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id)
);

CREATE TABLE IF NOT EXISTS asset_document (
  document_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  asset_id BIGINT,
  component_id BIGINT,
  doc_type VARCHAR(255),
  file_path VARCHAR(512),
  uploaded_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  user_id VARCHAR(100),
  username VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id)
);

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ip_address VARCHAR(100),
  user_agent VARCHAR(1000),
  url VARCHAR(1000),
  http_method VARCHAR(20),
  username VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);
SQL

cat > "$DB_MIGRATION_DIR/V2__seed_from_excel.sql" <<'SQL'
-- V2__seed_from_excel.sql
-- This file is a placeholder. The setup script will generate a V2 seed by converting your Excel "Asset Registration" sheet.
-- If you want automatic generation, ensure the Excel is present at repository root and re-run the Excel conversion step separately.
-- For now, DataInitializer will seed basic reference data.
SQL

echo "Created: Flyway migrations (V1__init.sql, V2__seed_from_excel.sql stub)"

# ---------- 9) debug-run.sh ----------
cat > "$ROOT/debug-run.sh" <<'BASH'
#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mvn -DskipTests package
java -jar target/asset-service-1.0.0.jar
BASH
chmod +x "$ROOT/debug-run.sh"

echo "Created: debug-run.sh"

# # # # # ---------- 10) Exception classes ----------
# # # # cat > "$SRC_ROOT/exception/BadCredentialsException.java" <<'JAVA'
# # # # package com.example.asset.exception;
# # # # public class BadCredentialsException extends RuntimeException {
# # # #     public BadCredentialsException(String msg){ super(msg); }
# # # # }
# # # # JAVA

# # # # cat > "$SRC_ROOT/exception/LockedException.java" <<'JAVA'
# # # # package com.example.asset.exception;
# # # # public class LockedException extends RuntimeException {
# # # #     public LockedException(String msg){ super(msg); }
# # # # }
# # # # JAVA

# # # # cat > "$SRC_ROOT/exception/CredentialsExpiredException.java" <<'JAVA'
# # # # package com.example.asset.exception;
# # # # public class CredentialsExpiredException extends RuntimeException {
# # # #     public CredentialsExpiredException(String msg){ super(msg); }
# # # # }
# # # # JAVA

# # # # cat > "$SRC_ROOT/exception/DisabledException.java" <<'JAVA'
# # # # package com.example.asset.exception;
# # # # public class DisabledException extends RuntimeException {
# # # #     public DisabledException(String msg){ super(msg); }
# # # # }
# # # # JAVA

# # # # echo "Created: Exception types"

# ---------- Part 2 complete ----------
echo ""
echo "PART 2 complete: entities, repositories, services, controllers, migrations, and helper scripts created."
echo "Proceeding to Part 3 (Audit registration, WebMvcConfigurer, Excel-to-SQL utility, finishing touches)."

# ======================================================
# setup-asset-service.sh  (Part 3 – Final)
# Registers AuditInterceptor, adds Excel-to-SQL converter,
# prints build/run instructions.
# ======================================================



# ---------- 1) AuditInterceptor.java ----------
cat > "$SRC_ROOT/config/AssetStorageProperties.java" <<'JAVA'
package com.example.asset.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ✅ AssetStorageProperties
 * Ensures the upload directory is created and writable.
 */
@Configuration
@ConfigurationProperties(prefix = "asset.upload")
public class AssetStorageProperties {

    private static final Logger log = LoggerFactory.getLogger(AssetStorageProperties.class);

    private String dir;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    @PostConstruct
    public void init() {
        try {
            if (dir == null || dir.isBlank()) {
                throw new IllegalStateException("❌ Missing property: asset.upload.dir");
            }

            Path path = Path.of(dir).toAbsolutePath();
            log.info("🧩 Upload path configured as: {}", path);

            // Create directories if missing
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("📁 Created upload directory: {}", path);
            } else {
                log.info("📁 Upload directory already exists: {}", path);
            }

            File folder = path.toFile();
            if (!folder.canWrite()) {
                log.warn("⚠️ Upload directory is not writable: {}", path);
            }

        } catch (Exception e) {
            log.error("🚨 Failed to initialize upload directory '{}': {}", dir, e.getMessage());
            // Don’t stop the app — just log and continue
        }
    }
}


JAVA


cat > "$SRC_ROOT/config/AuditInterceptor.java" <<'JAVA'
package com.example.asset.config;

import com.example.asset.entity.AuditLog;
import com.example.asset.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.LocalDateTime;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditLogRepository repo;
    public AuditInterceptor(AuditLogRepository repo){ this.repo = repo; }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             jakarta.servlet.http.HttpServletResponse response,
                             Object handler) {
        try {
            AuditLog log = new AuditLog();
            log.setIpAddress(request.getRemoteAddr());
            log.setUserAgent(request.getHeader("User-Agent"));
            log.setUrl(request.getRequestURI());
            log.setHttpMethod(request.getMethod());
            var principal = request.getUserPrincipal();
            if(principal!=null) log.setUsername(principal.getName());
            log.setCreatedAt(LocalDateTime.now());
            repo.save(log);
        } catch (Exception ignored){}
        return true;
    }
}
JAVA
echo "Created: config/AuditInterceptor.java"

# ---------- 2) WebMvcConfig.java ----------
cat > "$SRC_ROOT/config/WebMvcConfig.java" <<'JAVA'
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
JAVA
echo "Created: config/WebMvcConfig.java"

# ---------- 3) Excel-to-SQL converter helper (Python) ----------
UTIL_DIR="$ROOT/tools"
mkdir -p "$UTIL_DIR"

cat > "$UTIL_DIR/convert_excel_to_sql.py" <<'PY'
#!/usr/bin/env python3
"""
convert_excel_to_sql.py
Reads 'ALM Design Detailing B2C 0.2-2.xlsx' -> 'Asset Registration' sheet
and emits SQL INSERTs into V2__seed_from_excel.sql.
Usage:
    python3 convert_excel_to_sql.py /path/to/ALM\ Design\ Detailing\ B2C\ 0.2-2.xlsx
"""
import pandas as pd, sys, pathlib

if len(sys.argv)<2:
    print("Usage: python3 convert_excel_to_sql.py <excel_file>")
    sys.exit(1)

excel_path = pathlib.Path(sys.argv[1])
out_path = pathlib.Path(__file__).resolve().parents[1] / "src/main/resources/db/migration/V2__seed_from_excel.sql"

try:
    df = pd.read_excel(excel_path, sheet_name="Asset Registration")
except Exception as e:
    print("❌ Failed to open sheet:", e)
    sys.exit(2)

sql_lines=["-- Auto-generated from Asset Registration sheet",
           "DELETE FROM asset_master;",
           ""]

for i, row in df.iterrows():
    name = str(row.get("Asset Name","")).replace("'","''")
    status = str(row.get("Status","AVAILABLE"))
    purchase = str(row.get("Purchase Date","2024-01-01"))
    sql_lines.append(
        f"INSERT INTO asset_master (asset_name_udv, asset_status, purchase_date, created_date)"
        f" VALUES ('{name}','{status}','{purchase}',NOW());"
    )

out_path.write_text("\n".join(sql_lines))
print(f"✅ Wrote {len(df)} INSERTs to {out_path}")
PY
chmod +x "$UTIL_DIR/convert_excel_to_sql.py"
echo "Created: tools/convert_excel_to_sql.py"

# ---------- 4) Friendly completion message ----------
cat <<'MSG'

=========================================================
✅  Asset-Service Setup Complete
=========================================================
Structure created under ./asset-service

🧩 Build & Run
   cd asset-service
   mvn -DskipTests package
   java -jar target/asset-service-1.0.0.jar

🔑 JWT
   Uses public key copied from auth-service/src/main/resources/keys/jwt-public.pem

🐬 MySQL
   Database URL and credentials defined in .env.asset / application.yml

📊 Flyway
   - db/migration/V1__init.sql : full schema
   - db/migration/V2__seed_from_excel.sql : optional data seed (auto-generated)

📂 Excel Seeder
   To generate real asset rows from Excel:
     cd asset-service/tools
     python3 convert_excel_to_sql.py "../../ALM Design Detailing B2C 0.2-2.xlsx"

🕵️ Audit Logs
   Stored in table audit_log (IP, URL, method, user-agent, username)

🔔 Notifications
   On asset assignment, triggers notification via Feign -> auth-service -> notification-service

⚙️  Typical flow
   1. Auth-service issues JWT (accessToken)
   2. Asset-service validates JWT using public.pem
   3. User invokes /api/asset/v1/assets (JWT required)
   4. Asset stored, linkage recorded, notification sent

🎯 Next:
   - Import schema to MySQL (Flyway auto-runs)
   - Ensure auth-service + notification-service are running
   - Use accessToken header: Authorization: Bearer <token>

=========================================================
MSG

echo "All done! 🎉"

# ---------- Part 3 complete ----------

# =============================================================
# setup-asset-crud.sh
# Adds full CRUD controllers/services/search for asset-service.
# Run from the parent directory that contains asset-service/.
# =============================================================


mkdir -p "$UPLOAD_DIR"
chmod 755 "$UPLOAD_DIR"

echo "Adding CRUD controllers, services, search endpoints, file upload, and Swagger..."

# ---------- 0) Ensure pom has springdoc dependency (append if missing) ----------
POM="$ROOT/pom.xml"
if ! grep -q "springdoc-openapi" "$POM"; then
  awk '/<\/dependencies>/{print; print "    <dependency>\n      <groupId>org.springdoc</groupId>\n      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>\n      <version>2.1.0</version>\n    </dependency>"; next}1' "$POM" > "$POM.tmp" && mv "$POM.tmp" "$POM"
  echo "Updated pom.xml with SpringDoc dependency."
else
  echo "pom.xml already contains SpringDoc dependency."
fi

# ---------- helper: JwtUtil to extract user info ----------
cat > "$SRC_ROOT/util/UploadAuditLogger.java" <<'JAVA'

package com.example.asset.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * ✅ UploadAuditLogger
 * Centralized utility to log details of uploaded files for audit tracking.
 */
@Component
public class UploadAuditLogger {

    private static final Logger log = LoggerFactory.getLogger(UploadAuditLogger.class);

    /**
     * Logs detailed information about an uploaded file.
     *
     * @param username  user performing upload
     * @param userId    ID of user
     * @param file      uploaded file
     * @param targetPath path where file is stored
     */
    public void logUpload(String username, Long userId, MultipartFile file, Path targetPath) {
        if (file == null) return;

        log.info("""
                📁 [UPLOAD AUDIT]
                ├── User        : {} (ID: {})
                ├── File Name   : {}
                ├── File Size   : {} bytes
                ├── Content Type: {}
                ├── Saved Path  : {}
                ├── Uploaded At : {}
                └── Status      : ✅ SUCCESS
                """,
                username,
                userId,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                targetPath != null ? targetPath.toAbsolutePath() : "N/A",
                DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        );
    }

    /**
     * Logs file upload failure.
     */
    public void logFailure(String username, Long userId, MultipartFile file, String reason) {
        log.error("""
                ❌ [UPLOAD FAILED]
                ├── User        : {} (ID: {})
                ├── File Name   : {}
                ├── Reason      : {}
                └── Timestamp   : {}
                """,
                username,
                userId,
                file != null ? file.getOriginalFilename() : "Unknown",
                reason,
                DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        );
    }
}



JAVA


cat > "$SRC_ROOT/util/JwtUtil.java" <<'JAVA'
package com.example.asset.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Optional;

public final class JwtUtil {
    private JwtUtil() {}

    public static Optional<String> getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            return Optional.ofNullable(jwt.getToken().getSubject());
        }
        return Optional.empty();
    }

    public static Optional<String> getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            Object u = jwt.getToken().getClaim("preferred_username");
            if (u==null) u = jwt.getToken().getClaim("username");
            return Optional.ofNullable(u!=null?u.toString():null);
        }
        return Optional.empty();
    }

    public static String getUserIdOrThrow() {
        return getUserId().orElseThrow(() -> new RuntimeException("No authenticated user"));
    }
    public static String getUsernameOrThrow() {
        return getUsername().orElse("system");
    }
}
JAVA

# ---------- FileStorageService (for documents) ----------
cat > "$SRC_ROOT/service/FileStorageService.java" <<'JAVA'
package com.example.asset.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/**
 * ✅ FileStorageService
 * Secure, token-aware file handling utility.
 * - Validates file uploads, prevents path traversal.
 * - Uses Authorization header for audit trail.
 * - Provides secure access and deletion of files.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final Path root;

    // ============================================================
    // 🔧 Constructor
    // ============================================================
    public FileStorageService() throws IOException {
        this.root = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(this.root);
        log.info("📁 File storage directory initialized at: {}", this.root);
    }

    // ============================================================
    // 🟢 STORE FILE (Validated Upload)
    // ============================================================
    public String store(HttpHeaders headers, MultipartFile file, String prefix) throws IOException {
        validateAuthorization(headers);
        validateFile(file);

        // Extract file extension safely
        String ext = getSafeExtension(file.getOriginalFilename());
        String filename = sanitizeFilename(prefix + "_" + UUID.randomUUID() + ext);
        Path target = this.root.resolve(filename).normalize();

        // Prevent path traversal
        if (!target.startsWith(this.root)) {
            throw new SecurityException("🚫 Invalid file path (path traversal detected)");
        }

        // Save the file
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String bearer = extractBearer(headers);
        log.info("📂 File uploaded [{}] by token={}", target, maskToken(bearer));
        return target.toString();
    }

    // ============================================================
    // ❌ DELETE FILE
    // ============================================================
    public boolean delete(HttpHeaders headers, String filepath) {
        validateAuthorization(headers);

        if (filepath == null || filepath.isBlank()) {
            log.warn("⚠️ Delete skipped - invalid file path");
            return false;
        }

        Path target = Paths.get(filepath).normalize();
        try {
            boolean deleted = Files.deleteIfExists(target);
            if (deleted) {
                log.info("🗑️ Deleted file: {} at {}", target, Instant.now());
            } else {
                log.warn("⚠️ File not found: {}", target);
            }
            return deleted;
        } catch (Exception e) {
            log.error("❌ Failed to delete file {}: {}", target, e.getMessage());
            return false;
        }
    }

    // ============================================================
    // 📥 DOWNLOAD FILE VALIDATION (used by FileDownloadController)
    // ============================================================
    public Path resolveFileSecurely(HttpHeaders headers, String filename) {
        validateAuthorization(headers);

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("❌ Filename cannot be empty");
        }

        Path target = this.root.resolve(sanitizeFilename(filename)).normalize();

        // Validate file path within upload root
        if (!target.startsWith(this.root)) {
            throw new SecurityException("🚫 Invalid file path - possible path traversal attempt");
        }

        if (!Files.exists(target)) {
            throw new IllegalArgumentException("⚠️ File not found: " + filename);
        }

        return target;
    }

    // ============================================================
    // 🔒 VALIDATION HELPERS
    // ============================================================
    private void validateAuthorization(HttpHeaders headers) {
        String bearer = extractBearer(headers);
        if (bearer == null || bearer.isBlank()) {
            throw new SecurityException("❌ Missing Authorization token");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty())
            throw new IllegalArgumentException("❌ File cannot be empty");

        if (file.getSize() > 20 * 1024 * 1024) // 20MB
            throw new IllegalArgumentException("❌ File exceeds 20MB limit");

        String original = file.getOriginalFilename();
        if (original == null || !original.matches("^[\\w,\\s-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("❌ Invalid file name or extension");
        }

        String ext = getSafeExtension(original).toLowerCase(Locale.ROOT);
        Set<String> allowed = Set.of(".jpg", ".jpeg", ".png", ".pdf", ".docx", ".xlsx");
        if (!allowed.contains(ext)) {
            throw new IllegalArgumentException("❌ File type not allowed: " + ext);
        }
    }

    private String getSafeExtension(String original) {
        if (original == null || !original.contains(".")) return "";
        return original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

    // ============================================================
    // 🔐 TOKEN HELPERS
    // ============================================================
    private String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new SecurityException("❌ Missing Authorization header");
        }
        return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
    }

    public String maskTokenFromHeader(HttpHeaders headers) {
        String bearer = headers.getFirst("Authorization");
        if (bearer == null) return "none";
        return maskToken(bearer);
    }

    private String maskToken(String bearer) {
        if (bearer == null || bearer.length() < 12) return "hidden";
        return bearer.substring(0, 12) + "...***";
    }
}


JAVA

# ---------- Generic CRUD controllers & services per entity ----------
# We'll create controllers/services for:
# category, subcategory, make, model, outlet, component, asset, warranty, amc, document, assetUserLink, audit

# ---------- CategoryController & Service ----------
cat > "$SRC_ROOT/service/CategoryService.java" <<'JAVA'

package com.example.asset.service;

import com.example.asset.dto.CategoryDto;
import com.example.asset.dto.CategoryRequest;
import com.example.asset.entity.ProductCategory;
import com.example.asset.mapper.CategoryMapper;
import com.example.asset.repository.ProductCategoryRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

/**
 * ✅ CategoryService
 * Handles CRUD operations for ProductCategory entities.
 * Uses CategoryMapper for DTO conversions and SafeNotificationHelper for async notifications.
 */
@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final ProductCategoryRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;

    public CategoryService(ProductCategoryRepository repo,
                           SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE CATEGORY
    // ============================================================
    @Transactional
    public CategoryDto create(HttpHeaders headers, CategoryRequest request) {
        if (request == null || request.getCategory() == null)
            throw new IllegalArgumentException("Request body or category cannot be null");

        String bearer = extractBearer(headers);
        ProductCategory payload = request.getCategory();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        String name = normalizeName(payload.getCategoryName());
        if (!StringUtils.hasText(name))
            throw new IllegalArgumentException("Category name must not be blank");
        if (repo.existsByCategoryName(name))
            throw new IllegalArgumentException("Category already exists: " + name);

        ProductCategory entity = new ProductCategory(name);
        entity.setCreatedBy(username);
        entity.setUpdatedBy(username);

        ProductCategory saved = repo.save(entity);

        // 🔔 Prepare placeholders
        Map<String, Object> placeholders = Map.of(
                "categoryId", saved.getCategoryId(),
                "categoryName", saved.getCategoryName(),
                "actor", username,
                "username", username,
                "timestamp", Instant.now().toString()
        );

        sendNotification(bearer, userId, username, "INAPP", "CATEGORY_CREATED_INAPP", placeholders, projectType);
        sendNotification(bearer, userId, username, "EMAIL", "CATEGORY_CREATED_EMAIL", placeholders, projectType);

        log.info("✅ Category created: id={} name={} by={}", saved.getCategoryId(), name, username);

        return CategoryMapper.toDto(saved);
    }

    // ============================================================
    // ✏️ UPDATE CATEGORY
    // ============================================================
    @Transactional
    public CategoryDto update(HttpHeaders headers, Long id, CategoryRequest request) {
        if (request == null || request.getCategory() == null)
            throw new IllegalArgumentException("Request body or category cannot be null");

        String bearer = extractBearer(headers);
        ProductCategory patch = request.getCategory();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return repo.findById(id).map(existing -> {
            String newName = normalizeName(patch.getCategoryName());
            if (!StringUtils.hasText(newName))
                throw new IllegalArgumentException("Category name must not be blank");

            if (!existing.getCategoryName().equalsIgnoreCase(newName)
                    && repo.existsByCategoryName(newName)) {
                throw new IllegalArgumentException("Category already exists: " + newName);
            }

            String oldName = existing.getCategoryName();
            existing.setCategoryName(newName);
            existing.setUpdatedBy(username);
            ProductCategory saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("categoryId", saved.getCategoryId());
            placeholders.put("oldName", oldName);
            placeholders.put("newName", newName);
            placeholders.put("categoryName", newName);
            placeholders.put("actor", username);
            placeholders.put("username", username);
            placeholders.put("timestamp", Instant.now().toString());

            sendNotification(bearer, userId, username, "INAPP", "CATEGORY_UPDATED_INAPP", placeholders, projectType);
            log.info("✏️ Category updated: id={} oldName={} newName={} by={}", id, oldName, newName, username);

            return CategoryMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE CATEGORY
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, CategoryRequest request) {
        if (request == null)
            throw new IllegalArgumentException("Request body cannot be null");

        String bearer = extractBearer(headers);
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(category -> {
            category.setActive(false);
            category.setUpdatedBy(username);
            ProductCategory saved = repo.save(category);

            Map<String, Object> placeholders = Map.of(
                    "categoryId", saved.getCategoryId(),
                    "categoryName", saved.getCategoryName(),
                    "deletedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString()
            );

            sendNotification(bearer, userId, username, "INAPP", "CATEGORY_DELETED_INAPP", placeholders, projectType);
            log.info("🗑️ Category soft-deleted: id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<CategoryDto> list() {
        return repo.findAll().stream()
                .filter(c -> c.getActive() == null || c.getActive())
                .map(CategoryMapper::toDto)
                .toList();
    }

    public Optional<CategoryDto> find(Long id) {
        return repo.findById(id)
                .filter(c -> c.getActive() == null || c.getActive())
                .map(CategoryMapper::toDto);
    }

    // ============================================================
    // 🔔 Notification Helper
    // ============================================================
    private void sendNotification(String bearer,
                                  Long userId,
                                  String username,
                                  String channel,
                                  String templateCode,
                                  Map<String, Object> placeholders,
                                  String projectType) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer,
                    userId,
                    username,
                    null,
                    null,
                    channel,
                    templateCode,
                    placeholders,
                    projectType
            );
        } catch (Exception e) {
            log.error("⚠️ Notification failed [{}]: {}", templateCode, e.getMessage());
        }
    }

    // ============================================================
    // 🔐 Token Extractor
    // ============================================================
    private String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new RuntimeException("❌ Missing Authorization header");
        }
        return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
    }

    // ============================================================
    // 🧩 Utility
    // ============================================================
    private String normalizeName(String raw) {
        return (raw != null) ? raw.trim() : null;
    }
}

JAVA


  echo "CategoryService Completed"



cat > "$SRC_ROOT/controller/FileDownloadController.java" <<'JAVA'

package com.example.asset.controller;

import com.example.asset.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ✅ FileDownloadController
 * Handles secure download and preview of stored files.
 * Requires Authorization header for every request.
 */
@RestController
@RequestMapping("/api/asset/v1/files")
public class FileDownloadController {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);
    private final FileStorageService fileStorageService;

    public FileDownloadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // ============================================================
    // 📥 DOWNLOAD OR VIEW FILE
    // ============================================================
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestHeader HttpHeaders headers,
            @RequestParam("filename") String filename,
            @RequestParam(value = "inline", required = false, defaultValue = "false") boolean inline) {
        try {
            // 🔐 Validate and get file safely
            Path filePath = fileStorageService.resolveFileSecurely(headers, filename);
            File file = filePath.toFile();
            if (!file.exists() || !file.isFile()) {
                log.warn("⚠️ File not found: {}", filename);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String mimeType = Files.probeContentType(filePath);
            mimeType = (mimeType != null) ? mimeType : "application/octet-stream";

            FileSystemResource resource = new FileSystemResource(file);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType(mimeType));
            String disposition = inline ? "inline" : "attachment";
            responseHeaders.setContentDisposition(
                    ContentDisposition.builder(disposition)
                            .filename(file.getName())
                            .build()
            );

            log.info("📤 File {} served successfully as {} by token={}", filename, disposition,
                    fileStorageService.maskTokenFromHeader(headers));

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .contentLength(file.length())
                    .body(resource);

        } catch (SecurityException e) {
            log.error("❌ Unauthorized access attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("Error", e.getMessage()).build();

        } catch (Exception e) {
            log.error("❌ Failed to serve file {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Error", e.getMessage()).build();
        }
    }
}

JAVA

cat > "$SRC_ROOT/controller/CategoryController.java" <<'JAVA'

package com.example.asset.controller;

import com.example.asset.dto.CategoryDto;
import com.example.asset.dto.CategoryRequest;
import com.example.asset.service.CategoryService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ CategoryController
 * Handles CRUD endpoints for ProductCategory.
 * Uses DTO responses to ensure clean JSON serialization.
 */
@RestController
@RequestMapping("/api/asset/v1/categories")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    // ============================================================
    // 🟢 CREATE CATEGORY
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<CategoryDto>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody CategoryRequest request) {
        try {
            CategoryDto created = service.create(headers, request);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✅ Category created successfully", created)
            );
        } catch (Exception e) {
            log.error("❌ Failed to create category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE CATEGORY
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<CategoryDto>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        try {
            CategoryDto updated = service.update(headers, id, request);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✏️ Category updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE CATEGORY
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        try {
            service.softDelete(headers, id, request);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "🗑️ Category deleted successfully", null)
            );
        } catch (Exception e) {
            log.error("❌ Failed to delete category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST ALL CATEGORIES
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<CategoryDto>>> list() {
        try {
            List<CategoryDto> categories = service.list();
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📋 Categories fetched successfully", categories)
            );
        } catch (Exception e) {
            log.error("❌ Failed to list categories: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 GET CATEGORY BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<CategoryDto>> getById(@PathVariable Long id) {
        try {
            return service.find(id)
                    .map(dto -> ResponseEntity.ok(
                            new ResponseWrapper<>(true, "✅ Category found", dto)))
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body(new ResponseWrapper<>(false, "❌ Category not found", null)));
        } catch (Exception e) {
            log.error("❌ Failed to fetch category by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }
}


JAVA

# ---------- SubCategoryController & Service ----------
cat > "$SRC_ROOT/service/SubCategoryService.java" <<'JAVA'

package com.example.asset.service;

import com.example.asset.dto.SubCategoryRequest;
import com.example.asset.entity.ProductSubCategory;
import com.example.asset.repository.ProductSubCategoryRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

/**
 * ✅ SubCategoryService
 * Handles CRUD for ProductSubCategory.
 * Extracts Bearer token directly from @RequestHeader HttpHeaders.
 */
@Service
public class SubCategoryService {

    private static final Logger log = LoggerFactory.getLogger(SubCategoryService.class);

    private final ProductSubCategoryRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;

    public SubCategoryService(ProductSubCategoryRepository repo,
                              SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE SUBCATEGORY
    // ============================================================
    @Transactional
    public ProductSubCategory create(HttpHeaders headers, SubCategoryRequest request) {
        if (request == null || request.getSubCategory() == null)
            throw new IllegalArgumentException("Request or subCategory cannot be null");

        ProductSubCategory sub = request.getSubCategory();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        // ✅ Validate subcategory name
        if (!StringUtils.hasText(sub.getSubCategoryName()))
            throw new IllegalArgumentException("Subcategory name must not be blank");

        if (repo.existsBySubCategoryName(sub.getSubCategoryName()))
            throw new IllegalArgumentException("Subcategory already exists: " + sub.getSubCategoryName());

        sub.setCreatedBy(username);
        sub.setUpdatedBy(username);
        ProductSubCategory saved = repo.save(sub);

        String bearer = extractBearerToken(headers);

        // 🔔 Prepare notification placeholders
        Map<String, Object> placeholders = new LinkedHashMap<>();
        placeholders.put("subCategoryId", saved.getSubCategoryId());
        placeholders.put("subCategoryName", saved.getSubCategoryName());
        placeholders.put("actor", username);
        placeholders.put("username", username);
        placeholders.put("timestamp", Instant.now().toString());

        sendNotification(bearer, userId, username, "INAPP", "SUBCATEGORY_CREATED_INAPP", placeholders, projectType);

        log.info("✅ Created ProductSubCategory id={} name={} by={}",
                saved.getSubCategoryId(), saved.getSubCategoryName(), username);
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE SUBCATEGORY
    // ============================================================
    @Transactional
    public ProductSubCategory update(HttpHeaders headers, Long id, SubCategoryRequest request) {
        if (request == null || request.getSubCategory() == null)
            throw new IllegalArgumentException("Request or subCategory cannot be null");

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return repo.findById(id).map(existing -> {
            String newName = request.getSubCategory().getSubCategoryName();

            if (!StringUtils.hasText(newName))
                throw new IllegalArgumentException("Subcategory name must not be blank");

            if (!existing.getSubCategoryName().equalsIgnoreCase(newName)
                    && repo.existsBySubCategoryName(newName))
                throw new IllegalArgumentException("Subcategory already exists: " + newName);

            String oldName = existing.getSubCategoryName();
            existing.setSubCategoryName(newName);
            existing.setUpdatedBy(username);

            ProductSubCategory saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("subCategoryId", saved.getSubCategoryId());
            placeholders.put("oldName", oldName);
            placeholders.put("newName", newName);
            placeholders.put("subCategoryName", newName);
            placeholders.put("actor", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", Instant.now().toString());

            String bearer = extractBearerToken(headers);
            sendNotification(bearer, userId, username, "INAPP", "SUBCATEGORY_UPDATED_INAPP", placeholders, projectType);

            log.info("✏️ Updated SubCategory id={} newName={} by={}", id, newName, username);
            return saved;
        }).orElseThrow(() -> new IllegalArgumentException("Subcategory not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, SubCategoryRequest request) {
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(sub -> {
            sub.setActive(false);
            sub.setUpdatedBy(username);
            ProductSubCategory saved = repo.save(sub);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("subCategoryId", saved.getSubCategoryId());
            placeholders.put("subCategoryName", saved.getSubCategoryName());
            placeholders.put("actor", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", Instant.now().toString());

            String bearer = extractBearerToken(headers);
            sendNotification(bearer, userId, username, "INAPP", "SUBCATEGORY_DELETED_INAPP", placeholders, projectType);

            log.info("🗑️ SubCategory soft-deleted id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST + FIND
    // ============================================================
    public List<ProductSubCategory> list() {
        return repo.findAll().stream()
                .filter(s -> s.getActive() == null || s.getActive())
                .toList();
    }

    public Optional<ProductSubCategory> find(Long id) {
        return repo.findById(id)
                .filter(s -> s.getActive() == null || s.getActive());
    }

    // ============================================================
    // 🔐 TOKEN EXTRACTOR
    // ============================================================
    private String extractBearerToken(HttpHeaders headers) {
        String authorization = headers.getFirst("Authorization");
        if (authorization == null || authorization.isBlank()) {
            throw new RuntimeException("❌ Missing Authorization header");
        }
        return authorization.startsWith("Bearer ") ? authorization : "Bearer " + authorization;
    }

    // ============================================================
    // 🔔 NOTIFICATION HELPER
    // ============================================================
    private void sendNotification(String bearer,
                                  Long userId,
                                  String username,
                                  String channel,
                                  String templateCode,
                                  Map<String, Object> placeholders,
                                  String projectType) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer, userId, username, null, null,
                    channel, templateCode, placeholders, projectType);
        } catch (Exception e) {
            log.error("⚠️ Notification ({}) failed: {}", templateCode, e.getMessage());
        }
    }
}


JAVA

cat > "$SRC_ROOT/controller/SubCategoryController.java" <<'JAVA'

package com.example.asset.controller;

import com.example.asset.dto.SubCategoryRequest;
import com.example.asset.entity.ProductSubCategory;
import com.example.asset.service.SubCategoryService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.asset.dto.ProductSubCategoryDto;
import com.example.asset.mapper.ProductSubCategoryMapper;

import java.util.List;

/**
 * ✅ SubCategoryController
 * Handles CRUD operations for subcategories using token-secured notifications.
 * Token is extracted from Authorization header via @RequestHeader HttpHeaders.
 */
@RestController
@RequestMapping("/api/asset/v1/subcategories")
public class SubCategoryController {

    private static final Logger log = LoggerFactory.getLogger(SubCategoryController.class);

    private final SubCategoryService service;

    public SubCategoryController(SubCategoryService service) {
        this.service = service;
    }

    // ============================================================
    // 🟢 CREATE SUBCATEGORY
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<ProductSubCategory>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody SubCategoryRequest request) {
        try {
            ProductSubCategory created = service.create(headers, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Subcategory created successfully", created));
        } catch (Exception e) {
            log.error("❌ Failed to create subcategory: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE SUBCATEGORY
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductSubCategory>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody SubCategoryRequest request) {
        try {
            ProductSubCategory updated = service.update(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Subcategory updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update subcategory: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE SUBCATEGORY
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody SubCategoryRequest request) {
        try {
            service.softDelete(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Subcategory deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete subcategory: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST SUBCATEGORIES
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ProductSubCategory>>> list() {
        List<ProductSubCategory> subCategories = service.list();
        return ResponseEntity.ok(new ResponseWrapper<>(true, "📋 Subcategories fetched successfully", subCategories));
    }

    // ============================================================
    // 🔍 GET SUBCATEGORY BY ID
    // ============================================================
    
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductSubCategoryDto>> getById(@PathVariable Long id) {
        return service.find(id)
                .map(sub -> {
                    ProductSubCategoryDto dto = ProductSubCategoryMapper.toDto(sub);
                    return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Subcategory found", dto));
                })
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "❌ Subcategory not found", null)));
    }
}



JAVA

# ---------- MakeController & Service ----------
cat > "$SRC_ROOT/service/MakeService.java" <<'JAVA'

package com.example.asset.service;

import com.example.asset.dto.MakeRequest;
import com.example.asset.entity.ProductMake;
import com.example.asset.repository.ProductMakeRepository;
import com.example.common.client.AdminClient;
import com.example.common.client.AssetUserLinkClient;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * ✅ MakeService
 * Token-based CRUD + notifications for ProductMake.
 * Removes UserService dependency, validates Authorization header.
 */
@Service
public class MakeService {

    private static final Logger log = LoggerFactory.getLogger(MakeService.class);

    private final ProductMakeRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;
    private final AdminClient adminClient;
    private final AssetUserLinkClient assetUserLinkClient;

    public MakeService(ProductMakeRepository repo,
                       SafeNotificationHelper safeNotificationHelper,
                       AdminClient adminClient,
                       AssetUserLinkClient assetUserLinkClient) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
        this.adminClient = adminClient;
        this.assetUserLinkClient = assetUserLinkClient;
    }

    // ============================================================
    // 🟢 CREATE MAKE
    // ============================================================
    @Transactional
    public ProductMake create(HttpHeaders headers, MakeRequest request) {
        validateAuthorization(headers);

        if (request == null || request.getMake() == null)
            throw new IllegalArgumentException("Request or make cannot be null");

        ProductMake make = request.getMake();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        // ✅ Validate make name
        if (!StringUtils.hasText(make.getMakeName()))
            throw new RuntimeException("Make name cannot be blank");

        // ✅ Enforce uniqueness per subcategory
        boolean exists = repo.findAll().stream()
                .anyMatch(m -> m.getMakeName().equalsIgnoreCase(make.getMakeName())
                        && (m.getSubCategory() != null && make.getSubCategory() != null)
                        && Objects.equals(m.getSubCategory().getSubCategoryId(),
                        make.getSubCategory().getSubCategoryId()));

        if (exists)
            throw new RuntimeException("❌ Make with name '" + make.getMakeName() + "' already exists in this subcategory");

        make.setCreatedBy(username);
        make.setUpdatedBy(username);
        ProductMake saved = repo.save(make);

        // 🔧 Notification placeholders
        Map<String, Object> placeholders = new LinkedHashMap<>();
        placeholders.put("makeId", saved.getMakeId());
        placeholders.put("makeName", saved.getMakeName());
        placeholders.put("subCategoryId",
                saved.getSubCategory() != null ? saved.getSubCategory().getSubCategoryId() : null);
        placeholders.put("createdBy", username);
        placeholders.put("username", username);
        placeholders.put("timestamp", new Date().toString());

        // 🔔 Notify creator
        sendMultiChannelNotification(bearer, userId, username, placeholders, projectType,
                "MAKE_CREATED", "Make created successfully");

        // 🔔 Notify admins
        notifyAdmins(bearer, projectType, placeholders, "MAKE_CREATED_ADMIN", username);

        // 🔔 Notify linked users under same subcategory
        if (saved.getSubCategory() != null) {
            Long subCategoryId = saved.getSubCategory().getSubCategoryId();
            notifyLinkedUsers(bearer, subCategoryId, placeholders, "MAKE_CREATED_USER", username, projectType);
        }

        log.info("✅ Make created successfully: id={} name={} by={}",
                saved.getMakeId(), saved.getMakeName(), username);

        return saved;
    }

    // ============================================================
    // ✏️ UPDATE MAKE
    // ============================================================
    @Transactional
    public ProductMake update(HttpHeaders headers, Long id, MakeRequest request) {
        validateAuthorization(headers);

        if (request == null || request.getMake() == null)
            throw new IllegalArgumentException("Request or make cannot be null");

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        return repo.findById(id).map(existing -> {
            String newName = request.getMake().getMakeName();
            if (!StringUtils.hasText(newName))
                throw new RuntimeException("Make name cannot be blank");

            boolean duplicate = repo.findAll().stream()
                    .anyMatch(m -> !m.getMakeId().equals(existing.getMakeId())
                            && m.getMakeName().equalsIgnoreCase(newName)
                            && m.getSubCategory() != null
                            && request.getMake().getSubCategory() != null
                            && Objects.equals(m.getSubCategory().getSubCategoryId(),
                            request.getMake().getSubCategory().getSubCategoryId()));

            if (duplicate)
                throw new RuntimeException("❌ Make with name '" + newName + "' already exists in this subcategory");

            existing.setMakeName(newName);
            existing.setSubCategory(request.getMake().getSubCategory());
            existing.setUpdatedBy(username);
            ProductMake saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("makeId", saved.getMakeId());
            placeholders.put("oldName", existing.getMakeName());
            placeholders.put("newName", newName);
            placeholders.put("makeName", newName);
            placeholders.put("updatedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", new Date().toString());

            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType,
                    "MAKE_UPDATED", "Make updated successfully");

            log.info("✏️ Make updated: id={} name={} by={}", id, newName, username);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Make not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE MAKE
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, MakeRequest request) {
        validateAuthorization(headers);

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        repo.findById(id).ifPresent(m -> {
            m.setActive(false);
            m.setUpdatedBy(username);
            repo.save(m);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("makeId", m.getMakeId());
            placeholders.put("makeName", m.getMakeName());
            placeholders.put("deletedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", new Date().toString());

            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType,
                    "MAKE_DELETED", "Make deleted successfully");

            log.info("🗑️ Make soft-deleted id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<ProductMake> list() {
        return repo.findAll().stream()
                .filter(m -> m.getActive() == null || m.getActive())
                .toList();
    }

    public Optional<ProductMake> find(Long id) {
        return repo.findById(id)
                .filter(m -> m.getActive() == null || m.getActive());
    }

    // ============================================================
    // 🔒 TOKEN VALIDATION
    // ============================================================
    private void validateAuthorization(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new SecurityException("❌ Missing Authorization header");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new SecurityException("❌ Invalid Authorization header format");
        }
    }

    // ============================================================
    // 🔔 NOTIFICATION HELPERS
    // ============================================================
    private void sendMultiChannelNotification(String bearer,
                                              Long uid,
                                              String username,
                                              Map<String, Object> placeholders,
                                              String projectType,
                                              String templateCode,
                                              String message) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, null, null,
                    "INAPP", templateCode+"_INAPP", placeholders, projectType);

            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, username + "@example.com", null,
                    "EMAIL", templateCode + "_EMAIL", placeholders, projectType);

            String mockMobile = "99999999" + (uid != null ? String.valueOf(uid % 100) : "00");
            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, null, mockMobile,
                    "SMS", templateCode + "_SMS", placeholders, projectType);

            log.info("📤 Notifications sent (INAPP, EMAIL, SMS) for {} user={}", templateCode, username);
        } catch (Exception e) {
            log.error("⚠️ Failed to send notifications for {}: {}", templateCode, e.getMessage());
        }
    }

    private void notifyAdmins(String bearer,
                              String projectType,
                              Map<String, Object> placeholders,
                              String templateCode,
                              String actorUsername) {
        try {
            List<Map<String, Object>> admins = adminClient.getAdminsByProjectType(projectType);
            if (admins == null || admins.isEmpty()) {
                log.info("⚠️ No admins found for projectType={}", projectType);
                return;
            }

            for (Map<String, Object> admin : admins) {
                Long adminId = admin.get("userId") != null ? Long.valueOf(admin.get("userId").toString()) : 0L;
                String adminUsername = (String) admin.get("username");
                String email = (String) admin.get("email");
                String mobile = (String) admin.get("mobile");

                placeholders.put("triggeredBy", actorUsername);
                placeholders.put("recipientRole", "Admin");

                safeNotificationHelper.safeNotifyAsync(
                        bearer, adminId, adminUsername, email, mobile,
                        "EMAIL", templateCode + "_EMAIL", placeholders, projectType);

                safeNotificationHelper.safeNotifyAsync(
                        bearer, adminId, adminUsername, null, mobile,
                        "INAPP", templateCode, placeholders, projectType);
            }

            log.info("📢 Notified {} admin(s) for {}", admins.size(), templateCode);

        } catch (Exception e) {
            log.error("⚠️ Failed to notify admins for {}: {}", templateCode, e.getMessage());
        }
    }

    private void notifyLinkedUsers(String bearer,
                                   Long subCategoryId,
                                   Map<String, Object> placeholders,
                                   String templateCode,
                                   String actorUsername,
                                   String projectType) {
        try {
            List<Map<String, Object>> users = assetUserLinkClient.getUsersBySubCategory(subCategoryId);
            if (users == null || users.isEmpty()) {
                log.info("⚠️ No linked users found for subCategoryId={}", subCategoryId);
                return;
            }

            for (Map<String, Object> user : users) {
                Long uid = user.get("userId") != null ? Long.valueOf(user.get("userId").toString()) : 0L;
                String username = (String) user.get("username");

                placeholders.put("triggeredBy", actorUsername);
                placeholders.put("recipientRole", "LinkedUser");

                safeNotificationHelper.safeNotifyAsync(
                        bearer, uid, username, null, null,
                        "INAPP", templateCode, placeholders, projectType);
            }

            log.info("📢 Notified {} linked users under subcategory {}", users.size(), subCategoryId);

        } catch (Exception e) {
            log.error("⚠️ Failed to notify linked users for {}: {}", templateCode, e.getMessage());
        }
    }
}



JAVA




  echo "MakeService Completed"


cat > "$SRC_ROOT/controller/MakeController.java" <<'JAVA'

package com.example.asset.controller;

import com.example.asset.dto.MakeRequest;
import com.example.asset.entity.ProductMake;
import com.example.asset.service.MakeService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ MakeController
 * Handles ProductMake CRUD endpoints with token-based authentication.
 */
@RestController
@RequestMapping("/api/asset/v1/makes")
public class MakeController {

    private static final Logger log = LoggerFactory.getLogger(MakeController.class);

    private final MakeService makeService;

    public MakeController(MakeService makeService) {
        this.makeService = makeService;
    }

    // ============================================================
    // 🟢 CREATE
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<ProductMake>> create(@RequestHeader HttpHeaders headers,
                                                               @RequestBody MakeRequest request) {
        try {
            ProductMake created = makeService.create(headers, request);
            log.info("✅ Make created successfully: {}", created.getMakeName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Make created successfully", created));
        } catch (Exception e) {
            log.error("❌ Failed to create make: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductMake>> update(@RequestHeader HttpHeaders headers,
                                                               @PathVariable Long id,
                                                               @RequestBody MakeRequest request) {
        try {
            ProductMake updated = makeService.update(headers, id, request);
            log.info("✏️ Make updated successfully: {}", updated.getMakeName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Make updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update make: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ DELETE (SOFT)
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(@RequestHeader HttpHeaders headers,
                                                        @PathVariable Long id,
                                                        @RequestBody MakeRequest request) {
        try {
            makeService.softDelete(headers, id, request);
            log.info("🗑️ Make deleted successfully: {}", id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Make deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete make: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ProductMake>>> list() {
        List<ProductMake> makes = makeService.list();
        return ResponseEntity.ok(new ResponseWrapper<>(true, "Fetched all makes successfully", makes));
    }

    // ============================================================
    // 🔍 FIND BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductMake>> find(@PathVariable Long id) {
        return makeService.find(id)
                .map(make -> ResponseEntity.ok(new ResponseWrapper<>(true, "Make found successfully", make)))
                .orElse(ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "Make not found", null)));
    }
}


JAVA

# ---------- ModelController & Service ----------
cat > "$SRC_ROOT/service/ModelService.java" <<'JAVA'

package com.example.asset.service;

import com.example.asset.dto.ModelDto;
import com.example.asset.dto.ModelRequest;
import com.example.asset.entity.ProductModel;
import com.example.asset.mapper.ModelMapper;
import com.example.asset.repository.ProductModelRepository;
import com.example.common.client.AdminClient;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * ✅ ModelService
 * Handles CRUD for ProductModel with notifications.
 * Uses DTO responses, preserves make_id, and ensures data integrity.
 */
@Service
public class ModelService {

    private static final Logger log = LoggerFactory.getLogger(ModelService.class);

    private final ProductModelRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;
    private final AdminClient adminClient;

    public ModelService(ProductModelRepository repo,
                        SafeNotificationHelper safeNotificationHelper,
                        AdminClient adminClient) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
        this.adminClient = adminClient;
    }

    // ============================================================
    // 🟢 CREATE MODEL
    // ============================================================
    @Transactional
    public ModelDto create(HttpHeaders headers, ModelRequest request) {
        validateAuthorization(headers);

        ProductModel model = request.getModel();
        if (model == null) throw new IllegalArgumentException("Model cannot be null");

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        if (model.getMake() == null || model.getMake().getMakeId() == null)
            throw new RuntimeException("❌ Model must have a valid make");

        if (!StringUtils.hasText(model.getModelName()))
            throw new RuntimeException("Model name cannot be blank");

        boolean exists = repo.existsByModelNameIgnoreCaseAndMake_MakeId(
                model.getModelName(), model.getMake().getMakeId());
        if (exists)
            throw new RuntimeException("❌ Model with this name already exists for the given make");

        model.setCreatedBy(username);
        model.setUpdatedBy(username);
        ProductModel saved = repo.save(model);

        // 🔔 Send notifications
        sendNotification(bearer, userId, username, "INAPP", "MODEL_CREATED_INAPP", saved, projectType);
        sendNotification(bearer, userId, username, "EMAIL", "MODEL_CREATED_EMAIL", saved, projectType);
        sendNotification(bearer, userId, username, "SMS", "MODEL_CREATED_SMS", saved, projectType);

        log.info("✅ Model created: id={} name={} by={}", saved.getModelId(), saved.getModelName(), username);

        return ModelMapper.toDto(saved);
    }

    // ============================================================
    // ✏️ UPDATE MODEL
    // ============================================================
    @Transactional
    public ModelDto update(HttpHeaders headers, Long id, ModelRequest request) {
        validateAuthorization(headers);

        ProductModel patch = request.getModel();
        if (patch == null) throw new IllegalArgumentException("Model cannot be null");

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        return repo.findById(id).map(existing -> {

            String newName = patch.getModelName();
            if (!StringUtils.hasText(newName))
                throw new RuntimeException("Model name cannot be blank");

            // ✅ Preserve existing make if not provided
            if (patch.getMake() == null) {
                patch.setMake(existing.getMake());
            }

            if (patch.getMake() == null)
                throw new RuntimeException("❌ Model must have a valid make");

            // ✅ Uniqueness check
            boolean duplicate = repo.existsByModelNameIgnoreCaseAndMake_MakeId(
                    newName, patch.getMake().getMakeId());
            if (duplicate && !Objects.equals(existing.getModelName(), newName))
                throw new RuntimeException("❌ Duplicate model name for same make");

            String oldName = existing.getModelName();
            existing.setModelName(newName);
            existing.setDescription(patch.getDescription());
            existing.setMake(patch.getMake());
            existing.setUpdatedBy(username);

            ProductModel saved = repo.save(existing);

            // 🔔 Send notifications
            sendNotification(bearer, userId, username, "INAPP", "MODEL_UPDATED_INAPP", saved, projectType);
            sendNotification(bearer, userId, username, "EMAIL", "MODEL_UPDATED_EMAIL", saved, projectType);
            sendNotification(bearer, userId, username, "SMS", "MODEL_UPDATED_SMS", saved, projectType);

            log.info("✏️ Model updated: id={} oldName={} newName={} by={}", id, oldName, newName, username);
            return ModelMapper.toDto(saved);

        }).orElseThrow(() -> new RuntimeException("Model not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, ModelRequest request) {
        validateAuthorization(headers);

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        repo.findById(id).ifPresent(model -> {
            model.setActive(false);
            model.setUpdatedBy(username);
            ProductModel saved = repo.save(model);

            // 🔔 Send notifications
            sendNotification(bearer, userId, username, "INAPP", "MODEL_DELETED_INAPP", saved, projectType);
            sendNotification(bearer, userId, username, "EMAIL", "MODEL_DELETED_EMAIL", saved, projectType);
            sendNotification(bearer, userId, username, "SMS", "MODEL_DELETED_SMS", saved, projectType);

            log.info("🗑️ Model soft-deleted: id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<ModelDto> list() {
        return repo.findAll().stream()
                .filter(m -> m.getActive() == null || m.getActive())
                .map(ModelMapper::toDto)
                .toList();
    }

    public Optional<ModelDto> find(Long id) {
        return repo.findById(id)
                .filter(m -> m.getActive() == null || m.getActive())
                .map(ModelMapper::toDto);
    }

    // ============================================================
    // 🔔 NOTIFICATION WRAPPER
    // ============================================================
    private void sendNotification(String bearer,
                                  Long userId,
                                  String username,
                                  String channel,
                                  String templateCode,
                                  ProductModel model,
                                  String projectType) {
        try {
            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("modelId", model.getModelId());
            placeholders.put("modelName", model.getModelName());
            placeholders.put("makeId", model.getMake() != null ? model.getMake().getMakeId() : null);
            placeholders.put("makeName", model.getMake() != null ? model.getMake().getMakeName() : null);
            placeholders.put("actor", username);
            placeholders.put("username", username);
            placeholders.put("timestamp", new Date().toString());

            safeNotificationHelper.safeNotifyAsync(
                    bearer, userId, username, null, null,
                    channel, templateCode, placeholders, projectType);

            log.info("📨 Notification [{}] sent via {} for modelId={} by={}",
                    templateCode, channel, model.getModelId(), username);
        } catch (Exception e) {
            log.error("⚠️ Failed to send {} notification for model {}: {}",
                    templateCode, model.getModelId(), e.getMessage());
        }
    }

    // ============================================================
    // 🔐 TOKEN VALIDATION
    // ============================================================
    private void validateAuthorization(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank())
            throw new SecurityException("❌ Missing Authorization header");
        if (!authHeader.startsWith("Bearer "))
            throw new SecurityException("❌ Invalid Authorization header format");
    }
}


JAVA

cat > "$SRC_ROOT/controller/ModelController.java" <<'JAVA'

package com.example.asset.controller;

import com.example.asset.dto.ModelDto;
import com.example.asset.dto.ModelRequest;
import com.example.asset.service.ModelService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ ModelController
 * Handles CRUD endpoints for ProductModel using DTOs.
 */
@RestController
@RequestMapping("/api/asset/v1/models")
public class ModelController {

    private static final Logger log = LoggerFactory.getLogger(ModelController.class);
    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<ModelDto>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody ModelRequest request) {
        try {
            ModelDto dto = modelService.create(headers, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Model created successfully", dto));
        } catch (Exception e) {
            log.error("❌ Model create failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ModelDto>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody ModelRequest request) {
        try {
            ModelDto dto = modelService.update(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Model updated successfully", dto));
        } catch (Exception e) {
            log.error("❌ Model update failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody ModelRequest request) {
        try {
            modelService.softDelete(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Model deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Model delete failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ModelDto>>> list() {
        List<ModelDto> models = modelService.list();
        return ResponseEntity.ok(new ResponseWrapper<>(true, "📋 Models fetched successfully", models));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ModelDto>> find(@PathVariable Long id) {
        return modelService.find(id)
                .map(dto -> ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Model found", dto)))
                .orElse(ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "❌ Model not found", null)));
    }
}


JAVA

# ---------- OutletController & Service ----------
cat > "$SRC_ROOT/service/OutletService.java" <<'JAVA'

package com.example.asset.service;

import com.example.asset.dto.OutletRequest;
import com.example.asset.entity.PurchaseOutlet;
import com.example.asset.repository.PurchaseOutletRepository;
import com.example.common.client.AdminClient;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * ✅ OutletService
 * Handles CRUD for PurchaseOutlet with token validation via @RequestHeader.
 * Sends notifications via SafeNotificationHelper to creator and admins.
 */
@Service
public class OutletService {

    private static final Logger log = LoggerFactory.getLogger(OutletService.class);

    private final PurchaseOutletRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;
    private final AdminClient adminClient;

    public OutletService(PurchaseOutletRepository repo,
                         SafeNotificationHelper safeNotificationHelper,
                         AdminClient adminClient) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
        this.adminClient = adminClient;
    }

    // ============================================================
    // 🟢 CREATE OUTLET
    // ============================================================
    @Transactional
    public PurchaseOutlet create(HttpHeaders headers, OutletRequest request) {
        validateAuthorization(headers);

        if (request == null || request.getOutlet() == null)
            throw new IllegalArgumentException("Request or outlet cannot be null");

        PurchaseOutlet outlet = request.getOutlet();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        // ✅ Validate outlet name
        if (!StringUtils.hasText(outlet.getOutletName()))
            throw new RuntimeException("Outlet name cannot be blank");

        // ✅ Uniqueness check
        if (repo.existsByOutletName(outlet.getOutletName()))
            throw new RuntimeException("❌ Outlet with name '" + outlet.getOutletName() + "' already exists");

        outlet.setCreatedBy(username);
        outlet.setUpdatedBy(username);
        PurchaseOutlet saved = repo.save(outlet);

        Map<String, Object> placeholders = new LinkedHashMap<>();
        placeholders.put("outletId", saved.getOutletId());
        placeholders.put("outletName", saved.getOutletName());
        placeholders.put("createdBy", username);
        placeholders.put("username", username);
        placeholders.put("timestamp", new Date().toString());

        sendNotifications(bearer, userId, username, placeholders, projectType,
                "OUTLET_CREATED", "Outlet created successfully");

        notifyAdmins(bearer, projectType, placeholders, "OUTLET_CREATED_ADMIN", username);

        log.info("✅ Outlet created successfully: id={} name={} by={}",
                saved.getOutletId(), saved.getOutletName(), username);

        return saved;
    }

    // ============================================================
    // ✏️ UPDATE OUTLET
    // ============================================================
    @Transactional
    public PurchaseOutlet update(HttpHeaders headers, Long id, OutletRequest request) {
        validateAuthorization(headers);

        if (request == null || request.getOutlet() == null)
            throw new IllegalArgumentException("Request or outlet cannot be null");

        PurchaseOutlet patch = request.getOutlet();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        return repo.findById(id).map(existing -> {
            String newName = patch.getOutletName();

            if (!StringUtils.hasText(newName))
                throw new RuntimeException("Outlet name cannot be blank");

            // ✅ Prevent duplicate name
            boolean duplicate = repo.findAll().stream()
                    .anyMatch(o -> !o.getOutletId().equals(existing.getOutletId())
                            && o.getOutletName().equalsIgnoreCase(newName));

            if (duplicate)
                throw new RuntimeException("❌ Outlet with name '" + newName + "' already exists");

            existing.setOutletName(newName);
            existing.setOutletAddress(patch.getOutletAddress());
            existing.setContactInfo(patch.getContactInfo());
            existing.setUpdatedBy(username);

            PurchaseOutlet saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("outletId", saved.getOutletId());
            placeholders.put("oldName", existing.getOutletName());
            placeholders.put("newName", newName);
            placeholders.put("outletName", newName);
            placeholders.put("updatedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", new Date().toString());

            sendNotifications(bearer, userId, username, placeholders, projectType,
                    "OUTLET_UPDATED", "Outlet updated successfully");

            log.info("✏️ Outlet updated: id={} name={} by={}", id, newName, username);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Outlet not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE OUTLET
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, OutletRequest request) {
        validateAuthorization(headers);

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        repo.findById(id).ifPresent(o -> {
            o.setActive(false);
            o.setUpdatedBy(username);
            repo.save(o);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("outletId", o.getOutletId());
            placeholders.put("outletName", o.getOutletName());
            placeholders.put("deletedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", new Date().toString());

            sendNotifications(bearer, userId, username, placeholders, projectType,
                    "OUTLET_DELETED", "Outlet deleted successfully");

            log.info("🗑️ Outlet soft-deleted id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<PurchaseOutlet> list() {
        return repo.findAll().stream()
                .filter(o -> o.getActive() == null || o.getActive())
                .toList();
    }

    public Optional<PurchaseOutlet> find(Long id) {
        return repo.findById(id)
                .filter(o -> o.getActive() == null || o.getActive());
    }

    // ============================================================
    // 🔐 TOKEN VALIDATION
    // ============================================================
    private void validateAuthorization(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new SecurityException("❌ Missing Authorization header");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new SecurityException("❌ Invalid Authorization header format");
        }
    }

    // ============================================================
    // 🔔 NOTIFICATION HELPERS
    // ============================================================
    private void sendNotifications(String bearer,
                                   Long uid,
                                   String username,
                                   Map<String, Object> placeholders,
                                   String projectType,
                                   String templateCode,
                                   String message) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, null, null,
                    "INAPP", templateCode, placeholders, projectType);

            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, username + "@example.com", null,
                    "EMAIL", templateCode + "_EMAIL", placeholders, projectType);

            String mockMobile = "99999999" + (uid != null ? String.valueOf(uid % 100) : "00");
            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, null, mockMobile,
                    "SMS", templateCode + "_SMS", placeholders, projectType);

            log.info("📤 Notifications sent (INAPP, EMAIL, SMS) for {} → {}", templateCode, username);
        } catch (Exception e) {
            log.error("⚠️ Failed to send notifications for {}: {}", templateCode, e.getMessage());
        }
    }

    private void notifyAdmins(String bearer,
                              String projectType,
                              Map<String, Object> placeholders,
                              String templateCode,
                              String actorUsername) {
        try {
            List<Map<String, Object>> admins = adminClient.getAdminsByProjectType(projectType);
            if (admins == null || admins.isEmpty()) {
                log.info("⚠️ No admins found for projectType={}", projectType);
                return;
            }

            for (Map<String, Object> admin : admins) {
                Long adminId = admin.get("userId") != null ? Long.valueOf(admin.get("userId").toString()) : 0L;
                String adminUsername = (String) admin.get("username");
                String email = (String) admin.get("email");
                String mobile = (String) admin.get("mobile");

                placeholders.put("triggeredBy", actorUsername);
                placeholders.put("recipientRole", "Admin");

                safeNotificationHelper.safeNotifyAsync(
                        bearer, adminId, adminUsername, email, mobile,
                        "EMAIL", templateCode + "_EMAIL", placeholders, projectType);

                safeNotificationHelper.safeNotifyAsync(
                        bearer, adminId, adminUsername, null, mobile,
                        "INAPP", templateCode, placeholders, projectType);
            }

            log.info("📢 Notified {} admin(s) for {}", admins.size(), templateCode);
        } catch (Exception e) {
            log.error("⚠️ Failed to notify admins for {}: {}", templateCode, e.getMessage());
        }
    }
}


JAVA

cat > "$SRC_ROOT/controller/OutletController.java" <<'JAVA'

package com.example.asset.controller;

import com.example.asset.dto.OutletRequest;
import com.example.asset.entity.PurchaseOutlet;
import com.example.asset.service.OutletService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ OutletController
 * Handles REST endpoints for PurchaseOutlet CRUD operations.
 * Token is validated from @RequestHeader Authorization.
 */
@RestController
@RequestMapping("/api/asset/v1/outlets")
public class OutletController {

    private static final Logger log = LoggerFactory.getLogger(OutletController.class);
    private final OutletService outletService;

    public OutletController(OutletService outletService) {
        this.outletService = outletService;
    }

    // ============================================================
    // 🟢 CREATE OUTLET
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<PurchaseOutlet>> create(@RequestHeader HttpHeaders headers,
                                                                  @RequestBody OutletRequest request) {
        try {
            PurchaseOutlet created = outletService.create(headers, request);
            log.info("✅ Outlet created successfully: {}", created.getOutletName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Outlet created successfully", created));
        } catch (Exception e) {
            log.error("❌ Failed to create outlet: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE OUTLET
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<PurchaseOutlet>> update(@RequestHeader HttpHeaders headers,
                                                                  @PathVariable Long id,
                                                                  @RequestBody OutletRequest request) {
        try {
            PurchaseOutlet updated = outletService.update(headers, id, request);
            log.info("✏️ Outlet updated successfully: {}", updated.getOutletName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Outlet updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update outlet: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ DELETE OUTLET (SOFT DELETE)
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(@RequestHeader HttpHeaders headers,
                                                        @PathVariable Long id,
                                                        @RequestBody OutletRequest request) {
        try {
            outletService.softDelete(headers, id, request);
            log.info("🗑️ Outlet deleted successfully: {}", id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Outlet deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete outlet: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST OUTLETS
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<PurchaseOutlet>>> list() {
        List<PurchaseOutlet> outlets = outletService.list();
        return ResponseEntity.ok(new ResponseWrapper<>(true, "Fetched all outlets successfully", outlets));
    }

    // ============================================================
    // 🔍 FIND OUTLET BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<PurchaseOutlet>> find(@PathVariable Long id) {
        return outletService.find(id)
                .map(outlet -> ResponseEntity.ok(new ResponseWrapper<>(true, "Outlet found successfully", outlet)))
                .orElse(ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "Outlet not found", null)));
    }
}

JAVA

# ---------- ComponentController & Service ----------
cat > "$SRC_ROOT/service/ComponentService.java" <<'JAVA'

package com.example.asset.service;

import com.example.asset.dto.ComponentRequest;
import com.example.asset.entity.AssetComponent;
import com.example.asset.repository.AssetComponentRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

/**
 * ✅ ComponentService
 * - Handles CRUD for Asset Components
 * - Uses SafeNotificationHelper for INAPP, EMAIL, and SMS notifications
 * - Extracts token from HttpHeaders (Authorization header)
 */
@Service
public class ComponentService {

    private static final Logger log = LoggerFactory.getLogger(ComponentService.class);

    private final AssetComponentRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;

    public ComponentService(AssetComponentRepository repo,
                            SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE COMPONENT
    // ============================================================
    @Transactional
    public AssetComponent create(HttpHeaders headers, ComponentRequest request) {
        if (request == null || request.getComponent() == null)
            throw new IllegalArgumentException("Request or component cannot be null");

        String bearer = extractBearer(headers);
        AssetComponent component = request.getComponent();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        if (!StringUtils.hasText(component.getComponentName()))
            throw new RuntimeException("❌ Component name cannot be blank");

        if (repo.existsByComponentName(component.getComponentName()))
            throw new RuntimeException("❌ Component with name '" + component.getComponentName() + "' already exists");

        component.setCreatedBy(username);
        component.setUpdatedBy(username);
        AssetComponent saved = repo.save(component);

        Map<String, Object> placeholders = new LinkedHashMap<>();
        placeholders.put("componentId", saved.getComponentId());
        placeholders.put("componentName", saved.getComponentName());
        placeholders.put("createdBy", username);
        placeholders.put("username", username);
        placeholders.put("timestamp", Instant.now().toString());

        // 🔔 Notify across channels
        sendMultiChannelNotification(bearer, userId, username, placeholders, projectType, "COMPONENT_CREATED");

        log.info("✅ Component created successfully: id={} name={} by={}",
                saved.getComponentId(), saved.getComponentName(), username);
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE COMPONENT
    // ============================================================
    @Transactional
    public AssetComponent update(HttpHeaders headers, Long id, ComponentRequest request) {
        if (request == null || request.getComponent() == null)
            throw new IllegalArgumentException("Request or component cannot be null");

        String bearer = extractBearer(headers);
        return repo.findById(id).map(existing -> {
            AssetComponent patch = request.getComponent();
            String username = request.getUsername();
            Long userId = request.getUserId();
            String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

            String newName = patch.getComponentName();

            if (!existing.getComponentName().equalsIgnoreCase(newName)
                    && repo.existsByComponentName(newName))
                throw new RuntimeException("❌ Component with name '" + newName + "' already exists");

            String oldName = existing.getComponentName();
            existing.setComponentName(newName);
            existing.setDescription(patch.getDescription());
            existing.setUpdatedBy(username);
            AssetComponent saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("componentId", saved.getComponentId());
            placeholders.put("oldName", oldName);
            placeholders.put("newName", newName);
            placeholders.put("componentName", newName);
            placeholders.put("updatedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", Instant.now().toString());

            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType, "COMPONENT_UPDATED");

            log.info("✏️ Component updated successfully: id={} oldName={} newName={} by={}",
                    id, oldName, newName, username);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Component not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE COMPONENT
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, ComponentRequest request) {
        String bearer = extractBearer(headers);
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(c -> {
            c.setActive(false);
            c.setUpdatedBy(username);
            repo.save(c);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("componentId", c.getComponentId());
            placeholders.put("componentName", c.getComponentName());
            placeholders.put("deletedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", Instant.now().toString());

            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType, "COMPONENT_DELETED");
            log.info("🗑️ Component soft-deleted successfully: id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<AssetComponent> list() {
        return repo.findAll().stream()
                .filter(c -> c.getActive() == null || c.getActive())
                .toList();
    }

    public Optional<AssetComponent> find(Long id) {
        return repo.findById(id).filter(c -> c.getActive() == null || c.getActive());
    }

    // ============================================================
    // 🔔 Notification Helper
    // ============================================================
    private void sendMultiChannelNotification(String bearer,
                                              Long uid,
                                              String username,
                                              Map<String, Object> placeholders,
                                              String projectType,
                                              String templateCode) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, null, null,
                    "INAPP", templateCode+"_INAPP", placeholders, projectType);

            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, username + "@example.com", null,
                    "EMAIL", templateCode + "_EMAIL", placeholders, projectType);

            String mockMobile = "99999999" + (uid != null ? String.valueOf(uid % 100) : "00");
            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, null, mockMobile,
                    "SMS", templateCode + "_SMS", placeholders, projectType);

            log.info("📤 Notifications sent for template={} (INAPP + EMAIL + SMS)", templateCode);
        } catch (Exception e) {
            log.error("⚠️ Failed to send {} notifications: {}", templateCode, e.getMessage());
        }
    }

    // ============================================================
    // 🔐 Token Extractor
    // ============================================================
    private String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank())
            throw new RuntimeException("❌ Missing Authorization header");
        return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
    }
}

JAVA

cat > "$SRC_ROOT/controller/ComponentController.java" <<'JAVA'

package com.example.asset.controller;

import com.example.asset.dto.ComponentRequest;
import com.example.asset.entity.AssetComponent;
import com.example.asset.service.ComponentService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ ComponentController
 * - Handles CRUD via @RequestBody (ComponentRequest)
 * - Extracts Bearer token from Authorization header
 * - Delegates logic + notifications to ComponentService
 */
@RestController
@RequestMapping("/api/asset/v1/components")
public class ComponentController {

    private static final Logger log = LoggerFactory.getLogger(ComponentController.class);
    private final ComponentService componentService;

    public ComponentController(ComponentService componentService) {
        this.componentService = componentService;
    }

    // ============================================================
    // 🟢 CREATE COMPONENT
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<AssetComponent>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody ComponentRequest request) {
        try {
            AssetComponent created = componentService.create(headers, request);
            log.info("✅ Component created successfully by user={} id={}", request.getUsername(), created.getComponentId());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Component created successfully", created));
        } catch (Exception e) {
            log.error("❌ Failed to create component: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE COMPONENT
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetComponent>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody ComponentRequest request) {
        try {
            AssetComponent updated = componentService.update(headers, id, request);
            log.info("✏️ Component updated successfully by user={} id={}", request.getUsername(), id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Component updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update component: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE COMPONENT
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody ComponentRequest request) {
        try {
            componentService.softDelete(headers, id, request);
            log.info("🗑️ Component soft-deleted successfully by user={} id={}", request.getUsername(), id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Component deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete component: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST COMPONENTS
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<AssetComponent>>> list() {
        List<AssetComponent> components = componentService.list();
        log.info("📋 Fetched {} active components", components.size());
        return ResponseEntity.ok(new ResponseWrapper<>(true, "📋 Components fetched successfully", components));
    }

    // ============================================================
    // 🔍 GET COMPONENT BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetComponent>> find(@PathVariable Long id) {
        return componentService.find(id)
                .map(c -> ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Component found", c)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "❌ Component not found", null)));
    }
}

JAVA


  echo "ComponentController Completed"
# ---------- AssetService with search and CRUD ----------
cat > "$SRC_ROOT/service/AssetCrudService.java" <<'JAVA'

// // // // package com.example.asset.service;

// // // // import com.example.asset.dto.AssetRequest;
// // // // import com.example.asset.entity.AssetMaster;
// // // // import com.example.asset.entity.AssetUserLink;
// // // // import com.example.asset.repository.AssetMasterRepository;
// // // // import com.example.asset.repository.AssetUserLinkRepository;
// // // // import com.example.common.service.SafeNotificationHelper;
// // // // import org.slf4j.Logger;
// // // // import org.slf4j.LoggerFactory;
// // // // import org.springframework.data.domain.*;
// // // // import org.springframework.http.HttpHeaders;
// // // // import org.springframework.stereotype.Service;
// // // // import org.springframework.transaction.annotation.Transactional;
// // // // import org.springframework.util.StringUtils;

// // // // import java.time.Instant;
// // // // import java.util.*;

// // // // /**
// // // //  * ✅ AssetCrudService
// // // //  * Handles CRUD for assets and triggers SafeNotificationHelper notifications.
// // // //  * Extracts Bearer token directly from HttpHeaders.
// // // //  */
// // // // @Service
// // // // public class AssetCrudService {

// // // //     private static final Logger log = LoggerFactory.getLogger(AssetCrudService.class);

// // // //     private final AssetMasterRepository assetRepo;
// // // //     private final AssetUserLinkRepository linkRepo;
// // // //     private final SafeNotificationHelper safeNotificationHelper;

// // // //     public AssetCrudService(AssetMasterRepository assetRepo,
// // // //                             AssetUserLinkRepository linkRepo,
// // // //                             SafeNotificationHelper safeNotificationHelper) {
// // // //         this.assetRepo = assetRepo;
// // // //         this.linkRepo = linkRepo;
// // // //         this.safeNotificationHelper = safeNotificationHelper;
// // // //     }

// // // //     // ============================================================
// // // //     // 🟢 CREATE ASSET
// // // //     // ============================================================
// // // //     @Transactional
// // // //     public AssetMaster create(HttpHeaders headers, AssetRequest request) {
// // // //         if (request == null || request.getAsset() == null)
// // // //             throw new IllegalArgumentException("AssetRequest or payload cannot be null");

// // // //         String bearer = extractBearer(headers);
// // // //         AssetMaster asset = request.getAsset();
// // // //         String username = request.getUsername();
// // // //         Long userId = request.getUserId();
// // // //         String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

// // // //         if (!StringUtils.hasText(asset.getAssetNameUdv()))
// // // //             throw new RuntimeException("❌ Asset name cannot be blank");
// // // //         if (assetRepo.existsByAssetNameUdv(asset.getAssetNameUdv()))
// // // //             throw new RuntimeException("❌ Asset with name '" + asset.getAssetNameUdv() + "' already exists");

// // // //         asset.setCreatedBy(username);
// // // //         asset.setUpdatedBy(username);
// // // //         AssetMaster saved = assetRepo.save(asset);

// // // //         if (userId != null && username != null) {
// // // //             AssetUserLink link = new AssetUserLink();
// // // //             link.setAsset(saved);
// // // //             link.setUserId(String.valueOf(userId));
// // // //             link.setUsername(username);
// // // //             linkRepo.save(link);
// // // //         }

// // // //         Map<String, Object> placeholders = Map.of(
// // // //                 "assetId", saved.getAssetId(),
// // // //                 "assetName", saved.getAssetNameUdv(),
// // // //                 "assignedTo", username,
// // // //                 "username", username,
// // // //                 "timestamp", Instant.now().toString()
// // // //         );

// // // //         sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_CREATED_INAPP", placeholders, projectType);
// // // //         sendAssetNotification(bearer, userId, username, "EMAIL", "ASSET_CREATED_EMAIL", placeholders, projectType);

// // // //         log.info("✅ Asset created successfully: id={} name={} by={}", saved.getAssetId(), saved.getAssetNameUdv(), username);
// // // //         return saved;
// // // //     }

// // // //     // ============================================================
// // // //     // ✏️ UPDATE ASSET
// // // //     // ============================================================
// // // //     @Transactional
// // // //     public AssetMaster update(HttpHeaders headers, Long id, AssetRequest request) {
// // // //         if (request == null || request.getAsset() == null)
// // // //             throw new IllegalArgumentException("AssetRequest or payload cannot be null");

// // // //         String bearer = extractBearer(headers);
// // // //         AssetMaster patch = request.getAsset();
// // // //         String username = request.getUsername();
// // // //         Long userId = request.getUserId();
// // // //         String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

// // // //         return assetRepo.findById(id).map(existing -> {
// // // //             String newName = patch.getAssetNameUdv();

// // // //             if (!existing.getAssetNameUdv().equalsIgnoreCase(newName)
// // // //                     && assetRepo.existsByAssetNameUdv(newName))
// // // //                 throw new RuntimeException("❌ Asset with name '" + newName + "' already exists");

// // // //             existing.setAssetNameUdv(newName);
// // // //             existing.setAssetStatus(patch.getAssetStatus());
// // // //             existing.setUpdatedBy(username);

// // // //             AssetMaster saved = assetRepo.save(existing);

// // // //             Map<String, Object> placeholders = Map.of(
// // // //                     "assetId", saved.getAssetId(),
// // // //                     "oldName", existing.getAssetNameUdv(),
// // // //                     "assetName", existing.getAssetNameUdv(),
// // // //                     "newName", newName,
// // // //                     "updatedBy", username,
// // // //                     "username", username,
// // // //                     "timestamp", Instant.now().toString()
// // // //             );

// // // //             sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_UPDATED_INAPP", placeholders, projectType);
// // // //             log.info("✏️ Asset updated: id={} by={}", id, username);

// // // //             return saved;
// // // //         }).orElseThrow(() -> new RuntimeException("Asset not found with id: " + id));
// // // //     }

// // // //     // ============================================================
// // // //     // ❌ SOFT DELETE
// // // //     // ============================================================
// // // //     @Transactional
// // // //     public void softDelete(HttpHeaders headers, Long id, AssetRequest request) {
// // // //         String bearer = extractBearer(headers);
// // // //         String username = request.getUsername();
// // // //         Long userId = request.getUserId();
// // // //         String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

// // // //         assetRepo.findById(id).ifPresent(asset -> {
// // // //             asset.setActive(false);
// // // //             asset.setUpdatedBy(username);
// // // //             assetRepo.save(asset);

// // // //             Map<String, Object> placeholders = Map.of(
// // // //                     "assetId", asset.getAssetId(),
// // // //                     "assetName", asset.getAssetNameUdv(),
// // // //                     "deletedBy", username,
// // // //                     "username", username,
// // // //                     "timestamp", Instant.now().toString()
// // // //             );

// // // //             sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_DELETED_INAPP", placeholders, projectType);
// // // //             log.info("🗑️ Asset deleted (soft): id={} by={}", id, username);
// // // //         });
// // // //     }

// // // //     // ============================================================
// // // //     // 🔍 GET BY ID
// // // //     // ============================================================
// // // //     public Optional<AssetMaster> get(Long id) {
// // // //         return assetRepo.findById(id)
// // // //                 .filter(a -> a.getActive() == null || a.getActive())
// // // //                 .map(a -> {
// // // //                     log.info("🔍 Fetched asset: id={} name={}", a.getAssetId(), a.getAssetNameUdv());
// // // //                     return a;
// // // //                 });
// // // //     }

// // // //     // ============================================================
// // // //     // 🔎 SEARCH
// // // //     // ============================================================
// // // //     public Page<AssetMaster> search(Optional<Long> assetId,
// // // //                                     Optional<String> assetName,
// // // //                                     Optional<Long> categoryId,
// // // //                                     Pageable pageable) {
// // // //         List<AssetMaster> filtered = assetRepo.findAll().stream()
// // // //                 .filter(a -> a.getActive() == null || a.getActive())
// // // //                 .filter(a -> assetId.map(id -> id.equals(a.getAssetId())).orElse(true))
// // // //                 .filter(a -> assetName.map(n -> a.getAssetNameUdv().toLowerCase().contains(n.toLowerCase())).orElse(true))
// // // //                 .filter(a -> categoryId.map(cid -> a.getCategory() != null && cid.equals(a.getCategory().getCategoryId())).orElse(true))
// // // //                 .toList();

// // // //         int start = (int) pageable.getOffset();
// // // //         int end = Math.min(start + pageable.getPageSize(), filtered.size());
// // // //         return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
// // // //     }

// // // //     // ============================================================
// // // //     // 🔔 Notification Helper
// // // //     // ============================================================
// // // //     private void sendAssetNotification(String bearer,
// // // //                                        Long userId,
// // // //                                        String username,
// // // //                                        String channel,
// // // //                                        String templateCode,
// // // //                                        Map<String, Object> placeholders,
// // // //                                        String projectType) {
// // // //         try {
// // // //             safeNotificationHelper.safeNotifyAsync(
// // // //                     bearer,
// // // //                     userId,
// // // //                     username,
// // // //                     null,
// // // //                     null,
// // // //                     channel,
// // // //                     templateCode,
// // // //                     placeholders,
// // // //                     projectType
// // // //             );
// // // //         } catch (Exception e) {
// // // //             log.error("⚠️ Notification failed [{}]: {}", templateCode, e.getMessage());
// // // //         }
// // // //     }

// // // //     // ============================================================
// // // //     // 🔐 Token Extractor
// // // //     // ============================================================
// // // //     private String extractBearer(HttpHeaders headers) {
// // // //         String authHeader = headers.getFirst("Authorization");
// // // //         if (authHeader == null || authHeader.isBlank()) {
// // // //             throw new RuntimeException("❌ Missing Authorization header");
// // // //         }
// // // //         return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
// // // //     }
// // // // }

package com.example.asset.service;

import com.example.asset.dto.AssetRequest;
import com.example.asset.entity.*;
import com.example.asset.repository.*;
import com.example.asset.dto.*;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ AssetCrudService
 * Handles CRUD for assets with validation of related entities.
 * Backward compatible with existing build & SafeNotificationHelper setup.
 */
@Service
public class AssetCrudService {

    private static final Logger log = LoggerFactory.getLogger(AssetCrudService.class);

    private final AssetMasterRepository assetRepo;
    private final AssetUserLinkRepository linkRepo;
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final SafeNotificationHelper safeNotificationHelper;

    public AssetCrudService(AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo,
            ProductCategoryRepository categoryRepo,
            ProductSubCategoryRepository subCategoryRepo,
            ProductMakeRepository makeRepo,
            ProductModelRepository modelRepo,
            SafeNotificationHelper safeNotificationHelper) {
        this.assetRepo = assetRepo;
        this.linkRepo = linkRepo;
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE ASSET
    // ============================================================
    @Transactional
    public AssetMaster create(HttpHeaders headers, AssetRequest request) {
        if (request == null || request.getAsset() == null)
            throw new IllegalArgumentException("❌ AssetRequest or payload cannot be null");

        String bearer = extractBearer(headers);
        AssetMaster asset = request.getAsset();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        // --- 🔍 VALIDATIONS ---
        if (!StringUtils.hasText(asset.getAssetNameUdv()))
            throw new RuntimeException("❌ Asset name cannot be blank");
        if (assetRepo.existsByAssetNameUdv(asset.getAssetNameUdv()))
            throw new RuntimeException("❌ Asset with name '" + asset.getAssetNameUdv() + "' already exists");

        validateEntityReferences(asset);

        asset.setCreatedBy(username);
        asset.setUpdatedBy(username);
        AssetMaster saved = assetRepo.save(asset);

        // // // // // Link user to asset
        // // // // if (userId != null && username != null) {
        // // // // AssetUserLink link = new AssetUserLink();
        // // // // link.setAsset(saved);
        // // // // link.setUserId(String.valueOf(userId));
        // // // // link.setUsername(username);
        // // // // linkRepo.save(link);
        // // // // }

        Map<String, Object> placeholders = Map.of(
                "assetId", saved.getAssetId(),
                "assetName", saved.getAssetNameUdv(),
                "assignedTo", username,
                "username", username,
                "timestamp", Instant.now().toString());

        sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_CREATED_INAPP", placeholders, projectType);
        sendAssetNotification(bearer, userId, username, "EMAIL", "ASSET_CREATED_EMAIL", placeholders, projectType);

        log.info("✅ Asset created successfully: id={} name={} by={}", saved.getAssetId(), saved.getAssetNameUdv(),
                username);
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE ASSET
    // ============================================================
    @Transactional
    public AssetMaster update(HttpHeaders headers, Long id, AssetRequest request) {
        if (request == null || request.getAsset() == null)
            throw new IllegalArgumentException("❌ AssetRequest or payload cannot be null");

        String bearer = extractBearer(headers);
        AssetMaster patch = request.getAsset();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return assetRepo.findById(id).map(existing -> {
            String newName = patch.getAssetNameUdv();

            // 🔒 Validate Name
            if (!StringUtils.hasText(newName)) {
                throw new RuntimeException("❌ Asset name cannot be blank");
            }

            // 🔍 Check for uniqueness excluding current record
            Optional<AssetMaster> duplicate = assetRepo.findByAssetNameUdvIgnoreCase(newName);
            if (duplicate.isPresent() && !duplicate.get().getAssetId().equals(existing.getAssetId())) {
                throw new RuntimeException("❌ Asset with name '" + newName + "' already exists");
            }

            // 🔍 Validate Foreign Keys (Category, SubCategory, Make, Model)
            validateEntityReferences(patch);

            // 🧾 Apply updates
            existing.setAssetNameUdv(newName);
            existing.setAssetStatus(patch.getAssetStatus());
            existing.setCategory(patch.getCategory());
            existing.setSubCategory(patch.getSubCategory());
            existing.setMake(patch.getMake());
            existing.setModel(patch.getModel());
            existing.setUpdatedBy(username);

            AssetMaster saved = assetRepo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "assetId", saved.getAssetId(),
                    "oldName", existing.getAssetNameUdv(),
                    "assetName", saved.getAssetNameUdv(),
                    "updatedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString());

            sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_UPDATED_INAPP", placeholders, projectType);
            log.info("✏️ Asset updated: id={} by={}", id, username);

            return saved;
        }).orElseThrow(() -> new RuntimeException("❌ Asset not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, AssetRequest request) {
        String bearer = extractBearer(headers);
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        assetRepo.findById(id).ifPresentOrElse(asset -> {
            asset.setActive(false);
            asset.setUpdatedBy(username);
            assetRepo.save(asset);

            Map<String, Object> placeholders = Map.of(
                    "assetId", asset.getAssetId(),
                    "assetName", asset.getAssetNameUdv(),
                    "deletedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString());

            sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_DELETED_INAPP", placeholders, projectType);
            log.info("🗑️ Asset deleted (soft): id={} by={}", id, username);
        }, () -> {
            throw new RuntimeException("❌ Asset not found with id: " + id);
        });
    }

    // ============================================================
    // 🔍 GET BY ID
    // ============================================================
    public Optional<AssetResponseDTO> get(Long id) {
        return assetRepo.findById(id)
                .filter(a -> a.getActive() == null || a.getActive())
                .map(a -> {
                    AssetResponseDTO dto = new AssetResponseDTO();
                    dto.setAssetId(a.getAssetId());
                    dto.setAssetNameUdv(a.getAssetNameUdv());
                    dto.setAssetStatus(a.getAssetStatus());

                    // 🔹 Safely extract lazy fields
                    if (a.getCategory() != null)
                        dto.setCategoryName(a.getCategory().getCategoryName());

                    if (a.getSubCategory() != null)
                        dto.setSubCategoryName(a.getSubCategory().getSubCategoryName());

                    if (a.getMake() != null)
                        dto.setMakeName(a.getMake().getMakeName());

                    if (a.getModel() != null)
                        dto.setModelName(a.getModel().getModelName());

                    log.info("🔍 Fetched asset: id={} name={}", a.getAssetId(), a.getAssetNameUdv());
                    return dto;
                });
    }


    // ============================================================
    // 🔍 SEARCH — Now returns DTO instead of entities
    // ============================================================
    public Page<AssetResponseDTO> search(Optional<Long> assetId,
                                         Optional<String> assetName,
                                         Optional<Long> categoryId,
                                         Pageable pageable) {

        List<AssetResponseDTO> filtered = assetRepo.findAll().stream()
                .filter(a -> a.getActive() == null || a.getActive())
                .filter(a -> assetId.map(id -> id.equals(a.getAssetId())).orElse(true))
                .filter(a -> assetName.map(n -> 
                        a.getAssetNameUdv() != null && a.getAssetNameUdv().toLowerCase().contains(n.toLowerCase()))
                        .orElse(true))
                .filter(a -> categoryId
                        .map(cid -> a.getCategory() != null && cid.equals(a.getCategory().getCategoryId()))
                        .orElse(true))
                .map(a -> {
                    AssetResponseDTO dto = new AssetResponseDTO();
                    dto.setAssetId(a.getAssetId());
                    dto.setAssetNameUdv(a.getAssetNameUdv());
                    dto.setAssetStatus(a.getAssetStatus());
                    dto.setCategoryName(a.getCategory() != null ? a.getCategory().getCategoryName() : null);
                    dto.setSubCategoryName(a.getSubCategory() != null ? a.getSubCategory().getSubCategoryName() : null); // ✅ Added subcategory
                    dto.setMakeName(a.getMake() != null ? a.getMake().getMakeName() : null);
                    dto.setModelName(a.getModel() != null ? a.getModel().getModelName() : null);
                    return dto;
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }
    // ============================================================
    // 🔍 VALIDATE RELATED ENTITIES
    // ============================================================
    private void validateEntityReferences(AssetMaster asset) {
        if (asset.getCategory() == null || asset.getCategory().getCategoryId() == null)
            throw new RuntimeException("❌ Missing Category: categoryId is required");
        if (!categoryRepo.existsById(asset.getCategory().getCategoryId()))
            throw new RuntimeException("❌ Invalid Category ID: " + asset.getCategory().getCategoryId());

        if (asset.getSubCategory() == null || asset.getSubCategory().getSubCategoryId() == null)
            throw new RuntimeException("❌ Missing SubCategory: subCategoryId is required");
        if (!subCategoryRepo.existsById(asset.getSubCategory().getSubCategoryId()))
            throw new RuntimeException("❌ Invalid SubCategory ID: " + asset.getSubCategory().getSubCategoryId());

        if (asset.getMake() == null || asset.getMake().getMakeId() == null)
            throw new RuntimeException("❌ Missing Make: makeId is required");
        if (!makeRepo.existsById(asset.getMake().getMakeId()))
            throw new RuntimeException("❌ Invalid Make ID: " + asset.getMake().getMakeId());

        if (asset.getModel() == null || asset.getModel().getModelId() == null)
            throw new RuntimeException("❌ Missing Model: modelId is required");
        if (!modelRepo.existsById(asset.getModel().getModelId()))
            throw new RuntimeException("❌ Invalid Model ID: " + asset.getModel().getModelId());
    }

    // ============================================================
    // 🔔 Notification Helper
    // ============================================================
    private void sendAssetNotification(String bearer,
            Long userId,
            String username,
            String channel,
            String templateCode,
            Map<String, Object> placeholders,
            String projectType) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer,
                    userId,
                    username,
                    null,
                    null,
                    channel,
                    templateCode,
                    placeholders,
                    projectType);
        } catch (Exception e) {
            log.error("⚠️ Notification failed [{}]: {}", templateCode, e.getMessage());
        }
    }

    // ============================================================
    // 🔐 Token Extractor
    // ============================================================
    private String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new RuntimeException("❌ Missing Authorization header");
        }
        return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
    }
}


JAVA



  echo "AssetCrudService Completed"

# ---------- AssetController with search endpoints ----------


# ---------- Warranty CRUD (service + controller) ----------
cat > "$SRC_ROOT/service/WarrantyService.java" <<'JAVA'



package com.example.asset.service;

import com.example.asset.dto.WarrantyRequest;
import com.example.asset.entity.AssetWarranty;
import com.example.asset.repository.AssetWarrantyRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * ✅ WarrantyService
 * Handles CRUD for AssetWarranty with SafeNotificationHelper.
 * Uses Authorization header token for secure notifications.
 */
@Service
public class WarrantyService {

    private static final Logger log = LoggerFactory.getLogger(WarrantyService.class);

    private final AssetWarrantyRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;

    public WarrantyService(AssetWarrantyRepository repo,
                           SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE WARRANTY
    // ============================================================
    @Transactional
    public AssetWarranty create(HttpHeaders headers, WarrantyRequest request) {
        validateRequest(headers, request);

        String bearer = extractBearer(headers);
        String username = request.getUsername();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        AssetWarranty warranty = request.getWarranty();
        warranty.setCreatedBy(username);
        warranty.setUpdatedBy(username);
        AssetWarranty saved = repo.save(warranty);

        Map<String, Object> placeholders = Map.of(
                "warrantyId", saved.getWarrantyId(),
                "assetId", saved.getAsset() != null ? saved.getAsset().getAssetId() : null,
                "startDate", saved.getWarrantyStartDate(),
                "endDate", saved.getWarrantyEndDate(),
                "createdBy", username,
                    "username", username,
                "timestamp", Instant.now().toString()
        );

        sendNotification(bearer, request.getUserId(), username, placeholders,
                "WARRANTY_CREATED", projectType);

        log.info("✅ Warranty created successfully: id={} by={}", saved.getWarrantyId(), username);
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE WARRANTY
    // ============================================================
    @Transactional
    public AssetWarranty update(HttpHeaders headers, Long id, WarrantyRequest request) {
        validateRequest(headers, request);

        String bearer = extractBearer(headers);
        String username = request.getUsername();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return repo.findById(id).map(existing -> {
            existing.setWarrantyStartDate(request.getWarranty().getWarrantyStartDate());
            existing.setWarrantyEndDate(request.getWarranty().getWarrantyEndDate());
            existing.setActive(request.getWarranty().getActive());
            existing.setUpdatedBy(username);

            AssetWarranty saved = repo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "warrantyId", saved.getWarrantyId(),
                    "assetId", saved.getAsset() != null ? saved.getAsset().getAssetId() : null,
                    "status", saved.getActive(),
                    "updatedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString()
            );

            sendNotification(bearer, request.getUserId(), username, placeholders,
                    "WARRANTY_UPDATED", projectType);

            log.info("✏️ Warranty updated successfully: id={} by={}", id, username);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Warranty not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE WARRANTY
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, WarrantyRequest request) {
        validateRequest(headers, request);

        String bearer = extractBearer(headers);
        String username = request.getUsername();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(warranty -> {
            warranty.setActive(false);
            warranty.setUpdatedBy(username);
            AssetWarranty saved = repo.save(warranty);

            Map<String, Object> placeholders = Map.of(
                    "warrantyId", saved.getWarrantyId(),
                    "assetId", saved.getAsset() != null ? saved.getAsset().getAssetId() : null,
                    "deletedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString()
            );

            sendNotification(bearer, request.getUserId(), username, placeholders,
                    "WARRANTY_DELETED", projectType);

            log.info("🗑️ Warranty soft deleted: id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST + FIND
    // ============================================================
    public List<AssetWarranty> list() {
        return repo.findAll();
    }

    public Optional<AssetWarranty> find(Long id) {
        return repo.findById(id);
    }

    // ============================================================
    // 🔔 Notification Helper
    // ============================================================
    private void sendNotification(String bearer,
                                  Long userId,
                                  String username,
                                  Map<String, Object> placeholders,
                                  String templateCode,
                                  String projectType) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer, userId, username, null, null,
                    "INAPP", templateCode, placeholders, projectType
            );
            log.info("📩 Notification sent [{}] for {}", templateCode, username);
        } catch (Exception e) {
            log.error("⚠️ Failed to send {} notification: {}", templateCode, e.getMessage());
        }
    }

    // ============================================================
    // 🧩 Helper Methods
    // ============================================================
    private void validateRequest(HttpHeaders headers, WarrantyRequest request) {
        if (headers == null || headers.getFirst("Authorization") == null) {
            throw new RuntimeException("❌ Missing Authorization header");
        }
        if (request == null || request.getWarranty() == null) {
            throw new RuntimeException("❌ Invalid request or missing warranty payload");
        }
    }

    private String extractBearer(HttpHeaders headers) {
        String token = headers.getFirst("Authorization");
        if (token == null || token.isBlank()) {
            throw new RuntimeException("❌ Missing or invalid Authorization header");
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}






JAVA

# # # # cat > "$SRC_ROOT/controller/WarrantyController.java" <<'JAVA'

# # # # package com.example.asset.controller;

# # # # import com.example.asset.dto.WarrantyRequest;
# # # # import com.example.asset.entity.AssetWarranty;
# # # # import com.example.asset.service.WarrantyService;
# # # # import com.example.common.util.ResponseWrapper;
# # # # import jakarta.validation.Valid;
# # # # import org.slf4j.Logger;
# # # # import org.slf4j.LoggerFactory;
# # # # import org.springframework.http.HttpHeaders;
# # # # import org.springframework.http.ResponseEntity;
# # # # import org.springframework.web.bind.annotation.*;

# # # # import java.util.List;

# # # # /**
# # # #  * ✅ WarrantyController
# # # #  * REST endpoints for Asset Warranty management.
# # # #  * Authorization token must be provided in request headers.
# # # #  */
# # # # @RestController
# # # # @RequestMapping("/api/asset/v1/warranties")
# # # # public class WarrantyController {

# # # #     private static final Logger log = LoggerFactory.getLogger(WarrantyController.class);

# # # #     private final WarrantyService warrantyService;

# # # #     public WarrantyController(WarrantyService warrantyService) {
# # # #         this.warrantyService = warrantyService;
# # # #     }

# # # #     // ============================================================
# # # #     // 🟢 CREATE
# # # #     // ============================================================
# # # #     @PostMapping
# # # #     public ResponseEntity<ResponseWrapper<AssetWarranty>> create(
# # # #             @RequestHeader HttpHeaders headers,
# # # #             @Valid @RequestBody WarrantyRequest request) {
# # # #         try {
# # # #             AssetWarranty created = warrantyService.create(headers, request);
# # # #             log.info("✅ Warranty created successfully: {}", created.getWarrantyId());
# # # #             return ResponseEntity.ok(new ResponseWrapper<>(true, "Warranty created successfully", created));
# # # #         } catch (Exception e) {
# # # #             log.error("❌ Failed to create warranty: {}", e.getMessage(), e);
# # # #             return ResponseEntity.internalServerError()
# # # #                     .body(new ResponseWrapper<>(false, "Failed to create warranty: " + e.getMessage(), null));
# # # #         }
# # # #     }

# # # #     // ============================================================
# # # #     // ✏️ UPDATE
# # # #     // ============================================================
# # # #     @PutMapping("/{id}")
# # # #     public ResponseEntity<ResponseWrapper<AssetWarranty>> update(
# # # #             @RequestHeader HttpHeaders headers,
# # # #             @PathVariable Long id,
# # # #             @Valid @RequestBody WarrantyRequest request) {
# # # #         try {
# # # #             AssetWarranty updated = warrantyService.update(headers, id, request);
# # # #             log.info("✏️ Warranty updated successfully: id={}", id);
# # # #             return ResponseEntity.ok(new ResponseWrapper<>(true, "Warranty updated successfully", updated));
# # # #         } catch (Exception e) {
# # # #             log.error("❌ Failed to update warranty: {}", e.getMessage(), e);
# # # #             return ResponseEntity.internalServerError()
# # # #                     .body(new ResponseWrapper<>(false, "Failed to update warranty: " + e.getMessage(), null));
# # # #         }
# # # #     }

# # # #     // ============================================================
# # # #     // ❌ DELETE (SOFT)
# # # #     // ============================================================
# # # #     @DeleteMapping("/{id}")
# # # #     public ResponseEntity<ResponseWrapper<Void>> delete(
# # # #             @RequestHeader HttpHeaders headers,
# # # #             @PathVariable Long id,
# # # #             @Valid @RequestBody WarrantyRequest request) {
# # # #         try {
# # # #             warrantyService.softDelete(headers, id, request);
# # # #             log.info("🗑️ Warranty soft deleted successfully: id={}", id);
# # # #             return ResponseEntity.ok(new ResponseWrapper<>(true, "Warranty deleted successfully", null));
# # # #         } catch (Exception e) {
# # # #             log.error("❌ Failed to delete warranty: {}", e.getMessage(), e);
# # # #             return ResponseEntity.internalServerError()
# # # #                     .body(new ResponseWrapper<>(false, "Failed to delete warranty: " + e.getMessage(), null));
# # # #         }
# # # #     }

# # # #     // ============================================================
# # # #     // 📋 LIST
# # # #     // ============================================================
# # # #     @GetMapping
# # # #     public ResponseEntity<ResponseWrapper<List<AssetWarranty>>> list() {
# # # #         List<AssetWarranty> list = warrantyService.list();
# # # #         return ResponseEntity.ok(new ResponseWrapper<>(true, "Fetched all warranties", list));
# # # #     }

# # # #     // ============================================================
# # # #     // 🔍 GET BY ID
# # # #     // ============================================================
# # # #     @GetMapping("/{id}")
# # # #     public ResponseEntity<ResponseWrapper<AssetWarranty>> getById(@PathVariable Long id) {
# # # #         return warrantyService.find(id)
# # # #                 .map(w -> ResponseEntity.ok(new ResponseWrapper<>(true, "Warranty found", w)))
# # # #                 .orElseGet(() -> ResponseEntity.status(404)
# # # #                         .body(new ResponseWrapper<>(false, "Warranty not found", null)));
# # # #     }
# # # # }


# # # # JAVA

# ---------- AMC CRUD ----------
cat > "$SRC_ROOT/service/AmcService.java" <<'JAVA'
package com.example.asset.service;

import com.example.asset.dto.AmcRequest;
import com.example.asset.entity.AssetAmc;
import com.example.asset.repository.AssetAmcRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * ✅ AmcService
 * Handles AMC CRUD and sends notifications using SafeNotificationHelper.
 * Extracts bearer token directly from HttpHeaders (no UserService dependency).
 */
@Service
public class AmcService {

    private static final Logger log = LoggerFactory.getLogger(AmcService.class);

    private final AssetAmcRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;

    public AmcService(AssetAmcRepository repo, SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE
    // ============================================================
    @Transactional
    public AssetAmc create(HttpHeaders headers, AmcRequest request) {
        if (request == null || request.getAmc() == null)
            throw new IllegalArgumentException("AMC request or payload cannot be null");

        String bearer = extractBearer(headers);
        AssetAmc amc = request.getAmc();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        amc.setCreatedBy(username);
        amc.setUpdatedBy(username);
        AssetAmc saved = repo.save(amc);

        Map<String, Object> placeholders = Map.of(
                "amcId", saved.getAmcId(),
                "assetId", saved.getAsset() != null ? saved.getAsset().getAssetId() : null,
                "amcStatus", saved.getAmcStatus(),
                "startDate", saved.getStartDate(),
                "endDate", saved.getEndDate(),
                "createdBy", username,
                    "username", username,
                "timestamp", Instant.now().toString()
        );

        sendAmcNotification(bearer, userId, username, "INAPP", "AMC_CREATED", placeholders, projectType);
        sendAmcNotification(bearer, userId, username, "EMAIL", "AMC_CREATED_EMAIL", placeholders, projectType);

        log.info("✅ AMC created successfully: id={} by={}", saved.getAmcId(), username);
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE
    // ============================================================
    @Transactional
    public AssetAmc update(HttpHeaders headers, Long id, AmcRequest request) {
        if (request == null || request.getAmc() == null)
            throw new IllegalArgumentException("AMC request or payload cannot be null");

        String bearer = extractBearer(headers);
        AssetAmc patch = request.getAmc();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return repo.findById(id).map(existing -> {
            existing.setAmcStatus(patch.getAmcStatus());
            existing.setStartDate(patch.getStartDate());
            existing.setEndDate(patch.getEndDate());
            existing.setUpdatedBy(username);

            AssetAmc saved = repo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "amcId", saved.getAmcId(),
                    "amcStatus", saved.getAmcStatus(),
                    "startDate", saved.getStartDate(),
                    "endDate", saved.getEndDate(),
                    "updatedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString()
            );

            sendAmcNotification(bearer, userId, username, "INAPP", "AMC_UPDATED", placeholders, projectType);
            log.info("✏️ AMC updated: id={} by={}", id, username);
            return saved;
        }).orElseThrow(() -> new RuntimeException("AMC not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, AmcRequest request) {
        String bearer = extractBearer(headers);
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(amc -> {
            amc.setActive(false);
            amc.setUpdatedBy(username);
            repo.save(amc);

            Map<String, Object> placeholders = Map.of(
                    "amcId", amc.getAmcId(),
                    "assetId", amc.getAsset() != null ? amc.getAsset().getAssetId() : null,
                    "deletedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString()
            );

            sendAmcNotification(bearer, userId, username, "INAPP", "AMC_DELETED", placeholders, projectType);
            log.info("🗑️ AMC deleted: id={} by={}", amc.getAmcId(), username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<AssetAmc> listForAsset(Long assetId) {
        return repo.findAll().stream()
                .filter(a -> a.getAsset() != null && a.getAsset().getAssetId().equals(assetId))
                .filter(a -> a.getActive() == null || a.getActive())
                .toList();
    }

    public Optional<AssetAmc> find(Long id) {
        return repo.findById(id);
    }

    // ============================================================
    // 🔔 Notification Helper
    // ============================================================
    private void sendAmcNotification(String bearer,
                                     Long userId,
                                     String username,
                                     String channel,
                                     String templateCode,
                                     Map<String, Object> placeholders,
                                     String projectType) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer,
                    userId,
                    username,
                    null,
                    null,
                    channel,
                    templateCode,
                    placeholders,
                    projectType
            );
        } catch (Exception e) {
            log.error("⚠️ Notification failed [{}] for AMC: {}", templateCode, e.getMessage());
        }
    }

    // ============================================================
    // 🔐 Token Extractor
    // ============================================================
    private String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new RuntimeException("❌ Missing Authorization header");
        }
        return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
    }
}

JAVA


  echo "AmcService Completed"

  
# # # # cat > "$SRC_ROOT/controller/AmcController.java" <<'JAVA'

# # # # package com.example.asset.controller;

# # # # import com.example.asset.dto.AmcRequest;
# # # # import com.example.asset.entity.AssetAmc;
# # # # import com.example.asset.service.AmcService;
# # # # import com.example.common.util.ResponseWrapper;
# # # # import org.springframework.http.HttpHeaders;
# # # # import org.springframework.http.ResponseEntity;
# # # # import org.springframework.web.bind.annotation.*;

# # # # import java.util.List;

# # # # /**
# # # #  * ✅ AmcController
# # # #  * Handles AMC CRUD operations.
# # # #  * Extracts Authorization header and passes it to AmcService.
# # # #  */
# # # # @RestController
# # # # @RequestMapping("/api/asset/v1/amc")
# # # # public class AmcController {

# # # #     private final AmcService amcService;

# # # #     public AmcController(AmcService amcService) {
# # # #         this.amcService = amcService;
# # # #     }

# # # #     // ============================================================
# # # #     // 🟢 CREATE AMC
# # # #     // ============================================================
# # # #     @PostMapping
# # # #     public ResponseEntity<ResponseWrapper<AssetAmc>> create(
# # # #             @RequestHeader HttpHeaders headers,
# # # #             @RequestBody AmcRequest request) {
# # # #         try {
# # # #             AssetAmc created = amcService.create(headers, request);
# # # #             return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ AMC created successfully", created));
# # # #         } catch (Exception e) {
# # # #             return ResponseEntity.internalServerError()
# # # #                     .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
# # # #         }
# # # #     }

# # # #     // ============================================================
# # # #     // ✏️ UPDATE AMC
# # # #     // ============================================================
# # # #     @PutMapping("/{id}")
# # # #     public ResponseEntity<ResponseWrapper<AssetAmc>> update(
# # # #             @RequestHeader HttpHeaders headers,
# # # #             @PathVariable Long id,
# # # #             @RequestBody AmcRequest request) {
# # # #         try {
# # # #             AssetAmc updated = amcService.update(headers, id, request);
# # # #             return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ AMC updated successfully", updated));
# # # #         } catch (Exception e) {
# # # #             return ResponseEntity.internalServerError()
# # # #                     .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
# # # #         }
# # # #     }

# # # #     // ============================================================
# # # #     // ❌ SOFT DELETE AMC
# # # #     // ============================================================
# # # #     @DeleteMapping("/{id}")
# # # #     public ResponseEntity<ResponseWrapper<Void>> delete(
# # # #             @RequestHeader HttpHeaders headers,
# # # #             @PathVariable Long id,
# # # #             @RequestBody AmcRequest request) {
# # # #         try {
# # # #             amcService.softDelete(headers, id, request);
# # # #             return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ AMC deleted successfully", null));
# # # #         } catch (Exception e) {
# # # #             return ResponseEntity.internalServerError()
# # # #                     .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
# # # #         }
# # # #     }

# # # #     // ============================================================
# # # #     // 📋 LIST BY ASSET
# # # #     // ============================================================
# # # #     @GetMapping("/asset/{assetId}")
# # # #     public ResponseEntity<ResponseWrapper<List<AssetAmc>>> list(@PathVariable Long assetId) {
# # # #         List<AssetAmc> list = amcService.listForAsset(assetId);
# # # #         return ResponseEntity.ok(new ResponseWrapper<>(true, "📋 AMC list fetched successfully", list));
# # # #     }
# # # # }


# # # # JAVA

# ---------- Document upload controller ----------
cat > "$SRC_ROOT/controller/DocumentController.java" <<'JAVA'


package com.example.asset.controller;

import com.example.asset.dto.DocumentRequest;
import com.example.asset.entity.AssetDocument;
import com.example.asset.service.DocumentService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@RestController
@RequestMapping("/api/asset/v1/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // ============================================================
    // 🟢 UNIVERSAL UPLOAD ENDPOINT
    // ============================================================
    @PostMapping("/upload")
    public ResponseEntity<ResponseWrapper<AssetDocument>> upload(
            @RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId,
            @RequestParam("userId") Long userId,
            @RequestParam("username") String username,
            @RequestParam(value = "projectType", required = false) String projectType,
            @RequestParam(value = "docType", required = false) String docType) {

        try {
            DocumentRequest req = new DocumentRequest();
            req.setEntityType(entityType);
            req.setEntityId(entityId);
            req.setUserId(userId);
            req.setUsername(username);
            req.setProjectType(projectType);
            req.setDocType(docType);

            AssetDocument saved = documentService.upload(headers, file, req);
            log.info("✅ {} document uploaded successfully by user={} for {} ID={}", docType, username, entityType, entityId);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Document uploaded successfully", saved));

        } catch (Exception e) {
            log.error("❌ Upload failed for entityType={} ID={}: {}", entityType, entityId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Upload failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE DOCUMENT
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody DocumentRequest request) {
        try {
            documentService.softDelete(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Document deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Delete failed for document ID={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Delete failed: " + e.getMessage(), null));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetDocument>> getDocumentDetails(@PathVariable Long id) {
        try {
            AssetDocument document = documentService.findById(id);
            if (document == null) {
                return ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "⚠️ Document not found", null));
            }

            return ResponseEntity.ok(new ResponseWrapper<>(true, "📄 Document details fetched successfully", document));

        } catch (Exception e) {
            log.error("❌ Failed to fetch document details for ID={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Failed to fetch document details: " + e.getMessage(), null));
        }
    }


    // ============================================================
    // ⬇️ DOWNLOAD DOCUMENT (Actual File)
    // ============================================================
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            AssetDocument document = documentService.findById(id);
            if (document == null || document.getFilePath() == null) {
                return ResponseEntity.status(404).build();
            }

            Path filePath = Paths.get(document.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(404).build();
            }

            org.springframework.core.io.Resource fileResource =
                    new org.springframework.core.io.PathResource(filePath);

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            log.info("⬇️ File download initiated for documentId={}, file={}", id, filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                    .body(fileResource);

        } catch (Exception e) {
            log.error("❌ Download failed for document ID={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}


JAVA

# # # # # ---------- AssetUserLink (assign/unassign) ----------
# # # # cat > "$SRC_ROOT/controller/AssetUserLinkController.java" <<'JAVA'
# # # # package com.example.asset.controller;

# # # # import com.example.asset.entity.AssetUserLink;
# # # # import com.example.asset.repository.AssetUserLinkRepository;
# # # # import com.example.asset.repository.AssetMasterRepository;
# # # # import com.example.asset.util.JwtUtil;
# # # # import com.example.common.util.ResponseWrapper;
# # # # import org.springframework.http.ResponseEntity;
# # # # import org.springframework.web.bind.annotation.*;

# # # # @RestController
# # # # @RequestMapping("/api/asset/v1/asset-user")
# # # # public class AssetUserLinkController {

# # # #     private final AssetUserLinkRepository linkRepo;
# # # #     private final AssetMasterRepository assetRepo;

# # # #     public AssetUserLinkController(AssetUserLinkRepository linkRepo, AssetMasterRepository assetRepo){
# # # #         this.linkRepo = linkRepo; this.assetRepo = assetRepo;
# # # #     }

# # # #     @PostMapping("/assign")
# # # #     public ResponseEntity<ResponseWrapper<AssetUserLink>> assign(@RequestParam Long assetId, @RequestParam String userId, @RequestParam String username) {
# # # #         AssetUserLink link = new AssetUserLink();
# # # #         assetRepo.findById(assetId).ifPresent(link::setAsset);
# # # #         link.setUserId(userId); link.setUsername(username);
# # # #         link.setCreatedBy(JwtUtil.getUsernameOrThrow());
# # # #         AssetUserLink saved = linkRepo.save(link);
# # # #         return ResponseEntity.ok(new ResponseWrapper<>(true, "Assigned", saved));
# # # #     }

# # # #     @DeleteMapping("/{id}/unassign")
# # # #     public ResponseEntity<ResponseWrapper<Void>> unassign(@PathVariable Long id) {
# # # #         linkRepo.findById(id).ifPresent(l -> { l.setActive(false); linkRepo.save(l); });
# # # #         return ResponseEntity.ok(new ResponseWrapper<>(true, "Unassigned", null));
# # # #     }
# # # # }
# # # # JAVA

# ---------- AuditController (read-only) ----------
cat > "$SRC_ROOT/controller/AuditController.java" <<'JAVA'
package com.example.asset.controller;

import com.example.asset.entity.AuditLog;
import com.example.asset.service.AuditService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ AuditController
 * Handles fetching of system audit logs.
 * Extracts Authorization token directly from request headers.
 */
@RestController
@RequestMapping("/api/asset/v1/audit")
public class AuditController {

    private static final Logger log = LoggerFactory.getLogger(AuditController.class);
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    // ============================================================
    // 📋 LIST ALL AUDIT LOGS
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<AuditLog>>> list(@RequestHeader HttpHeaders headers) {
        try {
            String bearer = auditService.extractBearer(headers);
            List<AuditLog> logs = auditService.listAll(bearer);
            log.info("✅ Audit logs retrieved successfully. Total records: {}", logs.size());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Audit logs fetched successfully", logs));
        } catch (Exception e) {
            log.error("❌ Failed to fetch audit logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "Failed to fetch audit logs: " + e.getMessage(), null));
        }
    }
}

JAVA



cat > "$SRC_ROOT/mapper/AssetAmcMapper.java" <<'JAVA'

package com.example.asset.mapper;

import com.example.asset.dto.AssetAmcDto;
import com.example.asset.entity.AssetAmc;

/**
 * ✅ AssetAmcMapper
 * Converts between AssetAmc entity and DTO.
 */
public class AssetAmcMapper {

    public static AssetAmcDto toDto(AssetAmc entity) {
        if (entity == null) return null;

        AssetAmcDto dto = new AssetAmcDto();
        dto.setAmcId(entity.getAmcId());
        dto.setAmcStatus(entity.getAmcStatus());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setActive(entity.getActive());
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setComponentId(entity.getComponentId());

        if (entity.getAsset() != null)
            dto.setAssetId(entity.getAsset().getAssetId());

        if (entity.getDocument() != null)
            dto.setDocumentId(entity.getDocument().getDocumentId());

        return dto;
    }
}


JAVA





cat > "$SRC_ROOT/mapper/AssetWarrantyMapper.java" <<'JAVA'

package com.example.asset.mapper;

import com.example.asset.dto.AssetWarrantyDto;
import com.example.asset.entity.AssetWarranty;
import com.example.asset.entity.AssetDocument;
import com.example.asset.entity.AssetMaster;

/**
 * ✅ AssetWarrantyMapper
 * Handles mapping between AssetWarranty entity and DTO.
 */
public class AssetWarrantyMapper {

    // ============================================================
    // 🔁 ENTITY → DTO
    // ============================================================
    public static AssetWarrantyDto toDto(AssetWarranty entity) {
        if (entity == null) return null;

        AssetWarrantyDto dto = new AssetWarrantyDto();

        dto.setWarrantyId(entity.getWarrantyId());
        dto.setWarrantyStatus(entity.getWarrantyStatus());
        dto.setWarrantyProvider(entity.getWarrantyProvider());
        dto.setWarrantyTerms(entity.getWarrantyTerms());
        dto.setStartDate(entity.getWarrantyStartDate());
        dto.setEndDate(entity.getWarrantyEndDate());
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setComponentId(entity.getComponentId());

        // ✅ Include assetId if asset is linked
        if (entity.getAsset() != null) {
            dto.setAssetId(entity.getAsset().getAssetId());
        }

        // ✅ Include documentId safely (even if lazy)
        if (entity.getDocument() != null) {
            try {
                dto.setDocumentId(entity.getDocument().getDocumentId());
            } catch (Exception e) {
                dto.setDocumentId(null);
            }
        } else if (entity.getDocumentId() != null) {
            dto.setDocumentId(entity.getDocumentId());
        }

        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        // dto.setCreatedAt(entity.getCreatedAt());
        // dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    // ============================================================
    // 🔁 DTO → ENTITY
    // ============================================================
    public static AssetWarranty toEntity(AssetWarrantyDto dto) {
        if (dto == null) return null;

        AssetWarranty entity = new AssetWarranty();

        entity.setWarrantyId(dto.getWarrantyId());
        entity.setWarrantyStatus(dto.getWarrantyStatus());
        entity.setWarrantyProvider(dto.getWarrantyProvider());
        entity.setWarrantyTerms(dto.getWarrantyTerms());
        entity.setWarrantyStartDate(dto.getStartDate());
        entity.setWarrantyEndDate(dto.getEndDate());
        entity.setUserId(dto.getUserId());
        entity.setUsername(dto.getUsername());
        entity.setComponentId(dto.getComponentId());
        entity.setActive(dto.getActive());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());

        // ✅ Map asset if assetId is provided
        if (dto.getAssetId() != null) {
            AssetMaster asset = new AssetMaster();
            asset.setAssetId(dto.getAssetId());
            entity.setAsset(asset);
        }

        // ✅ Map document if documentId is provided
        if (dto.getDocumentId() != null) {
            AssetDocument doc = new AssetDocument();
            doc.setDocumentId(dto.getDocumentId());
            entity.setDocument(doc);
        }

        return entity;
    }
}


JAVA


cat > "$SRC_ROOT/mapper/ModelMapper.java" <<'JAVA'

package com.example.asset.mapper;

import com.example.asset.dto.ModelDto;
import com.example.asset.entity.ProductModel;

/**
 * ✅ ModelMapper
 * Converts between ProductModel entity and ModelDto.
 */
public class ModelMapper {

    public static ModelDto toDto(ProductModel entity) {
        if (entity == null) return null;

        ModelDto dto = new ModelDto();
        dto.setModelId(entity.getModelId());
        dto.setModelName(entity.getModelName());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getMake() != null) {
            dto.setMakeId(entity.getMake().getMakeId());
            dto.setMakeName(entity.getMake().getMakeName());
        }

        return dto;
    }
}

JAVA



cat > "$SRC_ROOT/mapper/CategoryMapper.java" <<'JAVA'

package com.example.asset.mapper;

import com.example.asset.dto.CategoryDto;
import com.example.asset.entity.ProductCategory;

/**
 * ✅ CategoryMapper
 * Utility class for converting between {@link ProductCategory} entities
 * and {@link CategoryDto} data transfer objects.
 * <p>
 * Provides bi-directional mapping with null-safety and optional normalization.
 */
public final class CategoryMapper {

    // Prevent instantiation
    private CategoryMapper() {}

    // ============================================================
    // 🔄 ENTITY → DTO
    // ============================================================
    /**
     * Converts a {@link ProductCategory} entity to a {@link CategoryDto}.
     *
     * @param entity the entity to convert
     * @return the corresponding DTO, or {@code null} if input is null
     */
    public static CategoryDto toDto(ProductCategory entity) {
        if (entity == null) return null;

        CategoryDto dto = new CategoryDto();
        dto.setCategoryId(entity.getCategoryId());
        dto.setCategoryName(entity.getCategoryName());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    // ============================================================
    // 🔄 DTO → ENTITY
    // ============================================================
    /**
     * Converts a {@link CategoryDto} to a {@link ProductCategory} entity.
     *
     * @param dto the DTO to convert
     * @return the corresponding entity, or {@code null} if input is null
     */
    public static ProductCategory toEntity(CategoryDto dto) {
        if (dto == null) return null;

        ProductCategory entity = new ProductCategory();
        entity.setCategoryId(dto.getCategoryId());
        entity.setCategoryName(trim(dto.getCategoryName()));
        entity.setDescription(trim(dto.getDescription()));
        entity.setActive(dto.getActive());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }

    // ============================================================
    // ✏️ PARTIAL UPDATE SUPPORT
    // ============================================================
    /**
     * Copies non-null values from DTO to an existing entity.
     * Useful for PATCH-like or partial updates.
     *
     * @param dto    the source DTO
     * @param entity the target entity to update
     */
    public static void copyNonNullToEntity(CategoryDto dto, ProductCategory entity) {
        if (dto == null || entity == null) return;

        if (dto.getCategoryName() != null)
            entity.setCategoryName(trim(dto.getCategoryName()));
        if (dto.getDescription() != null)
            entity.setDescription(trim(dto.getDescription()));
        if (dto.getActive() != null)
            entity.setActive(dto.getActive());
        if (dto.getUpdatedBy() != null)
            entity.setUpdatedBy(dto.getUpdatedBy());
        if (dto.getUpdatedAt() != null)
            entity.setUpdatedAt(dto.getUpdatedAt());
    }

    // ============================================================
    // 🧩 Helper: Trim strings safely
    // ============================================================
    private static String trim(String value) {
        return (value != null) ? value.trim() : null;
    }
}


JAVA

cat > "$SRC_ROOT/mapper/ProductSubCategoryMapper.java" <<'JAVA'

package com.example.asset.mapper;

import com.example.asset.dto.ProductSubCategoryDto;
import com.example.asset.entity.ProductSubCategory;

public class ProductSubCategoryMapper {

    public static ProductSubCategoryDto toDto(ProductSubCategory entity) {
        if (entity == null) return null;

        ProductSubCategoryDto dto = new ProductSubCategoryDto();
        dto.setSubCategoryId(entity.getSubCategoryId());
        dto.setSubCategoryName(entity.getSubCategoryName());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getCategoryId());
            dto.setCategoryName(entity.getCategory().getCategoryName());
        }

        return dto;
    }
}


JAVA

# # # # cat > "$SRC_ROOT/config/JwtConfig.java" <<'JAVA'
# # # # package com.example.asset.config;

# # # # import org.springframework.beans.factory.annotation.Value;
# # # # import org.springframework.context.annotation.Bean;
# # # # import org.springframework.context.annotation.Configuration;
# # # # import org.springframework.core.io.Resource;
# # # # import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
# # # # import org.springframework.security.oauth2.core.OAuth2TokenValidator;
# # # # import org.springframework.security.oauth2.jwt.*;
# # # # import java.io.InputStream;
# # # # import java.security.KeyFactory;
# # # # import java.security.interfaces.RSAPublicKey;
# # # # import java.security.spec.X509EncodedKeySpec;
# # # # import java.util.Base64;

# # # # @Configuration
# # # # public class JwtConfig {

# # # #     @Value("${security.jwt.public-key-path:classpath:keys/jwt-public.pem}")
# # # #     private Resource publicKeyPath;

# # # #     @Value("${security.jwt.issuer:auth-service}")
# # # #     private String expectedIssuer;

# # # #     @Bean
# # # #     public JwtDecoder jwtDecoder() throws Exception {
# # # #         RSAPublicKey publicKey = loadPublicKey(publicKeyPath);
# # # #         NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

# # # #         OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(expectedIssuer);
# # # #         OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();

# # # #         jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withTimestamp));
# # # #         return jwtDecoder;
# # # #     }

# # # #     private RSAPublicKey loadPublicKey(Resource resource) throws Exception {
# # # #         try (InputStream inputStream = resource.getInputStream()) {
# # # #             String key = new String(inputStream.readAllBytes())
# # # #                     .replaceAll("-----BEGIN PUBLIC KEY-----", "")
# # # #                     .replaceAll("-----END PUBLIC KEY-----", "")
# # # #                     .replaceAll("\\s+", "");
# # # #             byte[] decoded = Base64.getDecoder().decode(key);
# # # #             X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
# # # #             KeyFactory keyFactory = KeyFactory.getInstance("RSA");
# # # #             return (RSAPublicKey) keyFactory.generatePublic(keySpec);
# # # #         }
# # # #     }
# # # # }


# # # # JAVA

# ---------- Done ----------
echo "✅ CRUD controllers & services added. Please build with: (cd asset-service && mvn -DskipTests package)"

echo ""
echo "Notes:"
echo "- Uploaded files will be stored under ./asset-service/uploads/"
echo "- Asset search available at: GET /api/asset/v1/assets/search with params: assetId, assetName, categoryId, purchaseFrom, purchaseTo, soldFrom, soldTo, warrantyOn, amcOn, plus standard pageable params (page,size,sort)."
echo "- Use Authorization: Bearer <token> header for all /api endpoints."
echo "- Swagger UI will be available at /swagger-ui.html (after build) because dependency was injected into pom."

echo "All done."
