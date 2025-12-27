
package com.example.asset.controller;

import com.example.asset.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ✅ FileDownloadController
 * Handles secure download and preview of stored files.
 * Requires Authorization header for every request.
 */
@RestController
@RequestMapping("/api/asset/v1/files")
public class FileDownloadController {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);
    private final FileStorageService fileStorageService;

    public FileDownloadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // ============================================================
    // 📥 DOWNLOAD OR VIEW FILE
    // ============================================================
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestHeader HttpHeaders headers,
            @RequestParam("filename") String filename,
            @RequestParam(value = "inline", required = false, defaultValue = "false") boolean inline) {
        try {
            // 🔐 Validate and get file safely
            Path filePath = fileStorageService.resolveFileSecurely(headers, filename);
            File file = filePath.toFile();
            if (!file.exists() || !file.isFile()) {
                log.warn("⚠️ File not found: {}", filename);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String mimeType = Files.probeContentType(filePath);
            mimeType = (mimeType != null) ? mimeType : "application/octet-stream";

            FileSystemResource resource = new FileSystemResource(file);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType(mimeType));
            String disposition = inline ? "inline" : "attachment";
            responseHeaders.setContentDisposition(
                    ContentDisposition.builder(disposition)
                            .filename(file.getName())
                            .build()
            );

            log.info("📤 File {} served successfully as {} by token={}", filename, disposition,
                    fileStorageService.maskTokenFromHeader(headers));

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .contentLength(file.length())
                    .body(resource);

        } catch (SecurityException e) {
            log.error("❌ Unauthorized access attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("Error", e.getMessage()).build();

        } catch (Exception e) {
            log.error("❌ Failed to serve file {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Error", e.getMessage()).build();
        }
    }
}

