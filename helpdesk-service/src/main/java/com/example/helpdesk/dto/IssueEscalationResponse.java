package com.example.helpdesk.dto;

import com.example.helpdesk.enums.SupportLevel;
import java.time.LocalDateTime;

public class IssueEscalationResponse {
    private Long id;
    private Long issueId;
    private SupportLevel fromLevel;
    private SupportLevel toLevel;
    private LocalDateTime escalatedAt;
    private String escalationReason;
    private String escalatedBy;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
    public SupportLevel getFromLevel() { return fromLevel; }
    public void setFromLevel(SupportLevel fromLevel) { this.fromLevel = fromLevel; }
    public SupportLevel getToLevel() { return toLevel; }
    public void setToLevel(SupportLevel toLevel) { this.toLevel = toLevel; }
    public LocalDateTime getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(LocalDateTime escalatedAt) { this.escalatedAt = escalatedAt; }
    public String getEscalationReason() { return escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }
    public String getEscalatedBy() { return escalatedBy; }
    public void setEscalatedBy(String escalatedBy) { this.escalatedBy = escalatedBy; }
}

