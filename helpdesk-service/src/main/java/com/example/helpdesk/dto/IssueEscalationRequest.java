package com.example.helpdesk.dto;

import com.example.helpdesk.enums.SupportLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssueEscalationRequest {
    @NotNull(message = "Target support level is required")
    private SupportLevel toLevel;

    private String escalationReason; // Optional reason for manual escalation
}

