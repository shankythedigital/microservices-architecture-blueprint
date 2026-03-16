
package com.example.asset.service;

import com.example.asset.dto.AssetWarrantyDto;
import com.example.asset.dto.AssetWarrantyRequest;
import com.example.asset.dto.DocumentRequest;
import com.example.asset.entity.AssetDocument;
import com.example.asset.entity.AssetMaster;
import com.example.asset.entity.AssetWarranty;
import com.example.asset.mapper.AssetWarrantyMapper;
import com.example.asset.repository.AssetComponentRepository;
import com.example.asset.repository.AssetDocumentRepository;
import com.example.asset.repository.AssetMasterRepository;
import com.example.asset.repository.AssetWarrantyRepository;
import com.example.common.service.SafeNotificationHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ✅ AssetWarrantyService
 * Handles all warranty operations (create, update, delete, list)
 * and manages document linkage & notifications.
 */
@Service
public class AssetWarrantyService {

    private static final Logger log = LoggerFactory.getLogger(AssetWarrantyService.class);

    private final AssetWarrantyRepository warrantyRepo;
    private final AssetMasterRepository assetRepo;
    private final DocumentService documentService;
    private final SafeNotificationHelper notificationHelper;
    private final DocumentTypeMasterService documentTypeMasterService;

    private final AssetComponentRepository componentRepo;
    private final AssetDocumentRepository documentRepo;

    public AssetWarrantyService(
            AssetWarrantyRepository warrantyRepo,
            AssetMasterRepository assetRepo,
            AssetDocumentRepository documentRepo,
            AssetComponentRepository componentRepo,
            DocumentService documentService,
            SafeNotificationHelper notificationHelper,
            DocumentTypeMasterService documentTypeMasterService) {
        this.warrantyRepo = warrantyRepo;
        this.assetRepo = assetRepo;
        this.documentRepo = documentRepo;
        this.componentRepo = componentRepo;
        this.documentService = documentService;
        this.notificationHelper = notificationHelper;
        this.documentTypeMasterService = documentTypeMasterService;
    }

    // ============================================================
    // 🟢 CREATE WARRANTY
    // ============================================================
    
    @Transactional
    public AssetWarrantyDto create(HttpHeaders headers, AssetWarrantyRequest request, MultipartFile file) {
        validateRequest(request);

        // Fetch validated asset directly
        AssetMaster asset = assetRepo.findById(request.getAssetId()).get();

        AssetWarranty warranty = new AssetWarranty();
        warranty.setAsset(asset);
        warranty.setWarrantyStatus(request.getWarrantyStatus());
        warranty.setWarrantyProvider(request.getWarrantyProvider());
        warranty.setWarrantyTerms(request.getWarrantyTerms());
        warranty.setWarrantyStartDate(LocalDate.parse(request.getStartDate()));
        warranty.setWarrantyEndDate(LocalDate.parse(request.getEndDate()));
        warranty.setUserId(request.getUserId());
        warranty.setUsername(request.getUsername());
        warranty.setDocumentId(request.getDocumentId());
        warranty.setComponentId(request.getComponentId());
        warranty.setActive(true);
        warranty.setCreatedBy(request.getUsername());
        warranty.setUpdatedBy(request.getUsername());

        // ✅ Duplicate check: only one active warranty per asset
        Long assetId = asset.getAssetId();
        if (assetId != null && warrantyRepo.existsByAsset_AssetIdAndActiveTrue(assetId)) {
            throw new RuntimeException("❌ An active warranty already exists for this asset (assetId: " + assetId + "). Deactivate the existing one or use update.");
        }

        AssetWarranty saved = warrantyRepo.save(warranty);

        if (file != null && !file.isEmpty()) {
            String docType = (request.getDocType() != null && !request.getDocType().isBlank())
                    ? request.getDocType().trim() : "pdf";
            documentTypeMasterService.validate(docType);
            DocumentRequest docReq = buildDocumentRequest(request, docType);
            docReq.setEntityType("WARRANTY");
            docReq.setEntityId(saved.getWarrantyId());
            AssetDocument savedDoc = documentService.upload(headers, file, docReq);
            saved.setDocument(savedDoc);
            saved = warrantyRepo.save(saved);
        }

        log.info("✅ Warranty created successfully (ID={}) for assetId={}", saved.getWarrantyId(), asset.getAssetId());

        sendNotification(headers, request, "WARRANTY_CREATED_INAPP",
                Map.of("warrantyId", saved.getWarrantyId(), "assetId", asset.getAssetId()));

        return AssetWarrantyMapper.toDto(saved);
    }

