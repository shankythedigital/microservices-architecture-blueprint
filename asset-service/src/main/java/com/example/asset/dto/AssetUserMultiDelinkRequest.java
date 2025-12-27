
package com.example.asset.dto;

import java.util.List;

/**
 * Multi-Entity Delink Request DTO.
 *
 * ➤ Backward-compatible with the existing string list format:
 *      ["ASSET:1001", "COMPONENT:501", "DOCUMENT:701"]
 *
 * ➤ Adds typed fields:
 *      assetId, componentId, modelId, makeId, amcId, warrantyId, documentId
 *
 * NO IMPACT TO EXISTING FUNCTIONALITY.
 */
public class AssetUserMultiDelinkRequest {

    // ============================================================
    // CALLER DETAILS
    // ============================================================
    private Long userId;        // logged-in user performing delink
    private String username;      // caller name

    // ============================================================
    // TARGET USER DETAILS
    // ============================================================
    private Long targetUserId;      // user to be unassigned
    private String targetUsername;  // target username

    // ============================================================
    // ORIGINAL FORMAT (STILL SUPPORTED)
    // ============================================================
    private List<String> entityLinks;
    // Example: ["ASSET:1001", "COMPONENT:501", "DOCUMENT:701"]

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
    // GETTERS + SETTERS
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
        parseEntityLinks();  // auto map string entries → typed fields
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
    // INTERNAL PARSER: Converts "TYPE:ID" → typed fields
    // ============================================================
    private void parseEntityLinks() {
        if (entityLinks == null || entityLinks.isEmpty()) return;

        for (String entry : entityLinks) {
            if (entry == null || !entry.contains(":")) continue;

            String[] parts = entry.split(":");
            if (parts.length != 2) continue;

            String type = parts[0].trim().toUpperCase();
            Long id = safeParse(parts[1]);

            if (id == null) continue;

            switch (type) {
                case "ASSET"      -> this.assetId = id;
                case "COMPONENT"  -> this.componentId = id;
                case "MODEL"      -> this.modelId = id;
                case "MAKE"       -> this.makeId = id;
                case "AMC"        -> this.amcId = id;
                case "WARRANTY"   -> this.warrantyId = id;
                case "DOCUMENT"   -> this.documentId = id;
                default           -> { /* ignore */ }
            }
        }
    }

    private Long safeParse(String input) {
        try {
            return Long.parseLong(input.trim());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "AssetUserMultiDelinkRequest{" +
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


