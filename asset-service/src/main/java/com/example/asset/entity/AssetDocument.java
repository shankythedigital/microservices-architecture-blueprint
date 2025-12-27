package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ✅ AssetDocument Entity
 *
 * Represents uploaded documents (images, files, etc.) linked to
 * any entity (Asset, AMC, Warranty, Category, etc.).
 *
 * - Supports dynamic linking via (entityType, entityId)
 * - Maintains soft delete (active flag)
 * - Includes user & audit metadata
 */
@Entity
@Table(name = "asset_document",
       indexes = {
           @Index(name = "idx_entity_type_id", columnList = "entity_type, entity_id"),
           @Index(name = "idx_doc_type", columnList = "doc_type")
       })
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "asset"})
public class AssetDocument extends BaseEntity implements Serializable {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    // ============================================================
    // 📎 Linkage (Generic Entity Reference)
    // ============================================================
    /**
     * The entity type this document belongs to (e.g. ASSET, AMC, WARRANTY).
     */
    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    /**
     * The ID of the entity this document is linked to.
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    // ============================================================
    // 📦 File Metadata
    // ============================================================
    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "doc_type", length = 100)
    private String docType;

    @Column(name = "uploaded_date")
    private LocalDateTime uploadedDate;

    // ============================================================
    // 👤 User Context
    // ============================================================
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "project_type", length = 255)
    private String projectType;

    // ============================================================
    // 🔗 Asset Relationship (Backward Compatibility)
    // ============================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    @JsonIgnoreProperties({"documents", "hibernateLazyInitializer", "handler"})
    private AssetMaster asset;

    // ============================================================
    // 🧾 Custom State
    // ============================================================
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // ============================================================
    // 🔧 Getters & Setters
    // ============================================================

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public LocalDateTime getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(LocalDateTime uploadedDate) {
        this.uploadedDate = uploadedDate;
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

    public AssetMaster getAsset() {
        return asset;
    }

    public void setAsset(AssetMaster asset) {
        this.asset = asset;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    // ============================================================
    // 🧠 toString for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "AssetDocument{" +
                "documentId=" + documentId +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", fileName='" + fileName + '\'' +
                ", docType='" + docType + '\'' +
                ", filePath='" + filePath + '\'' +
                ", uploadedDate=" + uploadedDate +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", active=" + active +
                '}';
    }
}


