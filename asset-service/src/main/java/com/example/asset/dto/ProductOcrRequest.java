package com.example.asset.dto;

import java.util.List;

/**
 * ✅ ProductOcrRequest
 * Request DTO for OCR product extraction.
 */
public class ProductOcrRequest {
    
    private Long userId;
    private String username;
    private String projectType;
    private Long subCategoryId;
    private String subCategoryName;
    private Boolean autoCreateMake;
    private Boolean autoCreateModel;
    private List<String> preferredMakes; // For disambiguation

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public ProductOcrRequest() {}

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

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public Boolean getAutoCreateMake() {
        return autoCreateMake != null ? autoCreateMake : true; // Default to true
    }

    public void setAutoCreateMake(Boolean autoCreateMake) {
        this.autoCreateMake = autoCreateMake;
    }

    public Boolean getAutoCreateModel() {
        return autoCreateModel != null ? autoCreateModel : true; // Default to true
    }

    public void setAutoCreateModel(Boolean autoCreateModel) {
        this.autoCreateModel = autoCreateModel;
    }

    public List<String> getPreferredMakes() {
        return preferredMakes;
    }

    public void setPreferredMakes(List<String> preferredMakes) {
        this.preferredMakes = preferredMakes;
    }
}

