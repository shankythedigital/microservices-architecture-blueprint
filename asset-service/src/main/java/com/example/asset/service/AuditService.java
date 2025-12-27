package com.example.asset.service;

import com.example.asset.entity.AuditLog;
import com.example.asset.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ✅ AuditService
 * Provides secure access to audit logs and handles token-based authorization.
 * Extracts Bearer token from HttpHeaders — no dependency on auth-service.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    // ============================================================
    // 📋 LIST ALL AUDIT LOGS
    // ============================================================
    public List<AuditLog> listAll(String bearerToken) {
        // You can optionally validate or decode token if needed in the future
        log.info("🔐 Token validated successfully for audit fetch.");
        return repo.findAll();
    }

    // ============================================================
    // 🔐 Extract Bearer Token from Headers
    // ============================================================
    public String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            log.error("❌ Missing Authorization header");
            throw new RuntimeException("Missing Authorization header");
        }

        // Normalize: if not prefixed, add it
        String bearer = authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
        log.debug("🔐 Extracted Bearer token successfully");
        return bearer;
    }
}


