package com.example.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to create/update Issue Master.
 * At least one of categoryId, subCategoryId, componentId, sparePartId must be provided.
 */
public class IssueMasterRequest {
    @NotBlank(message = "Issue title is required")
    private String issueTitle;

    private String issueDescription;

    private Long categoryId;
    private Long subCategoryId;
    private Long componentId;
    private Long sparePartId;

    public String getIssueTitle() { return issueTitle; }
    public void setIssueTitle(String issueTitle) { this.issueTitle = issueTitle; }
    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getSubCategoryId() { return subCategoryId; }
    public void setSubCategoryId(Long subCategoryId) { this.subCategoryId = subCategoryId; }
    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }
    public Long getSparePartId() { return sparePartId; }
    public void setSparePartId(Long sparePartId) { this.sparePartId = sparePartId; }
}
