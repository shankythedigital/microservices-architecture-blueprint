package com.example.asset.dto;

import com.example.common.jpa.BaseEntity;
import java.io.Serializable;

/**
 * ✅ ComponentDto
 * Data Transfer Object for {@link com.example.asset.entity.AssetComponent}.
 * 
 * Used for safely transferring component data between layers
 * (controller ↔ service ↔ client) without exposing entity internals.
 * Includes all optional fields for complete JSON responses.
 */
public class ComponentDto extends BaseEntity implements Serializable {

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    private Long componentId;
    private String componentName;
    private String description; // Optional field

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ============================================================
    // 🧠 toString() for Logging
    // ============================================================
    @Override
    public String toString() {
        return "ComponentDto{" +
                "componentId=" + componentId +
                ", componentName='" + componentName + '\'' +
                ", description='" + description + '\'' +
                ", active=" + getActive() +
                ", createdBy='" + getCreatedBy() + '\'' +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}

