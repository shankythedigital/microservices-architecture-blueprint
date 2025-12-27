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


