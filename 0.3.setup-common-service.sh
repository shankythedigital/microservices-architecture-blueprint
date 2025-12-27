#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT=$(pwd)
COMMON_DIR="$PROJECT_ROOT/common-service"

SRC_ROOT="$COMMON_DIR/src/main/java/com/example/common"

echo "📦 Setting up common-service module at: $COMMON_DIR"

# Create directories
mkdir -p "$SRC_ROOT"/{config,security,jpa,util,exception,constants,filter,converter,client,service,config,entity,repository}
mkdir -p "$COMMON_DIR/src/main/resources/env"
mkdir -p "$COMMON_DIR/src/test/java"


# ---------- Defaults (customize by editing the script or environment variables) ----------

AUTH_SERVER_PORT=${AUTH_SERVER_PORT:-8081}
ASSET_SERVER_PORT=${ASSET_SERVER_PORT:-8083}
NOTIFICATION_SERVER_PORT=${NOTIFICATION_SERVER_PORT:-8084}
: "${AUTH_SERVICE_URL:=http://localhost:$AUTH_SERVER_PORT}"
: "${NOTIFICATION_SERVICE_URL:=http://localhost:$NOTIFICATION_SERVER_PORT}"
: "${JWT_PUBLIC_KEY_CLASSPATH:=classpath:keys/jwt-public.pem}"

###############################################
# 1) pom.xml
###############################################
cat > "$COMMON_DIR/pom.xml" <<'XML'

<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.example</groupId>
    <artifactId>microservices-architecture-blueprint</artifactId>
    <version>0.0.5-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
  </parent>

  <artifactId>common-service</artifactId>
  <packaging>jar</packaging>
  <name>common-service</name>

  <properties>
    <java.version>17</java.version>
    <spring-cloud.version>2023.0.6</spring-cloud.version>
  </properties>

  <dependencies>
    <!-- Core -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- Web / Filters -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- JPA (for BaseEntity and attribute converters) -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Security / OAuth2 resource server helpers -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- Feign to share token via interceptor -->
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <!-- JSON Web Token utilities -->
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


    <dependency>

      <groupId>javax.annotation</groupId>

      <artifactId>javax.annotation-api</artifactId>

      <version>1.3.2</version>

    </dependency>

    <!-- Attribute encryption: javax crypto available via JDK -->
    <!-- MySQL connector (runtime) -->
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>

    <!-- Validation -->
    <dependency>
      <groupId>jakarta.validation</groupId>
      <artifactId>jakarta.validation-api</artifactId>
    </dependency>

    <!-- Test -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>

    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <scope>provided</scope>
    </dependency>


    <!-- Auth Service  -->
    <!-- <dependency>
      <groupId>com.example</groupId>
      <artifactId>auth-service</artifactId>
      <version>0.0.5-SNAPSHOT</version>
    </dependency> -->


    <!-- Notification Service  -->
    <!-- <dependency>
      <groupId>com.example</groupId>
      <artifactId>notification-service</artifactId>      
      <version>1.0.0</version>
    </dependency> -->

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

  <!-- ✅ Add build plugin for correct BOOT-INF structure -->
  
  <build>
    <plugins>
      <!-- Maven Compiler Plugin -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
          <source>${java.version}</source>
          <target>${java.version}</target>
        </configuration>
      </plugin>
  
      <!-- Spring Boot Plugin (disabled for library modules) -->
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        
        <configuration>
          <skip>true</skip>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
XML

###############################################
# 2) JwtProvider (shared)
###############################################
cat > "$SRC_ROOT/security/JwtProvider.java" <<'JAVA'
package com.example.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

/**
 * Minimal JWT provider for shared verification/generation in dev.
 * In production use the public key of auth-service or JWK set.
 */
public class JwtProvider {
    private static final String SECRET = System.getenv().getOrDefault("JWT_SECRET", "ChangeMeTo32ByteStrongSecretKey!");
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generateToken(String subject, long expiryMs) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public static String getSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token).getBody().getSubject();
    }
}
JAVA

###############################################
# 3) AccessTokenFilter (sets SecurityContext)
###############################################
cat > "$SRC_ROOT/security/AccessTokenFilter.java" <<'JAVA'
package com.example.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;
import java.util.Collections;

/**
 * Simple filter that validates shared JWTs using JwtProvider.
 * Put this filter in resource server chain only if you want shared validation fallback.
 */
public class AccessTokenFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String h = request.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            if (JwtProvider.validateToken(token)) {
                String subject = JwtProvider.getSubject(token);
                var auth = new UsernamePasswordAuthenticationToken(subject, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
JAVA


# ---------- 4) Services (AuthTokenService & AssetService) ----------
cat > "$SRC_ROOT/service/AuthTokenService.java" <<'JAVA'
package com.example.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${services.auth.base-url:http://localhost:$AUTH_SERVER_PORT}")
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
###############################################
# 4) FeignTokenInterceptor (relay Authorization header)
###############################################
cat > "$SRC_ROOT/config/FeignTokenInterceptor.java" <<'JAVA'
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
JAVA



cat > "$SRC_ROOT/config/SchedulerConfig.java" <<'JAVA'
package com.example.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
}


JAVA


cat > "$SRC_ROOT/config/AsyncConfig.java" <<'JAVA'

package com.example.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * ✅ Enables asynchronous execution across all microservices using @Async
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}


JAVA

###############################################
# 5) FeignAuthConfig (alternate static token header)
###############################################
cat > "$SRC_ROOT/config/FeignAuthConfig.java" <<'JAVA'
package com.example.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Optional: adds a static token from ENV to outgoing Feign requests when no request-scope header exists.
 */
@Configuration
public class FeignAuthConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            String token = System.getenv().getOrDefault("FEIGN_ACCESS_TOKEN", "");
            if (!token.isEmpty()) {
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}
JAVA

###############################################
# 6) ResponseWrapper (explicit getters/setters)
###############################################
cat > "$SRC_ROOT/util/FileStorageUtil.java" <<'JAVA'

package com.example.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * ✅ FileStorageUtil
 * Handles all physical file storage operations (save, retrieve, delete).
 * Default mode: stores files under `/uploads/{entityType}/` directory.
 *
 * Example saved path:
 *   uploads/ASSET/ASSET_2025-11-02_18-45-30_550e8400-e29b.pdf
 */
@Component
public class FileStorageUtil {

    private static final Logger log = LoggerFactory.getLogger(FileStorageUtil.class);
    private static final String BASE_DIR = "uploads";  // Relative to project root

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * ✅ Saves file and returns the relative path.
     */
    public String storeFile(MultipartFile file, String entityType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("❌ Cannot store empty file.");
        }

        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("❌ entityType cannot be null or empty.");
        }

        // Normalize type name (e.g. ASSET, AMC, WARRANTY)
        String typeDir = sanitizeName(entityType.toUpperCase());

        // Build storage directory path
        Path uploadDir = Paths.get(BASE_DIR, typeDir).toAbsolutePath().normalize();

        // Ensure directory exists
        Files.createDirectories(uploadDir);

        // Generate a unique, safe filename
        String originalName = sanitizeName(file.getOriginalFilename());
        String fileExt = getFileExtension(originalName);
        String uniqueName = typeDir + "_" +
                LocalDateTime.now().format(FORMATTER) + "_" +
                UUID.randomUUID() + (fileExt.isEmpty() ? "" : "." + fileExt);

        Path targetPath = uploadDir.resolve(uniqueName);

        // Copy file to target location (replace existing if needed)
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = BASE_DIR + "/" + typeDir + "/" + uniqueName;
        log.info("📁 File stored successfully: {}", relativePath);

        return relativePath;
    }

    /**
     * ✅ Reads file as Path for download/streaming.
     */
    public Path getFilePath(String relativePath) {
        Path path = Paths.get(relativePath).normalize().toAbsolutePath();
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("❌ File not found: " + relativePath);
        }
        return path;
    }

    /**
     * 🗑️ Deletes file from disk.
     */
    public boolean deleteFile(String relativePath) {
        try {
            Path path = Paths.get(relativePath).normalize().toAbsolutePath();
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("❌ Failed to delete file: {}", relativePath, e);
            return false;
        }
    }

    // ============================================================
    // 🧰 Helper methods
    // ============================================================

    private String sanitizeName(String name) {
        return name == null ? "UNKNOWN"
                : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0 && dotIndex < fileName.length() - 1)
                ? fileName.substring(dotIndex + 1)
                : "";
    }
}

JAVA


cat > "$SRC_ROOT/util/ResponseWrapper.java" <<'JAVA'

package com.example.common.util;

/**
 * ✅ Standard API response wrapper for all microservices.
 * Ensures consistent structure for success and error responses.
 *
 * @param <T> the type of the response payload
 */
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

JAVA

###############################################
# 7) HashUtil
###############################################
cat > "$SRC_ROOT/util/HashUtil.java" <<'JAVA'


package com.example.common.util;

import java.security.MessageDigest;

import javax.crypto.spec.SecretKeySpec;

import javax.crypto.Mac;

import java.util.Base64;

public class HashUtil {
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b: bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static String base64Key;

    public static void init(String base64KeyIn) {
        base64Key = base64KeyIn;
    }

    public static String fingerprint(String value) {
        if (value == null) return null;
        try {
            byte[] key = Base64.getDecoder().decode(base64Key);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] out = mac.doFinal(value.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }
}





JAVA



cat > $SRC_ROOT/util/RequestContext.java <<'JAVA'
package com.example.common.util;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class RequestContext {
    private static final ThreadLocal<String> ipHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> uaHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> urlHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> methodHolder = new ThreadLocal<>();

     
    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    // ---------------------------------------------
    // 🔹 Generic context storage
    // ---------------------------------------------
    public static void set(String key, Object value) {
        context.get().put(key, value);
    }

    public static Object get(String key) {
        return context.get().get(key);
    }

    public static void clear() {
        context.remove();
    }
    public static void setIp(String ip) { ipHolder.set(ip); }
    public static String getIp() { return ipHolder.get(); }
    public static void clearIp() { ipHolder.remove(); }

    public static void setUserAgent(String ua) { uaHolder.set(ua); }
    public static String getUserAgent() { return uaHolder.get(); }
    public static void clearUserAgent() { uaHolder.remove(); }

    public static void setUrl(String url) { urlHolder.set(url); }
    public static String getUrl() { return urlHolder.get(); }
    public static void clearUrl() { urlHolder.remove(); }

    public static void setMethod(String method) { methodHolder.set(method); }
    public static String getMethod() { return methodHolder.get(); }
    public static void clearMethod() { methodHolder.remove(); }


    public static Long getSessionId() {
        Object sid = get("sessionId");
        if (sid == null) return null;
        try {
            return Long.parseLong(sid.toString());
        } catch (Exception e) {
            return null;
        }
    }


    public static Long getUserId() {
        Object uid = get("userId");
        if (uid == null) return null;
        try {
            return Long.parseLong(uid.toString());
        } catch (Exception e) {
            return null;
        }
    }

    // Optional: populate from Spring RequestContextHolder
    public static void populateFromSpringContext() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                // You can populate here if you have specific request attributes
            }
        } catch (Exception ignored) {}
    }
    public static void clearAll() {
        clearIp();
        clearUserAgent();
        clearUrl();
        clearMethod();
    }
}

JAVA



cat > $SRC_ROOT/client/AdminClient.java <<'JAVA'
package com.example.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

/**
 * ✅ AdminClient
 * Fetches project admin users from the auth-service.
 */
@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AdminClient {

    /**
     * Returns a list of admin user details (id, username, email, mobile)
     * for the specified project type.
     */
    @GetMapping("/api/auth/v1/admins")
    List<Map<String, Object>> getAdminsByProjectType(@RequestParam("projectType") String projectType);
}

JAVA


cat > "$SRC_ROOT/client/AssetUserLinkClient.java" <<'JAVA'
package com.example.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

/**
 * ✅ AssetUserLinkClient
 * Fetches all users linked to assets under a given subcategory.
 */
@FeignClient(name = "asset-service", url = "${asset.service.url}")
public interface AssetUserLinkClient {

    @GetMapping("/api/asset/v1/userlinks/by-subcategory")
    List<Map<String, Object>> getUsersBySubCategory(@RequestParam("subCategoryId") Long subCategoryId);
}

JAVA

cat > $SRC_ROOT/client/NotificationClient.java <<'JAVA'

package com.example.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;


@FeignClient(name = "notification-service", url = "${notification.service.url}")

public interface NotificationClient {

    @PostMapping
    // void sendNotification (@RequestBody Map<String, Object> payload); 
    void sendNotification (@RequestBody Map<String, Object> payload,
    @RequestHeader("Authorization") String bearerToken); 

    // void sendNotification(
    // @RequestParam("mobile") String mobile, 
    // @RequestParam("username") String username,
    // @RequestParam("email") String email,
    // @RequestParam("templateCode") String templateCode,
    // @RequestParam Map<String, String> placeholders);

    
}

JAVA

cat > "$SRC_ROOT/util/EncryptionKeyProvider.java" <<'JAVA'
// // // // package com.example.common.util;

// // // // import java.io.BufferedReader;
// // // // import java.io.IOException;
// // // // import java.nio.charset.StandardCharsets;
// // // // import java.nio.file.*;
// // // // import java.util.Base64;
// // // // import java.util.Properties;
// // // // import java.util.stream.Stream;

// // // // /**
// // // //  * Locates and normalizes an encryption key to a Base64-encoded 32-byte value
// // // //  * suitable for AES-256 (what JpaAttributeEncryptor expects).
// // // //  *
// // // //  * Search order (first match wins):
// // // //  *  1) ENV: AUTH_ENC_KEY
// // // //  *  2) ENV: ENCRYPTION_KEY
// // // //  *  3) System property: auth.enc.key
// // // //  *  4) env/.env.auth (key AUTH_ENC_KEY)
// // // //  *  5) env/.env (keys AUTH_ENC_KEY or ENCRYPTION_KEY)
// // // //  *  6) single-line file: encryption.key under common-service resources
// // // //  *  7) properties file: enc.properties (auth.enc.key or encryption.key)
// // // //  *
// // // //  * If an ASCII/raw key is found it is converted to bytes, then padded/truncated to 32 bytes.
// // // //  * If a Base64 string is found, it's decoded; result padded/truncated to 32 bytes.
// // // //  *
// // // //  * Returns a Base64 string of exactly 32 bytes.
// // // //  */
// // // // public final class EncryptionKeyProvider {

// // // //     // Default resources path — adjust if your project layout differs.
// // // //     private static final Path COMMON_RESOURCES = Paths.get(
// // // //             "/Users/neilnaik/Documents/Shashank/Asset-LifeCycle-Management/Complete-Asset-Management/Github/microservices-architecture-blueprint/common-service/src/main/resources"
// // // //     );
// // // //     private static final Path ENV_DIR = COMMON_RESOURCES.resolve("env");
// // // //     private static final Path ENV_AUTH_FILE = ENV_DIR.resolve(".env.auth");
// // // //     private static final Path ENV_FILE = ENV_DIR.resolve(".env");
// // // //     private static final Path KEY_FILE = COMMON_RESOURCES.resolve("encryption.key");
// // // //     private static final Path PROPS_FILE = COMMON_RESOURCES.resolve("enc.properties");

// // // //     private static final int KEY_LEN = 32; // bytes for AES-256

// // // //     private EncryptionKeyProvider() { /* static helper */ }

// // // //     /**
// // // //      * Locate an encryption key and return it as a Base64-encoded 32-byte value.
// // // //      * Throws IllegalStateException if no valid key is found.
// // // //      */
// // // //     public static String getNormalizedBase64Key() {
// // // //         String raw = null;

// // // //         // 1,2) environment variables
// // // //         raw = firstNonBlank(System.getenv("AUTH_ENC_KEY"), System.getenv("ENCRYPTION_KEY"));

// // // //         // 3) system property
// // // //         if (isBlank(raw)) {
// // // //             raw = System.getProperty("auth.enc.key");
// // // //         }

// // // //         // 4) .env.auth
// // // //         if (isBlank(raw) && Files.isReadable(ENV_AUTH_FILE)) {
// // // //             raw = readKeyFromEnvFile(ENV_AUTH_FILE, "AUTH_ENC_KEY");
// // // //         }

// // // //         // 5) .env
// // // //         if (isBlank(raw) && Files.isReadable(ENV_FILE)) {
// // // //             raw = readKeyFromEnvFile(ENV_FILE, "AUTH_ENC_KEY");
// // // //             if (isBlank(raw)) raw = readKeyFromEnvFile(ENV_FILE, "ENCRYPTION_KEY");
// // // //         }

// // // //         // 6) single-line key file
// // // //         if (isBlank(raw) && Files.isReadable(KEY_FILE)) {
// // // //             raw = readSingleLineFile(KEY_FILE);
// // // //         }

// // // //         // 7) properties file
// // // //         if (isBlank(raw) && Files.isReadable(PROPS_FILE)) {
// // // //             raw = readFromProperties(PROPS_FILE, "auth.enc.key", "encryption.key");
// // // //         }

// // // //         if (isBlank(raw)) {
// // // //             throw new IllegalStateException("No encryption key found. Provide AUTH_ENC_KEY or ENCRYPTION_KEY env var, system property auth.enc.key, "
// // // //                     + ENV_AUTH_FILE + " (.env.auth), " + ENV_FILE + " (.env), " + KEY_FILE + " (encryption.key), or " + PROPS_FILE + " (enc.properties).");
// // // //         }

