package com.example.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/*
 Simple AES/CBC/PKCS5 implementation for demonstration.
 In production:
 - Use a KMS to manage keys (AWS KMS, GCP KMS, Azure KeyVault)
 - Use authenticated encryption (GCM) and rotate keys
 - Do not store keys in source code or config files in plaintext
*/
@Component
@Converter
public class AesAttributeConverter implements AttributeConverter<String, String> {

    private static String SECRET = System.getenv().getOrDefault("ENCRYPTION_KEY", "change-this-to-32-byte-secret!!");
    private static final String ALGO = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;

    private SecretKeySpec getKeySpec() {
        byte[] keyBytes = new byte[16];
        byte[] secretBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(secretBytes, 0, keyBytes, 0, Math.min(secretBytes.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            byte[] iv = new byte[IV_SIZE];
            SecureRandom sr = new SecureRandom();
            sr.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, getKeySpec(), ivSpec);
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception ex) {
            throw new RuntimeException("Encryption error", ex);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            byte[] encrypted = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, getKeySpec(), new IvParameterSpec(iv));
            byte[] original = cipher.doFinal(encrypted);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("Decryption error", ex);
        }
    }
}
