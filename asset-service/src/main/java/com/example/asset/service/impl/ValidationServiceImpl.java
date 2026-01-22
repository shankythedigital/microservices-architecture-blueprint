package com.example.asset.service.impl;

import com.example.asset.dto.AssetUserUniversalLinkRequest;
import com.example.asset.repository.*;
import com.example.asset.service.ValidationService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ValidationServiceImpl implements ValidationService {

    private final AssetMasterRepository assetRepo;
    private final AssetComponentRepository componentRepo;
    private final ProductModelRepository modelRepo;
    private final ProductMakeRepository makeRepo;
    private final AssetWarrantyRepository warrantyRepo;
    private final AssetAmcRepository amcRepo;
    private final AssetDocumentRepository documentRepo;
    private final AssetUserLinkRepository linkRepo;

    public ValidationServiceImpl(
            AssetMasterRepository assetRepo,
            AssetComponentRepository componentRepo,
            ProductModelRepository modelRepo,
            ProductMakeRepository makeRepo,
            AssetWarrantyRepository warrantyRepo,
            AssetAmcRepository amcRepo,
            AssetDocumentRepository documentRepo,
            AssetUserLinkRepository linkRepo) {
        this.assetRepo = assetRepo;
        this.componentRepo = componentRepo;
        this.modelRepo = modelRepo;
        this.makeRepo = makeRepo;
        this.warrantyRepo = warrantyRepo;
        this.amcRepo = amcRepo;
        this.documentRepo = documentRepo;
        this.linkRepo = linkRepo;
    }

    // Entry: validate the unified request (used by controller before calling service)
    @Override
    public void validateLinkRequest(AssetUserUniversalLinkRequest req) {
        if (req == null) throw new IllegalArgumentException("Request missing");
        if (req.getEntityType() == null || req.getEntityType().isBlank())
            throw new IllegalArgumentException("Entity type is missing");
        if (req.getEntityId() == null)
            throw new IllegalArgumentException("Entity ID is missing");
        if (req.getTargetUserId() == null || req.getTargetUsername() == null)
            throw new IllegalArgumentException("Target user details missing");

        // ensure source entity exists
        ensureEntityExists(req.getEntityType(), req.getEntityId());

        // fail-fast if already linked (to someone)
        if (isAlreadyLinked(req.getEntityType(), req.getEntityId())) {
            throw new IllegalStateException(req.getEntityType().toUpperCase() +
                    " is already linked to another user");
        }
    }

    @Override
    public void ensureEntityExists(String type, Long id) {
        if (type == null || id == null) throw new IllegalArgumentException("Invalid parameters");
        switch (type.trim().toUpperCase()) {
            case "ASSET":
                if (!assetRepo.existsById(id)) throw new IllegalArgumentException("Asset not found: " + id);
                return;
            case "COMPONENT":
                if (!componentRepo.existsById(id)) throw new IllegalArgumentException("Component not found: " + id);
                return;
            case "MODEL":
                if (!modelRepo.existsById(id)) throw new IllegalArgumentException("Model not found: " + id);
                return;
            case "MAKE":
                if (!makeRepo.existsById(id)) throw new IllegalArgumentException("Make not found: " + id);
                return;
            case "WARRANTY":
                if (!warrantyRepo.existsById(id)) throw new IllegalArgumentException("Warranty not found: " + id);
                return;
            case "AMC":
                if (!amcRepo.existsById(id)) throw new IllegalArgumentException("AMC not found: " + id);
                return;
            case "DOCUMENT":
                if (!documentRepo.existsById(id)) throw new IllegalArgumentException("Document not found: " + id);
                return;
            default:
                throw new IllegalArgumentException("Unknown entity type: " + type);
        }
    }

    /**
     * Checks if the given entity is already linked to ANY user (not a specific user).
     */
    @Override
    public boolean isAlreadyLinked(String type, Long id) {
        if (type == null || id == null) return false;
        switch (type.trim().toUpperCase()) {
            case "ASSET":
                return linkRepo.existsByAssetIdAndActiveTrue(id);
            case "COMPONENT":
                return linkRepo.existsByComponentIdAndActiveTrue(id);
            case "MODEL":
                // If any asset for this model is linked
                return assetRepo.findAll().stream()
                        .filter(a -> a.getModel() != null && Objects.equals(a.getModel().getModelId(), id))
                        .anyMatch(a -> linkRepo.existsByAssetIdAndActiveTrue(a.getAssetId()));
            case "MAKE":
                return assetRepo.findAll().stream()
                        .filter(a -> a.getMake() != null && Objects.equals(a.getMake().getMakeId(), id))
                        .anyMatch(a -> linkRepo.existsByAssetIdAndActiveTrue(a.getAssetId()));
            case "WARRANTY":
                // warranty linked check implemented in repo if available; fall back to lookup
                return warrantyRepo.existsByAsset_AssetIdAndActiveTrue(id);
            case "AMC":
                return amcRepo.existsByAsset_AssetIdAndActiveTrue(id);
            case "DOCUMENT":
                return documentRepo.findById(id)
                        .map(d -> d.getAsset() != null && linkRepo.existsByAssetIdAndActiveTrue(d.getAsset().getAssetId()))
                        .orElse(false);
            default:
                return false;
        }
    }

    /**
     * Checks whether the given entity is linked to the provided user.
     * Used for delink validation and "already exists" check for SAME user.
     */
    @Override
    public boolean isAlreadyLinkedToUser(String entityType, Long entityId, Long userId) {
        if (entityType == null || entityId == null || userId == null) return false;
        switch (entityType.trim().toUpperCase()) {
            case "ASSET":
                return linkRepo.findByAssetIdAndUserIdAndActiveTrue(entityId, userId).isPresent();
            case "COMPONENT":
                return linkRepo.findByComponentIdAndUserIdAndActiveTrue(entityId, userId).isPresent();
            case "MODEL":
                return assetRepo.findAll().stream()
                        .filter(a -> a.getModel() != null && Objects.equals(a.getModel().getModelId(), entityId))
                        .anyMatch(a -> linkRepo.existsByAssetIdAndUserIdAndActiveTrue(a.getAssetId(), userId));
            case "MAKE":
                return assetRepo.findAll().stream()
                        .filter(a -> a.getMake() != null && Objects.equals(a.getMake().getMakeId(), entityId))
                        .anyMatch(a -> linkRepo.existsByAssetIdAndUserIdAndActiveTrue(a.getAssetId(), userId));
            case "AMC":
                return amcRepo.findById(entityId)
                        .map(amc -> amc.getAsset() != null && linkRepo.existsByAssetIdAndUserIdAndActiveTrue(amc.getAsset().getAssetId(), userId))
                        .orElse(false);
            case "WARRANTY":
                return warrantyRepo.findById(entityId)
                        .map(w -> w.getAsset() != null && linkRepo.existsByAssetIdAndUserIdAndActiveTrue(w.getAsset().getAssetId(), userId))
                        .orElse(false);
            case "DOCUMENT":
                return documentRepo.findById(entityId)
                        .map(d -> d.getAsset() != null && linkRepo.existsByAssetIdAndUserIdAndActiveTrue(d.getAsset().getAssetId(), userId))
                        .orElse(false);
            default:
                return false;
        }
    }

    @Override
    public void validateLinkRequestSingle(String entityType, Long entityId, Long targetUserId, String targetUsername) {
        if (entityType == null || entityType.isBlank()) throw new IllegalArgumentException("Entity type missing");
        if (entityId == null) throw new IllegalArgumentException("Entity ID missing");
        if (targetUserId == null || targetUsername == null) throw new IllegalArgumentException("Target user missing");

        ensureEntityExists(entityType, entityId);

        // If already linked to the same user -> caller expects "already exist" handling,
        // but validation for preventing linking to different user:
        if (isAlreadyLinked(entityType, entityId)) {
            throw new IllegalStateException(entityType.toUpperCase() + " already linked");
        }
    }

    @Override
    public void validateDelinkRequestSingle(String entityType, Long entityId, Long targetUserId) {
        if (entityType == null || entityType.isBlank()) throw new IllegalArgumentException("Entity type missing");
        if (entityId == null) throw new IllegalArgumentException("Entity ID missing");
        if (targetUserId == null) throw new IllegalArgumentException("Target user missing");

        ensureEntityExists(entityType, entityId);

        if (!isAlreadyLinkedToUser(entityType, entityId, targetUserId)) {
            throw new IllegalStateException(entityType.toUpperCase() + " not linked to this user");
        }
    }

    @Override
    public void ensureEntityLinked(String type, Long entityId, Long targetUserId) {
        // used by controller prior to delink
        boolean linked;
        switch (type.trim().toUpperCase()) {
            case "ASSET":
                linked = linkRepo.findByAssetIdAndUserIdAndActiveTrue(entityId, targetUserId).isPresent();
                break;
            case "COMPONENT":
                linked = linkRepo.findByComponentIdAndUserIdAndActiveTrue(entityId, targetUserId).isPresent();
                break;
            default:
                // For MODEL/MAKE/AMC/WARRANTY/DOCUMENT we accept delink (service will handle specifics)
                linked = true;
        }
        if (!linked) {
            throw new IllegalStateException("No active link exists for " + type + " with ID " + entityId + " and user " + targetUserId);
        }
    }
}


