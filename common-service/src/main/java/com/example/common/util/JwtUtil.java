

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


