package com.example.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

@Component
public class JwtUtil {

    private PrivateKey privateKey; // optional for signing
    private PublicKey publicKey;   // required for verifying
    private Key hmacKey;           // fallback if no RSA provided
    private boolean useRsa = false;

    @Value("${jwt.private-key-path:}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path:}")
    private Resource publicKeyResource;

    @Value("${jwt.secret:}") // fallback secret if no RSA
    private String hmacSecret;

    @Value("${jwt.access-token-validity-seconds:900}")
    private long accessTokenValiditySeconds;

    @Value("${jwt.refresh-token-validity-seconds:1209600}")
    private long refreshTokenValiditySeconds;

    @PostConstruct
    private void init() {
        try {
            if (publicKeyResource != null && publicKeyResource.exists()) {
                this.publicKey = loadPublicKey(publicKeyResource);
                System.out.println("🔒 Loaded RSA public key");
                useRsa = true;
            }

            if (privateKeyResource != null && privateKeyResource.exists()) {
                this.privateKey = loadPrivateKey(privateKeyResource);
                System.out.println("🔑 Loaded RSA private key");
                useRsa = true;
            }

            if (!useRsa) {
                if (hmacSecret == null || hmacSecret.length() < 32) {
                    throw new IllegalArgumentException("❌ jwt.secret must be at least 32 characters if RSA keys not provided");
                }
                this.hmacKey = Keys.hmacShaKeyFor(hmacSecret.getBytes());
                System.out.println("⚡ Using HMAC (HS256) for JWT");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JwtUtil: " + e.getMessage(), e);
        }
    }

     public long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    public long getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }
    private PrivateKey loadPrivateKey(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            String keyPem = new String(is.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(keyPem);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        }
    }

    private PublicKey loadPublicKey(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            String keyPem = new String(is.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(keyPem);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        }
    }

    // ----------------------------
    // Token Generation
    // ----------------------------
    public String generateAccessToken(Long userId, Long sessionId, List<String> roles) {
        Instant now = Instant.now();
        Date expiry = Date.from(now.plusSeconds(accessTokenValiditySeconds));

        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of("uid", userId, "sid", sessionId, "roles", roles))
                .setIssuedAt(Date.from(now))
                .setExpiration(expiry);

        if (useRsa && privateKey != null) {
            builder.signWith(privateKey, SignatureAlgorithm.RS256);
        } else {
            builder.signWith(hmacKey, SignatureAlgorithm.HS256);
        }
        return builder.compact();
    }

    public String generateRefreshToken(Long userId, Long sessionId) {
        Instant now = Instant.now();
        Date expiry = Date.from(now.plusSeconds(refreshTokenValiditySeconds));

        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of("uid", userId, "sid", sessionId))
                .setIssuedAt(Date.from(now))
                .setExpiration(expiry);

        if (useRsa && privateKey != null) {
            builder.signWith(privateKey, SignatureAlgorithm.RS256);
        } else {
            builder.signWith(hmacKey, SignatureAlgorithm.HS256);
        }
        return builder.compact();
    }

    // ----------------------------
    // Token Validation
    // ----------------------------
    public Jws<Claims> parseToken(String token) {
        JwtParserBuilder parser = Jwts.parserBuilder();
        if (useRsa) {
            parser.setSigningKey(publicKey);
        } else {
            parser.setSigningKey(hmacKey);
        }
        return parser.build().parseClaimsJws(token);
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).getBody().getSubject();
    }

    public Long getSessionIdFromToken(String token) {
        Object sid = parseToken(token).getBody().get("sid");
        return sid != null ? Long.valueOf(sid.toString()) : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Object roles = parseToken(token).getBody().get("roles");
        return roles instanceof List ? (List<String>) roles : List.of();
    }
}