// // // //         // Normalize: if value appears Base64, decode it, else treat as UTF-8 bytes.
// // // //         byte[] keyBytes = tryBase64Decode(raw);
// // // //         if (keyBytes == null) {
// // // //             keyBytes = raw.getBytes(StandardCharsets.UTF_8);
// // // //         }

// // // //         // Pad or truncate to KEY_LEN
// // // //         if (keyBytes.length != KEY_LEN) {
// // // //             byte[] normalized = new byte[KEY_LEN];
// // // //             int copy = Math.min(keyBytes.length, KEY_LEN);
// // // //             System.arraycopy(keyBytes, 0, normalized, 0, copy);
// // // //             keyBytes = normalized;
// // // //         }

// // // //         // Return Base64-encoded 32-byte string
// // // //         return Base64.getEncoder().encodeToString(keyBytes);
// // // //     }

// // // //     // ---------------- helpers ----------------

// // // //     private static boolean isBlank(String s) {
// // // //         return s == null || s.trim().isEmpty();
// // // //     }

// // // //     private static String firstNonBlank(String... vals) {
// // // //         if (vals == null) return null;
// // // //         for (String v : vals) if (!isBlank(v)) return v;
// // // //         return null;
// // // //     }

// // // //     private static byte[] tryBase64Decode(String s) {
// // // //         try {
// // // //             byte[] dec = Base64.getDecoder().decode(s);
// // // //             // if decoding yields < 1 byte, consider it invalid
// // // //             if (dec == null || dec.length == 0) return null;
// // // //             return dec;
// // // //         } catch (IllegalArgumentException e) {
// // // //             return null;
// // // //         }
// // // //     }

// // // //     private static String readKeyFromEnvFile(Path p, String keyName) {
// // // //         try (Stream<String> lines = Files.lines(p, StandardCharsets.UTF_8)) {
// // // //             return lines
// // // //                     .map(String::trim)
// // // //                     .filter(line -> !line.isEmpty() && !line.startsWith("#"))
// // // //                     .map(line -> {
// // // //                         int idx = line.indexOf('=');
// // // //                         if (idx <= 0) return null;
// // // //                         String k = line.substring(0, idx).trim();
// // // //                         String v = line.substring(idx + 1).trim();
// // // //                         return k.equals(keyName) ? v : null;
// // // //                     })
// // // //                     .filter(v -> v != null && !v.isEmpty())
// // // //                     .findFirst()
// // // //                     .orElse(null);
// // // //         } catch (IOException e) {
// // // //             return null;
// // // //         }
// // // //     }

// // // //     private static String readSingleLineFile(Path p) {
// // // //         try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
// // // //             String line = r.readLine();
// // // //             return (line == null) ? null : line.trim();
// // // //         } catch (IOException e) {
// // // //             return null;
// // // //         }
// // // //     }

// // // //     private static String readFromProperties(Path p, String... keys) {
// // // //         Properties props = new Properties();
// // // //         try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
// // // //             props.load(r);
// // // //             for (String k : keys) {
// // // //                 String v = props.getProperty(k);
// // // //                 if (!isBlank(v)) return v.trim();
// // // //             }
// // // //         } catch (IOException ignored) {
// // // //         }
// // // //         return null;
// // // //     }
// // // // }


// // VERSION #2
// package com.example.common.util;

// import java.io.BufferedReader;
// import java.io.File;
// import java.io.IOException;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.*;
// import java.util.Base64;
// import java.util.Properties;
// import java.util.stream.Stream;

// public final class EncryptionKeyProvider {

//     private static final Path EXPORT_ENV = Paths.get("/opt/app/env/exported.env");
//     private static final Path KEY_FILE = Paths.get("/opt/app/env/encryption.key");
//     private static final int KEY_LEN = 32;

//     private EncryptionKeyProvider() { }

//     public static String getNormalizedBase64Key() {
//         String raw = firstNonBlank(System.getenv("AUTH_ENC_KEY"), System.getenv("ENCRYPTION_KEY"));

//         // try to read exported.env if not found in process env
//         if (isBlank(raw) && Files.isReadable(EXPORT_ENV)) {
//             raw = readKeyFromEnvFile(EXPORT_ENV, "AUTH_ENC_KEY");
//             if (isBlank(raw)) raw = readKeyFromEnvFile(EXPORT_ENV, "ENCRYPTION_KEY");
//         }

//         // try single file
//         if (isBlank(raw) && Files.isReadable(KEY_FILE)) {
//             raw = readSingleLineFile(KEY_FILE);
//         }

//         if (isBlank(raw)) {
//             throw new IllegalStateException("❌ No encryption key found.\n"
//                     + "Set AUTH_ENC_KEY or ENCRYPTION_KEY as environment variables (loaded from /opt/app/env/exported.env).");
//         }

//         byte[] keyBytes = tryBase64Decode(raw);
//         if (keyBytes == null) keyBytes = raw.getBytes(StandardCharsets.UTF_8);

//         if (keyBytes.length != KEY_LEN) {
//             byte[] normalized = new byte[KEY_LEN];
//             int copy = Math.min(keyBytes.length, KEY_LEN);
//             System.arraycopy(keyBytes, 0, normalized, 0, copy);
//             keyBytes = normalized;
//         }

//         return Base64.getEncoder().encodeToString(keyBytes);
//     }

//     // helpers
//     private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
//     private static String firstNonBlank(String... vals) {
//         if (vals == null) return null;
//         for (String v : vals) if (!isBlank(v)) return v;
//         return null;
//     }
//     private static byte[] tryBase64Decode(String s) {
//         try {
//             byte[] dec = Base64.getDecoder().decode(s);
//             if (dec == null || dec.length == 0) return null;
//             return dec;
//         } catch (IllegalArgumentException e) {
//             return null;
//         }
//     }
//     private static String readKeyFromEnvFile(Path p, String keyName) {
//         try (Stream<String> lines = Files.lines(p, StandardCharsets.UTF_8)) {
//             return lines
//                     .map(String::trim)
//                     .filter(l -> !l.isEmpty() && !l.startsWith("#"))
//                     .map(line -> {
//                         int idx = line.indexOf('=');
//                         if (idx <= 0) return null;
//                         String k = line.substring(0, idx).trim();
//                         String v = line.substring(idx + 1).trim();
//                         return k.equals(keyName) ? v : null;
//                     })
//                     .filter(v -> v != null && !v.isEmpty())
//                     .findFirst()
//                     .orElse(null);
//         } catch (IOException e) {
//             return null;
//         }
//     }
//     private static String readSingleLineFile(Path p) {
//         try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
//             String line = r.readLine();
//             return (line == null) ? null : line.trim();
//         } catch (IOException e) {
//             return null;
//         }
//     }
// }

package com.example.common.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Cloud-first AES-256 EncryptionKeyProvider.
 *
 * Priority (first match wins):
 *
 * 1) CLOUD:
 *     /opt/app/env/exported.env       (AUTH_ENC_KEY / ENCRYPTION_KEY)
 *     /opt/app/env/encryption.key     (single-line RAW or Base64)
 *
 * 2) LOCAL classpath:
 *     env/.env.auth
 *     env/.env
 *     encryption.key
 *     enc.properties
 *
 * 3) ENV variables:
 *     AUTH_ENC_KEY
 *     ENCRYPTION_KEY
 *
 * 4) JVM property:
 *     -Dauth.enc.key
 *
 * Output:
 *     Base64 string representing EXACT 32-byte AES-256 key.
 */
public final class EncryptionKeyProvider {

    private static final Object LOCK = new Object();
    private static volatile byte[] KEY_BYTES;

    private static final Path CLOUD_ENV = Paths.get("/opt/app/env/exported.env");
    private static final Path CLOUD_KEY = Paths.get("/opt/app/env/encryption.key");

    private static final int AES_LEN = 32; // 32 bytes AES-256

    private EncryptionKeyProvider() {}

    // ============================================================================
    // PUBLIC API
    // ============================================================================
    public static String getNormalizedBase64Key() {
        ensureInitialized();
        return Base64.getEncoder().encodeToString(KEY_BYTES);
    }

    // ============================================================================
    // INITIALIZER — cloud → local → env → system
    // ============================================================================
    private static void ensureInitialized() {
        if (KEY_BYTES != null) return;

        synchronized (LOCK) {
            if (KEY_BYTES != null) return;

            System.out.println("--------------------------------------------------");
            System.out.println("[ENC] Resolving AES encryption key (cloud → local → env → system)");
            System.out.println("--------------------------------------------------");

            String raw = null;

            // ----------------------------------------------------------
            // 1️⃣ CLOUD: exported.env
            // ----------------------------------------------------------
            if (Files.isReadable(CLOUD_ENV)) {
                raw = readEnvFile(CLOUD_ENV, "AUTH_ENC_KEY");
                if (isBlank(raw))
                    raw = readEnvFile(CLOUD_ENV, "ENCRYPTION_KEY");

                if (!isBlank(raw)) {
                    logSource("CLOUD", CLOUD_ENV.toString(), raw);
                    KEY_BYTES = normalize(raw);
                    return;
                }
            }

            // ----------------------------------------------------------
            // 2️⃣ CLOUD: encryption.key
            // ----------------------------------------------------------
            if (Files.isReadable(CLOUD_KEY)) {
                raw = readSingleLineFile(CLOUD_KEY);

                if (!isBlank(raw)) {
                    logSource("CLOUD", CLOUD_KEY.toString(), raw);
                    KEY_BYTES = normalize(raw);
                    return;
                }
            }

            // ----------------------------------------------------------
            // 3️⃣ LOCAL CLASSPATH FILES
            // ----------------------------------------------------------

            raw = readEnvFromClasspath("env/.env.auth", "AUTH_ENC_KEY");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:env/.env.auth", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readEnvFromClasspath("env/.env", "AUTH_ENC_KEY");
            if (isBlank(raw))
                raw = readEnvFromClasspath("env/.env", "ENCRYPTION_KEY");

            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:env/.env", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readSingleLineClasspath("encryption.key");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:encryption.key", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readFromClasspathProperties("enc.properties",
                    "auth.enc.key", "encryption.key");

            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:enc.properties", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            // ----------------------------------------------------------
            // 4️⃣ ENV variables
            // ----------------------------------------------------------
            raw = firstNonBlank(
                    System.getenv("AUTH_ENC_KEY"),
                    System.getenv("ENCRYPTION_KEY")
            );

            if (!isBlank(raw)) {
                logSource("ENV", "AUTH_ENC_KEY / ENCRYPTION_KEY", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            // ----------------------------------------------------------
            // 5️⃣ JVM system property
            // ----------------------------------------------------------
            raw = System.getProperty("auth.enc.key");
            if (!isBlank(raw)) {
                logSource("SYSTEM", "-Dauth.enc.key", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            throw new IllegalStateException("❌ No AES encryption key found in cloud/local/env/system sources.");
        }
    }

    // ============================================================================
    // LOGGING HELPERS
    // ============================================================================
    private static void logSource(String type, String source, String raw) {
        String preview = raw.length() > 10 ? raw.substring(0, 10) + "..." : raw;
        System.out.println("[ENC] ✔ Loaded from " + type + ": " + source);
        System.out.println("[ENC]   Key Preview: " + preview);
    }

    private static void logNormalized(byte[] bytes) {
        System.out.println("[ENC] ✔ Normalized to EXACT 32 bytes (AES-256)");
        System.out.println("[ENC]   Final Base64 length = " +
                Base64.getEncoder().encodeToString(bytes).length());
    }

    // ============================================================================
    // KEY NORMALIZATION (ALWAYS 32 BYTES)
    // ============================================================================
    private static byte[] normalize(String raw) {
        byte[] bytes;

        try {
            bytes = Base64.getDecoder().decode(raw);
        } catch (IllegalArgumentException ex) {
            bytes = raw.getBytes(StandardCharsets.UTF_8);
        }

        if (bytes.length != AES_LEN) {
            byte[] fixed = new byte[AES_LEN];
            System.arraycopy(bytes, 0, fixed, 0, Math.min(bytes.length, AES_LEN));
            bytes = fixed;
        }

        logNormalized(bytes);
        return bytes;
    }

    // ============================================================================
    // CLOUD FILE READERS
    // ============================================================================
    private static String readEnvFile(Path path, String key) {
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .map(String::trim)
                    .filter(l -> l.contains("="))
                    .map(l -> {
                        int idx = l.indexOf('=');
                        String k = l.substring(0, idx).trim();
                        String v = l.substring(idx + 1).trim();
                        return k.equals(key) ? v : null;
                    })
                    .filter(v -> !isBlank(v))
                    .findFirst().orElse(null);
        } catch (IOException ignored) {}
        return null;
    }

    private static String readSingleLineFile(Path p) {
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line = br.readLine();
            return line == null ? null : line.trim();
        } catch (IOException ignored) {}
        return null;
    }

    // ============================================================================
    // CLASSPATH READERS
    // ============================================================================
    private static String readEnvFromClasspath(String resource, String keyName) {
        try (InputStream in = EncryptionKeyProvider.class.getClassLoader()
                .getResourceAsStream(resource)) {

            if (in == null) return null;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                return br.lines()
                        .map(String::trim)
                        .filter(l -> l.contains("="))
                        .map(l -> {
                            int idx = l.indexOf('=');
                            String k = l.substring(0, idx).trim();
                            String v = l.substring(idx + 1).trim();
                            return k.equals(keyName) ? v : null;
                        })
                        .filter(v -> !isBlank(v))
                        .findFirst().orElse(null);
            }
        } catch (IOException ignored) {}
        return null;
    }

    private static String readSingleLineClasspath(String resource) {
        try (InputStream in = EncryptionKeyProvider.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            return line == null ? null : line.trim();
        } catch (IOException ignored) {}
        return null;
    }

    private static String readFromClasspathProperties(String resource, String... keys) {
        try (InputStream in = EncryptionKeyProvider.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return null;

            Properties p = new Properties();
            p.load(in);

            for (String k : keys) {
                String v = p.getProperty(k);
                if (!isBlank(v)) return v.trim();
            }
        } catch (IOException ignored) {}
        return null;
    }

    // ============================================================================
    // UTILITY HELPERS
    // ============================================================================
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (!isBlank(v)) return v;
        return null;
    }
}


JAVA




cat > "$SRC_ROOT/util/HmacUtil.java" <<'JAVA'
// package com.example.common.util;

// import javax.crypto.Mac;
// import javax.crypto.spec.SecretKeySpec;
// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.*;
// import java.util.HexFormat;
// import java.util.Properties;
// import java.util.stream.Stream;

// /**
//  * HMAC-SHA256 utility that lazily initializes its key from multiple sources.
//  *
//  * Initialization order:
//  * 1) ENV: AUTH_HMAC_KEY or HMAC_KEY
//  * 2) System property: auth.hmac.key
//  * 3) common-service resources env/.env.auth (AUTH_HMAC_KEY)
//  * 4) common-service resources env/.env (AUTH_HMAC_KEY or HMAC_KEY)
//  * 5) common-service resources hmac.key (single-line)
//  * 6) common-service resources hmac.properties (hmac.key or auth.hmac.key)
//  * 7) classpath resources with same names (fallback)
//  *
//  * This class does NOT require Spring lifecycle; it initializes on first use.
//  */
// public final class HmacUtil {

//     private static final String HMAC_ALGO = "HmacSHA256";
//     private static volatile byte[] KEY_BYTES; // guarded by initLock
//     private static final Object initLock = new Object();

//     // Absolute path to the common-service resources folder (adjust if needed)
//     private static final Path COMMON_RESOURCES = Paths.get(
//         "/Users/neilnaik/Documents/Shashank/Asset-LifeCycle-Management/Complete-Asset-Management/Github/microservices-architecture-blueprint/common-service/src/main/resources"
//     );

//     private static final Path ENV_DIR = COMMON_RESOURCES.resolve("env");
//     private static final Path ENV_AUTH_FILE = ENV_DIR.resolve(".env.auth");
//     private static final Path ENV_FILE = ENV_DIR.resolve(".env");
//     private static final Path HMAC_KEY_FILE = COMMON_RESOURCES.resolve("hmac.key");
//     private static final Path HMAC_PROPERTIES_FILE = COMMON_RESOURCES.resolve("hmac.properties");

//     // Minimum accepted key length in bytes (16)
//     private static final int MIN_KEY_BYTES = 16;

//     private HmacUtil() { /* utility */ }

//     // ---------------- public API ----------------

//     /**
//      * Compute HMAC-SHA256 and return lowercase hex string.
//      * Lazily initializes the key on first call.
//      */
//     public static String hmacHex(String data) {
//         if (data == null) return null;
//         ensureInitialized();
//         try {
//             Mac mac = Mac.getInstance(HMAC_ALGO);
//             mac.init(new SecretKeySpec(KEY_BYTES, HMAC_ALGO));
//             byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
//             return HexFormat.of().formatHex(raw);
//         } catch (Exception e) {
//             throw new RuntimeException("Failed to compute HMAC", e);
//         }
//     }

//     /** Case-insensitive compare of expected hex vs computed */
//     public static boolean verifyHmac(String data, String expectedHex) {
//         if (expectedHex == null) return false;
//         String actual = hmacHex(data);
//         return actual != null && actual.equalsIgnoreCase(expectedHex);
//     }

//     // ---------------- initialization ----------------

