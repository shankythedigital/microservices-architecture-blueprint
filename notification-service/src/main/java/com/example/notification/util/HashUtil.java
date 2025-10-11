package com.example.notification.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HashUtil {
    private static String base64Key;

    public static void init(String base64KeyIn) {
        base64Key = base64KeyIn;
    }

    public static String fingerprint(String value) {
        if (value == null) return null;
        try {
            byte[] key = Base64.getDecoder().decode(base64Key);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] out = mac.doFinal(value.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }
}
