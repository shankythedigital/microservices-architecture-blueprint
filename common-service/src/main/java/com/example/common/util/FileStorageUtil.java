
// package com.example.common.util;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Component;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.IOException;
// import java.nio.file.*;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.UUID;

// /**
//  * ✅ FileStorageUtil
//  * Handles all physical file storage operations (save, retrieve, delete).
//  * Default mode: stores files under `/uploads/{entityType}/` directory.
//  *
//  * Example saved path:
//  *   uploads/ASSET/ASSET_2025-11-02_18-45-30_550e8400-e29b.pdf
//  */
// @Component
// public class FileStorageUtil {

//     private static final Logger log = LoggerFactory.getLogger(FileStorageUtil.class);
//     private static final String BASE_DIR = "uploads";  // Relative to project root

//     private static final DateTimeFormatter FORMATTER =
//             DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

//     /**
//      * ✅ Saves file and returns the relative path.
//      */
//     public String storeFile(MultipartFile file, String entityType) throws IOException {
//         if (file == null || file.isEmpty()) {
//             throw new IllegalArgumentException("❌ Cannot store empty file.");
//         }

//         if (entityType == null || entityType.isBlank()) {
//             throw new IllegalArgumentException("❌ entityType cannot be null or empty.");
//         }

//         // Normalize type name (e.g. ASSET, AMC, WARRANTY)
//         String typeDir = sanitizeName(entityType.toUpperCase());

//         // Build storage directory path
//         Path uploadDir = Paths.get(BASE_DIR, typeDir).toAbsolutePath().normalize();

//         // Ensure directory exists
//         Files.createDirectories(uploadDir);

//         // Generate a unique, safe filename
//         String originalName = sanitizeName(file.getOriginalFilename());
//         String fileExt = getFileExtension(originalName);
//         String uniqueName = typeDir + "_" +
//                 LocalDateTime.now().format(FORMATTER) + "_" +
//                 UUID.randomUUID() + (fileExt.isEmpty() ? "" : "." + fileExt);

//         Path targetPath = uploadDir.resolve(uniqueName);

//         // Copy file to target location (replace existing if needed)
//         Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

//         String relativePath = BASE_DIR + "/" + typeDir + "/" + uniqueName;
//         log.info("📁 File stored successfully: {}", relativePath);

//         return relativePath;
//     }

//     /**
//      * ✅ Reads file as Path for download/streaming.
//      */
//     public Path getFilePath(String relativePath) {
//         Path path = Paths.get(relativePath).normalize().toAbsolutePath();
//         if (!Files.exists(path)) {
//             throw new IllegalArgumentException("❌ File not found: " + relativePath);
//         }
//         return path;
//     }

//     /**
//      * 🗑️ Deletes file from disk.
//      */
//     public boolean deleteFile(String relativePath) {
//         try {
//             Path path = Paths.get(relativePath).normalize().toAbsolutePath();
//             return Files.deleteIfExists(path);
//         } catch (IOException e) {
//             log.error("❌ Failed to delete file: {}", relativePath, e);
//             return false;
//         }
//     }

//     // ============================================================
//     // 🧰 Helper methods
//     // ============================================================

//     private String sanitizeName(String name) {
//         return name == null ? "UNKNOWN"
//                 : name.replaceAll("[^a-zA-Z0-9._-]", "_");
//     }

//     private String getFileExtension(String fileName) {
//         int dotIndex = fileName.lastIndexOf('.');
//         return (dotIndex > 0 && dotIndex < fileName.length() - 1)
//                 ? fileName.substring(dotIndex + 1)
//                 : "";
//     }
// }



package com.example.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;



import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;

/**
 * ✅ FileStorageUtil
 *
 * Supports 2 storage modes:
 *
 * 1. LOCAL  -> Stores file in local uploads directory
 * 2. SUPABASE -> Uploads file to Supabase Storage bucket
 *
 * Controlled using flag:
 *
 * storage.mode=LOCAL
 * OR
 * storage.mode=SUPABASE
 */
@Component
public class FileStorageUtil {

    private static final Logger log = LoggerFactory.getLogger(FileStorageUtil.class);

    // ============================================================
    // ✅ STORAGE MODE FLAG
    // ============================================================

    @Value("${storage.mode:LOCAL}")
    private String storageMode;

    // ============================================================
    // ✅ LOCAL STORAGE CONFIG
    // ============================================================

    private static final String BASE_DIR = "uploads";

