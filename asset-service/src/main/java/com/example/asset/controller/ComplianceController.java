package com.example.asset.controller;

import com.example.asset.dto.ComplianceCheckRequest;
import com.example.asset.dto.ComplianceCheckResult;
import com.example.asset.dto.ComplianceMetrics;
import com.example.common.util.ResponseWrapper;
import com.example.asset.entity.ComplianceViolation;
import com.example.asset.entity.ComplianceStatusMaster;
import com.example.asset.service.ComplianceMetricsService;
import com.example.asset.service.ComplianceAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ✅ ComplianceController
 * REST endpoints for validation and compliance checking.
 */
@RestController
@RequestMapping("/api/asset/v1/compliance")
public class ComplianceController {

    private static final Logger log = LoggerFactory.getLogger(ComplianceController.class);
    private final ComplianceAgentService complianceService;
    private final ComplianceMetricsService metricsService;

    public ComplianceController(ComplianceAgentService complianceService,
                               ComplianceMetricsService metricsService) {
        this.complianceService = complianceService;
        this.metricsService = metricsService;
    }

    // ============================================================
    // 🔍 VALIDATE ENTITY
    // ============================================================
    @PostMapping("/validate")
    public ResponseEntity<ResponseWrapper<ComplianceCheckResult>> validateEntity(
            @RequestHeader HttpHeaders headers,
            @RequestBody ComplianceCheckRequest request) {
        try {
            log.info("🔍 Validating entity: {} {}", request.getEntityType(), request.getEntityId());
            
            ComplianceCheckResult result = complianceService.validateEntityById(
                    request.getEntityType(), request.getEntityId());
            
            log.info("✅ Compliance check completed: compliant={}, violations={}",
                    result.isCompliant(), result.getViolations().size());
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    result.getMessage(),
                    result));
        } catch (Exception e) {
            log.error("❌ Compliance validation failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 VALIDATE ENTITY BY TYPE AND ID
    // ============================================================
    @GetMapping("/validate/{entityType}/{entityId}")
    public ResponseEntity<ResponseWrapper<ComplianceCheckResult>> validateEntityById(
            @RequestHeader HttpHeaders headers,
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        try {
            log.info("🔍 Validating entity: {} {}", entityType, entityId);
            
            ComplianceCheckResult result = complianceService.validateEntityById(entityType, entityId);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    result.getMessage(),
                    result));
        } catch (Exception e) {
            log.error("❌ Compliance validation failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 GET COMPLIANCE STATUS
    // ============================================================
    @GetMapping("/status/{entityType}/{entityId}")
    public ResponseEntity<ResponseWrapper<ComplianceStatusMaster>> getComplianceStatus(
            @RequestHeader HttpHeaders headers,
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        try {
            ComplianceStatusMaster status = complianceService.getComplianceStatus(entityType, entityId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Compliance status retrieved",
                    status));
        } catch (Exception e) {
            log.error("❌ Failed to get compliance status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 GET VIOLATIONS
    // ============================================================
    @GetMapping("/violations/{entityType}/{entityId}")
    public ResponseEntity<ResponseWrapper<List<ComplianceViolation>>> getViolations(
            @RequestHeader HttpHeaders headers,
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(value = "unresolvedOnly", defaultValue = "true") boolean unresolvedOnly) {
        try {
            List<ComplianceViolation> violations = complianceService.getViolations(
                    entityType, entityId, unresolvedOnly);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Violations retrieved: " + violations.size(),
                    violations));
        } catch (Exception e) {
            log.error("❌ Failed to get violations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✅ RESOLVE VIOLATION
    // ============================================================
    @PostMapping("/violations/{violationId}/resolve")
    public ResponseEntity<ResponseWrapper<ComplianceViolation>> resolveViolation(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long violationId,
            @RequestParam("resolvedBy") String resolvedBy,
            @RequestParam(value = "notes", required = false) String notes) {
        try {
            ComplianceViolation violation = complianceService.resolveViolation(
                    violationId, resolvedBy, notes);
            
            log.info("✅ Violation {} resolved by {}", violationId, resolvedBy);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Violation resolved successfully",
                    violation));
        } catch (Exception e) {
            log.error("❌ Failed to resolve violation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📊 COMPLIANCE REPORT
    // ============================================================
    @GetMapping("/report/{entityType}/{entityId}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> generateReport(
            @RequestHeader HttpHeaders headers,
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        try {
            Map<String, Object> report = complianceService.generateComplianceReport(
                    entityType, entityId);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Compliance report generated",
                    report));
        } catch (Exception e) {
            log.error("❌ Failed to generate compliance report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔄 BULK VALIDATION
    // ============================================================
    @PostMapping("/validate/bulk/{entityType}")
    public ResponseEntity<ResponseWrapper<List<ComplianceCheckResult>>> validateBulk(
            @RequestHeader HttpHeaders headers,
            @PathVariable String entityType,
            @RequestBody List<Long> entityIds) {
        try {
            log.info("🔄 Bulk validation for {} entities: {}", entityType, entityIds.size());
            
            List<ComplianceCheckResult> results = complianceService.validateBulk(
                    entityType, entityIds);
            
            long compliantCount = results.stream().filter(ComplianceCheckResult::isCompliant).count();
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    String.format("Bulk validation completed: %d/%d compliant",
                            compliantCount, results.size()),
                    results));
        } catch (Exception e) {
            log.error("❌ Bulk validation failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📊 COMPLIANCE METRICS
    // ============================================================
    @GetMapping("/metrics")
    public ResponseEntity<ResponseWrapper<ComplianceMetrics>> getMetrics(
            @RequestHeader HttpHeaders headers) {
        try {
            ComplianceMetrics metrics = metricsService.generateOverallMetrics();
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Compliance metrics retrieved",
                    metrics));
        } catch (Exception e) {
            log.error("❌ Failed to get compliance metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📊 COMPLIANCE METRICS BY ENTITY TYPE
    // ============================================================
    @GetMapping("/metrics/{entityType}")
    public ResponseEntity<ResponseWrapper<ComplianceMetrics>> getMetricsByEntityType(
            @RequestHeader HttpHeaders headers,
            @PathVariable String entityType) {
        try {
            ComplianceMetrics metrics = metricsService.generateMetricsByEntityType(entityType);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Compliance metrics retrieved for " + entityType,
                    metrics));
        } catch (Exception e) {
            log.error("❌ Failed to get compliance metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📊 VIOLATIONS SUMMARY
    // ============================================================
    @GetMapping("/violations/summary")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getViolationsSummary(
            @RequestHeader HttpHeaders headers) {
        try {
            Map<String, Object> summary = metricsService.getViolationsSummary();
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Violations summary retrieved",
                    summary));
        } catch (Exception e) {
            log.error("❌ Failed to get violations summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }
}

