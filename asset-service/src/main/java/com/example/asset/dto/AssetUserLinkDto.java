package com.example.asset.dto;

import com.example.common.util.PiiMaskingUtil;
import java.time.LocalDateTime;

/**
 * ✅ AssetUserLinkDto
 * Data Transfer Object for AssetUserLink responses.
 * 🔐 DPDPA Compliance: All PII fields (username, email, mobile) are automatically masked.
 */
public class AssetUserLinkDto {

    private Long linkId;
    private Long assetId;
    private Long componentId;
    private Long userId;
    
    // 🔐 DPDPA Compliance: Masked PII fields
    private String username;
    private String email;
    private String mobile;
    
    private LocalDateTime assignedDate;
    private LocalDateTime unassignedDate;
    private Integer sequenceOrder;
    private Boolean isFavourite;
    private Boolean isMostLike;
    private Boolean active;
    
    // Internal storage for unmasked values
    private transient String usernameUnmasked;
    private transient String emailUnmasked;
    private transient String mobileUnmasked;

    // Getters with masking
    public Long getLinkId() { return linkId; }
    public void setLinkId(Long linkId) { this.linkId = linkId; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    /**
     * 🔐 DPDPA Compliance: Returns masked username
     */
    public String getUsername() {
        if (usernameUnmasked != null) {
            return PiiMaskingUtil.maskUsername(usernameUnmasked);
        }
        return username != null ? PiiMaskingUtil.maskUsername(username) : null;
    }
    
    public void setUsername(String username) {
        this.usernameUnmasked = username;
        this.username = username;
    }

    /**
     * 🔐 DPDPA Compliance: Returns masked email
     */
    public String getEmail() {
        if (emailUnmasked != null) {
            return PiiMaskingUtil.maskEmail(emailUnmasked);
        }
        return email != null ? PiiMaskingUtil.maskEmail(email) : null;
    }
    
    public void setEmail(String email) {
        this.emailUnmasked = email;
        this.email = email;
    }

    /**
     * 🔐 DPDPA Compliance: Returns masked mobile
     */
    public String getMobile() {
        if (mobileUnmasked != null) {
            return PiiMaskingUtil.maskMobile(mobileUnmasked);
        }
        return mobile != null ? PiiMaskingUtil.maskMobile(mobile) : null;
    }
    
    public void setMobile(String mobile) {
        this.mobileUnmasked = mobile;
        this.mobile = mobile;
    }

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

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

