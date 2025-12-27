
package com.example.asset.util;

import jakarta.servlet.http.HttpServletRequest;

public class AuditLoggingUtil {

    public static String getClientIP(HttpServletRequest req) {
        if (req == null) return "UNKNOWN";
        String ip = req.getHeader("X-FORWARDED-FOR");
        return (ip == null ? req.getRemoteAddr() : ip);
    }

    public static String getUserAgent(HttpServletRequest req) {
        return (req == null ? "UNKNOWN" : req.getHeader("User-Agent"));
    }

    public static String getUrl(HttpServletRequest req) {
        return (req == null ? "UNKNOWN" : req.getRequestURI());
    }

    public static String getMethod(HttpServletRequest req) {
        return (req == null ? "UNKNOWN" : req.getMethod());
    }
}


