package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Escalation Matrix defines the support level assignment and escalation rules
 * based on priority, service, and time-based SLA.
 */
@Entity
@Table(name = "escalation_matrix",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"related_service", "priority", "support_level"})
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EscalationMatrix extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "related_service", nullable = false)
    private RelatedService relatedService;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "support_level", nullable = false)
    private SupportLevel supportLevel;

    @Column(name = "initial_assignment_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private SupportLevel initialAssignmentLevel;

    @Column(name = "escalate_to_level")
    @Enumerated(EnumType.STRING)
    private SupportLevel escalateToLevel; // Next level for escalation

    @Column(name = "escalation_time_minutes")
    private Integer escalationTimeMinutes; // Time before auto-escalation

    @Column(name = "response_time_minutes")
    private Integer responseTimeMinutes; // SLA response time

    @Column(name = "resolution_time_minutes")
    private Integer resolutionTimeMinutes; // SLA resolution time

    @Column(name = "is_active")
    private Boolean isActive = true;
}

