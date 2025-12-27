
package com.example.asset.service;

import com.example.asset.dto.BulkComponentRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.ComponentDto;
import com.example.asset.dto.ComponentRequest;
import com.example.asset.entity.AssetComponent;
import com.example.asset.mapper.ComponentMapper;
import com.example.asset.repository.AssetComponentRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

/**
 * ✅ ComponentService
 * - Handles CRUD for Asset Components
 * - Uses SafeNotificationHelper for INAPP, EMAIL, and SMS notifications
 * - Extracts token from HttpHeaders (Authorization header)
 */
@Service
public class ComponentService {

    private static final Logger log = LoggerFactory.getLogger(ComponentService.class);

    private final AssetComponentRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;

    public ComponentService(AssetComponentRepository repo,
                            SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE COMPONENT
    // ============================================================
    @Transactional
    public AssetComponent create(HttpHeaders headers, ComponentRequest request) {
        if (request == null || request.getComponent() == null)
            throw new IllegalArgumentException("Request or component cannot be null");

        String bearer = extractBearer(headers);
        AssetComponent component = request.getComponent();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        String componentName = component.getComponentName() != null ? component.getComponentName().trim() : null;
        if (!StringUtils.hasText(componentName))
            throw new RuntimeException("❌ Component name cannot be blank");
        
        component.setComponentName(componentName);

        // ✅ Use case-insensitive check to prevent duplicates like "RAM" and "ram"
        if (repo.existsByComponentNameIgnoreCase(componentName))
            throw new RuntimeException("❌ Component with name '" + componentName + "' already exists");

        component.setCreatedBy(username);
        component.setUpdatedBy(username);
        AssetComponent saved = repo.save(component);

        Map<String, Object> placeholders = new LinkedHashMap<>();
        placeholders.put("componentId", saved.getComponentId());
        placeholders.put("componentName", saved.getComponentName());
        placeholders.put("createdBy", username);
        placeholders.put("username", username);
        placeholders.put("timestamp", Instant.now().toString());

        // 🔔 Notify across channels
        sendMultiChannelNotification(bearer, userId, username, placeholders, projectType, "COMPONENT_CREATED");

        log.info("✅ Component created successfully: id={} name={} by={}",
                saved.getComponentId(), saved.getComponentName(), username);
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE COMPONENT
    // ============================================================
    @Transactional
    public AssetComponent update(HttpHeaders headers, Long id, ComponentRequest request) {
        if (request == null || request.getComponent() == null)
            throw new IllegalArgumentException("Request or component cannot be null");

        String bearer = extractBearer(headers);
        return repo.findById(id).map(existing -> {
            AssetComponent patch = request.getComponent();
            String username = request.getUsername();
            Long userId = request.getUserId();
            String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

            String newName = patch.getComponentName();

            if (!existing.getComponentName().equalsIgnoreCase(newName)
                    && repo.existsByComponentName(newName))
                throw new RuntimeException("❌ Component with name '" + newName + "' already exists");

            String oldName = existing.getComponentName();
            existing.setComponentName(newName);
            existing.setDescription(patch.getDescription());
            existing.setUpdatedBy(username);
            AssetComponent saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("componentId", saved.getComponentId());
            placeholders.put("oldName", oldName);
            placeholders.put("newName", newName);
            placeholders.put("componentName", newName);
            placeholders.put("updatedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", Instant.now().toString());

            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType, "COMPONENT_UPDATED");

            log.info("✏️ Component updated successfully: id={} oldName={} newName={} by={}",
                    id, oldName, newName, username);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Component not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE COMPONENT
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, ComponentRequest request) {
        String bearer = extractBearer(headers);
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(c -> {
            c.setActive(false);
            c.setUpdatedBy(username);
            repo.save(c);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("componentId", c.getComponentId());
            placeholders.put("componentName", c.getComponentName());
            placeholders.put("deletedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", Instant.now().toString());

            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType, "COMPONENT_DELETED");
            log.info("🗑️ Component soft-deleted successfully: id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<AssetComponent> list() {
        return repo.findAll().stream()
                .filter(c -> c.getActive() == null || c.getActive())
                .toList();
    }

    public Optional<AssetComponent> find(Long id) {
        return repo.findById(id).filter(c -> c.getActive() == null || c.getActive());
    }

    // ============================================================
    // 📦 BULK UPLOAD COMPONENTS (NEW - using BulkComponentRequest)
    // ============================================================
    @Transactional
    public BulkUploadResponse<ComponentDto> bulkCreate(HttpHeaders headers, BulkComponentRequest bulkRequest) {
        BulkUploadResponse<ComponentDto> response = new BulkUploadResponse<>();
        
        if (bulkRequest == null || bulkRequest.getComponents() == null) {
            throw new IllegalArgumentException("Bulk request cannot be null");
        }

        List<BulkComponentRequest.SimpleComponentDto> items = bulkRequest.getComponents();
        response.setTotalCount(items.size());

        String username = bulkRequest.getUsername();
        Long userId = bulkRequest.getUserId();
        String projectType = Optional.ofNullable(bulkRequest.getProjectType()).orElse("ASSET_SERVICE");

        for (int i = 0; i < items.size(); i++) {
            try {
                BulkComponentRequest.SimpleComponentDto item = items.get(i);

                // ✅ VALIDATION: Required field
                if (item.getComponentName() == null || item.getComponentName().trim().isEmpty()) {
                    response.addFailure(i, "Component name is required");
                    continue;
                }

                String componentName = item.getComponentName().trim();

                // ✅ VALIDATION: Name length (reasonable max, typically 200 chars)
                if (componentName.length() > 200) {
                    response.addFailure(i, "Component name exceeds maximum length of 200 characters");
                    continue;
                }

                // ✅ VALIDATION: Name uniqueness (case-insensitive check to prevent duplicates)
                if (repo.existsByComponentNameIgnoreCase(componentName)) {
                    response.addFailure(i, "Component with name '" + componentName + "' already exists");
                    continue;
                }

                // ✅ VALIDATION: Description length (reasonable max, typically 500 chars)
                String description = null;
                if (item.getDescription() != null && !item.getDescription().trim().isEmpty()) {
                    description = item.getDescription().trim();
                    if (description.length() > 500) {
                        response.addFailure(i, "Description exceeds maximum length of 500 characters");
                        continue;
                    }
                }

                // CREATE: Primary key is auto-generated
                ComponentRequest createReq = new ComponentRequest();
                createReq.setUserId(userId);
                createReq.setUsername(username);
                createReq.setProjectType(projectType);

                AssetComponent component = new AssetComponent();
                component.setComponentName(componentName);
                // Only set description if provided (optional field)
                if (description != null) {
                    component.setDescription(description);
                }

                createReq.setComponent(component);
                AssetComponent created = create(headers, createReq);
                // Convert entity to DTO to include all optional fields in JSON response
                ComponentDto result = ComponentMapper.toDto(created);
                response.addSuccess(i, result);
                log.debug("✅ Created component name={}", componentName);

            } catch (Exception e) {
                log.error("❌ Bulk component failed at index {}: {}", i, e.getMessage());
                response.addFailure(i, e.getMessage());
            }
        }

        log.info("📦 Bulk component upload: {}/{} success",
                response.getSuccessCount(), response.getTotalCount());
        return response;
    }

    // ============================================================
    // 🔔 Notification Helper
    // ============================================================
    private void sendMultiChannelNotification(String bearer,
                                              Long uid,
                                              String username,
                                              Map<String, Object> placeholders,
                                              String projectType,
                                              String templateCode) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, null, null,
                    "INAPP", templateCode+"_INAPP", placeholders, projectType);

            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, username + "@example.com", null,
                    "EMAIL", templateCode + "_EMAIL", placeholders, projectType);

            String mockMobile = "99999999" + (uid != null ? String.valueOf(uid % 100) : "00");
            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, null, mockMobile,
                    "SMS", templateCode + "_SMS", placeholders, projectType);

            log.info("📤 Notifications sent for template={} (INAPP + EMAIL + SMS)", templateCode);
        } catch (Exception e) {
            log.error("⚠️ Failed to send {} notifications: {}", templateCode, e.getMessage());
        }
    }

    // ============================================================
    // 🔐 Token Extractor
    // ============================================================
    private String extractBearer(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank())
            throw new RuntimeException("❌ Missing Authorization header");
        return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
    }
}

