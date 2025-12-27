
package com.example.asset.dto;

import com.example.asset.entity.ProductModel;

/**
 * ✅ ModelRequest DTO
 * Wrapper for handling ProductModel CRUD operations.
 * 
 * Includes:
 *  - userId       → Request initiator (for audit)
 *  - username     → Name of the user performing the action
 *  - projectType  → Originating microservice or context (e.g., ASSET_SERVICE)
 *  - model        → The ProductModel entity payload
 */
public class ModelRequest {

    private Long userId;
    private String username;
    private String projectType;
    private ProductModel model;

    // ============================================================
    // 🧩 Constructors
    // ============================================================
    public ModelRequest() {}

    public ModelRequest(Long userId, String username, String projectType, ProductModel model) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.model = model;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public ProductModel getModel() {
        return model;
    }

    public void setModel(ProductModel model) {
        this.model = model;
    }

    // ============================================================
    // 🧠 toString (for logging and debugging)
    // ============================================================
    @Override
    public String toString() {
        return "ModelRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", model=" + (model != null ? model.getModelName() : "null") +
                '}';
    }
}

