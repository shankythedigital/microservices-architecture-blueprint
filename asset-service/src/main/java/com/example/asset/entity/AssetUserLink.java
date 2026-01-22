package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AssetUserLink — assignment record that links asset or component to a user.
 * - Uses asset_id / component_id columns (nullable for component-only or asset-only).
 * - Keeps audit via BaseEntity (createdBy, createdAt, updatedBy, updatedAt, active).
 */
@Entity
@Table(name = "asset_user_link")
public class AssetUserLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "asset_id")
    private Long assetId;

    @Column(name = "component_id")
    private Long componentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false, length = 255)
    private String username;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "mobile", length = 15)
    private String mobile;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate = LocalDateTime.now();

    @Column(name = "unassigned_date")
    private LocalDateTime unassignedDate;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_favourite")
    private Boolean isFavourite = false;

    @Column(name = "is_most_like")
    private Boolean isMostLike = false;

    // ---------- Relationship back to AssetMaster (many links → one asset)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", insertable = false, updatable = false)
    @JsonIgnore
    private AssetMaster asset;

    // --------------------
    // Getters / Setters
    // --------------------
    public Long getLinkId() { return linkId; }
    public void setLinkId(Long linkId) { this.linkId = linkId; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public LocalDateTime getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }

    public LocalDateTime getUnassignedDate() { return unassignedDate; }
    public void setUnassignedDate(LocalDateTime unassignedDate) { this.unassignedDate = unassignedDate; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
    public Boolean getIsFavourite() { return isFavourite; }
    public void setIsFavourite(Boolean isFavourite) { this.isFavourite = isFavourite; }
    public Boolean getIsMostLike() { return isMostLike; }
    public void setIsMostLike(Boolean isMostLike) { this.isMostLike = isMostLike; }

    public AssetMaster getAsset() { return asset; }
    public void setAsset(AssetMaster asset) { this.asset = asset; }

    @Override
    public String toString() {
        return "AssetUserLink{" +
                "linkId=" + linkId +
                ", assetId=" + assetId +
                ", componentId=" + componentId +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", assignedDate=" + assignedDate +
                ", unassignedDate=" + unassignedDate +
                ", active=" + getActive() +
                ", createdBy='" + getCreatedBy() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedBy='" + getUpdatedBy() + '\'' +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}

