package com.example.asset.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_make")
public class ProductMake extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long makeId;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    private ProductSubCategory subCategory;

    private String makeName;

    public ProductMake(){}
    public ProductMake(String makeName, ProductSubCategory subCategory){
        this.makeName = makeName; this.subCategory = subCategory;
    }

    public Long getMakeId(){ return makeId; }
    public void setMakeId(Long makeId){ this.makeId = makeId; }
    public ProductSubCategory getSubCategory(){ return subCategory; }
    public void setSubCategory(ProductSubCategory subCategory){ this.subCategory = subCategory; }
    public String getMakeName(){ return makeName; }
    public void setMakeName(String makeName){ this.makeName = makeName; }
}
