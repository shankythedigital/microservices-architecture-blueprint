
package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * ✅ VendorMaster Entity
 * Represents a vendor or supplier associated with asset purchases.
 * A vendor can have multiple outlets.
 */
@Entity
@Table(
    name = "vendor_master",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"vendor_name"})
    }
)
public class VendorMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendorId;

    @Column(name = "vendor_name", nullable = false, length = 150)
    private String vendorName;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(length = 100)
    private String email;

    @Column(length = 15)
    private String mobile;

    @Column(length = 255)
    private String address;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_favourite")
    private Boolean isFavourite = false;

    @Column(name = "is_most_like")
    private Boolean isMostLike = false;

    // ✅ One vendor can have multiple outlets
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PurchaseOutlet> outlets = new HashSet<>();

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public VendorMaster() {}

    public VendorMaster(String vendorName, String contactPerson, String email, String mobile, String address) {
        this.vendorName = vendorName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.mobile = mobile;
        this.address = address;
    }

    // ============================================================
    // ✅ Getters and Setters
    // ============================================================
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
    public Boolean getIsFavourite() { return isFavourite; }
    public void setIsFavourite(Boolean isFavourite) { this.isFavourite = isFavourite; }
    public Boolean getIsMostLike() { return isMostLike; }
    public void setIsMostLike(Boolean isMostLike) { this.isMostLike = isMostLike; }

    public Set<PurchaseOutlet> getOutlets() { return outlets; }
    public void setOutlets(Set<PurchaseOutlet> outlets) { this.outlets = outlets; }

    // ============================================================
    // ✅ Convenience
    // ============================================================
    @Override
    public String toString() {
        return "VendorMaster{" +
                "vendorId=" + vendorId +
                ", vendorName='" + vendorName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}


