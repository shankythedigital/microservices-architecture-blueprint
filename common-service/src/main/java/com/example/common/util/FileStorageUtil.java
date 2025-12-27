
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

/**
 * ✅ FileStorageUtil
 * Handles all physical file storage operations (save, retrieve, delete).
 * Default mode: stores files under `/uploads/{entityType}/` directory.
 *
 * Example saved path:
 *   uploads/ASSET/ASSET_2025-11-02_18-45-30_550e8400-e29b.pdf
 */
@Component
public class FileStorageUtil {

    private static final Logger log = LoggerFactory.getLogger(FileStorageUtil.class);
    private static final String BASE_DIR = "uploads";  // Relative to project root

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * ✅ Saves file and returns the relative path.
     */
    public String storeFile(MultipartFile file, String entityType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("❌ Cannot store empty file.");
        }

        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("❌ entityType cannot be null or empty.");
        }

        // Normalize type name (e.g. ASSET, AMC, WARRANTY)
        String typeDir = sanitizeName(entityType.toUpperCase());

        // Build storage directory path
        Path uploadDir = Paths.get(BASE_DIR, typeDir).toAbsolutePath().normalize();

        // Ensure directory exists
        Files.createDirectories(uploadDir);

        // Generate a unique, safe filename
        String originalName = sanitizeName(file.getOriginalFilename());
        String fileExt = getFileExtension(originalName);
        String uniqueName = typeDir + "_" +
                LocalDateTime.now().format(FORMATTER) + "_" +
                UUID.randomUUID() + (fileExt.isEmpty() ? "" : "." + fileExt);

        Path targetPath = uploadDir.resolve(uniqueName);

        // Copy file to target location (replace existing if needed)
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = BASE_DIR + "/" + typeDir + "/" + uniqueName;
        log.info("📁 File stored successfully: {}", relativePath);

        return relativePath;
    }

    /**
     * ✅ Reads file as Path for download/streaming.
     */
    public Path getFilePath(String relativePath) {
        Path path = Paths.get(relativePath).normalize().toAbsolutePath();
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("❌ File not found: " + relativePath);
        }
        return path;
    }

    /**
     * 🗑️ Deletes file from disk.
     */
    public boolean deleteFile(String relativePath) {
        try {
            Path path = Paths.get(relativePath).normalize().toAbsolutePath();
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("❌ Failed to delete file: {}", relativePath, e);
            return false;
        }
    }

    // ============================================================
    // 🧰 Helper methods
    // ============================================================

    private String sanitizeName(String name) {
        return name == null ? "UNKNOWN"
                : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0 && dotIndex < fileName.length() - 1)
                ? fileName.substring(dotIndex + 1)
                : "";
    }
}

