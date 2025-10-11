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
