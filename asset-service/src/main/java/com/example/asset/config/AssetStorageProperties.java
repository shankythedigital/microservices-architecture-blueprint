package com.example.asset.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ✅ AssetStorageProperties
 * Ensures the upload directory is created and writable.
 */
@Configuration
@ConfigurationProperties(prefix = "asset.upload")
public class AssetStorageProperties {

    private static final Logger log = LoggerFactory.getLogger(AssetStorageProperties.class);

    private String dir;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    @PostConstruct
    public void init() {
        try {
            if (dir == null || dir.isBlank()) {
                throw new IllegalStateException("❌ Missing property: asset.upload.dir");
            }

            Path path = Path.of(dir).toAbsolutePath();
            log.info("🧩 Upload path configured as: {}", path);

            // Create directories if missing
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("📁 Created upload directory: {}", path);
            } else {
                log.info("📁 Upload directory already exists: {}", path);
            }

            File folder = path.toFile();
            if (!folder.canWrite()) {
                log.warn("⚠️ Upload directory is not writable: {}", path);
            }

        } catch (Exception e) {
            log.error("🚨 Failed to initialize upload directory '{}': {}", dir, e.getMessage());
            // Don’t stop the app — just log and continue
        }
    }
}


