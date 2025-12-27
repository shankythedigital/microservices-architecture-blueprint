
package com.example.asset.dto;

import java.time.LocalDate;

/**
 * ✅ AssetWarrantyDto
 * Data Transfer Object for Warranty entity responses.
 * Represents warranty details, asset linkage, and document metadata.
 */
public class AssetWarrantyDto {

    // ============================================================
    // 🔑 Identifiers
    // ============================================================
    private Long warrantyId;       // Primary key of warranty record
    private Long assetId;          // Linked asset ID
    private Long componentId;      // Optional component ID

    // ============================================================
    // 🧾 Warranty Info
    // ============================================================
    private String warrantyStatus;
    private String warrantyProvider;
    private String warrantyTerms;
    private LocalDate startDate;
    private LocalDate endDate;

    // ============================================================
    // 👤 User Info
    // ============================================================
    private Long userId;
    private String username;

    // ============================================================
    // 📎 Document Linkage
    // ============================================================
    private Long documentId;       // Linked document ID (from AssetDocument)
    private String docType;        // Type of document (e.g., WARRANTY_DOC)
    private String filePath;       // Stored file location (useful for UI download)

    // ============================================================
    // 🕒 Audit Info
    // ============================================================
    private Boolean active;
    private String createdBy;
    private String updatedBy;
    private String createdAt;
    private String updatedAt;

    // ============================================================
    // 🔧 Getters and Setters
    // ============================================================

    public Long getWarrantyId() {
        return warrantyId;
    }

    public void setWarrantyId(Long warrantyId) {
        this.warrantyId = warrantyId;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ============================================================
    // 🧠 toString for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "AssetWarrantyDto{" +
                "warrantyId=" + warrantyId +
                ", assetId=" + assetId +
                ", componentId=" + componentId +
                ", warrantyStatus='" + warrantyStatus + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", documentId=" + documentId +
                ", docType='" + docType + '\'' +
                ", filePath='" + filePath + '\'' +
                ", active=" + active +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}




