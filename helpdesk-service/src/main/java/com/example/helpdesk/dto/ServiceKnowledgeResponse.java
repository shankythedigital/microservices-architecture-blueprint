package com.example.helpdesk.dto;

import com.example.helpdesk.enums.RelatedService;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceKnowledgeResponse {
    private Long id;
    private RelatedService service;
    private String topic;
    private String content;
    private String category;
    private String apiEndpoints;
    private String commonIssues;
    private String troubleshootingSteps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

