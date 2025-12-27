package com.example.asset.dto;

import com.example.common.jpa.BaseEntity;
import java.io.Serializable;

/**
 * ✅ MakeDto
 * Data Transfer Object for {@link com.example.asset.entity.ProductMake}.
 * 
 * Used for safely transferring make data between layers
 * (controller ↔ service ↔ client) without exposing entity internals.
 * Includes all optional fields for complete JSON responses.
 */
public class MakeDto extends BaseEntity implements Serializable {

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    private Long makeId;
    private String makeName;

    // ============================================================
    // 🔗 Foreign Key Fields (Optional)
    // ============================================================
    private Long subCategoryId;
    private String subCategoryName;

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getMakeId() {
        return makeId;
    }

    public void setMakeId(Long makeId) {
        this.makeId = makeId;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    // ============================================================
    // 🧠 toString() for Logging
    // ============================================================
    @Override
    public String toString() {
        return "MakeDto{" +
                "makeId=" + makeId +
                ", makeName='" + makeName + '\'' +
                ", subCategoryId=" + subCategoryId +
                ", subCategoryName='" + subCategoryName + '\'' +
                ", active=" + getActive() +
                ", createdBy='" + getCreatedBy() + '\'' +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}

