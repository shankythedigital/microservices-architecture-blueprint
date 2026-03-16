package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for decrypting an AES-GCM encrypted value.
 * Used by admin decryption API for PII fields encrypted via JpaAttributeEncryptor.
 */
public class DecryptRequest {

    /** Base64-encoded encrypted string (AES-GCM format from JpaAttributeEncryptor). */
    @NotBlank(message = "encryptedValue is required")
    private String encryptedValue;

    public String getEncryptedValue() {
        return encryptedValue;
    }

    public void setEncryptedValue(String encryptedValue) {
        this.encryptedValue = encryptedValue;
    }
}
