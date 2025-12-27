package com.example.asset.dto;

import java.util.List;

/**
 * ✅ BulkAssetRequest DTO
 * Request wrapper for bulk asset upload operations.
 * Supports both JSON bulk upload and Excel file parsing.
 */
public class BulkAssetRequest {

    private Long userId;
    private String username;
    private String projectType;

    private List<SimpleAssetDto> assets;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public List<SimpleAssetDto> getAssets() { return assets; }
    public void setAssets(List<SimpleAssetDto> assets) { this.assets = assets; }

    /**
     * ✅ SimpleAssetDto
     * Simplified DTO for bulk asset operations.
     * Supports foreign keys by ID or name lookup.
     */
    public static class SimpleAssetDto {
        private Long assetId; // Primary key from Excel (optional)
        private String assetNameUdv; // Required
        private String assetStatus; // Optional
        
        // Foreign keys (IDs take priority over names)
        private Long categoryId;
        private String categoryName;
        private Long subCategoryId;
        private String subCategoryName;
        private Long makeId;
        private String makeName;
        private Long modelId;
        private String modelName;
        
        // Components (stored in separate rows - one component per row)
        private List<Long> componentIds; // List of component IDs from separate rows
        private List<String> componentNames; // List of component names from separate rows (alternative to IDs)

        // Getters and Setters
        public Long getAssetId() { return assetId; }
        public void setAssetId(Long assetId) { this.assetId = assetId; }

        public String getAssetNameUdv() { return assetNameUdv; }
        public void setAssetNameUdv(String assetNameUdv) { this.assetNameUdv = assetNameUdv; }

        public String getAssetStatus() { return assetStatus; }
        public void setAssetStatus(String assetStatus) { this.assetStatus = assetStatus; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public Long getSubCategoryId() { return subCategoryId; }
        public void setSubCategoryId(Long subCategoryId) { this.subCategoryId = subCategoryId; }

        public String getSubCategoryName() { return subCategoryName; }
        public void setSubCategoryName(String subCategoryName) { this.subCategoryName = subCategoryName; }

        public Long getMakeId() { return makeId; }
        public void setMakeId(Long makeId) { this.makeId = makeId; }

        public String getMakeName() { return makeName; }
        public void setMakeName(String makeName) { this.makeName = makeName; }

        public Long getModelId() { return modelId; }
        public void setModelId(Long modelId) { this.modelId = modelId; }

        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }

        public List<Long> getComponentIds() { return componentIds; }
        public void setComponentIds(List<Long> componentIds) { this.componentIds = componentIds; }

        public List<String> getComponentNames() { return componentNames; }
        public void setComponentNames(List<String> componentNames) { this.componentNames = componentNames; }
    }
}

