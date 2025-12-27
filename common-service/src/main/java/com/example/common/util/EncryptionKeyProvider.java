// // // // package com.example.common.util;

// // // // import java.io.BufferedReader;
// // // // import java.io.IOException;
// // // // import java.nio.charset.StandardCharsets;
// // // // import java.nio.file.*;
// // // // import java.util.Base64;
// // // // import java.util.Properties;
// // // // import java.util.stream.Stream;

// // // // /**
// // // //  * Locates and normalizes an encryption key to a Base64-encoded 32-byte value
// // // //  * suitable for AES-256 (what JpaAttributeEncryptor expects).
// // // //  *
// // // //  * Search order (first match wins):
// // // //  *  1) ENV: AUTH_ENC_KEY
// // // //  *  2) ENV: ENCRYPTION_KEY
// // // //  *  3) System property: auth.enc.key
// // // //  *  4) env/.env.auth (key AUTH_ENC_KEY)
// // // //  *  5) env/.env (keys AUTH_ENC_KEY or ENCRYPTION_KEY)
// // // //  *  6) single-line file: encryption.key under common-service resources
// // // //  *  7) properties file: enc.properties (auth.enc.key or encryption.key)
// // // //  *
// // // //  * If an ASCII/raw key is found it is converted to bytes, then padded/truncated to 32 bytes.
// // // //  * If a Base64 string is found, it's decoded; result padded/truncated to 32 bytes.
// // // //  *
// // // //  * Returns a Base64 string of exactly 32 bytes.
// // // //  */
// // // // public final class EncryptionKeyProvider {

// // // //     // Default resources path — adjust if your project layout differs.
// // // //     private static final Path COMMON_RESOURCES = Paths.get(
// // // //             "/Users/neilnaik/Documents/Shashank/Asset-LifeCycle-Management/Complete-Asset-Management/Github/microservices-architecture-blueprint/common-service/src/main/resources"
// // // //     );
// // // //     private static final Path ENV_DIR = COMMON_RESOURCES.resolve("env");
// // // //     private static final Path ENV_AUTH_FILE = ENV_DIR.resolve(".env.auth");
// // // //     private static final Path ENV_FILE = ENV_DIR.resolve(".env");
// // // //     private static final Path KEY_FILE = COMMON_RESOURCES.resolve("encryption.key");
// // // //     private static final Path PROPS_FILE = COMMON_RESOURCES.resolve("enc.properties");

// // // //     private static final int KEY_LEN = 32; // bytes for AES-256

// // // //     private EncryptionKeyProvider() { /* static helper */ }

// // // //     /**
// // // //      * Locate an encryption key and return it as a Base64-encoded 32-byte value.
// // // //      * Throws IllegalStateException if no valid key is found.
// // // //      */
// // // //     public static String getNormalizedBase64Key() {
// // // //         String raw = null;

// // // //         // 1,2) environment variables
// // // //         raw = firstNonBlank(System.getenv("AUTH_ENC_KEY"), System.getenv("ENCRYPTION_KEY"));

// // // //         // 3) system property
// // // //         if (isBlank(raw)) {
// // // //             raw = System.getProperty("auth.enc.key");
// // // //         }

// // // //         // 4) .env.auth
// // // //         if (isBlank(raw) && Files.isReadable(ENV_AUTH_FILE)) {
// // // //             raw = readKeyFromEnvFile(ENV_AUTH_FILE, "AUTH_ENC_KEY");
// // // //         }

// // // //         // 5) .env
// // // //         if (isBlank(raw) && Files.isReadable(ENV_FILE)) {
// // // //             raw = readKeyFromEnvFile(ENV_FILE, "AUTH_ENC_KEY");
// // // //             if (isBlank(raw)) raw = readKeyFromEnvFile(ENV_FILE, "ENCRYPTION_KEY");
// // // //         }

// // // //         // 6) single-line key file
// // // //         if (isBlank(raw) && Files.isReadable(KEY_FILE)) {
// // // //             raw = readSingleLineFile(KEY_FILE);
// // // //         }

// // // //         // 7) properties file
// // // //         if (isBlank(raw) && Files.isReadable(PROPS_FILE)) {
// // // //             raw = readFromProperties(PROPS_FILE, "auth.enc.key", "encryption.key");
// // // //         }

// // // //         if (isBlank(raw)) {
// // // //             throw new IllegalStateException("No encryption key found. Provide AUTH_ENC_KEY or ENCRYPTION_KEY env var, system property auth.enc.key, "
// // // //                     + ENV_AUTH_FILE + " (.env.auth), " + ENV_FILE + " (.env), " + KEY_FILE + " (encryption.key), or " + PROPS_FILE + " (enc.properties).");
// // // //         }

