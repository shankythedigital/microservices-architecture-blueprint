
package com.example.asset.service;

import com.example.asset.dto.AssetUserMultiDelinkRequest;
import com.example.asset.dto.AssetUserMultiLinkRequest;
import com.example.asset.entity.AssetMaster;
import com.example.asset.entity.AssetUserLink;
import com.example.asset.repository.*;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Conservative, non-breaking UserLinkService implementation.
 * Supports:
 *  - linkEntity / delinkEntity (unified)
 *  - linkMultipleEntities / delinkMultipleEntities
 *  - getAssetsAssignedToUser
 *  - getSingleAsset
 *  - getUsersBySubCategory
 */
@Service
public class UserLinkService {

    private static final Logger log = LoggerFactory.getLogger(UserLinkService.class);

    private final AssetUserLinkRepository linkRepo;
    private final AssetMasterRepository assetRepo;
    private final AssetComponentRepository componentRepo;
    private final ProductModelRepository modelRepo;
    private final ProductMakeRepository makeRepo;
    private final AssetWarrantyRepository warrantyRepo;
    private final AssetAmcRepository amcRepo;
    private final AssetDocumentRepository documentRepo;
    private final SafeNotificationHelper safeNotificationHelper;

    public UserLinkService(
            AssetUserLinkRepository linkRepo,
            AssetMasterRepository assetRepo,
            AssetComponentRepository componentRepo,
            ProductModelRepository modelRepo,
            ProductMakeRepository makeRepo,
            AssetWarrantyRepository warrantyRepo,
            AssetAmcRepository amcRepo,
            AssetDocumentRepository documentRepo,
            SafeNotificationHelper safeNotificationHelper) {

        this.linkRepo = linkRepo;
        this.assetRepo = assetRepo;
        this.componentRepo = componentRepo;
        this.modelRepo = modelRepo;
        this.makeRepo = makeRepo;
        this.warrantyRepo = warrantyRepo;
        this.amcRepo = amcRepo;
        this.documentRepo = documentRepo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // -------------------------------------------------------------------------
    // Unified single-entity link
    // returns "already exist" if already linked to same user, "linked" when linked
    // -------------------------------------------------------------------------
    public String linkEntity(String bearer,
                             String entityType,
                             Long entityId,
                             Long targetUserId,
                             String targetUsername,
                             Long createdByUserId,
                             String createdByUserName) {

        log.info("linkEntity: type={}, id={}, toUser={}, by={}", entityType, entityId, targetUsername, createdByUserName);
        String et = entityType == null ? "" : entityType.trim().toUpperCase();

        switch (et) {
            case "ASSET":
                return linkAsset(entityId, targetUserId, targetUsername, createdByUserId, createdByUserName, bearer);
            case "COMPONENT":
                return linkComponent(entityId, targetUserId, targetUsername, createdByUserId, createdByUserName, bearer);
            case "MODEL":
                return linkModel(entityId, targetUserId, targetUsername, createdByUserId, createdByUserName, bearer);
            case "MAKE":
                return linkMake(entityId, targetUserId, targetUsername, createdByUserId, createdByUserName, bearer);
            case "AMC":
            case "WARRANTY":
            case "DOCUMENT":
                // treat as asset-level link via asset referenced from these entities
                return linkViaAssetFromRelatedEntity(et, entityId, targetUserId, targetUsername, createdByUserId, createdByUserName, bearer);
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    // -------------------------------------------------------------------------
    // Unified single-entity delink
    // -------------------------------------------------------------------------
    public String delinkEntity(String bearer,
                               String entityType,
                               Long entityId,
                               Long targetUserId,
                               String targetUsername,
                               Long updatedByUserId,
                               String updatedByUserName) {

        log.info("delinkEntity: type={}, id={}, user={}, by={}", entityType, entityId, targetUserId, updatedByUserName);
        String et = entityType == null ? "" : entityType.trim().toUpperCase();

        switch (et) {
            case "ASSET":
                return delinkAsset(entityId, targetUserId, updatedByUserName, updatedByUserId, bearer);
            case "COMPONENT":
                return delinkComponent(entityId, targetUserId, updatedByUserName, updatedByUserId, bearer);
            case "MODEL":
                return delinkModel(entityId, targetUserId, updatedByUserName, updatedByUserId, bearer);
            case "MAKE":
                return delinkMake(entityId, targetUserId, updatedByUserName, updatedByUserId, bearer);
            case "AMC":
            case "WARRANTY":
            case "DOCUMENT":
                return delinkViaAssetFromRelatedEntity(et, entityId, targetUserId, updatedByUserName, updatedByUserId, bearer);
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    // -------------------------------------------------------------------------
    // Multi-entity helpers (controller passes validationService in)
    // request DTO shape assumed — we build safe map results
    // -------------------------------------------------------------------------
    public Map<String, Object> linkMultipleEntities(String bearer,
                                                    AssetUserMultiLinkRequest request,
                                                    com.example.asset.service.ValidationService validationService) {

        Map<String, Object> result = new LinkedHashMap<>();
        // for each provided entity in request attempt link (if present)
        if (request.getAssetId() != null) {
            try {
                validationService.validateLinkRequestSingle("ASSET", request.getAssetId(), request.getTargetUserId(), request.getTargetUsername());
                String msg = linkEntity(bearer, "ASSET", request.getAssetId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("ASSET", msg);
            } catch (Exception e) {
                result.put("ASSET", "ERROR: " + e.getMessage());
            }
        }
        if (request.getComponentId() != null) {
            try {
                validationService.validateLinkRequestSingle("COMPONENT", request.getComponentId(), request.getTargetUserId(), request.getTargetUsername());
                String msg = linkEntity(bearer, "COMPONENT", request.getComponentId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("COMPONENT", msg);
            } catch (Exception e) {
                result.put("COMPONENT", "ERROR: " + e.getMessage());
            }
        }
        if (request.getModelId() != null) {
            try {
                validationService.validateLinkRequestSingle("MODEL", request.getModelId(), request.getTargetUserId(), request.getTargetUsername());
                String msg = linkEntity(bearer, "MODEL", request.getModelId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("MODEL", msg);
            } catch (Exception e) {
                result.put("MODEL", "ERROR: " + e.getMessage());
            }
        }
        if (request.getMakeId() != null) {
            try {
                validationService.validateLinkRequestSingle("MAKE", request.getMakeId(), request.getTargetUserId(), request.getTargetUsername());
                String msg = linkEntity(bearer, "MAKE", request.getMakeId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("MAKE", msg);
            } catch (Exception e) {
                result.put("MAKE", "ERROR: " + e.getMessage());
            }
        }
        if (request.getAmcId() != null) {
            try {
                validationService.validateLinkRequestSingle("AMC", request.getAmcId(), request.getTargetUserId(), request.getTargetUsername());
                String msg = linkEntity(bearer, "AMC", request.getAmcId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("AMC", msg);
            } catch (Exception e) {
                result.put("AMC", "ERROR: " + e.getMessage());
            }
        }
        if (request.getWarrantyId() != null) {
            try {
                validationService.validateLinkRequestSingle("WARRANTY", request.getWarrantyId(), request.getTargetUserId(), request.getTargetUsername());
                String msg = linkEntity(bearer, "WARRANTY", request.getWarrantyId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("WARRANTY", msg);
            } catch (Exception e) {
                result.put("WARRANTY", "ERROR: " + e.getMessage());
            }
        }
        if (request.getDocumentId() != null) {
            try {
                validationService.validateLinkRequestSingle("DOCUMENT", request.getDocumentId(), request.getTargetUserId(), request.getTargetUsername());
                String msg = linkEntity(bearer, "DOCUMENT", request.getDocumentId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("DOCUMENT", msg);
            } catch (Exception e) {
                result.put("DOCUMENT", "ERROR: " + e.getMessage());
            }
        }

        return result;
    }

    public Map<String, Object> delinkMultipleEntities(String bearer,
                                                      AssetUserMultiDelinkRequest request,
                                                      com.example.asset.service.ValidationService validationService) {

        Map<String, Object> result = new LinkedHashMap<>();

        if (request.getAssetId() != null) {
            try {
                validationService.validateDelinkRequestSingle("ASSET", request.getAssetId(), request.getTargetUserId());
                String msg = delinkEntity(bearer, "ASSET", request.getAssetId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("ASSET", msg);
            } catch (Exception e) {
                result.put("ASSET", "ERROR: " + e.getMessage());
            }
        }

        if (request.getComponentId() != null) {
            try {
                validationService.validateDelinkRequestSingle("COMPONENT", request.getComponentId(), request.getTargetUserId());
                String msg = delinkEntity(bearer, "COMPONENT", request.getComponentId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("COMPONENT", msg);
            } catch (Exception e) {
                result.put("COMPONENT", "ERROR: " + e.getMessage());
            }
        }

        if (request.getModelId() != null) {
            try {
                validationService.validateDelinkRequestSingle("MODEL", request.getModelId(), request.getTargetUserId());
                String msg = delinkEntity(bearer, "MODEL", request.getModelId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("MODEL", msg);
            } catch (Exception e) {
                result.put("MODEL", "ERROR: " + e.getMessage());
            }
        }

        if (request.getMakeId() != null) {
            try {
                validationService.validateDelinkRequestSingle("MAKE", request.getMakeId(), request.getTargetUserId());
                String msg = delinkEntity(bearer, "MAKE", request.getMakeId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("MAKE", msg);
            } catch (Exception e) {
                result.put("MAKE", "ERROR: " + e.getMessage());
            }
        }

        if (request.getAmcId() != null) {
            try {
                validationService.validateDelinkRequestSingle("AMC", request.getAmcId(), request.getTargetUserId());
                String msg = delinkEntity(bearer, "AMC", request.getAmcId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("AMC", msg);
            } catch (Exception e) {
                result.put("AMC", "ERROR: " + e.getMessage());
            }
        }

        if (request.getWarrantyId() != null) {
            try {
                validationService.validateDelinkRequestSingle("WARRANTY", request.getWarrantyId(), request.getTargetUserId());
                String msg = delinkEntity(bearer, "WARRANTY", request.getWarrantyId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("WARRANTY", msg);
            } catch (Exception e) {
                result.put("WARRANTY", "ERROR: " + e.getMessage());
            }
        }

        if (request.getDocumentId() != null) {
            try {
                validationService.validateDelinkRequestSingle("DOCUMENT", request.getDocumentId(), request.getTargetUserId());
                String msg = delinkEntity(bearer, "DOCUMENT", request.getDocumentId(), request.getTargetUserId(), request.getTargetUsername(), request.getUserId(), request.getUsername());
                result.put("DOCUMENT", msg);
            } catch (Exception e) {
                result.put("DOCUMENT", "ERROR: " + e.getMessage());
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // Asset/Component specific operations
    // -------------------------------------------------------------------------
    private String linkAsset(Long assetId, Long targetUserId, String targetUsername, Long createdByUserId, String createdByUserName, String bearer) {
        // if already linked to the same user -> return already exist
        if (linkRepo.findByAssetIdAndUserIdAndActiveTrue(assetId, targetUserId).isPresent()) {
            log.info("Asset {} already linked to user {}", assetId, targetUserId);
            return "already exist";
        }

        // if asset linked to another active user -> prevent
        if (linkRepo.existsByAssetIdAndActiveTrue(assetId) && !linkRepo.findByAssetIdAndUserIdAndActiveTrue(assetId, targetUserId).isPresent()) {
            throw new IllegalStateException("Asset is already assigned to another user");
        }

        AssetUserLink link = new AssetUserLink();
        link.setAssetId(assetId);
        link.setComponentId(null);
        link.setUserId(targetUserId);
        link.setUsername(targetUsername);
        link.setActive(true);
        link.setAssignedDate(LocalDateTime.now());
        link.setCreatedBy(createdByUserName);
        link.setCreatedAt(LocalDateTime.now());

        linkRepo.save(link);
        notifyAudit(bearer, "LINK_ASSET", createdByUserName, assetId, null);

        return "linked";
    }

    private String linkComponent(Long componentId, Long targetUserId, String targetUsername, Long createdByUserId, String createdByUserName, String bearer) {
        if (linkRepo.findByComponentIdAndUserIdAndActiveTrue(componentId, targetUserId).isPresent()) {
            log.info("Component {} already linked to user {}", componentId, targetUserId);
            return "already exist";
        }

        if (linkRepo.existsByComponentIdAndActiveTrue(componentId) && !linkRepo.findByComponentIdAndUserIdAndActiveTrue(componentId, targetUserId).isPresent()) {
            throw new IllegalStateException("Component is already assigned to another user");
        }

        AssetUserLink link = new AssetUserLink();
        link.setAssetId(null);
        link.setComponentId(componentId);
        link.setUserId(targetUserId);
        link.setUsername(targetUsername);
        link.setActive(true);
        link.setAssignedDate(LocalDateTime.now());
        link.setCreatedBy(createdByUserName);
        link.setCreatedAt(LocalDateTime.now());

        linkRepo.save(link);
        notifyAudit(bearer, "LINK_COMPONENT", createdByUserName, null, componentId);

        return "linked";
    }

    private String linkModel(Long modelId, Long targetUserId, String targetUsername, Long createdByUserId, String createdByUserName, String bearer) {
        List<AssetMaster> assets = assetRepo.findAll().stream()
                .filter(a -> a.getModel() != null && Objects.equals(a.getModel().getModelId(), modelId))
                .collect(Collectors.toList());

        if (assets.isEmpty()) throw new IllegalArgumentException("No assets found for model " + modelId);

        int linked = 0;
        int already = 0;
        for (AssetMaster a : assets) {
            if (linkRepo.findByAssetIdAndUserIdAndActiveTrue(a.getAssetId(), targetUserId).isPresent()) {
                already++;
                continue;
            }
            if (linkRepo.existsByAssetIdAndActiveTrue(a.getAssetId())) {
                // asset assigned to different user — skip and report
                continue;
            }
            AssetUserLink link = new AssetUserLink();
            link.setAssetId(a.getAssetId());
            link.setComponentId(null);
            link.setUserId(targetUserId);
            link.setUsername(targetUsername);
            link.setActive(true);
            link.setAssignedDate(LocalDateTime.now());
            link.setCreatedBy(createdByUserName);
            link.setCreatedAt(LocalDateTime.now());
            linkRepo.save(link);
            linked++;
        }
        notifyAudit(bearer, "LINK_MODEL", createdByUserName, null, null);
        return (linked == 0 && already > 0) ? "already exist" : ("linked:" + linked + ", already:" + already);
    }

    private String linkMake(Long makeId, Long targetUserId, String targetUsername, Long createdByUserId, String createdByUserName, String bearer) {
        List<AssetMaster> assets = assetRepo.findAll().stream()
                .filter(a -> a.getMake() != null && Objects.equals(a.getMake().getMakeId(), makeId))
                .collect(Collectors.toList());

        if (assets.isEmpty()) throw new IllegalArgumentException("No assets found for make " + makeId);

        int linked = 0;
        int already = 0;
        for (AssetMaster a : assets) {
            if (linkRepo.findByAssetIdAndUserIdAndActiveTrue(a.getAssetId(), targetUserId).isPresent()) {
                already++;
                continue;
            }
            if (linkRepo.existsByAssetIdAndActiveTrue(a.getAssetId())) {
                // already assigned to other user - skip
                continue;
            }
            AssetUserLink link = new AssetUserLink();
            link.setAssetId(a.getAssetId());
            link.setComponentId(null);
            link.setUserId(targetUserId);
            link.setUsername(targetUsername);
            link.setActive(true);
            link.setAssignedDate(LocalDateTime.now());
            link.setCreatedBy(createdByUserName);
            link.setCreatedAt(LocalDateTime.now());
            linkRepo.save(link);
            linked++;
        }
        notifyAudit(bearer, "LINK_MAKE", createdByUserName, null, null);
        return (linked == 0 && already > 0) ? "already exist" : ("linked:" + linked + ", already:" + already);
    }


    // ========================================================================
    // FIXED AMC/WARRANTY/DOCUMENT MAPPING (correct entity field)
    // ========================================================================
    private List<Long> resolveAssetIdsFromRelatedEntity(String type, Long id) {

        return switch (type) {

            case "AMC" -> amcRepo.findById(id)
                    .filter(a -> a.getAsset() != null)
                    .map(a -> List.of(a.getAsset().getAssetId()))
                    .orElse(List.of());

            case "WARRANTY" -> warrantyRepo.findById(id)
                    .filter(w -> w.getAsset() != null)
                    .map(w -> List.of(w.getAsset().getAssetId()))
                    .orElse(List.of());

            case "DOCUMENT" -> documentRepo.findById(id)
                    .filter(d -> d.getAsset() != null)
                    .map(d -> List.of(d.getAsset().getAssetId()))
                    .orElse(List.of());

            default -> List.of();
        };
    }

    private String linkViaAssetFromRelatedEntity(String et, Long id, Long targetUserId, String targetUsername, Long createdByUserId, String createdByUserName, String bearer) {
        // find corresponding asset and link it
        List<Long> assetIds = resolveAssetIdsFromRelatedEntity(et, id);

        if (assetIds.isEmpty()) throw new IllegalArgumentException("No asset mapping found for " + et + " id " + id);

        int linked = 0;
        int already = 0;
        for (Long aid : assetIds) {
            if (linkRepo.findByAssetIdAndUserIdAndActiveTrue(aid, targetUserId).isPresent()) {
                already++;
                continue;
            }
            if (linkRepo.existsByAssetIdAndActiveTrue(aid)) {
                continue; // assigned to other user
            }
            AssetUserLink link = new AssetUserLink();
            link.setAssetId(aid);
            link.setComponentId(null);
            link.setUserId(targetUserId);
            link.setUsername(targetUsername);
            link.setActive(true);
            link.setAssignedDate(LocalDateTime.now());
            link.setCreatedBy(createdByUserName);
            link.setCreatedAt(LocalDateTime.now());
            linkRepo.save(link);
            linked++;
        }
        notifyAudit(bearer, "LINK_" + et, createdByUserName, null, null);
        return (linked == 0 && already > 0) ? "already exist" : ("linked:" + linked + ", already:" + already);
    }

    // -------------------------------------------------------------------------
    // Delink implementations
    // -------------------------------------------------------------------------
    private String delinkAsset(Long assetId, Long targetUserId, String updatedBy, Long updatedById, String bearer) {
        Optional<AssetUserLink> opt = linkRepo.findByAssetIdAndUserIdAndActiveTrue(assetId, targetUserId);
        if (opt.isEmpty()) {
            throw new IllegalStateException("No active link exists for asset " + assetId + " and user " + targetUserId);
        }
        AssetUserLink link = opt.get();
        link.setActive(false);
        link.setUnassignedDate(LocalDateTime.now());
        link.setUpdatedBy(updatedBy);
        link.setUpdatedAt(LocalDateTime.now());
        linkRepo.save(link);
        notifyAudit(bearer, "DELINK_ASSET", updatedBy, assetId, null);
        return "delinked";
    }

    private String delinkComponent(Long componentId, Long targetUserId, String updatedBy, Long updatedById, String bearer) {
        Optional<AssetUserLink> opt = linkRepo.findByComponentIdAndUserIdAndActiveTrue(componentId, targetUserId);
        if (opt.isEmpty()) {
            throw new IllegalStateException("No active link exists for component " + componentId + " and user " + targetUserId);
        }
        AssetUserLink link = opt.get();
        link.setActive(false);
        link.setUnassignedDate(LocalDateTime.now());
        link.setUpdatedBy(updatedBy);
        link.setUpdatedAt(LocalDateTime.now());
        linkRepo.save(link);
        notifyAudit(bearer, "DELINK_COMPONENT", updatedBy, null, componentId);
        return "delinked";
    }

    private String delinkModel(Long modelId, Long targetUserId, String updatedBy, Long updatedById, String bearer) {
        List<AssetMaster> assets = assetRepo.findAll().stream()
                .filter(a -> a.getModel() != null && Objects.equals(a.getModel().getModelId(), modelId))
                .collect(Collectors.toList());
        int delinked = 0;
        for (AssetMaster a : assets) {
            linkRepo.findByAssetIdAndUserIdAndActiveTrue(a.getAssetId(), targetUserId)
                    .ifPresent(link -> {
                        link.setActive(false);
                        link.setUnassignedDate(LocalDateTime.now());
                        link.setUpdatedBy(updatedBy);
                        link.setUpdatedAt(LocalDateTime.now());
                        linkRepo.save(link);
                    });
            // count after fetch
            if (!linkRepo.existsByAssetIdAndUserIdAndActiveTrue(a.getAssetId(), targetUserId)) delinked++;
        }
        notifyAudit(bearer, "DELINK_MODEL", updatedBy, null, null);
        return delinked > 0 ? "delinked:" + delinked : "nothing";
    }

    private String delinkMake(Long makeId, Long targetUserId, String updatedBy, Long updatedById, String bearer) {
        List<AssetMaster> assets = assetRepo.findAll().stream()
                .filter(a -> a.getMake() != null && Objects.equals(a.getMake().getMakeId(), makeId))
                .collect(Collectors.toList());
        int delinked = 0;
        for (AssetMaster a : assets) {
            linkRepo.findByAssetIdAndUserIdAndActiveTrue(a.getAssetId(), targetUserId)
                    .ifPresent(link -> {
                        link.setActive(false);
                        link.setUnassignedDate(LocalDateTime.now());
                        link.setUpdatedBy(updatedBy);
                        link.setUpdatedAt(LocalDateTime.now());
                        linkRepo.save(link);
                    });
            if (!linkRepo.existsByAssetIdAndUserIdAndActiveTrue(a.getAssetId(), targetUserId)) delinked++;
        }
        notifyAudit(bearer, "DELINK_MAKE", updatedBy, null, null);
        return delinked > 0 ? "delinked:" + delinked : "nothing";
    }

    private String delinkViaAssetFromRelatedEntity(String et, Long id, Long targetUserId, String updatedBy, Long updatedById, String bearer) {
        List<Long> assetIds = resolveAssetIdsFromRelatedEntity(et, id);
        if (assetIds.isEmpty()) return "nothing";

        int delinked = 0;
        for (Long aid : assetIds) {
            Optional<AssetUserLink> opt = linkRepo.findByAssetIdAndUserIdAndActiveTrue(aid, targetUserId);
            if (opt.isPresent()) {
                AssetUserLink link = opt.get();
                link.setActive(false);
                link.setUnassignedDate(LocalDateTime.now());
                link.setUpdatedBy(updatedBy);
                link.setUpdatedAt(LocalDateTime.now());
                linkRepo.save(link);
                delinked++;
            }
        }
        notifyAudit(bearer, "DELINK_" + et, updatedBy, null, null);
        return delinked > 0 ? "delinked:" + delinked : "nothing";
    }

    // -------------------------------------------------------------------------
    // Query helpers (existing functionality)
    // -------------------------------------------------------------------------
    public List<Map<String, Object>> getAssetsAssignedToUser(Long userId) {
        List<AssetUserLink> links = linkRepo.findByUserIdAndActiveTrue(userId);
        if (links == null || links.isEmpty()) return Collections.emptyList();

        List<Map<String, Object>> out = new ArrayList<>();
        for (AssetUserLink l : links) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("linkId", l.getLinkId());
            row.put("assetId", l.getAssetId());
            row.put("componentId", l.getComponentId());
            row.put("assignedDate", l.getAssignedDate());
            row.put("createdBy", l.getCreatedBy());
            row.put("createdAt", l.getCreatedAt());
            row.put("updatedBy", l.getUpdatedBy());
            row.put("updatedAt", l.getUpdatedAt());
            out.add(row);
        }
        return out;
    }

    public Map<String, Object> getSingleAsset(Long assetId, Long componentId) {
        Optional<AssetUserLink> opt = (componentId != null)
                ? linkRepo.findFirstByComponentId(componentId)
                : linkRepo.findFirstByAssetId(assetId);

        if (opt.isEmpty()) throw new IllegalStateException("Asset/Component not found");

        AssetUserLink l = opt.get();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assetId", l.getAssetId());
        result.put("componentId", l.getComponentId());
        result.put("userId", l.getUserId());
        result.put("username", l.getUsername());
        result.put("assignedDate", l.getAssignedDate());
        result.put("createdBy", l.getCreatedBy());
        result.put("createdAt", l.getCreatedAt());
        result.put("updatedBy", l.getUpdatedBy());
        result.put("updatedAt", l.getUpdatedAt());
        return result;
    }

    public List<Map<String, Object>> getUsersBySubCategory(String bearer, Long subCategoryId) {
        // fetch all active links then filter via assetRepo's subCategory relationship (similar to original code)
        List<AssetUserLink> activeLinks = linkRepo.findBySubCategoryId(subCategoryId);
        List<AssetUserLink> filtered = activeLinks.stream()
                .filter(l -> l.getAssetId() != null)
                .filter(l -> assetRepo.findById(l.getAssetId())
                        .map(a -> a.getSubCategory() != null && Objects.equals(a.getSubCategory().getSubCategoryId(), subCategoryId))
                        .orElse(false))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = filtered.stream()
                .map(link -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("userId", link.getUserId());
                    m.put("username", link.getUsername());
                    m.put("email", link.getEmail());
                    m.put("mobile", link.getMobile());
                    m.put("assignedDate", link.getAssignedDate());
                    return m;
                }).distinct().collect(Collectors.toList());

        // send safe notification (audit)
        try {
            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("subCategoryId", subCategoryId);
            placeholders.put("userCount", result.size());
            placeholders.put("timestamp", new Date().toString());
            placeholders.put("username", "system");
            safeNotificationHelper.safeNotifyAsync(bearer, null, "system", null, null, "INAPP", "USER_LINK_QUERY", placeholders, "ASSET_SERVICE");
        } catch (Exception ex) {
            log.warn("Safe notify failed: {}", ex.getMessage());
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // Notification helper
    // -------------------------------------------------------------------------
    private void notifyAudit(String bearer, String action, String username, Long assetId, Long componentId) {
        try {
            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("action", action);
            placeholders.put("username", username);
            placeholders.put("assetId", assetId);
            placeholders.put("componentId", componentId);
            placeholders.put("timestamp", new Date().toString());
            safeNotificationHelper.safeNotifyAsync(bearer, null, username, null, null, "INAPP", action, placeholders, "ASSET_SERVICE");
        } catch (Exception e) {
            log.warn("safeNotifyAsync failed for {}: {}", action, e.getMessage());
        }
    }
}




