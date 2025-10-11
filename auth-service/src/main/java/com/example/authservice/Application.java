

package com.example.authservice;

import com.example.authservice.crypto.JpaAttributeEncryptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

import java.util.Base64;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.authservice.client")
public class Application {

    @Value("${auth.enc.key}")
    private String encKey;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void initEncryptor() {
        try {
            byte[] keyBytes;
            try {
                // Try Base64 decode first
                keyBytes = Base64.getDecoder().decode(encKey);
            } catch (IllegalArgumentException e) {
                // If not Base64, fall back to raw bytes (UTF-8)
                keyBytes = encKey.getBytes();
                // Ensure key length is 32 bytes (AES-256 requirement)
                if (keyBytes.length < 32) {
                    byte[] padded = new byte[32];
                    System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
                    keyBytes = padded;
                } else if (keyBytes.length > 32) {
                    byte[] truncated = new byte[32];
                    System.arraycopy(keyBytes, 0, truncated, 0, 32);
                    keyBytes = truncated;
                }
            }

            JpaAttributeEncryptor.init(Base64.getEncoder().encodeToString(keyBytes));
            System.out.println("✅ JpaAttributeEncryptor initialized successfully");

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to initialize JpaAttributeEncryptor", e);
        }
    }
}






