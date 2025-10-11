package com.example.asset.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "asset_amc")
public class AssetAmc extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long amcId;

    @ManyToOne @JoinColumn(name="asset_id")
    private AssetMaster asset;

    @ManyToOne @JoinColumn(name="component_id")
    private AssetComponent component;

    private Date startDate;
    private Date endDate;
    private String amcStatus;
    private String documentPath;
    private String userId;
    private String username;

    public Long getAmcId(){ return amcId; }
    public void setAmcId(Long amcId){ this.amcId = amcId; }
    public AssetMaster getAsset(){ return asset; }
    public void setAsset(AssetMaster asset){ this.asset = asset; }
    public AssetComponent getComponent(){ return component; }
    public void setComponent(AssetComponent component){ this.component = component; }
    public Date getStartDate(){ return startDate; }
    public void setStartDate(Date startDate){ this.startDate = startDate; }
    public Date getEndDate(){ return endDate; }
    public void setEndDate(Date endDate){ this.endDate = endDate; }
    public String getAmcStatus(){ return amcStatus; }
    public void setAmcStatus(String amcStatus){ this.amcStatus = amcStatus; }
    public String getDocumentPath(){ return documentPath; }
    public void setDocumentPath(String documentPath){ this.documentPath = documentPath; }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getUsername(){ return username; }
    public void setUsername(String username){ this.username = username; }
}
