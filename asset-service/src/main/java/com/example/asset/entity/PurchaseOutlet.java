package com.example.asset.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "purchase_outlet")
public class PurchaseOutlet extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outletId;
    private String outletName;
    private String outletAddress;
    private String contactInfo;
    private String outletType;

    public PurchaseOutlet(){}
    public PurchaseOutlet(String outletName, String outletAddress, String contactInfo, String outletType){
        this.outletName = outletName; this.outletAddress = outletAddress; this.contactInfo = contactInfo; this.outletType = outletType;
    }

    public Long getOutletId(){ return outletId; }
    public void setOutletId(Long outletId){ this.outletId = outletId; }
    public String getOutletName(){ return outletName; }
    public void setOutletName(String outletName){ this.outletName = outletName; }
    public String getOutletAddress(){ return outletAddress; }
    public void setOutletAddress(String outletAddress){ this.outletAddress = outletAddress; }
    public String getContactInfo(){ return contactInfo; }
    public void setContactInfo(String contactInfo){ this.contactInfo = contactInfo; }
    public String getOutletType(){ return outletType; }
    public void setOutletType(String outletType){ this.outletType = outletType; }
}
