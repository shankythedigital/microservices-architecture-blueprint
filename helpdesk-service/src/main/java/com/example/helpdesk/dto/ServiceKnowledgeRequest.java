package com.example.helpdesk.dto;

import com.example.helpdesk.enums.RelatedService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceKnowledgeRequest {
    @NotNull(message = "Service is required")
    private RelatedService service;

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Category is required")
    private String category;

    private String apiEndpoints;
    private String commonIssues;
    private String troubleshootingSteps;
}

