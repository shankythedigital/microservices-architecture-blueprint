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
        // Handle null or empty values immediately - these should never be decrypted
        if (cipher == null) {
            return null;
        }
        
        // Trim and check for empty/blank strings
        String trimmed = cipher.trim();
        if (trimmed.isEmpty() || trimmed.isBlank()) {
            return null;
        }

        // Always wrap everything in try-catch to prevent ANY exceptions from propagating
        // This ensures backward compatibility with existing plain text data and prevents crashes
        try {
            // Check if the data appears to be encrypted (Base64 format with minimum length)
            // Encrypted data should be Base64 encoded and have a minimum length
            // Plain text data (from before encryption) will be returned as-is for backward compatibility
            if (!isEncryptedData(trimmed)) {
                log.debug("🔓 [Decrypt] Data appears to be plain text (not encrypted), returning as-is. Length={}", trimmed.length());
                return trimmed;
            }

            log.debug("🔓 [Decrypt] Decrypting DB column (length={})", trimmed.length());

            // Attempt decryption - if this fails, we'll catch and return original
            String decrypted = aes.decrypt(trimmed);
            log.debug("🔓 [Decrypt] Completed (result-length={})", decrypted != null ? decrypted.length() : 0);
            return decrypted;

        } catch (Throwable ex) {
            // Catch ALL exceptions including RuntimeException, Error, etc.
            // If decryption fails for ANY reason, return the original value
            // This handles:
            // - Plain text data from before encryption was enabled
            // - Corrupted encrypted data
            // - Invalid Base64 data that passed initial checks
            // - BufferUnderflowException, IllegalArgumentException, etc.
            // - Any other decryption errors
            log.warn("⚠️ [Decrypt] Failed to decrypt data (length={}). It may be plain text from before encryption was enabled. " +
                    "Returning original value. Error type: {}, Message: {}", 
                    trimmed.length(), ex.getClass().getSimpleName(), ex.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("⚠️ [Decrypt] Failed data sample (first 50 chars): {}", 
                        trimmed.length() > 50 ? trimmed.substring(0, 50) + "..." : trimmed);
                log.debug("⚠️ [Decrypt] Full exception: ", ex);
            }
            // Return original value to prevent application crashes
            // This allows the system to continue working during migration period
            return trimmed;
        }
    }

    /**
     * Checks if the data appears to be encrypted (Base64 format)
     * Encrypted data should:
     * - Be valid Base64
     * - Have minimum length (AES-GCM encrypted data with IV and tag is typically 50+ chars)
     * - Not contain common plain text patterns (spaces, simple readable text)
     * 
     * This method is conservative - it only returns true if we're very confident the data is encrypted.
     * If in doubt, it returns false (treating as plain text) to ensure backward compatibility.
     * 
     * @param data The data to check
     * @return true if data appears to be encrypted, false otherwise (defaults to false for safety)
     */
    private boolean isEncryptedData(String data) {
        // Be very conservative - only treat as encrypted if we're very sure
        if (data == null || data.length() < 50) {
            // Too short to be encrypted (AES-GCM encrypted data with IV+tag is typically 50+ chars)
            // Plain text usernames/emails/mobiles are usually much shorter (5-50 chars)
            return false;
        }

        // Check if it contains spaces - encrypted data never has spaces
        if (data.contains(" ") || data.contains("\n") || data.contains("\t")) {
            return false;
        }

        // Check if it's valid Base64 format (only Base64 characters)
        String base64Pattern = "^[A-Za-z0-9+/=]+$";
        if (!data.matches(base64Pattern)) {
            // Contains non-Base64 characters, likely plain text
            return false;
        }

        // Check if it's valid Base64 and has proper structure
        try {
            byte[] decoded = Base64.getDecoder().decode(data);
            // Encrypted data should have minimum structure (IV + ciphertext + tag)
            // AES-GCM requires at least 12 bytes IV + some ciphertext + 16 bytes tag
            // For a typical username/email/mobile, encrypted size would be at least 40+ bytes
            if (decoded.length < 40) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            // Not valid Base64, definitely plain text
            return false;
        }

        // If we get here, it's likely encrypted:
        // - Long enough (50+ chars)
        // - Valid Base64 format
        // - No spaces or special characters
        // - Decoded length is sufficient
        return true;
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


