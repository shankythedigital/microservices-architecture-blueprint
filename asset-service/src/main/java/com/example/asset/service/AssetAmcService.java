
package com.example.asset.service;

import com.example.asset.dto.AssetAmcDto;
import com.example.asset.dto.AssetAmcRequest;
import com.example.asset.dto.DocumentRequest;
import com.example.asset.entity.AssetAmc;
import com.example.asset.entity.AssetDocument;
import com.example.asset.entity.AssetMaster;
import com.example.asset.mapper.AssetAmcMapper;
import com.example.asset.repository.AssetAmcRepository;
import com.example.asset.repository.AssetComponentRepository;
import com.example.asset.repository.AssetDocumentRepository;
import com.example.asset.repository.AssetMasterRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.List;
import java.util.Optional;

@Service
public class AssetAmcService {

    private static final Logger log = LoggerFactory.getLogger(AssetAmcService.class);

    private final AssetAmcRepository amcRepo;
    private final AssetMasterRepository assetRepo;
    private final AssetDocumentRepository documentRepo;
    private final AssetComponentRepository componentRepo;
    private final DocumentService documentService;
    private final SafeNotificationHelper notificationHelper;

       public AssetAmcService(
            AssetAmcRepository amcRepo,
            AssetMasterRepository assetRepo,
            AssetDocumentRepository documentRepo,
            AssetComponentRepository componentRepo,
            DocumentService documentService,
            SafeNotificationHelper notificationHelper) {
        this.amcRepo = amcRepo;
        this.assetRepo = assetRepo;
        this.documentRepo = documentRepo;
        this.componentRepo = componentRepo;
        this.documentService = documentService;
        this.notificationHelper = notificationHelper;
    }

    // ============================================================
    // 🟢 CREATE AMC
    // ============================================================
    @Transactional
    public AssetAmcDto create(HttpHeaders headers, AssetAmcRequest request, MultipartFile file) {
        validateRequest(request);

        AssetMaster asset = assetRepo.findById(request.getAssetId())
                .orElseThrow(() -> new IllegalArgumentException("❌ Asset not found for ID: " + request.getAssetId()));

        AssetAmc amc = new AssetAmc();
        amc.setAmcStatus(request.getAmcStatus());
        amc.setStartDate(request.getStartDate());
        amc.setEndDate(request.getEndDate());
        amc.setAsset(asset);
        amc.setDocumentId(request.getDocumentId());
        amc.setComponentId(request.getComponentId());
        amc.setUserId(request.getUserId());
        amc.setUsername(request.getUsername());
        amc.setCreatedBy(request.getUsername());
        amc.setUpdatedBy(request.getUsername());
        amc.setActive(true);

        // ✅ Upload Document (if present)
        if (file != null && !file.isEmpty()) {
            DocumentRequest docReq = buildDocumentRequest(request, "AMC_DOCUMENT");
            AssetDocument doc = documentService.upload(headers, file, docReq);
            amc.setDocument(doc);
        }

        AssetAmc saved = amcRepo.save(amc);
        log.info("✅ AMC created successfully (ID={}) for assetId={}", saved.getAmcId(), asset.getAssetId());

        sendNotification(headers, request, "AMC_CREATED_INAPP",
                Map.of("amcId", saved.getAmcId(), "assetId", asset.getAssetId()));

        return AssetAmcMapper.toDto(saved);
    }

    // ============================================================
    // ✏️ UPDATE AMC
    // ============================================================
    @Transactional
    public AssetAmcDto update(HttpHeaders headers, Long id, AssetAmcRequest request, MultipartFile file) {
        validateRequest(request);

        AssetAmc existing = amcRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("❌ AMC not found for ID: " + id));

        existing.setAmcStatus(request.getAmcStatus());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setUpdatedBy(request.getUsername());

        // ✅ Replace or add document
        if (file != null && !file.isEmpty()) {
            DocumentRequest docReq = buildDocumentRequest(request, "AMC_DOCUMENT");
            AssetDocument newDoc = documentService.upload(headers, file, docReq);
            existing.setDocument(newDoc);
        }

        AssetAmc updated = amcRepo.save(existing);
        log.info("✏️ AMC updated successfully (ID={}) by user={}", id, request.getUsername());

        sendNotification(headers, request, "AMC_UPDATED_INAPP",
                Map.of("amcId", id, "assetId", existing.getAsset().getAssetId()));

