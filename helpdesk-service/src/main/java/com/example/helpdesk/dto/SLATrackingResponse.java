package com.example.helpdesk.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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
}

