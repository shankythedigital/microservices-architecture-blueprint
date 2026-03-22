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

import com.example.common.util.AesGcmEncryptor;
import com.example.common.util.EncryptionKeyProvider;
import com.example.common.util.PiiDataValidator;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.Base64;

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

    
    private static final Logger log = LoggerFactory.getLogger(JpaAttributeEncryptor.class);
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
    // Validates PII first so encryption never fails due to invalid input.
    // ===================================================================
    @Override
    public String convertToDatabaseColumn(String plain) {
        if (plain == null) return null;

        // Validate and normalize PII — prevents getBytes(UTF_8), unpaired surrogates, control chars from failing
        String normalized = PiiDataValidator.normalizeForEncryption(plain);
        if (normalized == null) return null;

        log.debug("🔒 [Encrypt] Encrypting attribute (length={})", normalized.length());

        try {
            String encrypted = aes.encrypt(normalized);
            if (encrypted == null) {
                log.error("❌ [Encrypt] AesGcmEncryptor returned null (unexpected)");
                throw new IllegalStateException("PII encryption returned null — cannot store unencrypted");
            }
            log.debug("🔒 [Encrypt] Completed → ciphertext length={}", encrypted.length());
            return encrypted;
        } catch (Exception ex) {
            log.error("❌ [Encrypt] Failed after validation. Cause={}", ex.getMessage());
            throw new IllegalStateException("PII encryption failed — ensure AUTH_ENC_KEY is valid. " + ex.getMessage(), ex);
        }
    }

    // ===================================================================
    //  🔓 Decrypt after reading from DB
    // Never throws — AesGcmEncryptor.decrypt returns null on failure; legacy plain text returned as-is.
    // ===================================================================
    @Override
    public String convertToEntityAttribute(String cipher) {
        if (cipher == null) return null;

        String trimmed = cipher.trim();
        if (trimmed.isEmpty() || trimmed.isBlank()) return null;

        log.debug("🔓 [Decrypt] Decrypting DB column (length={})", trimmed.length());
        String decrypted = aes.decrypt(trimmed);
        if (decrypted == null) {
            log.debug("🔓 [Decrypt] Not encrypted or invalid — returning as-is (legacy plain text)");
            return trimmed;
        }
        String validated = PiiDataValidator.validateDecrypted(decrypted);
        log.debug("🔓 [Decrypt] Completed (result-length={})", validated != null ? validated.length() : 0);
        return validated != null && !validated.isEmpty() ? validated : decrypted;
    }

    /**
     * Truncates a string for safe logging (avoids dumping huge values).
     */
    private static String truncate(String s, int maxLen) {
        if (s == null) return "null";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
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


