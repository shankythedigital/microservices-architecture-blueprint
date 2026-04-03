package com.example.asset.dto;

import java.io.Serializable;
import com.example.common.jpa.BaseEntity;

/**
 * ✅ CategoryDto
 * Data Transfer Object for {@link com.example.asset.entity.ProductCategory}.
 * 
 * Used for safely transferring category data between layers
 * (controller ↔ service ↔ client) without exposing entity internals.
 */
public class CategoryDto extends BaseEntity implements Serializable {

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    private Long categoryId;
    private String categoryName;
    private String description;
    private Integer sequenceOrder;
    private Boolean isFavourite;
    private Boolean isMostLike;

    private String imageUrl;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // ============================================================
    // 🧠 toString() for Logging
    // ============================================================
    @Override
    public String toString() {
        return "CategoryDto{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", description='" + description + '\'' +
                ", active=" + getActive()+
                ", createdBy='" + getCreatedBy() + '\'' +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}



