package com.example.asset.dto;

import com.example.common.jpa.BaseEntity;
import java.io.Serializable;

/**
 * ✅ OutletDto
 * Data Transfer Object for {@link com.example.asset.entity.PurchaseOutlet}.
 * 
 * Used for safely transferring outlet data between layers
 * (controller ↔ service ↔ client) without exposing entity internals.
 * Includes all optional fields for complete JSON responses.
 */
public class OutletDto extends BaseEntity implements Serializable {

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    private Long outletId;
    private String outletName;

    // ============================================================
    // 📦 Optional Fields
    // ============================================================
    private String outletAddress; // Optional
    private String contactInfo; // Optional

    // ============================================================
    // 🔗 Foreign Key Fields (Optional)
    // ============================================================
    private Long vendorId;
    private String vendorName;

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getOutletId() {
        return outletId;
    }

    public void setOutletId(Long outletId) {
        this.outletId = outletId;
    }

    public String getOutletName() {
        return outletName;
    }

    public void setOutletName(String outletName) {
        this.outletName = outletName;
    }

    public String getOutletAddress() {
        return outletAddress;
    }

    public void setOutletAddress(String outletAddress) {
        this.outletAddress = outletAddress;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    // ============================================================
    // 🧠 toString() for Logging
    // ============================================================
    @Override
    public String toString() {
        return "OutletDto{" +
                "outletId=" + outletId +
                ", outletName='" + outletName + '\'' +
                ", outletAddress='" + outletAddress + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", vendorId=" + vendorId +
                ", vendorName='" + vendorName + '\'' +
                ", active=" + getActive() +
                ", createdBy='" + getCreatedBy() + '\'' +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}

