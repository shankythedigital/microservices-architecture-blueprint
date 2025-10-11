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
KEYS_DIR="$RES_ROOT/keys"



echo "Creating asset-service scaffold under: $ROOT"
mkdir -p "$SRC_ROOT"/{config,controller,dto,entity,exception,repository,security,service,service/client,util}
mkdir -p "$RES_ROOT"
mkdir -p "$DB_MIGRATION_DIR"
mkdir -p "$KEYS_DIR"

# ---------- Defaults (customize by editing the script or environment variables) ----------
: "${GROUP_ID:=com.example}"
: "${ARTIFACT_ID:=asset-service}"
: "${BASE_PACKAGE:=com.example.asset}"
: "${SERVER_PORT:=8083}"
: "${MYSQL_URL:=jdbc:mysql://localhost:3306/assetdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}"
: "${MYSQL_USER:=root}"
: "${MYSQL_PASS:=Snmysql@1110}"
: "${AUTH_SERVICE_URL:=http://localhost:8081}"
: "${NOTIFICATION_SERVICE_URL:=http://localhost:8082}"
: "${JWT_PUBLIC_KEY_CLASSPATH:=classpath:keys/jwt-public.pem}"

echo "Using defaults:"
echo " GROUP_ID=$GROUP_ID ARTIFACT_ID=$ARTIFACT_ID BASE_PACKAGE=$BASE_PACKAGE"
echo " SERVER_PORT=$SERVER_PORT MYSQL_URL=$MYSQL_URL"
echo " AUTH_SERVICE_URL=$AUTH_SERVICE_URL NOTIFICATION_SERVICE_URL=$NOTIFICATION_SERVICE_URL"
echo " JWT_PUBLIC_KEY_CLASSPATH=$JWT_PUBLIC_KEY_CLASSPATH"

# ---------- Copy jwt-public.pem from auth-service (copy mode) ----------
AUTH_KEY_SRC="$(pwd)/auth-service/src/main/resources/keys/jwt-public.pem"
if [[ -f "$AUTH_KEY_SRC" ]]; then
  cp -f "$AUTH_KEY_SRC" "$KEYS_DIR/jwt-public.pem"
  echo "Copied jwt-public.pem from auth-service -> $KEYS_DIR/jwt-public.pem"
else
  echo "WARNING: $AUTH_KEY_SRC not found."
  echo "Please ensure auth-service has keys at: auth-service/src/main/resources/keys/jwt-public.pem"
  echo "Continuing — but JWT verification will fail until public key is present."
fi

# ---------- 1) pom.xml ----------
cat > "$ROOT/pom.xml" <<'XML'
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
    <relativePath/>
  </parent>

  <groupId>com.example</groupId>
  <artifactId>asset-service</artifactId>
  <version>1.0.0</version>
  <name>asset-service</name>

  <properties>
    <java.version>17</java.version>
    <spring-cloud.version>2023.0.6</spring-cloud.version>
  </properties>

  <dependencies>
    <!-- Spring Boot starters -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- MySQL runtime -->
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>

    <!-- Feign for notification client -->
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <!-- JSON Web Token libs -->
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-api</artifactId>
      <version>0.11.5</version>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-impl</artifactId>
      <version>0.11.5</version>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-jackson</artifactId>
      <version>0.11.5</version>
    </dependency>

    <!-- Validation & utilities -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Lombok optional -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>

  </dependencies>

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
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
XML

echo "Created: $ROOT/pom.xml"

# ---------- 2) application.yml ----------
cat > "$RES_ROOT/application.yml" <<YML
server:
  port: ${SERVER_PORT}
  servlet:
    context-path: /asset-service

spring:
  application:
    name: asset-service

  datasource:
    url: ${MYSQL_URL}
    username: ${MYSQL_USER}
    password: ${MYSQL_PASS}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      connection-timeout: 20000

  jpa:
    hibernate:
      ddl-auto: update    # instead of 'validate'
    database-platform: org.hibernate.dialect.MySQL8Dialect
    show-sql: false

services:
  auth:
    base-url: ${AUTH_SERVICE_URL}
  notification:
    base-url: ${NOTIFICATION_SERVICE_URL}

security:
  jwt:
    public-key-path: ${JWT_PUBLIC_KEY_CLASSPATH}
    issuer: "auth-service"
    audience: "asset-service"

