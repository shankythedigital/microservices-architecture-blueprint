

package com.example.common.util;

import java.security.MessageDigest;

import javax.crypto.spec.SecretKeySpec;

import javax.crypto.Mac;

import java.util.Base64;

public class HashUtil {
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b: bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
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





