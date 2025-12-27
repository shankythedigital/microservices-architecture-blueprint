// package com.example.common.util;

// import javax.crypto.Mac;
// import javax.crypto.spec.SecretKeySpec;
// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.*;
// import java.util.HexFormat;
// import java.util.Properties;
// import java.util.stream.Stream;

// /**
//  * HMAC-SHA256 utility that lazily initializes its key from multiple sources.
//  *
//  * Initialization order:
//  * 1) ENV: AUTH_HMAC_KEY or HMAC_KEY
//  * 2) System property: auth.hmac.key
//  * 3) common-service resources env/.env.auth (AUTH_HMAC_KEY)
//  * 4) common-service resources env/.env (AUTH_HMAC_KEY or HMAC_KEY)
//  * 5) common-service resources hmac.key (single-line)
//  * 6) common-service resources hmac.properties (hmac.key or auth.hmac.key)
//  * 7) classpath resources with same names (fallback)
//  *
//  * This class does NOT require Spring lifecycle; it initializes on first use.
//  */
// public final class HmacUtil {

//     private static final String HMAC_ALGO = "HmacSHA256";
//     private static volatile byte[] KEY_BYTES; // guarded by initLock
//     private static final Object initLock = new Object();

//     // Absolute path to the common-service resources folder (adjust if needed)
//     private static final Path COMMON_RESOURCES = Paths.get(
//         "/Users/neilnaik/Documents/Shashank/Asset-LifeCycle-Management/Complete-Asset-Management/Github/microservices-architecture-blueprint/common-service/src/main/resources"
//     );

//     private static final Path ENV_DIR = COMMON_RESOURCES.resolve("env");
//     private static final Path ENV_AUTH_FILE = ENV_DIR.resolve(".env.auth");
//     private static final Path ENV_FILE = ENV_DIR.resolve(".env");
//     private static final Path HMAC_KEY_FILE = COMMON_RESOURCES.resolve("hmac.key");
//     private static final Path HMAC_PROPERTIES_FILE = COMMON_RESOURCES.resolve("hmac.properties");

//     // Minimum accepted key length in bytes (16)
//     private static final int MIN_KEY_BYTES = 16;

//     private HmacUtil() { /* utility */ }

//     // ---------------- public API ----------------

//     /**
//      * Compute HMAC-SHA256 and return lowercase hex string.
//      * Lazily initializes the key on first call.
//      */
//     public static String hmacHex(String data) {
//         if (data == null) return null;
//         ensureInitialized();
//         try {
//             Mac mac = Mac.getInstance(HMAC_ALGO);
//             mac.init(new SecretKeySpec(KEY_BYTES, HMAC_ALGO));
//             byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
//             return HexFormat.of().formatHex(raw);
//         } catch (Exception e) {
//             throw new RuntimeException("Failed to compute HMAC", e);
//         }
//     }

//     /** Case-insensitive compare of expected hex vs computed */
//     public static boolean verifyHmac(String data, String expectedHex) {
//         if (expectedHex == null) return false;
//         String actual = hmacHex(data);
//         return actual != null && actual.equalsIgnoreCase(expectedHex);
//     }

//     // ---------------- initialization ----------------

//     private static void ensureInitialized() {
//         if (KEY_BYTES == null) {
//             synchronized (initLock) {
//                 if (KEY_BYTES == null) {
//                     String key = locateKey();
//                     if (key == null || key.trim().isEmpty()) {
//                         throw new IllegalStateException("HMAC key not found. Provide AUTH_HMAC_KEY or HMAC_KEY env var, " +
//                                 "system property auth.hmac.key, or one of the files under " + COMMON_RESOURCES);
//                     }
//                     byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
//                     if (bytes.length < MIN_KEY_BYTES) {
//                         throw new IllegalArgumentException("HMAC key must be at least " + MIN_KEY_BYTES + " bytes");
//                     }
//                     KEY_BYTES = bytes;
//                     // short preview only
//                     String preview = key.length() > 8 ? key.substring(0, 6) + "..." : key;
//                     System.out.println("HmacUtil initialized — key length=" + KEY_BYTES.length + ", preview=" + preview);
//                 }
//             }
//         }
//     }

//     /**
//      * Try to find the key from multiple sources.
//      */
//     private static String locateKey() {
//         // 1) environment variables
//         String v = firstNonBlank(System.getenv("AUTH_HMAC_KEY"), System.getenv("HMAC_KEY"));
//         if (!isBlank(v)) return v;

