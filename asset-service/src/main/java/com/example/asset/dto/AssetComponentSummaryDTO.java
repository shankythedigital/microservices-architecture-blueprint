package com.example.asset.dto;

/**
 * Lightweight component row for asset API responses (catalog component + optional image URL).
 */
public class AssetComponentSummaryDTO {

    private Long componentId;
    private String componentName;
    private String imageUrl;

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