management:
  endpoints:
    web:
      exposure: health,info

logging:
  level:
    root: INFO
    com.example.asset: DEBUG
YML

echo "Created: $RES_ROOT/application.yml"

# ---------- 3) .env.asset ----------
cat > "$ROOT/.env.asset" <<ENV
MYSQL_URL=${MYSQL_URL}
MYSQL_USER=${MYSQL_USER}
MYSQL_PASS=${MYSQL_PASS}
SERVER_PORT=${SERVER_PORT}
AUTH_SERVICE_URL=${AUTH_SERVICE_URL}
NOTIFICATION_SERVICE_URL=${NOTIFICATION_SERVICE_URL}
JWT_PUBLIC_KEY_CLASSPATH=${JWT_PUBLIC_KEY_CLASSPATH}
ENV

echo "Created: $ROOT/.env.asset"

# ---------- 4) AssetServiceApplication.java ----------
cat > "$SRC_ROOT/AssetServiceApplication.java" <<JAVA
package com.example.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.asset.service.client")
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

# ---------- 6) BaseEntity.java ----------
cat > "$SRC_ROOT/entity/BaseEntity.java" <<JAVA
package com.example.asset.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "active")
    private Boolean active = true;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
JAVA

echo "Created: entity/BaseEntity.java"

# ---------- 7) security/JwtVerifier.java ----------
cat > "$SRC_ROOT/security/JwtVerifier.java" <<'JAVA'
package com.example.asset.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class JwtVerifier {

    private final PublicKey publicKey;

    public JwtVerifier(String publicKeyPath) {
        this.publicKey = loadPublicKey(publicKeyPath);
    }

    private PublicKey loadPublicKey(String path) {
        try {
            byte[] keyBytes;
            if (path.startsWith("classpath:")) {
                String r = path.replace("classpath:", "");
                try (InputStream in = new ClassPathResource(r).getInputStream()) {
                    keyBytes = in.readAllBytes();
                }
            } else {
                keyBytes = Files.readAllBytes(Paths.get(path));
            }
            String key = new String(keyBytes)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key: " + e.getMessage(), e);
        }
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid or expired token", e);
        }
    }
}
JAVA

echo "Created: security/JwtVerifier.java"

# ---------- 8) security/JwtAuthFilter.java ----------
cat > "$SRC_ROOT/security/JwtAuthFilter.java" <<'JAVA'
package com.example.asset.security;

import io.jsonwebtoken.Claims;
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
                Claims claims = jwtVerifier.validateToken(token);
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

