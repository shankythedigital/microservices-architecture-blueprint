
package com.example.asset.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * ✅ UploadAuditLogger
 * Centralized utility to log details of uploaded files for audit tracking.
 */
@Component
public class UploadAuditLogger {

    private static final Logger log = LoggerFactory.getLogger(UploadAuditLogger.class);

    /**
     * Logs detailed information about an uploaded file.
     *
     * @param username  user performing upload
     * @param userId    ID of user
     * @param file      uploaded file
     * @param targetPath path where file is stored
     */
    public void logUpload(String username, Long userId, MultipartFile file, Path targetPath) {
        if (file == null) return;

        log.info("""
                📁 [UPLOAD AUDIT]
                ├── User        : {} (ID: {})
                ├── File Name   : {}
                ├── File Size   : {} bytes
                ├── Content Type: {}
                ├── Saved Path  : {}
                ├── Uploaded At : {}
                └── Status      : ✅ SUCCESS
                """,
                username,
                userId,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                targetPath != null ? targetPath.toAbsolutePath() : "N/A",
                DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        );
    }

    /**
     * Logs file upload failure.
     */
    public void logFailure(String username, Long userId, MultipartFile file, String reason) {
        log.error("""
                ❌ [UPLOAD FAILED]
                ├── User        : {} (ID: {})
                ├── File Name   : {}
                ├── Reason      : {}
                └── Timestamp   : {}
                """,
                username,
                userId,
                file != null ? file.getOriginalFilename() : "Unknown",
                reason,
                DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        );
    }
}



