
package com.example.asset.dto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * ✅ AssetAmcDto
 * Safe DTO for transferring AMC data between layers.
 */
public class AssetAmcDto implements Serializable {

    private Long amcId;
    private String amcStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private Long userId;
    private String username;
    private Long componentId;
    private Long assetId;
    private Long documentId;

    // Getters & Setters
    public Long getAmcId() { return amcId; }
    public void setAmcId(Long amcId) { this.amcId = amcId; }

    public String getAmcStatus() { return amcStatus; }
    public void setAmcStatus(String amcStatus) { this.amcStatus = amcStatus; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
}


