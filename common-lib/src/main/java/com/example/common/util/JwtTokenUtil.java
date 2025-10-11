// common-lib/src/main/java/com/example/common/util/JwtTokenUtil.java
package com.example.common.util;

import io.jsonwebtoken.*;
import java.util.Date;

public class JwtTokenUtil {
    private static final String SECRET = System.getenv().getOrDefault("JWT_SECRET", "4aD9#kLp!2zQmN7xYvRtWpShUfBgJcKd");

    public static String generateToken(String subject, long expirationMs) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
                .compact();
    }

    public static String getUsernameFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET.getBytes())
                .parseClaimsJws(token).getBody().getSubject();
    }
}

