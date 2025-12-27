package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ✅ ComplianceSeverityMaster Entity
 * Master table for compliance severity levels (replaces enum).
 */
@Entity
@Table(name = "compliance_severity_master")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplianceSeverityMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "severity_id")
    private Long severityId;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "level", nullable = false)
    private Integer level; // 1=CRITICAL, 2=HIGH, 3=MEDIUM, 4=LOW, 5=INFO

    @Column(name = "blocks_operation", nullable = false)
    private Boolean blocksOperation = false;

    public ComplianceSeverityMaster() {}

    public ComplianceSeverityMaster(String code, String name, String description, Integer level, Boolean blocksOperation) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.level = level;
        this.blocksOperation = blocksOperation;
    }

    // Getters and Setters
    public Long getSeverityId() { return severityId; }
    public void setSeverityId(Long severityId) { this.severityId = severityId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Boolean getBlocksOperation() { return blocksOperation; }
    public void setBlocksOperation(Boolean blocksOperation) { this.blocksOperation = blocksOperation; }
}
