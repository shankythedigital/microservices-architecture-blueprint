package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * ✅ AssetWarranty Entity
 * Represents warranty details for an asset.
 * Linked with AssetMaster and optionally an AssetDocument.
 * Handles null-safe document_id persistence.
 */
@Entity
@Table(name = "asset_warranty")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "asset"})
public class AssetWarranty extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warranty_id")
    private Long warrantyId;

    @Column(name = "warranty_status")
    private String warrantyStatus;

    @Column(name = "warranty_provider")
    private String warrantyProvider;

    @Column(name = "warranty_terms", length = 1000)
    private String warrantyTerms;

    @Column(name = "start_date")
    private LocalDate warrantyStartDate;

    @Column(name = "end_date")
    private LocalDate warrantyEndDate;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "component_id")
    private Long componentId;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_favourite")
    private Boolean isFavourite = false;

    @Column(name = "is_most_like")
    private Boolean isMostLike = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    @JsonIgnoreProperties({"warranties", "hibernateLazyInitializer", "handler"})
    private AssetMaster asset;

    // ✅ Persisted foreign key to AssetDocument table
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", referencedColumnName = "document_id", nullable = true)
    private AssetDocument document;

    // ============================================================
    // 🔧 Getters & Setters
    // ============================================================

    public Long getWarrantyId() {
        return warrantyId;
    }

    public void setWarrantyId(Long warrantyId) {
        this.warrantyId = warrantyId;
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

    public LocalDate getWarrantyStartDate() {
        return warrantyStartDate;
    }

    public void setWarrantyStartDate(LocalDate warrantyStartDate) {
        this.warrantyStartDate = warrantyStartDate;
    }

    public LocalDate getWarrantyEndDate() {
        return warrantyEndDate;
    }

    public void setWarrantyEndDate(LocalDate warrantyEndDate) {
        this.warrantyEndDate = warrantyEndDate;
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

    // ✅ Safe getter for FK
    public Long getDocumentId() {
        return (document != null) ? document.getDocumentId() : null;
    }

    // ✅ Handles both null and valid ID values
    public void setDocumentId(Long documentId) {
        if (documentId == null) {
            this.document = null; // store as NULL in DB
        } else {
            AssetDocument doc = new AssetDocument();
            doc.setDocumentId(documentId);
            this.document = doc;
        }
    }

    // ============================================================
    // 🧠 Convenience Aliases
    // ============================================================
    @Transient
    public LocalDate getStartDate() {
        return getWarrantyStartDate();
    }

    @Transient
    public LocalDate getEndDate() {
        return getWarrantyEndDate();
    }

    @Override
    public String toString() {
        return "AssetWarranty{" +
                "warrantyId=" + warrantyId +
                ", warrantyStatus='" + warrantyStatus + '\'' +
                ", warrantyProvider='" + warrantyProvider + '\'' +
                ", warrantyStartDate=" + warrantyStartDate +
                ", warrantyEndDate=" + warrantyEndDate +
                ", documentId=" + getDocumentId() +
                ", username='" + username + '\'' +
                '}';
    }
}