// // // //         // Normalize: if value appears Base64, decode it, else treat as UTF-8 bytes.
// // // //         byte[] keyBytes = tryBase64Decode(raw);
// // // //         if (keyBytes == null) {
// // // //             keyBytes = raw.getBytes(StandardCharsets.UTF_8);
// // // //         }

// // // //         // Pad or truncate to KEY_LEN
// // // //         if (keyBytes.length != KEY_LEN) {
// // // //             byte[] normalized = new byte[KEY_LEN];
// // // //             int copy = Math.min(keyBytes.length, KEY_LEN);
// // // //             System.arraycopy(keyBytes, 0, normalized, 0, copy);
// // // //             keyBytes = normalized;
// // // //         }

// // // //         // Return Base64-encoded 32-byte string
// // // //         return Base64.getEncoder().encodeToString(keyBytes);
// // // //     }

// // // //     // ---------------- helpers ----------------

// // // //     private static boolean isBlank(String s) {
// // // //         return s == null || s.trim().isEmpty();
// // // //     }

// // // //     private static String firstNonBlank(String... vals) {
// // // //         if (vals == null) return null;
// // // //         for (String v : vals) if (!isBlank(v)) return v;
// // // //         return null;
// // // //     }

// // // //     private static byte[] tryBase64Decode(String s) {
// // // //         try {
// // // //             byte[] dec = Base64.getDecoder().decode(s);
// // // //             // if decoding yields < 1 byte, consider it invalid
// // // //             if (dec == null || dec.length == 0) return null;
// // // //             return dec;
// // // //         } catch (IllegalArgumentException e) {
// // // //             return null;
// // // //         }
// // // //     }

// // // //     private static String readKeyFromEnvFile(Path p, String keyName) {
// // // //         try (Stream<String> lines = Files.lines(p, StandardCharsets.UTF_8)) {
// // // //             return lines
// // // //                     .map(String::trim)
// // // //                     .filter(line -> !line.isEmpty() && !line.startsWith("#"))
// // // //                     .map(line -> {
// // // //                         int idx = line.indexOf('=');
// // // //                         if (idx <= 0) return null;
// // // //                         String k = line.substring(0, idx).trim();
// // // //                         String v = line.substring(idx + 1).trim();
// // // //                         return k.equals(keyName) ? v : null;
// // // //                     })
// // // //                     .filter(v -> v != null && !v.isEmpty())
// // // //                     .findFirst()
// // // //                     .orElse(null);
// // // //         } catch (IOException e) {
// // // //             return null;
// // // //         }
// // // //     }

// // // //     private static String readSingleLineFile(Path p) {
// // // //         try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
// // // //             String line = r.readLine();
// // // //             return (line == null) ? null : line.trim();
// // // //         } catch (IOException e) {
// // // //             return null;
// // // //         }
// // // //     }

// // // //     private static String readFromProperties(Path p, String... keys) {
// // // //         Properties props = new Properties();
// // // //         try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
// // // //             props.load(r);
// // // //             for (String k : keys) {
// // // //                 String v = props.getProperty(k);
// // // //                 if (!isBlank(v)) return v.trim();
// // // //             }
// // // //         } catch (IOException ignored) {
// // // //         }
// // // //         return null;
// // // //     }
// // // // }


// // VERSION #2
// package com.example.common.util;

// import java.io.BufferedReader;
// import java.io.File;
// import java.io.IOException;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.*;
// import java.util.Base64;
// import java.util.Properties;
// import java.util.stream.Stream;

// public final class EncryptionKeyProvider {

//     private static final Path EXPORT_ENV = Paths.get("/opt/app/env/exported.env");
//     private static final Path KEY_FILE = Paths.get("/opt/app/env/encryption.key");
//     private static final int KEY_LEN = 32;

//     private EncryptionKeyProvider() { }

//     public static String getNormalizedBase64Key() {
//         String raw = firstNonBlank(System.getenv("AUTH_ENC_KEY"), System.getenv("ENCRYPTION_KEY"));

//         // try to read exported.env if not found in process env
//         if (isBlank(raw) && Files.isReadable(EXPORT_ENV)) {
//             raw = readKeyFromEnvFile(EXPORT_ENV, "AUTH_ENC_KEY");
//             if (isBlank(raw)) raw = readKeyFromEnvFile(EXPORT_ENV, "ENCRYPTION_KEY");
//         }

