
package com.example.asset.service;

import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.CategoryDto;
import com.example.asset.dto.CategoryRequest;
import com.example.asset.dto.BulkCategoryRequest;
import com.example.asset.entity.ProductCategory;
import com.example.asset.mapper.CategoryMapper;
import com.example.asset.repository.ProductCategoryRepository;
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
 * ✅ CategoryService
 * Handles CRUD operations for ProductCategory entities.
 * Uses CategoryMapper for DTO conversions and SafeNotificationHelper for async notifications.
 */
@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final ProductCategoryRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;

    public CategoryService(ProductCategoryRepository repo,
                           SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE CATEGORY
    // ============================================================
    @Transactional
    public CategoryDto create(HttpHeaders headers, CategoryRequest request) {
        if (request == null || request.getCategory() == null)
            throw new IllegalArgumentException("Request body or category cannot be null");

        String bearer = extractBearer(headers);
        ProductCategory payload = request.getCategory();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        String name = normalizeName(payload.getCategoryName());
        if (!StringUtils.hasText(name))
            throw new IllegalArgumentException("Category name must not be blank");
        // ✅ Use case-insensitive check to prevent duplicates like "Electronics" and "electronics"
        if (repo.existsByCategoryNameIgnoreCase(name))
            throw new IllegalArgumentException("Category already exists: " + name);

        ProductCategory entity = new ProductCategory(name);
        entity.setSequenceOrder(payload.getSequenceOrder());
        entity.setDescription(payload.getDescription());
        entity.setIsFavourite(payload.getIsFavourite() != null ? payload.getIsFavourite() : false);
        entity.setIsMostLike(payload.getIsMostLike() != null ? payload.getIsMostLike() : false);
        entity.setCreatedBy(username);
        entity.setUpdatedBy(username);

        ProductCategory saved = repo.save(entity);

        // 🔔 Prepare placeholders
        Map<String, Object> placeholders = Map.of(
                "categoryId", saved.getCategoryId(),
                "categoryName", saved.getCategoryName(),
                "actor", username,
                "username", username,
                "timestamp", Instant.now().toString()
        );

        sendNotification(bearer, userId, username, "INAPP", "CATEGORY_CREATED_INAPP", placeholders, projectType);
        sendNotification(bearer, userId, username, "EMAIL", "CATEGORY_CREATED_EMAIL", placeholders, projectType);

        log.info("✅ Category created: id={} name={} by={}", saved.getCategoryId(), name, username);

        return CategoryMapper.toDto(saved);
    }

    // ============================================================
    // ✏️ UPDATE CATEGORY
    // ============================================================
    @Transactional
    public CategoryDto update(HttpHeaders headers, Long id, CategoryRequest request) {
        if (request == null || request.getCategory() == null)
            throw new IllegalArgumentException("Request body or category cannot be null");

        String bearer = extractBearer(headers);
        ProductCategory patch = request.getCategory();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return repo.findById(id).map(existing -> {
            String newName = normalizeName(patch.getCategoryName());
            if (!StringUtils.hasText(newName))
                throw new IllegalArgumentException("Category name must not be blank");

            // ✅ Use case-insensitive check to prevent duplicates
            if (!existing.getCategoryName().equalsIgnoreCase(newName)
                    && repo.existsByCategoryNameIgnoreCase(newName)) {
                throw new IllegalArgumentException("Category already exists: " + newName);
            }

            String oldName = existing.getCategoryName();
            existing.setCategoryName(newName);
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
            existing.setUpdatedBy(username);
            ProductCategory saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("categoryId", saved.getCategoryId());
            placeholders.put("oldName", oldName);
            placeholders.put("newName", newName);
            placeholders.put("categoryName", newName);
            placeholders.put("actor", username);
            placeholders.put("username", username);
            placeholders.put("timestamp", Instant.now().toString());

            sendNotification(bearer, userId, username, "INAPP", "CATEGORY_UPDATED_INAPP", placeholders, projectType);
            log.info("✏️ Category updated: id={} oldName={} newName={} by={}", id, oldName, newName, username);

            return CategoryMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE CATEGORY
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, CategoryRequest request) {
        if (request == null)
            throw new IllegalArgumentException("Request body cannot be null");

        String bearer = extractBearer(headers);
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(category -> {
            category.setActive(false);
            category.setUpdatedBy(username);
            ProductCategory saved = repo.save(category);

            Map<String, Object> placeholders = Map.of(
                    "categoryId", saved.getCategoryId(),
                    "categoryName", saved.getCategoryName(),
                    "deletedBy", username,
                    "username", username,
                    "timestamp", Instant.now().toString()
            );

            sendNotification(bearer, userId, username, "INAPP", "CATEGORY_DELETED_INAPP", placeholders, projectType);
            log.info("🗑️ Category soft-deleted: id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<CategoryDto> list() {
        return repo.findAll().stream()
                .filter(c -> c.getActive() == null || c.getActive())
                .sorted((a, b) -> {
                    // Priority: 1. isMostLike (true first), 2. isFavourite (true first), 3. sequenceOrder (lower first), 4. categoryName
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
                        return a.getCategoryName().compareToIgnoreCase(b.getCategoryName());
                    }
                    if (seqA == null) return 1;
                    if (seqB == null) return -1;
                    int seqCompare = seqA.compareTo(seqB);
                    return seqCompare != 0 ? seqCompare : a.getCategoryName().compareToIgnoreCase(b.getCategoryName());
                })
                .map(CategoryMapper::toDto)
                .toList();
    }

    public Optional<CategoryDto> find(Long id) {
        return repo.findById(id)
                .filter(c -> c.getActive() == null || c.getActive())
                .map(CategoryMapper::toDto);
    }

   // ============================================================
// 🆕 NEW BULK CREATE USING BulkCategoryRequest (non-breaking)
// ============================================================
@Transactional
public BulkUploadResponse<CategoryDto> bulkCreate(
        HttpHeaders headers,
        BulkCategoryRequest bulkRequest) {

    BulkUploadResponse<CategoryDto> response = new BulkUploadResponse<>();

    if (bulkRequest == null || bulkRequest.getCategories() == null) {
        throw new IllegalArgumentException("Bulk request cannot be null");
    }

    List<BulkCategoryRequest.SimpleCategoryDto> items = bulkRequest.getCategories();
    response.setTotalCount(items.size());

    String username = bulkRequest.getUsername();
    Long userId = bulkRequest.getUserId();
    String projectType = Optional.ofNullable(bulkRequest.getProjectType()).orElse("ASSET_SERVICE");

    for (int i = 0; i < items.size(); i++) {
        try {
            BulkCategoryRequest.SimpleCategoryDto item = items.get(i);

            // ✅ VALIDATION: Required field
            if (item.getCategoryName() == null || item.getCategoryName().trim().isEmpty()) {
                response.addFailure(i, "Category name is required");
                continue;
            }

            String categoryName = normalizeName(item.getCategoryName());

            // ✅ VALIDATION: Name length (max 200 chars per entity constraint)
            if (categoryName.length() > 200) {
                response.addFailure(i, "Category name exceeds maximum length of 200 characters");
                continue;
            }

            // ✅ VALIDATION: Name uniqueness (case-insensitive check to prevent duplicates)
            if (repo.existsByCategoryNameIgnoreCase(categoryName)) {
                response.addFailure(i, "Category with name '" + categoryName + "' already exists");
                continue;
            }

            // ✅ VALIDATION: Description length (max 500 chars per entity constraint)
            String description = null;
            if (item.getDescription() != null && !item.getDescription().trim().isEmpty()) {
                description = item.getDescription().trim();
                if (description.length() > 500) {
                    response.addFailure(i, "Description exceeds maximum length of 500 characters");
                    continue;
                }
            }

            // CREATE: Primary key is auto-generated
            CategoryRequest createReq = new CategoryRequest();
            createReq.setUserId(userId);
            createReq.setUsername(username);
            createReq.setProjectType(projectType);

            ProductCategory pc = new ProductCategory();
            pc.setCategoryName(categoryName);
            // Only set description if provided (optional field)
            if (description != null) {
                pc.setDescription(description);
            }

            createReq.setCategory(pc);

            CategoryDto result = create(headers, createReq);
            response.addSuccess(i, result);
            log.debug("✅ Created category name={}", categoryName);

        } catch (Exception e) {
            log.error("❌ Bulk category failed at index {}: {}", i, e.getMessage());
            response.addFailure(i, e.getMessage());
        }
    }

    log.info("📦 Bulk category upload: {}/{} success",
            response.getSuccessCount(), response.getTotalCount());

    return response;
}


    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for a category (accessible to all authenticated users)
     */
    @Transactional
    public CategoryDto updateFavourite(HttpHeaders headers, Long id, Boolean isFavourite) {
        String bearer = extractBearer(headers);
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return repo.findById(id).map(existing -> {
            existing.setIsFavourite(isFavourite != null ? isFavourite : false);
            existing.setUpdatedBy(username);
            ProductCategory saved = repo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "categoryId", saved.getCategoryId(),
                    "categoryName", saved.getCategoryName(),
                    "isFavourite", saved.getIsFavourite(),
                    "actor", username,
                    "timestamp", Instant.now().toString()
            );

            sendNotification(bearer, userId, username, "INAPP", "CATEGORY_FAVOURITE_UPDATED_INAPP", placeholders, projectType);
            log.info("⭐ Category favourite updated: id={} isFavourite={} by={}", id, isFavourite, username);

            return CategoryMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    /**
     * Toggle most like status for a category (accessible to all authenticated users)
     */
    @Transactional
    public CategoryDto updateMostLike(HttpHeaders headers, Long id, Boolean isMostLike) {
        String bearer = extractBearer(headers);
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return repo.findById(id).map(existing -> {
            existing.setIsMostLike(isMostLike != null ? isMostLike : false);
            existing.setUpdatedBy(username);
            ProductCategory saved = repo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "categoryId", saved.getCategoryId(),
                    "categoryName", saved.getCategoryName(),
                    "isMostLike", saved.getIsMostLike(),
                    "actor", username,
                    "timestamp", Instant.now().toString()
            );

            sendNotification(bearer, userId, username, "INAPP", "CATEGORY_MOST_LIKE_UPDATED_INAPP", placeholders, projectType);
            log.info("⭐ Category most like updated: id={} isMostLike={} by={}", id, isMostLike, username);

            return CategoryMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    /**
     * Update sequence order for a category (admin only)
     */
    @Transactional
    public CategoryDto updateSequenceOrder(HttpHeaders headers, Long id, Integer sequenceOrder) {
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
            ProductCategory saved = repo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "categoryId", saved.getCategoryId(),
                    "categoryName", saved.getCategoryName(),
                    "sequenceOrder", saved.getSequenceOrder() != null ? saved.getSequenceOrder() : 0,
                    "actor", username,
                    "timestamp", Instant.now().toString()
            );

            sendNotification(bearer, userId, username, "INAPP", "CATEGORY_SEQUENCE_UPDATED_INAPP", placeholders, projectType);
            log.info("📊 Category sequence order updated: id={} sequenceOrder={} by={}", id, sequenceOrder, username);

            return CategoryMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    // ============================================================
    // 🔔 Notification Helper
    // ============================================================
    private void sendNotification(String bearer,
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
            log.error("⚠️ Notification failed [{}]: {}", templateCode, e.getMessage());
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

    // ============================================================
    // 🧩 Utility
    // ============================================================
    private String normalizeName(String raw) {
        return (raw != null) ? raw.trim() : null;
    }
}

