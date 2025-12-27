


package com.example.asset.service;

import com.example.asset.dto.WarrantyRequest;
import com.example.asset.entity.AssetWarranty;
import com.example.asset.repository.AssetWarrantyRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * ✅ WarrantyService
 * Handles CRUD for AssetWarranty with SafeNotificationHelper.
 * Uses Authorization header token for secure notifications.
 */
@Service
public class WarrantyService {

    private static final Logger log = LoggerFactory.getLogger(WarrantyService.class);

    private final AssetWarrantyRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;

    public WarrantyService(AssetWarrantyRepository repo,
                           SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE WARRANTY
    // ============================================================
    @Transactional
    public AssetWarranty create(HttpHeaders headers, WarrantyRequest request) {
        validateRequest(headers, request);

        String bearer = extractBearer(headers);
        String username = request.getUsername();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        AssetWarranty warranty = request.getWarranty();
        warranty.setCreatedBy(username);
        warranty.setUpdatedBy(username);
        AssetWarranty saved = repo.save(warranty);

        Map<String, Object> placeholders = Map.of(
                "warrantyId", saved.getWarrantyId(),
                "assetId", saved.getAsset() != null ? saved.getAsset().getAssetId() : null,
                "startDate", saved.getWarrantyStartDate(),
                "endDate", saved.getWarrantyEndDate(),
                "createdBy", username,
                    "username", username,
                "timestamp", Instant.now().toString()
        );

        sendNotification(bearer, request.getUserId(), username, placeholders,
                "WARRANTY_CREATED", projectType);

        log.info("✅ Warranty created successfully: id={} by={}", saved.getWarrantyId(), username);
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE WARRANTY
    // ============================================================
    @Transactional
    public AssetWarranty update(HttpHeaders headers, Long id, WarrantyRequest request) {
        validateRequest(headers, request);

        String bearer = extractBearer(headers);
        String username = request.getUsername();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return repo.findById(id).map(existing -> {
            existing.setWarrantyStartDate(request.getWarranty().getWarrantyStartDate());
            existing.setWarrantyEndDate(request.getWarranty().getWarrantyEndDate());
            existing.setActive(request.getWarranty().getActive());
            existing.setUpdatedBy(username);

            AssetWarranty saved = repo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "warrantyId", saved.getWarrantyId(),
                    "assetId", saved.getAsset() != null ? saved.getAsset().getAssetId() : null,
                    "status", saved.getActive(),
                    "updatedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString()
            );

            sendNotification(bearer, request.getUserId(), username, placeholders,
                    "WARRANTY_UPDATED", projectType);

            log.info("✏️ Warranty updated successfully: id={} by={}", id, username);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Warranty not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE WARRANTY
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, WarrantyRequest request) {
        validateRequest(headers, request);

        String bearer = extractBearer(headers);
        String username = request.getUsername();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(warranty -> {
            warranty.setActive(false);
            warranty.setUpdatedBy(username);
            AssetWarranty saved = repo.save(warranty);

            Map<String, Object> placeholders = Map.of(
                    "warrantyId", saved.getWarrantyId(),
                    "assetId", saved.getAsset() != null ? saved.getAsset().getAssetId() : null,
                    "deletedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString()
            );

            sendNotification(bearer, request.getUserId(), username, placeholders,
                    "WARRANTY_DELETED", projectType);

            log.info("🗑️ Warranty soft deleted: id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST + FIND
    // ============================================================
    public List<AssetWarranty> list() {
        return repo.findAll();
    }

    public Optional<AssetWarranty> find(Long id) {
        return repo.findById(id);
    }

    // ============================================================
    // 🔔 Notification Helper
    // ============================================================
    private void sendNotification(String bearer,
                                  Long userId,
                                  String username,
                                  Map<String, Object> placeholders,
                                  String templateCode,
                                  String projectType) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer, userId, username, null, null,
                    "INAPP", templateCode, placeholders, projectType
            );
            log.info("📩 Notification sent [{}] for {}", templateCode, username);
        } catch (Exception e) {
            log.error("⚠️ Failed to send {} notification: {}", templateCode, e.getMessage());
        }
    }

    // ============================================================
    // 🧩 Helper Methods
    // ============================================================
    private void validateRequest(HttpHeaders headers, WarrantyRequest request) {
        if (headers == null || headers.getFirst("Authorization") == null) {
            throw new RuntimeException("❌ Missing Authorization header");
        }
        if (request == null || request.getWarranty() == null) {
            throw new RuntimeException("❌ Invalid request or missing warranty payload");
        }
    }

    private String extractBearer(HttpHeaders headers) {
        String token = headers.getFirst("Authorization");
        if (token == null || token.isBlank()) {
            throw new RuntimeException("❌ Missing or invalid Authorization header");
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}






