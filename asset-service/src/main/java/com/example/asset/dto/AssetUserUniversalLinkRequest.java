

package com.example.asset.dto;

public class AssetUserUniversalLinkRequest {

    private Long userId;              // caller
    private String username;            // caller name

    private String entityType;          // ASSET, COMPONENT, MODEL, MAKE, AMC, WARRANTY, DOCUMENT
    private Long entityId;              // ID of the entity

    private Long targetUserId;          // user we want to link to entity
    private String targetUsername;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }

    @Override
    public String toString() {
        return "AssetUserUniversalLinkRequest{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", targetUserId=" + targetUserId +
                ", targetUsername='" + targetUsername + '\'' +
                '}';
    }
}

