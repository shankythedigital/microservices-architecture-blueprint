

// // // // // package com.example.authservice;

// // // // // import com.example.common.converter.JpaAttributeEncryptor;
// // // // // import com.example.common.util.EncryptionKeyProvider;
// // // // // import org.springframework.boot.SpringApplication;
// // // // // import org.springframework.boot.autoconfigure.SpringBootApplication;
// // // // // import org.springframework.boot.autoconfigure.domain.EntityScan;
// // // // // import org.springframework.cloud.openfeign.EnableFeignClients;
// // // // // import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// // // // // import jakarta.annotation.PostConstruct;



// // // // // @EnableFeignClients(basePackages = "com.example.common.client")
// // // // // @SpringBootApplication(
// // // // //     scanBasePackages = {
// // // // //         "com.example.authservice",   // your main service
// // // // //         "com.example.common"         // include shared/common beans
// // // // //     }
// // // // // )
// // // // // @EnableJpaRepositories(basePackages = {
// // // // //         "com.example.authservice.repository",
// // // // //         "com.example.common.repository"    // ✅ include common repositories
// // // // // })
// // // // // @EntityScan(basePackages = {
// // // // //         "com.example.authservice.model",
// // // // //         "com.example.common.entity"        // ✅ include common entities
// // // // // })
// // // // // public class Application {

// // // // //     public static void main(String[] args) {
// // // // //         SpringApplication.run(Application.class, args);
// // // // //     }

// // // // //     /**
// // // // //      * Initialize the JPA attribute encryptor using a normalized Base64-encoded 32-byte key
// // // // //      * from the shared common-service (or other configured sources).
// // // // //      */
// // // // //     @PostConstruct
// // // // //     public void initEncryptor() {
// // // // //         try {
// // // // //             String base64Key = EncryptionKeyProvider.getNormalizedBase64Key();
// // // // //             // JpaAttributeEncryptor expects a Base64-encoded key string (32 bytes when decoded)
// // // // //             JpaAttributeEncryptor.init(base64Key);
// // // // //             System.out.println("✅ JpaAttributeEncryptor initialized successfully (base64Key preview: "
// // // // //                     + (base64Key.length() > 8 ? base64Key.substring(0, 8) + "..." : base64Key) + ")");
// // // // //         } catch (Exception e) {
// // // // //             throw new RuntimeException("❌ Failed to initialize JpaAttributeEncryptor", e);
// // // // //         }
// // // // //     }
// // // // // }

package com.example.authservice;

import com.example.common.util.EncryptionKeyProvider;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.common.security.JwtAuthFilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ======================================================================
 * 🚀 Auth Service — Boot Application (FINAL VERSION)
 * ======================================================================
 * IMPORTANT:
 *   ✔ DO NOT call JpaAttributeEncryptor.init() anymore.
 *   ✔ JpaAttributeEncryptor autoloads its AES key in its constructor.
 *   ✔ Only warm up EncryptionKeyProvider so logs show source (cloud/local).
 * ======================================================================
 */
 
@EnableFeignClients(basePackages = "com.example.common.client")
@SpringBootApplication(
        scanBasePackages = {
                "com.example.authservice",
                "com.example.common"
        }
)
@EnableJpaRepositories(basePackages = {
        "com.example.authservice.repository",
        "com.example.common.repository"
})
@EntityScan(basePackages = {
        "com.example.authservice.model",
        "com.example.common.entity"
})
public class Application {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);


    public static void main(String[] args) {
        System.out.println("----------------------------------------------------------");
        System.out.println("🚀 Starting AUTH-SERVICE (Spring Boot + AES Encryption)");
        System.out.println("----------------------------------------------------------");
        SpringApplication.run(Application.class, args);
    }

    /**
     * Warm-up only:
     *  ✔ Load EncryptionKeyProvider once
     *  ✔ Print key source (LOCAL/CLOUD/ENV)
     *  ✔ Print fingerprint (safe)
     *  ❌ Do NOT initialize JpaAttributeEncryptor (handled automatically)
     */
    @PostConstruct
    public void warmupEncryption() {

        log.info("---------------------------------------------------------------");
        log.info("🔐 [Application] Warming up EncryptionKeyProvider...");
        log.info("---------------------------------------------------------------");

        try {
            String base64Key = EncryptionKeyProvider.getNormalizedBase64Key();

            log.info("🔑 AES-256 encryption key loaded successfully");
            log.info("🔍 Key fingerprint: {}", safeFingerprint(base64Key));

            log.info("✔ JPA Encryption will auto-initialize via JpaAttributeEncryptor");
            log.info("---------------------------------------------------------------\n");

        } catch (Exception ex) {
            log.error("❌ [Application] Encryption warmup failed: {}", ex.getMessage());
            throw new RuntimeException("Fatal: Could not initialize encryption", ex);
        }
    }

    // Utility: Safe SHA-256 fingerprint of Base64 key (NEVER print real key)
    private String safeFingerprint(String base64Key) {
        try {
            java.security.MessageDigest sha = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = sha.digest(base64Key.getBytes());
            return java.util.Base64.getEncoder().encodeToString(digest).substring(0, 16);
        } catch (Exception e) {
            return "N/A";
        }
    }
}











