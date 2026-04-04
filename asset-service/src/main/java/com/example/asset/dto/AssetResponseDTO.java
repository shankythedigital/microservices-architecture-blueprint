package com.example.asset.dto;

import java.util.ArrayList;
import java.util.List;

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

    /** Direct asset image URL (nullable). */
    private String imageUrl;

    // ============================================================
    // 🏷️ Linked Master Data
    // ============================================================
    private String categoryName;
    private String subCategoryName;
    private String makeName;
    private String modelName;

    private String categoryImageUrl;
    private String subCategoryImageUrl;
    private String makeImageUrl;
    private String modelImageUrl;

    // ============================================================
    // 🏪 Purchase — vendor / outlet (first active purchase row)
    // ============================================================
    private String vendorName;
    private String vendorImageUrl;
    private String outletName;
    private String outletImageUrl;

    // ============================================================
    // 📎 Warranty / AMC — linked documents (use download API for bytes)
    // ============================================================
    private Long warrantyDocumentId;
    private String warrantyDocumentType;
    private Long amcDocumentId;
    private String amcDocumentType;

    /** User-uploaded appliance photo (download via documents API). */
    private Long assetPhotoDocumentId;
    private String assetPhotoDocumentType;

    // ============================================================
    // 🧩 Components linked to asset (catalog)
    // ============================================================
    private List<AssetComponentSummaryDTO> components = new ArrayList<>();

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategoryImageUrl() {
        return categoryImageUrl;
    }

    public void setCategoryImageUrl(String categoryImageUrl) {
        this.categoryImageUrl = categoryImageUrl;
    }

    public String getSubCategoryImageUrl() {
        return subCategoryImageUrl;
    }

    public void setSubCategoryImageUrl(String subCategoryImageUrl) {
        this.subCategoryImageUrl = subCategoryImageUrl;
    }

    public String getMakeImageUrl() {
        return makeImageUrl;
    }

    public void setMakeImageUrl(String makeImageUrl) {
        this.makeImageUrl = makeImageUrl;
    }

    public String getModelImageUrl() {
        return modelImageUrl;
    }

    public void setModelImageUrl(String modelImageUrl) {
        this.modelImageUrl = modelImageUrl;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorImageUrl() {
        return vendorImageUrl;
    }

    public void setVendorImageUrl(String vendorImageUrl) {
        this.vendorImageUrl = vendorImageUrl;
    }

    public String getOutletName() {
        return outletName;
    }

    public void setOutletName(String outletName) {
        this.outletName = outletName;
    }

    public String getOutletImageUrl() {
        return outletImageUrl;
    }

    public void setOutletImageUrl(String outletImageUrl) {
        this.outletImageUrl = outletImageUrl;
    }

    public Long getWarrantyDocumentId() {
        return warrantyDocumentId;
    }

    public void setWarrantyDocumentId(Long warrantyDocumentId) {
        this.warrantyDocumentId = warrantyDocumentId;
    }

    public String getWarrantyDocumentType() {
        return warrantyDocumentType;
    }

    public void setWarrantyDocumentType(String warrantyDocumentType) {
        this.warrantyDocumentType = warrantyDocumentType;
    }

    public Long getAmcDocumentId() {
        return amcDocumentId;
    }

    public void setAmcDocumentId(Long amcDocumentId) {
        this.amcDocumentId = amcDocumentId;
    }

    public String getAmcDocumentType() {
        return amcDocumentType;
    }

    public void setAmcDocumentType(String amcDocumentType) {
        this.amcDocumentType = amcDocumentType;
    }

    public Long getAssetPhotoDocumentId() {
        return assetPhotoDocumentId;
    }

    public void setAssetPhotoDocumentId(Long assetPhotoDocumentId) {
        this.assetPhotoDocumentId = assetPhotoDocumentId;
    }

    public String getAssetPhotoDocumentType() {
        return assetPhotoDocumentType;
    }

    public void setAssetPhotoDocumentType(String assetPhotoDocumentType) {
        this.assetPhotoDocumentType = assetPhotoDocumentType;
    }

    public List<AssetComponentSummaryDTO> getComponents() {
        return components;
    }

    public void setComponents(List<AssetComponentSummaryDTO> components) {
        this.components = components != null ? components : new ArrayList<>();
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
                ", imageUrl='" + imageUrl + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", subCategoryName='" + subCategoryName + '\'' +
                ", makeName='" + makeName + '\'' +
                ", modelName='" + modelName + '\'' +
                ", components=" + (components != null ? components.size() : 0) +
                '}';
    }
}