//     private static void ensureInitialized() {
//         if (KEY_BYTES == null) {
//             synchronized (initLock) {
//                 if (KEY_BYTES == null) {
//                     String key = locateKey();
//                     if (key == null || key.trim().isEmpty()) {
//                         throw new IllegalStateException("HMAC key not found. Provide AUTH_HMAC_KEY or HMAC_KEY env var, " +
//                                 "system property auth.hmac.key, or one of the files under " + COMMON_RESOURCES);
//                     }
//                     byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
//                     if (bytes.length < MIN_KEY_BYTES) {
//                         throw new IllegalArgumentException("HMAC key must be at least " + MIN_KEY_BYTES + " bytes");
//                     }
//                     KEY_BYTES = bytes;
//                     // short preview only
//                     String preview = key.length() > 8 ? key.substring(0, 6) + "..." : key;
//                     System.out.println("HmacUtil initialized — key length=" + KEY_BYTES.length + ", preview=" + preview);
//                 }
//             }
//         }
//     }

//     /**
//      * Try to find the key from multiple sources.
//      */
//     private static String locateKey() {
//         // 1) environment variables
//         String v = firstNonBlank(System.getenv("AUTH_HMAC_KEY"), System.getenv("HMAC_KEY"));
//         if (!isBlank(v)) return v;

//         // 2) system property
//         v = System.getProperty("auth.hmac.key");
//         if (!isBlank(v)) return v;

//         // 3) .env.auth file in common resources
//         if (Files.isReadable(ENV_AUTH_FILE)) {
//             v = readKeyFromEnvFile(ENV_AUTH_FILE, "AUTH_HMAC_KEY");
//             if (!isBlank(v)) return v;
//         }

//         // 4) .env generic
//         if (Files.isReadable(ENV_FILE)) {
//             v = readKeyFromEnvFile(ENV_FILE, "AUTH_HMAC_KEY");
//             if (!isBlank(v)) return v;
//             v = readKeyFromEnvFile(ENV_FILE, "HMAC_KEY");
//             if (!isBlank(v)) return v;
//         }

//         // 5) hmac.key single-line file
//         if (Files.isReadable(HMAC_KEY_FILE)) {
//             v = readSingleLineFile(HMAC_KEY_FILE);
//             if (!isBlank(v)) return v;
//         }

//         // 6) hmac.properties
//         if (Files.isReadable(HMAC_PROPERTIES_FILE)) {
//             v = readFromProperties(HMAC_PROPERTIES_FILE, "hmac.key", "auth.hmac.key");
//             if (!isBlank(v)) return v;
//         }

//         // 7) fallback to classpath resources (env/.env.auth etc)
//         v = readFromClasspath(".env.auth", "AUTH_HMAC_KEY");
//         if (!isBlank(v)) return v;
//         v = readFromClasspath(".env", "AUTH_HMAC_KEY", "HMAC_KEY");
//         if (!isBlank(v)) return v;
//         v = readSingleLineFromClasspath("hmac.key");
//         if (!isBlank(v)) return v;
//         v = readFromClasspathProperties("hmac.properties", "hmac.key", "auth.hmac.key");
//         if (!isBlank(v)) return v;

//         return null;
//     }

//     // ---------------- helpers ----------------

//     private static boolean isBlank(String s) {
//         return s == null || s.trim().isEmpty();
//     }

//     private static String firstNonBlank(String... vals) {
//         if (vals == null) return null;
//         for (String v : vals) if (!isBlank(v)) return v;
//         return null;
//     }

//     private static String readKeyFromEnvFile(Path p, String keyName) {
//         try (Stream<String> lines = Files.lines(p, StandardCharsets.UTF_8)) {
//             return lines
//                     .map(String::trim)
//                     .filter(line -> !line.isEmpty() && !line.startsWith("#"))
//                     .map(line -> {
//                         int idx = line.indexOf('=');
//                         if (idx <= 0) return null;
//                         String k = line.substring(0, idx).trim();
//                         String v = line.substring(idx + 1).trim();
//                         return k.equals(keyName) ? v : null;
//                     })
//                     .filter(v -> v != null && !v.isEmpty())
//                     .findFirst()
//                     .orElse(null);
//         } catch (IOException e) {
//             return null;
//         }
//     }

//     private static String readSingleLineFile(Path p) {
//         try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
//             String line = r.readLine();
//             return (line == null) ? null : line.trim();
//         } catch (IOException e) {
//             return null;
//         }
//     }

//     private static String readFromProperties(Path p, String... keys) {
//         Properties props = new Properties();
//         try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
//             props.load(r);
//             for (String k : keys) {
//                 String v = props.getProperty(k);
//                 if (!isBlank(v)) return v.trim();
//             }
//         } catch (IOException ignored) {}
//         return null;
//     }

//     private static String readFromClasspath(String resourceName, String... keys) {
//         try (InputStream is = HmacUtil.class.getClassLoader().getResourceAsStream(resourceName)) {
//             if (is == null) return null;
//             try (BufferedReader r = new BufferedReader(new java.io.InputStreamReader(is, StandardCharsets.UTF_8))) {
//                 return r.lines()
//                         .map(String::trim)
//                         .filter(line -> !line.isEmpty() && !line.startsWith("#"))
//                         .map(line -> {
//                             int idx = line.indexOf('=');
//                             if (idx <= 0) return null;
//                             String k = line.substring(0, idx).trim();
//                             String v = line.substring(idx + 1).trim();
//                             for (String key : keys) {
//                                 if (k.equals(key)) return v;
//                             }
//                             return null;
//                         })
//                         .filter(v -> v != null && !v.isEmpty())
//                         .findFirst()
//                         .orElse(null);
//             }
//         } catch (IOException ignored) { }
//         return null;
//     }

//     private static String readSingleLineFromClasspath(String resourceName) {
//         try (InputStream is = HmacUtil.class.getClassLoader().getResourceAsStream(resourceName)) {
//             if (is == null) return null;
//             try (BufferedReader r = new BufferedReader(new java.io.InputStreamReader(is, StandardCharsets.UTF_8))) {
//                 String line = r.readLine();
//                 return line == null ? null : line.trim();
//             }
//         } catch (IOException ignored) {}
//         return null;
//     }

//     private static String readFromClasspathProperties(String resourceName, String... keys) {
//         try (InputStream is = HmacUtil.class.getClassLoader().getResourceAsStream(resourceName)) {
//             if (is == null) return null;
//             Properties props = new Properties();
//             props.load(is);
//             for (String k : keys) {
//                 String v = props.getProperty(k);
//                 if (!isBlank(v)) return v.trim();
//             }
//         } catch (IOException ignored) {}
//         return null;
//     }
// }

package com.example.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Cloud-first HMAC key resolver.
 *
 * Priority:
 * 1) CLOUD: /opt/app/env/exported.env      (AUTH_HMAC_KEY / HMAC_KEY)
 * 2) CLOUD: /opt/app/env/hmac.key          (single-line)
 *
 * 3) LOCAL classpath:
 *      env/.env.auth
 *      env/.env
 *      hmac.key
 *      hmac.properties
 *
 * 4) ENV variables: AUTH_HMAC_KEY, HMAC_KEY
 *
 * 5) JVM system property: auth.hmac.key
 *
 * Output → Base64(32 bytes) final HMAC key.
 */
public final class HmacUtil {

    private static volatile byte[] KEY_BYTES;
    private static final Object LOCK = new Object();

    private static final Path CLOUD_ENV = Paths.get("/opt/app/env/exported.env");
    private static final Path CLOUD_KEY = Paths.get("/opt/app/env/hmac.key");

    private static final int KEY_LEN = 32;

    private HmacUtil() {}

    // ============================================================================
    // PUBLIC API
    // ============================================================================
    public static String hmacHex(String data) {
        ensureInitialized();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(KEY_BYTES, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to compute HMAC", ex);
        }
    }

