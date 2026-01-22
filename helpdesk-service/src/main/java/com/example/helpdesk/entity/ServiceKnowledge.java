package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import com.example.helpdesk.enums.RelatedService;
import jakarta.persistence.*;

@Entity
@Table(name = "service_knowledge")
public class ServiceKnowledge extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelatedService service;

    @Column(nullable = false)
    private String topic;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String category;

    @Column(name = "api_endpoints", columnDefinition = "TEXT")
    private String apiEndpoints; // JSON or comma-separated list

    @Column(name = "common_issues", columnDefinition = "TEXT")
    private String commonIssues; // JSON or text

    @Column(name = "troubleshooting_steps", columnDefinition = "TEXT")
    private String troubleshootingSteps;

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
}