import com.example.asset.security.JwtAuthFilter;
import com.example.asset.security.JwtVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Value("\${security.jwt.public-key-path:classpath:keys/jwt-public.pem}")
    private String publicKeyPath;

    @Bean
    public JwtVerifier jwtVerifier() {
        return new JwtVerifier(publicKeyPath);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthFilter jwtFilter = new JwtAuthFilter(jwtVerifier());
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/public/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
JAVA

echo "Created: config/SecurityConfig.java"

# ---------- 10) config/FeignConfig.java ----------
cat > "$SRC_ROOT/config/FeignConfig.java" <<JAVA
package com.example.asset.config;

import com.example.asset.service.AuthTokenService;
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
JAVA

echo "Created: config/FeignConfig.java"

# ---------- 11) GlobalExceptionHandler (exception handling) ----------
cat > "$SRC_ROOT/exception/GlobalExceptionHandler.java" <<JAVA
package com.example.asset.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> badCred(BadCredentialsException ex) {
        return ResponseEntity.status(401).body("Invalid credentials: " + ex.getMessage());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<String> locked(LockedException ex) {
        return ResponseEntity.status(423).body("Account locked: " + ex.getMessage());
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<String> expired(CredentialsExpiredException ex) {
        return ResponseEntity.status(401).body("Credentials expired: " + ex.getMessage());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> disabled(DisabledException ex) {
        return ResponseEntity.status(403).body("Account disabled: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> generic(Exception ex) {
        return ResponseEntity.internalServerError().body("Unexpected error: " + ex.getMessage());
    }
}
JAVA

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

echo "Created: exception/GlobalExceptionHandler.java"

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

import jakarta.persistence.*;

@Entity
@Table(name = "product_category")
public class ProductCategory extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;
    private String categoryName;

    public ProductCategory(){}
    public ProductCategory(String categoryName){ this.categoryName = categoryName; }

    public Long getCategoryId(){ return categoryId; }
    public void setCategoryId(Long categoryId){ this.categoryId = categoryId; }
    public String getCategoryName(){ return categoryName; }
    public void setCategoryName(String categoryName){ this.categoryName = categoryName; }
}
JAVA

cat > "$SRC_ROOT/entity/ProductSubCategory.java" <<'JAVA'
package com.example.asset.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_sub_category")
public class ProductSubCategory extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subCategoryId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    private String subCategoryName;

    public ProductSubCategory(){}
    public ProductSubCategory(String subCategoryName, ProductCategory category){
        this.subCategoryName = subCategoryName; this.category = category;
    }

    public Long getSubCategoryId(){ return subCategoryId; }
    public void setSubCategoryId(Long subCategoryId){ this.subCategoryId = subCategoryId; }
    public ProductCategory getCategory(){ return category; }
    public void setCategory(ProductCategory category){ this.category = category; }
    public String getSubCategoryName(){ return subCategoryName; }
    public void setSubCategoryName(String subCategoryName){ this.subCategoryName = subCategoryName; }
}
JAVA

cat > "$SRC_ROOT/entity/ProductMake.java" <<'JAVA'
package com.example.asset.entity;

import jakarta.persistence.*;

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

import jakarta.persistence.*;

@Entity
@Table(name = "product_model")
public class ProductModel extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modelId;

    @ManyToOne
    @JoinColumn(name = "make_id")
    private ProductMake make;

    private String modelName;

    public ProductModel(){}
    public ProductModel(String modelName, ProductMake make){
        this.modelName = modelName; this.make = make;
    }

    public Long getModelId(){ return modelId; }
    public void setModelId(Long modelId){ this.modelId = modelId; }
    public ProductMake getMake(){ return make; }
    public void setMake(ProductMake make){ this.make = make; }
    public String getModelName(){ return modelName; }
    public void setModelName(String modelName){ this.modelName = modelName; }
}
JAVA

cat > "$SRC_ROOT/entity/PurchaseOutlet.java" <<'JAVA'
package com.example.asset.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "purchase_outlet")
public class PurchaseOutlet extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outletId;
    private String outletName;
    private String outletAddress;
    private String contactInfo;
    private String outletType;

    public PurchaseOutlet(){}
    public PurchaseOutlet(String outletName, String outletAddress, String contactInfo, String outletType){
        this.outletName = outletName; this.outletAddress = outletAddress; this.contactInfo = contactInfo; this.outletType = outletType;
    }

    public Long getOutletId(){ return outletId; }
    public void setOutletId(Long outletId){ this.outletId = outletId; }
    public String getOutletName(){ return outletName; }
    public void setOutletName(String outletName){ this.outletName = outletName; }
    public String getOutletAddress(){ return outletAddress; }
    public void setOutletAddress(String outletAddress){ this.outletAddress = outletAddress; }
    public String getContactInfo(){ return contactInfo; }
    public void setContactInfo(String contactInfo){ this.contactInfo = contactInfo; }
    public String getOutletType(){ return outletType; }
    public void setOutletType(String outletType){ this.outletType = outletType; }
}
JAVA

cat > "$SRC_ROOT/entity/AssetComponent.java" <<'JAVA'
package com.example.asset.entity;

import jakarta.persistence.*;

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

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;

