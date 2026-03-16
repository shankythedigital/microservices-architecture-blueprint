package com.example.authservice.service;

import com.example.authservice.dto.DecryptResponse;
import com.example.common.util.AesGcmEncryptor;
import com.example.common.util.EncryptionKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service to decrypt AES-GCM encrypted values.
 * Uses the same key and format as JpaAttributeEncryptor (AES-256-GCM).
 * Admin-only use: audit, support, compliance.
 */
@Service
public class DecryptionService {

    private static final Logger log = LoggerFactory.getLogger(DecryptionService.class);
    private final AesGcmEncryptor aes;

    public DecryptionService() {
        String base64Key = EncryptionKeyProvider.getNormalizedBase64Key();
        this.aes = new AesGcmEncryptor(base64Key);
        log.info("🔐 [DecryptionService] Initialized with AES-256 key from EncryptionKeyProvider");
    }

    /**
     * Decrypts an AES-GCM encrypted Base64 string.
     *
     * @param encryptedValue Base64-encoded ciphertext (IV || TAG || CIPHERTEXT)
     * @return DecryptResponse with decrypted plaintext, or null if input is null/blank
     * @throws IllegalArgumentException if decryption fails (invalid format, wrong key, tampered data)
     */
    public DecryptResponse decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return new DecryptResponse(null);
        }
        String trimmed = encryptedValue.trim();
        try {
            String decrypted = aes.decrypt(trimmed);
            log.debug("🔓 [DecryptionService] Decryption successful (input length={}, output length={})",
                    trimmed.length(), decrypted != null ? decrypted.length() : 0);
            return new DecryptResponse(decrypted);
        } catch (Exception e) {
            log.warn("⚠️ [DecryptionService] Decryption failed: {}", e.getMessage());
            throw new IllegalArgumentException("Decryption failed: " + e.getMessage(), e);
        }
    }
}
