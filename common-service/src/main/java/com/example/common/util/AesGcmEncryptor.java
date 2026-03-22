

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

    private static final Logger log = LoggerFactory.getLogger(AesGcmEncryptor.class);
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
    // Encrypt → Base64( IV || CIPHERTEXT_WITH_TAG )
    // Uses validated input; normalizes to ensure getBytes(UTF_8) never fails.
    // --------------------------------------------------------------------
    public String encrypt(String plaintext) {
        if (plaintext == null) return null;
        String normalized = PiiDataValidator.normalizeForEncryption(plaintext);
        if (normalized == null || normalized.isEmpty()) return null;

        try {
            byte[] plainBytes = normalized.getBytes(StandardCharsets.UTF_8);
            byte[] iv = new byte[IV_LEN];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LEN, iv));

            byte[] encrypted = cipher.doFinal(plainBytes);

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            String output = Base64.getEncoder().encodeToString(buffer.array());
            log.debug("🔒 [Encrypt] OK — plaintextLength={} cipherLength={}", normalized.length(), output.length());
            return output;
        } catch (Exception e) {
            log.error("❌ [Encrypt] Failed: {}", e.getMessage());
            throw new RuntimeException("AES encryption failed: " + e.getMessage(), e);
        }
    }

    // --------------------------------------------------------------------
    // Decrypt → plaintext. Returns null on failure — never throws.
    // Uses safe UTF-8 decode so malformed bytes never cause failure.
    // --------------------------------------------------------------------
    public String decrypt(String base64Cipher) {
        if (base64Cipher == null || base64Cipher.isBlank()) return null;

        String trimmed = base64Cipher.trim();
        if (trimmed.isEmpty()) return null;

        try {
            byte[] decoded = Base64.getDecoder().decode(trimmed);
            if (decoded.length < IV_LEN + 16) return null;

            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LEN];
            buffer.get(iv);

            byte[] ciphertextWithTag = new byte[buffer.remaining()];
            buffer.get(ciphertextWithTag);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LEN, iv));

            byte[] plainBytes = cipher.doFinal(ciphertextWithTag);
            String plaintext = PiiDataValidator.decodeUtf8Safe(plainBytes);
            String validated = PiiDataValidator.validateDecrypted(plaintext);
            log.debug("🔓 [Decrypt] OK — outputLength={}", validated != null ? validated.length() : 0);
            return validated != null && !validated.isEmpty() ? validated : plaintext;
        } catch (Exception e) {
            log.debug("🔓 [Decrypt] Failed (not encrypted or invalid): {}", e.getMessage());
            return null; // Never throw — graceful degradation for plain text / corrupted data
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

