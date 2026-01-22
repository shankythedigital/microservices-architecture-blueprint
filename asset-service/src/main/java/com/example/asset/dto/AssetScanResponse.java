package com.example.asset.dto;

import java.time.LocalDate;

/**
 * ✅ AssetScanResponse
 * Response DTO containing comprehensive asset details after scanning.
 */
public class AssetScanResponse {
    
    private Long assetId;
    private String assetNameUdv;
    private String serialNumber;
    private String assetStatus;
    private LocalDate purchaseDate;
    
    // Master Data
    private String categoryName;
    private String subCategoryName;
    private String makeName;
    private String modelName;
    
    // Scan Metadata
    private String matchedBy; // "ASSET_ID", "ASSET_NAME_UDV", "SERIAL_NUMBER"
    private String scanValue;
    private String scanType;
    
    public AssetScanResponse() {
    }
    
    // Getters and Setters
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
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public String getAssetStatus() {
        return assetStatus;
    }
    
    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }
    
    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
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
    
    public String getMatchedBy() {
        return matchedBy;
    }
    
    public void setMatchedBy(String matchedBy) {
        this.matchedBy = matchedBy;
    }
    
    public String getScanValue() {
        return scanValue;
    }
    
    public void setScanValue(String scanValue) {
        this.scanValue = scanValue;
    }
    
    public String getScanType() {
        return scanType;
    }
    
    public void setScanType(String scanType) {
        this.scanType = scanType;
    }
    
    @Override
    public String toString() {
        return "AssetScanResponse{" +
                "assetId=" + assetId +
                ", assetNameUdv='" + assetNameUdv + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", assetStatus='" + assetStatus + '\'' +
                ", purchaseDate=" + purchaseDate +
                ", categoryName='" + categoryName + '\'' +
                ", subCategoryName='" + subCategoryName + '\'' +
                ", makeName='" + makeName + '\'' +
                ", modelName='" + modelName + '\'' +
                ", matchedBy='" + matchedBy + '\'' +
                ", scanValue='" + scanValue + '\'' +
                ", scanType='" + scanType + '\'' +
                '}';
    }
}

