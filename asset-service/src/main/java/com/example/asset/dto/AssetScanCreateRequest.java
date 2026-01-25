package com.example.asset.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ✅ AssetScanCreateRequest
 * Enhanced request DTO for scanning QR/barcode and creating/updating assets with all related entities.
 * Supports structured data extraction from scanned codes.
 */
public class AssetScanCreateRequest {
    
    // ============================================================
    // 📱 Scan Information
    // ============================================================
    private String scanValue; // QR code or barcode value
    private String scanType;  // "QR", "BARCODE", or "AUTO"
    
    // ============================================================
    // 👤 User Context
    // ============================================================
    private Long userId;
    private String username;
    private String projectType;
    
    // ============================================================
    // 📦 Asset Basic Information (if creating new asset)
    // ============================================================
    private String assetNameUdv;
    private String serialNumber;
    private Long categoryId;
    private Long subCategoryId;
    private Long makeId;
    private Long modelId;
    // Name fields for AI agent extraction (will be resolved to IDs)
    private String categoryName;
    private String subCategoryName;
    private String makeName;
    private String modelName;
    private String assetStatus;
    private LocalDate purchaseDate;
    
    // ============================================================
    // 🛡️ Warranty Information (optional)
    // ============================================================
    private WarrantyData warranty;
    
    // ============================================================
    // 📋 AMC Information (optional)
    // ============================================================
    private AmcData amc;
    
    // ============================================================
    // 👥 User Assignment (optional)
    // ============================================================
    private Long targetUserId;
    private String targetUsername;
    
    // ============================================================
    // 🔧 Components (optional)
    // ============================================================
    private List<Long> componentIds;
    
    // ============================================================
    // 📄 Raw Scanned Data (for AI agent to parse)
    // ============================================================
    private Map<String, Object> rawData;
    private String rawDataString; // If QR contains JSON string
    
    // ============================================================
    // 🔧 Getters and Setters
    // ============================================================
    
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
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public Long getSubCategoryId() {
        return subCategoryId;
    }
    
    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }
    
    public Long getMakeId() {
        return makeId;
    }
    
    public void setMakeId(Long makeId) {
        this.makeId = makeId;
    }
    
    public Long getModelId() {
        return modelId;
    }
    
    public void setModelId(Long modelId) {
        this.modelId = modelId;
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
    
    public WarrantyData getWarranty() {
        return warranty;
    }
    
    public void setWarranty(WarrantyData warranty) {
        this.warranty = warranty;
    }
    
    public AmcData getAmc() {
        return amc;
    }
    
    public void setAmc(AmcData amc) {
        this.amc = amc;
    }
    
    public Long getTargetUserId() {
        return targetUserId;
    }
    
    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }
    
    public String getTargetUsername() {
        return targetUsername;
    }
    
    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }
    
    public List<Long> getComponentIds() {
        return componentIds;
    }
    
    public void setComponentIds(List<Long> componentIds) {
        this.componentIds = componentIds;
    }
    
    public Map<String, Object> getRawData() {
        return rawData;
    }
    
    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
    }
    
    public String getRawDataString() {
        return rawDataString;
    }
    
    public void setRawDataString(String rawDataString) {
        this.rawDataString = rawDataString;
    }
    
    // ============================================================
    // 📋 Nested Data Classes
    // ============================================================
    
    public static class WarrantyData {
        private String warrantyStatus;
        private String warrantyProvider;
        private String warrantyTerms;
        private LocalDate startDate;
        private LocalDate endDate;
        private Long componentId;
        private Long documentId;
        
        // Getters and Setters
        public String getWarrantyStatus() { return warrantyStatus; }
        public void setWarrantyStatus(String warrantyStatus) { this.warrantyStatus = warrantyStatus; }
        
        public String getWarrantyProvider() { return warrantyProvider; }
        public void setWarrantyProvider(String warrantyProvider) { this.warrantyProvider = warrantyProvider; }
        
        public String getWarrantyTerms() { return warrantyTerms; }
        public void setWarrantyTerms(String warrantyTerms) { this.warrantyTerms = warrantyTerms; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public Long getComponentId() { return componentId; }
        public void setComponentId(Long componentId) { this.componentId = componentId; }
        
        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
    }
    
    public static class AmcData {
        private String amcStatus;
        private LocalDate startDate;
        private LocalDate endDate;
        private Long componentId;
        private Long documentId;
        
        // Getters and Setters
        public String getAmcStatus() { return amcStatus; }
        public void setAmcStatus(String amcStatus) { this.amcStatus = amcStatus; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public Long getComponentId() { return componentId; }
        public void setComponentId(Long componentId) { this.componentId = componentId; }
        
        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
    }
}

