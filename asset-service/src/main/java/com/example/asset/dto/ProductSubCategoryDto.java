


package com.example.asset.dto;
import com.example.common.jpa.BaseEntity;

/**
 * ✅ ProductSubCategoryDto
 * Data Transfer Object for exposing ProductSubCategory details safely.
 * 
 * Avoids lazy-loading issues and provides a clean, lightweight response
 * for REST APIs.
 */
public class ProductSubCategoryDto  extends BaseEntity {

    // ============================================================
    // 🔑 Basic Info
    // ============================================================
    private Long subCategoryId;
    private String subCategoryName;
    private String description;

    // ============================================================
    // 🔗 Category Info
    // ============================================================
    private Long categoryId;
    private String categoryName;

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    // ============================================================
    // 🧠 toString() for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "ProductSubCategoryDto{" +
                "subCategoryId=" + subCategoryId +
                ", subCategoryName='" + subCategoryName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", active=" + getActive()+
                ", createdBy='" + getCreatedBy() + '\'' +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}

