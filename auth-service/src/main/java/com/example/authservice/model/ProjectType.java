package com.example.authservice.model;

import jakarta.persistence.*;
import com.example.common.jpa.BaseEntity;

/**
 * ✅ ProjectType Entity
 * Master table for project types (e.g., ECOM, ASSET, etc.)
 * Used to categorize users and maintain referential integrity.
 */
@Entity
@Table(
    name = "project_type",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"code"})
    }
)
public class ProjectType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_type_id")
    private Long projectTypeId;

    /**
     * Unique code for the project type (e.g., ECOM, ASSET, INVENTORY)
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Human-readable name of the project type
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Description of the project type
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Display order for UI sorting
     */
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // ============================================================
    // 🧾 Constructors
    // ============================================================
    public ProjectType() {
    }

    public ProjectType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public ProjectType(String code, String name, String description, Integer displayOrder) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getProjectTypeId() {
        return projectTypeId;
    }

    public void setProjectTypeId(Long projectTypeId) {
        this.projectTypeId = projectTypeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    // ============================================================
    // 🧾 toString, equals, hashCode
    // ============================================================
    @Override
    public String toString() {
        return "ProjectType{" +
                "projectTypeId=" + projectTypeId +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", displayOrder=" + displayOrder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectType that = (ProjectType) o;
        return projectTypeId != null && projectTypeId.equals(that.projectTypeId);
    }

    @Override
    public int hashCode() {
        return projectTypeId != null ? projectTypeId.hashCode() : 0;
    }
}
