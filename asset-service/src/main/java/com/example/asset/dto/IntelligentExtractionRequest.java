package com.example.asset.dto;

/**
 * ✅ IntelligentExtractionRequest
 * Request DTO for intelligent document extraction with comprehensive asset information.
 */
public class IntelligentExtractionRequest {
    
    private Long userId;
    private String username;
    private String projectType;
    private String documentType; // INVOICE, WARRANTY_CARD, AMC_DOCUMENT, SPEC_SHEET, MANUAL, etc.
    private Boolean autoCreateEntities; // Auto-create missing entities
    private Boolean extractWarranty;
    private Boolean extractAmc;
    private Boolean extractComponents;
    private Long existingAssetId; // Link to existing asset if available

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public IntelligentExtractionRequest() {
        this.autoCreateEntities = true;
        this.extractWarranty = true;
        this.extractAmc = true;
        this.extractComponents = true;
    }

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

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Boolean getAutoCreateEntities() {
        return autoCreateEntities != null ? autoCreateEntities : true;
    }

    public void setAutoCreateEntities(Boolean autoCreateEntities) {
        this.autoCreateEntities = autoCreateEntities;
    }

    public Boolean getExtractWarranty() {
        return extractWarranty != null ? extractWarranty : true;
    }

    public void setExtractWarranty(Boolean extractWarranty) {
        this.extractWarranty = extractWarranty;
    }

    public Boolean getExtractAmc() {
        return extractAmc != null ? extractAmc : true;
    }

    public void setExtractAmc(Boolean extractAmc) {
        this.extractAmc = extractAmc;
    }

    public Boolean getExtractComponents() {
        return extractComponents != null ? extractComponents : true;
    }

    public void setExtractComponents(Boolean extractComponents) {
        this.extractComponents = extractComponents;
    }

    public Long getExistingAssetId() {
        return existingAssetId;
    }

    public void setExistingAssetId(Long existingAssetId) {
        this.existingAssetId = existingAssetId;
    }
}