    // ============================================================
    // ✏️ UPDATE WARRANTY
    // ============================================================
    @Transactional
    public AssetWarrantyDto update(HttpHeaders headers, Long id, AssetWarrantyRequest request, MultipartFile file) {
        validateRequest(request);

        AssetWarranty warranty = warrantyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("❌ Warranty not found with ID: " + id));

        warranty.setWarrantyStatus(request.getWarrantyStatus());
        warranty.setWarrantyProvider(request.getWarrantyProvider());
        warranty.setWarrantyTerms(request.getWarrantyTerms());
        warranty.setWarrantyStartDate(LocalDate.parse(request.getStartDate()));
        warranty.setWarrantyEndDate(LocalDate.parse(request.getEndDate()));
        warranty.setUpdatedBy(request.getUsername());
        warranty.setDocumentId(request.getDocumentId());
        warranty.setComponentId(request.getComponentId());

        // ✅ Replace or add document
        if (file != null && !file.isEmpty()) {
            String docType = (request.getDocType() != null && !request.getDocType().isBlank())
                    ? request.getDocType().trim() : "pdf";
            documentTypeMasterService.validate(docType);
            DocumentRequest docReq = buildDocumentRequest(request, docType);
            docReq.setEntityType("WARRANTY");
            docReq.setEntityId(id);
            AssetDocument newDoc = documentService.upload(headers, file, docReq);
            warranty.setDocument(newDoc);
        }

        AssetWarranty updated = warrantyRepo.save(warranty);
        log.info("✏️ Warranty updated successfully (ID={}) by user={}", id, request.getUsername());

        sendNotification(headers, request, "WARRANTY_UPDATED_INAPP",
                Map.of("warrantyId", id, "actor", request.getUsername()));

