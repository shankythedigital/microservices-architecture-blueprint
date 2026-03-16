
package com.example.asset.service;

import com.example.asset.dto.BulkModelRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.DocumentRequest;
import com.example.asset.dto.ModelDto;
import com.example.asset.dto.ModelRequest;
import com.example.asset.entity.ProductMake;
import com.example.asset.entity.ProductModel;
import com.example.asset.mapper.ModelMapper;
import com.example.asset.repository.ProductMakeRepository;
import com.example.asset.repository.ProductModelRepository;
import com.example.common.client.AdminClient;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import java.util.*;

/**
 * ✅ ModelService
 * Handles CRUD for ProductModel with notifications.
 * Uses DTO responses, preserves make_id, and ensures data integrity.
 */
@Service
public class ModelService {

    private static final Logger log = LoggerFactory.getLogger(ModelService.class);
    private static final int BULK_BATCH_SIZE = 500;

    private final ProductModelRepository repo;
    private final EntityManager entityManager;
    private final ProductMakeRepository makeRepo;
    private final SafeNotificationHelper safeNotificationHelper;
    @SuppressWarnings("unused")
    private final AdminClient adminClient; // Reserved for future admin operations
    private final DocumentService documentService;
    private final DocumentTypeMasterService documentTypeMasterService;

    public ModelService(ProductModelRepository repo,
                        ProductMakeRepository makeRepo,
                        SafeNotificationHelper safeNotificationHelper,
                        AdminClient adminClient,
                        EntityManager entityManager,
                        DocumentService documentService,
                        DocumentTypeMasterService documentTypeMasterService) {
        this.repo = repo;
        this.makeRepo = makeRepo;
        this.safeNotificationHelper = safeNotificationHelper;
        this.adminClient = adminClient;
        this.entityManager = entityManager;
        this.documentService = documentService;
        this.documentTypeMasterService = documentTypeMasterService;
    }

    // ============================================================
    // 🟢 CREATE MODEL
    // ============================================================
    @Transactional
    public ModelDto create(HttpHeaders headers, ModelRequest request) {
        return create(headers, request, null, null, false);
    }

    @Transactional
    public ModelDto create(HttpHeaders headers, ModelRequest request, boolean skipNotifications) {
        return create(headers, request, null, null, skipNotifications);
    }

    /**
     * Create model, optionally skipping per-item notifications (used by bulk upload to avoid
     * thousands of async notification tasks that can cause resource/context issues).
     */
    @Transactional
    public ModelDto create(HttpHeaders headers, ModelRequest request, MultipartFile document, String docType, boolean skipNotifications) {
        validateAuthorization(headers);

        ProductModel model = request.getModel();
        if (model == null) throw new IllegalArgumentException("Model cannot be null");

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        if (model.getMake() == null || model.getMake().getMakeId() == null)
            throw new RuntimeException("❌ Model must have a valid make");

        if (!StringUtils.hasText(model.getModelName()))
            throw new RuntimeException("Model name cannot be blank");

        boolean exists = repo.existsByModelNameIgnoreCaseAndMake_MakeId(
                model.getModelName(), model.getMake().getMakeId());
        if (exists)
            throw new RuntimeException("❌ Model with this name already exists for the given make");

        // Set favourite and mostLike if provided
        if (model.getIsFavourite() == null) {
            model.setIsFavourite(false);
        }
        if (model.getIsMostLike() == null) {
            model.setIsMostLike(false);
        }
        model.setCreatedBy(username);
        model.setUpdatedBy(username);
        ProductModel saved = repo.save(model);

        // 📎 Upload document and store in AssetDocument (required for create, skip for bulk)
        if (document != null && !document.isEmpty() && docType != null && !docType.isBlank()) {
            DocumentRequest docRequest = new DocumentRequest();
            docRequest.setUserId(userId);
            docRequest.setUsername(username);
            docRequest.setProjectType(projectType);
            docRequest.setEntityType("MODEL");
            docRequest.setEntityId(saved.getModelId());
            docRequest.setDocType(docType.trim());
            documentService.upload(headers, document, docRequest);
            log.info("✅ Document uploaded for model ID={} with docType={}", saved.getModelId(), docType);
        }

        if (!skipNotifications) {
            sendNotification(bearer, userId, username, "INAPP", "MODEL_CREATED_INAPP", saved, projectType);
            sendNotification(bearer, userId, username, "EMAIL", "MODEL_CREATED_EMAIL", saved, projectType);
            sendNotification(bearer, userId, username, "SMS", "MODEL_CREATED_SMS", saved, projectType);
            log.info("✅ Model created: id={} name={} by={}", saved.getModelId(), saved.getModelName(), username);
        }

        return ModelMapper.toDto(saved);
    }

