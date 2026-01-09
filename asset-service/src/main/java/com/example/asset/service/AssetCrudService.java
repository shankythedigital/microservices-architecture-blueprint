
// // // // package com.example.asset.service;

// // // // import com.example.asset.dto.AssetRequest;
// // // // import com.example.asset.entity.AssetMaster;
// // // // import com.example.asset.entity.AssetUserLink;
// // // // import com.example.asset.repository.AssetMasterRepository;
// // // // import com.example.asset.repository.AssetUserLinkRepository;
// // // // import com.example.common.service.SafeNotificationHelper;
// // // // import org.slf4j.Logger;
// // // // import org.slf4j.LoggerFactory;
// // // // import org.springframework.data.domain.*;
// // // // import org.springframework.http.HttpHeaders;
// // // // import org.springframework.stereotype.Service;
// // // // import org.springframework.transaction.annotation.Transactional;
// // // // import org.springframework.util.StringUtils;

// // // // import java.time.Instant;
// // // // import java.util.*;

// // // // /**
// // // //  * ✅ AssetCrudService
// // // //  * Handles CRUD for assets and triggers SafeNotificationHelper notifications.
// // // //  * Extracts Bearer token directly from HttpHeaders.
// // // //  */
// // // // @Service
// // // // public class AssetCrudService {

// // // //     private static final Logger log = LoggerFactory.getLogger(AssetCrudService.class);

// // // //     private final AssetMasterRepository assetRepo;
// // // //     private final AssetUserLinkRepository linkRepo;
// // // //     private final SafeNotificationHelper safeNotificationHelper;

// // // //     public AssetCrudService(AssetMasterRepository assetRepo,
// // // //                             AssetUserLinkRepository linkRepo,
// // // //                             SafeNotificationHelper safeNotificationHelper) {
// // // //         this.assetRepo = assetRepo;
// // // //         this.linkRepo = linkRepo;
// // // //         this.safeNotificationHelper = safeNotificationHelper;
// // // //     }

// // // //     // ============================================================
// // // //     // 🟢 CREATE ASSET
// // // //     // ============================================================
// // // //     @Transactional
// // // //     public AssetMaster create(HttpHeaders headers, AssetRequest request) {
// // // //         if (request == null || request.getAsset() == null)
// // // //             throw new IllegalArgumentException("AssetRequest or payload cannot be null");

// // // //         String bearer = extractBearer(headers);
// // // //         AssetMaster asset = request.getAsset();
// // // //         String username = request.getUsername();
// // // //         Long userId = request.getUserId();
// // // //         String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

// // // //         if (!StringUtils.hasText(asset.getAssetNameUdv()))
// // // //             throw new RuntimeException("❌ Asset name cannot be blank");
// // // //         if (assetRepo.existsByAssetNameUdv(asset.getAssetNameUdv()))
// // // //             throw new RuntimeException("❌ Asset with name '" + asset.getAssetNameUdv() + "' already exists");

// // // //         asset.setCreatedBy(username);
// // // //         asset.setUpdatedBy(username);
// // // //         AssetMaster saved = assetRepo.save(asset);

// // // //         if (userId != null && username != null) {
// // // //             AssetUserLink link = new AssetUserLink();
// // // //             link.setAsset(saved);
// // // //             link.setUserId(String.valueOf(userId));
// // // //             link.setUsername(username);
// // // //             linkRepo.save(link);
// // // //         }

// // // //         Map<String, Object> placeholders = Map.of(
// // // //                 "assetId", saved.getAssetId(),
// // // //                 "assetName", saved.getAssetNameUdv(),
// // // //                 "assignedTo", username,
// // // //                 "username", username,
// // // //                 "timestamp", Instant.now().toString()
// // // //         );

// // // //         sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_CREATED_INAPP", placeholders, projectType);
// // // //         sendAssetNotification(bearer, userId, username, "EMAIL", "ASSET_CREATED_EMAIL", placeholders, projectType);

// // // //         log.info("✅ Asset created successfully: id={} name={} by={}", saved.getAssetId(), saved.getAssetNameUdv(), username);
// // // //         return saved;
// // // //     }

// // // //     // ============================================================
// // // //     // ✏️ UPDATE ASSET
// // // //     // ============================================================
// // // //     @Transactional
// // // //     public AssetMaster update(HttpHeaders headers, Long id, AssetRequest request) {
// // // //         if (request == null || request.getAsset() == null)
// // // //             throw new IllegalArgumentException("AssetRequest or payload cannot be null");

// // // //         String bearer = extractBearer(headers);
// // // //         AssetMaster patch = request.getAsset();
// // // //         String username = request.getUsername();
// // // //         Long userId = request.getUserId();
// // // //         String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

// // // //         return assetRepo.findById(id).map(existing -> {
// // // //             String newName = patch.getAssetNameUdv();

// // // //             if (!existing.getAssetNameUdv().equalsIgnoreCase(newName)
// // // //                     && assetRepo.existsByAssetNameUdv(newName))
// // // //                 throw new RuntimeException("❌ Asset with name '" + newName + "' already exists");

