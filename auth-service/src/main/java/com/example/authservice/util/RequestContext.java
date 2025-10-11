package com.example.authservice.util;

public class RequestContext {
    private static final ThreadLocal<String> ipHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> uaHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> urlHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> methodHolder = new ThreadLocal<>();

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

    public static void clearAll() {
        clearIp();
        clearUserAgent();
        clearUrl();
        clearMethod();
    }
}
