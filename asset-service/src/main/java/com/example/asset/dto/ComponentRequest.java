
package com.example.asset.dto;

import com.example.asset.entity.AssetComponent;

/**
 * ✅ ComponentRequest DTO
 * Used for @RequestBody requests that include user context.
 */
public class ComponentRequest {

    private Long userId;
    private String username;
    private String projectType;
    private AssetComponent component;

    // ----- Getters -----
    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getProjectType() {
        return projectType;
    }

    public AssetComponent getComponent() {
        return component;
    }

    // ----- Setters -----
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public void setComponent(AssetComponent component) {
        this.component = component;
    }
}


