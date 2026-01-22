package com.example.helpdesk.dto;

import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import jakarta.validation.constraints.NotNull;
public class EscalationMatrixRequest {
    @NotNull(message = "Related service is required")
    private RelatedService relatedService;

    @NotNull(message = "Priority is required")
    private IssuePriority priority;

    @NotNull(message = "Support level is required")
    private SupportLevel supportLevel;

    @NotNull(message = "Initial assignment level is required")
    private SupportLevel initialAssignmentLevel;

    private SupportLevel escalateToLevel;

    private Integer escalationTimeMinutes; // Time before auto-escalation

    @NotNull(message = "Response time is required")
    private Integer responseTimeMinutes; // SLA response time

    @NotNull(message = "Resolution time is required")
    private Integer resolutionTimeMinutes; // SLA resolution time

    private Boolean isActive = true;

    // Getters and Setters
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
}

