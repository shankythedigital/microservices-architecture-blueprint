package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * ✅ AssetAmc Entity
 * Represents Annual Maintenance Contract details for an asset.
 * Automatically linked to uploaded documents and assets.
 * Handles null-safe document_id persistence.
 */
@Entity
@Table(name = "asset_amc")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "asset"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetAmc extends BaseEntity implements Serializable {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amc_id")
    private Long amcId;

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    @Column(name = "amc_status", length = 100)
    private String amcStatus;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "component_id")
    private Long componentId;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_favourite")
    private Boolean isFavourite = false;

    @Column(name = "is_most_like")
    private Boolean isMostLike = false;

    // ============================================================
    // 🔗 Relationships
    // ============================================================

    /**
     * Many AMCs can belong to one Asset.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AssetMaster asset;

    /**
     * Each AMC can have one linked uploaded document.
     * If null, document_id will be stored as NULL.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", referencedColumnName = "document_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AssetDocument document;

    // ============================================================
    // 🔧 Getters and Setters
    // ============================================================

    public Long getAmcId() {
        return amcId;
    }

    public void setAmcId(Long amcId) {
        this.amcId = amcId;
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

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public Boolean getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(Boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    public Boolean getIsMostLike() {
        return isMostLike;
    }

    public void setIsMostLike(Boolean isMostLike) {
        this.isMostLike = isMostLike;
    }

    public AssetMaster getAsset() {
        return asset;
    }

    public void setAsset(AssetMaster asset) {
        this.asset = asset;
    }

    public AssetDocument getDocument() {
        return document;
    }

    public void setDocument(AssetDocument document) {
        this.document = document;
    }

    // ✅ Null-safe foreign key management
    public Long getDocumentId() {
        return (document != null) ? document.getDocumentId() : null;
    }

    public void setDocumentId(Long documentId) {
        if (documentId == null) {
            this.document = null; // store NULL in DB
        } else {
            AssetDocument doc = new AssetDocument();
            doc.setDocumentId(documentId);
            this.document = doc; // store FK reference
        }
    }

    // ============================================================
    // 🧠 Debug-friendly toString()
    // ============================================================
    @Override
    public String toString() {
        return "AssetAmc{" +
                "amcId=" + amcId +
                ", amcStatus='" + amcStatus + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", componentId=" + componentId +
                ", assetId=" + (asset != null ? asset.getAssetId() : null) +
                ", documentId=" + getDocumentId() +
                '}';
    }
}