    // ============================================================
    // ✏️ UPDATE MODEL
    // ============================================================
    @Transactional
    public ModelDto update(HttpHeaders headers, Long id, ModelRequest request) {
        validateAuthorization(headers);

        ProductModel patch = request.getModel();
        if (patch == null) throw new IllegalArgumentException("Model cannot be null");

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        return repo.findById(id).map(existing -> {

            String newName = patch.getModelName() != null ? patch.getModelName().trim() : null;
            if (!StringUtils.hasText(newName))
                throw new RuntimeException("Model name cannot be blank");

            // ✅ Preserve existing make if not provided
            if (patch.getMake() == null) {
                patch.setMake(existing.getMake());
            }

            if (patch.getMake() == null)
                throw new RuntimeException("❌ Model must have a valid make");

            // ✅ Uniqueness check: duplicate only when name is actually changing (case-insensitive)
            boolean duplicate = repo.existsByModelNameIgnoreCaseAndMake_MakeId(
                    newName, patch.getMake().getMakeId());
            boolean nameChanged = existing.getModelName() == null
                    ? StringUtils.hasText(newName)
                    : !existing.getModelName().equalsIgnoreCase(newName);
            if (duplicate && nameChanged)
                throw new RuntimeException("❌ Model with name '" + newName + "' already exists for this make");

            String oldName = existing.getModelName();
            existing.setModelName(newName);
            if (patch.getDescription() != null) {
                existing.setDescription(patch.getDescription());
            }
            if (patch.getSequenceOrder() != null) {
                existing.setSequenceOrder(patch.getSequenceOrder());
            }
            if (patch.getIsFavourite() != null) {
                existing.setIsFavourite(patch.getIsFavourite());
            }
            if (patch.getIsMostLike() != null) {
                existing.setIsMostLike(patch.getIsMostLike());
            }
            existing.setMake(patch.getMake());
            existing.setUpdatedBy(username);

            ProductModel saved = repo.save(existing);

            // 🔔 Send notifications
            sendNotification(bearer, userId, username, "INAPP", "MODEL_UPDATED_INAPP", saved, projectType);
            sendNotification(bearer, userId, username, "EMAIL", "MODEL_UPDATED_EMAIL", saved, projectType);
            sendNotification(bearer, userId, username, "SMS", "MODEL_UPDATED_SMS", saved, projectType);

            log.info("✏️ Model updated: id={} oldName={} newName={} by={}", id, oldName, newName, username);
            return ModelMapper.toDto(saved);

        }).orElseThrow(() -> new RuntimeException("Model not found with id: " + id));
    }

