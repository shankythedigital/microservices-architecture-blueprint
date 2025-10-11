package com.example.notification.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JpaAttributeEncryptor implements AttributeConverter<String, String> {
    private static AesGcmEncryptor encryptor;

    public static void init(byte[] key) {
        encryptor = new AesGcmEncryptor(key);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return encryptor.decrypt(dbData);
    }
}
