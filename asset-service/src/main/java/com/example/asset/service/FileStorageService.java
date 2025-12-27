
package com.example.asset.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/**
 * ✅ FileStorageService
 * Secure, token-aware file handling utility.
 * - Validates file uploads, prevents path traversal.
 * - Uses Authorization header for audit trail.
 * - Provides secure access and deletion of files.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final Path root;

    // ============================================================
    // 🔧 Constructor
    // ============================================================
    public FileStorageService() throws IOException {
        this.root = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(this.root);
        log.info("📁 File storage directory initialized at: {}", this.root);
    }

    // ============================================================
    // 🟢 STORE FILE (Validated Upload)
    // ============================================================
    public String store(HttpHeaders headers, MultipartFile file, String prefix) throws IOException {
        validateAuthorization(headers);
        validateFile(file);

        // Extract file extension safely
        String ext = getSafeExtension(file.getOriginalFilename());
        String filename = sanitizeFilename(prefix + "_" + UUID.randomUUID() + ext);
        Path target = this.root.resolve(filename).normalize();

        // Prevent path traversal
        if (!target.startsWith(this.root)) {
            throw new SecurityException("🚫 Invalid file path (path traversal detected)");
        }

        // Save the file
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String bearer = extractBearer(headers);
        log.info("📂 File uploaded [{}] by token={}", target, maskToken(bearer));
        return target.toString();
    }

    // ============================================================
    // ❌ DELETE FILE
    // ============================================================
    public boolean delete(HttpHeaders headers, String filepath) {
        validateAuthorization(headers);

        if (filepath == null || filepath.isBlank()) {
            log.warn("⚠️ Delete skipped - invalid file path");
            return false;
        }

        Path target = Paths.get(filepath).normalize();
        try {
            boolean deleted = Files.deleteIfExists(target);
            if (deleted) {
                log.info("🗑️ Deleted file: {} at {}", target, Instant.now());
            } else {
                log.warn("⚠️ File not found: {}", target);
            }
            return deleted;
        } catch (Exception e) {
            log.error("❌ Failed to delete file {}: {}", target, e.getMessage());
            return false;
        }
    }

    // ============================================================
    // 📥 DOWNLOAD FILE VALIDATION (used by FileDownloadController)
    // ============================================================
    public Path resolveFileSecurely(HttpHeaders headers, String filename) {
        validateAuthorization(headers);

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("❌ Filename cannot be empty");
        }

        Path target = this.root.resolve(sanitizeFilename(filename)).normalize();

        // Validate file path within upload root
        if (!target.startsWith(this.root)) {
            throw new SecurityException("🚫 Invalid file path - possible path traversal attempt");
        }

        if (!Files.exists(target)) {
            throw new IllegalArgumentException("⚠️ File not found: " + filename);
        }

        return target;
    }

    // ============================================================
    // 🔒 VALIDATION HELPERS
    // ============================================================
    private void validateAuthorization(HttpHeaders headers) {
        String bearer = extractBearer(headers);
        if (bearer == null || bearer.isBlank()) {
            throw new SecurityException("❌ Missing Authorization token");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty())
            throw new IllegalArgumentException("❌ File cannot be empty");

        if (file.getSize() > 20 * 1024 * 1024) // 20MB
            throw new IllegalArgumentException("❌ File exceeds 20MB limit");

        String original = file.getOriginalFilename();
        if (original == null || !original.matches("^[\\w,\\s-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("❌ Invalid file name or extension");
        }

        String ext = getSafeExtension(original).toLowerCase(Locale.ROOT);
        Set<String> allowed = Set.of(".jpg", ".jpeg", ".png", ".pdf", ".docx", ".xlsx");
        if (!allowed.contains(ext)) {
            throw new IllegalArgumentException("❌ File type not allowed: " + ext);
        }
    }

    private String getSafeExtension(String original) {
        if (original == null || !original.contains(".")) return "";
        return original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

    // ============================================================
    // 🔐 TOKEN HELPERS
    // ============================================================
    private String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new SecurityException("❌ Missing Authorization header");
        }
        return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
    }

    public String maskTokenFromHeader(HttpHeaders headers) {
        String bearer = headers.getFirst("Authorization");
        if (bearer == null) return "none";
        return maskToken(bearer);
    }

    private String maskToken(String bearer) {
        if (bearer == null || bearer.length() < 12) return "hidden";
        return bearer.substring(0, 12) + "...***";
    }
}


