package com.example.asset.dto;

import com.example.asset.entity.AssetDocument;
import com.example.asset.entity.ProductCategory;
import com.example.asset.entity.ProductMake;
import com.example.asset.entity.ProductModel;
import com.example.asset.entity.ProductSubCategory;

/**
 * ✅ ProductOcrResponse
 * Response DTO for OCR product extraction results.
 */
public class ProductOcrResponse {
    
    private String extractedText;
    private ExtractedProductInfo extractedInfo;
    private ProductCategory category;
    private ProductSubCategory subCategory;
    private ProductMake make;
    private ProductModel model;
    private Boolean categoryCreated;
    private Boolean subCategoryCreated;
    private Boolean makeCreated;
    private Boolean modelCreated;
    private AssetDocument document;
    private String status;
    private String message;
    private Double confidence; // OCR confidence score (0-1)

    // ============================================================
    // ✅ Nested class for extracted information
    // ============================================================
    public static class ExtractedProductInfo {
        private String makeName;
        private String modelName;
        private String categoryName;
        private String subCategoryName;
        private String serialNumber;
        private String description;
        private String brand;
        private String manufacturer;

        // Getters and Setters
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

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public void setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
        }
    }

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public ProductOcrResponse() {
        this.extractedInfo = new ExtractedProductInfo();
    }

    // ============================================================
    // ✅ Getters and Setters
    // ============================================================
    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public ExtractedProductInfo getExtractedInfo() {
        return extractedInfo;
    }

    public void setExtractedInfo(ExtractedProductInfo extractedInfo) {
        this.extractedInfo = extractedInfo;
    }

    public ProductMake getMake() {
        return make;
    }

    public void setMake(ProductMake make) {
        this.make = make;
    }

    public ProductModel getModel() {
        return model;
    }

    public void setModel(ProductModel model) {
        this.model = model;
    }

    public Boolean getMakeCreated() {
        return makeCreated;
    }

    public void setMakeCreated(Boolean makeCreated) {
        this.makeCreated = makeCreated;
    }

    public Boolean getModelCreated() {
        return modelCreated;
    }

    public void setModelCreated(Boolean modelCreated) {
        this.modelCreated = modelCreated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public ProductSubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(ProductSubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public Boolean getCategoryCreated() {
        return categoryCreated;
    }

    public void setCategoryCreated(Boolean categoryCreated) {
        this.categoryCreated = categoryCreated;
    }

    public Boolean getSubCategoryCreated() {
        return subCategoryCreated;
    }

    public void setSubCategoryCreated(Boolean subCategoryCreated) {
        this.subCategoryCreated = subCategoryCreated;
    }

    public AssetDocument getDocument() {
        return document;
    }

    public void setDocument(AssetDocument document) {
        this.document = document;
    }
}

