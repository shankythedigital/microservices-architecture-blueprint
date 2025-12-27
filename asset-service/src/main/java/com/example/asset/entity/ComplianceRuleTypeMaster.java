package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ✅ ComplianceRuleTypeMaster Entity
 * Master table for compliance rule types (replaces enum).
 */
@Entity
@Table(name = "compliance_rule_type_master")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplianceRuleTypeMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_type_id")
    private Long ruleTypeId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "category", length = 50)
    private String category; // DATA_VALIDATION, BUSINESS_RULES, COMPLIANCE_RULES, SECURITY_RULES

    @Column(name = "priority", nullable = false)
    private Integer priority = 100;

    public ComplianceRuleTypeMaster() {}

    public ComplianceRuleTypeMaster(String code, String name, String description, String category) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    // Getters and Setters
    public Long getRuleTypeId() { return ruleTypeId; }
    public void setRuleTypeId(Long ruleTypeId) { this.ruleTypeId = ruleTypeId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
}