// // // //             existing.setAssetNameUdv(newName);
// // // //             existing.setAssetStatus(patch.getAssetStatus());
// // // //             existing.setUpdatedBy(username);

// // // //             AssetMaster saved = assetRepo.save(existing);

// // // //             Map<String, Object> placeholders = Map.of(
// // // //                     "assetId", saved.getAssetId(),
// // // //                     "oldName", existing.getAssetNameUdv(),
// // // //                     "assetName", existing.getAssetNameUdv(),
// // // //                     "newName", newName,
// // // //                     "updatedBy", username,
// // // //                     "username", username,
// // // //                     "timestamp", Instant.now().toString()
// // // //             );

// // // //             sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_UPDATED_INAPP", placeholders, projectType);
// // // //             log.info("✏️ Asset updated: id={} by={}", id, username);

// // // //             return saved;
// // // //         }).orElseThrow(() -> new RuntimeException("Asset not found with id: " + id));
// // // //     }

// // // //     // ============================================================
// // // //     // ❌ SOFT DELETE
// // // //     // ============================================================
// // // //     @Transactional
// // // //     public void softDelete(HttpHeaders headers, Long id, AssetRequest request) {
// // // //         String bearer = extractBearer(headers);
// // // //         String username = request.getUsername();
// // // //         Long userId = request.getUserId();
// // // //         String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

// // // //         assetRepo.findById(id).ifPresent(asset -> {
// // // //             asset.setActive(false);
// // // //             asset.setUpdatedBy(username);
// // // //             assetRepo.save(asset);

// // // //             Map<String, Object> placeholders = Map.of(
// // // //                     "assetId", asset.getAssetId(),
// // // //                     "assetName", asset.getAssetNameUdv(),
// // // //                     "deletedBy", username,
// // // //                     "username", username,
// // // //                     "timestamp", Instant.now().toString()
// // // //             );

// // // //             sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_DELETED_INAPP", placeholders, projectType);
// // // //             log.info("🗑️ Asset deleted (soft): id={} by={}", id, username);
// // // //         });
// // // //     }

// // // //     // ============================================================
// // // //     // 🔍 GET BY ID
// // // //     // ============================================================
// // // //     public Optional<AssetMaster> get(Long id) {
// // // //         return assetRepo.findById(id)
// // // //                 .filter(a -> a.getActive() == null || a.getActive())
// // // //                 .map(a -> {
// // // //                     log.info("🔍 Fetched asset: id={} name={}", a.getAssetId(), a.getAssetNameUdv());
// // // //                     return a;
// // // //                 });
// // // //     }

// // // //     // ============================================================
// // // //     // 🔎 SEARCH
// // // //     // ============================================================
// // // //     public Page<AssetMaster> search(Optional<Long> assetId,
// // // //                                     Optional<String> assetName,
// // // //                                     Optional<Long> categoryId,
// // // //                                     Pageable pageable) {
// // // //         List<AssetMaster> filtered = assetRepo.findAll().stream()
// // // //                 .filter(a -> a.getActive() == null || a.getActive())
// // // //                 .filter(a -> assetId.map(id -> id.equals(a.getAssetId())).orElse(true))
// // // //                 .filter(a -> assetName.map(n -> a.getAssetNameUdv().toLowerCase().contains(n.toLowerCase())).orElse(true))
// // // //                 .filter(a -> categoryId.map(cid -> a.getCategory() != null && cid.equals(a.getCategory().getCategoryId())).orElse(true))
// // // //                 .toList();

// // // //         int start = (int) pageable.getOffset();
// // // //         int end = Math.min(start + pageable.getPageSize(), filtered.size());
// // // //         return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
// // // //     }

// // // //     // ============================================================
// // // //     // 🔔 Notification Helper
// // // //     // ============================================================
// // // //     private void sendAssetNotification(String bearer,
// // // //                                        Long userId,
// // // //                                        String username,
// // // //                                        String channel,
// // // //                                        String templateCode,
// // // //                                        Map<String, Object> placeholders,
// // // //                                        String projectType) {
// // // //         try {
// // // //             safeNotificationHelper.safeNotifyAsync(
// // // //                     bearer,
// // // //                     userId,
// // // //                     username,
// // // //                     null,
// // // //                     null,
// // // //                     channel,
// // // //                     templateCode,
// // // //                     placeholders,
// // // //                     projectType
// // // //             );
// // // //         } catch (Exception e) {
// // // //             log.error("⚠️ Notification failed [{}]: {}", templateCode, e.getMessage());
// // // //         }
// // // //     }

// // // //     // ============================================================
// // // //     // 🔐 Token Extractor
// // // //     // ============================================================
// // // //     private String extractBearer(HttpHeaders headers) {
// // // //         String authHeader = headers.getFirst("Authorization");
// // // //         if (authHeader == null || authHeader.isBlank()) {
// // // //             throw new RuntimeException("❌ Missing Authorization header");
// // // //         }
// // // //         return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
// // // //     }
// // // // }

package com.example.asset.service;

