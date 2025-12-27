
package com.example.asset.dto;

/**
 * ✅ Generic DocumentRequest DTO
 * Used for uploading any file (document, image, etc.)
 * Supports linking to multiple entity types dynamically (Asset, AMC, Warranty, etc.)
 * and maintains backward compatibility with asset/component linkage.
 */
public class DocumentRequest {

    // ============================================================
    // 👤 User Context
    // ============================================================
    private Long userId;
    private String username;
    private String projectType;

    // ============================================================
    // 🔗 Entity Linkage (Generic)
    // ============================================================
    private String entityType; // e.g., ASSET, COMPONENT, AMC, WARRANTY, CATEGORY, SUBCATEGORY, OUTLET, MAKE, MODEL, VENDOR
    private Long entityId;     // Generic ID for linking (e.g., assetId, warrantyId, etc.)

    // ============================================================
    // 🔗 Specific Linkage (for backward compatibility)
    // ============================================================
    private Long assetId;       // Direct link to Asset
    private Long componentId;   // Direct link to Component

    // ============================================================
    // 📎 Document Info
    // ============================================================
    private String docType;     // e.g., IMAGE, PDF, RECEIPT, AGREEMENT

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

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
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

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    // ============================================================
    // 🧠 toString() for debugging/logging
    // ============================================================
    @Override
    public String toString() {
        return "DocumentRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", assetId=" + assetId +
                ", componentId=" + componentId +
                ", docType='" + docType + '\'' +
                '}';
    }
}


