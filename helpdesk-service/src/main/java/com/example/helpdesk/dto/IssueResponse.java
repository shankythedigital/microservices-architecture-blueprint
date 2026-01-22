package com.example.helpdesk.dto;

import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.IssueStatus;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import java.time.LocalDateTime;

public class IssueResponse {
    private Long id;
    private String title;
    private String description;
    private IssueStatus status;
    private IssuePriority priority;
    private RelatedService relatedService;
    private String reportedBy;
    private String assignedTo;
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
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
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
}

