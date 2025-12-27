package com.example.asset.dto;

import java.util.List;

public class BulkCategoryRequest {

    private Long userId;
    private String username;
    private String projectType;

    private List<SimpleCategoryDto> categories;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public List<SimpleCategoryDto> getCategories() { return categories; }
    public void setCategories(List<SimpleCategoryDto> categories) { this.categories = categories; }

    public static class SimpleCategoryDto {
        private Long categoryId; // Primary key from Excel
        private String categoryName;
        private String description;

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