    // ============================================================================
    // INITIALIZATION — cloud → local → env → system property
    // ============================================================================
    private static void ensureInitialized() {
        if (KEY_BYTES != null) return;

        synchronized (LOCK) {
            if (KEY_BYTES != null) return;

            String raw = null;

            System.out.println("--------------------------------------------------");
            System.out.println("[HMAC] Resolving HMAC key (cloud → local → env → system)");
            System.out.println("--------------------------------------------------");

            // ----------------------------------------------------------
            // 1️⃣ CLOUD: exported.env
            // ----------------------------------------------------------
            if (Files.isReadable(CLOUD_ENV)) {
                raw = readEnvFile(CLOUD_ENV, "AUTH_HMAC_KEY");
                if (isBlank(raw)) raw = readEnvFile(CLOUD_ENV, "HMAC_KEY");

                if (!isBlank(raw)) {
                    logSource("CLOUD", CLOUD_ENV.toString(), raw);
                    KEY_BYTES = normalize(raw);
                    return;
                }
            }

            // ----------------------------------------------------------
            // 2️⃣ CLOUD: /opt/app/env/hmac.key
            // ----------------------------------------------------------
            if (Files.isReadable(CLOUD_KEY)) {
                raw = readSingleLineFile(CLOUD_KEY);

                if (!isBlank(raw)) {
                    logSource("CLOUD", CLOUD_KEY.toString(), raw);
                    KEY_BYTES = normalize(raw);
                    return;
                }
            }

            // ----------------------------------------------------------
            // 3️⃣ LOCAL classpath resources
            // ----------------------------------------------------------
            raw = readFromClasspathEnv("env/.env.auth", "AUTH_HMAC_KEY");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:env/.env.auth", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readFromClasspathEnv("env/.env", "AUTH_HMAC_KEY");
            if (isBlank(raw)) raw = readFromClasspathEnv("env/.env", "HMAC_KEY");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:env/.env", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readSingleLineClasspath("hmac.key");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:hmac.key", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readClasspathProperties("hmac.properties",
                    "hmac.key", "auth.hmac.key");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:hmac.properties", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            // ----------------------------------------------------------
            // 4️⃣ ENV variables
            // ----------------------------------------------------------
            raw = firstNonBlank(
                    System.getenv("AUTH_HMAC_KEY"),
                    System.getenv("HMAC_KEY")
            );
            if (!isBlank(raw)) {
                logSource("ENV", "AUTH_HMAC_KEY / HMAC_KEY", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            // ----------------------------------------------------------
            // 5️⃣ JVM System Property
            // ----------------------------------------------------------
            raw = System.getProperty("auth.hmac.key");
            if (!isBlank(raw)) {
                logSource("SYSTEM", "-Dauth.hmac.key", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            throw new IllegalStateException("❌ No HMAC key found in cloud/local/env/system sources.");
        }
    }

    // ============================================================================
    // LOGGING
    // ============================================================================
    private static void logSource(String type, String source, String raw) {
        String preview = raw.length() > 10 ? raw.substring(0, 10) + "..." : raw;
        System.out.println("[HMAC] ✔ Loaded from " + type + ": " + source);
        System.out.println("[HMAC]   Raw preview: " + preview);
    }

    private static void logNormalized(byte[] bytes) {
        System.out.println("[HMAC] ✔ Normalized key to EXACT 32 bytes");
        System.out.println("[HMAC]   Base64 length = " +
                Base64.getEncoder().encodeToString(bytes).length());
    }

    // ============================================================================
    // NORMALIZATION
    // ============================================================================
    private static byte[] normalize(String raw) {
        byte[] bytes;

        try {
            bytes = Base64.getDecoder().decode(raw);
        } catch (Exception ex) {
            bytes = raw.getBytes(StandardCharsets.UTF_8);
        }

        if (bytes.length != KEY_LEN) {
            byte[] fixed = new byte[KEY_LEN];
            System.arraycopy(bytes, 0, fixed,
                    0, Math.min(bytes.length, KEY_LEN));
            bytes = fixed;
        }

        logNormalized(bytes);
        return bytes;
    }

    // ============================================================================
    // HELPERS
    // ============================================================================
    private static boolean isBlank(String s) {
        return s == null || s.isEmpty() || s.trim().isEmpty();
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (!isBlank(v)) return v;
        return null;
    }

    private static String readEnvFile(Path p, String key) {
        try (Stream<String> lines = Files.lines(p)) {
            return lines
                    .map(String::trim)
                    .filter(l -> !l.startsWith("#"))
                    .filter(l -> l.contains("="))
                    .map(l -> {
                        String k = l.substring(0, l.indexOf("=")).trim();
                        String v = l.substring(l.indexOf("=") + 1).trim();
                        return k.equals(key) ? v : null;
                    })
                    .filter(v -> !isBlank(v))
                    .findFirst()
                    .orElse(null);
        } catch (IOException ignored) {}
        return null;
    }

    private static String readSingleLineFile(Path p) {
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line = br.readLine();
            return line == null ? null : line.trim();
        } catch (IOException ignored) {}
        return null;
    }

    private static String readFromClasspathEnv(String resource, String key) {
        try (InputStream in = HmacUtil.class.getClassLoader()
                .getResourceAsStream(resource)) {
            if (in == null) return null;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(in))) {

                return br.lines()
                        .map(String::trim)
                        .filter(l -> !l.startsWith("#"))
                        .filter(l -> l.contains("="))
                        .map(l -> {
                            String k = l.substring(0, l.indexOf("=")).trim();
                            String v = l.substring(l.indexOf("=") + 1).trim();
                            return k.equals(key) ? v : null;
                        })
                        .filter(v -> !isBlank(v))
                        .findFirst().orElse(null);
            }
        } catch (IOException ignored) {}
        return null;
    }

    private static String readSingleLineClasspath(String resource) {
        try (InputStream in = HmacUtil.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            return line == null ? null : line.trim();
        } catch (IOException ignored) {}
        return null;
    }

    private static String readClasspathProperties(String resource, String... keys) {
        try (InputStream in = HmacUtil.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return null;
            Properties props = new Properties();
            props.load(in);

            for (String k : keys) {
                String v = props.getProperty(k);
                if (!isBlank(v)) return v.trim();
            }

        } catch (IOException ignored) {}
        return null;
    }
}


JAVA




cat > "$SRC_ROOT/util/JwtUtil.java" <<'JAVA'


// // // // // package com.example.common.util;

// // // // // import io.jsonwebtoken.*;
// // // // // import io.jsonwebtoken.security.Keys;
// // // // // import jakarta.annotation.PostConstruct;
// // // // // import org.springframework.beans.factory.annotation.Value;
// // // // // import org.springframework.core.io.Resource;
// // // // // import org.springframework.stereotype.Component;

// // // // // import java.io.InputStream;
// // // // // import java.nio.charset.StandardCharsets;
// // // // // import java.security.*;
// // // // // import java.security.spec.PKCS8EncodedKeySpec;
// // // // // import java.security.spec.X509EncodedKeySpec;
// // // // // import java.time.Instant;
// // // // // import java.util.*;

// // // // // /**
// // // // //  * ✅ Shared JWT utility for all microservices.
// // // // //  * Supports both:
// // // // //  *  - RSA (public/private PEM keypair) signing (RS256)
// // // // //  *  - HMAC (HS256) secret key signing
// // // // //  *
// // // // //  * Loads config from environment variables or Spring Boot properties.
// // // // //  * Automatically determines signing mode at startup.
// // // // //  */
// // // // // @Component
// // // // // public class JwtUtil {

// // // // //     // ------------------------------------------------------------------------
// // // // //     // 🔧 Configurable properties (Spring Boot / environment)
// // // // //     // ------------------------------------------------------------------------
// // // // //     @Value("${JWT_PRIVATE_KEY_PATH:#{null}}")
// // // // //     private Resource privateKeyResource;

// // // // //     @Value("${JWT_PUBLIC_KEY_PATH:#{null}}")
// // // // //     private Resource publicKeyResource;

// // // // //     @Value("${JWT_SECRET:#{null}}")
// // // // //     private String hmacSecret;

// // // // //     @Value("${JWT_ACCESS_TOKEN_VALIDITY_SECONDS:900}") // 15 minutes default
// // // // //     private long accessTokenValiditySeconds;

// // // // //     @Value("${JWT_REFRESH_TOKEN_VALIDITY_SECONDS:1209600}") // 14 days default
// // // // //     private long refreshTokenValiditySeconds;

// // // // //     // ------------------------------------------------------------------------
// // // // //     // 🔑 Internal key material
// // // // //     // ------------------------------------------------------------------------
// // // // //     private PrivateKey privateKey;
// // // // //     private PublicKey publicKey;
// // // // //     private Key hmacKey;
// // // // //     private boolean useRsa = false;

// // // // //     // ------------------------------------------------------------------------
// // // // //     // 🚀 Initialization
// // // // //     // ------------------------------------------------------------------------
// // // // //     @PostConstruct
// // // // //     private void init() {
// // // // //         try {
// // // // //             // Attempt to load RSA keys first
// // // // //             if (publicKeyResource != null && publicKeyResource.exists()) {
// // // // //                 this.publicKey = loadPublicKey(publicKeyResource);
// // // // //                 System.out.println("🔒 [JwtUtil] Loaded RSA public key");
// // // // //                 useRsa = true;
// // // // //             }

// // // // //             if (privateKeyResource != null && privateKeyResource.exists()) {
// // // // //                 this.privateKey = loadPrivateKey(privateKeyResource);
// // // // //                 System.out.println("🔑 [JwtUtil] Loaded RSA private key");
// // // // //                 useRsa = true;
// // // // //             }

// // // // //             // Fallback to HMAC mode if RSA keys not found
// // // // //             if (!useRsa) {
// // // // //                 if (hmacSecret == null || hmacSecret.isBlank() || hmacSecret.length() < 32) {
// // // // //                     throw new IllegalArgumentException(
// // // // //                             "❌ JWT_SECRET must be at least 32 characters long when RSA keys are not provided");
// // // // //                 }
// // // // //                 this.hmacKey = Keys.hmacShaKeyFor(hmacSecret.getBytes(StandardCharsets.UTF_8));
// // // // //                 System.out.println("⚡ [JwtUtil] Using HMAC (HS256) mode for JWT");
// // // // //             }

// // // // //             System.out.printf("✅ [JwtUtil] Initialized — Algorithm: %s%n", useRsa ? "RS256" : "HS256");

// // // // //         } catch (Exception e) {
// // // // //             throw new RuntimeException("Failed to initialize JwtUtil: " + e.getMessage(), e);
// // // // //         }
// // // // //     }

// // // // //     // ------------------------------------------------------------------------
// // // // //     // 🧩 Key Loaders
// // // // //     // ------------------------------------------------------------------------
// // // // //     private PrivateKey loadPrivateKey(Resource resource) throws Exception {
// // // // //         try (InputStream is = resource.getInputStream()) {
// // // // //             String keyPem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
// // // // //                     .replace("-----BEGIN PRIVATE KEY-----", "")
// // // // //                     .replace("-----END PRIVATE KEY-----", "")
// // // // //                     .replaceAll("\\s+", "");
// // // // //             byte[] decoded = Base64.getDecoder().decode(keyPem);
// // // // //             return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
// // // // //         }
// // // // //     }

// // // // //     private PublicKey loadPublicKey(Resource resource) throws Exception {
// // // // //         try (InputStream is = resource.getInputStream()) {
// // // // //             String keyPem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
// // // // //                     .replace("-----BEGIN PUBLIC KEY-----", "")
// // // // //                     .replace("-----END PUBLIC KEY-----", "")
// // // // //                     .replaceAll("\\s+", "");
// // // // //             byte[] decoded = Base64.getDecoder().decode(keyPem);
// // // // //             return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
// // // // //         }
// // // // //     }

// // // // //     // ------------------------------------------------------------------------
// // // // //     // 🔐 Token Generation
// // // // //     // ------------------------------------------------------------------------
// // // // //     public String generateAccessToken(Long userId, String username, Long sessionId, List<String> roles) {
// // // // //         Instant now = Instant.now();
// // // // //         Date expiry = Date.from(now.plusSeconds(accessTokenValiditySeconds));
    
// // // // //         Map<String, Object> claims = new HashMap<>();
// // // // //         claims.put("uid", userId);
// // // // //         claims.put("sid", sessionId);
// // // // //         claims.put("roles", roles);
// // // // //         claims.put("username", username); // ✅ add username
    
// // // // //         JwtBuilder builder = Jwts.builder()
// // // // //                 .setSubject(String.valueOf(userId))
// // // // //                 .addClaims(claims)
// // // // //                 .setIssuedAt(Date.from(now))
// // // // //                 .setExpiration(expiry);
    
// // // // //         signToken(builder);
// // // // //         return builder.compact();
// // // // //     }
    
// // // // //     public String generateRefreshToken(Long userId, String username, Long sessionId) {
// // // // //         Instant now = Instant.now();
// // // // //         Date expiry = Date.from(now.plusSeconds(refreshTokenValiditySeconds));
    
// // // // //         Map<String, Object> claims = new HashMap<>();
// // // // //         claims.put("uid", userId);
// // // // //         claims.put("sid", sessionId);
// // // // //         claims.put("username", username); // ✅ add username
    
// // // // //         JwtBuilder builder = Jwts.builder()
// // // // //                 .setSubject(String.valueOf(userId))
// // // // //                 .addClaims(claims)
// // // // //                 .setIssuedAt(Date.from(now))
// // // // //                 .setExpiration(expiry);
    
// // // // //         signToken(builder);
// // // // //         return builder.compact();
// // // // //     }
// // // // //     public String generateServiceToken() {
// // // // //         // 🧠 Used for internal service-to-service authentication
// // // // //         // Assigns a fixed "service" username and ROLE_SERVICE role
// // // // //         Long systemUserId = 0L;
// // // // //         Long systemSessionId = 0L;
// // // // //         String systemUsername = "service";
// // // // //         List<String> systemRoles = List.of("ROLE_SERVICE");
    
// // // // //         return generateAccessToken(systemUserId, systemUsername, systemSessionId, systemRoles);
// // // // //     }
    

// // // // //     public String getUsername(String token) {
// // // // //         Object username = parseToken(token).getBody().get("username");
// // // // //         return username != null ? username.toString() : null;
// // // // //     }
    

// // // // //     private void signToken(JwtBuilder builder) {
// // // // //         if (useRsa && privateKey != null) {
// // // // //             builder.signWith(privateKey, SignatureAlgorithm.RS256);
// // // // //         } else {
// // // // //             builder.signWith(hmacKey, SignatureAlgorithm.HS256);
// // // // //         }
// // // // //     }

// // // // //     // ------------------------------------------------------------------------
// // // // //     // 🔍 Token Parsing and Validation
// // // // //     // ------------------------------------------------------------------------
// // // // //     public Jws<Claims> parseToken(String token) {
// // // // //         JwtParserBuilder parser = Jwts.parserBuilder();
// // // // //         if (useRsa) {
// // // // //             parser.setSigningKey(publicKey);
// // // // //         } else {
// // // // //             parser.setSigningKey(hmacKey);
// // // // //         }
// // // // //         return parser.build().parseClaimsJws(token);
// // // // //     }

// // // // //     public boolean validateToken(String token) {
// // // // //         try {
// // // // //             parseToken(token);
// // // // //             return true;
// // // // //         } catch (JwtException e) {
// // // // //             return false;
// // // // //         }
// // // // //     }

// // // // //     // ------------------------------------------------------------------------
// // // // //     // 🧾 Accessor Helpers
// // // // //     // ------------------------------------------------------------------------
// // // // //     public String getUserId(String token) {
// // // // //         return parseToken(token).getBody().getSubject();
// // // // //     }

// // // // //     public Long getSessionId(String token) {
// // // // //         Object sid = parseToken(token).getBody().get("sid");
// // // // //         return sid != null ? Long.parseLong(sid.toString()) : null;
// // // // //     }

// // // // //     @SuppressWarnings("unchecked")
// // // // //     public List<String> getRoles(String token) {
// // // // //         Object roles = parseToken(token).getBody().get("roles");
// // // // //         return roles instanceof List ? (List<String>) roles : Collections.emptyList();
// // // // //     }

// // // // //     public long getAccessTokenValiditySeconds() {
// // // // //         return accessTokenValiditySeconds;
// // // // //     }

// // // // //     public long getRefreshTokenValiditySeconds() {
// // // // //         return refreshTokenValiditySeconds;
// // // // //     }

// // // // //     public boolean isUsingRsa() {
// // // // //         return useRsa;
// // // // //     }
// // // // // }


// // package com.example.common.util;

// // import io.jsonwebtoken.*;
// // import io.jsonwebtoken.security.Keys;
// // import jakarta.annotation.PostConstruct;
// // import org.springframework.beans.factory.annotation.Value;
// // import org.springframework.core.io.Resource;
// // import org.springframework.stereotype.Component;

// // import java.io.InputStream;
// // import java.nio.charset.StandardCharsets;
// // import java.nio.file.*;
// // import java.security.*;
// // import java.security.spec.PKCS8EncodedKeySpec;
// // import java.security.spec.X509EncodedKeySpec;
// // import java.time.Instant;
// // import java.util.*;

// // @Component
// // public class JwtUtil {

// //     // =============================================================
// //     // 🔧 CONFIG (Spring Boot OR Environment)
// //     // =============================================================

// //     @Value("${JWT_PRIVATE_KEY_PATH:#{null}}")
// //     private Resource privateKeyResource;

// //     @Value("${JWT_PUBLIC_KEY_PATH:#{null}}")
// //     private Resource publicKeyResource;

// //     @Value("${JWT_SECRET:#{null}}")
// //     private String hmacSecret;

// //     @Value("${JWT_ACCESS_TOKEN_VALIDITY_SECONDS:900}")
// //     private long accessSeconds;

// //     @Value("${JWT_REFRESH_TOKEN_VALIDITY_SECONDS:1209600}")
// //     private long refreshSeconds;

// //     // =============================================================
// //     // 🔐 INTERNAL KEY MATERIAL
// //     // =============================================================

// //     private PrivateKey privateKey;
// //     private PublicKey publicKey;
// //     private Key hmacKey;

// //     private boolean useRsa = false;
// //     private boolean isCloud;

// //     // Cloud fallback directory (systemd/EC2)
// //     private static final Path CLOUD_KEY_DIR = Paths.get("/opt/app/env/keys");

// //     // =============================================================
// //     // 🚀 INITIALIZATION
// //     // =============================================================
// //     @PostConstruct
// //     private void init() {
// //         detectEnvironment();

// //         System.out.println("--------------------------------------------------");
// //         System.out.println("🔐 [JwtUtil] Initializing JWT subsystem...");
// //         System.out.println("🌍 Environment: " + (isCloud ? "CLOUD (EC2/SSM)" : "LOCAL (DEV/IDE)"));
// //         System.out.println("--------------------------------------------------");

// //         try {
// //             loadRsaKeys();
// //             fallbackToHmac();

// //             System.out.println("--------------------------------------------------");
// //             System.out.printf("🎯 JWT Mode          : %s%n", useRsa ? "RSA (RS256)" : "HMAC (HS256)");
// //             System.out.printf("⏳ Access Validity   : %d sec%n", accessSeconds);
// //             System.out.printf("⏳ Refresh Validity  : %d sec%n", refreshSeconds);
// //             System.out.println("--------------------------------------------------\n");

// //         } catch (Exception e) {
// //             throw new RuntimeException("❌ Failed to initialize JwtUtil: " + e.getMessage(), e);
// //         }
// //     }

// //     // =============================================================
// //     // 🗺️ Cloud vs Local Detection
// //     // =============================================================
// //     private void detectEnvironment() {
// //         isCloud =
// //                 System.getenv("AWS_EXECUTION_ENV") != null ||
// //                 System.getenv("EC2_INSTANCE_ID") != null ||
// //                 System.getenv("ECS_CONTAINER_METADATA_URI") != null;

// //         System.out.println("🔍 [JwtUtil] Cloud detection → " + (isCloud ? "CLOUD mode" : "LOCAL mode"));
// //     }

// //     // =============================================================
// //     // 📌 Load RSA Keys (Local or Cloud)
// //     // =============================================================
// //     private void loadRsaKeys() throws Exception {
// //         boolean pubFound = false;
// //         boolean privFound = false;

// //         // ----------------- PUBLIC KEY -----------------
// //         if (publicKeyResource != null && publicKeyResource.exists()) {
// //             publicKey = loadPublicKey(publicKeyResource);
// //             System.out.println("🔒 [JwtUtil] Loaded PUBLIC key (LOCAL CLASSPATH)");
// //             pubFound = true;
// //         } else {
// //             Path pub = CLOUD_KEY_DIR.resolve("jwt-public.pem");
// //             if (Files.isReadable(pub)) {
// //                 publicKey = loadPublicKey(Files.readString(pub));
// //                 System.out.println("🔒 [JwtUtil] Loaded PUBLIC key (CLOUD FILESYSTEM)");
// //                 pubFound = true;
// //             }
// //         }

// //         // ----------------- PRIVATE KEY -----------------
// //         if (privateKeyResource != null && privateKeyResource.exists()) {
// //             privateKey = loadPrivateKey(privateKeyResource);
// //             System.out.println("🔑 [JwtUtil] Loaded PRIVATE key (LOCAL CLASSPATH)");
// //             privFound = true;
// //         } else {
// //             Path priv = CLOUD_KEY_DIR.resolve("jwt-private.pem");
// //             if (Files.isReadable(priv)) {
// //                 privateKey = loadPrivateKey(Files.readString(priv));
// //                 System.out.println("🔑 [JwtUtil] Loaded PRIVATE key (CLOUD FILESYSTEM)");
// //                 privFound = true;
// //             }
// //         }

// //         if (pubFound && privFound) {
// //             useRsa = true;
// //             System.out.println("✔ [JwtUtil] RSA mode ACTIVATED");
// //             System.out.println("   🔎 Public Fingerprint  : " + fingerprint(publicKey.getEncoded()));
// //             System.out.println("   🔎 Private Fingerprint : " + fingerprint(privateKey.getEncoded()));
// //         } else {
// //             System.out.println("⚠️  [JwtUtil] RSA keys missing → will fallback to HMAC");
// //         }
// //     }

// //     // =============================================================
// //     // 📌 HMAC Fallback (if RSA not present)
// //     // =============================================================
// //     private void fallbackToHmac() {

// //         if (useRsa) return;

// //         System.out.println("⚠️  [JwtUtil] Switching to HMAC mode (HS256)");

// //         if (hmacSecret == null || hmacSecret.length() < 32) {
// //             throw new IllegalArgumentException(
// //                     "❌ JWT_SECRET must be >= 32 chars when RSA keys are not provided."
// //             );
// //         }

// //         hmacKey = Keys.hmacShaKeyFor(hmacSecret.getBytes(StandardCharsets.UTF_8));

// //         System.out.println("🔑 [JwtUtil] Loaded HMAC Secret Key");
// //         System.out.println("   🔎 HMAC Fingerprint: " + fingerprint(hmacSecret.getBytes()));
// //     }

// //     // =============================================================
// //     // 🗝️ RSA Key Loaders
// //     // =============================================================
// //     private PublicKey loadPublicKey(Resource resource) throws Exception {
// //         return loadPublicKey(new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
// //     }

// //     private PublicKey loadPublicKey(String pem) throws Exception {
// //         String key = pem.replace("-----BEGIN PUBLIC KEY-----", "")
// //                 .replace("-----END PUBLIC KEY-----", "")
// //                 .replaceAll("\\s+", "");

// //         return KeyFactory.getInstance("RSA")
// //                 .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key)));
// //     }

// //     private PrivateKey loadPrivateKey(Resource resource) throws Exception {
// //         return loadPrivateKey(new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
// //     }

// //     private PrivateKey loadPrivateKey(String pem) throws Exception {
// //         String key = pem.replace("-----BEGIN PRIVATE KEY-----", "")
// //                 .replace("-----END PRIVATE KEY-----", "")
// //                 .replaceAll("\\s+", "");

// //         return KeyFactory.getInstance("RSA")
// //                 .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key)));
// //     }

// //     // =============================================================
// //     // 🔍 SHA-256 Fingerprint
// //     // =============================================================
// //     private String fingerprint(byte[] data) {
// //         try {
// //             return Base64.getEncoder().encodeToString(
// //                     MessageDigest.getInstance("SHA-256").digest(data)
// //             ).substring(0, 16);
// //         } catch (Exception e) {
// //             return "N/A";
// //         }
// //     }

// //     // =============================================================
// //     // 🔐 TOKEN GENERATION
// //     // =============================================================
// //     public String generateAccessToken(Long uid, String username, Long sid, List<String> roles) {

// //         Instant now = Instant.now();
// //         Date expiry = Date.from(now.plusSeconds(accessSeconds));

// //         Map<String, Object> claims = new HashMap<>();
// //         claims.put("uid", uid);
// //         claims.put("sid", sid);
// //         claims.put("roles", roles);
// //         claims.put("username", username);

// //         JwtBuilder builder = Jwts.builder()
// //                 .setSubject(uid.toString())
// //                 .addClaims(claims)
// //                 .setIssuedAt(Date.from(now))
// //                 .setExpiration(expiry);

// //         sign(builder);
// //         return builder.compact();
// //     }

// //     public String generateRefreshToken(Long uid, String username, Long sid) {

// //         Instant now = Instant.now();
// //         Date expiry = Date.from(now.plusSeconds(refreshSeconds));

// //         Map<String, Object> claims = Map.of(
// //                 "uid", uid,
// //                 "sid", sid,
// //                 "username", username
// //         );

// //         JwtBuilder builder = Jwts.builder()
// //                 .setSubject(uid.toString())
// //                 .addClaims(claims)
// //                 .setIssuedAt(Date.from(now))
// //                 .setExpiration(expiry);

// //         sign(builder);
// //         return builder.compact();
// //     }

// //     public String generateServiceToken() {
// //         return generateAccessToken(
// //                 0L, "service", 0L, List.of("ROLE_SERVICE")
// //         );
// //     }

// //     private void sign(JwtBuilder b) {
// //         if (useRsa) b.signWith(privateKey, SignatureAlgorithm.RS256);
// //         else b.signWith(hmacKey, SignatureAlgorithm.HS256);
// //     }

// //     // =============================================================
// //     // 🔍 PARSE & VALIDATE
// //     // =============================================================
// //     public Jws<Claims> parseToken(String token) {
// //         JwtParserBuilder builder = Jwts.parserBuilder();
// //         if (useRsa) builder.setSigningKey(publicKey);
// //         else builder.setSigningKey(hmacKey);
// //         return builder.build().parseClaimsJws(token);
// //     }

// //     public boolean validate(String token) {
// //         try {
// //             parseToken(token);
// //             return true;
// //         } catch (JwtException e) {
// //             System.out.println("❌ [JwtUtil] Token validation failed: " + e.getMessage());
// //             return false;
// //         }
// //     }

// //     // =============================================================
// //     // 🧾 ACCESSORS
// //     // =============================================================
// //     public String getUsername(String t) { return parseToken(t).getBody().get("username", String.class); }
// //     public String getUserId(String t)    { return parseToken(t).getBody().getSubject(); }
// //     public Long getSessionId(String t)   { return parseToken(t).getBody().get("sid", Long.class); }

// //     @SuppressWarnings("unchecked")
// //     public List<String> getRoles(String t) {
// //         Object roles = parseToken(t).getBody().get("roles");
// //         return roles instanceof List ? (List<String>) roles : Collections.emptyList();
// //     }

// //     public boolean isUsingRsa() { return useRsa; }
// //     public long getAccessSeconds() { return accessSeconds; }
// //     public long getRefreshSeconds() { return refreshSeconds; }
// // }
package com.example.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

/**
 * JwtUtil - Cloud-first key loading (CLOUD -> LOCAL CLASSPATH -> HMAC)
 *
 * Rewritten to preserve all original behavior and public API.
 * Changes:
 *  - Cloud filesystem (/opt/app/env/keys) is checked BEFORE classpath resources.
 *  - All logging/prints kept and adjusted to show which source the keys came from.
 *  - Behavior, method names and signatures unchanged.
 */
@Component
public class JwtUtil {

