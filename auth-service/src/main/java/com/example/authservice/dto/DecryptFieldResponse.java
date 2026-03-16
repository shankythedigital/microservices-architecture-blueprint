package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response for per-field decryption API.
 * Returns one decrypted value at a time. Call repeatedly for each field.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecryptFieldResponse {

    private String field;
    private String decryptedValue;

    public DecryptFieldResponse() {}

    public DecryptFieldResponse(String field, String decryptedValue) {
        this.field = field;
        this.decryptedValue = decryptedValue;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getDecryptedValue() {
        return decryptedValue;
    }

    public void setDecryptedValue(String decryptedValue) {
        this.decryptedValue = decryptedValue;
    }
}
