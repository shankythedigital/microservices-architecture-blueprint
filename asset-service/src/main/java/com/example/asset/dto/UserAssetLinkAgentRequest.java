package com.example.asset.dto;

import java.util.List;

/**
 * ✅ UserAssetLinkAgentRequest DTO
 * Request DTO for user-asset link agent operations.
 * Used for linking/delinking assets and components to users.
 */
public class UserAssetLinkAgentRequest {
    
    private Long assetId;
    private Long componentId;
    private Long userId;
    private String username;
    private String email;
    private String mobile;
    private String createdBy;
    private String updatedBy;
    
    // For bulk operations
    private List<Long> assetIds;

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    
    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<Long> getAssetIds() {
        return assetIds;
    }

    public void setAssetIds(List<Long> assetIds) {
        this.assetIds = assetIds;
    }
}

