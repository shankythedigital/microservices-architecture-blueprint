package com.example.asset.dto;

/**
 * ✅ AssetResponseDTO
 * Represents summarized asset details for API responses.
 */
public class AssetResponseDTO {

    // ============================================================
    // 🔑 Identifiers & Basic Info
    // ============================================================
    private Long assetId;
    private String assetNameUdv;
    private String assetStatus;

    // ============================================================
    // 🏷️ Linked Master Data
    // ============================================================
    private String categoryName;
    private String subCategoryName;
    private String makeName;
    private String modelName;

    // ============================================================
    // 🔧 Getters and Setters
    // ============================================================

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetNameUdv() {
        return assetNameUdv;
    }

    public void setAssetNameUdv(String assetNameUdv) {
        this.assetNameUdv = assetNameUdv;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    // ============================================================
    // 🧠 toString() for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "AssetResponseDTO{" +
                "assetId=" + assetId +
                ", assetNameUdv='" + assetNameUdv + '\'' +
                ", assetStatus='" + assetStatus + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", subCategoryName='" + subCategoryName + '\'' +
                ", makeName='" + makeName + '\'' +
                ", modelName='" + modelName + '\'' +
                '}';
    }
}


