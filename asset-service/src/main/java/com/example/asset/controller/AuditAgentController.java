package com.example.asset.controller;

import com.example.asset.dto.AuditAgentRequest;
import com.example.asset.entity.AuditLog;
import com.example.asset.service.AuditAgentService;
import com.example.common.util.ResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ✅ AuditAgentController
 * REST endpoints for audit logging and tracking.
 */
@RestController
@RequestMapping("/api/asset/v1/audit")
public class AuditAgentController {

    private static final Logger log = LoggerFactory.getLogger(AuditAgentController.class);
    private final AuditAgentService auditService;

    public AuditAgentController(AuditAgentService auditService) {
        this.auditService = auditService;
    }

    // ============================================================
    // 📝 LOG AUDIT EVENT (Unified endpoint for both simple and entity operations)
    // ============================================================
    @PostMapping("/log")
    public ResponseEntity<ResponseWrapper<AuditLog>> logEvent(
            @RequestHeader HttpHeaders headers,
            @RequestBody AuditAgentRequest request,
            HttpServletRequest httpRequest) {
        try {
            // Extract from JSON body (Postman collection format)
            String username = request.getUsername();
            String eventMessage = request.getEventMessage();
            String action = request.getAction();
            String entityType = request.getEntityType();
            Long entityId = request.getEntityId();
            Map<String, Object> oldValues = request.getOldValues();
            Map<String, Object> newValues = request.getNewValues();
            
            // If entityType/entityId/action provided, use logEntityOperation (Postman collection format)
            if (entityType != null && entityId != null) {
                String details = null;
                if (oldValues != null || newValues != null) {
                    details = String.format("Old: %s, New: %s", 
                            oldValues != null ? oldValues.toString() : "{}",
                            newValues != null ? newValues.toString() : "{}");
                }
                
                AuditLog auditLog = auditService.logEntityOperation(
                        username, action != null ? action : "OPERATION", 
                        entityType, entityId, details, httpRequest);
                return ResponseEntity.ok(new ResponseWrapper<>(
                        true, "Entity operation logged successfully", auditLog));
            } else {
                // Simple event logging (backward compatible)
                AuditLog auditLog = auditService.logEvent(username, eventMessage, httpRequest);
                return ResponseEntity.ok(new ResponseWrapper<>(
                        true, "Audit event logged successfully", auditLog));
            }
        } catch (Exception e) {
            log.error("❌ Failed to log audit event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 GET AUDIT LOGS
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<AuditLog>>> getAllAuditLogs(
            @RequestHeader HttpHeaders headers) {
        try {
            List<AuditLog> logs = auditService.getAllAuditLogs();
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Audit logs retrieved successfully", logs));
        } catch (Exception e) {
            log.error("❌ Failed to get audit logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ResponseWrapper<List<AuditLog>>> getAuditLogsByUsername(
            @RequestHeader HttpHeaders headers,
            @PathVariable String username) {
        try {
            List<AuditLog> logs = auditService.getAuditLogsByUsername(username);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Audit logs retrieved for user: " + username, logs));
        } catch (Exception e) {
            log.error("❌ Failed to get audit logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<ResponseWrapper<List<AuditLog>>> getAuditLogsByEntityType(
            @RequestHeader HttpHeaders headers,
            @PathVariable String entityType) {
        try {
            List<AuditLog> logs = auditService.getAuditLogsByEntityType(entityType);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Audit logs retrieved for entity type: " + entityType, logs));
        } catch (Exception e) {
            log.error("❌ Failed to get audit logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<ResponseWrapper<List<AuditLog>>> getAuditLogsByDateRange(
            @RequestHeader HttpHeaders headers,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<AuditLog> logs = auditService.getAuditLogsByDateRange(startDate, endDate);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Audit logs retrieved for date range", logs));
        } catch (Exception e) {
            log.error("❌ Failed to get audit logs: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ResponseWrapper<List<AuditLog>>> getRecentAuditLogs(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        try {
            List<AuditLog> logs = auditService.getRecentAuditLogs(limit);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Recent audit logs retrieved", logs));
        } catch (Exception e) {
            log.error("❌ Failed to get recent audit logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 SEARCH AUDIT LOGS
    // ============================================================
    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<List<AuditLog>>> searchAuditLogs(
            @RequestHeader HttpHeaders headers,
            @RequestParam("keyword") String keyword) {
        try {
            List<AuditLog> logs = auditService.searchAuditLogs(keyword);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Search completed", logs));
        } catch (Exception e) {
            log.error("❌ Failed to search audit logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📊 AUDIT STATISTICS
    // ============================================================
    @GetMapping("/statistics")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getAuditStatistics(
            @RequestHeader HttpHeaders headers) {
        try {
            Map<String, Object> stats = auditService.getAuditStatistics();
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Audit statistics retrieved", stats));
        } catch (Exception e) {
            log.error("❌ Failed to get audit statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🗑️ CLEANUP OPERATIONS
    // ============================================================
    @PostMapping("/cleanup")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> cleanupOldAuditLogs(
            @RequestHeader HttpHeaders headers,
            @RequestParam("daysToKeep") int daysToKeep) {
        try {
            int deletedCount = auditService.cleanupOldAuditLogs(daysToKeep);
            Map<String, Object> result = Map.of(
                    "deletedCount", deletedCount,
                    "daysToKeep", daysToKeep,
                    "message", "Cleanup completed successfully"
            );
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Cleanup completed", result));
        } catch (Exception e) {
            log.error("❌ Failed to cleanup audit logs: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }
}
