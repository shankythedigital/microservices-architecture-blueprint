package com.example.common.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Round-trip tests for the same AES-256-GCM format used by {@link JpaAttributeEncryptor}.
 */
class AesGcmEncryptorTest {

    /** Exactly 32 bytes, Base64-encoded (same shape as {@link EncryptionKeyProvider} output). */
    private static final String TEST_KEY_B64 = Base64.getEncoder().encodeToString(
            "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));

    @Test
    void encryptThenDecrypt_preservesPlaintext() {
        AesGcmEncryptor aes = new AesGcmEncryptor(TEST_KEY_B64);
        String plain = "user@example.com";
        String cipher = aes.encrypt(plain);
        assertNotNull(cipher);
        assertNotEquals(plain, cipher);
        assertEquals(plain, aes.decrypt(cipher));
    }

    @Test
    void encryptUsesRandomIv_soSamePlaintextYieldsDifferentCiphertext() {
        AesGcmEncryptor aes = new AesGcmEncryptor(TEST_KEY_B64);
        String c1 = aes.encrypt("same-value");
        String c2 = aes.encrypt("same-value");
        assertNotEquals(c1, c2);
        assertEquals("same-value", aes.decrypt(c1));
        assertEquals("same-value", aes.decrypt(c2));
    }

    @Test
    void decryptWithWrongKey_returnsNull() {
        AesGcmEncryptor encrypt = new AesGcmEncryptor(TEST_KEY_B64);
        String otherKeyB64 = Base64.getEncoder().encodeToString(
                "abcdefghijklmnopqrstuvwxyz012345".getBytes(StandardCharsets.UTF_8));
        AesGcmEncryptor wrongDecrypt = new AesGcmEncryptor(otherKeyB64);
        String cipher = encrypt.encrypt("secret");
        assertNull(wrongDecrypt.decrypt(cipher));
    }

    @Test
    void decryptNonCipher_returnsNull() {
        AesGcmEncryptor aes = new AesGcmEncryptor(TEST_KEY_B64);
        assertNull(aes.decrypt("not-base64-cipher"));
    }
}
