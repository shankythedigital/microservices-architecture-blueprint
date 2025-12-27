package com.example.asset.dto;

import com.example.asset.entity.AssetMaster;

/**
 * ✅ AssetRequest
 * Wrapper DTO for asset CRUD operations with audit metadata (userId, username, projectType).
 */
public class AssetRequest {

    private Long userId;
    private String username;
    private String projectType;
    private AssetMaster asset;

    // -------------------------------
    // 🧩 Getters and Setters
    // -------------------------------
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

    public AssetMaster getAsset() {
        return asset;
    }

    public void setAsset(AssetMaster asset) {
        this.asset = asset;
    }

    @Override
    public String toString() {
        return "AssetRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", projectType='" + projectType + '\'' +
                ", asset=" + (asset != null ? asset.toString() : "null") +
                '}';
    }
}