        return AssetWarrantyMapper.toDto(updated);
    }

    // ============================================================
    // ❌ SOFT DELETE WARRANTY
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, AssetWarrantyRequest request) {
        warrantyRepo.findById(id).ifPresent(warranty -> {
            warranty.setActive(false);
            warranty.setUpdatedBy(request.getUsername());
            warrantyRepo.save(warranty);
            log.info("🗑️ Warranty soft-deleted (ID={}) by user={}", id, request.getUsername());

            sendNotification(headers, request, "WARRANTY_DELETED_INAPP",
                    Map.of("warrantyId", id, "actor", request.getUsername()));
        });
    }

    // ============================================================
    // 📋 LIST & FIND
    // ============================================================
    @Transactional(readOnly = true)
    public List<AssetWarrantyDto> list() {
        return warrantyRepo.findAll().stream()
                .filter(w -> w.getActive() == null || w.getActive())
                .map(AssetWarrantyMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AssetWarrantyDto> find(Long id) {
        return warrantyRepo.findById(id)
                .filter(w -> w.getActive() == null || w.getActive())
                .map(AssetWarrantyMapper::toDto);
    }

    // ============================================================
    // 🧩 HELPER METHODS
    // ============================================================

    private DocumentRequest buildDocumentRequest(AssetWarrantyRequest request, String docType) {
        DocumentRequest docReq = new DocumentRequest();
        docReq.setUserId(request.getUserId());
        docReq.setUsername(request.getUsername());
        docReq.setProjectType(request.getProjectType());
        docReq.setAssetId(request.getAssetId());
        docReq.setComponentId(request.getComponentId());
        docReq.setDocType(docType);
        return docReq;
    }

    private void sendNotification(HttpHeaders headers, AssetWarrantyRequest request,
            String templateCode, Map<String, Object> placeholders) {
        try {
            String bearer = headers.getFirst("Authorization");
            notificationHelper.safeNotifyAsync(
                    bearer,
                    request.getUserId(),
                    request.getUsername(),
                    null, null,
                    "INAPP",
                    templateCode,
                    placeholders,
                    Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE"));
        } catch (Exception e) {
            log.warn("⚠️ Notification [{}] failed: {}", templateCode, e.getMessage());
        }
    }

    private void validateRequest(AssetWarrantyRequest req) {
        if (req == null)
            throw new IllegalArgumentException("❌ Warranty request cannot be null");

        if (!StringUtils.hasText(req.getWarrantyStatus()))
            throw new IllegalArgumentException("❌ Warranty status cannot be blank");

        if (!StringUtils.hasText(req.getStartDate()) || !StringUtils.hasText(req.getEndDate()))
            throw new IllegalArgumentException("❌ Warranty start and end dates are required");

        if (req.getAssetId() == null)
            throw new IllegalArgumentException("❌ Asset ID is required for warranty");

        // ✅ Validate Asset existence
        if (!assetRepo.existsById(req.getAssetId())) {
            throw new IllegalArgumentException("❌ Invalid Asset ID: " + req.getAssetId());
        }

        // ✅ Validate Component if provided
        if (req.getComponentId() != null && !componentRepo.existsById(req.getComponentId())) {
            throw new IllegalArgumentException("❌ Invalid Component ID: " + req.getComponentId());
        }

        // ✅ Validate Document if provided
        if (req.getDocumentId() != null && !documentRepo.existsById(req.getDocumentId())) {
            throw new IllegalArgumentException("❌ Invalid Document ID: " + req.getDocumentId());
        }
    }

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for a warranty (accessible to all authenticated users)
     */
    @Transactional
    public AssetWarrantyDto updateFavourite(HttpHeaders headers, Long id, Boolean isFavourite) {
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return warrantyRepo.findById(id).map(existing -> {
            existing.setIsFavourite(isFavourite != null ? isFavourite : false);
            existing.setUpdatedBy(username);
            AssetWarranty saved = warrantyRepo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "warrantyId", saved.getWarrantyId(),
                    "isFavourite", saved.getIsFavourite(),
                    "actor", username,
                    "timestamp", java.time.Instant.now().toString()
            );

            notificationHelper.safeNotifyAsync(
                    headers.getFirst("Authorization"),
                    userId, username, null, null,
                    "INAPP", "WARRANTY_FAVOURITE_UPDATED_INAPP",
                    placeholders, projectType);
            log.info("⭐ Warranty favourite updated: id={} isFavourite={} by={}", id, isFavourite, username);

            return AssetWarrantyMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Warranty not found with id: " + id));
    }

    /**
     * Toggle most like status for a warranty (accessible to all authenticated users)
     */
    @Transactional
    public AssetWarrantyDto updateMostLike(HttpHeaders headers, Long id, Boolean isMostLike) {
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return warrantyRepo.findById(id).map(existing -> {
            existing.setIsMostLike(isMostLike != null ? isMostLike : false);
            existing.setUpdatedBy(username);
            AssetWarranty saved = warrantyRepo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "warrantyId", saved.getWarrantyId(),
                    "isMostLike", saved.getIsMostLike(),
                    "actor", username,
                    "timestamp", java.time.Instant.now().toString()
            );

            notificationHelper.safeNotifyAsync(
                    headers.getFirst("Authorization"),
                    userId, username, null, null,
                    "INAPP", "WARRANTY_MOST_LIKE_UPDATED_INAPP",
                    placeholders, projectType);
            log.info("⭐ Warranty most like updated: id={} isMostLike={} by={}", id, isMostLike, username);

            return AssetWarrantyMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Warranty not found with id: " + id));
    }

    /**
     * Update sequence order for a warranty (admin only)
     */
    @Transactional
    public AssetWarrantyDto updateSequenceOrder(HttpHeaders headers, Long id, Integer sequenceOrder) {
        // Check if user is admin
        if (!com.example.asset.util.JwtUtil.isAdmin()) {
            throw new RuntimeException("Access denied: Only admins can update sequence order");
        }

        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return warrantyRepo.findById(id).map(existing -> {
            existing.setSequenceOrder(sequenceOrder);
            existing.setUpdatedBy(username);
            AssetWarranty saved = warrantyRepo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "warrantyId", saved.getWarrantyId(),
                    "sequenceOrder", saved.getSequenceOrder() != null ? saved.getSequenceOrder() : 0,
                    "actor", username,
                    "timestamp", java.time.Instant.now().toString()
            );

            notificationHelper.safeNotifyAsync(
                    headers.getFirst("Authorization"),
                    userId, username, null, null,
                    "INAPP", "WARRANTY_SEQUENCE_UPDATED_INAPP",
                    placeholders, projectType);
            log.info("📊 Warranty sequence order updated: id={} sequenceOrder={} by={}", id, sequenceOrder, username);

            return AssetWarrantyMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Warranty not found with id: " + id));
    }
}




