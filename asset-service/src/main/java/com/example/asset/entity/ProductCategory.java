package com.example.asset.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_category")
public class ProductCategory extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;
    private String categoryName;

    public ProductCategory(){}
    public ProductCategory(String categoryName){ this.categoryName = categoryName; }

    public Long getCategoryId(){ return categoryId; }
    public void setCategoryId(Long categoryId){ this.categoryId = categoryId; }
    public String getCategoryName(){ return categoryName; }
    public void setCategoryName(String categoryName){ this.categoryName = categoryName; }
}
