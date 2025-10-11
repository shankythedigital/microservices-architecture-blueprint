package com.example.asset.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "asset_warranty")
public class AssetWarranty extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long warrantyId;

    @ManyToOne @JoinColumn(name="asset_id")
    private AssetMaster asset;

    @ManyToOne @JoinColumn(name="component_id")
    private AssetComponent component;

    private String warrantyType;
    private Date startDate;
    private Date endDate;
    private String documentPath;
    private String userId;
    private String username;

    public Long getWarrantyId(){ return warrantyId; }
    public void setWarrantyId(Long warrantyId){ this.warrantyId = warrantyId; }
    public AssetMaster getAsset(){ return asset; }
    public void setAsset(AssetMaster asset){ this.asset = asset; }
    public AssetComponent getComponent(){ return component; }
    public void setComponent(AssetComponent component){ this.component = component; }
    public String getWarrantyType(){ return warrantyType; }
    public void setWarrantyType(String warrantyType){ this.warrantyType = warrantyType; }
    public Date getStartDate(){ return startDate; }
    public void setStartDate(Date startDate){ this.startDate = startDate; }
    public Date getEndDate(){ return endDate; }
    public void setEndDate(Date endDate){ this.endDate = endDate; }
    public String getDocumentPath(){ return documentPath; }
    public void setDocumentPath(String documentPath){ this.documentPath = documentPath; }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getUsername(){ return username; }
    public void setUsername(String username){ this.username = username; }
}