    // =============================================================
    // 🔧 CONFIG (Spring Boot OR Environment)
    // =============================================================

    @Value("${JWT_PRIVATE_KEY_PATH:#{null}}")
    private Resource privateKeyResource;

    @Value("${JWT_PUBLIC_KEY_PATH:#{null}}")
    private Resource publicKeyResource;

    @Value("${JWT_SECRET:#{null}}")
    private String hmacSecret;

    @Value("${JWT_ACCESS_TOKEN_VALIDITY_SECONDS:900}")
    private long accessSeconds;

    @Value("${JWT_REFRESH_TOKEN_VALIDITY_SECONDS:1209600}")
    private long refreshSeconds;

    // =============================================================
    // 🔐 INTERNAL KEY MATERIAL
    // =============================================================

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Key hmacKey;

    private boolean useRsa = false;
    private boolean isCloud;

    // Cloud fallback directory (systemd/EC2)
    private static final Path CLOUD_KEY_DIR = Paths.get("/opt/app/env/keys");

    // =============================================================
    // 🚀 INITIALIZATION
    // =============================================================
    @PostConstruct
    private void init() {
        detectEnvironment();

        System.out.println("--------------------------------------------------");
        System.out.println("🔐 [JwtUtil] Initializing JWT subsystem...");
        System.out.println("🌍 Environment: " + (isCloud ? "CLOUD (EC2/SSM)" : "LOCAL (DEV/IDE)"));
        System.out.println("--------------------------------------------------");

        try {
            loadRsaKeys();      // cloud-first inside method
            fallbackToHmac();   // unchanged behavior

            System.out.println("--------------------------------------------------");
            System.out.printf("🎯 JWT Mode          : %s%n", useRsa ? "RSA (RS256)" : "HMAC (HS256)");
            System.out.printf("⏳ Access Validity   : %d sec%n", accessSeconds);
            System.out.printf("⏳ Refresh Validity  : %d sec%n", refreshSeconds);
            System.out.println("--------------------------------------------------\n");

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to initialize JwtUtil: " + e.getMessage(), e);
        }
    }

    // =============================================================
    // 🗺️ Cloud vs Local Detection
    // =============================================================
    private void detectEnvironment() {
        isCloud =
                System.getenv("AWS_EXECUTION_ENV") != null ||
                System.getenv("EC2_INSTANCE_ID") != null ||
                System.getenv("ECS_CONTAINER_METADATA_URI") != null;

        System.out.println("🔍 [JwtUtil] Cloud detection → " + (isCloud ? "CLOUD mode" : "LOCAL mode"));
    }

    // =============================================================
    // 📌 Load RSA Keys (CLOUD -> LOCAL)
    // =============================================================
    private void loadRsaKeys() throws Exception {
        boolean pubFound = false;
        boolean privFound = false;

        // ----------------- PUBLIC KEY: CLOUD FIRST -----------------
        Path cloudPub = CLOUD_KEY_DIR.resolve("jwt-public.pem");
        if (Files.isReadable(cloudPub)) {
            String pem = Files.readString(cloudPub);
            publicKey = loadPublicKey(pem);
            System.out.println("🔒 [JwtUtil] Loaded PUBLIC key (CLOUD FILESYSTEM: " + cloudPub + ")");
            pubFound = true;
        } else if (publicKeyResource != null && publicKeyResource.exists()) { // LOCAL CLASSPATH next
            publicKey = loadPublicKey(publicKeyResource);
            System.out.println("🔒 [JwtUtil] Loaded PUBLIC key (LOCAL CLASSPATH)");
            pubFound = true;
        } else {
            // neither cloud nor classpath public key found
            System.out.println("ℹ️  [JwtUtil] PUBLIC key not found in CLOUD or LOCAL classpath");
        }

        // ----------------- PRIVATE KEY: CLOUD FIRST -----------------
        Path cloudPriv = CLOUD_KEY_DIR.resolve("jwt-private.pem");
        if (Files.isReadable(cloudPriv)) {
            String pem = Files.readString(cloudPriv);
            privateKey = loadPrivateKey(pem);
            System.out.println("🔑 [JwtUtil] Loaded PRIVATE key (CLOUD FILESYSTEM: " + cloudPriv + ")");
            privFound = true;
        } else if (privateKeyResource != null && privateKeyResource.exists()) { // LOCAL CLASSPATH next
            privateKey = loadPrivateKey(privateKeyResource);
            System.out.println("🔑 [JwtUtil] Loaded PRIVATE key (LOCAL CLASSPATH)");
            privFound = true;
        } else {
            // neither cloud nor classpath private key found
            System.out.println("ℹ️  [JwtUtil] PRIVATE key not found in CLOUD or LOCAL classpath");
        }

        if (pubFound && privFound) {
            useRsa = true;
            System.out.println("✔ [JwtUtil] RSA mode ACTIVATED");
            System.out.println("   🔎 Public Fingerprint  : " + fingerprint(publicKey.getEncoded()));
            System.out.println("   🔎 Private Fingerprint : " + fingerprint(privateKey.getEncoded()));
        } else {
            System.out.println("⚠️  [JwtUtil] RSA keys missing → will fallback to HMAC");
        }
    }

    // =============================================================
    // 📌 HMAC Fallback (if RSA not present)
    // =============================================================
    private void fallbackToHmac() {

        if (useRsa) return;

        System.out.println("⚠️  [JwtUtil] Switching to HMAC mode (HS256)");

        if (hmacSecret == null || hmacSecret.length() < 32) {
            throw new IllegalArgumentException(
                    "❌ JWT_SECRET must be >= 32 chars when RSA keys are not provided."
            );
        }

        hmacKey = Keys.hmacShaKeyFor(hmacSecret.getBytes(StandardCharsets.UTF_8));

        System.out.println("🔑 [JwtUtil] Loaded HMAC Secret Key");
        System.out.println("   🔎 HMAC Fingerprint: " + fingerprint(hmacSecret.getBytes()));
    }

    // =============================================================
    // 🗝️ RSA Key Loaders
    // =============================================================
    private PublicKey loadPublicKey(Resource resource) throws Exception {
        return loadPublicKey(new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    }

    private PublicKey loadPublicKey(String pem) throws Exception {
        String key = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key)));
    }

    private PrivateKey loadPrivateKey(Resource resource) throws Exception {
        return loadPrivateKey(new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    }

    private PrivateKey loadPrivateKey(String pem) throws Exception {
        String key = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key)));
    }

    // =============================================================
    // 🔍 SHA-256 Fingerprint
    // =============================================================
    private String fingerprint(byte[] data) {
        try {
            return Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(data)
            ).substring(0, 16);
        } catch (Exception e) {
            return "N/A";
        }
    }

    // =============================================================
    // 🔐 TOKEN GENERATION
    // =============================================================
    public String generateAccessToken(Long uid, String username, Long sid, List<String> roles) {

        Instant now = Instant.now();
        Date expiry = Date.from(now.plusSeconds(accessSeconds));

        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", uid);
        claims.put("sid", sid);
        claims.put("roles", roles);
        claims.put("username", username);

        JwtBuilder builder = Jwts.builder()
                .setSubject(uid.toString())
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(expiry);

        sign(builder);
        return builder.compact();
    }

    public String generateRefreshToken(Long uid, String username, Long sid) {

        Instant now = Instant.now();
        Date expiry = Date.from(now.plusSeconds(refreshSeconds));

        Map<String, Object> claims = Map.of(
                "uid", uid,
                "sid", sid,
                "username", username
        );

        JwtBuilder builder = Jwts.builder()
                .setSubject(uid.toString())
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(expiry);

        sign(builder);
        return builder.compact();
    }

    public String generateServiceToken() {
        return generateAccessToken(
                0L, "service", 0L, List.of("ROLE_SERVICE")
        );
    }

    private void sign(JwtBuilder b) {
        if (useRsa) b.signWith(privateKey, SignatureAlgorithm.RS256);
        else b.signWith(hmacKey, SignatureAlgorithm.HS256);
    }

    // =============================================================
    // 🔍 PARSE & VALIDATE
    // =============================================================
    public Jws<Claims> parseToken(String token) {
        JwtParserBuilder builder = Jwts.parserBuilder();
        if (useRsa) builder.setSigningKey(publicKey);
        else builder.setSigningKey(hmacKey);
        return builder.build().parseClaimsJws(token);
    }

    public boolean validate(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            System.out.println("❌ [JwtUtil] Token validation failed: " + e.getMessage());
            return false;
        }
    }

    // =============================================================
    // 🧾 ACCESSORS
    // =============================================================
    public String getUsername(String t) { return parseToken(t).getBody().get("username", String.class); }
    public String getUserId(String t)    { return parseToken(t).getBody().getSubject(); }
    public Long getSessionId(String t)   { return parseToken(t).getBody().get("sid", Long.class); }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String t) {
        Object roles = parseToken(t).getBody().get("roles");
        return roles instanceof List ? (List<String>) roles : Collections.emptyList();
    }

    public boolean isUsingRsa() { return useRsa; }
    public long getAccessSeconds() { return accessSeconds; }
    public long getRefreshSeconds() { return refreshSeconds; }
}


JAVA




cat > "$SRC_ROOT/security/JwtAuthFilter.java" <<'JAVA'

// // // // package com.example.common.security;

// // // // import io.jsonwebtoken.Claims;
// // // // import jakarta.servlet.FilterChain;
// // // // import jakarta.servlet.ServletException;
// // // // import jakarta.servlet.http.HttpServletRequest;
// // // // import jakarta.servlet.http.HttpServletResponse;
// // // // import org.slf4j.Logger;
// // // // import org.slf4j.LoggerFactory;
// // // // import org.springframework.http.HttpHeaders;
// // // // import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// // // // import org.springframework.security.core.authority.SimpleGrantedAuthority;
// // // // import org.springframework.security.core.context.SecurityContextHolder;
// // // // import org.springframework.stereotype.Component;
// // // // import org.springframework.web.filter.OncePerRequestFilter;

// // // // import java.io.IOException;
// // // // import java.util.Collection;
// // // // import java.util.Collections;
// // // // import java.util.List;

// // // // /**
// // // //  * ✅ JwtAuthFilter
// // // //  * 
// // // //  * Common authentication filter that validates incoming requests
// // // //  * using JWT tokens verified by JwtVerifier.
// // // //  */
// // // // @Component
// // // // public class JwtAuthFilter extends OncePerRequestFilter {

// // // //     private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

// // // //     private final JwtVerifier jwtVerifier;

// // // //     public JwtAuthFilter(JwtVerifier jwtVerifier) {
// // // //         this.jwtVerifier = jwtVerifier;
// // // //     }

// // // //     @Override
// // // //     protected void doFilterInternal(HttpServletRequest request,
// // // //                                     HttpServletResponse response,
// // // //                                     FilterChain filterChain)
// // // //             throws ServletException, IOException {
// // // //         String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

// // // //         if (authHeader == null || !authHeader.startsWith("Bearer ")) {
// // // //             filterChain.doFilter(request, response);
// // // //             return;
// // // //         }

// // // //         try {
// // // //             Claims claims = jwtVerifier.validateToken(authHeader);
// // // //             String username = claims.getSubject();
// // // //             List<String> roles = claims.get("roles", List.class);
// // // //             Collection<SimpleGrantedAuthority> authorities = roles != null
// // // //                     ? roles.stream().map(SimpleGrantedAuthority::new).toList()
// // // //                     : Collections.emptyList();

// // // //             UsernamePasswordAuthenticationToken authentication =
// // // //                     new UsernamePasswordAuthenticationToken(username, authHeader, authorities);
// // // //             SecurityContextHolder.getContext().setAuthentication(authentication);

// // // //             log.debug("✅ Authenticated user={} roles={}", username, roles);
// // // //         } catch (Exception e) {
// // // //             log.warn("⚠️ JWT validation failed: {}", e.getMessage());
// // // //         }

// // // //         filterChain.doFilter(request, response);
// // // //     }
// // // // }


package com.example.common.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * =======================================================================
 *  🔐 JwtAuthFilter — Enterprise-Grade Authentication Filter
 * =======================================================================
 *
 *  • Validates incoming requests using JwtVerifier.
 *  • Extracts user identity + roles.
 *  • Enriches Spring SecurityContext (ThreadLocal).
 *
 *  Diagnostics:
 *  • Logs token source
 *  • Logs roles extracted
 *  • Logs reason if validation fails
 *  • Ignores missing tokens (allows public endpoints)
 *
 * =======================================================================
 */

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtVerifier verifier;

    public JwtAuthFilter(JwtVerifier verifier) {
        this.verifier = verifier;
    }

    // ===================================================================
    //  🚦 MAIN FILTER LOGIC
    // ===================================================================
    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);

        // ---------------------------------------------------------------
        // 1️⃣ No token → continue without authentication
        // ---------------------------------------------------------------
        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("🔸 [JwtAuthFilter] No Authorization header for path={}", path);
            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7).trim();
        log.debug("🔍 [JwtAuthFilter] Bearer token detected for path={}", path);

        // ---------------------------------------------------------------
        // 2️⃣ Validate token via JwtVerifier
        // ---------------------------------------------------------------
        try {
            Claims claims = verifier.validate(token);

            String userId = claims.getSubject();
            Object rolesObj = claims.get("roles");
            List<String> roles = (rolesObj instanceof List<?>)
                    ? ((List<?>) rolesObj).stream().map(Object::toString).toList()
                    : Collections.emptyList();

            log.debug("✔ [JwtAuthFilter] Valid token → uid={} roles={}", userId, roles);

            // -----------------------------------------------------------
            // 3️⃣ Build Spring Security authentication
            // -----------------------------------------------------------
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, token, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ex) {
            log.warn("❌ [JwtAuthFilter] Token validation FAILED → reason={}", ex.getMessage());
            // DO NOT block request — let controller decide authorization
            // (Spring security config handles "authenticated()" paths)
        }

        // ---------------------------------------------------------------
        // 4️⃣ Continue filter chain
        // ---------------------------------------------------------------
        chain.doFilter(req, res);
    }
}



JAVA


cat > "$SRC_ROOT/security/JwtVerifier.java" <<'JAVA'

// // // // package com.example.common.security;

// // // // import io.jsonwebtoken.*;
// // // // import io.jsonwebtoken.security.Keys;
// // // // import jakarta.annotation.PostConstruct;
// // // // import org.slf4j.Logger;
// // // // import org.slf4j.LoggerFactory;
// // // // import org.springframework.beans.factory.annotation.Value;
// // // // import org.springframework.core.io.Resource;
// // // // import org.springframework.stereotype.Component;

// // // // import java.io.InputStream;
// // // // import java.nio.charset.StandardCharsets;
// // // // import java.security.Key;
// // // // import java.security.PublicKey;
// // // // import java.security.spec.X509EncodedKeySpec;
// // // // import java.security.KeyFactory;
// // // // import java.util.Base64;
// // // // import java.util.List;
// // // // import java.util.Collections;
// // // // import lombok.extern.slf4j.Slf4j;



// // // // /**
// // // //  * ✅ JwtVerifier
// // // //  * Lightweight read-only verifier for downstream microservices.
// // // //  * Loads ONLY the public key (RSA) or HMAC secret (for local dev fallback).
// // // //  */
// // // // @Slf4j
// // // // @Component
// // // // public class JwtVerifier {

    

// // // //     // 👇 add this line — this creates the logger explicitly
// // // //     private static final Logger log = LoggerFactory.getLogger(JwtVerifier.class);

// // // //     @Value("${JWT_PUBLIC_KEY_PATH:#{null}}")
// // // //     private Resource publicKeyResource;

// // // //     @Value("${JWT_SECRET:#{null}}")
// // // //     private String hmacSecret;

// // // //     private PublicKey publicKey;
// // // //     private Key hmacKey;
// // // //     private boolean useRsa = false;

