package com.example.common.util;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * ========================================================================
 * 🔐 PiiDataValidator — Validation for PII before encryption / after decryption
 * ========================================================================
 *
 * Ensures encryption and decryption never fail due to invalid input:
 * - Normalizes null, empty, blank
 * - Sanitizes invalid UTF-16 (unpaired surrogates) and control chars
 * - Enforces max length to prevent crypto buffer issues
 * - Safe UTF-8 decode for decrypted bytes (malformed → replacement, never throws)
 * - Never throws — always returns a safe value for crypto operations
 *
 * ========================================================================
 */
public final class PiiDataValidator {

    private static final int MAX_PII_LENGTH = 32_768; // 32KB — safe for TEXT columns
    private static final char REPLACEMENT_CHAR = '?';

    private PiiDataValidator() {}

    /**
     * Normalizes and validates PII before encryption.
     * Returns a string that will never cause encryption to fail.
     *
     * @param value Raw PII value (may be null, empty, or contain invalid chars)
     * @return Normalized string safe for encryption, or null
     */
    public static String normalizeForEncryption(String value) {
        if (value == null) return null;

        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;

        // Sanitize invalid UTF-16 and control chars (prevents getBytes(UTF_8) from failing)
        String sanitized = sanitizeForCrypto(trimmed);
        if (sanitized == null || sanitized.isEmpty()) return null;

        // Enforce max length
        if (sanitized.length() > MAX_PII_LENGTH) {
            return sanitized.substring(0, MAX_PII_LENGTH);
        }

        return sanitized;
    }

    /**
     * Validates decrypted output. Sanitizes in case decryption produced invalid UTF-8.
     * Never throws.
     */
    public static String validateDecrypted(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        return sanitizeForCrypto(trimmed);
    }

    /**
     * Sanitizes string for crypto operations: fixes unpaired surrogates and removes
     * control chars that can cause getBytes(UTF_8) or new String(bytes, UTF_8) to fail.
     * Never throws.
     */
    public static String sanitizeForCrypto(String input) {
        if (input == null || input.isEmpty()) return input;

        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            // Unpaired high surrogate
            if (Character.isHighSurrogate(c)) {
                if (i + 1 < input.length() && Character.isLowSurrogate(input.charAt(i + 1))) {
                    sb.append(c).append(input.charAt(i + 1));
                    i++;
                } else {
                    sb.append(REPLACEMENT_CHAR);
                }
            } else if (Character.isLowSurrogate(c)) {
                sb.append(REPLACEMENT_CHAR);
            } else if (c >= 0x20 && c != 0x7F && (c < 0x80 || !Character.isISOControl(c))) {
                sb.append(c);
            } else {
                sb.append(REPLACEMENT_CHAR);
            }
        }
        return sb.toString();
    }

    /**
     * Checks if value is safe to encrypt (non-null, non-empty after trim).
     */
    public static boolean isEncryptable(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Decodes bytes to UTF-8 string safely. Malformed/invalid sequences are replaced with '?'.
     * Never throws — ensures decryption output can always be converted to a displayable string.
     *
     * @param bytes Raw bytes (e.g. from decryption)
     * @return Decoded string, or empty string if bytes null/empty
     */
    public static String decodeUtf8Safe(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return ""; // Should never occur with REPLACE action
        }
    }
}