@Entity
@Table(name = "asset_master")
public class AssetMaster extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    private String assetNameUdv;

    @ManyToOne @JoinColumn(name="category_id")
    private ProductCategory category;

    @ManyToOne @JoinColumn(name="sub_category_id")
    private ProductSubCategory subCategory;

    @ManyToOne @JoinColumn(name="make_id")
    private ProductMake make;

    @ManyToOne @JoinColumn(name="model_id")
    private ProductModel model;

    private String makeUdv;
    private String modelUdv;
    private String purchaseMode;
    @ManyToOne @JoinColumn(name="purchase_outlet_id")
    private PurchaseOutlet purchaseOutlet;
    private String purchaseOutletUdv;
    private String purchaseOutletAddressUdv;
    private Date purchaseDate;
    private String assetStatus;
    private Date soldOnDate;
    private String salesChannelName;
    private Date createdDate;

    @ManyToMany
    @JoinTable(
        name = "asset_component_link",               // ✅ clearer join table name
        joinColumns = @JoinColumn(name = "asset_id"),
        inverseJoinColumns = @JoinColumn(name = "component_id")
    )
    private Set<AssetComponent> components = new HashSet<>();


    public Long getAssetId(){ return assetId; }
    public void setAssetId(Long assetId){ this.assetId = assetId; }

    public String getAssetNameUdv(){ return assetNameUdv; }
    public void setAssetNameUdv(String assetNameUdv){ this.assetNameUdv = assetNameUdv; }

    public ProductCategory getCategory(){ return category; }
    public void setCategory(ProductCategory category){ this.category = category; }

    public ProductSubCategory getSubCategory(){ return subCategory; }
    public void setSubCategory(ProductSubCategory subCategory){ this.subCategory = subCategory; }

    public ProductMake getMake(){ return make; }
    public void setMake(ProductMake make){ this.make = make; }

    public ProductModel getModel(){ return model; }
    public void setModel(ProductModel model){ this.model = model; }

    public String getMakeUdv(){ return makeUdv; }
    public void setMakeUdv(String makeUdv){ this.makeUdv = makeUdv; }

    public String getModelUdv(){ return modelUdv; }
    public void setModelUdv(String modelUdv){ this.modelUdv = modelUdv; }

    public String getPurchaseMode(){ return purchaseMode; }
    public void setPurchaseMode(String purchaseMode){ this.purchaseMode = purchaseMode; }

    public PurchaseOutlet getPurchaseOutlet(){ return purchaseOutlet; }
    public void setPurchaseOutlet(PurchaseOutlet purchaseOutlet){ this.purchaseOutlet = purchaseOutlet; }

    public String getPurchaseOutletUdv(){ return purchaseOutletUdv; }
    public void setPurchaseOutletUdv(String purchaseOutletUdv){ this.purchaseOutletUdv = purchaseOutletUdv; }

    public String getPurchaseOutletAddressUdv(){ return purchaseOutletAddressUdv; }
    public void setPurchaseOutletAddressUdv(String purchaseOutletAddressUdv){ this.purchaseOutletAddressUdv = purchaseOutletAddressUdv; }

    public Date getPurchaseDate(){ return purchaseDate; }
    public void setPurchaseDate(Date purchaseDate){ this.purchaseDate = purchaseDate; }

    public String getAssetStatus(){ return assetStatus; }
    public void setAssetStatus(String assetStatus){ this.assetStatus = assetStatus; }

    public Date getSoldOnDate(){ return soldOnDate; }
    public void setSoldOnDate(Date soldOnDate){ this.soldOnDate = soldOnDate; }

    public String getSalesChannelName(){ return salesChannelName; }
    public void setSalesChannelName(String salesChannelName){ this.salesChannelName = salesChannelName; }

    public Date getCreatedDate(){ return createdDate; }
    public void setCreatedDate(Date createdDate){ this.createdDate = createdDate; }

    public Set<AssetComponent> getComponents(){ return components; }
    public void setComponents(Set<AssetComponent> components){ this.components = components; }
}
JAVA

cat > "$SRC_ROOT/entity/AssetUserLink.java" <<'JAVA'
package com.example.asset.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_user_link")
public class AssetUserLink extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkId;

    @ManyToOne @JoinColumn(name="asset_id")
    private AssetMaster asset;

    private String userId;
    private String username;
    private LocalDateTime assignedDate = LocalDateTime.now();

    public Long getLinkId(){ return linkId; }
    public void setLinkId(Long linkId){ this.linkId = linkId; }
    public AssetMaster getAsset(){ return asset; }
    public void setAsset(AssetMaster asset){ this.asset = asset; }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getUsername(){ return username; }
    public void setUsername(String username){ this.username = username; }
    public LocalDateTime getAssignedDate(){ return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate){ this.assignedDate = assignedDate; }
}
JAVA