    @Transactional
    public ModelDto updateWithDocument(HttpHeaders headers, Long id, ModelRequest request, MultipartFile document, String docType) {
        documentTypeMasterService.validate(docType);
        if (document == null || document.isEmpty())
            throw new IllegalArgumentException("Document is required for update");
        ModelDto updated = update(headers, id, request);
        DocumentRequest docRequest = new DocumentRequest();
        docRequest.setUserId(request.getUserId());
        docRequest.setUsername(request.getUsername());
        docRequest.setProjectType(Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE"));
        docRequest.setEntityType("MODEL");
        docRequest.setEntityId(id);
        docRequest.setDocType(docType.trim());
        documentService.upload(headers, document, docRequest);
        log.info("✅ Document uploaded for model ID={} with docType={}", id, docType);
        return updated;
    }

    // ============================================================
    // ❌ SOFT DELETE
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, ModelRequest request) {
        validateAuthorization(headers);

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        repo.findById(id).ifPresent(model -> {
            model.setActive(false);
            model.setUpdatedBy(username);
            ProductModel saved = repo.save(model);

            // 🔔 Send notifications
            sendNotification(bearer, userId, username, "INAPP", "MODEL_DELETED_INAPP", saved, projectType);
            sendNotification(bearer, userId, username, "EMAIL", "MODEL_DELETED_EMAIL", saved, projectType);
            sendNotification(bearer, userId, username, "SMS", "MODEL_DELETED_SMS", saved, projectType);

            log.info("🗑️ Model soft-deleted: id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<ModelDto> list() {
        return repo.findAll().stream()
                .filter(m -> m.getActive() == null || m.getActive())
                .sorted((a, b) -> {
                    // Priority: 1. isMostLike (true first), 2. isFavourite (true first), 3. sequenceOrder (lower first), 4. modelName
                    Boolean mostLikeA = a.getIsMostLike() != null ? a.getIsMostLike() : false;
                    Boolean mostLikeB = b.getIsMostLike() != null ? b.getIsMostLike() : false;
                    int mostLikeCompare = Boolean.compare(mostLikeB, mostLikeA); // true first (descending)
                    if (mostLikeCompare != 0) return mostLikeCompare;
                    
                    Boolean favA = a.getIsFavourite() != null ? a.getIsFavourite() : false;
                    Boolean favB = b.getIsFavourite() != null ? b.getIsFavourite() : false;
                    int favCompare = Boolean.compare(favB, favA); // true first (descending)
                    if (favCompare != 0) return favCompare;
                    
                    // Then by sequenceOrder (nulls last)
                    Integer seqA = a.getSequenceOrder();
                    Integer seqB = b.getSequenceOrder();
                    if (seqA == null && seqB == null) {
                        return a.getModelName().compareToIgnoreCase(b.getModelName());
                    }
                    if (seqA == null) return 1;
                    if (seqB == null) return -1;
                    int seqCompare = seqA.compareTo(seqB);
                    return seqCompare != 0 ? seqCompare : a.getModelName().compareToIgnoreCase(b.getModelName());
                })
                .map(ModelMapper::toDto)
                .toList();
    }

    public Optional<ModelDto> find(Long id) {
        return repo.findById(id)
                .filter(m -> m.getActive() == null || m.getActive())
                .map(ModelMapper::toDto);
    }

    // ============================================================
    // 📋 VALIDATE BULK MODELS (no persist - for pre-check before background processing)
    // ============================================================
    @Transactional(readOnly = true)
    public BulkUploadResponse<ModelDto> validateBulkModels(BulkModelRequest bulkRequest) {
        BulkUploadResponse<ModelDto> response = new BulkUploadResponse<>();
        if (bulkRequest == null || bulkRequest.getModels() == null) {
            throw new IllegalArgumentException("Bulk request cannot be null");
        }
        List<BulkModelRequest.SimpleModelDto> items = bulkRequest.getModels();
        response.setTotalCount(items.size());

        for (int i = 0; i < items.size(); i++) {
            try {
                BulkModelRequest.SimpleModelDto item = items.get(i);
                if (item.getModelName() == null || item.getModelName().trim().isEmpty()) {
                    response.addSkipped(i, "Model name is required");
                    continue;
                }
                String modelName = item.getModelName().trim();
                if (modelName.length() > 150) {
                    response.addFailure(i, "Model name exceeds maximum length of 150 characters");
                    continue;
                }
                if (item.getDescription() != null && !item.getDescription().trim().isEmpty()) {
                    String desc = item.getDescription().trim();
                    if (desc.length() > 255) {
                        response.addFailure(i, "Description exceeds maximum length of 255 characters");
                        continue;
                    }
                }
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
                        response.addFailure(i, "Make not found with name: " + item.getMakeName());
                        continue;
                    }
                } else {
                    response.addFailure(i, "Make is required (provide make_id or make_name)");
                    continue;
                }
                if (repo.existsByModelNameIgnoreCaseAndMake_MakeId(modelName, make.getMakeId())) {
                    response.addSkipped(i, "Model '" + modelName + "' already exists for this make");
                    continue;
                }
                response.addSuccess(i, null); // Valid, no DTO yet
            } catch (Exception e) {
                response.addFailure(i, e.getMessage());
            }
        }
        return response;
    }

    /**
     * Run bulk create in background. Outcomes are sent via Email, SMS and WhatsApp when complete.
     */
    @Async
    @Transactional
    public void bulkCreateAsync(String bearerToken, BulkModelRequest bulkRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", bearerToken != null && bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            bulkCreate(headers, bulkRequest);
            log.info("📦 Background bulk model upload completed for user={}", bulkRequest.getUsername());
        } catch (Exception e) {
            log.error("❌ Background bulk model upload failed: {}", e.getMessage(), e);
            try {
                String bearer = bearerToken != null && bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken;
                HttpHeaders h = new HttpHeaders();
                h.set("Authorization", bearer);
                int total = bulkRequest != null && bulkRequest.getModels() != null ? bulkRequest.getModels().size() : 0;
                Map<String, Object> placeholders = new LinkedHashMap<>();
                placeholders.put("entityType", "Model");
                placeholders.put("totalCount", total);
                placeholders.put("successCount", 0);
                placeholders.put("failureCount", total);
                placeholders.put("error", e.getMessage());
                placeholders.put("username", bulkRequest != null ? bulkRequest.getUsername() : "unknown");
                placeholders.put("timestamp", java.time.Instant.now().toString());
                safeNotificationHelper.safeNotifyAsync(bearer, bulkRequest != null ? bulkRequest.getUserId() : null,
                        bulkRequest != null ? bulkRequest.getUsername() : "unknown",
                        null, null, "EMAIL", "MASTER_DATA_BULK_UPLOAD_EMAIL", placeholders,
                        bulkRequest != null && bulkRequest.getProjectType() != null ? bulkRequest.getProjectType() : "ASSET_SERVICE");
            } catch (Exception ex) {
                log.warn("⚠️ Failed to send failure notification: {}", ex.getMessage());
            }
        }
    }

