package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.IssueStatus;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "issues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
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
}

