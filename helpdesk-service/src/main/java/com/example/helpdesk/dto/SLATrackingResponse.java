package com.example.helpdesk.dto;

import java.time.LocalDateTime;

public class SLATrackingResponse {
    private Long id;
    private Long issueId;
    private Integer responseTimeMinutes;
    private Integer resolutionTimeMinutes;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;
    private Boolean responseSLAMet;
    private Boolean resolutionSLAMet;
    private LocalDateTime responseSLABreachAt;
    private LocalDateTime resolutionSLABreachAt;
    private Integer actualResponseTimeMinutes;
    private Integer actualResolutionTimeMinutes;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
    public Integer getResponseTimeMinutes() { return responseTimeMinutes; }
    public void setResponseTimeMinutes(Integer responseTimeMinutes) { this.responseTimeMinutes = responseTimeMinutes; }
    public Integer getResolutionTimeMinutes() { return resolutionTimeMinutes; }
    public void setResolutionTimeMinutes(Integer resolutionTimeMinutes) { this.resolutionTimeMinutes = resolutionTimeMinutes; }
    public LocalDateTime getFirstResponseAt() { return firstResponseAt; }
    public void setFirstResponseAt(LocalDateTime firstResponseAt) { this.firstResponseAt = firstResponseAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public Boolean getResponseSLAMet() { return responseSLAMet; }
    public void setResponseSLAMet(Boolean responseSLAMet) { this.responseSLAMet = responseSLAMet; }
    public Boolean getResolutionSLAMet() { return resolutionSLAMet; }
    public void setResolutionSLAMet(Boolean resolutionSLAMet) { this.resolutionSLAMet = resolutionSLAMet; }
    public LocalDateTime getResponseSLABreachAt() { return responseSLABreachAt; }
    public void setResponseSLABreachAt(LocalDateTime responseSLABreachAt) { this.responseSLABreachAt = responseSLABreachAt; }
    public LocalDateTime getResolutionSLABreachAt() { return resolutionSLABreachAt; }
    public void setResolutionSLABreachAt(LocalDateTime resolutionSLABreachAt) { this.resolutionSLABreachAt = resolutionSLABreachAt; }
    public Integer getActualResponseTimeMinutes() { return actualResponseTimeMinutes; }
    public void setActualResponseTimeMinutes(Integer actualResponseTimeMinutes) { this.actualResponseTimeMinutes = actualResponseTimeMinutes; }
    public Integer getActualResolutionTimeMinutes() { return actualResolutionTimeMinutes; }
    public void setActualResolutionTimeMinutes(Integer actualResolutionTimeMinutes) { this.actualResolutionTimeMinutes = actualResolutionTimeMinutes; }
}

