package com.example.common.util;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * ========================================================================
 * 🔐 EncryptDecryptUtil — Secure AES-256-GCM + HMAC Search
 * ========================================================================
 *
 * ✔ Random IV (secure)
 * ✔ AES-GCM encryption/decryption
 * ✔ HMAC-SHA256 for search
 * ✔ Compatible with existing EncryptionKeyProvider
 *
 * Store:
 *   encrypted_value → for decrypt
 *   hash_value      → for search
 *
 * ========================================================================
 */
public final class EncryptDecryptUtil {

    private static final int AES_KEY_LEN = 32;
    private static final int IV_LEN = 12;
    private static final int TAG_LEN = 128;

    private static volatile byte[] ENC_KEY_BYTES;
    private static volatile byte[] HMAC_KEY_BYTES;

    private static final Object LOCK = new Object();
    private static final SecureRandom RANDOM = new SecureRandom();

    private EncryptDecryptUtil() {}

    // ============================================================
    // 🔐 ENCRYPT
    // ============================================================
    public static String encrypt(String plaintext) {
        if (plaintext == null) return null;

        String normalized = PiiDataValidator.normalizeForEncryption(plaintext);
        if (normalized == null) return null;

        ensureInitialized();

        try {
            byte[] iv = new byte[IV_LEN];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(ENC_KEY_BYTES, "AES"),
                    new GCMParameterSpec(TAG_LEN, iv));

            byte[] encrypted = cipher.doFinal(normalized.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            return Base64.getEncoder().encodeToString(buffer.array());

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    // ============================================================
    // 🔓 DECRYPT
    // ============================================================
    public static String decrypt(String base64Cipher) {
        if (base64Cipher == null || base64Cipher.isBlank()) return null;

        ensureInitialized();

        try {
            byte[] decoded = Base64.getDecoder().decode(base64Cipher);

            if (decoded.length < IV_LEN + 16) return null;

            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_LEN];
            buffer.get(iv);

            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(ENC_KEY_BYTES, "AES"),
                    new GCMParameterSpec(TAG_LEN, iv));

            byte[] plainBytes = cipher.doFinal(cipherText);

            String plainText = PiiDataValidator.decodeUtf8Safe(plainBytes);
            return PiiDataValidator.validateDecrypted(plainText);

        } catch (Exception e) {
            return null; // safe fail
        }
    }

    // ============================================================
    // 🔍 HMAC (SEARCH)
    // ============================================================
    public static String hmac(String plaintext) {
        if (plaintext == null) return null;

        String normalized = PiiDataValidator.normalizeForEncryption(plaintext);
        if (normalized == null) return null;

        ensureInitialized();

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(HMAC_KEY_BYTES, "HmacSHA256"));

            byte[] hash = mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("HMAC generation failed", e);
        }
    }

    // ============================================================
    // ✔ VERIFY (for search match)
    // ============================================================
    public static boolean verify(String plaintext, String storedHash) {
        if (plaintext == null || storedHash == null) return false;

        try {
            return hmac(plaintext).equals(storedHash);
        } catch (Exception e) {
            return false;
        }
    }

    // ============================================================
    // 🔄 DECRYPT OR RETURN ORIGINAL
    // ============================================================
    public static String decryptOrOriginal(String value) {
        if (value == null || value.isBlank()) return value;

        String decrypted = decrypt(value);
        return decrypted != null ? decrypted : value;
    }

    // ============================================================
    // 🔑 INITIALIZATION
    // ============================================================
    private static void ensureInitialized() {
        if (ENC_KEY_BYTES != null && HMAC_KEY_BYTES != null) return;

        synchronized (LOCK) {
            if (ENC_KEY_BYTES != null && HMAC_KEY_BYTES != null) return;

            String base64Key = EncryptionKeyProvider.getNormalizedBase64Key();
            byte[] masterKey = Base64.getDecoder().decode(base64Key);

            if (masterKey.length != AES_KEY_LEN) {
                throw new IllegalStateException("AES key must be 32 bytes");
            }

            // 🔐 Key separation using SHA-256
            ENC_KEY_BYTES = deriveKey(masterKey, "ENC");
            HMAC_KEY_BYTES = deriveKey(masterKey, "HMAC");
        }
    }

    // ============================================================
    // 🔑 KEY DERIVATION (IMPORTANT FIX)
    // ============================================================
    private static byte[] deriveKey(byte[] masterKey, String purpose) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(masterKey);
            digest.update(purpose.getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (Exception e) {
            throw new RuntimeException("Key derivation failed", e);
        }
    }
}