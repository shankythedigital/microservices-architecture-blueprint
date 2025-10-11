
package com.example.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "encryption")
public class EncryptionProperties {
    /**
     * 32-byte secret key for AES encryption.
     * Must be exactly 32 chars for AES-256.
     */
    private String key = "ChangeThisTo32ByteKey_ReplaceInProd!";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

