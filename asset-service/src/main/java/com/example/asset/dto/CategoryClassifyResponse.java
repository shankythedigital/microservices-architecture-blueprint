package com.example.asset.dto;

import java.io.Serializable;

/**
 * Response DTO for image-based category/subcategory classification.
 * Used when user uploads a photo or scan to get suggested category.
 * Includes master match: categoryId, subCategoryId when matched from DB.
 */
public class CategoryClassifyResponse implements Serializable {

    private String category;
    private String subcategory;
    private String source;       // KEYWORD, LLM, OCR, BARCODE_LOOKUP, DEFAULT
    private String extractedText; // Text extracted from image via OCR (for debugging/transparency)

    /** Brand/logo name extracted from image (OCR + LLM) */
    private String extractedBrandName;
    /** Product name extracted from image (OCR + LLM) */
    private String extractedProductName;

    /** Category ID from master when matched; null if no match */
    private Long categoryId;
    /** SubCategory ID from master when matched; null if no match */
    private Long subCategoryId;
    /** Category name from master (may differ from suggested category) */
    private String matchedCategoryName;
    /** SubCategory name from master (may differ from suggested subcategory) */
    private String matchedSubCategoryName;
    /** True when category and subcategory were found in master */
    private Boolean matchedFromMaster;

    public CategoryClassifyResponse() {}

    public CategoryClassifyResponse(String category, String subcategory, String source, String extractedText) {
        this.category = category;
        this.subcategory = subcategory;
        this.source = source;
        this.extractedText = extractedText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String getExtractedBrandName() {
        return extractedBrandName;
    }

    public void setExtractedBrandName(String extractedBrandName) {
        this.extractedBrandName = extractedBrandName;
    }

    public String getExtractedProductName() {
        return extractedProductName;
    }

    public void setExtractedProductName(String extractedProductName) {
        this.extractedProductName = extractedProductName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getMatchedCategoryName() {
        return matchedCategoryName;
    }

    public void setMatchedCategoryName(String matchedCategoryName) {
        this.matchedCategoryName = matchedCategoryName;
    }

    public String getMatchedSubCategoryName() {
        return matchedSubCategoryName;
    }

    public void setMatchedSubCategoryName(String matchedSubCategoryName) {
        this.matchedSubCategoryName = matchedSubCategoryName;
    }

    public Boolean getMatchedFromMaster() {
        return matchedFromMaster;
    }

    public void setMatchedFromMaster(Boolean matchedFromMaster) {
        this.matchedFromMaster = matchedFromMaster;
    }
}
