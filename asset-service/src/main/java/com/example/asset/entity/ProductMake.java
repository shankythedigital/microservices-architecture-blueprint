package com.example.asset.entity;

import jakarta.persistence.*;
import com.example.common.jpa.BaseEntity;


@Entity
@Table(name = "product_make")
public class ProductMake extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long makeId;

    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    private ProductSubCategory subCategory;

    private String makeName;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_favourite")
    private Boolean isFavourite = false;

    @Column(name = "is_most_like")
    private Boolean isMostLike = false;

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
    public Integer getSequenceOrder(){ return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder){ this.sequenceOrder = sequenceOrder; }
    public Boolean getIsFavourite(){ return isFavourite; }
    public void setIsFavourite(Boolean isFavourite){ this.isFavourite = isFavourite; }
    public Boolean getIsMostLike(){ return isMostLike; }
    public void setIsMostLike(Boolean isMostLike){ this.isMostLike = isMostLike; }
}