    // ============================================================
    // ✅ SUPABASE CONFIG
    // ============================================================

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket}")
    private String supabaseBucket;

    @Value("${supabase.api.key}")
    private String supabaseApiKey;

    // ============================================================

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final OkHttpClient client = new OkHttpClient();

    /**
     * ✅ Main method
     */
    public String storeFile(MultipartFile file, String entityType) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("❌ Cannot store empty file.");
        }

        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("❌ entityType cannot be null or empty.");
        }

        // ✅ Decide storage based on flag
        if ("SUPABASE".equalsIgnoreCase(storageMode)) {
            return storeFileInSupabase(file, entityType);
        }

        return storeFileLocally(file, entityType);
    }

    // ============================================================
    // ✅ LOCAL STORAGE
    // ============================================================

    private String storeFileLocally(MultipartFile file, String entityType) throws IOException {

        String typeDir = sanitizeName(entityType.toUpperCase());

        Path uploadDir = Paths.get(BASE_DIR, typeDir)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(uploadDir);

        String uniqueName = generateUniqueFileName(file, typeDir);

        Path targetPath = uploadDir.resolve(uniqueName);

        Files.copy(
                file.getInputStream(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        String relativePath = BASE_DIR + "/" + typeDir + "/" + uniqueName;

        log.info("📁 File stored locally: {}", relativePath);

        return relativePath;
    }

    // ============================================================
    // ✅ SUPABASE STORAGE
    // ============================================================

    private String storeFileInSupabase(MultipartFile file, String entityType) throws IOException {

        String typeDir = sanitizeName(entityType.toUpperCase());

        String uniqueName = generateUniqueFileName(file, typeDir);

        // Example:
        // ASSET/ASSET_2026-05-28_10-22-33_uuid.pdf
        String objectPath = typeDir + "/" + uniqueName;

        String uploadUrl =
                supabaseUrl +
                "/storage/v1/object/" +
                supabaseBucket +
                "/" +
                objectPath;

        RequestBody requestBody = RequestBody.create(
                file.getBytes(),
                MediaType.parse(file.getContentType())
        );

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .addHeader("apikey", supabaseApiKey)
                .addHeader("Authorization", "Bearer " + supabaseApiKey)
                .addHeader("Content-Type", file.getContentType())
                .addHeader("x-upsert", "true")
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {

                String errorBody = response.body() != null
                        ? response.body().string()
                        : "Unknown error";

                log.error("❌ Supabase upload failed: {}", errorBody);

                throw new RuntimeException(
                        "Failed to upload file to Supabase: " + errorBody
                );
            }
        }

        // Public URL
        String publicUrl =
                supabaseUrl +
                "/storage/v1/object/public/" +
                supabaseBucket +
                "/" +
                objectPath;

        log.info("☁️ File uploaded to Supabase: {}", publicUrl);

        return publicUrl;
    }

    // ============================================================
    // ✅ GET FILE PATH (LOCAL ONLY)
    // ============================================================

    public Path getFilePath(String relativePath) {

        if ("SUPABASE".equalsIgnoreCase(storageMode)) {
            throw new UnsupportedOperationException(
                    "Direct file path not supported for Supabase storage."
            );
        }

        Path path = Paths.get(relativePath)
                .normalize()
                .toAbsolutePath();

        if (!Files.exists(path)) {
            throw new IllegalArgumentException(
                    "❌ File not found: " + relativePath
            );
        }

        return path;
    }

    // ============================================================
    // ✅ DELETE FILE
    // ============================================================

    public boolean deleteFile(String pathOrUrl) {

        try {

            if ("SUPABASE".equalsIgnoreCase(storageMode)) {
                return deleteFileFromSupabase(pathOrUrl);
            }

            Path path = Paths.get(pathOrUrl)
                    .normalize()
                    .toAbsolutePath();

            return Files.deleteIfExists(path);

        } catch (Exception e) {

            log.error("❌ Failed to delete file: {}", pathOrUrl, e);

            return false;
        }
    }

    // ============================================================
    // ✅ DELETE FROM SUPABASE
    // ============================================================

    private boolean deleteFileFromSupabase(String publicUrl) {

        try {

            String objectPath = extractObjectPath(publicUrl);

            String deleteUrl =
                    supabaseUrl +
                    "/storage/v1/object/" +
                    supabaseBucket +
                    "/" +
                    objectPath;

            Request request = new Request.Builder()
                    .url(deleteUrl)
                    .delete()
                    .addHeader("apikey", supabaseApiKey)
                    .addHeader("Authorization", "Bearer " + supabaseApiKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {

                if (!response.isSuccessful()) {

                    log.error("❌ Failed to delete from Supabase");

                    return false;
                }
            }

            log.info("🗑️ File deleted from Supabase: {}", publicUrl);

            return true;

        } catch (Exception e) {

            log.error("❌ Supabase delete error", e);

            return false;
        }
    }

    // ============================================================
    // 🧰 HELPERS
    // ============================================================

    private String generateUniqueFileName(
            MultipartFile file,
            String typeDir
    ) {

        String originalName = sanitizeName(file.getOriginalFilename());

        String fileExt = getFileExtension(originalName);

        return typeDir + "_" +
                LocalDateTime.now().format(FORMATTER) + "_" +
                UUID.randomUUID() +
                (fileExt.isEmpty() ? "" : "." + fileExt);
    }

    private String sanitizeName(String name) {

        return name == null
                ? "UNKNOWN"
                : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getFileExtension(String fileName) {

        int dotIndex = fileName.lastIndexOf('.');

        return (dotIndex > 0 && dotIndex < fileName.length() - 1)
                ? fileName.substring(dotIndex + 1)
                : "";
    }

    /**
     * Extracts:
     * ASSET/file.pdf
     * from public URL
     */
    private String extractObjectPath(String publicUrl) {

        String prefix =
                "/storage/v1/object/public/" +
                supabaseBucket +
                "/";

        int index = publicUrl.indexOf(prefix);

        if (index == -1) {
            throw new IllegalArgumentException(
                    "Invalid Supabase public URL"
            );
        }

        return publicUrl.substring(index + prefix.length());
    }
}
