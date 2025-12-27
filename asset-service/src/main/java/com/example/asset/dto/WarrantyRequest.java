
package com.example.asset.dto;

import com.example.asset.entity.AssetWarranty;

/**
 * ✅ WarrantyRequest DTO
 * Unified request wrapper for all warranty operations.
 */
public class WarrantyRequest {

    private Long userId;
    private String username;
    private String projectType;
    private AssetWarranty warranty;

    // --- Getters & Setters ---
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public AssetWarranty getWarranty() { return warranty; }
    public void setWarranty(AssetWarranty warranty) { this.warranty = warranty; }
}