    // ============================================================
    // 📦 BULK UPLOAD MODELS (NEW - using BulkModelRequest)
    // ============================================================
    @Transactional
    public BulkUploadResponse<ModelDto> bulkCreate(HttpHeaders headers, BulkModelRequest bulkRequest) {
        BulkUploadResponse<ModelDto> response = new BulkUploadResponse<>();
        
        if (bulkRequest == null || bulkRequest.getModels() == null) {
            throw new IllegalArgumentException("Bulk request cannot be null");
        }

        List<BulkModelRequest.SimpleModelDto> items = bulkRequest.getModels();
        response.setTotalCount(items.size());

        String username = bulkRequest.getUsername();
        Long userId = bulkRequest.getUserId();
        String projectType = Optional.ofNullable(bulkRequest.getProjectType()).orElse("ASSET_SERVICE");

        for (int i = 0; i < items.size(); i++) {
            try {
                BulkModelRequest.SimpleModelDto item = items.get(i);

                // ✅ VALIDATION: Required field
                if (item.getModelName() == null || item.getModelName().trim().isEmpty()) {
                    response.addSkipped(i, "Model name is required");
                    continue;
                }

                String modelName = item.getModelName().trim();

                // ✅ VALIDATION: Name length (max 150 chars per entity constraint)
                if (modelName.length() > 150) {
                    response.addFailure(i, "Model name exceeds maximum length of 150 characters");
                    continue;
                }

                // ✅ VALIDATION: Description length (max 255 chars per entity constraint)
                String description = null;
                if (item.getDescription() != null && !item.getDescription().trim().isEmpty()) {
                    description = item.getDescription().trim();
                    if (description.length() > 255) {
                        response.addFailure(i, "Description exceeds maximum length of 255 characters");
                        continue;
                    }
                }

                // ✅ VALIDATION: Foreign key - Make is REQUIRED via makeId or make_name (Excel may have "Make" / "Make Name")
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
                        response.addFailure(i, "Make not found with name: " + item.getMakeName());
                        continue;
                    }
                } else {
                    response.addFailure(i, "Make is required (provide make_id or make_name)");
                    continue;
                }

                // ✅ VALIDATION: Uniqueness check (model name must be unique per make)
                boolean exists = repo.existsByModelNameIgnoreCaseAndMake_MakeId(modelName, make.getMakeId());
                if (exists) {
                    response.addSkipped(i, "Model with name '" + modelName + "' already exists for this make");
                    continue;
                }

                // CREATE: persist only (no create() to avoid auth/DataSource path in loop)
                ProductModel model = new ProductModel();
                model.setModelName(modelName);
                if (description != null) model.setDescription(description);
                model.setMake(make);
                model.setIsFavourite(false);
                model.setIsMostLike(false);
                model.setCreatedBy(username);
                model.setUpdatedBy(username);
                ProductModel saved = repo.save(model);
                response.addSuccess(i, ModelMapper.toDto(saved));
                log.debug("✅ Created model name={}", modelName);

            } catch (Exception e) {
                log.error("❌ Bulk model failed at index {}: {}", i, e.getMessage());
                response.addFailure(i, e.getMessage());
            }

