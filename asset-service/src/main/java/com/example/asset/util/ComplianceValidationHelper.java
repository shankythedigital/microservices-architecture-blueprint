package com.example.asset.util;

import com.example.asset.dto.ComplianceCheckResult;
import com.example.asset.entity.ComplianceViolation;
import com.example.asset.entity.ComplianceStatusMaster;
import com.example.asset.exception.ComplianceException;
import com.example.asset.service.ComplianceAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ ComplianceValidationHelper
 * Utility class for integrating compliance validation into existing services.
 * Provides helper methods to validate entities before save operations.
 */
@Component
public class ComplianceValidationHelper {

    private static final Logger log = LoggerFactory.getLogger(ComplianceValidationHelper.class);
    private final ComplianceAgentService complianceService;

    public ComplianceValidationHelper(ComplianceAgentService complianceService) {
        this.complianceService = complianceService;
    }

    // ============================================================
    // 🔍 VALIDATE BEFORE CREATE
    // ============================================================
    public void validateBeforeCreate(String entityType, Object entity) {
        ComplianceCheckResult result = complianceService.validateEntity(entityType, null, entity);
        
        if (!result.isCompliant()) {
            List<ComplianceViolation> blockingViolations = result.getViolations().stream()
                    .filter(v -> v.getRule() != null && v.getRule().getBlocksOperation())
                    .collect(Collectors.toList());
            
            if (!blockingViolations.isEmpty()) {
                throw new ComplianceException(
                        "Entity failed compliance validation with " + blockingViolations.size() + " blocking violation(s)",
                        blockingViolations,
                        true);
            }
            
            // Log non-blocking violations
            log.warn("⚠️ Entity {} has {} non-blocking compliance violations", 
                    entityType, result.getViolations().size());
        }
    }

    // ============================================================
    // 🔍 VALIDATE BEFORE UPDATE
    // ============================================================
    public void validateBeforeUpdate(String entityType, Long entityId, Object entity) {
        ComplianceCheckResult result = complianceService.validateEntity(entityType, entityId, entity);
        
        if (!result.isCompliant()) {
            List<ComplianceViolation> blockingViolations = result.getViolations().stream()
                    .filter(v -> v.getRule() != null && v.getRule().getBlocksOperation())
                    .collect(Collectors.toList());
            
            if (!blockingViolations.isEmpty()) {
                throw new ComplianceException(
                        "Entity failed compliance validation with " + blockingViolations.size() + " blocking violation(s)",
                        blockingViolations,
                        true);
            }
            
            log.warn("⚠️ Entity {} {} has {} non-blocking compliance violations", 
                    entityType, entityId, result.getViolations().size());
        }
    }

    // ============================================================
    // 🔍 VALIDATE AND GET RESULT
    // ============================================================
    public ComplianceCheckResult validateAndGetResult(String entityType, Long entityId, Object entity) {
        return complianceService.validateEntity(entityType, entityId, entity);
    }

    // ============================================================
    // ✅ CHECK IF COMPLIANT
    // ============================================================
    public boolean isCompliant(String entityType, Long entityId) {
        ComplianceStatusMaster status = complianceService.getComplianceStatus(entityType, entityId);
        return status != null && 
               status.getIsResolved() != null && 
               status.getIsResolved() &&
               "COMPLIANT".equalsIgnoreCase(status.getCode());
    }

    // ============================================================
    // 📋 GET VIOLATIONS
    // ============================================================
    public List<ComplianceViolation> getViolations(String entityType, Long entityId, boolean unresolvedOnly) {
        return complianceService.getViolations(entityType, entityId, unresolvedOnly);
    }
}

