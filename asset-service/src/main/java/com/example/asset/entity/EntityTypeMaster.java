package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ✅ EntityTypeMaster Entity
 * Master table for entity types used in polymorphic relationships.
 * Stores all valid entity types (ASSET, COMPONENT, MAKE, MODEL, etc.)
 */
@Entity
@Table(
    name = "entity_type_master",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"code"})
    }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityTypeMaster extends BaseEntity {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entity_type_id")
    private Integer entityTypeId;

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    /**
     * Unique code for the entity type (e.g., ASSET, COMPONENT, MAKE)
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Human-readable description of the entity type
     */
    @Column(name = "description", length = 255)
    private String description;

    // ============================================================
    // 🧾 Constructors
    // ============================================================
    public EntityTypeMaster() {
    }

    public EntityTypeMaster(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Integer getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(Integer entityTypeId) {
        this.entityTypeId = entityTypeId;
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

    // ============================================================
    // 🧠 toString
    // ============================================================
    @Override
    public String toString() {
        return "EntityTypeMaster{" +
                "entityTypeId=" + entityTypeId +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", active=" + getActive() +
                '}';
    }
}
