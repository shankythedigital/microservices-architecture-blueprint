package com.example.asset.dto;

import com.example.asset.entity.ProductMake;

/**
 * ✅ MakeRequest DTO
 * Wrapper for all ProductMake operations.
 * Includes:
 *  - userId (Long)
 *  - username (String)
 *  - projectType (String, optional)
 *  - make (ProductMake entity payload)
 */
public class MakeRequest {

    private Long userId;
    private String username;
    private String projectType;
    private ProductMake make;

    // ============================================================
    // 🧩 Constructors
    // ============================================================
    public MakeRequest() {
    }

    public MakeRequest(Long userId, String username, String projectType, ProductMake make) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.make = make;
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

    public ProductMake getMake() {
        return make;
    }

    public void setMake(ProductMake make) {
        this.make = make;
    }

    // ============================================================
    // 🧠 toString (for logging)
    // ============================================================
    @Override
    public String toString() {
        return "MakeRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", make=" + (make != null ? make.getMakeName() : "null") +
                '}';
    }
}


