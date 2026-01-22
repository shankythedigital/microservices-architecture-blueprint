package com.example.helpdesk.dto;

import com.example.helpdesk.enums.SupportLevel;
import jakarta.validation.constraints.NotNull;
public class IssueEscalationRequest {
    @NotNull(message = "Target support level is required")
    private SupportLevel toLevel;

    private String escalationReason; // Optional reason for manual escalation

    // Getters and Setters
    public SupportLevel getToLevel() { return toLevel; }
    public void setToLevel(SupportLevel toLevel) { this.toLevel = toLevel; }
    public String getEscalationReason() { return escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }
}

