package com.example.authservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * Utility for computing/verifying HMAC-SHA256 hashes.
 * Loads HMAC key from application.yml or environment.
 */
@Component
public class HmacUtil {

    private static final String HMAC_ALGO = "HmacSHA256";

    private static byte[] KEY;

    // Load from application.yml -> hmac.key
    @Value("${hmac.key:}")
    private String configKey;

    @PostConstruct
    private void init() {
        if (configKey == null || configKey.isBlank()) {
            // fallback
            configKey = System.getenv().getOrDefault("HMAC_KEY",
                    System.getProperty("hmac.key", "ChangeThisToAnotherKeyForHMAC_ReplaceInProd!"));
        }

        if (configKey.length() < 16) {
            throw new IllegalArgumentException("❌ HMAC key must be at least 16 characters (configured in application.yml or env HMAC_KEY)");
        }

        KEY = configKey.getBytes(StandardCharsets.UTF_8);
        System.out.println("🔑 HmacUtil initialized with key length=" + KEY.length);
    }

    /** Generate HMAC (hex-encoded) */
    public static String hmacHex(String data) {
        if (data == null) return null;
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(KEY, HMAC_ALGO));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }

    /** Verify if provided hex HMAC matches the computed value */
    public static boolean verifyHmac(String data, String expectedHex) {
        String actual = hmacHex(data);
        return actual != null && actual.equalsIgnoreCase(expectedHex);
    }
}

