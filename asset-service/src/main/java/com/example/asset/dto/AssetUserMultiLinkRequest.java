
package com.example.asset.dto;

import java.util.List;

/**
 * Multi-Entity Link Request DTO.
 * 
 * ➤ Backward-compatible with existing format:
 *      ["ASSET:1001", "COMPONENT:5001", "WARRANTY:9001"]
 *
 * ➤ Adds typed fields used by controller/service:
 *      assetId, componentId, modelId, makeId, amcId, warrantyId, documentId
 *
 * NO BREAKING CHANGES TO EXISTING FUNCTIONALITY.
 */
public class AssetUserMultiLinkRequest {

    // ============================================================
    // CALLER INFO
    // ============================================================
    private Long userId;      // caller userId
    private String username;    // caller username

    // ============================================================
    // TARGET USER INFO
    // ============================================================
    private Long targetUserId;
    private String targetUsername;

    // ============================================================
    // ORIGINAL OLD FORMAT (STILL SUPPORTED)
    // ============================================================
    // Example: ["ASSET:1001", "COMPONENT:501", "WARRANTY:901"]
    private List<String> entityLinks;

    // ============================================================
    // NEW TYPED FIELDS (NON-BREAKING)
    // ============================================================
    private Long assetId;
    private Long componentId;
    private Long modelId;
    private Long makeId;
    private Long amcId;
    private Long warrantyId;
    private Long documentId;

    // ============================================================
    // GETTERS & SETTERS (BACKWARD COMPATIBLE)
    // ============================================================

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }

    public List<String> getEntityLinks() { return entityLinks; }

    public void setEntityLinks(List<String> entityLinks) {
        this.entityLinks = entityLinks;
        parseEntityLinks();   // safely parse to typed fields
    }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }

    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }

    public Long getMakeId() { return makeId; }
    public void setMakeId(Long makeId) { this.makeId = makeId; }

    public Long getAmcId() { return amcId; }
    public void setAmcId(Long amcId) { this.amcId = amcId; }

    public Long getWarrantyId() { return warrantyId; }
    public void setWarrantyId(Long warrantyId) { this.warrantyId = warrantyId; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    // ============================================================
    // INTERNAL PARSER — AUTO MAPS STRINGS LIKE "ASSET:1001"
    // ============================================================
    private void parseEntityLinks() {
        if (entityLinks == null || entityLinks.isEmpty())
            return;

        for (String entry : entityLinks) {
            if (entry == null || !entry.contains(":"))
                continue;

            String[] parts = entry.split(":");
            if (parts.length != 2) continue;

            String type = parts[0].trim().toUpperCase();
            Long id = safeParseLong(parts[1]);

            if (id == null) continue;

            switch (type) {
                case "ASSET"      -> this.assetId = id;
                case "COMPONENT"  -> this.componentId = id;
                case "MODEL"      -> this.modelId = id;
                case "MAKE"       -> this.makeId = id;
                case "AMC"        -> this.amcId = id;
                case "WARRANTY"   -> this.warrantyId = id;
                case "DOCUMENT"   -> this.documentId = id;
                default           -> { /* ignore unknown types */ }
            }
        }
    }

    private Long safeParseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "AssetUserMultiLinkRequest{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", targetUserId=" + targetUserId +
                ", targetUsername='" + targetUsername + '\'' +
                ", entityLinks=" + entityLinks +
                ", assetId=" + assetId +
                ", componentId=" + componentId +
                ", modelId=" + modelId +
                ", makeId=" + makeId +
                ", amcId=" + amcId +
                ", warrantyId=" + warrantyId +
                ", documentId=" + documentId +
                '}';
    }
}




