package com.example.asset.dto;

/**
 * ✅ ComplianceCheckRequest DTO
 * Request for compliance validation.
 */
public class ComplianceCheckRequest {
    
    private String entityType;
    private Long entityId;
    private Boolean autoResolve; // Auto-resolve violations if possible
    
    public ComplianceCheckRequest() {}
    
    public ComplianceCheckRequest(String entityType, Long entityId) {
        this.entityType = entityType;
        this.entityId = entityId;
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
    
    public Boolean getAutoResolve() {
        return autoResolve;
    }
    
    public void setAutoResolve(Boolean autoResolve) {
        this.autoResolve = autoResolve;
    }
}
