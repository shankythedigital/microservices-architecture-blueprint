package com.example.asset.dto;

import java.util.List;

/**
 * ✅ ProductScanPreviewResponse
 * Response DTO for product scan preview - displays extracted information without saving.
 * Used for mobile camera OCR preview functionality.
 */
public class ProductScanPreviewResponse {
    
    private String status;
    private String message;
    private ExtractedProductInfo productInfo;
    private Double confidence;
    private Long processingTimeMs;

    // ============================================================
    // ✅ Nested class for extracted product information
    // ============================================================
    public static class ExtractedProductInfo {
        private String categoryName;
        private String subCategoryName;
        private String makeName;
        private String modelName;
        private String brand;
        private String manufacturer;
        private String serialNumber;
        private String description;
        private List<String> componentNames;
        private String extractedText;

        // Getters and Setters
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        
        public String getSubCategoryName() { return subCategoryName; }
        public void setSubCategoryName(String subCategoryName) { this.subCategoryName = subCategoryName; }
        
        public String getMakeName() { return makeName; }
        public void setMakeName(String makeName) { this.makeName = makeName; }
        
        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }
        
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        
        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
        
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<String> getComponentNames() { return componentNames; }
        public void setComponentNames(List<String> componentNames) { this.componentNames = componentNames; }
        
        public String getExtractedText() { return extractedText; }
        public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    }

    // ============================================================
    // 🔧 Getters and Setters
    // ============================================================
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public ExtractedProductInfo getProductInfo() { return productInfo; }
    public void setProductInfo(ExtractedProductInfo productInfo) { this.productInfo = productInfo; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
}

