package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Tracks SLA metrics and violations for issues
 */
@Entity
@Table(name = "sla_tracking")
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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Issue getIssue() { return issue; }
    public void setIssue(Issue issue) { this.issue = issue; }
    public Integer getResponseTimeMinutes() { return responseTimeMinutes; }
    public void setResponseTimeMinutes(Integer responseTimeMinutes) { this.responseTimeMinutes = responseTimeMinutes; }
    public Integer getResolutionTimeMinutes() { return resolutionTimeMinutes; }
    public void setResolutionTimeMinutes(Integer resolutionTimeMinutes) { this.resolutionTimeMinutes = resolutionTimeMinutes; }
    public LocalDateTime getFirstResponseAt() { return firstResponseAt; }
    public void setFirstResponseAt(LocalDateTime firstResponseAt) { this.firstResponseAt = firstResponseAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public Boolean getResponseSLAMet() { return responseSLAMet; }
    public void setResponseSLAMet(Boolean responseSLAMet) { this.responseSLAMet = responseSLAMet; }
    public Boolean getResolutionSLAMet() { return resolutionSLAMet; }
    public void setResolutionSLAMet(Boolean resolutionSLAMet) { this.resolutionSLAMet = resolutionSLAMet; }
    public LocalDateTime getResponseSLABreachAt() { return responseSLABreachAt; }
    public void setResponseSLABreachAt(LocalDateTime responseSLABreachAt) { this.responseSLABreachAt = responseSLABreachAt; }
    public LocalDateTime getResolutionSLABreachAt() { return resolutionSLABreachAt; }
    public void setResolutionSLABreachAt(LocalDateTime resolutionSLABreachAt) { this.resolutionSLABreachAt = resolutionSLABreachAt; }
    public Integer getActualResponseTimeMinutes() { return actualResponseTimeMinutes; }
    public void setActualResponseTimeMinutes(Integer actualResponseTimeMinutes) { this.actualResponseTimeMinutes = actualResponseTimeMinutes; }
    public Integer getActualResolutionTimeMinutes() { return actualResolutionTimeMinutes; }
    public void setActualResolutionTimeMinutes(Integer actualResolutionTimeMinutes) { this.actualResolutionTimeMinutes = actualResolutionTimeMinutes; }
}

