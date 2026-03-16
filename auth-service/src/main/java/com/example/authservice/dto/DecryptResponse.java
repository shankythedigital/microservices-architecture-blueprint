package com.example.authservice.dto;

/**
 * Response for decryption API. Contains the decrypted plaintext value.
 * Admin-only endpoint for PII decryption (audit, support, compliance).
 */
public class DecryptResponse {

    private String decryptedValue;

    public DecryptResponse() {}

    public DecryptResponse(String decryptedValue) {
        this.decryptedValue = decryptedValue;
    }

    public String getDecryptedValue() {
        return decryptedValue;
    }

    public void setDecryptedValue(String decryptedValue) {
        this.decryptedValue = decryptedValue;
    }
}
