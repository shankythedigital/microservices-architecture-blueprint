package com.example.helpdesk.dto;

import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
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
}

