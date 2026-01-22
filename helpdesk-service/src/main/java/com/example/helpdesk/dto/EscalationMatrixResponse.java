package com.example.helpdesk.dto;

import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import java.time.LocalDateTime;

public class EscalationMatrixResponse {
    private Long id;
    private RelatedService relatedService;
    private IssuePriority priority;
    private SupportLevel supportLevel;
    private SupportLevel initialAssignmentLevel;
    private SupportLevel escalateToLevel;
    private Integer escalationTimeMinutes;
    private Integer responseTimeMinutes;
    private Integer resolutionTimeMinutes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RelatedService getRelatedService() { return relatedService; }
    public void setRelatedService(RelatedService relatedService) { this.relatedService = relatedService; }
    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }
    public SupportLevel getSupportLevel() { return supportLevel; }
    public void setSupportLevel(SupportLevel supportLevel) { this.supportLevel = supportLevel; }
    public SupportLevel getInitialAssignmentLevel() { return initialAssignmentLevel; }
    public void setInitialAssignmentLevel(SupportLevel initialAssignmentLevel) { this.initialAssignmentLevel = initialAssignmentLevel; }
    public SupportLevel getEscalateToLevel() { return escalateToLevel; }
    public void setEscalateToLevel(SupportLevel escalateToLevel) { this.escalateToLevel = escalateToLevel; }
    public Integer getEscalationTimeMinutes() { return escalationTimeMinutes; }
    public void setEscalationTimeMinutes(Integer escalationTimeMinutes) { this.escalationTimeMinutes = escalationTimeMinutes; }
    public Integer getResponseTimeMinutes() { return responseTimeMinutes; }
    public void setResponseTimeMinutes(Integer responseTimeMinutes) { this.responseTimeMinutes = responseTimeMinutes; }
    public Integer getResolutionTimeMinutes() { return resolutionTimeMinutes; }
    public void setResolutionTimeMinutes(Integer resolutionTimeMinutes) { this.resolutionTimeMinutes = resolutionTimeMinutes; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

