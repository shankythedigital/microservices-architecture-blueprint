
package com.example.authservice.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256 GCM encryption/decryption utility.
 * Key must be 32 bytes (256-bit).
 */
public class AesGcmEncryptor {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;       // 96 bits
    private static final int TAG_LENGTH = 128;     // 128-bit auth tag

    private final byte[] key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmEncryptor(byte[] key) {
        if (key == null || key.length != 32) {
            throw new IllegalArgumentException("AES-GCM key must be 32 bytes (256-bit). Provided: " + (key != null ? key.length : 0));
        }
        this.key = key;
    }

    /**
     * Encrypt plain text into Base64 encoded cipher text
     */
    public String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // prepend IV for decryption
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM encryption failed", e);
        }
    }

    /**
     * Decrypt Base64 encoded cipher text back to plain text
     */
    public String decrypt(String base64Cipher) {
        if (base64Cipher == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(base64Cipher);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            byte[] cipherText = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM decryption failed", e);
        }
    }
}


