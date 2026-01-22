package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks SLA metrics and violations for issues
 */
@Entity
@Table(name = "sla_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SLATracking extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false, unique = true)
    private Issue issue;

    @Column(name = "response_time_minutes")
    private Integer responseTimeMinutes; // Target response time

    @Column(name = "resolution_time_minutes")
    private Integer resolutionTimeMinutes; // Target resolution time

    @Column(name = "first_response_at")
    private LocalDateTime firstResponseAt; // When first response was given

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt; // When issue was resolved

    @Column(name = "response_sla_met")
    private Boolean responseSLAMet; // Whether response SLA was met

    @Column(name = "resolution_sla_met")
    private Boolean resolutionSLAMet; // Whether resolution SLA was met

    @Column(name = "response_sla_breach_at")
    private LocalDateTime responseSLABreachAt; // When response SLA was breached

    @Column(name = "resolution_sla_breach_at")
    private LocalDateTime resolutionSLABreachAt; // When resolution SLA was breached

    @Column(name = "actual_response_time_minutes")
    private Integer actualResponseTimeMinutes; // Actual time taken for first response

    @Column(name = "actual_resolution_time_minutes")
    private Integer actualResolutionTimeMinutes; // Actual time taken for resolution
}

