package com.example.asset.service;

import com.example.asset.entity.AuditLog;
import com.example.asset.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ AuditAgentService
 * Comprehensive agent for audit logging and tracking.
 * Handles all audit operations with edge case management.
 */
@Service
public class AuditAgentService {

    private static final Logger log = LoggerFactory.getLogger(AuditAgentService.class);

    private final AuditLogRepository auditRepo;

    public AuditAgentService(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    // ============================================================
    // 📝 LOG AUDIT EVENT
    // ============================================================
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logEvent(String username, String eventMessage, HttpServletRequest request) {
        // Edge case: Null username
        if (!StringUtils.hasText(username)) {
            username = "SYSTEM";
            log.warn("⚠️ Audit event with null username, using SYSTEM");
        }

        // Edge case: Null event message
        if (!StringUtils.hasText(eventMessage)) {
            eventMessage = "Unknown event";
            log.warn("⚠️ Audit event with null message, using default");
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setEventMessage(eventMessage);
        auditLog.setEventTime(LocalDateTime.now());
        auditLog.setCreatedBy(username);
        auditLog.setUpdatedBy(username);
        auditLog.setActive(true);

        // Extract request information if available
        if (request != null) {
            auditLog.setIpAddress(extractIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setUrl(request.getRequestURI());
            auditLog.setHttpMethod(request.getMethod());
        }

        AuditLog saved = auditRepo.save(auditLog);
        log.debug("📝 Audit event logged: {} - {}", username, eventMessage);
        return saved;
    }

    // ============================================================
    // 📝 LOG AUDIT EVENT (Detailed)
    // ============================================================
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logEvent(String username, String eventMessage, String ipAddress,
                            String userAgent, String url, String httpMethod) {
        // Edge case: Null username
        if (!StringUtils.hasText(username)) {
            username = "SYSTEM";
        }

        // Edge case: Null event message
        if (!StringUtils.hasText(eventMessage)) {
            eventMessage = "Unknown event";
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setEventMessage(eventMessage);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setUrl(url);
        auditLog.setHttpMethod(httpMethod);
        auditLog.setEventTime(LocalDateTime.now());
        auditLog.setCreatedBy(username);
        auditLog.setUpdatedBy(username);
        auditLog.setActive(true);

        AuditLog saved = auditRepo.save(auditLog);
        log.debug("📝 Audit event logged: {} - {}", username, eventMessage);
        return saved;
    }

    // ============================================================
    // 📝 LOG ENTITY OPERATION
    // ============================================================
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logEntityOperation(String username, String operation, String entityType,
                                      Long entityId, String details, HttpServletRequest request) {
        String eventMessage = String.format("%s %s %s (ID: %d)", 
                username, operation, entityType, entityId);
        if (StringUtils.hasText(details)) {
            eventMessage += " - " + details;
        }

        return logEvent(username, eventMessage, request);
    }

    // ============================================================
    // 📋 GET AUDIT LOGS
    // ============================================================
    public List<AuditLog> getAllAuditLogs() {
        return auditRepo.findAll();
    }

    public List<AuditLog> getAuditLogsByUsername(String username) {
        // Edge case: Null username
        if (!StringUtils.hasText(username)) {
            return Collections.emptyList();
        }

        return auditRepo.findAll().stream()
                .filter(log -> username.equalsIgnoreCase(log.getUsername()))
                .sorted(Comparator.comparing(AuditLog::getEventTime).reversed())
                .collect(Collectors.toList());
    }

    public List<AuditLog> getAuditLogsByEntityType(String entityType) {
        // Edge case: Null entity type
        if (!StringUtils.hasText(entityType)) {
            return Collections.emptyList();
        }

        return auditRepo.findAll().stream()
                .filter(log -> log.getEventMessage() != null && 
                              log.getEventMessage().contains(entityType.toUpperCase()))
                .sorted(Comparator.comparing(AuditLog::getEventTime).reversed())
                .collect(Collectors.toList());
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // Edge case: Null dates
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("❌ Start date and end date cannot be null");
        }

        // Edge case: Invalid date range
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("❌ Start date cannot be after end date");
        }

        return auditRepo.findAll().stream()
                .filter(log -> {
                    LocalDateTime eventTime = log.getEventTime();
                    return eventTime != null && 
                           !eventTime.isBefore(startDate) && 
                           !eventTime.isAfter(endDate);
                })
                .sorted(Comparator.comparing(AuditLog::getEventTime).reversed())
                .collect(Collectors.toList());
    }

    public List<AuditLog> getRecentAuditLogs(int limit) {
        // Edge case: Invalid limit
        if (limit <= 0) {
            limit = 100; // Default limit
        }
        if (limit > 1000) {
            limit = 1000; // Max limit to prevent performance issues
        }

        return auditRepo.findAll().stream()
                .sorted(Comparator.comparing(AuditLog::getEventTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 📊 AUDIT STATISTICS
    // ============================================================
    public Map<String, Object> getAuditStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<AuditLog> allLogs = auditRepo.findAll();

        stats.put("totalLogs", allLogs.size());

        // Count by username
        Map<String, Long> byUsername = allLogs.stream()
                .filter(log -> log.getUsername() != null)
                .collect(Collectors.groupingBy(AuditLog::getUsername, Collectors.counting()));
        stats.put("logsByUsername", byUsername);
        stats.put("uniqueUsers", byUsername.size());

        // Count by HTTP method
        Map<String, Long> byMethod = allLogs.stream()
                .filter(log -> log.getHttpMethod() != null)
                .collect(Collectors.groupingBy(AuditLog::getHttpMethod, Collectors.counting()));
        stats.put("logsByMethod", byMethod);

        // Count by URL pattern
        Map<String, Long> byUrl = allLogs.stream()
                .filter(log -> log.getUrl() != null)
                .collect(Collectors.groupingBy(
                        log -> extractUrlPattern(log.getUrl()),
                        Collectors.counting()));
        stats.put("logsByUrlPattern", byUrl);

        // Recent activity (last 24 hours)
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        long recentActivity = allLogs.stream()
                .filter(log -> log.getEventTime() != null && log.getEventTime().isAfter(yesterday))
                .count();
        stats.put("recentActivity24h", recentActivity);

        return stats;
    }

    // ============================================================
    // 🧩 HELPER METHODS
    // ============================================================
    private String extractIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // Edge case: Handle proxy headers
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Edge case: Multiple IPs in X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    private String extractUrlPattern(String url) {
        if (url == null || url.isEmpty()) {
            return "UNKNOWN";
        }

        // Extract base path pattern (remove IDs and query params)
        String pattern = url.split("\\?")[0]; // Remove query params
        pattern = pattern.replaceAll("/\\d+", "/{id}"); // Replace IDs with placeholder
        return pattern;
    }

    // ============================================================
    // 🔍 SEARCH AUDIT LOGS
    // ============================================================
    public List<AuditLog> searchAuditLogs(String keyword) {
        // Edge case: Null or empty keyword
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        String searchTerm = keyword.toLowerCase();
        return auditRepo.findAll().stream()
                .filter(log -> {
                    boolean matches = false;
                    if (log.getUsername() != null && log.getUsername().toLowerCase().contains(searchTerm)) {
                        matches = true;
                    }
                    if (log.getEventMessage() != null && log.getEventMessage().toLowerCase().contains(searchTerm)) {
                        matches = true;
                    }
                    if (log.getUrl() != null && log.getUrl().toLowerCase().contains(searchTerm)) {
                        matches = true;
                    }
                    return matches;
                })
                .sorted(Comparator.comparing(AuditLog::getEventTime).reversed())
                .collect(Collectors.toList());
    }

    // ============================================================
    // 🗑️ CLEANUP OLD AUDIT LOGS
    // ============================================================
    @Transactional
    public int cleanupOldAuditLogs(int daysToKeep) {
        // Edge case: Invalid days
        if (daysToKeep < 0) {
            throw new IllegalArgumentException("❌ Days to keep cannot be negative");
        }
        if (daysToKeep < 30) {
            log.warn("⚠️ Cleaning up logs older than {} days - this may be too aggressive", daysToKeep);
        }

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        List<AuditLog> oldLogs = auditRepo.findAll().stream()
                .filter(log -> log.getEventTime() != null && log.getEventTime().isBefore(cutoffDate))
                .collect(Collectors.toList());

        // Soft delete
        oldLogs.forEach(log -> {
            log.setActive(false);
            log.setUpdatedBy("SYSTEM");
        });
        auditRepo.saveAll(oldLogs);

        log.info("🗑️ Cleaned up {} old audit logs (older than {} days)", oldLogs.size(), daysToKeep);
        return oldLogs.size();
    }
}

