package com.example.asset.dto;

/**
 * DTO used for:
 * - Linking asset/component to user
 * - Delinking asset/component from user
 *
 * Matches JSON request format:
 *
 * {
 *   "userId": 10,
 *   "username": "admin",
 *   "link": {
 *     "asset": {
 *       "assetId": 100,
 *       "componentId": 200,
 *       "assetuserId": 55,
 *       "assetusername": "john"
 *     }
 *   }
 * }
 */
public class AssetUserLinkRequest {

    private String userId;         // API caller userId (createdBy / updatedBy)
    private String username;     // API caller username

    private Link link;           // Asset link wrapper

    // ============================================================
    // Nested DTOs
    // ============================================================

    public static class Link {
        private Asset asset;

        public Asset getAsset() {
            return asset;
        }

        public void setAsset(Asset asset) {
            this.asset = asset;
        }
    }

    public static class Asset {

        private Long assetId;         // main asset id (nullable if componentId used)
        private Long componentId;     // component id (nullable if assetId used)

        private Long assetuserId;     // user who will receive the asset/component
        private String assetusername; // user name who will receive the asset/component

        public Long getAssetId() {
            return assetId;
        }

        public void setAssetId(Long assetId) {
            this.assetId = assetId;
        }

        public Long getComponentId() {
            return componentId;
        }

        public void setComponentId(Long componentId) {
            this.componentId = componentId;
        }

        public Long getAssetuserId() {
            return assetuserId;
        }

        public void setAssetuserId(Long assetuserId) {
            this.assetuserId = assetuserId;
        }

        public String getAssetusername() {
            return assetusername;
        }

        public void setAssetusername(String assetusername) {
            this.assetusername = assetusername;
        }
    }

    // ============================================================
    // Getters & Setters
    // ============================================================

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }
}



