package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import com.example.helpdesk.enums.SupportLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks escalation history for issues
 */
@Entity
@Table(name = "issue_escalations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
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
}

