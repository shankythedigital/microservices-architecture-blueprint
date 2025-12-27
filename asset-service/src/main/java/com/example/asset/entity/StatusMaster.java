package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ✅ StatusMaster Entity
 * Master table for status values used across the asset management system.
 * Stores all valid statuses (ASSET_AVAILABLE, AMC_ACTIVE, WARRANTY_EXPIRED, etc.)
 */
@Entity
@Table(
    name = "status_master",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"code"})
    }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusMaster extends BaseEntity {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Integer statusId;

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    /**
     * Unique code for the status (e.g., ASSET_AVAILABLE, AMC_ACTIVE)
     */
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    /**
     * Human-readable description of the status
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Category of the status (ASSET, AMC, WARRANTY, GENERAL)
     */
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    // ============================================================
    // 🧾 Constructors
    // ============================================================
    public StatusMaster() {
    }

    public StatusMaster(String code, String description, String category) {
        this.code = code;
        this.description = description;
        this.category = category;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // ============================================================
    // 🧠 toString
    // ============================================================
    @Override
    public String toString() {
        return "StatusMaster{" +
                "statusId=" + statusId +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", active=" + getActive() +
                '}';
    }
}
