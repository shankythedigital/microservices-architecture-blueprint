package com.example.notification.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AesGcmEncryptor {
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BIT_LENGTH = 128;
    private final byte[] key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmEncryptor(byte[] key) {
        if (key == null || key.length != 32) throw new IllegalArgumentException("Key must be 32 bytes");
        this.key = key;
    }

    public String encrypt(String plain) {
        if (plain == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec ks = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, ks, new GCMParameterSpec(TAG_BIT_LENGTH, iv));
            byte[] ct = cipher.doFinal(plain.getBytes("UTF-8"));
            byte[] combined = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ct, 0, combined, iv.length, ct.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String data) {
        if (data == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(data);
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            byte[] ct = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, ct, 0, ct.length);
            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec ks = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, ks, new GCMParameterSpec(TAG_BIT_LENGTH, iv));
            byte[] plain = cipher.doFinal(ct);
            return new String(plain, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
