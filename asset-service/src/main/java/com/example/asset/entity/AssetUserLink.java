package com.example.asset.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_user_link")
public class AssetUserLink extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkId;

    @ManyToOne @JoinColumn(name="asset_id")
    private AssetMaster asset;

    private String userId;
    private String username;
    private LocalDateTime assignedDate = LocalDateTime.now();

    public Long getLinkId(){ return linkId; }
    public void setLinkId(Long linkId){ this.linkId = linkId; }
    public AssetMaster getAsset(){ return asset; }
    public void setAsset(AssetMaster asset){ this.asset = asset; }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getUsername(){ return username; }
    public void setUsername(String username){ this.username = username; }
    public LocalDateTime getAssignedDate(){ return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate){ this.assignedDate = assignedDate; }
}
