package com.example.common.util;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class RequestContext {
    private static final ThreadLocal<String> ipHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> uaHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> urlHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> methodHolder = new ThreadLocal<>();

     
    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    // ---------------------------------------------
    // 🔹 Generic context storage
    // ---------------------------------------------
    public static void set(String key, Object value) {
        context.get().put(key, value);
    }

    public static Object get(String key) {
        return context.get().get(key);
    }

    public static void clear() {
        context.remove();
    }
    public static void setIp(String ip) { ipHolder.set(ip); }
    public static String getIp() { return ipHolder.get(); }
    public static void clearIp() { ipHolder.remove(); }

    public static void setUserAgent(String ua) { uaHolder.set(ua); }
    public static String getUserAgent() { return uaHolder.get(); }
    public static void clearUserAgent() { uaHolder.remove(); }

    public static void setUrl(String url) { urlHolder.set(url); }
    public static String getUrl() { return urlHolder.get(); }
    public static void clearUrl() { urlHolder.remove(); }

    public static void setMethod(String method) { methodHolder.set(method); }
    public static String getMethod() { return methodHolder.get(); }
    public static void clearMethod() { methodHolder.remove(); }


    public static Long getSessionId() {
        Object sid = get("sessionId");
        if (sid == null) return null;
        try {
            return Long.parseLong(sid.toString());
        } catch (Exception e) {
            return null;
        }
    }


    public static Long getUserId() {
        Object uid = get("userId");
        if (uid == null) return null;
        try {
            return Long.parseLong(uid.toString());
        } catch (Exception e) {
            return null;
        }
    }

    // Optional: populate from Spring RequestContextHolder
    public static void populateFromSpringContext() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                // You can populate here if you have specific request attributes
            }
        } catch (Exception ignored) {}
    }
    public static void clearAll() {
        clearIp();
        clearUserAgent();
        clearUrl();
        clearMethod();
    }
}

