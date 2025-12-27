package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * ✅ ComplianceViolation Entity
 * Tracks compliance violations for entities.
 * Records when rules are violated and their resolution status.
 */
@Entity
@Table(name = "compliance_violation")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplianceViolation extends BaseEntity {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "violation_id")
    private Long violationId;

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    /**
     * Reference to the compliance rule that was violated
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private ComplianceRule rule;

    /**
     * Entity type (ASSET, WARRANTY, AMC, etc.)
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * ID of the entity that violated the rule
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Severity of the violation (foreign key to compliance_severity_master)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "severity_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private ComplianceSeverityMaster severity;

    /**
     * Status of the violation (foreign key to compliance_status_master)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private ComplianceStatusMaster status;

    /**
     * Detailed violation message
     */
    @Column(name = "violation_message", length = 1000)
    private String violationMessage;

    /**
     * Field or property that caused the violation
     */
    @Column(name = "violated_field", length = 100)
    private String violatedField;

    /**
     * Expected value (for reference)
     */
    @Column(name = "expected_value", length = 500)
    private String expectedValue;

    /**
     * Actual value that caused violation
     */
    @Column(name = "actual_value", length = 500)
    private String actualValue;

    /**
     * When the violation was detected
     */
    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    /**
     * When the violation was resolved
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * User who detected/resolved the violation
     */
    @Column(name = "resolved_by", length = 255)
    private String resolvedBy;

    /**
     * Resolution notes
     */
    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    // ============================================================
    // 🧾 Constructors
    // ============================================================
    public ComplianceViolation() {
    }

    public ComplianceViolation(ComplianceRule rule, String entityType, Long entityId, 
                              ComplianceSeverityMaster severity, String violationMessage) {
        this.rule = rule;
        this.entityType = entityType;
        this.entityId = entityId;
        this.severity = severity;
        this.violationMessage = violationMessage;
        this.detectedAt = LocalDateTime.now();
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getViolationId() {
        return violationId;
    }

    public void setViolationId(Long violationId) {
        this.violationId = violationId;
    }

    public ComplianceRule getRule() {
        return rule;
    }

    public void setRule(ComplianceRule rule) {
        this.rule = rule;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public ComplianceSeverityMaster getSeverity() {
        return severity;
    }

    public void setSeverity(ComplianceSeverityMaster severity) {
        this.severity = severity;
    }

    public ComplianceStatusMaster getStatus() {
        return status;
    }

    public void setStatus(ComplianceStatusMaster status) {
        this.status = status;
    }

    public String getViolationMessage() {
        return violationMessage;
    }

    public void setViolationMessage(String violationMessage) {
        this.violationMessage = violationMessage;
    }

    public String getViolatedField() {
        return violatedField;
    }

    public void setViolatedField(String violatedField) {
        this.violatedField = violatedField;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    // ============================================================
    // 🧩 Helper Methods
    // ============================================================
    public void resolve(String resolvedBy, String notes, ComplianceStatusMaster compliantStatus) {
        this.status = compliantStatus;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
        this.resolutionNotes = notes;
    }

    public boolean isResolved() {
        return this.status != null && 
               this.status.getIsResolved() != null && 
               this.status.getIsResolved() && 
               this.resolvedAt != null;
    }

    // ============================================================
    // 🧠 toString
    // ============================================================
    @Override
    public String toString() {
        return "ComplianceViolation{" +
                "violationId=" + violationId +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", severity=" + severity +
                ", status=" + status +
                ", violationMessage='" + violationMessage + '\'' +
                ", detectedAt=" + detectedAt +
                ", resolved=" + isResolved() +
                '}';
    }
}

