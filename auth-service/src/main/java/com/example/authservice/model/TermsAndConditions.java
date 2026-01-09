package com.example.authservice.model;

import jakarta.persistence.*;
import com.example.common.jpa.BaseEntity;

/**
 * ✅ TermsAndConditions Entity
 * Stores Terms and Conditions content for different project types and versions.
 * Allows versioning and project-specific T&C.
 */
@Entity
@Table(
    name = "terms_and_conditions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_type", "version"})
    }
)
public class TermsAndConditions extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tc_id")
    private Long tcId;

    /**
     * Project type code (e.g., ASSET_SERVICE, ECOM)
     * Can be null for global/default T&C
     */
    @Column(name = "project_type", length = 50)
    private String projectType;

    /**
     * Version number (e.g., "1.0", "2.0")
     */
    @Column(name = "version", nullable = false, length = 20)
    private String version;

    /**
     * Title of the Terms and Conditions
     */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * Full content of Terms and Conditions (can be HTML or plain text)
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Short summary/description
     */
    @Column(name = "summary", length = 500)
    private String summary;

    /**
     * Language code (e.g., "en", "hi", "es")
     */
    @Column(name = "language", length = 10)
    private String language = "en";

    /**
     * Whether this is the active/current version
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Effective date from which this version is applicable
     */
    @Column(name = "effective_date")
    private java.time.LocalDate effectiveDate;

    /**
     * Display order for UI sorting
     */
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // ============================================================
    // 🧾 Constructors
    // ============================================================
    public TermsAndConditions() {
    }

    public TermsAndConditions(String projectType, String version, String title, String content) {
        this.projectType = projectType;
        this.version = version;
        this.title = title;
        this.content = content;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getTcId() {
        return tcId;
    }

    public void setTcId(Long tcId) {
        this.tcId = tcId;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public java.time.LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(java.time.LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
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
        return "TermsAndConditions{" +
                "tcId=" + tcId +
                ", projectType='" + projectType + '\'' +
                ", version='" + version + '\'' +
                ", title='" + title + '\'' +
                ", isActive=" + isActive +
                ", language='" + language + '\'' +
                '}';
    }
}