cat > "$SRC_ROOT/entity/AssetWarranty.java" <<'JAVA'
package com.example.asset.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "asset_warranty")
public class AssetWarranty extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long warrantyId;

    @ManyToOne @JoinColumn(name="asset_id")
    private AssetMaster asset;

    @ManyToOne @JoinColumn(name="component_id")
    private AssetComponent component;

    private String warrantyType;
    private Date startDate;
    private Date endDate;
    private String documentPath;
    private String userId;
    private String username;

    public Long getWarrantyId(){ return warrantyId; }
    public void setWarrantyId(Long warrantyId){ this.warrantyId = warrantyId; }
    public AssetMaster getAsset(){ return asset; }
    public void setAsset(AssetMaster asset){ this.asset = asset; }
    public AssetComponent getComponent(){ return component; }
    public void setComponent(AssetComponent component){ this.component = component; }
    public String getWarrantyType(){ return warrantyType; }
    public void setWarrantyType(String warrantyType){ this.warrantyType = warrantyType; }
    public Date getStartDate(){ return startDate; }
    public void setStartDate(Date startDate){ this.startDate = startDate; }
    public Date getEndDate(){ return endDate; }
    public void setEndDate(Date endDate){ this.endDate = endDate; }
    public String getDocumentPath(){ return documentPath; }
    public void setDocumentPath(String documentPath){ this.documentPath = documentPath; }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getUsername(){ return username; }
    public void setUsername(String username){ this.username = username; }
}
JAVA

cat > "$SRC_ROOT/entity/AssetAmc.java" <<'JAVA'
package com.example.asset.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "asset_amc")
public class AssetAmc extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long amcId;

    @ManyToOne @JoinColumn(name="asset_id")
    private AssetMaster asset;

    @ManyToOne @JoinColumn(name="component_id")
    private AssetComponent component;

    private Date startDate;
    private Date endDate;
    private String amcStatus;
    private String documentPath;
    private String userId;
    private String username;

    public Long getAmcId(){ return amcId; }
    public void setAmcId(Long amcId){ this.amcId = amcId; }
    public AssetMaster getAsset(){ return asset; }
    public void setAsset(AssetMaster asset){ this.asset = asset; }
    public AssetComponent getComponent(){ return component; }
    public void setComponent(AssetComponent component){ this.component = component; }
    public Date getStartDate(){ return startDate; }
    public void setStartDate(Date startDate){ this.startDate = startDate; }
    public Date getEndDate(){ return endDate; }
    public void setEndDate(Date endDate){ this.endDate = endDate; }
    public String getAmcStatus(){ return amcStatus; }
    public void setAmcStatus(String amcStatus){ this.amcStatus = amcStatus; }
    public String getDocumentPath(){ return documentPath; }
    public void setDocumentPath(String documentPath){ this.documentPath = documentPath; }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getUsername(){ return username; }
    public void setUsername(String username){ this.username = username; }
}
JAVA

cat > "$SRC_ROOT/entity/AssetDocument.java" <<'JAVA'
package com.example.asset.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_document")
public class AssetDocument extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne @JoinColumn(name="asset_id")
    private AssetMaster asset;

    @ManyToOne @JoinColumn(name="component_id")
    private AssetComponent component;

    private String docType;
    private String filePath;
    private LocalDateTime uploadedDate = LocalDateTime.now();
    private String userId;
    private String username;

    public Long getDocumentId(){ return documentId; }
    public void setDocumentId(Long documentId){ this.documentId = documentId; }
    public AssetMaster getAsset(){ return asset; }
    public void setAsset(AssetMaster asset){ this.asset = asset; }
    public AssetComponent getComponent(){ return component; }
    public void setComponent(AssetComponent component){ this.component = component; }
    public String getDocType(){ return docType; }
    public void setDocType(String docType){ this.docType = docType; }
    public String getFilePath(){ return filePath; }
    public void setFilePath(String filePath){ this.filePath = filePath; }
    public LocalDateTime getUploadedDate(){ return uploadedDate; }
    public void setUploadedDate(LocalDateTime uploadedDate){ this.uploadedDate = uploadedDate; }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getUsername(){ return username; }
    public void setUsername(String username){ this.username = username; }
}
JAVA

echo "Created: Entities"

# ---------- 2) Repositories ----------
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
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {}
JAVA

cat > "$SRC_ROOT/repository/ProductSubCategoryRepository.java" <<'JAVA'
package com.example.asset.repository;
import com.example.asset.entity.ProductSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductSubCategoryRepository extends JpaRepository<ProductSubCategory, Long> {}
JAVA