//         // try single file
//         if (isBlank(raw) && Files.isReadable(KEY_FILE)) {
//             raw = readSingleLineFile(KEY_FILE);
//         }

//         if (isBlank(raw)) {
//             throw new IllegalStateException("❌ No encryption key found.\n"
//                     + "Set AUTH_ENC_KEY or ENCRYPTION_KEY as environment variables (loaded from /opt/app/env/exported.env).");
//         }

//         byte[] keyBytes = tryBase64Decode(raw);
//         if (keyBytes == null) keyBytes = raw.getBytes(StandardCharsets.UTF_8);

//         if (keyBytes.length != KEY_LEN) {
//             byte[] normalized = new byte[KEY_LEN];
//             int copy = Math.min(keyBytes.length, KEY_LEN);
//             System.arraycopy(keyBytes, 0, normalized, 0, copy);
//             keyBytes = normalized;
//         }

//         return Base64.getEncoder().encodeToString(keyBytes);
//     }

//     // helpers
//     private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
//     private static String firstNonBlank(String... vals) {
//         if (vals == null) return null;
//         for (String v : vals) if (!isBlank(v)) return v;
//         return null;
//     }
//     private static byte[] tryBase64Decode(String s) {
//         try {
//             byte[] dec = Base64.getDecoder().decode(s);
//             if (dec == null || dec.length == 0) return null;
//             return dec;
//         } catch (IllegalArgumentException e) {
//             return null;
//         }
//     }
//     private static String readKeyFromEnvFile(Path p, String keyName) {
//         try (Stream<String> lines = Files.lines(p, StandardCharsets.UTF_8)) {
//             return lines
//                     .map(String::trim)
//                     .filter(l -> !l.isEmpty() && !l.startsWith("#"))
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
// }

package com.example.common.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Cloud-first AES-256 EncryptionKeyProvider.
 *
 * Priority (first match wins):
 *
 * 1) CLOUD:
 *     /opt/app/env/exported.env       (AUTH_ENC_KEY / ENCRYPTION_KEY)
 *     /opt/app/env/encryption.key     (single-line RAW or Base64)
 *
 * 2) LOCAL classpath:
 *     env/.env.auth
 *     env/.env
 *     encryption.key
 *     enc.properties
 *
 * 3) ENV variables:
 *     AUTH_ENC_KEY
 *     ENCRYPTION_KEY
 *
 * 4) JVM property:
 *     -Dauth.enc.key
 *
 * Output:
 *     Base64 string representing EXACT 32-byte AES-256 key.
 */
public final class EncryptionKeyProvider {

    private static final Object LOCK = new Object();
    private static volatile byte[] KEY_BYTES;

    private static final Path CLOUD_ENV = Paths.get("/opt/app/env/exported.env");
    private static final Path CLOUD_KEY = Paths.get("/opt/app/env/encryption.key");

    private static final int AES_LEN = 32; // 32 bytes AES-256

    private EncryptionKeyProvider() {}

    // ============================================================================
    // PUBLIC API
    // ============================================================================
    public static String getNormalizedBase64Key() {
        ensureInitialized();
        return Base64.getEncoder().encodeToString(KEY_BYTES);
    }