        return AssetAmcMapper.toDto(updated);
    }

    
    // ============================================================
    // ❌ SOFT DELETE AMC
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, AssetAmcRequest request) {
        amcRepo.findById(id).ifPresent(amc -> {
            amc.setActive(false);
            amc.setUpdatedBy(request.getUsername());
            amcRepo.save(amc);
            log.info("🗑️ AMC soft-deleted (ID={}) by user={}", id, request.getUsername());

            sendNotification(headers, request, "AMC_DELETED_INAPP",
                    Map.of("amcId", id, "actor", request.getUsername()));
        });
    }

    // ============================================================
    // 📢 NOTIFICATION HELPER
    // ============================================================
    private void sendNotification(HttpHeaders headers, AssetAmcRequest request,
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
                    request.getProjectType()
            );
        } catch (Exception e) {
            log.error("⚠️ Notification failed [{}]: {}", templateCode, e.getMessage());
        }
    }


     // ============================================================
    // 📋 LIST & FIND
    // ============================================================
    public List<AssetAmcDto> list() {
        return amcRepo.findAll().stream()
                .filter(a -> a.getActive() == null || a.getActive())
                .map(AssetAmcMapper::toDto)
                .toList();
    }

    public Optional<AssetAmcDto> find(Long id) {
        return amcRepo.findById(id)
                .filter(a -> a.getActive() == null || a.getActive())
                .map(AssetAmcMapper::toDto);
    }



    // ============================================================
    // 🧩 VALIDATION & HELPERS
    // ============================================================
    private void validateRequest(AssetAmcRequest req) {
        if (req == null)
            throw new IllegalArgumentException("❌ AMC request cannot be null");
        if (req.getAssetId() == null)
            throw new IllegalArgumentException("❌ Asset ID is required");
        if (!assetRepo.existsById(req.getAssetId()))
            throw new IllegalArgumentException("❌ Invalid Asset ID: " + req.getAssetId());

        // ✅ Validate Component (optional)
        if (req.getComponentId() != null && !componentRepo.existsById(req.getComponentId()))
            throw new IllegalArgumentException("❌ Invalid Component ID: " + req.getComponentId());

        // ✅ Validate Document (optional)
        if (req.getDocumentId() != null && !documentRepo.existsById(req.getDocumentId()))
            throw new IllegalArgumentException("❌ Invalid Document ID: " + req.getDocumentId());
    }

    private DocumentRequest buildDocumentRequest(AssetAmcRequest req, String docType) {
        DocumentRequest docReq = new DocumentRequest();
        docReq.setUserId(req.getUserId());
        docReq.setUsername(req.getUsername());
        docReq.setProjectType(req.getProjectType());
        docReq.setEntityType("AMC");
        docReq.setEntityId(req.getAssetId());
        docReq.setDocType(docType);
        return docReq;
    }

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for an AMC (accessible to all authenticated users)
     */
    @Transactional
    public AssetAmcDto updateFavourite(HttpHeaders headers, Long id, Boolean isFavourite) {
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return amcRepo.findById(id).map(existing -> {
            existing.setIsFavourite(isFavourite != null ? isFavourite : false);
            existing.setUpdatedBy(username);
            AssetAmc saved = amcRepo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "amcId", saved.getAmcId(),
                    "isFavourite", saved.getIsFavourite(),
                    "actor", username,
                    "timestamp", java.time.Instant.now().toString()
            );

            notificationHelper.safeNotifyAsync(
                    headers.getFirst("Authorization"),
                    userId, username, null, null,
                    "INAPP", "AMC_FAVOURITE_UPDATED_INAPP",
                    placeholders, projectType);
            log.info("⭐ AMC favourite updated: id={} isFavourite={} by={}", id, isFavourite, username);

            return AssetAmcMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("AMC not found with id: " + id));
    }

    /**
     * Toggle most like status for an AMC (accessible to all authenticated users)
     */
    @Transactional
    public AssetAmcDto updateMostLike(HttpHeaders headers, Long id, Boolean isMostLike) {
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return amcRepo.findById(id).map(existing -> {
            existing.setIsMostLike(isMostLike != null ? isMostLike : false);
            existing.setUpdatedBy(username);
            AssetAmc saved = amcRepo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "amcId", saved.getAmcId(),
                    "isMostLike", saved.getIsMostLike(),
                    "actor", username,
                    "timestamp", java.time.Instant.now().toString()
            );

            notificationHelper.safeNotifyAsync(
                    headers.getFirst("Authorization"),
                    userId, username, null, null,
                    "INAPP", "AMC_MOST_LIKE_UPDATED_INAPP",
                    placeholders, projectType);
            log.info("⭐ AMC most like updated: id={} isMostLike={} by={}", id, isMostLike, username);

            return AssetAmcMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("AMC not found with id: " + id));
    }

    /**
     * Update sequence order for an AMC (admin only)
     */
    @Transactional
    public AssetAmcDto updateSequenceOrder(HttpHeaders headers, Long id, Integer sequenceOrder) {
        // Check if user is admin
        if (!com.example.asset.util.JwtUtil.isAdmin()) {
            throw new RuntimeException("Access denied: Only admins can update sequence order");
        }

        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return amcRepo.findById(id).map(existing -> {
            existing.setSequenceOrder(sequenceOrder);
            existing.setUpdatedBy(username);
            AssetAmc saved = amcRepo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "amcId", saved.getAmcId(),
                    "sequenceOrder", saved.getSequenceOrder() != null ? saved.getSequenceOrder() : 0,
                    "actor", username,
                    "timestamp", java.time.Instant.now().toString()
            );

            notificationHelper.safeNotifyAsync(
                    headers.getFirst("Authorization"),
                    userId, username, null, null,
                    "INAPP", "AMC_SEQUENCE_UPDATED_INAPP",
                    placeholders, projectType);
            log.info("📊 AMC sequence order updated: id={} sequenceOrder={} by={}", id, sequenceOrder, username);

            return AssetAmcMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("AMC not found with id: " + id));
    }
}


