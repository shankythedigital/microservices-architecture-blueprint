package com.example.asset.dto;

import com.example.common.jpa.BaseEntity;
import java.io.Serializable;

/**
 * ✅ VendorDto
 * Data Transfer Object for {@link com.example.asset.entity.VendorMaster}.
 * 
 * Used for safely transferring vendor data between layers
 * (controller ↔ service ↔ client) without exposing entity internals.
 * Includes all optional fields for complete JSON responses.
 */
public class VendorDto extends BaseEntity implements Serializable {

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    private Long vendorId;
    private String vendorName;

    // ============================================================
    // 📦 Optional Fields
    // ============================================================
    private String contactPerson; // Optional
    private String email; // Optional
    private String mobile; // Optional
    private String address; // Optional

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
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

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // ============================================================
    // 🧠 toString() for Logging
    // ============================================================
    @Override
    public String toString() {
        return "VendorDto{" +
                "vendorId=" + vendorId +
                ", vendorName='" + vendorName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", address='" + address + '\'' +
                ", active=" + getActive() +
                ", createdBy='" + getCreatedBy() + '\'' +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}

