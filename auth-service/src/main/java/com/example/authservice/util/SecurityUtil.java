
package com.example.authservice.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    /**
     * Reads the authenticated principal name and tries to parse it as Long userId.
     * This assumes your JwtFilter sets Authentication.getName() to the user's id string.
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}


