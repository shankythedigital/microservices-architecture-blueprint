package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ✅ ComplianceStatusMaster Entity
 * Master table for compliance status values (replaces enum).
 */
@Entity
@Table(name = "compliance_status_master")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplianceStatusMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    public ComplianceStatusMaster() {}

    public ComplianceStatusMaster(String code, String name, String description, Boolean isResolved) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.isResolved = isResolved;
    }

    // Getters and Setters
    public Long getStatusId() { return statusId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
}
