
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

import java.time.LocalDate;
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
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final VendorRepository vendorRepo;
    private final PurchaseOutletRepository outletRepo;
    private final StatusMasterRepository statusRepo;
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
            ProductCategoryRepository categoryRepo,
            ProductSubCategoryRepository subCategoryRepo,
            VendorRepository vendorRepo,
            PurchaseOutletRepository outletRepo,
            StatusMasterRepository statusRepo,
            SafeNotificationHelper safeNotificationHelper) {

        this.linkRepo = linkRepo;
        this.assetRepo = assetRepo;
        this.componentRepo = componentRepo;
        this.modelRepo = modelRepo;
        this.makeRepo = makeRepo;
        this.warrantyRepo = warrantyRepo;
        this.amcRepo = amcRepo;
        this.documentRepo = documentRepo;
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.vendorRepo = vendorRepo;
        this.outletRepo = outletRepo;
        this.statusRepo = statusRepo;
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
    // Comprehensive Master Data Retrieval
    // -------------------------------------------------------------------------
    /**
     * Get all master data in detail including:
     * - Users (from asset user links)
     * - Assets
     * - Components
     * - Warranties
     * - AMCs
     * - Makes
     * - Models
     * - Categories
     * - Sub-categories
     * - Vendors
     * - Outlets
     * - Statuses
     */
    public Map<String, Object> getAllMasterDataInDetail() {
        return getAllMasterDataInDetailByUserId(null, null, null);
    }

    /**
     * Helper method to sort entities by: 1. isMostLike (true first), 2. isFavourite (true first), 
     * 3. sequenceOrder (nulls last), 4. name/ID
     * Works with Map<String, Object> entities that have these fields
     */
    private void sortBySequenceOrder(List<Map<String, Object>> entities, String nameField) {
        entities.sort((a, b) -> {
            // Priority 1: isMostLike (true first)
            Boolean mostLikeA = (Boolean) a.get("isMostLike");
            Boolean mostLikeB = (Boolean) b.get("isMostLike");
            boolean mostLikeAVal = mostLikeA != null ? mostLikeA : false;
            boolean mostLikeBVal = mostLikeB != null ? mostLikeB : false;
            int mostLikeCompare = Boolean.compare(mostLikeBVal, mostLikeAVal); // true first (descending)
            if (mostLikeCompare != 0) return mostLikeCompare;
            
            // Priority 2: isFavourite (true first)
            Boolean favA = (Boolean) a.get("isFavourite");
            Boolean favB = (Boolean) b.get("isFavourite");
            boolean favAVal = favA != null ? favA : false;
            boolean favBVal = favB != null ? favB : false;
            int favCompare = Boolean.compare(favBVal, favAVal); // true first (descending)
            if (favCompare != 0) return favCompare;
            
            // Priority 3: sequenceOrder (nulls last)
            Integer seqA = (Integer) a.get("sequenceOrder");
            Integer seqB = (Integer) b.get("sequenceOrder");
            if (seqA == null && seqB == null) {
                // Both null, sort by name field
                Object nameA = a.get(nameField);
                Object nameB = b.get(nameField);
                if (nameA == null && nameB == null) return 0;
                if (nameA == null) return 1;
                if (nameB == null) return -1;
                return nameA.toString().compareToIgnoreCase(nameB.toString());
            }
            if (seqA == null) return 1;
            if (seqB == null) return -1;
            int seqCompare = seqA.compareTo(seqB);
            if (seqCompare != 0) return seqCompare;
            
            // Priority 4: If sequenceOrder is same, sort by name
            Object nameA = a.get(nameField);
            Object nameB = b.get(nameField);
            if (nameA == null && nameB == null) return 0;
            if (nameA == null) return 1;
            if (nameB == null) return -1;
            return nameA.toString().compareToIgnoreCase(nameB.toString());
        });
    }

    /**
     * Helper method to fetch documents for an entity type and ID
     * Returns a list of document information including file paths, names, and types
     */
    private List<Map<String, Object>> getDocumentsForEntity(String entityType, Long entityId) {
        if (entityId == null) {
            return new ArrayList<>();
        }
        try {
            return documentRepo.findAllByEntityTypeIgnoreCaseAndEntityIdAndActiveTrue(entityType, entityId)
                    .stream()
                    .map(doc -> {
                        Map<String, Object> docMap = new LinkedHashMap<>();
                        docMap.put("documentId", doc.getDocumentId());
                        docMap.put("fileName", doc.getFileName());
                        docMap.put("filePath", doc.getFilePath());
                        docMap.put("docType", doc.getDocType());
                        docMap.put("uploadedDate", doc.getUploadedDate());
                        docMap.put("entityType", doc.getEntityType());
                        docMap.put("entityId", doc.getEntityId());
                        return docMap;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("⚠️ Error fetching documents for {} ID {}: {}", entityType, entityId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all master data in detail filtered by user ID.
     * Returns only data related to the specified user:
     * - User information
     * - Assets assigned to the user
     * - Components assigned to the user
     * - Warranties for user's assets
     * - AMCs for user's assets
     * - Makes/Models/Categories/Sub-categories of user's assets
     * - Vendors/Outlets related to user's assets
     * - Statuses used by user's assets
     * - Documents (images, PDFs, PNGs, JPGs, etc.) for all entities
     * 
     * @param userId Optional user ID to filter data (null returns all)
     * @param loginUserId The ID of the user making the request (for audit)
     * @param loginUsername The username of the user making the request (for audit)
     */
    public Map<String, Object> getAllMasterDataInDetailByUserId(Long userId, Long loginUserId, String loginUsername) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        try {
            // Get user's active links to determine which assets/components are assigned
            Set<Long> userAssetIds = new HashSet<>();
            Set<Long> userComponentIds = new HashSet<>();
            AssetUserLink userLinkInfo = null;
            
            if (userId != null) {
                List<AssetUserLink> userLinks = linkRepo.findByUserIdAndActiveTrue(userId);
                for (AssetUserLink link : userLinks) {
                    if (link.getAssetId() != null) {
                        userAssetIds.add(link.getAssetId());
                    }
                    if (link.getComponentId() != null) {
                        userComponentIds.add(link.getComponentId());
                    }
                    if (userLinkInfo == null) {
                        userLinkInfo = link; // Store first link for user info
                    }
                }
                
                if (userLinks.isEmpty()) {
                    log.warn("⚠️ No active links found for userId: {}", userId);
                    result.put("message", "No active links found for user ID: " + userId);
                }
            }
            
            // 1. USER INFORMATION
            List<Map<String, Object>> users = new ArrayList<>();
            if (userId != null && userLinkInfo != null) {
                Map<String, Object> userMap = new LinkedHashMap<>();
                userMap.put("userId", userLinkInfo.getUserId());
                userMap.put("username", userLinkInfo.getUsername());
                userMap.put("email", userLinkInfo.getEmail());
                userMap.put("mobile", userLinkInfo.getMobile());
                userMap.put("assignedDate", userLinkInfo.getAssignedDate());
                userMap.put("unassignedDate", userLinkInfo.getUnassignedDate());
                userMap.put("sequenceOrder", userLinkInfo.getSequenceOrder());
                userMap.put("isFavourite", userLinkInfo.getIsFavourite());
                userMap.put("isMostLike", userLinkInfo.getIsMostLike());
                // Add documents for user (if USER entity type is supported)
                userMap.put("documents", getDocumentsForEntity("USER", userLinkInfo.getUserId()));
                users.add(userMap);
            } else if (userId == null) {
                // If no userId specified, return all users
                Set<Long> uniqueUserIds = new HashSet<>();
                List<AssetUserLink> allLinks = linkRepo.findAll();
                for (AssetUserLink link : allLinks) {
                    if (link.getUserId() != null && !uniqueUserIds.contains(link.getUserId())) {
                        uniqueUserIds.add(link.getUserId());
                        Map<String, Object> userMap = new LinkedHashMap<>();
                        userMap.put("userId", link.getUserId());
                        userMap.put("username", link.getUsername());
                        userMap.put("email", link.getEmail());
                        userMap.put("mobile", link.getMobile());
                        userMap.put("sequenceOrder", link.getSequenceOrder());
                        userMap.put("isFavourite", link.getIsFavourite());
                        userMap.put("isMostLike", link.getIsMostLike());
                        // Add documents for user (if USER entity type is supported)
                        userMap.put("documents", getDocumentsForEntity("USER", link.getUserId()));
                        users.add(userMap);
                    }
                }
            }
            sortBySequenceOrder(users, "username");
            result.put("users", users);
            
            // 2. ASSETS (filtered by userId if provided)
            List<Map<String, Object>> assets = (userId == null ? assetRepo.findAll() : 
                    assetRepo.findAll().stream()
                            .filter(asset -> userAssetIds.contains(asset.getAssetId()))
                            .collect(Collectors.toList())).stream()
                    .map(asset -> {
                        Map<String, Object> assetMap = new LinkedHashMap<>();
                        assetMap.put("assetId", asset.getAssetId());
                        assetMap.put("assetNameUdv", asset.getAssetNameUdv());
                        assetMap.put("assetStatus", asset.getAssetStatus());
                        if (asset.getCategory() != null) {
                            assetMap.put("categoryId", asset.getCategory().getCategoryId());
                            assetMap.put("categoryName", asset.getCategory().getCategoryName());
                        }
                        if (asset.getSubCategory() != null) {
                            assetMap.put("subCategoryId", asset.getSubCategory().getSubCategoryId());
                            assetMap.put("subCategoryName", asset.getSubCategory().getSubCategoryName());
                        }
                        if (asset.getMake() != null) {
                            assetMap.put("makeId", asset.getMake().getMakeId());
                            assetMap.put("makeName", asset.getMake().getMakeName());
                        }
                        if (asset.getModel() != null) {
                            assetMap.put("modelId", asset.getModel().getModelId());
                            assetMap.put("modelName", asset.getModel().getModelName());
                        }
                        assetMap.put("active", asset.getActive());
                        assetMap.put("createdBy", asset.getCreatedBy());
                        assetMap.put("createdAt", asset.getCreatedAt());
                        assetMap.put("updatedBy", asset.getUpdatedBy());
                        assetMap.put("updatedAt", asset.getUpdatedAt());
                        assetMap.put("sequenceOrder", asset.getSequenceOrder());
                        assetMap.put("isFavourite", asset.getIsFavourite());
                        assetMap.put("isMostLike", asset.getIsMostLike());
                        // Add documents for asset
                        assetMap.put("documents", getDocumentsForEntity("ASSET", asset.getAssetId()));
                        return assetMap;
                    })
                    .collect(Collectors.toList());
            sortBySequenceOrder(assets, "assetNameUdv");
            result.put("assets", assets);
            
            // Collect related IDs for filtering other entities
            Set<Long> relatedCategoryIds = new HashSet<>();
            Set<Long> relatedSubCategoryIds = new HashSet<>();
            Set<Long> relatedMakeIds = new HashSet<>();
            Set<Long> relatedModelIds = new HashSet<>();
            
            for (AssetMaster asset : (userId == null ? assetRepo.findAll() : 
                    assetRepo.findAll().stream()
                            .filter(a -> userAssetIds.contains(a.getAssetId()))
                            .collect(Collectors.toList()))) {
                if (asset.getCategory() != null) {
                    relatedCategoryIds.add(asset.getCategory().getCategoryId());
                }
                if (asset.getSubCategory() != null) {
                    relatedSubCategoryIds.add(asset.getSubCategory().getSubCategoryId());
                }
                if (asset.getMake() != null) {
                    relatedMakeIds.add(asset.getMake().getMakeId());
                }
                if (asset.getModel() != null) {
                    relatedModelIds.add(asset.getModel().getModelId());
                }
            }
            
            // 3. COMPONENTS (filtered by userId if provided)
            List<Map<String, Object>> components = (userId == null ? componentRepo.findAll() : 
                    componentRepo.findAll().stream()
                            .filter(component -> userComponentIds.contains(component.getComponentId()))
                            .collect(Collectors.toList())).stream()
                    .map(component -> {
                        Map<String, Object> compMap = new LinkedHashMap<>();
                        compMap.put("componentId", component.getComponentId());
                        compMap.put("componentName", component.getComponentName());
                        compMap.put("description", component.getDescription());
                        compMap.put("active", component.getActive());
                        compMap.put("createdBy", component.getCreatedBy());
                        compMap.put("createdAt", component.getCreatedAt());
                        compMap.put("updatedBy", component.getUpdatedBy());
                        compMap.put("updatedAt", component.getUpdatedAt());
                        compMap.put("sequenceOrder", component.getSequenceOrder());
                        compMap.put("isFavourite", component.getIsFavourite());
                        compMap.put("isMostLike", component.getIsMostLike());
                        // Add documents for component
                        compMap.put("documents", getDocumentsForEntity("COMPONENT", component.getComponentId()));
                        return compMap;
                    })
                    .collect(Collectors.toList());
            sortBySequenceOrder(components, "componentName");
            result.put("components", components);
            
            // 4. WARRANTIES (filtered by user's assets if userId provided)
            List<Map<String, Object>> warranties = (userId == null ? warrantyRepo.findAll() : 
                    warrantyRepo.findAll().stream()
                            .filter(warranty -> warranty.getAsset() != null && 
                                    userAssetIds.contains(warranty.getAsset().getAssetId()))
                            .collect(Collectors.toList())).stream()
                    .map(warranty -> {
                        Map<String, Object> warrantyMap = new LinkedHashMap<>();
                        warrantyMap.put("warrantyId", warranty.getWarrantyId());
                        if (warranty.getAsset() != null) {
                            warrantyMap.put("assetId", warranty.getAsset().getAssetId());
                            warrantyMap.put("assetName", warranty.getAsset().getAssetNameUdv());
                        }
                        warrantyMap.put("warrantyStartDate", warranty.getWarrantyStartDate());
                        warrantyMap.put("warrantyEndDate", warranty.getWarrantyEndDate());
                        warrantyMap.put("warrantyProvider", warranty.getWarrantyProvider());
                        warrantyMap.put("warrantyStatus", warranty.getWarrantyStatus());
                        warrantyMap.put("warrantyTerms", warranty.getWarrantyTerms());
                        warrantyMap.put("userId", warranty.getUserId());
                        warrantyMap.put("username", warranty.getUsername());
                        warrantyMap.put("componentId", warranty.getComponentId());
                        warrantyMap.put("documentId", warranty.getDocumentId());
                        warrantyMap.put("active", warranty.getActive());
                        warrantyMap.put("createdBy", warranty.getCreatedBy());
                        warrantyMap.put("createdAt", warranty.getCreatedAt());
                        warrantyMap.put("updatedBy", warranty.getUpdatedBy());
                        warrantyMap.put("updatedAt", warranty.getUpdatedAt());
                        warrantyMap.put("sequenceOrder", warranty.getSequenceOrder());
                        warrantyMap.put("isFavourite", warranty.getIsFavourite());
                        warrantyMap.put("isMostLike", warranty.getIsMostLike());
                        // Add documents for warranty
                        warrantyMap.put("documents", getDocumentsForEntity("WARRANTY", warranty.getWarrantyId()));
                        return warrantyMap;
                    })
                    .collect(Collectors.toList());
            sortBySequenceOrder(warranties, "warrantyProvider");
            result.put("warranties", warranties);
            
            // 5. AMCs (filtered by user's assets if userId provided)
            List<Map<String, Object>> amcs = (userId == null ? amcRepo.findAll() : 
                    amcRepo.findAll().stream()
                            .filter(amc -> amc.getAsset() != null && 
                                    userAssetIds.contains(amc.getAsset().getAssetId()))
                            .collect(Collectors.toList())).stream()
                    .map(amc -> {
                        Map<String, Object> amcMap = new LinkedHashMap<>();
                        amcMap.put("amcId", amc.getAmcId());
                        if (amc.getAsset() != null) {
                            amcMap.put("assetId", amc.getAsset().getAssetId());
                            amcMap.put("assetName", amc.getAsset().getAssetNameUdv());
                        }
                        amcMap.put("amcStartDate", amc.getStartDate());
                        amcMap.put("amcEndDate", amc.getEndDate());
                        amcMap.put("amcStatus", amc.getAmcStatus());
                        amcMap.put("userId", amc.getUserId());
                        amcMap.put("username", amc.getUsername());
                        amcMap.put("componentId", amc.getComponentId());
                        amcMap.put("documentId", amc.getDocumentId());
                        amcMap.put("active", amc.getActive());
                        amcMap.put("createdBy", amc.getCreatedBy());
                        amcMap.put("createdAt", amc.getCreatedAt());
                        amcMap.put("updatedBy", amc.getUpdatedBy());
                        amcMap.put("updatedAt", amc.getUpdatedAt());
                        amcMap.put("sequenceOrder", amc.getSequenceOrder());
                        amcMap.put("isFavourite", amc.getIsFavourite());
                        amcMap.put("isMostLike", amc.getIsMostLike());
                        // Add documents for AMC
                        amcMap.put("documents", getDocumentsForEntity("AMC", amc.getAmcId()));
                        return amcMap;
                    })
                    .collect(Collectors.toList());
            sortBySequenceOrder(amcs, "amcStatus");
            result.put("amcs", amcs);
            
            // 6. MAKES (filtered by user's assets if userId provided)
            List<Map<String, Object>> makes = (userId == null ? makeRepo.findAll() : 
                    makeRepo.findAll().stream()
                            .filter(make -> relatedMakeIds.contains(make.getMakeId()))
                            .collect(Collectors.toList())).stream()
                    .map(make -> {
                        Map<String, Object> makeMap = new LinkedHashMap<>();
                        makeMap.put("makeId", make.getMakeId());
                        makeMap.put("makeName", make.getMakeName());
                        if (make.getSubCategory() != null) {
                            makeMap.put("subCategoryId", make.getSubCategory().getSubCategoryId());
                            makeMap.put("subCategoryName", make.getSubCategory().getSubCategoryName());
                        }
                        makeMap.put("active", make.getActive());
                        makeMap.put("createdBy", make.getCreatedBy());
                        makeMap.put("createdAt", make.getCreatedAt());
                        makeMap.put("updatedBy", make.getUpdatedBy());
                        makeMap.put("updatedAt", make.getUpdatedAt());
                        makeMap.put("sequenceOrder", make.getSequenceOrder());
                        makeMap.put("isFavourite", make.getIsFavourite());
                        makeMap.put("isMostLike", make.getIsMostLike());
                        // Add documents for make
                        makeMap.put("documents", getDocumentsForEntity("MAKE", make.getMakeId()));
                        return makeMap;
                    })
                    .collect(Collectors.toList());
            sortBySequenceOrder(makes, "makeName");
            result.put("makes", makes);
            
            // 7. MODELS (filtered by user's assets if userId provided)
            List<Map<String, Object>> models = (userId == null ? modelRepo.findAll() : 
                    modelRepo.findAll().stream()
                            .filter(model -> relatedModelIds.contains(model.getModelId()))
                            .collect(Collectors.toList())).stream()
                    .map(model -> {
                        Map<String, Object> modelMap = new LinkedHashMap<>();
                        modelMap.put("modelId", model.getModelId());
                        modelMap.put("modelName", model.getModelName());
                        if (model.getMake() != null) {
                            modelMap.put("makeId", model.getMake().getMakeId());
                            modelMap.put("makeName", model.getMake().getMakeName());
                        }
                        modelMap.put("active", model.getActive());
                        modelMap.put("createdBy", model.getCreatedBy());
                        modelMap.put("createdAt", model.getCreatedAt());
                        modelMap.put("updatedBy", model.getUpdatedBy());
                        modelMap.put("updatedAt", model.getUpdatedAt());
                        modelMap.put("sequenceOrder", model.getSequenceOrder());
                        modelMap.put("isFavourite", model.getIsFavourite());
                        modelMap.put("isMostLike", model.getIsMostLike());
                        // Add documents for model
                        modelMap.put("documents", getDocumentsForEntity("MODEL", model.getModelId()));
                        return modelMap;
                    })
                    .collect(Collectors.toList());
            sortBySequenceOrder(models, "modelName");
            result.put("models", models);
            
            // 8. CATEGORIES (filtered by user's assets if userId provided)
            List<Map<String, Object>> categories = (userId == null ? categoryRepo.findAll() : 
                    categoryRepo.findAll().stream()
                            .filter(category -> relatedCategoryIds.contains(category.getCategoryId()))
                            .collect(Collectors.toList())).stream()
                    .map(category -> {
                        Map<String, Object> catMap = new LinkedHashMap<>();
                        catMap.put("categoryId", category.getCategoryId());
                        catMap.put("categoryName", category.getCategoryName());
                        catMap.put("description", category.getDescription());
                        catMap.put("active", category.getActive());
                        catMap.put("createdBy", category.getCreatedBy());
                        catMap.put("createdAt", category.getCreatedAt());
                        catMap.put("updatedBy", category.getUpdatedBy());
                        catMap.put("updatedAt", category.getUpdatedAt());
                        catMap.put("sequenceOrder", category.getSequenceOrder());
                        catMap.put("isFavourite", category.getIsFavourite());
                        catMap.put("isMostLike", category.getIsMostLike());
                        // Add documents for category
                        catMap.put("documents", getDocumentsForEntity("CATEGORY", category.getCategoryId()));
                        return catMap;
                    })
                    .collect(Collectors.toList());
            sortBySequenceOrder(categories, "categoryName");
            result.put("categories", categories);
            
            // 9. SUB-CATEGORIES (filtered by user's assets if userId provided)
            List<Map<String, Object>> subCategories = (userId == null ? subCategoryRepo.findAll() : 
                    subCategoryRepo.findAll().stream()
                            .filter(subCategory -> relatedSubCategoryIds.contains(subCategory.getSubCategoryId()))
                            .collect(Collectors.toList())).stream()
                    .map(subCategory -> {
                        Map<String, Object> subCatMap = new LinkedHashMap<>();
                        subCatMap.put("subCategoryId", subCategory.getSubCategoryId());
                        subCatMap.put("subCategoryName", subCategory.getSubCategoryName());
                        if (subCategory.getCategory() != null) {
                            subCatMap.put("categoryId", subCategory.getCategory().getCategoryId());
                            subCatMap.put("categoryName", subCategory.getCategory().getCategoryName());
                        }
                        subCatMap.put("active", subCategory.getActive());
                        subCatMap.put("createdBy", subCategory.getCreatedBy());
                        subCatMap.put("createdAt", subCategory.getCreatedAt());
                        subCatMap.put("updatedBy", subCategory.getUpdatedBy());
                        subCatMap.put("updatedAt", subCategory.getUpdatedAt());
                        subCatMap.put("sequenceOrder", subCategory.getSequenceOrder());
                        subCatMap.put("isFavourite", subCategory.getIsFavourite());
                        subCatMap.put("isMostLike", subCategory.getIsMostLike());
                        // Add documents for subCategory
                        subCatMap.put("documents", getDocumentsForEntity("SUBCATEGORY", subCategory.getSubCategoryId()));
                        return subCatMap;
                    })
                    .collect(Collectors.toList());
            sortBySequenceOrder(subCategories, "subCategoryName");
            result.put("subCategories", subCategories);
            
            // 10. VENDORS (return all vendors - not directly linked to assets in current schema)
            // Note: Vendors are master data and don't have a direct relationship with AssetMaster.
            // They are not filtered by userId as there's no direct link between vendors and user-assigned assets.
            // If vendor filtering is needed in the future, it would require adding a vendor relationship to AssetMaster.
            List<Map<String, Object>> vendors = vendorRepo.findAll().stream()
                    .map(vendor -> {
                        Map<String, Object> vendorMap = new LinkedHashMap<>();
                        vendorMap.put("vendorId", vendor.getVendorId());
                        vendorMap.put("vendorName", vendor.getVendorName());
                        vendorMap.put("contactPerson", vendor.getContactPerson());
                        vendorMap.put("email", vendor.getEmail());
                        vendorMap.put("mobile", vendor.getMobile());
                        vendorMap.put("address", vendor.getAddress());
                        if (vendor.getOutlets() != null) {
                            vendorMap.put("outletCount", vendor.getOutlets().size());
                        }
                        vendorMap.put("active", vendor.getActive());
                        vendorMap.put("createdBy", vendor.getCreatedBy());
                        vendorMap.put("createdAt", vendor.getCreatedAt());
                        vendorMap.put("updatedBy", vendor.getUpdatedBy());
                        vendorMap.put("updatedAt", vendor.getUpdatedAt());
                        vendorMap.put("sequenceOrder", vendor.getSequenceOrder());
                        vendorMap.put("isFavourite", vendor.getIsFavourite());
                        vendorMap.put("isMostLike", vendor.getIsMostLike());
                        // Add documents for vendor
                        vendorMap.put("documents", getDocumentsForEntity("VENDOR", vendor.getVendorId()));
                        return vendorMap;
                    })
                    .collect(Collectors.toList());
            sortBySequenceOrder(vendors, "vendorName");
            result.put("vendors", vendors);
            
            // 11. OUTLETS (return all outlets - not directly linked to assets in current schema)
            // Note: Outlets are master data and don't have a direct relationship with AssetMaster.
            // They are not filtered by userId as there's no direct link between outlets and user-assigned assets.
            // If outlet filtering is needed in the future, it would require adding an outlet relationship to AssetMaster.
            List<Map<String, Object>> outlets = outletRepo.findAll().stream()
                    .map(outlet -> {
                        Map<String, Object> outletMap = new LinkedHashMap<>();
                        outletMap.put("outletId", outlet.getOutletId());
                        outletMap.put("outletName", outlet.getOutletName());
                        outletMap.put("outletAddress", outlet.getOutletAddress());
                        outletMap.put("contactInfo", outlet.getContactInfo());
                        if (outlet.getVendor() != null) {
                            outletMap.put("vendorId", outlet.getVendor().getVendorId());
                            outletMap.put("vendorName", outlet.getVendor().getVendorName());
                        }
                        outletMap.put("active", outlet.getActive());
                        outletMap.put("createdBy", outlet.getCreatedBy());
                        outletMap.put("createdAt", outlet.getCreatedAt());
                        outletMap.put("updatedBy", outlet.getUpdatedBy());
                        outletMap.put("updatedAt", outlet.getUpdatedAt());
                        outletMap.put("sequenceOrder", outlet.getSequenceOrder());
                        outletMap.put("isFavourite", outlet.getIsFavourite());
                        outletMap.put("isMostLike", outlet.getIsMostLike());
                        // Add documents for outlet
                        outletMap.put("documents", getDocumentsForEntity("OUTLET", outlet.getOutletId()));
                        return outletMap;
                    })
                    .collect(Collectors.toList());
            sortBySequenceOrder(outlets, "outletName");
            result.put("outlets", outlets);
            
            // Collect status codes from user's assets
            Set<String> relatedStatusCodes = new HashSet<>();
            if (userId != null) {
                for (AssetMaster asset : assetRepo.findAll().stream()
                        .filter(a -> userAssetIds.contains(a.getAssetId()))
                        .collect(Collectors.toList())) {
                    if (asset.getAssetStatus() != null) {
                        relatedStatusCodes.add(asset.getAssetStatus());
                    }
                }
            }
            
            // 12. STATUSES (filtered by user's asset statuses if userId provided)
            List<Map<String, Object>> statuses = (userId == null ? statusRepo.findAll() : 
                    statusRepo.findAll().stream()
                            .filter(status -> relatedStatusCodes.contains(status.getCode()) || 
                                    relatedStatusCodes.isEmpty())
                            .collect(Collectors.toList())).stream()
                    .map(status -> {
                        Map<String, Object> statusMap = new LinkedHashMap<>();
                        statusMap.put("statusId", status.getStatusId());
                        statusMap.put("code", status.getCode());
                        statusMap.put("description", status.getDescription());
                        statusMap.put("category", status.getCategory());
                        statusMap.put("active", status.getActive());
                        statusMap.put("createdBy", status.getCreatedBy());
                        statusMap.put("createdAt", status.getCreatedAt());
                        statusMap.put("updatedBy", status.getUpdatedBy());
                        statusMap.put("updatedAt", status.getUpdatedAt());
                        statusMap.put("sequenceOrder", status.getSequenceOrder());
                        statusMap.put("isFavourite", status.getIsFavourite());
                        statusMap.put("isMostLike", status.getIsMostLike());
                        // Add documents for status (convert Integer to Long)
                        statusMap.put("documents", getDocumentsForEntity("STATUS", status.getStatusId() != null ? status.getStatusId().longValue() : null));
                        return statusMap;
                    })
                    .collect(Collectors.toList());
            sortBySequenceOrder(statuses, "code");
            result.put("statuses", statuses);
            
            // Summary counts
            Map<String, Long> summary = new LinkedHashMap<>();
            summary.put("totalUsers", (long) users.size());
            summary.put("totalAssets", (long) assets.size());
            summary.put("totalComponents", (long) components.size());
            summary.put("totalWarranties", (long) warranties.size());
            summary.put("totalAmcs", (long) amcs.size());
            summary.put("totalMakes", (long) makes.size());
            summary.put("totalModels", (long) models.size());
            summary.put("totalCategories", (long) categories.size());
            summary.put("totalSubCategories", (long) subCategories.size());
            summary.put("totalVendors", (long) vendors.size());
            summary.put("totalOutlets", (long) outlets.size());
            summary.put("totalStatuses", (long) statuses.size());
            result.put("summary", summary);
            
            // Add audit information (login user who made the request)
            Map<String, Object> auditInfo = new LinkedHashMap<>();
            auditInfo.put("loginUserId", loginUserId);
            auditInfo.put("loginUsername", loginUsername);
            auditInfo.put("requestedAt", LocalDateTime.now());
            if (userId != null) {
                auditInfo.put("filteredByUserId", userId);
            }
            result.put("audit", auditInfo);
            
            String logMessage = userId != null ? 
                    "✅ Retrieved master data for userId {}: {} users, {} assets, {} components, {} warranties, {} amcs, {} makes, {} models, {} categories, {} subCategories, {} vendors, {} outlets, {} statuses (requested by userId={}, username={})" :
                    "✅ Retrieved all master data: {} users, {} assets, {} components, {} warranties, {} amcs, {} makes, {} models, {} categories, {} subCategories, {} vendors, {} outlets, {} statuses (requested by userId={}, username={})";
            
            if (userId != null) {
                log.info(logMessage, userId, users.size(), assets.size(), components.size(), warranties.size(), amcs.size(),
                        makes.size(), models.size(), categories.size(), subCategories.size(),
                        vendors.size(), outlets.size(), statuses.size(), loginUserId, loginUsername);
            } else {
                log.info(logMessage, users.size(), assets.size(), components.size(), warranties.size(), amcs.size(),
                        makes.size(), models.size(), categories.size(), subCategories.size(),
                        vendors.size(), outlets.size(), statuses.size(), loginUserId, loginUsername);
            }
            
        } catch (Exception e) {
            log.error("❌ Error retrieving master data: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
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

    // ========================================================================
    // NEED YOUR ATTENTION - Comprehensive Dashboard API
    // ========================================================================
    /**
     * Get comprehensive "Need Your Attention" data including all entities in detail.
     * This API aggregates all features: users, assets, components, warranties, AMCs,
     * makes, models, categories, sub-categories, vendors, outlets, statuses, and more.
     * The data is filtered by the logged-in user (userId) to show only relevant information.
     * 
     * @param userId The ID of the user to filter data for (logged-in user). If null, returns all data.
     * @param loginUserId The ID of the user making the request (for audit)
     * @param loginUsername The username of the user making the request (for audit)
     * @return Comprehensive map with all entities and summary information filtered by userId
     */
    public Map<String, Object> getNeedYourAttentionData(Long userId, Long loginUserId, String loginUsername) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        try {
            log.info("📊 Need Your Attention API - Comprehensive data request for userId: {} (requested by: {})", 
                    userId, loginUsername);
            
            // Get master data filtered by userId (logged-in user)
            Map<String, Object> allMasterData = getAllMasterDataInDetailByUserId(userId, loginUserId, loginUsername);
            
            // Add enhanced summary counts
            @SuppressWarnings("unchecked")
            Map<String, Long> existingSummary = (Map<String, Long>) allMasterData.getOrDefault("summary", new LinkedHashMap<>());
            Map<String, Object> summary = new LinkedHashMap<>(existingSummary);
            
            // Add attention indicators
            Map<String, Object> attention = new LinkedHashMap<>();
            
            // Warranties expiring soon (within 30 days) and expired warranties
            List<Map<String, Object>> expiringWarranties = new ArrayList<>();
            List<Map<String, Object>> expiredWarranties = new ArrayList<>();
            LocalDate today = LocalDate.now();
            LocalDate thirtyDaysFromNow = today.plusDays(30);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> warranties = (List<Map<String, Object>>) allMasterData.getOrDefault("warranties", new ArrayList<>());
            for (Map<String, Object> warranty : warranties) {
                Object endDateObj = warranty.get("warrantyEndDate");
                if (endDateObj != null) {
                    LocalDate endDate = null;
                    if (endDateObj instanceof LocalDate) {
                        endDate = (LocalDate) endDateObj;
                    } else if (endDateObj instanceof String) {
                        try {
                            endDate = LocalDate.parse((String) endDateObj);
                        } catch (Exception e) {
                            log.debug("Could not parse warranty end date: {}", endDateObj);
                            continue;
                        }
                    }
                    if (endDate != null) {
                        if (endDate.isBefore(today)) {
                            // Already expired
                            expiredWarranties.add(warranty);
                        } else if (endDate.isAfter(today) && (endDate.isBefore(thirtyDaysFromNow) || endDate.isEqual(thirtyDaysFromNow))) {
                            // Expiring within 30 days
                            expiringWarranties.add(warranty);
                        }
                    }
                }
            }
            attention.put("expiringWarranties", expiringWarranties);
            attention.put("expiringWarrantiesCount", expiringWarranties.size());
            attention.put("expiredWarranties", expiredWarranties);
            attention.put("expiredWarrantiesCount", expiredWarranties.size());
            
            // AMCs expiring soon (within 30 days) and expired AMCs
            List<Map<String, Object>> expiringAmcs = new ArrayList<>();
            List<Map<String, Object>> expiredAmcs = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> amcs = (List<Map<String, Object>>) allMasterData.getOrDefault("amcs", new ArrayList<>());
            for (Map<String, Object> amc : amcs) {
                Object endDateObj = amc.get("amcEndDate");
                if (endDateObj != null) {
                    LocalDate endDate = null;
                    if (endDateObj instanceof LocalDate) {
                        endDate = (LocalDate) endDateObj;
                    } else if (endDateObj instanceof String) {
                        try {
                            endDate = LocalDate.parse((String) endDateObj);
                        } catch (Exception e) {
                            log.debug("Could not parse AMC end date: {}", endDateObj);
                            continue;
                        }
                    }
                    if (endDate != null) {
                        if (endDate.isBefore(today)) {
                            // Already expired
                            expiredAmcs.add(amc);
                        } else if (endDate.isAfter(today) && (endDate.isBefore(thirtyDaysFromNow) || endDate.isEqual(thirtyDaysFromNow))) {
                            // Expiring within 30 days
                            expiringAmcs.add(amc);
                        }
                    }
                }
            }
            attention.put("expiringAmcs", expiringAmcs);
            attention.put("expiringAmcsCount", expiringAmcs.size());
            attention.put("expiredAmcs", expiredAmcs);
            attention.put("expiredAmcsCount", expiredAmcs.size());
            
            // Assets without warranty
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> assets = (List<Map<String, Object>>) allMasterData.getOrDefault("assets", new ArrayList<>());
            Set<Long> assetsWithWarranty = warranties.stream()
                    .filter(w -> w.get("assetId") != null)
                    .map(w -> {
                        Object assetId = w.get("assetId");
                        if (assetId instanceof Number) {
                            return ((Number) assetId).longValue();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<Map<String, Object>> assetsWithoutWarranty = assets.stream()
                    .filter(a -> {
                        Object assetId = a.get("assetId");
                        if (assetId instanceof Number) {
                            return !assetsWithWarranty.contains(((Number) assetId).longValue());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            attention.put("assetsWithoutWarranty", assetsWithoutWarranty);
            attention.put("assetsWithoutWarrantyCount", assetsWithoutWarranty.size());
            
            // Assets without AMC
            Set<Long> assetsWithAmc = amcs.stream()
                    .filter(a -> a.get("assetId") != null)
                    .map(a -> {
                        Object assetId = a.get("assetId");
                        if (assetId instanceof Number) {
                            return ((Number) assetId).longValue();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<Map<String, Object>> assetsWithoutAmc = assets.stream()
                    .filter(a -> {
                        Object assetId = a.get("assetId");
                        if (assetId instanceof Number) {
                            return !assetsWithAmc.contains(((Number) assetId).longValue());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            attention.put("assetsWithoutAmc", assetsWithoutAmc);
            attention.put("assetsWithoutAmcCount", assetsWithoutAmc.size());
            
            // Unassigned assets (no active user link) - filtered by userId if provided
            // For a specific user, all returned assets are already assigned to that user,
            // so unassigned assets would be empty for user-specific view.
            // For system-wide view (userId == null), this shows assets not assigned to any user
            List<Map<String, Object>> unassignedAssets;
            if (userId != null) {
                // For user-specific filtering, all assets in the result are already assigned to the user
                // So there are no unassigned assets for this user's view
                unassignedAssets = new ArrayList<>();
            } else {
                // For system-wide view: get all assets assigned to any user
                List<AssetUserLink> allLinks = linkRepo.findAll();
                Set<Long> assignedAssetIds = allLinks.stream()
                        .filter(l -> l.getAssetId() != null && l.getActive() != null && l.getActive())
                        .map(AssetUserLink::getAssetId)
                        .collect(Collectors.toSet());
                // Filter to show assets not assigned to any user
                unassignedAssets = assets.stream()
                        .filter(a -> {
                            Object assetId = a.get("assetId");
                            if (assetId instanceof Number) {
                                return !assignedAssetIds.contains(((Number) assetId).longValue());
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
            }
            attention.put("unassignedAssets", unassignedAssets);
            attention.put("unassignedAssetsCount", unassignedAssets.size());
            
            // Inactive assets
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allAssets = (List<Map<String, Object>>) allMasterData.getOrDefault("assets", new ArrayList<>());
            List<Map<String, Object>> inactiveAssets = allAssets.stream()
                    .filter(a -> {
                        Object activeObj = a.get("active");
                        return activeObj != null && Boolean.FALSE.equals(activeObj);
                    })
                    .collect(Collectors.toList());
            attention.put("inactiveAssets", inactiveAssets);
            attention.put("inactiveAssetsCount", inactiveAssets.size());
            
            // Inactive components
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allComponents = (List<Map<String, Object>>) allMasterData.getOrDefault("components", new ArrayList<>());
            List<Map<String, Object>> inactiveComponents = allComponents.stream()
                    .filter(c -> {
                        Object activeObj = c.get("active");
                        return activeObj != null && Boolean.FALSE.equals(activeObj);
                    })
                    .collect(Collectors.toList());
            attention.put("inactiveComponents", inactiveComponents);
            attention.put("inactiveComponentsCount", inactiveComponents.size());
            
            // Inactive warranties
            List<Map<String, Object>> inactiveWarranties = warranties.stream()
                    .filter(w -> {
                        Object activeObj = w.get("active");
                        return activeObj != null && Boolean.FALSE.equals(activeObj);
                    })
                    .collect(Collectors.toList());
            attention.put("inactiveWarranties", inactiveWarranties);
            attention.put("inactiveWarrantiesCount", inactiveWarranties.size());
            
            // Inactive AMCs
            List<Map<String, Object>> inactiveAmcs = amcs.stream()
                    .filter(a -> {
                        Object activeObj = a.get("active");
                        return activeObj != null && Boolean.FALSE.equals(activeObj);
                    })
                    .collect(Collectors.toList());
            attention.put("inactiveAmcs", inactiveAmcs);
            attention.put("inactiveAmcsCount", inactiveAmcs.size());
            
            // Note: Password expiry and inactive users would require integration with auth-service
            // For now, we'll add placeholders that can be populated when auth-service API is available
            // To implement: Call auth-service API to get users with:
            //   - Password expiry (if passwordExpiryDate field exists)
            //   - Account locked (accountLocked = true)
            //   - Disabled accounts (enabled = false)
            //   - Users with high failed login attempts (failedAttempts > threshold)
            List<Map<String, Object>> usersNeedingAttention = new ArrayList<>();
            // For now, we can't check password expiry or account status without auth-service integration
            // This would require:
            // 1. HTTP client to call auth-service API
            // 2. Or shared database access to User and UserDetailMaster tables
            // Placeholder for future implementation
            attention.put("usersNeedingAttention", usersNeedingAttention);
            attention.put("usersNeedingAttentionCount", usersNeedingAttention.size());
            attention.put("passwordExpiryNote", "Password expiry tracking requires auth-service integration. " +
                    "To implement: Add passwordExpiryDate field in auth-service UserDetailMaster and create API endpoint to query users with expiring/expired passwords.");
            
            // Build final result - include all master data plus attention indicators
            result.putAll(allMasterData);
            result.put("summary", summary);
            result.put("attention", attention);
            
            // Update audit information
            @SuppressWarnings("unchecked")
            Map<String, Object> auditInfo = (Map<String, Object>) result.getOrDefault("audit", new LinkedHashMap<>());
            auditInfo.put("requestType", "NEED_YOUR_ATTENTION");
            if (userId != null) {
                auditInfo.put("filteredForUserId", userId);
            }
            result.put("audit", auditInfo);
            
            log.info("✅ Need Your Attention data retrieved successfully for userId: {} - Summary: {}, Attention Items: {} warranties expiring, {} warranties expired, {} AMCs expiring, {} AMCs expired, {} assets without warranty, {} assets without AMC, {} inactive assets, {} inactive components, {} inactive warranties, {} inactive AMCs, {} unassigned assets", 
                    userId, summary, expiringWarranties.size(), expiredWarranties.size(), 
                    expiringAmcs.size(), expiredAmcs.size(), assetsWithoutWarranty.size(), 
                    assetsWithoutAmc.size(), inactiveAssets.size(), inactiveComponents.size(),
                    inactiveWarranties.size(), inactiveAmcs.size(), unassignedAssets.size());
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve Need Your Attention data: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}




