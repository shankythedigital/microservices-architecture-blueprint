package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * AssetMaster — master record for an asset.
 * Keeps relationships to category, subcategory, make, model, components, documents,
 * warranty, amc and user links (one-to-many).
 *
 * Uses BaseEntity for audit fields: createdBy, createdAt, updatedBy, updatedAt, active.
 */
@Entity
@Table(name = "asset_master")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AssetMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Long assetId;

    @Column(name = "asset_name_udv", nullable = false, unique = true, length = 255)
    private String assetNameUdv;

    @Column(name = "serial_number", length = 255)
    private String serialNumber;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    // CATEGORY / SUBCATEGORY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({ "assets", "subCategories" })
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    @JsonIgnoreProperties({ "assets" })
    private ProductSubCategory subCategory;

    // MAKE / MODEL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "make_id")
    @JsonIgnoreProperties({ "assets" })
    private ProductMake make;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    @JsonIgnoreProperties({ "assets" })
    private ProductModel model;

    // WARRANTY & AMC (one-to-one)
    @OneToOne(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private AssetWarranty warranty;

    @OneToOne(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private AssetAmc amc;

    // USER LINKS — an asset may have multiple link records (historical + active)
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<AssetUserLink> userLinks = new HashSet<>();

    // COMPONENTS (many-to-many join table)
    @ManyToMany
    @JoinTable(
            name = "asset_component_link",
            joinColumns = @JoinColumn(name = "asset_id"),
            inverseJoinColumns = @JoinColumn(name = "component_id")
    )
    @JsonIgnoreProperties({ "assets" })
    private Set<AssetComponent> components = new HashSet<>();

    // DOCUMENTS
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<AssetDocument> documents = new HashSet<>();

    @Transient
    private String displayName;

    @Column(name = "asset_status")
    private String assetStatus;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_favourite")
    private Boolean isFavourite = false;

    @Column(name = "is_most_like")
    private Boolean isMostLike = false;

    // ------------------------
    // Getters / Setters
    // ------------------------
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetNameUdv() { return assetNameUdv; }
    public void setAssetNameUdv(String assetNameUdv) { this.assetNameUdv = assetNameUdv; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }

    public ProductSubCategory getSubCategory() { return subCategory; }
    public void setSubCategory(ProductSubCategory subCategory) { this.subCategory = subCategory; }

    public ProductMake getMake() { return make; }
    public void setMake(ProductMake make) { this.make = make; }

    public ProductModel getModel() { return model; }
    public void setModel(ProductModel model) { this.model = model; }

    public AssetWarranty getWarranty() { return warranty; }
    public void setWarranty(AssetWarranty warranty) { this.warranty = warranty; }

    public AssetAmc getAmc() { return amc; }
    public void setAmc(AssetAmc amc) { this.amc = amc; }

    public Set<AssetUserLink> getUserLinks() { return userLinks; }
    public void setUserLinks(Set<AssetUserLink> userLinks) { this.userLinks = userLinks; }

    public Set<AssetComponent> getComponents() { return components; }
    public void setComponents(Set<AssetComponent> components) { this.components = components; }

    public Set<AssetDocument> getDocuments() { return documents; }
    public void setDocuments(Set<AssetDocument> documents) { this.documents = documents; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAssetStatus() { return assetStatus; }
    public void setAssetStatus(String assetStatus) { this.assetStatus = assetStatus; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
    public Boolean getIsFavourite() { return isFavourite; }
    public void setIsFavourite(Boolean isFavourite) { this.isFavourite = isFavourite; }
    public Boolean getIsMostLike() { return isMostLike; }
    public void setIsMostLike(Boolean isMostLike) { this.isMostLike = isMostLike; }
}


