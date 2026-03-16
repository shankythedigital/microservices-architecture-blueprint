package com.example.helpdesk.dto;

import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.RelatedService;
import jakarta.validation.constraints.NotNull;
public class IssueRequest {
    /**
     * When raising ticket from issue master list: provide issueMasterId.
     * When creating custom issue: provide title and description.
     */
    private Long issueMasterId;

    private String title;
    private String description;

    @NotNull(message = "Priority is required")
    private IssuePriority priority;

    @NotNull(message = "Related service is required")
    private RelatedService relatedService;

    private Long assetId;
    private Long componentId;
    private Long sparePartId;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }
    public RelatedService getRelatedService() { return relatedService; }
    public void setRelatedService(RelatedService relatedService) { this.relatedService = relatedService; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }
    public Long getSparePartId() { return sparePartId; }
    public void setSparePartId(Long sparePartId) { this.sparePartId = sparePartId; }
    public Long getIssueMasterId() { return issueMasterId; }
    public void setIssueMasterId(Long issueMasterId) { this.issueMasterId = issueMasterId; }
}

