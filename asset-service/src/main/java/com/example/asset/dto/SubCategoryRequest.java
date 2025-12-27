package com.example.asset.dto;

import com.example.asset.entity.ProductSubCategory;

/**
 * ✅ SubCategoryRequest DTO
 * Wrapper for SubCategory operations that includes:
 *  - userId and username for audit context
 *  - projectType for notification scoping
 *  - the actual ProductSubCategory entity payload
 *
 * Used by SubCategoryController and SubCategoryService.
 */
public class SubCategoryRequest {

    // ============================================================
    // 👤 User Context
    // ============================================================
    private Long userId;
    private String username;

    // ============================================================
    // 🧩 Project Context
    // ============================================================
    private String projectType;

    // ============================================================
    // 📦 Payload
    // ============================================================
    private ProductSubCategory subCategory;

    // ============================================================
    // 🏗️ Constructors
    // ============================================================
    public SubCategoryRequest() {}

    public SubCategoryRequest(Long userId, String username, String projectType, ProductSubCategory subCategory) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.subCategory = subCategory;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public ProductSubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(ProductSubCategory subCategory) {
        this.subCategory = subCategory;
    }

    // ============================================================
    // 🧠 Debug-friendly toString()
    // ============================================================
    @Override
    public String toString() {
        return "SubCategoryRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", subCategory=" + (subCategory != null ? subCategory.getSubCategoryName() : "null") +
                '}';
    }
}

