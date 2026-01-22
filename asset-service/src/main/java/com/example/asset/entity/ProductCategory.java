package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * ✅ ProductCategory Entity
 * Represents a high-level category for products.
 * Extends BaseEntity to inherit audit fields and soft-delete support.
 */
@Entity
@Table(
    name = "product_category",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category_name"})
    }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "subCategories"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductCategory extends BaseEntity implements Serializable {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    @Column(name = "category_name", nullable = false, unique = true, length = 200)
    private String categoryName;

    @Column(name = "description", length = 500)
    private String description;

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
     * One category can have multiple subcategories.
     * Lazy fetch ensures performance.
     * Avoids recursion during JSON serialization.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"category", "hibernateLazyInitializer", "handler"})
    private List<ProductSubCategory> subCategories;

    // ============================================================
    // 🏗️ Constructors
    // ============================================================
    public ProductCategory() {}

    public ProductCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public List<ProductSubCategory> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<ProductSubCategory> subCategories) {
        this.subCategories = subCategories;
    }

    // ============================================================
    // 🧠 toString() for Debugging
    // ============================================================
    @Override
    public String toString() {
        return "ProductCategory{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", active=" + getActive() +
                ", createdBy='" + getCreatedBy() + '\'' +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                '}';
    }
}


