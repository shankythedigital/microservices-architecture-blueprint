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
            Path path = null;
            
            // If dir is configured, try to use it
            if (dir != null && !dir.isBlank()) {
                path = Path.of(dir).toAbsolutePath();
                log.info("🧩 Upload path configured as: {}", path);
                
                // Try to create/use the configured path
                try {
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                        log.info("📁 Created upload directory: {}", path);
                    } else {
                        log.info("📁 Upload directory already exists: {}", path);
                    }
                    
                    // Check if writable
                    File folder = path.toFile();
                    if (!folder.canWrite()) {
                        log.warn("⚠️ Configured upload directory is not writable: {}", path);
                        log.warn("   Falling back to default location...");
                        path = null; // Will use fallback
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Cannot use configured upload directory '{}': {}", path, e.getMessage());
                    log.warn("   Falling back to default location...");
                    path = null; // Will use fallback
                }
            }
            
            // Fallback to a safe default location if configured path failed
            if (path == null) {
                // Use project directory/uploads as fallback
                String userDir = System.getProperty("user.dir");
                path = Path.of(userDir, "uploads", "amc-docs").toAbsolutePath().normalize();
                log.info("📁 Using fallback upload directory: {}", path);
                
                // Update the dir property to the fallback
                this.dir = path.toString();
            }
            
            // Ensure the directory exists and is writable
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("✅ Created upload directory: {}", path);
            }
            
            File folder = path.toFile();
            if (!folder.canWrite()) {
                log.error("❌ Upload directory is not writable even after fallback: {}", path);
                log.error("   Please check permissions or configure a writable path in application.yml");
            } else {
                log.info("✅ Upload directory is ready: {}", path);
            }

        } catch (Exception e) {
            log.error("🚨 Failed to initialize upload directory: {}", e.getMessage());
            log.error("   The application will continue but file uploads may fail.");
            // Don't stop the app — just log and continue
        }
    }
}


