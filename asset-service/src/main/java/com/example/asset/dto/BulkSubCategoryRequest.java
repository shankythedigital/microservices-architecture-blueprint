package com.example.asset.dto;

import java.util.List;

public class BulkSubCategoryRequest {

    private Long userId;
    private String username;
    private String projectType;

    private List<SimpleSubCategoryDto> subCategories;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public List<SimpleSubCategoryDto> getSubCategories() { return subCategories; }
    public void setSubCategories(List<SimpleSubCategoryDto> subCategories) { this.subCategories = subCategories; }

    public static class SimpleSubCategoryDto {
        private Long subCategoryId; // Primary key from Excel
        private String subCategoryName;
        private String description;
        private String categoryName; // For lookup by name
        private Long categoryId; // Foreign key from Excel (category_id)

        public Long getSubCategoryId() { return subCategoryId; }
        public void setSubCategoryId(Long subCategoryId) { this.subCategoryId = subCategoryId; }

        public String getSubCategoryName() { return subCategoryName; }
        public void setSubCategoryName(String subCategoryName) { this.subCategoryName = subCategoryName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    }
}

