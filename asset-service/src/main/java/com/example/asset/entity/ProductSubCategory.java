package com.example.asset.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_sub_category")
public class ProductSubCategory extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subCategoryId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    private String subCategoryName;

    public ProductSubCategory(){}
    public ProductSubCategory(String subCategoryName, ProductCategory category){
        this.subCategoryName = subCategoryName; this.category = category;
    }

    public Long getSubCategoryId(){ return subCategoryId; }
    public void setSubCategoryId(Long subCategoryId){ this.subCategoryId = subCategoryId; }
    public ProductCategory getCategory(){ return category; }
    public void setCategory(ProductCategory category){ this.category = category; }
    public String getSubCategoryName(){ return subCategoryName; }
    public void setSubCategoryName(String subCategoryName){ this.subCategoryName = subCategoryName; }
}
