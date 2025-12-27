package com.example.asset.service;

import com.example.asset.dto.AmcRequest;
import com.example.asset.entity.AssetAmc;
import com.example.asset.repository.AssetAmcRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * ✅ AmcService
 * Handles AMC CRUD and sends notifications using SafeNotificationHelper.
 * Extracts bearer token directly from HttpHeaders (no UserService dependency).
 */
@Service
public class AmcService {

    private static final Logger log = LoggerFactory.getLogger(AmcService.class);

    private final AssetAmcRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;

    public AmcService(AssetAmcRepository repo, SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE
    // ============================================================
    @Transactional
    public AssetAmc create(HttpHeaders headers, AmcRequest request) {
        if (request == null || request.getAmc() == null)
            throw new IllegalArgumentException("AMC request or payload cannot be null");

        String bearer = extractBearer(headers);
        AssetAmc amc = request.getAmc();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        amc.setCreatedBy(username);
        amc.setUpdatedBy(username);
        AssetAmc saved = repo.save(amc);

        Map<String, Object> placeholders = Map.of(
                "amcId", saved.getAmcId(),
                "assetId", saved.getAsset() != null ? saved.getAsset().getAssetId() : null,
                "amcStatus", saved.getAmcStatus(),
                "startDate", saved.getStartDate(),
                "endDate", saved.getEndDate(),
                "createdBy", username,
                    "username", username,
                "timestamp", Instant.now().toString()
        );

        sendAmcNotification(bearer, userId, username, "INAPP", "AMC_CREATED", placeholders, projectType);
        sendAmcNotification(bearer, userId, username, "EMAIL", "AMC_CREATED_EMAIL", placeholders, projectType);

        log.info("✅ AMC created successfully: id={} by={}", saved.getAmcId(), username);
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE
    // ============================================================
    @Transactional
    public AssetAmc update(HttpHeaders headers, Long id, AmcRequest request) {
        if (request == null || request.getAmc() == null)
            throw new IllegalArgumentException("AMC request or payload cannot be null");

        String bearer = extractBearer(headers);
        AssetAmc patch = request.getAmc();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return repo.findById(id).map(existing -> {
            existing.setAmcStatus(patch.getAmcStatus());
            existing.setStartDate(patch.getStartDate());
            existing.setEndDate(patch.getEndDate());
            existing.setUpdatedBy(username);

            AssetAmc saved = repo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "amcId", saved.getAmcId(),
                    "amcStatus", saved.getAmcStatus(),
                    "startDate", saved.getStartDate(),
                    "endDate", saved.getEndDate(),
                    "updatedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString()
            );

            sendAmcNotification(bearer, userId, username, "INAPP", "AMC_UPDATED", placeholders, projectType);
            log.info("✏️ AMC updated: id={} by={}", id, username);
            return saved;
        }).orElseThrow(() -> new RuntimeException("AMC not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, AmcRequest request) {
        String bearer = extractBearer(headers);
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(amc -> {
            amc.setActive(false);
            amc.setUpdatedBy(username);
            repo.save(amc);

            Map<String, Object> placeholders = Map.of(
                    "amcId", amc.getAmcId(),
                    "assetId", amc.getAsset() != null ? amc.getAsset().getAssetId() : null,
                    "deletedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString()
            );

            sendAmcNotification(bearer, userId, username, "INAPP", "AMC_DELETED", placeholders, projectType);
            log.info("🗑️ AMC deleted: id={} by={}", amc.getAmcId(), username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<AssetAmc> listForAsset(Long assetId) {
        return repo.findAll().stream()
                .filter(a -> a.getAsset() != null && a.getAsset().getAssetId().equals(assetId))
                .filter(a -> a.getActive() == null || a.getActive())
                .toList();
    }

    public Optional<AssetAmc> find(Long id) {
        return repo.findById(id);
    }

    // ============================================================
    // 🔔 Notification Helper
    // ============================================================
    private void sendAmcNotification(String bearer,
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
                    projectType
            );
        } catch (Exception e) {
            log.error("⚠️ Notification failed [{}] for AMC: {}", templateCode, e.getMessage());
        }
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

