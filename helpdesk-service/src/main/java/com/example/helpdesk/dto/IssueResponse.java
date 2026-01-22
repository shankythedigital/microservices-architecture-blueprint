package com.example.helpdesk.dto;

import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.IssueStatus;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
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
}

