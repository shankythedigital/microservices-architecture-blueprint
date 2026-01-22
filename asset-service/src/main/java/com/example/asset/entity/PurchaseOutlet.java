package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

/**
 * ✅ PurchaseOutlet Entity
 * Represents a physical or online store/vendor from which assets can be purchased or serviced.
 *
 * Features:
 *  - Unique outlet name constraint
 *  - Linked with optional VendorMaster (for supplier/vendor management)
 *  - Extends BaseEntity for auditing and active flag handling
 */
@Entity
@Table(
    name = "purchase_outlet",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"outlet_name"})
    }
)
public class PurchaseOutlet extends BaseEntity {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outlet_id")
    private Long outletId;

    // ============================================================
    // 🏷️ Outlet Details
    // ============================================================
    @Column(name = "outlet_name", nullable = false, length = 150, unique = true)
    private String outletName;

    @Column(name = "outlet_address", length = 255)
    private String outletAddress;

    @Column(name = "contact_info", length = 100)
    private String contactInfo;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_favourite")
    private Boolean isFavourite = false;

    @Column(name = "is_most_like")
    private Boolean isMostLike = false;

    // ============================================================
    // 🧩 Optional Vendor Relationship
    // ============================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private VendorMaster vendor;

    // ============================================================
    // 🧩 Constructors
    // ============================================================
    public PurchaseOutlet() {}

    public PurchaseOutlet(String outletName, String outletAddress, String contactInfo) {
        this.outletName = outletName;
        this.outletAddress = outletAddress;
        this.contactInfo = contactInfo;
    }

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

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public Boolean getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(Boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    public Boolean getIsMostLike() {
        return isMostLike;
    }

    public void setIsMostLike(Boolean isMostLike) {
        this.isMostLike = isMostLike;
    }

    public VendorMaster getVendor() {
        return vendor;
    }

    public void setVendor(VendorMaster vendor) {
        this.vendor = vendor;
    }

    // ============================================================
    // 🧠 toString (For Logging)
    // ============================================================
    @Override
    public String toString() {
        return "PurchaseOutlet{" +
                "outletId=" + outletId +
                ", outletName='" + outletName + '\'' +
                ", outletAddress='" + outletAddress + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", active=" + getActive() +
                '}';
    }
}


