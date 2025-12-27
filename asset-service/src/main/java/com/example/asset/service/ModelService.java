
package com.example.asset.service;

import com.example.asset.dto.BulkModelRequest;
import com.example.asset.dto.BulkUploadResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * ✅ ModelService
 * Handles CRUD for ProductModel with notifications.
 * Uses DTO responses, preserves make_id, and ensures data integrity.
 */
@Service
public class ModelService {

    private static final Logger log = LoggerFactory.getLogger(ModelService.class);

    private final ProductModelRepository repo;
    private final ProductMakeRepository makeRepo;
    private final SafeNotificationHelper safeNotificationHelper;
    private final AdminClient adminClient;

    public ModelService(ProductModelRepository repo,
                        ProductMakeRepository makeRepo,
                        SafeNotificationHelper safeNotificationHelper,
                        AdminClient adminClient) {
        this.repo = repo;
        this.makeRepo = makeRepo;
        this.safeNotificationHelper = safeNotificationHelper;
        this.adminClient = adminClient;
    }

    // ============================================================
    // 🟢 CREATE MODEL
    // ============================================================
    @Transactional
    public ModelDto create(HttpHeaders headers, ModelRequest request) {
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

        model.setCreatedBy(username);
        model.setUpdatedBy(username);
        ProductModel saved = repo.save(model);

        // 🔔 Send notifications
        sendNotification(bearer, userId, username, "INAPP", "MODEL_CREATED_INAPP", saved, projectType);
        sendNotification(bearer, userId, username, "EMAIL", "MODEL_CREATED_EMAIL", saved, projectType);
        sendNotification(bearer, userId, username, "SMS", "MODEL_CREATED_SMS", saved, projectType);

        log.info("✅ Model created: id={} name={} by={}", saved.getModelId(), saved.getModelName(), username);

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

            String newName = patch.getModelName();
            if (!StringUtils.hasText(newName))
                throw new RuntimeException("Model name cannot be blank");

            // ✅ Preserve existing make if not provided
            if (patch.getMake() == null) {
                patch.setMake(existing.getMake());
            }

            if (patch.getMake() == null)
                throw new RuntimeException("❌ Model must have a valid make");

            // ✅ Uniqueness check
            boolean duplicate = repo.existsByModelNameIgnoreCaseAndMake_MakeId(
                    newName, patch.getMake().getMakeId());
            if (duplicate && !Objects.equals(existing.getModelName(), newName))
                throw new RuntimeException("❌ Duplicate model name for same make");

            String oldName = existing.getModelName();
            existing.setModelName(newName);
            existing.setDescription(patch.getDescription());
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
                .map(ModelMapper::toDto)
                .toList();
    }

    public Optional<ModelDto> find(Long id) {
        return repo.findById(id)
                .filter(m -> m.getActive() == null || m.getActive())
                .map(ModelMapper::toDto);
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
                    response.addFailure(i, "Model name is required");
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

                // ✅ VALIDATION: Foreign key - Make is REQUIRED (nullable = false in entity)
                ProductMake make = null;
                if (item.getMakeId() != null) {
                    // Prioritize ID over name
                    make = makeRepo.findById(item.getMakeId())
                            .filter(m -> m.getActive() == null || m.getActive())
                            .orElse(null);
                    if (make == null) {
                        response.addFailure(i, "Make not found with id: " + item.getMakeId());
                        continue;
                    }
                } else if (item.getMakeName() != null && !item.getMakeName().trim().isEmpty()) {
                    // Fallback to name lookup
                    make = makeRepo.findByMakeNameIgnoreCase(item.getMakeName().trim())
                            .filter(m -> m.getActive() == null || m.getActive())
                            .orElse(null);
                    if (make == null) {
                        response.addFailure(i, "Make not found with name: " + item.getMakeName().trim());
                        continue;
                    }
                } else {
                    response.addFailure(i, "Make is required (provide makeId or makeName)");
                    continue;
                }

                // ✅ VALIDATION: Uniqueness check (model name must be unique per make)
                boolean exists = repo.existsByModelNameIgnoreCaseAndMake_MakeId(modelName, make.getMakeId());
                if (exists) {
                    response.addFailure(i, "Model with name '" + modelName + "' already exists for this make");
                    continue;
                }

                // CREATE: Primary key is auto-generated
                ModelRequest createReq = new ModelRequest();
                createReq.setUserId(userId);
                createReq.setUsername(username);
                createReq.setProjectType(projectType);

                ProductModel model = new ProductModel();
                model.setModelName(modelName);
                // Only set description if provided (optional field)
                if (description != null) {
                    model.setDescription(description);
                }
                // Make is required
                model.setMake(make);

                createReq.setModel(model);
                ModelDto result = create(headers, createReq);
                response.addSuccess(i, result);
                log.debug("✅ Created model name={}", modelName);

            } catch (Exception e) {
                log.error("❌ Bulk model failed at index {}: {}", i, e.getMessage());
                response.addFailure(i, e.getMessage());
            }
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
    // 🔐 TOKEN VALIDATION
    // ============================================================
    private void validateAuthorization(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank())
            throw new SecurityException("❌ Missing Authorization header");
        if (!authHeader.startsWith("Bearer "))
            throw new SecurityException("❌ Invalid Authorization header format");
    }
}


