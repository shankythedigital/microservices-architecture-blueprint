package com.example.authservice.service;

import com.example.authservice.dto.DecryptResponse;
import com.example.common.util.AesGcmEncryptor;
import com.example.common.util.EncryptionKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service to decrypt AES-256-GCM encrypted values.
 * Uses same key/format as JpaAttributeEncryptor (common-service).
 * Admin-only use: audit, support, compliance.
 */
@Service
public class DecryptionService {

    private static final Logger log = LoggerFactory.getLogger(DecryptionService.class);
    private final AesGcmEncryptor aes;

    public DecryptionService() {
        this.aes = new AesGcmEncryptor(EncryptionKeyProvider.getNormalizedBase64Key());
        log.info("🔐 [DecryptionService] Initialized with AES-256-GCM (common-service)");
    }

    /**
     * Decrypts an AES-GCM encrypted Base64 string.
     * Never throws — returns DecryptResponse with null plaintext on failure.
     *
     * @param encryptedValue Base64-encoded ciphertext (IV || CIPHERTEXT || TAG)
     * @return DecryptResponse with decrypted plaintext, or null if input is null/blank or decryption fails
     */
    public DecryptResponse decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return new DecryptResponse(null);
        }
        String trimmed = encryptedValue.trim();
        String decrypted = aes.decrypt(trimmed);
        if (decrypted != null) {
            log.debug("🔓 [DecryptionService] Decryption successful (input length={}, output length={})",
                    trimmed.length(), decrypted.length());
        } else {
            log.debug("🔓 [DecryptionService] Decryption returned null (not encrypted or invalid format)");
        }
        return new DecryptResponse(decrypted);
    }
}
