package com.example.helpdesk.dto;

import com.example.helpdesk.enums.RelatedService;
import java.time.LocalDateTime;

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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

