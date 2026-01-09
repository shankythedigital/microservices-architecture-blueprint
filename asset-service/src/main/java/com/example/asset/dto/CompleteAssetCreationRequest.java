package com.example.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * ✅ CompleteAssetCreationRequest
 * DTO for creating an asset with all related information in one request:
 * - Asset basic info (name, model)
 * - Serial number
 * - Warranty information (start/end dates)
 * - Purchase invoice document
 * - User assignment
 */
public class CompleteAssetCreationRequest {

    // ============================================================
    // 👤 User Context
    // ============================================================
    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "username is required")
    private String username;

    private String projectType;

    // ============================================================
    // 📦 Asset Basic Information
    // ============================================================
    @NotBlank(message = "assetNameUdv (Title/UI Name) is required")
    private String assetNameUdv; // Title/UI Name

    @NotNull(message = "modelId (Model Number) is required")
    private Long modelId; // Model Number

    private String serialNumber; // Serial Number (optional)

    // Optional asset fields
    private Long categoryId;
    private Long subCategoryId;
    private Long makeId;
    private String assetStatus;

    // ============================================================
    // 🛡️ Warranty Information
    // ============================================================
    @NotNull(message = "warrantyStartDate (Purchase and Installation Date) is required")
    private LocalDate warrantyStartDate; // Purchase and Installation Date

    @NotNull(message = "warrantyEndDate (Limited Warranty) is required")
    private LocalDate warrantyEndDate; // Limited Warranty

    private String warrantyProvider;
    private String warrantyStatus;
    private String warrantyTerms;

    // ============================================================
    // 👥 User Assignment
    // ============================================================
    @NotNull(message = "targetUserId (Added to) is required")
    private Long targetUserId; // Added to - User ID to assign asset to

    private String targetUsername; // Added to - Username (optional, can be derived)

    // ============================================================
    // 🔧 Getters & Setters
    // ============================================================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getAssetNameUdv() {
        return assetNameUdv;
    }

    public void setAssetNameUdv(String assetNameUdv) {
        this.assetNameUdv = assetNameUdv;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
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

    public Long getMakeId() {
        return makeId;
    }

    public void setMakeId(Long makeId) {
        this.makeId = makeId;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    public LocalDate getWarrantyStartDate() {
        return warrantyStartDate;
    }

    public void setWarrantyStartDate(LocalDate warrantyStartDate) {
        this.warrantyStartDate = warrantyStartDate;
    }

    public LocalDate getWarrantyEndDate() {
        return warrantyEndDate;
    }

    public void setWarrantyEndDate(LocalDate warrantyEndDate) {
        this.warrantyEndDate = warrantyEndDate;
    }

    public String getWarrantyProvider() {
        return warrantyProvider;
    }

    public void setWarrantyProvider(String warrantyProvider) {
        this.warrantyProvider = warrantyProvider;
    }

    public String getWarrantyStatus() {
        return warrantyStatus;
    }

    public void setWarrantyStatus(String warrantyStatus) {
        this.warrantyStatus = warrantyStatus;
    }

    public String getWarrantyTerms() {
        return warrantyTerms;
    }

    public void setWarrantyTerms(String warrantyTerms) {
        this.warrantyTerms = warrantyTerms;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }
}

