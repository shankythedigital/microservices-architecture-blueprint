package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.IssueStatus;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "issues")
public class Issue extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status = IssueStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuePriority priority = IssuePriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelatedService relatedService;

    @Column(nullable = false)
    private String reportedBy; // User ID or email

    private String assignedTo; // Support agent ID or email

    @Enumerated(EnumType.STRING)
    @Column(name = "current_support_level")
    private SupportLevel currentSupportLevel; // Current support level (L1, L2, L3)

    @Column(name = "initial_support_level")
    @Enumerated(EnumType.STRING)
    private SupportLevel initialSupportLevel; // Initial support level assigned

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt; // When issue was first assigned

    @Column(name = "first_response_at")
    private LocalDateTime firstResponseAt; // When first response was given

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "escalation_count")
    private Integer escalationCount = 0; // Number of times escalated

    @Column(name = "last_escalated_at")
    private LocalDateTime lastEscalatedAt;

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IssueEscalation> escalations = new ArrayList<>();

    @OneToOne(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    private SLATracking slaTracking;

    @PreUpdate
    protected void onUpdate() {
        if (status == IssueStatus.RESOLVED && resolvedAt == null) {
            resolvedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }
    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }
    public RelatedService getRelatedService() { return relatedService; }
    public void setRelatedService(RelatedService relatedService) { this.relatedService = relatedService; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public SupportLevel getCurrentSupportLevel() { return currentSupportLevel; }
    public void setCurrentSupportLevel(SupportLevel currentSupportLevel) { this.currentSupportLevel = currentSupportLevel; }
    public SupportLevel getInitialSupportLevel() { return initialSupportLevel; }
    public void setInitialSupportLevel(SupportLevel initialSupportLevel) { this.initialSupportLevel = initialSupportLevel; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getFirstResponseAt() { return firstResponseAt; }
    public void setFirstResponseAt(LocalDateTime firstResponseAt) { this.firstResponseAt = firstResponseAt; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public Integer getEscalationCount() { return escalationCount; }
    public void setEscalationCount(Integer escalationCount) { this.escalationCount = escalationCount; }
    public LocalDateTime getLastEscalatedAt() { return lastEscalatedAt; }
    public void setLastEscalatedAt(LocalDateTime lastEscalatedAt) { this.lastEscalatedAt = lastEscalatedAt; }
    public List<IssueEscalation> getEscalations() { return escalations; }
    public void setEscalations(List<IssueEscalation> escalations) { this.escalations = escalations; }
    public SLATracking getSlaTracking() { return slaTracking; }
    public void setSlaTracking(SLATracking slaTracking) { this.slaTracking = slaTracking; }
}

