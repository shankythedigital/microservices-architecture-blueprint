package com.example.asset.dto;

import com.example.asset.entity.PurchaseOutlet;

/**
 * ✅ OutletRequest DTO
 * Wrapper for handling CRUD operations for PurchaseOutlet.
 * 
 * Includes:
 *  - userId       → Request initiator ID (for audit)
 *  - username     → Name of user performing the operation
 *  - projectType  → Originating microservice (e.g., ASSET_SERVICE)
 *  - outlet       → PurchaseOutlet entity payload
 */
public class OutletRequest {

    private Long userId;
    private String username;
    private String projectType;
    private PurchaseOutlet outlet;

    // ============================================================
    // 🧩 Constructors
    // ============================================================
    public OutletRequest() {}

    public OutletRequest(Long userId, String username, String projectType, PurchaseOutlet outlet) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.outlet = outlet;
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

    public PurchaseOutlet getOutlet() {
        return outlet;
    }

    public void setOutlet(PurchaseOutlet outlet) {
        this.outlet = outlet;
    }

    // ============================================================
    // 🧠 toString (useful for logging and debugging)
    // ============================================================
    @Override
    public String toString() {
        return "OutletRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", outlet=" + (outlet != null ? outlet.getOutletName() : "null") +
                '}';
    }
}

