package com.example.helpdesk.dto;

import com.example.helpdesk.enums.SupportLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IssueEscalationResponse {
    private Long id;
    private Long issueId;
    private SupportLevel fromLevel;
    private SupportLevel toLevel;
    private LocalDateTime escalatedAt;
    private String escalationReason;
    private String escalatedBy;
}

