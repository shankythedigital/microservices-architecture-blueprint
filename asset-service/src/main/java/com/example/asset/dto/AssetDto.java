
package com.example.asset.dto;

import java.util.Set;

/**
 * ✅ AssetDto
 * Data Transfer Object for Asset information including metadata,
 * purchase details, and assigned user context.
 */
public class AssetDto {

    private String assetNameUdv;
    private Long categoryId;
    private Long subCategoryId;
    private Long makeId;
    private Long modelId;
    private String makeUdv;
    private String modelUdv;
    private String purchaseMode;
    private Long purchaseOutletId;
    private String purchaseOutletUdv;
    private String purchaseOutletAddressUdv;
    private String purchaseDate; // yyyy-MM-dd
    private String assetStatus;
    private String soldOnDate;
    private String salesChannelName;
    private Set<Long> componentIds;
    private String userId;      // to assign
    private String username;    // to assign
    private String projecttype; // to assign

    // ----- Getters -----
    public String getAssetNameUdv() {
        return assetNameUdv;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public Long getMakeId() {
        return makeId;
    }

    public Long getModelId() {
        return modelId;
    }

    public String getMakeUdv() {
        return makeUdv;
    }

    public String getModelUdv() {
        return modelUdv;
    }

    public String getPurchaseMode() {
        return purchaseMode;
    }

    public Long getPurchaseOutletId() {
        return purchaseOutletId;
    }

    public String getPurchaseOutletUdv() {
        return purchaseOutletUdv;
    }

    public String getPurchaseOutletAddressUdv() {
        return purchaseOutletAddressUdv;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public String getSoldOnDate() {
        return soldOnDate;
    }

    public String getSalesChannelName() {
        return salesChannelName;
    }

    public Set<Long> getComponentIds() {
        return componentIds;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getProjecttype() {
        return projecttype;
    }

    // ----- Setters -----
    public void setAssetNameUdv(String assetNameUdv) {
        this.assetNameUdv = assetNameUdv;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public void setMakeId(Long makeId) {
        this.makeId = makeId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public void setMakeUdv(String makeUdv) {
        this.makeUdv = makeUdv;
    }

    public void setModelUdv(String modelUdv) {
        this.modelUdv = modelUdv;
    }

    public void setPurchaseMode(String purchaseMode) {
        this.purchaseMode = purchaseMode;
    }

    public void setPurchaseOutletId(Long purchaseOutletId) {
        this.purchaseOutletId = purchaseOutletId;
    }

    public void setPurchaseOutletUdv(String purchaseOutletUdv) {
        this.purchaseOutletUdv = purchaseOutletUdv;
    }

    public void setPurchaseOutletAddressUdv(String purchaseOutletAddressUdv) {
        this.purchaseOutletAddressUdv = purchaseOutletAddressUdv;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    public void setSoldOnDate(String soldOnDate) {
        this.soldOnDate = soldOnDate;
    }

    public void setSalesChannelName(String salesChannelName) {
        this.salesChannelName = salesChannelName;
    }

    public void setComponentIds(Set<Long> componentIds) {
        this.componentIds = componentIds;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProjecttype(String projecttype) {
        this.projecttype = projecttype;
    }
}