// // // //     @PostConstruct
// // // //     private void init() {
// // // //         try {
// // // //             if (publicKeyResource != null && publicKeyResource.exists()) {
// // // //                 this.publicKey = loadPublicKey(publicKeyResource);
// // // //                 this.useRsa = true;
// // // //                 System.out.println("🔒 [JwtVerifier] Loaded RSA public key for JWT validation");
// // // //             } else if (hmacSecret != null && hmacSecret.length() >= 32) {
// // // //                 this.hmacKey = Keys.hmacShaKeyFor(hmacSecret.getBytes(StandardCharsets.UTF_8));
// // // //                 this.useRsa = false;
// // // //                 System.out.println("⚡ [JwtVerifier] Using HMAC (HS256) for local JWT validation");
// // // //             } else {
// // // //                 throw new IllegalArgumentException("❌ Must provide either JWT_PUBLIC_KEY_PATH or a valid JWT_SECRET (≥32 chars)");
// // // //             }
// // // //         } catch (Exception e) {
// // // //             throw new RuntimeException("Failed to initialize JwtVerifier: " + e.getMessage(), e);
// // // //         }
// // // //     }

// // // //     // ------------------------------------------------------------
// // // //     // ✅ Public verification methods
// // // //     // ------------------------------------------------------------
// // // //     public Jws<Claims> parseToken(String token) {
// // // //         JwtParserBuilder parser = Jwts.parserBuilder();
// // // //         if (useRsa) {
// // // //             parser.setSigningKey(publicKey);
// // // //         } else {
// // // //             parser.setSigningKey(hmacKey);
// // // //         }
// // // //         return parser.build().parseClaimsJws(token);
// // // //     }

// // // //     public Claims validateToken(String token) {
// // // //         if (token == null || token.isBlank()) {
// // // //             throw new JwtException("Missing JWT token");
// // // //         }

// // // //         String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;

// // // //         try {
// // // //             return Jwts.parserBuilder()
// // // //                     .setSigningKey(publicKey)
// // // //                     .build()
// // // //                     .parseClaimsJws(actualToken)
// // // //                     .getBody();
// // // //         } catch (JwtException ex) {
// // // //             log.error("❌ Invalid JWT: {}", ex.getMessage());
// // // //             throw ex;
// // // //         }
// // // //     }


// // // //     public String getUserId(String token) {
// // // //         return parseToken(token).getBody().getSubject();
// // // //     }

// // // //     @SuppressWarnings("unchecked")
// // // //     public List<String> getRoles(String token) {
// // // //         Object roles = parseToken(token).getBody().get("roles");
// // // //         return roles instanceof List ? (List<String>) roles : Collections.emptyList();
// // // //     }

// // // //     // ------------------------------------------------------------
// // // //     // 🔑 Load RSA public key
// // // //     // ------------------------------------------------------------
// // // //     private PublicKey loadPublicKey(Resource resource) throws Exception {
// // // //         try (InputStream is = resource.getInputStream()) {
// // // //             String keyPem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
// // // //                     .replace("-----BEGIN PUBLIC KEY-----", "")
// // // //                     .replace("-----END PUBLIC KEY-----", "")
// // // //                     .replaceAll("\\s+", "");
// // // //             byte[] decoded = Base64.getDecoder().decode(keyPem);
// // // //             return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
// // // //         }
// // // //     }
// // // // }



// // // // // package com.example.common.security;

// // // // // import io.jsonwebtoken.*;
// // // // // import io.jsonwebtoken.security.Keys;
// // // // // import jakarta.annotation.PostConstruct;
// // // // // import org.slf4j.Logger;
// // // // // import org.slf4j.LoggerFactory;
// // // // // import org.springframework.beans.factory.annotation.Value;
// // // // // import org.springframework.core.io.Resource;
// // // // // import org.springframework.stereotype.Component;

// // // // // import java.io.InputStream;
// // // // // import java.nio.charset.StandardCharsets;
// // // // // import java.security.Key;
// // // // // import java.security.PublicKey;
// // // // // import java.security.spec.X509EncodedKeySpec;
// // // // // import java.security.KeyFactory;
// // // // // import java.util.Base64;
// // // // // import java.util.List;
// // // // // import java.util.Collections;
// // // // // import lombok.extern.slf4j.Slf4j;



// // // // // /**
// // // // //  * ✅ JwtVerifier
// // // // //  * Lightweight read-only verifier for downstream microservices.
// // // // //  * Loads ONLY the public key (RSA) or HMAC secret (for local dev fallback).
// // // // //  */
// // // // // @Slf4j
// // // // // @Component
// // // // // public class JwtVerifier {

    

// // // // //     // 👇 add this line — this creates the logger explicitly
// // // // //     private static final Logger log = LoggerFactory.getLogger(JwtVerifier.class);

// // // // //     @Value("${JWT_PUBLIC_KEY_PATH:#{null}}")
// // // // //     private Resource publicKeyResource;

// // // // //     @Value("${JWT_SECRET:#{null}}")
// // // // //     private String hmacSecret;

// // // // //     private PublicKey publicKey;
// // // // //     private Key hmacKey;
// // // // //     private boolean useRsa = false;

// // // // //     @PostConstruct
// // // // //     private void init() {
// // // // //         try {
// // // // //             if (publicKeyResource != null && publicKeyResource.exists()) {
// // // // //                 this.publicKey = loadPublicKey(publicKeyResource);
// // // // //                 this.useRsa = true;
// // // // //                 System.out.println("🔒 [JwtVerifier] Loaded RSA public key for JWT validation");
// // // // //             } else if (hmacSecret != null && hmacSecret.length() >= 32) {
// // // // //                 this.hmacKey = Keys.hmacShaKeyFor(hmacSecret.getBytes(StandardCharsets.UTF_8));
// // // // //                 this.useRsa = false;
// // // // //                 System.out.println("⚡ [JwtVerifier] Using HMAC (HS256) for local JWT validation");
// // // // //             } else {
// // // // //                 throw new IllegalArgumentException("❌ Must provide either JWT_PUBLIC_KEY_PATH or a valid JWT_SECRET (≥32 chars)");
// // // // //             }
// // // // //         } catch (Exception e) {
// // // // //             throw new RuntimeException("Failed to initialize JwtVerifier: " + e.getMessage(), e);
// // // // //         }
// // // // //     }

// // // // //     // ------------------------------------------------------------
// // // // //     // ✅ Public verification methods
// // // // //     // ------------------------------------------------------------
// // // // //     public Jws<Claims> parseToken(String token) {
// // // // //         JwtParserBuilder parser = Jwts.parserBuilder();
// // // // //         if (useRsa) {
// // // // //             parser.setSigningKey(publicKey);
// // // // //         } else {
// // // // //             parser.setSigningKey(hmacKey);
// // // // //         }
// // // // //         return parser.build().parseClaimsJws(token);
// // // // //     }

// // // // //     public Claims validateToken(String token) {
// // // // //         if (token == null || token.isBlank()) {
// // // // //             throw new JwtException("Missing JWT token");
// // // // //         }

// // // // //         String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;

// // // // //         try {
// // // // //             return Jwts.parserBuilder()
// // // // //                     .setSigningKey(publicKey)
// // // // //                     .build()
// // // // //                     .parseClaimsJws(actualToken)
// // // // //                     .getBody();
// // // // //         } catch (JwtException ex) {
// // // // //             log.error("❌ Invalid JWT: {}", ex.getMessage());
// // // // //             throw ex;
// // // // //         }
// // // // //     }


// // // // //     public String getUserId(String token) {
// // // // //         return parseToken(token).getBody().getSubject();
// // // // //     }

// // // // //     @SuppressWarnings("unchecked")
// // // // //     public List<String> getRoles(String token) {
// // // // //         Object roles = parseToken(token).getBody().get("roles");
// // // // //         return roles instanceof List ? (List<String>) roles : Collections.emptyList();
// // // // //     }

// // // // //     // ------------------------------------------------------------
// // // // //     // 🔑 Load RSA public key
// // // // //     // ------------------------------------------------------------
// // // // //     private PublicKey loadPublicKey(Resource resource) throws Exception {
// // // // //         try (InputStream is = resource.getInputStream()) {
// // // // //             String keyPem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
// // // // //                     .replace("-----BEGIN PUBLIC KEY-----", "")
// // // // //                     .replace("-----END PUBLIC KEY-----", "")
// // // // //                     .replaceAll("\\s+", "");
// // // // //             byte[] decoded = Base64.getDecoder().decode(keyPem);
// // // // //             return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
// // // // //         }
// // // // //     }
// // // // // }



package com.example.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import org.slf4j.Logger;

@Component
public class JwtVerifier {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    // =============================================================
    // 🔧 CONFIG
    // =============================================================

    @Value("${JWT_PUBLIC_KEY_PATH:#{null}}")
    private Resource publicKeyResource;

    @Value("${JWT_SECRET:#{null}}")
    private String hmacSecret;

    private PublicKey rsaPublicKey;
    private Key hmacKey;

    private boolean useRsa = false;
    private boolean isCloud = false;

    private static final Path CLOUD_KEY_DIR = Paths.get("/opt/app/env/keys");

    // =============================================================
    // 🚀 INITIALIZATION
    // =============================================================
    @PostConstruct
    private void init() {

    
        detectEnvironment();

        log.info("--------------------------------------------------");
        log.info("🔎 Initializing JwtVerifier...");
        log.info("🌍 Environment: {}", (isCloud ? "CLOUD (EC2/SSM)" : "LOCAL (DEV/IDE)"));
        log.info("--------------------------------------------------");

        try {
            loadRsaPublicKey();
            fallbackToHmac();
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to initialize JwtVerifier: " + e.getMessage(), e);
        }

        log.info("--------------------------------------------------");
        log.info("✔ JwtVerifier initialized — Mode: {}", useRsa ? "RSA (RS256)" : "HMAC (HS256)");
        if (useRsa)
            log.info("🔎 RSA Public Fingerprint: {}", fingerprint(rsaPublicKey.getEncoded()));
        else
            log.info("🔎 HMAC Key Fingerprint: {}", fingerprint(hmacSecret.getBytes()));
        log.info("--------------------------------------------------\n");
    }

    // =============================================================
    // 🧭 Cloud / Local Detection
    // =============================================================
    private void detectEnvironment() {
        isCloud =
                System.getenv("AWS_EXECUTION_ENV") != null ||
                System.getenv("EC2_INSTANCE_ID") != null ||
                System.getenv("ECS_CONTAINER_METADATA_URI") != null;

        log.info("🔍 Cloud detection → {}", isCloud ? "CLOUD mode" : "LOCAL mode");
    }

    // =============================================================
    // 🔒 Load RSA Public Key (LOCAL + CLOUD)
    // =============================================================
    private void loadRsaPublicKey() throws Exception {

        // ---------------- LOCAL CLASSPATH ----------------
        if (publicKeyResource != null && publicKeyResource.exists()) {
            rsaPublicKey = loadPublicKeyFromResource(publicKeyResource);
            useRsa = true;
            log.info("🔒 Loaded RSA PUBLIC key (LOCAL CLASSPATH)");
            return;
        }

        // ---------------- CLOUD FILESYSTEM ----------------
        Path cloudPub = CLOUD_KEY_DIR.resolve("jwt-public.pem");
        if (Files.isReadable(cloudPub)) {
            rsaPublicKey = loadPublicKeyFromString(Files.readString(cloudPub));
            useRsa = true;
            log.info("🔒 Loaded RSA PUBLIC key (CLOUD FILESYSTEM)");
            return;
        }

        log.warn("⚠️ RSA PUBLIC key not found — HMAC fallback will be used.");
    }

    // =============================================================
    // ⚠️ HMAC Fallback (for LOCAL DEV or missing RSA)
    // =============================================================
    private void fallbackToHmac() {

        if (useRsa) return; // RSA already active

        if (hmacSecret == null || hmacSecret.length() < 32) {
            throw new IllegalArgumentException(
                    "❌ JWT_SECRET must be at least 32 characters if RSA keys are missing."
            );
        }

        hmacKey = Keys.hmacShaKeyFor(hmacSecret.getBytes(StandardCharsets.UTF_8));
        useRsa = false;

        log.info("⚡ Using HMAC (HS256) fallback mode");
    }

    // =============================================================
    // 🔍 Public Key Loaders
    // =============================================================
    private PublicKey loadPublicKeyFromResource(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            return loadPublicKeyFromString(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private PublicKey loadPublicKeyFromString(String pem) throws Exception {
        String cleaned = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] raw = Base64.getDecoder().decode(cleaned);

        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(raw));
    }

    // =============================================================
    // 🔍 SHA-256 Fingerprint
    // =============================================================
    private String fingerprint(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(bytes)).substring(0, 16);
        } catch (Exception e) {
            return "N/A";
        }
    }

    // =============================================================
    // 🔐 Token Parsing (read-only)
    // =============================================================
    public Jws<Claims> parseToken(String token) {
        JwtParserBuilder builder = Jwts.parserBuilder();

        if (useRsa) builder.setSigningKey(rsaPublicKey);
        else builder.setSigningKey(hmacKey);

        return builder.build().parseClaimsJws(token);
    }

    public Claims validate(String token) {
        if (token == null || token.isBlank())
            throw new JwtException("JWT missing");

        String actual = token.startsWith("Bearer ") ? token.substring(7) : token;

        try {
            return parseToken(actual).getBody();
        } catch (JwtException ex) {
            log.error("❌ Invalid JWT: {}", ex.getMessage());
            throw ex;
        }
    }

    // =============================================================
    // 🧾 Accessors
    // =============================================================
    public String getUserId(String token) {
        return parseToken(token).getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object obj = parseToken(token).getBody().get("roles");
        return (obj instanceof List) ? (List<String>) obj : Collections.emptyList();
    }

    public boolean isUsingRsa() { return useRsa; }
}




JAVA

###############################################
# 8) BaseEntity (for JPA)
###############################################
cat > "$SRC_ROOT/jpa/BaseEntity.java" <<'JAVA'
package com.example.common.jpa;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;


@MappedSuperclass
public abstract class BaseEntity {
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name = "active")
    private Boolean active = true;

    public String getCreatedBy(){return createdBy;}
    public void setCreatedBy(String v){this.createdBy=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
    public String getUpdatedBy(){return updatedBy;}
    public void setUpdatedBy(String v){this.updatedBy=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
    public void setUpdatedAt(LocalDateTime v){this.updatedAt=v;}
    public Boolean getActive(){return active;}
    public void setActive(Boolean v){this.active=v;}
}
JAVA

###############################################
# 9) GlobalExceptionHandler
###############################################
cat > "$SRC_ROOT/exception/GlobalExceptionHandler.java" <<'JAVA'

package com.example.common.exception;

import com.example.common.util.ResponseWrapper;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ Global reusable exception handler
 *  - Handles JWT errors
 *  - Validation errors
 *  - Generic server errors
 *  - Can be reused across all microservices
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // ------------------------------------------------------------------------
    // 🔐 JWT / Authorization errors
    // ------------------------------------------------------------------------
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> handleJwtException(JwtException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("error", "Invalid or expired token");
        details.put("message", ex.getMessage());
        details.put("timestamp", LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseWrapper<>(false, "Unauthorized", details));
    }

    // ------------------------------------------------------------------------
    // ⚠️ Validation errors (e.g. @Valid)
    // ------------------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity
                .badRequest()
                .body(new ResponseWrapper<>(false, "Validation failed", errors));
    }

    // ------------------------------------------------------------------------
    // ⚠️ Illegal arguments (e.g., invalid token header, null data, etc.)
    // ------------------------------------------------------------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("error", "Bad Request");
        details.put("message", ex.getMessage());
        details.put("timestamp", LocalDateTime.now());

        return ResponseEntity
                .badRequest()
                .body(new ResponseWrapper<>(false, "Invalid request", details));
    }

    // ------------------------------------------------------------------------
    // 💥 Catch-all fallback
    // ------------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> handleGenericException(Exception ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("error", ex.getClass().getSimpleName());
        details.put("message", ex.getMessage());
        details.put("timestamp", LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseWrapper<>(false, "Internal Server Error", details));
    }
}

JAVA


# ---------- 10) Exception classes ----------
cat > "$SRC_ROOT/exception/BadCredentialsException.java" <<'JAVA'
package com.example.common.exception;
public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String msg){ super(msg); }
}
JAVA

cat > "$SRC_ROOT/exception/LockedException.java" <<'JAVA'
package com.example.common.exception;
public class LockedException extends RuntimeException {
    public LockedException(String msg){ super(msg); }
}
JAVA

cat > "$SRC_ROOT/exception/CredentialsExpiredException.java" <<'JAVA'
package com.example.common.exception;
public class CredentialsExpiredException extends RuntimeException {
    public CredentialsExpiredException(String msg){ super(msg); }
}
JAVA

cat > "$SRC_ROOT/exception/DisabledException.java" <<'JAVA'
package com.example.common.exception;
public class DisabledException extends RuntimeException {
    public DisabledException(String msg){ super(msg); }
}
JAVA

echo "Created: Exception types"

###############################################
# 10) AesGcmEncryptor (for attribute encryption)
###############################################
cat > "$SRC_ROOT/util/AesGcmEncryptor.java" <<'JAVA'


// // // // package com.example.common.util;

// // // // import javax.crypto.Cipher;
// // // // import javax.crypto.spec.GCMParameterSpec;
// // // // import javax.crypto.spec.SecretKeySpec;
// // // // import java.security.SecureRandom;
// // // // import java.util.Base64;