//         // 2) system property
//         v = System.getProperty("auth.hmac.key");
//         if (!isBlank(v)) return v;

//         // 3) .env.auth file in common resources
//         if (Files.isReadable(ENV_AUTH_FILE)) {
//             v = readKeyFromEnvFile(ENV_AUTH_FILE, "AUTH_HMAC_KEY");
//             if (!isBlank(v)) return v;
//         }

//         // 4) .env generic
//         if (Files.isReadable(ENV_FILE)) {
//             v = readKeyFromEnvFile(ENV_FILE, "AUTH_HMAC_KEY");
//             if (!isBlank(v)) return v;
//             v = readKeyFromEnvFile(ENV_FILE, "HMAC_KEY");
//             if (!isBlank(v)) return v;
//         }

//         // 5) hmac.key single-line file
//         if (Files.isReadable(HMAC_KEY_FILE)) {
//             v = readSingleLineFile(HMAC_KEY_FILE);
//             if (!isBlank(v)) return v;
//         }

//         // 6) hmac.properties
//         if (Files.isReadable(HMAC_PROPERTIES_FILE)) {
//             v = readFromProperties(HMAC_PROPERTIES_FILE, "hmac.key", "auth.hmac.key");
//             if (!isBlank(v)) return v;
//         }

//         // 7) fallback to classpath resources (env/.env.auth etc)
//         v = readFromClasspath(".env.auth", "AUTH_HMAC_KEY");
//         if (!isBlank(v)) return v;
//         v = readFromClasspath(".env", "AUTH_HMAC_KEY", "HMAC_KEY");
//         if (!isBlank(v)) return v;
//         v = readSingleLineFromClasspath("hmac.key");
//         if (!isBlank(v)) return v;
//         v = readFromClasspathProperties("hmac.properties", "hmac.key", "auth.hmac.key");
//         if (!isBlank(v)) return v;

//         return null;
//     }

//     // ---------------- helpers ----------------

//     private static boolean isBlank(String s) {
//         return s == null || s.trim().isEmpty();
//     }

//     private static String firstNonBlank(String... vals) {
//         if (vals == null) return null;
//         for (String v : vals) if (!isBlank(v)) return v;
//         return null;
//     }

//     private static String readKeyFromEnvFile(Path p, String keyName) {
//         try (Stream<String> lines = Files.lines(p, StandardCharsets.UTF_8)) {
//             return lines
//                     .map(String::trim)
//                     .filter(line -> !line.isEmpty() && !line.startsWith("#"))
//                     .map(line -> {
//                         int idx = line.indexOf('=');
//                         if (idx <= 0) return null;
//                         String k = line.substring(0, idx).trim();
//                         String v = line.substring(idx + 1).trim();
//                         return k.equals(keyName) ? v : null;
//                     })
//                     .filter(v -> v != null && !v.isEmpty())
//                     .findFirst()
//                     .orElse(null);
//         } catch (IOException e) {
//             return null;
//         }
//     }

//     private static String readSingleLineFile(Path p) {
//         try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
//             String line = r.readLine();
//             return (line == null) ? null : line.trim();
//         } catch (IOException e) {
//             return null;
//         }
//     }

//     private static String readFromProperties(Path p, String... keys) {
//         Properties props = new Properties();
//         try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
//             props.load(r);
//             for (String k : keys) {
//                 String v = props.getProperty(k);
//                 if (!isBlank(v)) return v.trim();
//             }
//         } catch (IOException ignored) {}
//         return null;
//     }

//     private static String readFromClasspath(String resourceName, String... keys) {
//         try (InputStream is = HmacUtil.class.getClassLoader().getResourceAsStream(resourceName)) {
//             if (is == null) return null;
//             try (BufferedReader r = new BufferedReader(new java.io.InputStreamReader(is, StandardCharsets.UTF_8))) {
//                 return r.lines()
//                         .map(String::trim)
//                         .filter(line -> !line.isEmpty() && !line.startsWith("#"))
//                         .map(line -> {
//                             int idx = line.indexOf('=');
//                             if (idx <= 0) return null;
//                             String k = line.substring(0, idx).trim();
//                             String v = line.substring(idx + 1).trim();
//                             for (String key : keys) {
//                                 if (k.equals(key)) return v;
//                             }
//                             return null;
//                         })
//                         .filter(v -> v != null && !v.isEmpty())
//                         .findFirst()
//                         .orElse(null);
//             }
//         } catch (IOException ignored) { }
//         return null;
//     }

