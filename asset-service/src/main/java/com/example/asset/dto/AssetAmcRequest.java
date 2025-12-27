

package com.example.asset.dto;

import java.time.LocalDate;

/**
 * ✅ AssetAmcRequest
 * Wrapper for AMC create/update operations.
 * Compatible with multipart/form-data uploads.
 */
public class AssetAmcRequest {

    // ============================================================
    // 👤 User Context
    // ============================================================
    private Long userId;
    private String username;
    private String projectType;

    // ============================================================
    // 🔗 Asset Context
    // ============================================================
    private Long assetId;
    private Long componentId;

    // ============================================================
    // 🧾 AMC Details
    // ============================================================
    private String amcStatus;
    private LocalDate startDate;
    private LocalDate endDate;


    // ============================================================
    // 📎 Optional Document Link (managed by DocumentController)
    // ============================================================
    private Long documentId;
    private String docType;


    // ============================================================
    // 🧾 Getters & Setters
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


    public String getAmcStatus() {
        return amcStatus;
    }

    public void setAmcStatus(String amcStatus) {
        this.amcStatus = amcStatus;
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
    // ============================================================
    // 🧠 toString (for debugging/logging)
    // ============================================================
    @Override
    public String toString() {
        return "AssetAmcRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", assetId=" + assetId +
                ", componentId=" + componentId +
                ", amcStatus='" + amcStatus + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}





