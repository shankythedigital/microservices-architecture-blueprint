package com.example.asset.dto;

import com.example.asset.entity.ComplianceViolation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ ComplianceCheckResult DTO
 * Result of a compliance validation check.
 */
public class ComplianceCheckResult {
    
    private String entityType;
    private Long entityId;
    private boolean compliant;
    private boolean hasBlockingViolations;
    private String status; // Status code (e.g., "COMPLIANT", "NON_COMPLIANT")
    private String message;
    private List<ComplianceViolation> violations;
    private LocalDateTime checkedAt;
    
    public ComplianceCheckResult() {
        this.violations = new ArrayList<>();
        this.checkedAt = LocalDateTime.now();
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public boolean isCompliant() {
        return compliant;
    }
    
    public void setCompliant(boolean compliant) {
        this.compliant = compliant;
        this.status = compliant ? "COMPLIANT" : "NON_COMPLIANT";
    }
    
    public boolean isHasBlockingViolations() {
        return hasBlockingViolations;
    }
    
    public void setHasBlockingViolations(boolean hasBlockingViolations) {
        this.hasBlockingViolations = hasBlockingViolations;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<ComplianceViolation> getViolations() {
        return violations;
    }
    
    public void setViolations(List<ComplianceViolation> violations) {
        this.violations = violations;
    }
    
    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }
    
    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }
}