//     private static String readSingleLineFromClasspath(String resourceName) {
//         try (InputStream is = HmacUtil.class.getClassLoader().getResourceAsStream(resourceName)) {
//             if (is == null) return null;
//             try (BufferedReader r = new BufferedReader(new java.io.InputStreamReader(is, StandardCharsets.UTF_8))) {
//                 String line = r.readLine();
//                 return line == null ? null : line.trim();
//             }
//         } catch (IOException ignored) {}
//         return null;
//     }

//     private static String readFromClasspathProperties(String resourceName, String... keys) {
//         try (InputStream is = HmacUtil.class.getClassLoader().getResourceAsStream(resourceName)) {
//             if (is == null) return null;
//             Properties props = new Properties();
//             props.load(is);
//             for (String k : keys) {
//                 String v = props.getProperty(k);
//                 if (!isBlank(v)) return v.trim();
//             }
//         } catch (IOException ignored) {}
//         return null;
//     }
// }

package com.example.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Cloud-first HMAC key resolver.
 *
 * Priority:
 * 1) CLOUD: /opt/app/env/exported.env      (AUTH_HMAC_KEY / HMAC_KEY)
 * 2) CLOUD: /opt/app/env/hmac.key          (single-line)
 *
 * 3) LOCAL classpath:
 *      env/.env.auth
 *      env/.env
 *      hmac.key
 *      hmac.properties
 *
 * 4) ENV variables: AUTH_HMAC_KEY, HMAC_KEY
 *
 * 5) JVM system property: auth.hmac.key
 *
 * Output → Base64(32 bytes) final HMAC key.
 */
public final class HmacUtil {

    private static volatile byte[] KEY_BYTES;
    private static final Object LOCK = new Object();

    private static final Path CLOUD_ENV = Paths.get("/opt/app/env/exported.env");
    private static final Path CLOUD_KEY = Paths.get("/opt/app/env/hmac.key");

    private static final int KEY_LEN = 32;

    private HmacUtil() {}

    // ============================================================================
    // PUBLIC API
    // ============================================================================
    public static String hmacHex(String data) {
        ensureInitialized();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(KEY_BYTES, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to compute HMAC", ex);
        }
    }

