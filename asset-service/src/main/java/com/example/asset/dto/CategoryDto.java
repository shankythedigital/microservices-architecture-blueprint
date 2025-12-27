package com.example.asset.dto;

import java.io.Serializable;
import com.example.common.jpa.BaseEntity;

/**
 * ✅ CategoryDto
 * Data Transfer Object for {@link com.example.asset.entity.ProductCategory}.
 * 
 * Used for safely transferring category data between layers
 * (controller ↔ service ↔ client) without exposing entity internals.
 */
public class CategoryDto extends BaseEntity implements Serializable {

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    private Long categoryId;
    private String categoryName;
    private String description;


    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
        return "CategoryDto{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", description='" + description + '\'' +
                ", active=" + getActive()+
                ", createdBy='" + getCreatedBy() + '\'' +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}