cat > "$SRC_ROOT/repository/ProductMakeRepository.java" <<'JAVA'
package com.example.asset.repository;
import com.example.asset.entity.ProductMake;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductMakeRepository extends JpaRepository<ProductMake, Long> {}
JAVA

cat > "$SRC_ROOT/repository/ProductModelRepository.java" <<'JAVA'
package com.example.asset.repository;
import com.example.asset.entity.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductModelRepository extends JpaRepository<ProductModel, Long> {}
JAVA

cat > "$SRC_ROOT/repository/PurchaseOutletRepository.java" <<'JAVA'
package com.example.asset.repository;
import com.example.asset.entity.PurchaseOutlet;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PurchaseOutletRepository extends JpaRepository<PurchaseOutlet, Long> {}
JAVA

cat > "$SRC_ROOT/repository/AssetComponentRepository.java" <<'JAVA'
package com.example.asset.repository;
import com.example.asset.entity.AssetComponent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AssetComponentRepository extends JpaRepository<AssetComponent, Long> {}
JAVA



cat > "$SRC_ROOT/repository/AssetMasterRepository.java" <<'JAVA'
package com.example.asset.repository;
import com.example.asset.entity.AssetMaster;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AssetMasterRepository extends JpaRepository<AssetMaster, Long> {}
JAVA

cat > "$SRC_ROOT/repository/AssetUserLinkRepository.java" <<'JAVA'
package com.example.asset.repository;
import com.example.asset.entity.AssetUserLink;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AssetUserLinkRepository extends JpaRepository<AssetUserLink, Long> {}
JAVA

cat > "$SRC_ROOT/repository/AssetWarrantyRepository.java" <<'JAVA'
package com.example.asset.repository;
import com.example.asset.entity.AssetWarranty;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AssetWarrantyRepository extends JpaRepository<AssetWarranty, Long> {}
JAVA

cat > "$SRC_ROOT/repository/AssetAmcRepository.java" <<'JAVA'
package com.example.asset.repository;
import com.example.asset.entity.AssetAmc;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AssetAmcRepository extends JpaRepository<AssetAmc, Long> {}
JAVA

cat > "$SRC_ROOT/repository/AssetDocumentRepository.java" <<'JAVA'
package com.example.asset.repository;
import com.example.asset.entity.AssetDocument;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AssetDocumentRepository extends JpaRepository<AssetDocument, Long> {}
JAVA

echo "Created: Repositories"

# ---------- 3) DTOs ----------
cat > "$SRC_ROOT/dto/AssetDto.java" <<'JAVA'
package com.example.asset.dto;
import java.util.Set;

public class AssetDto {
    public String assetNameUdv;
    public Long categoryId;
    public Long subCategoryId;
    public Long makeId;
    public Long modelId;
    public String makeUdv;
    public String modelUdv;
    public String purchaseMode;
    public Long purchaseOutletId;
    public String purchaseOutletUdv;
    public String purchaseOutletAddressUdv;
    public String purchaseDate; // yyyy-MM-dd
    public String assetStatus;
    public String soldOnDate;
    public String salesChannelName;
    public Set<Long> componentIds;
    public String userId;     // to assign
    public String username;   // to assign
}
JAVA

cat > "$SRC_ROOT/dto/AssetNotificationRequest.java" <<'JAVA'
package com.example.asset.dto;
import java.util.Map;
public class AssetNotificationRequest {
    public String channel;
    public String username;
    public String templateCode;
    public String userId;
    public Map<String, Object> placeholders;
}
JAVA

echo "Created: DTOs"