    // ============================================================================
    // INITIALIZATION — cloud → local → env → system property
    // ============================================================================
    private static void ensureInitialized() {
        if (KEY_BYTES != null) return;

        synchronized (LOCK) {
            if (KEY_BYTES != null) return;

            String raw = null;

            System.out.println("--------------------------------------------------");
            System.out.println("[HMAC] Resolving HMAC key (cloud → local → env → system)");
            System.out.println("--------------------------------------------------");

            // ----------------------------------------------------------
            // 1️⃣ CLOUD: exported.env
            // ----------------------------------------------------------
            if (Files.isReadable(CLOUD_ENV)) {
                raw = readEnvFile(CLOUD_ENV, "AUTH_HMAC_KEY");
                if (isBlank(raw)) raw = readEnvFile(CLOUD_ENV, "HMAC_KEY");

                if (!isBlank(raw)) {
                    logSource("CLOUD", CLOUD_ENV.toString(), raw);
                    KEY_BYTES = normalize(raw);
                    return;
                }
            }

            // ----------------------------------------------------------
            // 2️⃣ CLOUD: /opt/app/env/hmac.key
            // ----------------------------------------------------------
            if (Files.isReadable(CLOUD_KEY)) {
                raw = readSingleLineFile(CLOUD_KEY);

                if (!isBlank(raw)) {
                    logSource("CLOUD", CLOUD_KEY.toString(), raw);
                    KEY_BYTES = normalize(raw);
                    return;
                }
            }

            // ----------------------------------------------------------
            // 3️⃣ LOCAL classpath resources
            // ----------------------------------------------------------
            raw = readFromClasspathEnv("env/.env.auth", "AUTH_HMAC_KEY");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:env/.env.auth", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readFromClasspathEnv("env/.env", "AUTH_HMAC_KEY");
            if (isBlank(raw)) raw = readFromClasspathEnv("env/.env", "HMAC_KEY");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:env/.env", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readSingleLineClasspath("hmac.key");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:hmac.key", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readClasspathProperties("hmac.properties",
                    "hmac.key", "auth.hmac.key");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:hmac.properties", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            // ----------------------------------------------------------
            // 4️⃣ ENV variables
            // ----------------------------------------------------------
            raw = firstNonBlank(
                    System.getenv("AUTH_HMAC_KEY"),
                    System.getenv("HMAC_KEY")
            );
            if (!isBlank(raw)) {
                logSource("ENV", "AUTH_HMAC_KEY / HMAC_KEY", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            // ----------------------------------------------------------
            // 5️⃣ JVM System Property
            // ----------------------------------------------------------
            raw = System.getProperty("auth.hmac.key");
            if (!isBlank(raw)) {
                logSource("SYSTEM", "-Dauth.hmac.key", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            throw new IllegalStateException("❌ No HMAC key found in cloud/local/env/system sources.");
        }
    }

    // ============================================================================
    // LOGGING
    // ============================================================================
    private static void logSource(String type, String source, String raw) {
        String preview = raw.length() > 10 ? raw.substring(0, 10) + "..." : raw;
        System.out.println("[HMAC] ✔ Loaded from " + type + ": " + source);
        System.out.println("[HMAC]   Raw preview: " + preview);
    }

    private static void logNormalized(byte[] bytes) {
        System.out.println("[HMAC] ✔ Normalized key to EXACT 32 bytes");
        System.out.println("[HMAC]   Base64 length = " +
                Base64.getEncoder().encodeToString(bytes).length());
    }

    // ============================================================================
    // NORMALIZATION
    // ============================================================================
    private static byte[] normalize(String raw) {
        byte[] bytes;

        try {
            bytes = Base64.getDecoder().decode(raw);
        } catch (Exception ex) {
            bytes = raw.getBytes(StandardCharsets.UTF_8);
        }

        if (bytes.length != KEY_LEN) {
            byte[] fixed = new byte[KEY_LEN];
            System.arraycopy(bytes, 0, fixed,
                    0, Math.min(bytes.length, KEY_LEN));
            bytes = fixed;
        }

        logNormalized(bytes);
        return bytes;
    }

    // ============================================================================
    // HELPERS
    // ============================================================================
    private static boolean isBlank(String s) {
        return s == null || s.isEmpty() || s.trim().isEmpty();
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (!isBlank(v)) return v;
        return null;
    }

    private static String readEnvFile(Path p, String key) {
        try (Stream<String> lines = Files.lines(p)) {
            return lines
                    .map(String::trim)
                    .filter(l -> !l.startsWith("#"))
                    .filter(l -> l.contains("="))
                    .map(l -> {
                        String k = l.substring(0, l.indexOf("=")).trim();
                        String v = l.substring(l.indexOf("=") + 1).trim();
                        return k.equals(key) ? v : null;
                    })
                    .filter(v -> !isBlank(v))
                    .findFirst()
                    .orElse(null);
        } catch (IOException ignored) {}
        return null;
    }

    private static String readSingleLineFile(Path p) {
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line = br.readLine();
            return line == null ? null : line.trim();
        } catch (IOException ignored) {}
        return null;
    }

    private static String readFromClasspathEnv(String resource, String key) {
        try (InputStream in = HmacUtil.class.getClassLoader()
                .getResourceAsStream(resource)) {
            if (in == null) return null;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(in))) {

                return br.lines()
                        .map(String::trim)
                        .filter(l -> !l.startsWith("#"))
                        .filter(l -> l.contains("="))
                        .map(l -> {
                            String k = l.substring(0, l.indexOf("=")).trim();
                            String v = l.substring(l.indexOf("=") + 1).trim();
                            return k.equals(key) ? v : null;
                        })
                        .filter(v -> !isBlank(v))
                        .findFirst().orElse(null);
            }
        } catch (IOException ignored) {}
        return null;
    }

    private static String readSingleLineClasspath(String resource) {
        try (InputStream in = HmacUtil.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            return line == null ? null : line.trim();
        } catch (IOException ignored) {}
        return null;
    }

    private static String readClasspathProperties(String resource, String... keys) {
        try (InputStream in = HmacUtil.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return null;
            Properties props = new Properties();
            props.load(in);

            for (String k : keys) {
                String v = props.getProperty(k);
                if (!isBlank(v)) return v.trim();
            }

        } catch (IOException ignored) {}
        return null;
    }
}


