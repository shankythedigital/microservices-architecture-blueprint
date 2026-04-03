package com.example.helpdesk.dto;

import com.example.common.util.PiiMaskingUtil;
import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.IssueStatus;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import java.time.LocalDateTime;

/**
 * 🔐 DPDPA Compliance: All PII fields are automatically masked when serialized to JSON.
 */
public class IssueResponse {
    private Long id;
    private String title;
    private String description;
    private IssueStatus status;
    private IssuePriority priority;
    private RelatedService relatedService;
    private String reportedBy;    // Masked in getter
    private String assignedTo;    // Masked in getter
    
    // Internal storage for unmasked values
    private transient String reportedByUnmasked;
    private transient String assignedToUnmasked;
    private SupportLevel currentSupportLevel;
    private SupportLevel initialSupportLevel;
    private LocalDateTime assignedAt;
    private LocalDateTime firstResponseAt;
    private String resolution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private Integer escalationCount;
    private LocalDateTime lastEscalatedAt;
    private SLATrackingResponse slaTracking;
    private Long assetId;
    private Long componentId;
    private Long sparePartId;
    private Long issueMasterId;
    private Long categoryId;
    private Long subCategoryId;
    private Long loginUserId;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }
    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }
    public RelatedService getRelatedService() { return relatedService; }
    public void setRelatedService(RelatedService relatedService) { this.relatedService = relatedService; }
    
    /**
     * 🔐 DPDPA Compliance: Returns masked reportedBy
     */
    public String getReportedBy() {
        if (reportedByUnmasked != null) {
            return PiiMaskingUtil.maskUserId(reportedByUnmasked);
        }
        return reportedBy != null ? PiiMaskingUtil.maskUserId(reportedBy) : null;
    }
    
    public void setReportedBy(String reportedBy) {
        this.reportedByUnmasked = reportedBy;
        this.reportedBy = reportedBy;
    }
    
    /**
     * 🔐 DPDPA Compliance: Returns masked assignedTo
     */
    public String getAssignedTo() {
        if (assignedToUnmasked != null) {
            return PiiMaskingUtil.maskUserId(assignedToUnmasked);
        }
        return assignedTo != null ? PiiMaskingUtil.maskUserId(assignedTo) : null;
    }
    
    public void setAssignedTo(String assignedTo) {
        this.assignedToUnmasked = assignedTo;
        this.assignedTo = assignedTo;
    }
    public SupportLevel getCurrentSupportLevel() { return currentSupportLevel; }
    public void setCurrentSupportLevel(SupportLevel currentSupportLevel) { this.currentSupportLevel = currentSupportLevel; }
    public SupportLevel getInitialSupportLevel() { return initialSupportLevel; }
    public void setInitialSupportLevel(SupportLevel initialSupportLevel) { this.initialSupportLevel = initialSupportLevel; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getFirstResponseAt() { return firstResponseAt; }
    public void setFirstResponseAt(LocalDateTime firstResponseAt) { this.firstResponseAt = firstResponseAt; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public Integer getEscalationCount() { return escalationCount; }
    public void setEscalationCount(Integer escalationCount) { this.escalationCount = escalationCount; }
    public LocalDateTime getLastEscalatedAt() { return lastEscalatedAt; }
    public void setLastEscalatedAt(LocalDateTime lastEscalatedAt) { this.lastEscalatedAt = lastEscalatedAt; }
    public SLATrackingResponse getSlaTracking() { return slaTracking; }
    public void setSlaTracking(SLATrackingResponse slaTracking) { this.slaTracking = slaTracking; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }
    public Long getSparePartId() { return sparePartId; }
    public void setSparePartId(Long sparePartId) { this.sparePartId = sparePartId; }
    public Long getIssueMasterId() { return issueMasterId; }
    public void setIssueMasterId(Long issueMasterId) { this.issueMasterId = issueMasterId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getSubCategoryId() { return subCategoryId; }
    public void setSubCategoryId(Long subCategoryId) { this.subCategoryId = subCategoryId; }
    public Long getLoginUserId() { return loginUserId; }
    public void setLoginUserId(Long loginUserId) { this.loginUserId = loginUserId; }
}

