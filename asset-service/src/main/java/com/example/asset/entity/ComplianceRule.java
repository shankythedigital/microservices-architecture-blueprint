package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ✅ ComplianceRule Entity
 * Defines business rules and compliance requirements.
 * Rules can be configured per entity type and enforced automatically.
 */
@Entity
@Table(
    name = "compliance_rule",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rule_code", "entity_type"})
    }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplianceRule extends BaseEntity {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    /**
     * Unique rule code (e.g., ASSET_NAME_REQUIRED, WARRANTY_EXPIRY_CHECK)
     */
    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    /**
     * Human-readable rule name
     */
    @Column(name = "rule_name", nullable = false, length = 255)
    private String ruleName;

    /**
     * Description of what the rule checks
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Entity type this rule applies to (ASSET, WARRANTY, AMC, etc.)
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * Type of compliance rule (foreign key to compliance_rule_type_master)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_type_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private ComplianceRuleTypeMaster ruleType;

    /**
     * Severity level if rule is violated (foreign key to compliance_severity_master)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "severity_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private ComplianceSeverityMaster severity;

    /**
     * Rule expression/condition (JSON or expression string)
     * Example: {"field": "assetName", "operator": "notEmpty"}
     */
    @Column(name = "rule_expression", columnDefinition = "TEXT")
    private String ruleExpression;

    /**
     * Error message template when rule is violated
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * Whether rule blocks operation if violated
     */
    @Column(name = "blocks_operation", nullable = false)
    private Boolean blocksOperation = false;

    /**
     * Priority for rule execution (lower number = higher priority)
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 100;

    // ============================================================
    // 🧾 Constructors
    // ============================================================
    public ComplianceRule() {
    }

    public ComplianceRule(String ruleCode, String ruleName, String entityType, 
                         ComplianceRuleTypeMaster ruleType, ComplianceSeverityMaster severity) {
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.entityType = entityType;
        this.ruleType = ruleType;
        this.severity = severity;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public ComplianceRuleTypeMaster getRuleType() {
        return ruleType;
    }

    public void setRuleType(ComplianceRuleTypeMaster ruleType) {
        this.ruleType = ruleType;
    }

    public ComplianceSeverityMaster getSeverity() {
        return severity;
    }

    public void setSeverity(ComplianceSeverityMaster severity) {
        this.severity = severity;
    }

    public String getRuleExpression() {
        return ruleExpression;
    }

    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean getBlocksOperation() {
        return blocksOperation;
    }

    public void setBlocksOperation(Boolean blocksOperation) {
        this.blocksOperation = blocksOperation;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    // ============================================================
    // 🧠 toString
    // ============================================================
    @Override
    public String toString() {
        return "ComplianceRule{" +
                "ruleId=" + ruleId +
                ", ruleCode='" + ruleCode + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", entityType='" + entityType + '\'' +
                ", ruleType=" + ruleType +
                ", severity=" + severity +
                ", blocksOperation=" + blocksOperation +
                ", active=" + getActive() +
                '}';
    }
}

