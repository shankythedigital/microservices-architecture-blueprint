
package com.example.authservice.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Base64;

/**
 * JPA AttributeConverter that encrypts/decrypts String values transparently.
 * 
 * Note: Since AttributeConverter is managed by JPA (not Spring),
 * we cannot directly use @Autowired or @Value here.
 * Instead, we initialize the key once from environment variables.
 */
@Converter(autoApply = false)
public class JpaAttributeEncryptor implements AttributeConverter<String, String> {

    private static AesGcmEncryptor encryptor;

    // Initialize the encryptor once at application startup
    public static void init(String base64Key) {
        byte[] key = Base64.getDecoder().decode(base64Key);
        encryptor = new AesGcmEncryptor(key);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : encryptor.decrypt(dbData);
    }
}


