package com.example.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Encrypt/decrypt and HMAC behavior for identity and lookup (same key source as running services).
 */
class EncryptDecryptUtilTest {

    @Test
    void samePlaintext_producesSameHmac() {
        String a = EncryptDecryptUtil.hmac("lookup-stable-value");
        String b = EncryptDecryptUtil.hmac("lookup-stable-value");
        assertEquals(a, b);
    }

    @Test
    void encryptUsesRandomIv_soSamePlaintextYieldsDifferentCiphertext() {
        String a = EncryptDecryptUtil.encrypt("lookup-stable-value");
        String b = EncryptDecryptUtil.encrypt("lookup-stable-value");
        assertNotEquals(a, b);
        assertEquals("lookup-stable-value", EncryptDecryptUtil.decrypt(a));
        assertEquals("lookup-stable-value", EncryptDecryptUtil.decrypt(b));
    }

    @Test
    void roundTrip_encryptDecrypt_preservesPlaintext() {
        String plain = "otp-or-token-like-value";
        String cipher = EncryptDecryptUtil.encrypt(plain);
        assertNotEquals(plain, cipher);
        assertEquals(plain, EncryptDecryptUtil.decrypt(cipher));
    }

    @Test
    void verify_matchesHmac() {
        String plain = "verify-me";
        String stored = EncryptDecryptUtil.hmac(plain);
        assertTrue(EncryptDecryptUtil.verify(plain, stored));
        assertFalse(EncryptDecryptUtil.verify("other", stored));
    }
}
