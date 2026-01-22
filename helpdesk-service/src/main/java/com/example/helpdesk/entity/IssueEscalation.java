package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import com.example.helpdesk.enums.SupportLevel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Tracks escalation history for issues
 */
@Entity
@Table(name = "issue_escalations")
public class IssueEscalation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_level")
    private SupportLevel fromLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_level", nullable = false)
    private SupportLevel toLevel;

    @Column(name = "escalated_at", nullable = false)
    private LocalDateTime escalatedAt;

    @Column(name = "escalation_reason", columnDefinition = "TEXT")
    private String escalationReason; // Auto-escalation, manual, SLA breach

    @Column(name = "escalated_by")
    private String escalatedBy; // User who escalated or "SYSTEM" for auto-escalation

    @PrePersist
    protected void onCreate() {
        if (escalatedAt == null) {
            escalatedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Issue getIssue() { return issue; }
    public void setIssue(Issue issue) { this.issue = issue; }
    public SupportLevel getFromLevel() { return fromLevel; }
    public void setFromLevel(SupportLevel fromLevel) { this.fromLevel = fromLevel; }
    public SupportLevel getToLevel() { return toLevel; }
    public void setToLevel(SupportLevel toLevel) { this.toLevel = toLevel; }
    public LocalDateTime getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(LocalDateTime escalatedAt) { this.escalatedAt = escalatedAt; }
    public String getEscalationReason() { return escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }
    public String getEscalatedBy() { return escalatedBy; }
    public void setEscalatedBy(String escalatedBy) { this.escalatedBy = escalatedBy; }
}

