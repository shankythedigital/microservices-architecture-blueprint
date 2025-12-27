
package com.example.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * ✅ AssetWarrantyRequest
 * Request DTO for creating or updating warranty records.
 * Clean JSON structure — document upload handled separately.
 */
public class AssetWarrantyRequest {

    // ============================================================
    // 👤 User Context
    // ============================================================
    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "username is required")
    private String username;

    private String projectType;

    // ============================================================
    // 🔗 Asset & Component Link
    // ============================================================
    @NotNull(message = "assetId is required")
    private Long assetId;

    private Long componentId;

    // ============================================================
    // 🧾 Warranty Details
    // ============================================================
    @NotBlank(message = "warrantyStatus is required")
    private String warrantyStatus;

    private String warrantyProvider;
    private String warrantyTerms;

    @NotBlank(message = "startDate is required (format: yyyy-MM-dd)")
    private String startDate;

    @NotBlank(message = "endDate is required (format: yyyy-MM-dd)")
    private String endDate;

    // ============================================================
    // 📎 Optional Document Link (managed by DocumentController)
    // ============================================================
    private Long documentId;
    private String docType;

    // ============================================================
    // 🔧 Getters & Setters
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

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public String getWarrantyStatus() {
        return warrantyStatus;
    }

    public void setWarrantyStatus(String warrantyStatus) {
        this.warrantyStatus = warrantyStatus;
    }

    public String getWarrantyProvider() {
        return warrantyProvider;
    }

    public void setWarrantyProvider(String warrantyProvider) {
        this.warrantyProvider = warrantyProvider;
    }

    public String getWarrantyTerms() {
        return warrantyTerms;
    }

    public void setWarrantyTerms(String warrantyTerms) {
        this.warrantyTerms = warrantyTerms;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    // ============================================================
    // 🧠 Debugging
    // ============================================================
    @Override
    public String toString() {
        return "AssetWarrantyRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", assetId=" + assetId +
                ", componentId=" + componentId +
                ", warrantyStatus='" + warrantyStatus + '\'' +
                ", warrantyProvider='" + warrantyProvider + '\'' +
                ", warrantyTerms='" + warrantyTerms + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", documentId=" + documentId +
                ", docType='" + docType + '\'' +
                '}';
    }
}


