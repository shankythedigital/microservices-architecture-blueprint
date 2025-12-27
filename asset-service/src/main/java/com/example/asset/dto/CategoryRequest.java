package com.example.asset.dto;

import com.example.asset.entity.ProductCategory;

/**
 * ✅ CategoryRequest DTO
 * Wrapper object used for category CRUD APIs.
 * Includes user identity, project context, and the category payload.
 */
public class CategoryRequest {

    private Long userId;
    private String username;
    private String projectType; // e.g. "ASSET_SERVICE", "AUTH_SERVICE", etc.
    private ProductCategory category; // inner payload entity

    // ============================================================
    // ✅ Constructors
    // ============================================================

    public CategoryRequest() {}

    public CategoryRequest(Long userId, String username, String projectType, ProductCategory category) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.category = category;
    }

    // ============================================================
    // ✅ Getters and Setters
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

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    // ============================================================
    // ✅ Utility (Debugging / Logging)
    // ============================================================

    @Override
    public String toString() {
        return "CategoryRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", category=" + (category != null ? category.getCategoryName() : "null") +
                '}';
    }
}

