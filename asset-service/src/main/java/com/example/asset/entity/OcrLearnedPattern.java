package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

/**
 * ✅ OcrLearnedPattern Entity
 * Stores learned patterns from OCR training data.
 * These patterns improve extraction accuracy over time.
 */
@Entity
@Table(name = "ocr_learned_pattern")
public class OcrLearnedPattern extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pattern_id")
    private Long patternId;

    @Column(name = "pattern_type", length = 50, nullable = false)
    private String patternType; // MAKE, MODEL, SERIAL, CATEGORY, etc.

    @Column(name = "pattern_regex", columnDefinition = "TEXT")
    private String patternRegex; // Learned regex pattern

    @Column(name = "pattern_keywords", columnDefinition = "TEXT")
    private String patternKeywords; // JSON array of keywords

    @Column(name = "context_before", length = 100)
    private String contextBefore; // Text that appears before the value

    @Column(name = "context_after", length = 100)
    private String contextAfter; // Text that appears after the value

    @Column(name = "confidence_weight")
    private Double confidenceWeight = 1.0; // Weight for this pattern

    @Column(name = "usage_count")
    private Integer usageCount = 0; // How many times this pattern was used successfully

    @Column(name = "success_count")
    private Integer successCount = 0; // How many times it was correct

    @Column(name = "sub_category_id")
    private Long subCategoryId; // Pattern specific to subcategory

    @Column(name = "make_id")
    private Long makeId; // Pattern specific to make

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_used_at")
    private java.time.LocalDateTime lastUsedAt;

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public OcrLearnedPattern() {}

    // ============================================================
    // ✅ Getters and Setters
    // ============================================================
    public Long getPatternId() {
        return patternId;
    }

    public void setPatternId(Long patternId) {
        this.patternId = patternId;
    }

    public String getPatternType() {
        return patternType;
    }

    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }

    public String getPatternRegex() {
        return patternRegex;
    }

    public void setPatternRegex(String patternRegex) {
        this.patternRegex = patternRegex;
    }

    public String getPatternKeywords() {
        return patternKeywords;
    }

    public void setPatternKeywords(String patternKeywords) {
        this.patternKeywords = patternKeywords;
    }

    public String getContextBefore() {
        return contextBefore;
    }

    public void setContextBefore(String contextBefore) {
        this.contextBefore = contextBefore;
    }

    public String getContextAfter() {
        return contextAfter;
    }

    public void setContextAfter(String contextAfter) {
        this.contextAfter = contextAfter;
    }

    public Double getConfidenceWeight() {
        return confidenceWeight;
    }

    public void setConfidenceWeight(Double confidenceWeight) {
        this.confidenceWeight = confidenceWeight;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public Long getMakeId() {
        return makeId;
    }

    public void setMakeId(Long makeId) {
        this.makeId = makeId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public java.time.LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(java.time.LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}