import com.example.asset.dto.AssetRequest;
import com.example.asset.entity.*;
import com.example.asset.repository.*;
import com.example.asset.dto.*;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ AssetCrudService
 * Handles CRUD for assets with validation of related entities.
 * Backward compatible with existing build & SafeNotificationHelper setup.
 */
@Service
public class AssetCrudService {

    private static final Logger log = LoggerFactory.getLogger(AssetCrudService.class);

    private final AssetMasterRepository assetRepo;
    private final AssetUserLinkRepository linkRepo;
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final AssetComponentRepository componentRepo;
    private final AssetWarrantyRepository warrantyRepo;
    private final DocumentService documentService;
    private final UserLinkService userLinkService;
    private final SafeNotificationHelper safeNotificationHelper;

    public AssetCrudService(AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo,
            ProductCategoryRepository categoryRepo,
            ProductSubCategoryRepository subCategoryRepo,
            ProductMakeRepository makeRepo,
            ProductModelRepository modelRepo,
            AssetComponentRepository componentRepo,
            AssetWarrantyRepository warrantyRepo,
            DocumentService documentService,
            UserLinkService userLinkService,
            SafeNotificationHelper safeNotificationHelper) {
        this.assetRepo = assetRepo;
        this.linkRepo = linkRepo;
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.componentRepo = componentRepo;
        this.warrantyRepo = warrantyRepo;
        this.documentService = documentService;
        this.userLinkService = userLinkService;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE ASSET
    // ============================================================
    @Transactional
    public AssetMaster create(HttpHeaders headers, AssetRequest request) {
        if (request == null || request.getAsset() == null)
            throw new IllegalArgumentException("❌ AssetRequest or payload cannot be null");

        String bearer = extractBearer(headers);
        AssetMaster asset = request.getAsset();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        // --- 🔍 VALIDATIONS ---
        if (!StringUtils.hasText(asset.getAssetNameUdv()))
            throw new RuntimeException("❌ Asset name cannot be blank");
        if (assetRepo.existsByAssetNameUdv(asset.getAssetNameUdv()))
            throw new RuntimeException("❌ Asset with name '" + asset.getAssetNameUdv() + "' already exists");

        validateEntityReferences(asset);

        asset.setCreatedBy(username);
        asset.setUpdatedBy(username);
        AssetMaster saved = assetRepo.save(asset);

        // // // // // Link user to asset
        // // // // if (userId != null && username != null) {
        // // // // AssetUserLink link = new AssetUserLink();
        // // // // link.setAsset(saved);
        // // // // link.setUserId(String.valueOf(userId));
        // // // // link.setUsername(username);
        // // // // linkRepo.save(link);
        // // // // }

        Map<String, Object> placeholders = Map.of(
                "assetId", saved.getAssetId(),
                "assetName", saved.getAssetNameUdv(),
                "assignedTo", username,
                "username", username,
                "timestamp", Instant.now().toString());

        sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_CREATED_INAPP", placeholders, projectType);
        sendAssetNotification(bearer, userId, username, "EMAIL", "ASSET_CREATED_EMAIL", placeholders, projectType);

        log.info("✅ Asset created successfully: id={} name={} by={}", saved.getAssetId(), saved.getAssetNameUdv(),
                username);
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE ASSET
    // ============================================================
    @Transactional
    public AssetMaster update(HttpHeaders headers, Long id, AssetRequest request) {
        if (request == null || request.getAsset() == null)
            throw new IllegalArgumentException("❌ AssetRequest or payload cannot be null");

        String bearer = extractBearer(headers);
        AssetMaster patch = request.getAsset();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return assetRepo.findById(id).map(existing -> {
            String newName = patch.getAssetNameUdv();

            // 🔒 Validate Name
            if (!StringUtils.hasText(newName)) {
                throw new RuntimeException("❌ Asset name cannot be blank");
            }

            // 🔍 Check for uniqueness excluding current record
            Optional<AssetMaster> duplicate = assetRepo.findByAssetNameUdvIgnoreCase(newName);
            if (duplicate.isPresent() && !duplicate.get().getAssetId().equals(existing.getAssetId())) {
                throw new RuntimeException("❌ Asset with name '" + newName + "' already exists");
            }

            // 🔍 Validate Foreign Keys (Category, SubCategory, Make, Model)
            validateEntityReferences(patch);

            // 🧾 Apply updates
            existing.setAssetNameUdv(newName);
            existing.setAssetStatus(patch.getAssetStatus());
            existing.setCategory(patch.getCategory());
            existing.setSubCategory(patch.getSubCategory());
            existing.setMake(patch.getMake());
            existing.setModel(patch.getModel());
            existing.setUpdatedBy(username);

            AssetMaster saved = assetRepo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "assetId", saved.getAssetId(),
                    "oldName", existing.getAssetNameUdv(),
                    "assetName", saved.getAssetNameUdv(),
                    "updatedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString());

            sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_UPDATED_INAPP", placeholders, projectType);
            log.info("✏️ Asset updated: id={} by={}", id, username);

            return saved;
        }).orElseThrow(() -> new RuntimeException("❌ Asset not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, AssetRequest request) {
        String bearer = extractBearer(headers);
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        assetRepo.findById(id).ifPresentOrElse(asset -> {
            asset.setActive(false);
            asset.setUpdatedBy(username);
            assetRepo.save(asset);

            Map<String, Object> placeholders = Map.of(
                    "assetId", asset.getAssetId(),
                    "assetName", asset.getAssetNameUdv(),
                    "deletedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString());

            sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_DELETED_INAPP", placeholders, projectType);
            log.info("🗑️ Asset deleted (soft): id={} by={}", id, username);
        }, () -> {
            throw new RuntimeException("❌ Asset not found with id: " + id);
        });
    }

    // ============================================================
    // 🔍 GET BY ID
    // ============================================================
    public Optional<AssetResponseDTO> get(Long id) {
        return assetRepo.findById(id)
                .filter(a -> a.getActive() == null || a.getActive())
                .map(a -> {
                    AssetResponseDTO dto = new AssetResponseDTO();
                    dto.setAssetId(a.getAssetId());
                    dto.setAssetNameUdv(a.getAssetNameUdv());
                    dto.setAssetStatus(a.getAssetStatus());

                    // 🔹 Safely extract lazy fields
                    if (a.getCategory() != null)
                        dto.setCategoryName(a.getCategory().getCategoryName());

                    if (a.getSubCategory() != null)
                        dto.setSubCategoryName(a.getSubCategory().getSubCategoryName());

                    if (a.getMake() != null)
                        dto.setMakeName(a.getMake().getMakeName());

                    if (a.getModel() != null)
                        dto.setModelName(a.getModel().getModelName());

                    log.info("🔍 Fetched asset: id={} name={}", a.getAssetId(), a.getAssetNameUdv());
                    return dto;
                });
    }


    // ============================================================
    // 🔍 SEARCH — Now returns DTO instead of entities
    // ============================================================
    public Page<AssetResponseDTO> search(Optional<Long> assetId,
                                         Optional<String> assetName,
                                         Optional<Long> categoryId,
                                         Pageable pageable) {

        List<AssetResponseDTO> filtered = assetRepo.findAll().stream()
                .filter(a -> a.getActive() == null || a.getActive())
                .filter(a -> assetId.map(id -> id.equals(a.getAssetId())).orElse(true))
                .filter(a -> assetName.map(n -> 
                        a.getAssetNameUdv() != null && a.getAssetNameUdv().toLowerCase().contains(n.toLowerCase()))
                        .orElse(true))
                .filter(a -> categoryId
                        .map(cid -> a.getCategory() != null && cid.equals(a.getCategory().getCategoryId()))
                        .orElse(true))
                .map(a -> {
                    AssetResponseDTO dto = new AssetResponseDTO();
                    dto.setAssetId(a.getAssetId());
                    dto.setAssetNameUdv(a.getAssetNameUdv());
                    dto.setAssetStatus(a.getAssetStatus());
                    dto.setCategoryName(a.getCategory() != null ? a.getCategory().getCategoryName() : null);
                    dto.setSubCategoryName(a.getSubCategory() != null ? a.getSubCategory().getSubCategoryName() : null); // ✅ Added subcategory
                    dto.setMakeName(a.getMake() != null ? a.getMake().getMakeName() : null);
                    dto.setModelName(a.getModel() != null ? a.getModel().getModelName() : null);
                    return dto;
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    // ============================================================
    // 🔍 SEARCH BY KEYWORD — Searches across multiple fields
    // ============================================================
    public Page<AssetResponseDTO> searchByKeyword(String keyword, Pageable pageable) {
        String searchTerm = (keyword != null && !keyword.trim().isEmpty()) 
                ? keyword.toLowerCase().trim() 
                : null;

        List<AssetResponseDTO> filtered = assetRepo.findAll().stream()
                .filter(a -> a.getActive() == null || a.getActive())
                .filter(a -> {
                    if (searchTerm == null) return true;
                    
                    // Search across multiple fields
                    boolean matches = false;
                    if (a.getAssetNameUdv() != null && 
                        a.getAssetNameUdv().toLowerCase().contains(searchTerm)) {
                        matches = true;
                    }
                    if (a.getAssetStatus() != null && 
                        a.getAssetStatus().toLowerCase().contains(searchTerm)) {
                        matches = true;
                    }
                    if (a.getCategory() != null && a.getCategory().getCategoryName() != null &&
                        a.getCategory().getCategoryName().toLowerCase().contains(searchTerm)) {
                        matches = true;
                    }
                    if (a.getSubCategory() != null && a.getSubCategory().getSubCategoryName() != null &&
                        a.getSubCategory().getSubCategoryName().toLowerCase().contains(searchTerm)) {
                        matches = true;
                    }
                    if (a.getMake() != null && a.getMake().getMakeName() != null &&
                        a.getMake().getMakeName().toLowerCase().contains(searchTerm)) {
                        matches = true;
                    }
                    if (a.getModel() != null && a.getModel().getModelName() != null &&
                        a.getModel().getModelName().toLowerCase().contains(searchTerm)) {
                        matches = true;
                    }
                    return matches;
                })
                .map(a -> {
                    AssetResponseDTO dto = new AssetResponseDTO();
                    dto.setAssetId(a.getAssetId());
                    dto.setAssetNameUdv(a.getAssetNameUdv());
                    dto.setAssetStatus(a.getAssetStatus());
                    dto.setCategoryName(a.getCategory() != null ? a.getCategory().getCategoryName() : null);
                    dto.setSubCategoryName(a.getSubCategory() != null ? a.getSubCategory().getSubCategoryName() : null);
                    dto.setMakeName(a.getMake() != null ? a.getMake().getMakeName() : null);
                    dto.setModelName(a.getModel() != null ? a.getModel().getModelName() : null);
                    return dto;
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    // ============================================================
    // 📦 BULK UPLOAD ASSETS (NEW - using BulkAssetRequest)
    // ============================================================
    @Transactional
    public BulkUploadResponse<AssetResponseDTO> bulkCreate(HttpHeaders headers, BulkAssetRequest bulkRequest) {
        BulkUploadResponse<AssetResponseDTO> response = new BulkUploadResponse<>();
        
        if (bulkRequest == null || bulkRequest.getAssets() == null) {
            throw new IllegalArgumentException("Bulk request cannot be null");
        }

        List<BulkAssetRequest.SimpleAssetDto> items = bulkRequest.getAssets();
        response.setTotalCount(items.size());

        String username = bulkRequest.getUsername();
        Long userId = bulkRequest.getUserId();
        String projectType = Optional.ofNullable(bulkRequest.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = extractBearer(headers);

        for (int i = 0; i < items.size(); i++) {
            try {
                BulkAssetRequest.SimpleAssetDto item = items.get(i);

                // ✅ VALIDATION: Required field
                if (item.getAssetNameUdv() == null || item.getAssetNameUdv().trim().isEmpty()) {
                    response.addFailure(i, "Asset name (asset_name_udv) is required");
                    continue;
                }

                String assetNameUdv = item.getAssetNameUdv().trim();

                // ✅ VALIDATION: Name length (max 255 chars per entity constraint)
                if (assetNameUdv.length() > 255) {
                    response.addFailure(i, "Asset name exceeds maximum length of 255 characters");
                    continue;
                }

                // ✅ VALIDATION: Name uniqueness (case-insensitive check to prevent duplicates)
                if (assetRepo.existsByAssetNameUdv(assetNameUdv)) {
                    response.addFailure(i, "Asset with name '" + assetNameUdv + "' already exists");
                    continue;
                }

                // ✅ VALIDATION: Foreign keys - Category (required)
                ProductCategory category = null;
                if (item.getCategoryId() != null) {
                    category = categoryRepo.findById(item.getCategoryId())
                            .filter(c -> c.getActive() == null || c.getActive())
                            .orElse(null);
                    if (category == null) {
                        response.addFailure(i, "Category not found with id: " + item.getCategoryId());
                        continue;
                    }
                } else if (item.getCategoryName() != null && !item.getCategoryName().trim().isEmpty()) {
                    category = categoryRepo.findByCategoryNameIgnoreCase(item.getCategoryName().trim())
                            .filter(c -> c.getActive() == null || c.getActive())
                            .orElse(null);
                    if (category == null) {
                        response.addFailure(i, "Category not found with name: " + item.getCategoryName().trim());
                        continue;
                    }
                } else {
                    response.addFailure(i, "Category is required (provide category_id or category_name)");
                    continue;
                }

                // ✅ VALIDATION: Foreign keys - SubCategory (required)
                ProductSubCategory subCategory = null;
                if (item.getSubCategoryId() != null) {
                    subCategory = subCategoryRepo.findById(item.getSubCategoryId())
                            .filter(s -> s.getActive() == null || s.getActive())
                            .orElse(null);
                    if (subCategory == null) {
                        response.addFailure(i, "SubCategory not found with id: " + item.getSubCategoryId());
                        continue;
                    }
                } else if (item.getSubCategoryName() != null && !item.getSubCategoryName().trim().isEmpty()) {
                    subCategory = subCategoryRepo.findBySubCategoryNameIgnoreCase(item.getSubCategoryName().trim())
                            .filter(s -> s.getActive() == null || s.getActive())
                            .orElse(null);
                    if (subCategory == null) {
                        response.addFailure(i, "SubCategory not found with name: " + item.getSubCategoryName().trim());
                        continue;
                    }
                } else {
                    response.addFailure(i, "SubCategory is required (provide sub_category_id or subcategory_name)");
                    continue;
                }

                // ✅ VALIDATION: Foreign keys - Make (required)
                ProductMake make = null;
                if (item.getMakeId() != null) {
                    make = makeRepo.findById(item.getMakeId())
                            .filter(m -> m.getActive() == null || m.getActive())
                            .orElse(null);
                    if (make == null) {
                        response.addFailure(i, "Make not found with id: " + item.getMakeId());
                        continue;
                    }
                } else if (item.getMakeName() != null && !item.getMakeName().trim().isEmpty()) {
                    make = makeRepo.findByMakeNameIgnoreCase(item.getMakeName().trim())
                            .filter(m -> m.getActive() == null || m.getActive())
                            .orElse(null);
                    if (make == null) {
                        response.addFailure(i, "Make not found with name: " + item.getMakeName().trim());
                        continue;
                    }
                } else {
                    response.addFailure(i, "Make is required (provide make_id or make_name)");
                    continue;
                }

                // ✅ VALIDATION: Foreign keys - Model (required)
                ProductModel model = null;
                if (item.getModelId() != null) {
                    model = modelRepo.findById(item.getModelId())
                            .filter(m -> m.getActive() == null || m.getActive())
                            .orElse(null);
                    if (model == null) {
                        response.addFailure(i, "Model not found with id: " + item.getModelId());
                        continue;
                    }
                } else if (item.getModelName() != null && !item.getModelName().trim().isEmpty()) {
                    // Find model by name and make (models can have same name across different makes)
                    if (make != null) {
                        model = modelRepo.findByModelNameIgnoreCaseAndMake_MakeId(item.getModelName().trim(), make.getMakeId())
                                .filter(m -> m.getActive() == null || m.getActive())
                                .orElse(null);
                    } else {
                        // Try to find by name only (less precise)
                        List<ProductModel> models = modelRepo.findAll().stream()
                                .filter(m -> m.getModelName() != null 
                                        && m.getModelName().equalsIgnoreCase(item.getModelName().trim())
                                        && (m.getActive() == null || m.getActive()))
                                .toList();
                        if (models.size() == 1) {
                            model = models.get(0);
                        } else if (models.size() > 1) {
                            response.addFailure(i, "Multiple models found with name: " + item.getModelName().trim() + ". Please provide model_id or make_id");
                            continue;
                        }
                    }
                    if (model == null) {
                        response.addFailure(i, "Model not found with name: " + item.getModelName().trim());
                        continue;
                    }
                } else {
                    response.addFailure(i, "Model is required (provide model_id or model_name)");
                    continue;
                }

                // ✅ VALIDATION: Components (optional - from separate rows, now stored as List)
                Set<AssetComponent> components = new HashSet<>();
                if (item.getComponentIds() != null && !item.getComponentIds().isEmpty()) {
                    // Process component IDs from list (one per row)
                    for (Long componentId : item.getComponentIds()) {
                        if (componentId != null) {
                            componentRepo.findById(componentId)
                                    .filter(c -> c.getActive() == null || c.getActive())
                                    .ifPresent(components::add);
                        }
                    }
                } else if (item.getComponentNames() != null && !item.getComponentNames().isEmpty()) {
                    // Process component names from list (one per row)
                    for (String componentName : item.getComponentNames()) {
                        if (componentName != null && !componentName.trim().isEmpty()) {
                            componentRepo.findByComponentNameIgnoreCase(componentName.trim())
                                    .filter(c -> c.getActive() == null || c.getActive())
                                    .ifPresent(components::add);
                        }
                    }
                }

                // CREATE: Primary key is auto-generated
                // Directly save entity without calling create() to avoid per-item notifications
                AssetMaster asset = new AssetMaster();
                asset.setAssetNameUdv(assetNameUdv);
                asset.setCategory(category);
                asset.setSubCategory(subCategory);
                asset.setMake(make);
                asset.setModel(model);
                asset.setComponents(components);
                asset.setCreatedBy(username);
                asset.setUpdatedBy(username);
                
                // Only set asset_status if provided (optional field)
                if (item.getAssetStatus() != null && !item.getAssetStatus().trim().isEmpty()) {
                    asset.setAssetStatus(item.getAssetStatus().trim());
                }

                AssetMaster created = assetRepo.save(asset);
                
                // Convert entity to DTO to include all optional fields in JSON response
                AssetResponseDTO result = new AssetResponseDTO();
                result.setAssetId(created.getAssetId());
                result.setAssetNameUdv(created.getAssetNameUdv());
                result.setAssetStatus(created.getAssetStatus());
                if (created.getCategory() != null) {
                    result.setCategoryName(created.getCategory().getCategoryName());
                }
                if (created.getSubCategory() != null) {
                    result.setSubCategoryName(created.getSubCategory().getSubCategoryName());
                }
                if (created.getMake() != null) {
                    result.setMakeName(created.getMake().getMakeName());
                }
                if (created.getModel() != null) {
                    result.setModelName(created.getModel().getModelName());
                }
                
                response.addSuccess(i, result);
                log.debug("✅ Created asset name={}", assetNameUdv);

            } catch (Exception e) {
                log.error("❌ Bulk asset failed at index {}: {}", i, e.getMessage());
                response.addFailure(i, e.getMessage());
            }
        }

        // ✅ Send single notification for bulk operation (not per item)
        if (response.getSuccessCount() > 0) {
            try {
                Map<String, Object> placeholders = new LinkedHashMap<>();
                placeholders.put("totalCount", response.getTotalCount());
                placeholders.put("successCount", response.getSuccessCount());
                placeholders.put("failureCount", response.getFailureCount());
                placeholders.put("username", username);
                placeholders.put("timestamp", Instant.now().toString());

                sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_BULK_UPLOAD_INAPP", placeholders, projectType);
                sendAssetNotification(bearer, userId, username, "EMAIL", "ASSET_BULK_UPLOAD_EMAIL", placeholders, projectType);
            } catch (Exception e) {
                log.warn("⚠️ Failed to send bulk upload notification: {}", e.getMessage());
            }
        }

        log.info("📦 Bulk asset upload: {}/{} success",
                response.getSuccessCount(), response.getTotalCount());
        return response;
    }

    // ============================================================
    // 🔍 VALIDATE RELATED ENTITIES
    // ============================================================
    private void validateEntityReferences(AssetMaster asset) {
        if (asset.getCategory() == null || asset.getCategory().getCategoryId() == null)
            throw new RuntimeException("❌ Missing Category: categoryId is required");
        if (!categoryRepo.existsById(asset.getCategory().getCategoryId()))
            throw new RuntimeException("❌ Invalid Category ID: " + asset.getCategory().getCategoryId());

        if (asset.getSubCategory() == null || asset.getSubCategory().getSubCategoryId() == null)
            throw new RuntimeException("❌ Missing SubCategory: subCategoryId is required");
        if (!subCategoryRepo.existsById(asset.getSubCategory().getSubCategoryId()))
            throw new RuntimeException("❌ Invalid SubCategory ID: " + asset.getSubCategory().getSubCategoryId());

        if (asset.getMake() == null || asset.getMake().getMakeId() == null)
            throw new RuntimeException("❌ Missing Make: makeId is required");
        if (!makeRepo.existsById(asset.getMake().getMakeId()))
            throw new RuntimeException("❌ Invalid Make ID: " + asset.getMake().getMakeId());

        if (asset.getModel() == null || asset.getModel().getModelId() == null)
            throw new RuntimeException("❌ Missing Model: modelId is required");
        if (!modelRepo.existsById(asset.getModel().getModelId()))
            throw new RuntimeException("❌ Invalid Model ID: " + asset.getModel().getModelId());
    }

    // ============================================================
    // 🔔 Notification Helper
    // ============================================================
    private void sendAssetNotification(String bearer,
            Long userId,
            String username,
            String channel,
            String templateCode,
            Map<String, Object> placeholders,
            String projectType) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer,
                    userId,
                    username,
                    null,
                    null,
                    channel,
                    templateCode,
                    placeholders,
                    projectType);
        } catch (Exception e) {
            log.error("⚠️ Notification failed [{}]: {}", templateCode, e.getMessage());
        }
    }

    // ============================================================
    // 🚀 COMPLETE ASSET CREATION (All-in-One)
    // ============================================================
    /**
     * Create asset with all related information in one transaction:
     * - Asset creation (name, model, serial number)
     * - Warranty creation (start/end dates)
     * - Document upload (purchase invoice)
     * - User assignment
     */
    @Transactional
    public Map<String, Object> createCompleteAsset(
            HttpHeaders headers,
            CompleteAssetCreationRequest request,
            MultipartFile purchaseInvoiceFile) {

        String bearer = extractBearer(headers);
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        log.info("🚀 Creating complete asset: name={}, modelId={}, targetUserId={}",
                request.getAssetNameUdv(), request.getModelId(), request.getTargetUserId());

        // 1️⃣ VALIDATE MODEL
        ProductModel model = modelRepo.findById(request.getModelId())
                .orElseThrow(() -> new IllegalArgumentException("❌ Model not found with ID: " + request.getModelId()));

        // 2️⃣ CREATE ASSET
        AssetMaster asset = new AssetMaster();
        asset.setAssetNameUdv(request.getAssetNameUdv());
        asset.setModel(model);
        
        // Set serial number
        if (request.getSerialNumber() != null && !request.getSerialNumber().trim().isEmpty()) {
            asset.setSerialNumber(request.getSerialNumber().trim());
        }
        
        // Set purchase date (from warranty start date)
        if (request.getWarrantyStartDate() != null) {
            asset.setPurchaseDate(request.getWarrantyStartDate());
        }
        
        // Set optional fields
        if (request.getCategoryId() != null) {
            asset.setCategory(categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId())));
        }
        if (request.getSubCategoryId() != null) {
            asset.setSubCategory(subCategoryRepo.findById(request.getSubCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("SubCategory not found: " + request.getSubCategoryId())));
        }
        if (request.getMakeId() != null) {
            asset.setMake(makeRepo.findById(request.getMakeId())
                    .orElseThrow(() -> new IllegalArgumentException("Make not found: " + request.getMakeId())));
        } else if (model.getMake() != null) {
            asset.setMake(model.getMake()); // Use make from model if not specified
        }
        if (request.getAssetStatus() != null) {
            asset.setAssetStatus(request.getAssetStatus());
        }
        
        asset.setCreatedBy(username);
        asset.setUpdatedBy(username);
        asset.setActive(true);
        
        AssetMaster savedAsset = assetRepo.save(asset);
        log.info("✅ Asset created: id={}, name={}", savedAsset.getAssetId(), savedAsset.getAssetNameUdv());

        // 3️⃣ CREATE WARRANTY
        AssetWarranty warranty = new AssetWarranty();
        warranty.setAsset(savedAsset);
        warranty.setWarrantyStartDate(request.getWarrantyStartDate());
        warranty.setWarrantyEndDate(request.getWarrantyEndDate());
        warranty.setWarrantyStatus(Optional.ofNullable(request.getWarrantyStatus()).orElse("ACTIVE"));
        warranty.setWarrantyProvider(request.getWarrantyProvider());
        warranty.setWarrantyTerms(request.getWarrantyTerms());
        warranty.setUserId(userId);
        warranty.setUsername(username);
        warranty.setActive(true);
        warranty.setCreatedBy(username);
        warranty.setUpdatedBy(username);
        
        AssetWarranty savedWarranty = warrantyRepo.save(warranty);
        log.info("✅ Warranty created: id={} for assetId={}", savedWarranty.getWarrantyId(), savedAsset.getAssetId());

        // 4️⃣ UPLOAD DOCUMENT (if provided)
        AssetDocument savedDocument = null;
        if (purchaseInvoiceFile != null && !purchaseInvoiceFile.isEmpty()) {
            DocumentRequest docRequest = new DocumentRequest();
            docRequest.setUserId(userId);
            docRequest.setUsername(username);
            docRequest.setProjectType(projectType);
            docRequest.setEntityType("ASSET");
            docRequest.setEntityId(savedAsset.getAssetId());
            docRequest.setAssetId(savedAsset.getAssetId());
            docRequest.setDocType("PURCHASE_INVOICE");
            
            savedDocument = documentService.upload(headers, purchaseInvoiceFile, docRequest);
            log.info("✅ Document uploaded: id={} for assetId={}", savedDocument.getDocumentId(), savedAsset.getAssetId());
        }

        // 5️⃣ LINK USER TO ASSET
        String linkMessage = userLinkService.linkEntity(
                bearer,
                "ASSET",
                savedAsset.getAssetId(),
                request.getTargetUserId(),
                Optional.ofNullable(request.getTargetUsername()).orElse("user_" + request.getTargetUserId()),
                userId,
                username
        );
        log.info("✅ User linked to asset: {}", linkMessage);

        // 6️⃣ BUILD RESPONSE
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("assetId", savedAsset.getAssetId());
        response.put("assetNameUdv", savedAsset.getAssetNameUdv());
        response.put("serialNumber", savedAsset.getSerialNumber());
        response.put("purchaseDate", savedAsset.getPurchaseDate());
        response.put("modelId", savedAsset.getModel().getModelId());
        response.put("modelName", savedAsset.getModel().getModelName());
        if (savedAsset.getCategory() != null) {
            response.put("categoryId", savedAsset.getCategory().getCategoryId());
            response.put("categoryName", savedAsset.getCategory().getCategoryName());
        }
        if (savedAsset.getSubCategory() != null) {
            response.put("subCategoryId", savedAsset.getSubCategory().getSubCategoryId());
            response.put("subCategoryName", savedAsset.getSubCategory().getSubCategoryName());
        }
        if (savedAsset.getMake() != null) {
            response.put("makeId", savedAsset.getMake().getMakeId());
            response.put("makeName", savedAsset.getMake().getMakeName());
        }
        response.put("assetStatus", savedAsset.getAssetStatus());
        response.put("warrantyId", savedWarranty.getWarrantyId());
        response.put("warrantyStartDate", savedWarranty.getWarrantyStartDate());
        response.put("warrantyEndDate", savedWarranty.getWarrantyEndDate());
        response.put("warrantyProvider", savedWarranty.getWarrantyProvider());
        response.put("warrantyStatus", savedWarranty.getWarrantyStatus());
        if (savedDocument != null) {
            response.put("documentId", savedDocument.getDocumentId());
            response.put("documentFileName", savedDocument.getFileName());
            response.put("documentFilePath", savedDocument.getFilePath());
        }
        response.put("targetUserId", request.getTargetUserId());
        response.put("targetUsername", Optional.ofNullable(request.getTargetUsername())
                .orElse("user_" + request.getTargetUserId()));
        response.put("linkStatus", linkMessage);

        // 7️⃣ SEND NOTIFICATIONS
        Map<String, Object> placeholders = new LinkedHashMap<>();
        placeholders.put("assetId", savedAsset.getAssetId());
        placeholders.put("assetName", savedAsset.getAssetNameUdv());
        placeholders.put("targetUserId", request.getTargetUserId());
        placeholders.put("username", username);
        placeholders.put("timestamp", Instant.now().toString());
        
        sendAssetNotification(bearer, userId, username, "INAPP", "ASSET_CREATED_INAPP", placeholders, projectType);

        log.info("✅ Complete asset creation successful: assetId={}, warrantyId={}, documentId={}, targetUserId={}",
                savedAsset.getAssetId(), savedWarranty.getWarrantyId(),
                savedDocument != null ? savedDocument.getDocumentId() : null, request.getTargetUserId());

        return response;
    }

    // ============================================================
    // 🔐 Token Extractor
    // ============================================================
    private String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new RuntimeException("❌ Missing Authorization header");
        }
        return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
    }
}


