package com.example.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic encrypt/decrypt used for lookup-friendly values (same key source as JPA PII).
 * Requires a resolvable AES key (classpath env / AUTH_ENC_KEY / etc.) like a running service.
 */
class EncryptDecryptUtilTest {

    @Test
    void samePlaintext_producesSameCiphertext() {
        String a = EncryptDecryptUtil.encrypt("lookup-stable-value");
        String b = EncryptDecryptUtil.encrypt("lookup-stable-value");
        assertEquals(a, b);
    }

    @Test
    void roundTrip_preservesPlaintext() {
        String plain = "otp-or-token-like-value";
        String cipher = EncryptDecryptUtil.encrypt(plain);
        assertNotEquals(plain, cipher);
        assertEquals(plain, EncryptDecryptUtil.decrypt(cipher));
    }

    @Test
    void verify_matchesEncrypt() {
        String plain = "verify-me";
        String stored = EncryptDecryptUtil.encrypt(plain);
        assertTrue(EncryptDecryptUtil.verify(plain, stored));
        assertFalse(EncryptDecryptUtil.verify("other", stored));
    }
}
