package com.example.asset.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_model")
public class ProductModel extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modelId;

    @ManyToOne
    @JoinColumn(name = "make_id")
    private ProductMake make;

    private String modelName;

    public ProductModel(){}
    public ProductModel(String modelName, ProductMake make){
        this.modelName = modelName; this.make = make;
    }

    public Long getModelId(){ return modelId; }
    public void setModelId(Long modelId){ this.modelId = modelId; }
    public ProductMake getMake(){ return make; }
    public void setMake(ProductMake make){ this.make = make; }
    public String getModelName(){ return modelName; }
    public void setModelName(String modelName){ this.modelName = modelName; }
}