    // ============================================================================
    // INITIALIZER — cloud → local → env → system
    // ============================================================================
    private static void ensureInitialized() {
        if (KEY_BYTES != null) return;

        synchronized (LOCK) {
            if (KEY_BYTES != null) return;

            System.out.println("--------------------------------------------------");
            System.out.println("[ENC] Resolving AES encryption key (cloud → local → env → system)");
            System.out.println("--------------------------------------------------");

            String raw = null;

            // ----------------------------------------------------------
            // 1️⃣ CLOUD: exported.env
            // ----------------------------------------------------------
            if (Files.isReadable(CLOUD_ENV)) {
                raw = readEnvFile(CLOUD_ENV, "AUTH_ENC_KEY");
                if (isBlank(raw))
                    raw = readEnvFile(CLOUD_ENV, "ENCRYPTION_KEY");

                if (!isBlank(raw)) {
                    logSource("CLOUD", CLOUD_ENV.toString(), raw);
                    KEY_BYTES = normalize(raw);
                    return;
                }
            }

            // ----------------------------------------------------------
            // 2️⃣ CLOUD: encryption.key
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
            // 3️⃣ LOCAL CLASSPATH FILES
            // ----------------------------------------------------------

            raw = readEnvFromClasspath("env/.env.auth", "AUTH_ENC_KEY");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:env/.env.auth", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readEnvFromClasspath("env/.env", "AUTH_ENC_KEY");
            if (isBlank(raw))
                raw = readEnvFromClasspath("env/.env", "ENCRYPTION_KEY");

            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:env/.env", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readSingleLineClasspath("encryption.key");
            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:encryption.key", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            raw = readFromClasspathProperties("enc.properties",
                    "auth.enc.key", "encryption.key");

            if (!isBlank(raw)) {
                logSource("LOCAL", "classpath:enc.properties", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            // ----------------------------------------------------------
            // 4️⃣ ENV variables
            // ----------------------------------------------------------
            raw = firstNonBlank(
                    System.getenv("AUTH_ENC_KEY"),
                    System.getenv("ENCRYPTION_KEY")
            );

            if (!isBlank(raw)) {
                logSource("ENV", "AUTH_ENC_KEY / ENCRYPTION_KEY", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            // ----------------------------------------------------------
            // 5️⃣ JVM system property
            // ----------------------------------------------------------
            raw = System.getProperty("auth.enc.key");
            if (!isBlank(raw)) {
                logSource("SYSTEM", "-Dauth.enc.key", raw);
                KEY_BYTES = normalize(raw);
                return;
            }

            throw new IllegalStateException("❌ No AES encryption key found in cloud/local/env/system sources.");
        }
    }

    // ============================================================================
    // LOGGING HELPERS
    // ============================================================================
    private static void logSource(String type, String source, String raw) {
        String preview = raw.length() > 10 ? raw.substring(0, 10) + "..." : raw;
        System.out.println("[ENC] ✔ Loaded from " + type + ": " + source);
        System.out.println("[ENC]   Key Preview: " + preview);
    }

    private static void logNormalized(byte[] bytes) {
        System.out.println("[ENC] ✔ Normalized to EXACT 32 bytes (AES-256)");
        System.out.println("[ENC]   Final Base64 length = " +
                Base64.getEncoder().encodeToString(bytes).length());
    }

    // ============================================================================
    // KEY NORMALIZATION (ALWAYS 32 BYTES)
    // ============================================================================
    private static byte[] normalize(String raw) {
        byte[] bytes;

        try {
            bytes = Base64.getDecoder().decode(raw);
        } catch (IllegalArgumentException ex) {
            bytes = raw.getBytes(StandardCharsets.UTF_8);
        }

        if (bytes.length != AES_LEN) {
            byte[] fixed = new byte[AES_LEN];
            System.arraycopy(bytes, 0, fixed, 0, Math.min(bytes.length, AES_LEN));
            bytes = fixed;
        }

        logNormalized(bytes);
        return bytes;
    }

    // ============================================================================
    // CLOUD FILE READERS
    // ============================================================================
    private static String readEnvFile(Path path, String key) {
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .map(String::trim)
                    .filter(l -> l.contains("="))
                    .map(l -> {
                        int idx = l.indexOf('=');
                        String k = l.substring(0, idx).trim();
                        String v = l.substring(idx + 1).trim();
                        return k.equals(key) ? v : null;
                    })
                    .filter(v -> !isBlank(v))
                    .findFirst().orElse(null);
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

    // ============================================================================
    // CLASSPATH READERS
    // ============================================================================
    private static String readEnvFromClasspath(String resource, String keyName) {
        try (InputStream in = EncryptionKeyProvider.class.getClassLoader()
                .getResourceAsStream(resource)) {

            if (in == null) return null;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                return br.lines()
                        .map(String::trim)
                        .filter(l -> l.contains("="))
                        .map(l -> {
                            int idx = l.indexOf('=');
                            String k = l.substring(0, idx).trim();
                            String v = l.substring(idx + 1).trim();
                            return k.equals(keyName) ? v : null;
                        })
                        .filter(v -> !isBlank(v))
                        .findFirst().orElse(null);
            }
        } catch (IOException ignored) {}
        return null;
    }

    private static String readSingleLineClasspath(String resource) {
        try (InputStream in = EncryptionKeyProvider.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            return line == null ? null : line.trim();
        } catch (IOException ignored) {}
        return null;
    }

    private static String readFromClasspathProperties(String resource, String... keys) {
        try (InputStream in = EncryptionKeyProvider.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return null;

            Properties p = new Properties();
            p.load(in);

            for (String k : keys) {
                String v = p.getProperty(k);
                if (!isBlank(v)) return v.trim();
            }
        } catch (IOException ignored) {}
        return null;
    }

    // ============================================================================
    // UTILITY HELPERS
    // ============================================================================
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (!isBlank(v)) return v;
        return null;
    }
}


