

package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

/**
 * ✅ ProductModel Entity
 * Represents a specific model belonging to a product make.
 * Uniqueness is enforced per (make + modelName) combination.
 */
@Entity
@Table(
    name = "product_model",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"model_name", "make_id"})
    }
)
public class ProductModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "make_id", nullable = false)
    private ProductMake make;

    @Column(name = "model_name", nullable = false, length = 150)
    private String modelName;

    @Column(length = 255)
    private String description;

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public ProductModel() {}

    public ProductModel(String modelName, ProductMake make) {
        this.modelName = modelName;
        this.make = make;
    }

    // ============================================================
    // ✅ Getters and Setters
    // ============================================================

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public ProductMake getMake() {
        return make;
    }

    public void setMake(ProductMake make) {
        this.make = make;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ============================================================
    // ✅ Convenience Methods
    // ============================================================

    @Override
    public String toString() {
        return "ProductModel{" +
                "modelId=" + modelId +
                ", modelName='" + modelName + '\'' +
                ", make=" + (make != null ? make.getMakeName() : "null") +
                ", active=" + getActive() +
                '}';
    }
}