            // Periodic flush/clear to prevent session bloat and avoid transaction/DataSource issues at scale
            if ((i + 1) % BULK_BATCH_SIZE == 0) {
                try {
                    entityManager.flush();
                    entityManager.clear();
                    log.debug("📦 Flushed persistence context at index {}", i + 1);
                } catch (Exception ex) {
                    log.warn("⚠️ Flush/clear at index {}: {}", i + 1, ex.getMessage());
                }
            }
        }

        // Notify user: Email, SMS, WhatsApp, InApp - single-line summary
        try {
            String bearer = extractBearer(headers);
            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("entityType", "Model");
            placeholders.put("totalCount", response.getTotalCount());
            placeholders.put("successCount", response.getSuccessCount());
            placeholders.put("failureCount", response.getFailureCount());
            placeholders.put("skippedCount", response.getSkippedCount());
            placeholders.put("notUploadedCount", response.getSkippedCount());
            placeholders.put("username", username);
            placeholders.put("timestamp", java.time.Instant.now().toString());
            safeNotificationHelper.safeNotifyAsync(bearer, userId, username, null, null, "EMAIL", "MASTER_DATA_BULK_UPLOAD_EMAIL", placeholders, projectType);
            safeNotificationHelper.safeNotifyAsync(bearer, userId, username, null, null, "SMS", "MASTER_DATA_BULK_UPLOAD_SMS", placeholders, projectType);
            safeNotificationHelper.safeNotifyAsync(bearer, userId, username, null, null, "WHATSAPP", "MASTER_DATA_BULK_UPLOAD_WHATSAPP", placeholders, projectType);
            safeNotificationHelper.safeNotifyAsync(bearer, userId, username, null, null, "INAPP", "MASTER_DATA_BULK_UPLOAD_INAPP", placeholders, projectType);
        } catch (Exception e) {
            log.warn("⚠️ Failed to send bulk upload notification: {}", e.getMessage());
        }

        log.info("📦 Bulk model upload: {}/{} success",
                response.getSuccessCount(), response.getTotalCount());
        return response;
    }

    // ============================================================
    // 🔔 NOTIFICATION WRAPPER
    // ============================================================
    private void sendNotification(String bearer,
                                  Long userId,
                                  String username,
                                  String channel,
                                  String templateCode,
                                  ProductModel model,
                                  String projectType) {
        try {
            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("modelId", model.getModelId());
            placeholders.put("modelName", model.getModelName());
            placeholders.put("makeId", model.getMake() != null ? model.getMake().getMakeId() : null);
            placeholders.put("makeName", model.getMake() != null ? model.getMake().getMakeName() : null);
            placeholders.put("actor", username);
            placeholders.put("username", username);
            placeholders.put("timestamp", new Date().toString());

            safeNotificationHelper.safeNotifyAsync(
                    bearer, userId, username, null, null,
                    channel, templateCode, placeholders, projectType);

            log.info("📨 Notification [{}] sent via {} for modelId={} by={}",
                    templateCode, channel, model.getModelId(), username);
        } catch (Exception e) {
            log.error("⚠️ Failed to send {} notification for model {}: {}",
                    templateCode, model.getModelId(), e.getMessage());
        }
    }

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for a model (accessible to all authenticated users)
     */
    @Transactional
    public ModelDto updateFavourite(HttpHeaders headers, Long id, Boolean isFavourite) {
        String bearer = extractBearer(headers);
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return repo.findById(id).map(existing -> {
            existing.setIsFavourite(isFavourite != null ? isFavourite : false);
            existing.setUpdatedBy(username);
            ProductModel saved = repo.save(existing);

            sendNotification(bearer, userId, username, "INAPP", "MODEL_FAVOURITE_UPDATED_INAPP", saved, projectType);
            log.info("⭐ Model favourite updated: id={} isFavourite={} by={}", id, isFavourite, username);

            return ModelMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Model not found with id: " + id));
    }

    /**
     * Toggle most like status for a model (accessible to all authenticated users)
     */
    @Transactional
    public ModelDto updateMostLike(HttpHeaders headers, Long id, Boolean isMostLike) {
        String bearer = extractBearer(headers);
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return repo.findById(id).map(existing -> {
            existing.setIsMostLike(isMostLike != null ? isMostLike : false);
            existing.setUpdatedBy(username);
            ProductModel saved = repo.save(existing);

            sendNotification(bearer, userId, username, "INAPP", "MODEL_MOST_LIKE_UPDATED_INAPP", saved, projectType);
            log.info("⭐ Model most like updated: id={} isMostLike={} by={}", id, isMostLike, username);

            return ModelMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Model not found with id: " + id));
    }

    /**
     * Update sequence order for a model (admin only)
     */
    @Transactional
    public ModelDto updateSequenceOrder(HttpHeaders headers, Long id, Integer sequenceOrder) {
        // Check if user is admin
        if (!com.example.asset.util.JwtUtil.isAdmin()) {
            throw new RuntimeException("Access denied: Only admins can update sequence order");
        }

        String bearer = extractBearer(headers);
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return repo.findById(id).map(existing -> {
            existing.setSequenceOrder(sequenceOrder);
            existing.setUpdatedBy(username);
            ProductModel saved = repo.save(existing);

            sendNotification(bearer, userId, username, "INAPP", "MODEL_SEQUENCE_UPDATED_INAPP", saved, projectType);
            log.info("📊 Model sequence order updated: id={} sequenceOrder={} by={}", id, sequenceOrder, username);

            return ModelMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Model not found with id: " + id));
    }

    // ============================================================
    // 🔐 TOKEN VALIDATION & EXTRACTION
    // ============================================================
    private String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new RuntimeException("❌ Missing Authorization header");
        }
        return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
    }

    private void validateAuthorization(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank())
            throw new SecurityException("❌ Missing Authorization header");
        if (!authHeader.startsWith("Bearer "))
            throw new SecurityException("❌ Invalid Authorization header format");
    }
}


