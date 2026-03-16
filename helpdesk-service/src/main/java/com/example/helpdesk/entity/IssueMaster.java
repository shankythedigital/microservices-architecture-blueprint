package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

/**
 * Issue Master: predefined issue types linked to category, subcategory, component, or spare part.
 * When an issue type is not in the list, user can add it by selecting from master tables.
 * References: category_id, sub_category_id, component_id, spare_part_id (asset-service masters).
 */
@Entity
@Table(name = "issue_master")
public class IssueMaster extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_title", nullable = false, length = 255)
    private String issueTitle;

    @Column(name = "issue_description", columnDefinition = "TEXT")
    private String issueDescription;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "sub_category_id")
    private Long subCategoryId;

    @Column(name = "component_id")
    private Long componentId;

    @Column(name = "spare_part_id")
    private Long sparePartId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
