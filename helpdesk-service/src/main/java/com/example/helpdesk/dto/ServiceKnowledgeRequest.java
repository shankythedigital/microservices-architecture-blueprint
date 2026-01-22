package com.example.helpdesk.dto;

import com.example.helpdesk.enums.RelatedService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    // Getters and Setters
    public RelatedService getService() { return service; }
    public void setService(RelatedService service) { this.service = service; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getApiEndpoints() { return apiEndpoints; }
    public void setApiEndpoints(String apiEndpoints) { this.apiEndpoints = apiEndpoints; }
    public String getCommonIssues() { return commonIssues; }
    public void setCommonIssues(String commonIssues) { this.commonIssues = commonIssues; }
    public String getTroubleshootingSteps() { return troubleshootingSteps; }
    public void setTroubleshootingSteps(String troubleshootingSteps) { this.troubleshootingSteps = troubleshootingSteps; }
}

