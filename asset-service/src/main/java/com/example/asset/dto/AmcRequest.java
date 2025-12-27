

package com.example.asset.dto;

import com.example.asset.entity.AssetAmc;

/**
 * ✅ AmcRequest DTO
 * Wrapper for handling AMC CRUD requests with user details and project context.
 */
public class AmcRequest {

    private Long userId;
    private String username;
    private String projectType;
    private AssetAmc amc;
    
    public AmcRequest(Long userId, String username, String projectType, AssetAmc amc) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.amc = amc;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public AssetAmc getAmc() { return amc; }
    public void setAmc(AssetAmc amc) { this.amc = amc; }
}


