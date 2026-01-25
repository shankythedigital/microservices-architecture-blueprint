
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
    private static final Logger log = LoggerFactory.getLogger(JwtVerifier.class);

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




