package com.example.helpdesk.dto;

import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
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
}