# ---------- 4) Services (AuthTokenService & AssetService) ----------
cat > "$SRC_ROOT/service/AuthTokenService.java" <<'JAVA'
package com.example.asset.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${services.auth.base-url:http://localhost:8081}")
    private String authBaseUrl;

    @Value("${auth.client-id:asset-service}")
    private String clientId;

    @Value("${auth.client-secret:asset-secret}")
    private String clientSecret;

    public String getAccessToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() instanceof String token) {
            return token;
        }
        // fallback: client credentials (ensure your auth-service exposes this)
        RestTemplate rt = new RestTemplate();
        try {
            Map<String,Object> res = rt.postForObject(authBaseUrl + "/oauth/token",
                    Map.of("client_id", clientId, "client_secret", clientSecret, "grant_type", "client_credentials"),
                    Map.class);
            if (res != null) return (String)res.get("access_token");
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
JAVA

cat > "$SRC_ROOT/service/AssetService.java" <<'JAVA'
package com.example.asset.service;

import com.example.asset.dto.AssetDto;
import com.example.asset.entity.*;
import com.example.asset.repository.*;
import com.example.asset.service.client.NotificationClient;
import com.example.asset.dto.AssetNotificationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AssetService {

    private final AssetMasterRepository assetRepo;
    private final ProductCategoryRepository catRepo;
    private final ProductSubCategoryRepository subRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final AssetComponentRepository compRepo;
    private final AssetUserLinkRepository linkRepo;
    private final NotificationClient notificationClient;

    public AssetService(AssetMasterRepository assetRepo,
                        ProductCategoryRepository catRepo,
                        ProductSubCategoryRepository subRepo,
                        ProductMakeRepository makeRepo,
                        ProductModelRepository modelRepo,
                        AssetComponentRepository compRepo,
                        AssetUserLinkRepository linkRepo,
                        NotificationClient notificationClient) {
        this.assetRepo = assetRepo;
        this.catRepo = catRepo;
        this.subRepo = subRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.compRepo = compRepo;
        this.linkRepo = linkRepo;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public AssetMaster createAsset(AssetDto dto) throws Exception {
        AssetMaster a = new AssetMaster();
        a.setAssetNameUdv(dto.assetNameUdv);
        if (dto.categoryId != null) catRepo.findById(dto.categoryId).ifPresent(a::setCategory);
        if (dto.subCategoryId != null) subRepo.findById(dto.subCategoryId).ifPresent(a::setSubCategory);
        if (dto.makeId != null) makeRepo.findById(dto.makeId).ifPresent(a::setMake);
        if (dto.modelId != null) modelRepo.findById(dto.modelId).ifPresent(a::setModel);

        a.setMakeUdv(dto.makeUdv); a.setModelUdv(dto.modelUdv);
        a.setPurchaseMode(dto.purchaseMode);
        if (dto.purchaseDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            a.setPurchaseDate(sdf.parse(dto.purchaseDate));
        }
        a.setAssetStatus(Optional.ofNullable(dto.assetStatus).orElse("AVAILABLE"));
        if (dto.componentIds != null && !dto.componentIds.isEmpty()) {
            Set<AssetComponent> comps = new HashSet<>(compRepo.findAllById(dto.componentIds));
            a.setComponents(comps);
        }
        AssetMaster saved = assetRepo.save(a);

        // create user link if provided
        if (dto.userId != null) {
            AssetUserLink link = new AssetUserLink();
            link.setAsset(saved);
            link.setUserId(dto.userId);
            link.setUsername(dto.username);
            linkRepo.save(link);

            // fire notification
            try {
                AssetNotificationRequest req = new AssetNotificationRequest();
                req.channel = "EMAIL";
                req.username = dto.username;
                req.userId = dto.userId;
                req.templateCode = "ASSET_ASSIGNED";
                Map<String,Object> placeholders = new HashMap<>();
                placeholders.put("assetName", saved.getAssetNameUdv());
                placeholders.put("assetId", saved.getAssetId());
                req.placeholders = placeholders;
                notificationClient.sendNotification(req);
            } catch (Exception e) {
                // log and continue
                System.err.println("Notification failed: " + e.getMessage());
            }
        }

        return saved;
    }

    public Optional<AssetMaster> getAsset(Long id){ return assetRepo.findById(id); }
}
JAVA

echo "Created: Services"

# ---------- 5) Feign client ----------
cat > "$SRC_ROOT/service/client/NotificationClient.java" <<'JAVA'
package com.example.asset.service.client;

import com.example.asset.dto.AssetNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "notification-service", url = "${services.notification.base-url}", configuration = com.example.asset.config.FeignConfig.class)
public interface NotificationClient {
    @PostMapping("/api/notifications")
    void sendNotification(AssetNotificationRequest req);
}
JAVA

echo "Created: Feign client"

# ---------- 6) Controller ----------
cat > "$SRC_ROOT/controller/AssetController.java" <<'JAVA'
package com.example.asset.controller;

import com.example.asset.dto.AssetDto;
import com.example.asset.entity.AssetMaster;
import com.example.asset.service.AssetService;
import com.example.asset.util.ResponseWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final AssetService service;

    public AssetController(AssetService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<ResponseWrapper<AssetMaster>> create(@RequestBody AssetDto dto) {
        try {
            AssetMaster created = service.createAsset(dto);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Asset created successfully", created));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new ResponseWrapper<>(false, "Failed to create asset: " + ex.getMessage(), null));
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseWrapper<AssetMaster>> get(@PathVariable Long id) {
        return service.getAsset(id).map(a -> ResponseEntity.ok(new ResponseWrapper<>(true, "Asset found", a)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ResponseWrapper<>(false, "Asset not found", null)));
    }
}
JAVA

echo "Created: Controller"

# ---------- 7) DataInitializer (realistic seed) ----------
cat > "$SRC_ROOT/config/DataInitializer.java" <<'JAVA'
package com.example.asset.config;

import com.example.asset.entity.*;
import com.example.asset.repository.*;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class DataInitializer {

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

    @PostConstruct
    public void init() {
        if (catRepo.count() == 0) {
            ProductCategory white = new ProductCategory("White Goods / Major Home Appliances");
            ProductCategory consumer = new ProductCategory("Consumer Electronics");
            ProductCategory smartHome = new ProductCategory("Smart Home & Premium Appliances");
            catRepo.saveAll(List.of(white, consumer, smartHome));

            ProductSubCategory tv = new ProductSubCategory("Television", consumer);
            ProductSubCategory fridge = new ProductSubCategory("Refrigerator", white);
            ProductSubCategory smartphone = new ProductSubCategory("Smartphone", consumer);
            subRepo.saveAll(List.of(tv, fridge, smartphone));

            ProductMake samsung = new ProductMake("Samsung", tv);
            ProductMake lg = new ProductMake("LG", fridge);
            ProductMake apple = new ProductMake("Apple", smartphone);
            ProductMake oneplus = new ProductMake("OnePlus", smartphone);
            makeRepo.saveAll(List.of(samsung, lg, apple, oneplus));

            ProductModel s95 = new ProductModel("S95F OLED 65", samsung);
            ProductModel glTouch = new ProductModel("GL-Touch 260L", lg);
            ProductModel iphone = new ProductModel("iPhone 15 Pro", apple);
            ProductModel oneplus12 = new ProductModel("OnePlus 12", oneplus);
            modelRepo.saveAll(List.of(s95, glTouch, iphone, oneplus12));

            PurchaseOutlet amazon = new PurchaseOutlet("Amazon", "Online portal", "support@amazon.in", "ONLINE");
            PurchaseOutlet croma = new PurchaseOutlet("Croma", "Khar West Store", "022-11112222", "OFFLINE");
            outletRepo.saveAll(List.of(amazon, croma));

            AssetComponent battery = new AssetComponent();
            battery.setComponentName("Battery Pack");
            battery.setDescription("Device battery");

            AssetComponent charger = new AssetComponent();
            charger.setComponentName("Charger");
            charger.setDescription("Charger / Adapter");

            compRepo.saveAll(List.of(battery, charger));
        }
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

# ---------- 10) Exception classes ----------
cat > "$SRC_ROOT/exception/BadCredentialsException.java" <<'JAVA'
package com.example.asset.exception;
public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String msg){ super(msg); }
}
JAVA

cat > "$SRC_ROOT/exception/LockedException.java" <<'JAVA'
package com.example.asset.exception;
public class LockedException extends RuntimeException {
    public LockedException(String msg){ super(msg); }
}
JAVA

cat > "$SRC_ROOT/exception/CredentialsExpiredException.java" <<'JAVA'
package com.example.asset.exception;
public class CredentialsExpiredException extends RuntimeException {
    public CredentialsExpiredException(String msg){ super(msg); }
}
JAVA

cat > "$SRC_ROOT/exception/DisabledException.java" <<'JAVA'
package com.example.asset.exception;
public class DisabledException extends RuntimeException {
    public DisabledException(String msg){ super(msg); }
}
JAVA

echo "Created: Exception types"

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
   3. User invokes /api/v1/assets (JWT required)
   4. Asset stored, linkage recorded, notification sent

🎯 Next:
   - Import schema to MySQL (Flyway auto-runs)
   - Ensure auth-service + notification-service are running
   - Use accessToken header: Authorization: Bearer <token>

=========================================================
MSG

echo "All done! 🎉"
