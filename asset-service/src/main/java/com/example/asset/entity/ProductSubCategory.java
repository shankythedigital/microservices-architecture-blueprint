


package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import java.io.Serializable;


/**
 * ✅ ProductSubCategory Entity
 * Represents a subcategory within a product category.
 * Linked to ProductCategory, includes audit information.
 */
@Entity
@Table(name = "product_sub_category")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSubCategory  extends BaseEntity  implements Serializable {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_category_id")
    private Long subCategoryId;

    // ============================================================
    // 📦 Fields
    // ============================================================
    @Column(name = "sub_category_name", nullable = false, unique = true)
    private String subCategoryName;

    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private Boolean active = true;

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
     * Each SubCategory belongs to one Category.
     * Using LAZY fetch to avoid unnecessary joins, with safe serialization via @JsonIgnoreProperties.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"subCategories", "hibernateLazyInitializer", "handler"})
    private ProductCategory category;


    // ============================================================
    // 🏗️ Constructors
    // ============================================================
    public ProductSubCategory() {}

    public ProductSubCategory(String subCategoryName, ProductCategory category) {
        this.subCategoryName = subCategoryName;
        this.category = category;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }


    // ============================================================
    // 🧠 toString() for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "ProductSubCategory{" +
                "subCategoryId=" + subCategoryId +
                ", subCategoryName='" + subCategoryName + '\'' +
                ", active=" + active +
                ", category=" + (category != null ? category.getCategoryName() : "null") +
                '}';
    }
}