// // // // /**
// // // //  * AES-GCM encrypt/decrypt helper. Uses 12-byte IV and 128-bit tag.
// // // //  * Not a production KMS. Use a proper key management in prod.
// // // //  */
// // // // public class AesGcmEncryptor {

// // // //     private static final String ALGO = "AES/GCM/NoPadding";
// // // //     private static final int IV_SIZE = 12;
// // // //     private static final int TAG_BITS = 128;

// // // //     private final byte[] key;

// // // //     public AesGcmEncryptor(byte[] key) {
// // // //         if (key == null || (key.length != 16 && key.length != 32)) {
// // // //             throw new IllegalArgumentException("Invalid AES key length (16 or 32 bytes)");
// // // //         }
// // // //         this.key = key;
// // // //     }

// // // //     public String encrypt(String plaintext) {
// // // //         try {
// // // //             byte[] iv = new byte[IV_SIZE];
// // // //             SecureRandom random = new SecureRandom();
// // // //             random.nextBytes(iv);
// // // //             Cipher cipher = Cipher.getInstance(ALGO);
// // // //             SecretKeySpec ks = new SecretKeySpec(key, "AES");
// // // //             GCMParameterSpec spec = new GCMParameterSpec(TAG_BITS, iv);
// // // //             cipher.init(Cipher.ENCRYPT_MODE, ks, spec);
// // // //             byte[] ct = cipher.doFinal(plaintext.getBytes());
// // // //             byte[] combined = new byte[iv.length + ct.length];
// // // //             System.arraycopy(iv, 0, combined, 0, iv.length);
// // // //             System.arraycopy(ct, 0, combined, iv.length, ct.length);
// // // //             return Base64.getEncoder().encodeToString(combined);
// // // //         } catch (Exception e) {
// // // //             throw new RuntimeException(e);
// // // //         }
// // // //     }

// // // //     public String decrypt(String cipherTextB64) {
// // // //         try {
// // // //             byte[] combined = Base64.getDecoder().decode(cipherTextB64);
// // // //             byte[] iv = new byte[IV_SIZE];
// // // //             System.arraycopy(combined, 0, iv, 0, iv.length);
// // // //             byte[] ct = new byte[combined.length - iv.length];
// // // //             System.arraycopy(combined, iv.length, ct, 0, ct.length);
// // // //             Cipher cipher = Cipher.getInstance(ALGO);
// // // //             SecretKeySpec ks = new SecretKeySpec(key, "AES");
// // // //             GCMParameterSpec spec = new GCMParameterSpec(TAG_BITS, iv);
// // // //             cipher.init(Cipher.DECRYPT_MODE, ks, spec);
// // // //             byte[] pt = cipher.doFinal(ct);
// // // //             return new String(pt);
// // // //         } catch (Exception e) {
// // // //             throw new RuntimeException(e);
// // // //         }
// // // //     }
// // // // }


package com.example.common.util;



import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.common.security.JwtAuthFilter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;



/**
 * ========================================================================
 * 🔐 AesGcmEncryptor — Secure AES-256-GCM utility
 * ========================================================================
 *
 * ✔ Uses 256-bit AES key (32 bytes)
 * ✔ Encrypts with random 96-bit IV
 * ✔ Stores: BASE64( IV || TAG || CIPHERTEXT )
 * ✔ Full diagnostic logging (same style as EncryptionKeyProvider)
 * ✔ Safe logs (never logs plaintext or raw key)
 * ✔ Cloud/Local independent (key already prepared by EncryptionKeyProvider)
 *
 * ========================================================================
 */

public class AesGcmEncryptor {

    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final int AES_KEY_LEN = 32;   // 256-bit key
    private static final int IV_LEN = 12;        // 96-bit recommended for GCM
    private static final int TAG_LEN = 128;      // GCM tag length (bits)

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    // --------------------------------------------------------------------
    // Constructor — key MUST be Base64(32 bytes)
    // --------------------------------------------------------------------
    public AesGcmEncryptor(String base64Key) {

        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalArgumentException("❌ AES key missing. Provide Base64(32-byte) key.");
        }

        byte[] keyBytes = Base64.getDecoder().decode(base64Key);

        if (keyBytes.length != AES_KEY_LEN) {
            throw new IllegalArgumentException(
                    "❌ AES-256 key must be exactly 32 bytes. Got " + keyBytes.length);
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");

        log.info("🔐 [AesGcmEncryptor] AES-256 key loaded (cloud/local). Fingerprint={}",
                fingerprint(keyBytes));
    }

    // --------------------------------------------------------------------
    // Encrypt → Base64( IV || TAG || CIPHERTEXT )
    // --------------------------------------------------------------------
    public String encrypt(String plaintext) {

        if (plaintext == null) return null;

        try {
            byte[] iv = new byte[IV_LEN];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LEN, iv));

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV + encrypted into single buffer
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            String output = Base64.getEncoder().encodeToString(buffer.array());

            log.debug("🔒 [Encrypt] OK — plaintextLength={} cipherLength={}",
                    plaintext.length(), output.length());

            return output;

        } catch (Exception e) {
            log.error("❌ [Encrypt] Failed: {}", e.getMessage(), e);
            throw new RuntimeException("AES encryption failed: " + e.getMessage(), e);
        }
    }

    // --------------------------------------------------------------------
    // Decrypt → plaintext
    // --------------------------------------------------------------------
    public String decrypt(String base64Cipher) {

        if (base64Cipher == null) return null;

        try {
            byte[] decoded = Base64.getDecoder().decode(base64Cipher);

            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_LEN];
            buffer.get(iv);

            byte[] ciphertextWithTag = new byte[buffer.remaining()];
            buffer.get(ciphertextWithTag);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LEN, iv));

            String plaintext = new String(cipher.doFinal(ciphertextWithTag), StandardCharsets.UTF_8);

            log.debug("🔓 [Decrypt] OK — outputLength={}", plaintext.length());

            return plaintext;

        } catch (Exception e) {
            log.error("❌ [Decrypt] Failed: {}", e.getMessage());
            throw new RuntimeException("AES decryption failed: " + e.getMessage(), e);
        }
    }

    // --------------------------------------------------------------------
    // Safe key fingerprint — no sensitive exposure
    // --------------------------------------------------------------------
    private String fingerprint(byte[] keyBytes) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyBytes);
            return Base64.getEncoder().encodeToString(hash).substring(0, 16);
        } catch (Exception e) {
            return "N/A";
        }
    }
}

JAVA

###############################################
# 11) JpaAttributeEncryptor (JPA Converter)
###############################################
cat > "$SRC_ROOT/converter/JpaAttributeEncryptor.java" <<'JAVA'
// package com.example.common.converter;

// import com.example.common.util.AesGcmEncryptor;
// import jakarta.persistence.AttributeConverter;
// import jakarta.persistence.Converter;
// import java.nio.charset.StandardCharsets;

// import java.util.Base64;

// /**
//  * JPA attribute converter using AesGcmEncryptor.
//  * Reads key from env ENCRYPTION_KEY (expects 16 or 32 bytes).
//  */
// @Converter
// public class JpaAttributeEncryptor implements AttributeConverter<String, String> {

    
//     private static AesGcmEncryptor encryptor;

//     // Initialize the encryptor once at application startup
//     public static void init(String base64Key) {
//         byte[] key = Base64.getDecoder().decode(base64Key);
//         encryptor = new AesGcmEncryptor(key);
//     }

//     public JpaAttributeEncryptor() {
//         String k = System.getenv().getOrDefault("ENCRYPTION_KEY", "0123456789abcdef"); // default 16 bytes
//         byte[] key = k.getBytes(StandardCharsets.UTF_8);
//         this.encryptor = new AesGcmEncryptor(key);
//     }

//     @Override
//     public String convertToDatabaseColumn(String attribute) {
//         if (attribute == null) return null;
//         return encryptor.encrypt(attribute);
//     }

//     @Override
//     public String convertToEntityAttribute(String dbData) {
//         if (dbData == null) return null;
//         return encryptor.decrypt(dbData);
//     }
// }



package com.example.common.converter;

import com.example.common.security.JwtAuthFilter;
import com.example.common.util.AesGcmEncryptor;
import com.example.common.util.EncryptionKeyProvider;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Base64;
import java.security.MessageDigest;

/**
 * =======================================================================
 * 🔐 JpaAttributeEncryptor — Enterprise AES-GCM Converter
 * =======================================================================
 *
 *  • Uses AES-GCM 256-bit encryption for JPA fields.
 *  • Key is provided by EncryptionKeyProvider (local + cloud aware).
 *  • Includes detailed diagnostics WITHOUT exposing sensitive data.
 *
 * =======================================================================
 */

@Converter
public class JpaAttributeEncryptor implements AttributeConverter<String, String> {

    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final AesGcmEncryptor aes;

    public JpaAttributeEncryptor() {
        log.info("---------------------------------------------------------------");
        log.info("🔐 [JpaAttributeEncryptor] Initializing AES-GCM JPA Converter");
        log.info("---------------------------------------------------------------");

        // Fetch Base64-encoded normalized 32-byte AES key
        String base64Key = EncryptionKeyProvider.getNormalizedBase64Key();

        log.info("🔑 [JpaAttributeEncryptor] AES-256 key loaded. Fingerprint={}",
                safeKeyFingerprint(base64Key));

        this.aes = new AesGcmEncryptor(base64Key);

        log.info("✔ [JpaAttributeEncryptor] Ready — AES-GCM 256-bit enabled");
        log.info("---------------------------------------------------------------\n");
    }

    // ===================================================================
    //  🔒 Encrypt before storing to DB
    // ===================================================================
    @Override
    public String convertToDatabaseColumn(String plain) {
        if (plain == null) return null;

        log.debug("🔒 [Encrypt] Encrypting attribute (length={})", plain.length());

        try {
            String encrypted = aes.encrypt(plain);
            log.debug("🔒 [Encrypt] Completed → ciphertext length={}", encrypted.length());
            return encrypted;

        } catch (Exception ex) {
            log.error("❌ [Encrypt] Failed. Cause={}", ex.getMessage());
            throw new IllegalStateException(
                    "Encryption failed inside JpaAttributeEncryptor: " + ex.getMessage(), ex
            );
        }
    }

    // ===================================================================
    //  🔓 Decrypt after reading from DB
    // ===================================================================
    @Override
    public String convertToEntityAttribute(String cipher) {
        if (cipher == null) return null;

        log.debug("🔓 [Decrypt] Decrypting DB column (length={})", cipher.length());

        try {
            String decrypted = aes.decrypt(cipher);
            log.debug("🔓 [Decrypt] Completed (result-length={})", decrypted.length());
            return decrypted;

        } catch (Exception ex) {
            log.error("❌ [Decrypt] Failed. Cause={}", ex.getMessage());
            throw new IllegalStateException(
                    "Decryption failed inside JpaAttributeEncryptor: " + ex.getMessage(), ex
            );
        }
    }

    // ===================================================================
    //  🔏 Safe fingerprint for debugging (NO key leakage)
    // ===================================================================
    private String safeKeyFingerprint(String base64Key) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha.digest(base64Key.getBytes());
            return Base64.getEncoder().encodeToString(digest).substring(0, 16);
        } catch (Exception ignored) {
            return "N/A";
        }
    }
}


JAVA

###############################################
# 12) CorrelationIdFilter (propagate X-Correlation-Id)
###############################################
cat > "$SRC_ROOT/filter/CorrelationIdFilter.java" <<'JAVA'
package com.example.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

/**
 * Ensures each request has a Correlation ID header and makes it available downstream.
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String id = request.getHeader(HEADER);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        response.setHeader(HEADER, id);
        filterChain.doFilter(request, response);
    }
}
JAVA

###############################################
# 13) Simple Audit POJO (example)
###############################################
cat > "$SRC_ROOT/jpa/AuditRecord.java" <<'JAVA'
package com.example.common.jpa;

import java.time.LocalDateTime;

/**
 * Lightweight audit record POJO - for projects that want to store audit logs.
 */
public class AuditRecord {
    private LocalDateTime timestamp;
    private String username;
    private String ip;
    private String userAgent;
    private String url;
    private String method;
    private String details;

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
JAVA

###############################################
# 14) MainClass (module quick-run)
###############################################
cat > "$SRC_ROOT/MainClass.java" <<'JAVA'
package com.example.common;

public class MainClass {
    public static void main(String[] args) {
        System.out.println("✅ common-service module ready.");
    }
}
JAVA




cat > "$SRC_ROOT/service/SafeNotificationHelper.java" <<'JAVA'

package com.example.common.service;

import com.example.common.client.NotificationClient;
import com.example.common.entity.NotificationRetryLog;
import com.example.common.repository.NotificationRetryLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ✅ SafeNotificationHelper (v2)
 *
 * - Retries transient notification failures
 * - Persists failed notifications to DB for retry
 * - Optionally publishes to Kafka (future extension)
 */
@Component
@ConditionalOnProperty(name = "common.notification.enabled", havingValue = "true", matchIfMissing = true)
public class SafeNotificationHelper {

    private static final int MAX_RETRIES = 3;

    @Autowired
    private NotificationHelper notificationHelper;

    @Autowired
    private NotificationRetryLogRepository retryLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private NotificationClient notificationClient; // Optional, used internally

    /**
     * ✅ Safe send with retry and persistence fallback
     */
    public void safeNotify(String bearertoken,
                           Long userId,
                           String username,
                           String email,
                           String mobile,
                           String channel,
                           String templateCode,
                           Map<String, Object> placeholders,
                           String projectType) {

        Map<String, Object> audit = notificationHelper.buildAudit();

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                notificationHelper.sendNotification(bearertoken,
                        userId, username, email, mobile,
                        channel, templateCode, placeholders, audit, projectType
                );
                System.out.printf("📨 Notification via %s succeeded on attempt %d for %s%n",
                        channel, attempt, username);
                return;
            } catch (Exception ex) {
                System.err.printf("⚠️ Attempt %d/%d failed via %s for %s: %s%n",
                        attempt, MAX_RETRIES, channel, username, ex.getMessage());

                if (attempt == MAX_RETRIES) {
                    persistFailure(userId, username, channel, templateCode, placeholders, ex, attempt);
                }

                sleep(500);
            }
        }
    }

    /**
     * 🧵 Async variant (non-blocking)
     */
    @Async
    public void safeNotifyAsync(String bearertoken,
                                Long userId,
                                String username,
                                String email,
                                String mobile,
                                String channel,
                                String templateCode,
                                Map<String, Object> placeholders,
                                String projectType) {
        safeNotify(bearertoken,userId, username, email, mobile, channel, templateCode, placeholders, projectType);
    }

    /**
     * 🧠 Persist failed attempt for later retry (DB or Kafka)
     */
    private void persistFailure(Long userId,
                                String username,
                                String channel,
                                String templateCode,
                                Map<String, Object> payload,
                                Exception ex,
                                int retryCount) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            NotificationRetryLog log = new NotificationRetryLog(
                    userId, username, channel, templateCode, payloadJson, ex.getMessage(), retryCount
            );
            retryLogRepository.save(log);
            System.err.printf("💾 Stored failed notification for retry (%s, %s)%n", username, channel);
        } catch (Exception e) {
            System.err.printf("❌ Could not persist failed notification: %s%n", e.getMessage());
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}

JAVA


# # # # cat > "$SRC_ROOT/service/NotificationRetryProcessor.java" <<'JAVA'

# # # # package com.example.common.service;

# # # # import com.example.common.entity.NotificationRetryLog;
# # # # import com.example.common.repository.NotificationRetryLogRepository;
# # # # import org.springframework.beans.factory.annotation.Autowired;
# # # # import org.springframework.scheduling.annotation.Scheduled;
# # # # import org.springframework.stereotype.Component;

# # # # import java.util.List;
# # # # import java.util.Map;

# # # # /**
# # # #  * ✅ Periodically reattempts failed notifications from DB
# # # #  */
# # # # @Component
# # # # public class NotificationRetryProcessor {

# # # #     @Autowired
# # # #     private NotificationRetryLogRepository retryLogRepository;

# # # #     @Autowired
# # # #     private NotificationHelper notificationHelper;

# # # #     @Scheduled(fixedDelay = 60000) // every 1 min
# # # #     public void retryFailedNotifications() {
# # # #         List<NotificationRetryLog> failedList = retryLogRepository.findByProcessedFalseOrderByCreatedAtAsc();

# # # #         for (NotificationRetryLog log : failedList) {
# # # #             try {
# # # #                 System.out.printf("🔁 Retrying notification (id=%d, channel=%s)%n", log.getId(), log.getChannel());
# # # #                 // Convert payload JSON back to Map
# # # #                 Map<String, Object> placeholders = Map.of(); // Simplify or deserialize JSON
# # # #                 notificationHelper.sendNotification(
# # # #                         log.getUserId(),
# # # #                         log.getUsername(),
# # # #                         null,
# # # #                         null,
# # # #                         log.getChannel(),
# # # #                         log.getTemplateCode(),
# # # #                         placeholders,
# # # #                         Map.of(),
# # # #                         "RETRY_WORKER"
# # # #                 );
# # # #                 log.setProcessed(true);
# # # #                 retryLogRepository.save(log);
# # # #                 System.out.printf("✅ Retried successfully for user %s%n", log.getUsername());
# # # #             } catch (Exception ex) {
# # # #                 System.err.printf("⚠️ Retry failed for user %s: %s%n", log.getUsername(), ex.getMessage());
# # # #             }
# # # #         }
# # # #     }
# # # # }


# # # # JAVA

cat > "$SRC_ROOT/service/NotificationHelper.java" <<'JAVA'

package com.example.common.service;

import com.example.common.client.NotificationClient;
import com.example.common.util.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ✅ Shared notification utility for all microservices.
 * 
 * - Centralizes logic for building and sending notifications.
 * - Uses AuthTokenService for secure inter-service authentication.
 * - Safe to include in any service; no cyclic dependencies on auth-service.
 */
@Component
@ConditionalOnProperty(name = "common.notification.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationHelper {

    private static final Logger log = LoggerFactory.getLogger(NotificationHelper.class);

    private final NotificationClient notificationClient;
    private final AuthTokenService authTokenService;

    public NotificationHelper(@Nullable NotificationClient notificationClient,
                              @Nullable AuthTokenService authTokenService) {
        this.notificationClient = notificationClient;
        this.authTokenService = authTokenService;
    }

    /**
     * Send a notification using the Notification Service.
     */
    public void sendNotification(String bearerToken,
                                 Long userId,
                                 String username,
                                 String email,
                                 String mobile,
                                 String channel,
                                 String templateCode,
                                 Map<String, Object> placeholders,
                                 Map<String, Object> audit,
                                 String projectType) {
        if (notificationClient == null) {
            log.warn("⚠️ NotificationClient not available — skipping notification ({} for {})",
                     templateCode, username);
            return;
        }

        // String bearerToken = resolveToken();

        Map<String, Object> req = new LinkedHashMap<>();
        req.put("userId", userId);
        req.put("username", username);
        req.put("mobile", mobile);
        req.put("email", email);
        if (channel == null) {
            channel = "INAPP";
        }
        
        req.put("channel", channel.toUpperCase());
        
        if (!templateCode.contains("_" + channel.toUpperCase())) {
            templateCode = templateCode + "_" + channel.toUpperCase();
        }
        
        req.put("templateCode", templateCode);
        req.put("placeholders", placeholders == null ? Map.of() : placeholders);
        req.put("audit", audit == null ? buildAudit() : audit);
        req.put("projectType", projectType);

        try {
            notificationClient.sendNotification(req, bearerToken);
            log.info("📤 Sent notification via {} for user {}", channel, username);
        } catch (Exception e) {
            log.error("❌ Failed to send notification via {} for user {}: {}", channel, username, e.getMessage(), e);
            throw new RuntimeException("Failed to send notification: " + e.getMessage(), e);
        }
    }

    /**
     * Async variant for non-blocking sends.
     */
    @Async
    public void sendNotificationAsync(String bearertoken,
                                      Long userId,
                                      String username,
                                      String email,
                                      String mobile,
                                      String channel,
                                      String templateCode,
                                      Map<String, Object> placeholders,
                                      Map<String, Object> audit,
                                      String projectType) {
        sendNotification(bearertoken,userId, username, email, mobile, channel, templateCode, placeholders, audit, projectType);
    }

    /**
     * Builds standard request audit metadata.
     */
    public Map<String, Object> buildAudit() {
        Map<String, Object> audit = new LinkedHashMap<>();
        try {
            audit.put("ipAddress", RequestContext.getIp());
            audit.put("userAgent", RequestContext.getUserAgent());
            audit.put("url", RequestContext.getUrl());
            audit.put("httpMethod", RequestContext.getMethod());
            audit.put("sessionId", RequestContext.getSessionId());
        } catch (Exception e) {
            log.debug("Audit context not available: {}", e.getMessage());
        }
        return audit;
    }

    /**
     * Resolves a Bearer token via AuthTokenService.
     * Falls back to null if no token can be obtained.
     */
    private String resolveToken() {
        try {
            String token = authTokenService != null ? authTokenService.getAccessToken() : null;
            if (token != null && !token.isBlank()) {
                return token.startsWith("Bearer ") ? token : "Bearer " + token;
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not resolve service token: {}", e.getMessage());
        }
        return null;
    }
}

JAVA


cat > "$SRC_ROOT/entity/NotificationRetryLog.java" <<'JAVA'

package com.example.common.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ✅ Notification Retry Log Entity
 * Stores failed notification attempts for later reprocessing.
 * Used by NotificationRetryProcessor to periodically retry sending.
 */
@Entity
@Table(name = "notification_retry_log")
public class NotificationRetryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String username;
    private String channel;
    private String templateCode;

    @Column(columnDefinition = "TEXT")
    private String payloadJson; // serialized placeholders/payload

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private int retryCount;
    private boolean processed = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastAttemptAt = LocalDateTime.now();

    // ------------------------------------------------------------------------
    // ✅ Constructors
    // ------------------------------------------------------------------------

    public NotificationRetryLog() {}

    public NotificationRetryLog(Long userId,
                                String username,
                                String channel,
                                String templateCode,
                                String payloadJson,
                                String errorMessage,
                                int retryCount) {
        this.userId = userId;
        this.username = username;
        this.channel = channel;
        this.templateCode = templateCode;
        this.payloadJson = payloadJson;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.createdAt = LocalDateTime.now();
        this.lastAttemptAt = LocalDateTime.now();
        this.processed = false;
    }

    // ✅ Getters and setters omitted for brevity
    // (Include all fields with Lombok @Data if you use Lombok)

    // ------------------------------------------------------------------------
    // 🧩 Getters and Setters (if not using Lombok)
    // ------------------------------------------------------------------------

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getChannel() { return channel; }

    public void setChannel(String channel) { this.channel = channel; }

    public String getTemplateCode() { return templateCode; }

    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public String getPayloadJson() { return payloadJson; }

    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public String getErrorMessage() { return errorMessage; }

    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public int getRetryCount() { return retryCount; }

    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public boolean isProcessed() { return processed; }

    public void setProcessed(boolean processed) { this.processed = processed; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastAttemptAt() { return lastAttemptAt; }

    public void setLastAttemptAt(LocalDateTime lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
}


JAVA



cat > "$SRC_ROOT/repository/NotificationRetryLogRepository.java" <<'JAVA'
package com.example.common.repository;

import com.example.common.entity.NotificationRetryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRetryLogRepository extends JpaRepository<NotificationRetryLog, Long> {

    List<NotificationRetryLog> findByProcessedFalseOrderByCreatedAtAsc();
}


JAVA


# # # # ###############################################
# # # # # 15) ENV sample files
# # # # ###############################################



# # # # # ---------------------------
# # # # # Create .env.auth and symlink/copy as .env
# # # # # Creates environment files for auth / asset / notification services under common-service/src/main/resources/env
# # # # # Usage:
# # # # #   COMMON_DIR=/path/to/common-service ./setup-envs.sh
# # # # #
# # # # # Notes:
# # # # #  - If AUTH_ENC_KEY / AUTH_HMAC_KEY are already set in the environment, they will be reused.
# # # # #  - Generated files are created with mode 600. Do NOT commit them to git.

# # # # # Allow overriding location via env; default to ./common-service
# # # # COMMON_DIR="${COMMON_DIR:-$(pwd)/common-service}"
# # # # ENV_DIR="$COMMON_DIR/src/main/resources/env"
# # # # mkdir -p "$ENV_DIR"

# # # # AUTH_ENV_FILE="$ENV_DIR/.env.auth"
# # # # NOTIFICATION_ENV_FILE="$ENV_DIR/.env.notification"
# # # # ASSET_ENV_FILE="$ENV_DIR/.env.asset"
# # # # DOTENV_FILE="$ENV_DIR/.env"

# # # # # Ports (override via env if desired)
# # # # AUTH_SERVER_PORT="${AUTH_SERVER_PORT:-8081}"
# # # # NOTIFICATION_SERVER_PORT="${NOTIFICATION_SERVER_PORT:-8082}"
# # # # ASSET_SERVER_PORT="${ASSET_SERVER_PORT:-8083}"

# # # # # Allow users to predefine keys in the environment; otherwise generate them
# # # # AUTH_ENC_KEY="${AUTH_ENC_KEY:-}"
# # # # AUTH_HMAC_KEY="${AUTH_HMAC_KEY:-}"

# # # # # Helper to generate base64 keys (32 bytes)
# # # # generate_key() {
# # # #   if command -v openssl >/dev/null 2>&1; then
# # # #     openssl rand -base64 32
# # # #   else
# # # #     # fallback (less ideal)
# # # #     base64 /dev/urandom | head -c 44
# # # #   fi
# # # # }

# # # # # Create .env.auth
# # # # if [ -f "$AUTH_ENV_FILE" ]; then
# # # #   echo "Skipping: $AUTH_ENV_FILE already exists."
# # # # else
# # # #   echo "Generating $AUTH_ENV_FILE with encryption keys and DB credentials..."

# # # #   # generate only if not provided
# # # #   : "${AUTH_ENC_KEY:=$(generate_key)}"
# # # #   : "${AUTH_HMAC_KEY:=$(generate_key)}"
# # # #   : "${JWT_SECRET:="SuperSecureJWTKey_ChangeThisForProduction_1234567890!"}"

# # # #   # Do not echo the full keys; keep them private. Provide short preview only.
# # # #   KEY_PREVIEW_ENC="${AUTH_ENC_KEY:0:6}..."
# # # #   KEY_PREVIEW_HMAC="${AUTH_HMAC_KEY:0:6}..."

# # # # # Common JWT configuration (used by common-service)
# # # # JWT_PRIVATE_KEY_PATH=classpath:keys/jwt-private.pem
# # # # JWT_PUBLIC_KEY_PATH=classpath:keys/jwt-public.pem
# # # # JWT_SECRET=$JWT_SECRET
# # # # JWT_ACCESS_TOKEN_VALIDITY_SECONDS=900
# # # # JWT_REFRESH_TOKEN_VALIDITY_SECONDS=1209600

# # # #   cat > "$AUTH_ENV_FILE" <<EOF
# # # # AUTH_SERVICE_NAME=auth-service
# # # # AUTH_SERVER_PORT=${AUTH_SERVER_PORT}
# # # # JWT_PRIVATE_KEY_PATH=${JWT_PRIVATE_KEY_PATH}
# # # # JWT_PUBLIC_KEY_PATH=${JWT_PUBLIC_KEY_PATH}
# # # # AUTH_ENC_KEY=${AUTH_ENC_KEY}
# # # # AUTH_HMAC_KEY=${AUTH_HMAC_KEY}
# # # # DB_USERNAME=root
# # # # DB_PASSWORD=Snmysql@1110
# # # # ACCESS_TOKEN=change_this_token
# # # # JWT_SECRET=${JWT_SECRET}
# # # # ENCRYPTION_KEY=${AUTH_ENC_KEY}
# # # # FEIGN_ACCESS_TOKEN=

# # # # EOF

# # # #   chmod 600 "$AUTH_ENV_FILE"
# # # #   echo "Created $AUTH_ENV_FILE (keys preview: ENC=${KEY_PREVIEW_ENC} HMAC=${KEY_PREVIEW_HMAC}). DO NOT commit to git."
# # # # fi

# # # # # Ensure .env points to .env.auth (symlink preferred)
# # # # if [ -e "$DOTENV_FILE" ]; then
# # # #   echo "$DOTENV_FILE already exists; leaving as-is."
# # # # else
# # # #   # attempt to create symlink, fallback to copy
# # # #   if ln -sfn "$(basename "$AUTH_ENV_FILE")" "$DOTENV_FILE" 2>/dev/null; then
# # # #     # symlink uses relative name; ensure link target correct by creating inside same dir

# # # #     echo "Created symlink $DOTENV_FILE -> $(basename "$AUTH_ENV_FILE")"
# # # #   else
# # # #     cp "$AUTH_ENV_FILE" "$DOTENV_FILE"
# # # #     chmod 600 "$DOTENV_FILE"
# # # #     echo "Copied $AUTH_ENV_FILE -> $DOTENV_FILE"
# # # #   fi
# # # # fi

# # # # # Create .env.asset
# # # # if [ -f "$ASSET_ENV_FILE" ]; then
# # # #   echo "Skipping: $ASSET_ENV_FILE already exists."
# # # # else
# # # #   echo "Generating $ASSET_ENV_FILE..."
# # # # #   # Reuse the same keys generated above if present
# # # # #   : "${AUTH_ENC_KEY:=$(generate_key)}"
# # # # #   : "${AUTH_HMAC_KEY:=$(generate_key)}"

# # # #   cat > "$ASSET_ENV_FILE" <<EOF
# # # # ASSET_SERVICE_NAME=asset-service
# # # # ASSET_SERVER_PORT=${ASSET_SERVER_PORT}
# # # # AUTH_ENC_KEY=${AUTH_ENC_KEY}
# # # # AUTH_HMAC_KEY=${AUTH_HMAC_KEY}
# # # # ENCRYPTION_KEY=${AUTH_ENC_KEY}
# # # # MYSQL_URL=jdbc:mysql://localhost:3306/assetdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
# # # # ASSET_DB_USERNAME=root
# # # # ASSET_DB_PASSWORD=Snmysql@1110
# # # # ACCESS_TOKEN=change_this_token
# # # # JWT_SECRET=${JWT_SECRET}
# # # # FEIGN_ACCESS_TOKEN=

# # # # AUTH_SERVICE_URL=http://localhost:${AUTH_SERVER_PORT}
# # # # NOTIFICATION_SERVICE_URL=http://localhost:${NOTIFICATION_SERVER_PORT}
# # # # JWT_PUBLIC_KEY_CLASSPATH=classpath:keys/jwt-public.pem
# # # # EOF

# # # #   chmod 600 "$ASSET_ENV_FILE"
# # # #   echo "Created $ASSET_ENV_FILE (DO NOT commit to git)."
# # # # fi

# # # # # Create .env.notification
# # # # if [ -f "$NOTIFICATION_ENV_FILE" ]; then
# # # #   echo "Skipping: $NOTIFICATION_ENV_FILE already exists."
# # # # else
# # # #   echo "Generating $NOTIFICATION_ENV_FILE..."
# # # #   cat > "$NOTIFICATION_ENV_FILE" <<EOF
# # # # NOTIFICATION_SERVICE_NAME=notification-service
# # # # NOTIFICATION_SERVER_PORT=${NOTIFICATION_SERVER_PORT}
# # # # # add notification-specific env vars below
# # # # EOF
# # # #   chmod 600 "$NOTIFICATION_ENV_FILE"
# # # #   echo "Created $NOTIFICATION_ENV_FILE (DO NOT commit to git)."
# # # # fi

# # # # echo "Done. Environment files located at: $ENV_DIR"
# # # # echo "Important: Add $ENV_DIR to your .gitignore to avoid accidental commits."
# # # # echo
# # # # echo "To use these variables in your shell (temporary):"
# # # # echo "  export \$(grep -v '^#' $DOTENV_FILE | xargs)"
# # # # echo
# # # # echo "For docker-compose, add 'env_file: $ENV_DIR/.env' in your service config."
# # # # ###############################################
# # # # # 16) Add module entry to parent pom.xml (if missing)
# # # # ###############################################
# # # # if [ -f "$PROJECT_ROOT/pom.xml" ]; then
# # # #   if ! grep -q "<module>common-service</module>" "$PROJECT_ROOT/pom.xml"; then
# # # #     echo "🔧 Adding <module>common-service</module> to parent pom.xml ..."
# # # #     # Insert under <modules> if present, otherwise append modules block
# # # #     if grep -q "<modules>" "$PROJECT_ROOT/pom.xml"; then
# # # #       sed -i.bak '/<modules>/a\    <module>common-service</module>' "$PROJECT_ROOT/pom.xml"
# # # #     else
# # # #       # add modules block before </project>
# # # #       awk 'BEGIN{added=0} /<\/project>/{ if(!added){ print "  <modules>\n    <module>common-service</module>\n  </modules>\n"; added=1 } } {print}' "$PROJECT_ROOT/pom.xml" > "$PROJECT_ROOT/pom.xml.tmp" && mv "$PROJECT_ROOT/pom.xml.tmp" "$PROJECT_ROOT/pom.xml"
# # # #     fi
# # # #   fi
# # # # fi

# # # # ###############################################
# # # # # 17) Build the module
# # # # ###############################################
# # # # echo "🚀 Running mvn clean install for common-service (skipping tests)..."
# # # # mvn clean install -pl common-service -am -DskipTests

# # # # echo "✅ common-service created at $COMMON_DIR"
# # # # echo "  - Add dependency to your microservices pom.xml: <dependency><groupId>com.example</groupId><artifactId>common-service</artifactId><version>0.0.1-SNAPSHOT</version></dependency>"
# # # # echo "  - To avoid bean collisions: import only needed classes and avoid @Component-scan on broad packages."
# # # # echo ""
# # # # echo "Next recommendations:"
# # # # echo "  • In auth-service (recommended): expose a JWKS or public key, and let other services validate tokens using that public key (preferable to sharing a secret)."
# # # # echo "  • Use FeignTokenInterceptor to relay Authorization headers when calling other services."
# # # # echo "  • Use CorrelationIdFilter across services for tracing."
# # # # echo "  • Use JpaAttributeEncryptor for PII columns by annotating entity fields with @Convert."
# # # # echo ""
# # # # echo "If you want, I can also:"
# # # # echo "  - generate example entity classes that use JpaAttributeEncryptor,"
# # # # echo "  - add a CorrelationId logging MDC helper,"
# # # # echo "  - scaffold minimal example usages in auth/asset/notification services."
