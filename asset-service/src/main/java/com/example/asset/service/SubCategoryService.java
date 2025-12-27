
package com.example.asset.service;

import com.example.asset.dto.BulkSubCategoryRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.ProductSubCategoryDto;
import com.example.asset.dto.SubCategoryRequest;
import com.example.asset.entity.ProductCategory;
import com.example.asset.entity.ProductSubCategory;
import com.example.asset.mapper.ProductSubCategoryMapper;
import com.example.asset.repository.ProductCategoryRepository;
import com.example.asset.repository.ProductSubCategoryRepository;
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
 * ✅ SubCategoryService
 * Handles CRUD for ProductSubCategory.
 * Extracts Bearer token directly from @RequestHeader HttpHeaders.
 */
@Service
public class SubCategoryService {

    private static final Logger log = LoggerFactory.getLogger(SubCategoryService.class);

    private final ProductSubCategoryRepository repo;
    private final ProductCategoryRepository categoryRepo;
    private final SafeNotificationHelper safeNotificationHelper;

    public SubCategoryService(ProductSubCategoryRepository repo,
                              ProductCategoryRepository categoryRepo,
                              SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.categoryRepo = categoryRepo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE SUBCATEGORY
    // ============================================================
    @Transactional
    public ProductSubCategory create(HttpHeaders headers, SubCategoryRequest request) {
        if (request == null || request.getSubCategory() == null)
            throw new IllegalArgumentException("Request or subCategory cannot be null");

        ProductSubCategory sub = request.getSubCategory();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        // ✅ Validate subcategory name
        String subCategoryName = sub.getSubCategoryName() != null ? sub.getSubCategoryName().trim() : null;
        if (!StringUtils.hasText(subCategoryName))
            throw new IllegalArgumentException("Subcategory name must not be blank");
        
        sub.setSubCategoryName(subCategoryName);

        // ✅ Check uniqueness per category (subcategories can have same name in different categories)
        Long categoryId = sub.getCategory() != null ? sub.getCategory().getCategoryId() : null;
        if (categoryId != null) {
            // Check if subcategory with same name exists in this category
            if (repo.existsBySubCategoryNameIgnoreCaseAndCategory_CategoryId(subCategoryName, categoryId)) {
                throw new IllegalArgumentException("Subcategory with name '" + subCategoryName + "' already exists in this category");
            }
        } else {
            // If no category, check global uniqueness (fallback)
            if (repo.existsBySubCategoryNameIgnoreCase(subCategoryName)) {
                throw new IllegalArgumentException("Subcategory already exists: " + subCategoryName);
            }
        }

        sub.setCreatedBy(username);
        sub.setUpdatedBy(username);
        ProductSubCategory saved = repo.save(sub);

        String bearer = extractBearerToken(headers);

        // 🔔 Prepare notification placeholders
        Map<String, Object> placeholders = new LinkedHashMap<>();
        placeholders.put("subCategoryId", saved.getSubCategoryId());
        placeholders.put("subCategoryName", saved.getSubCategoryName());
        placeholders.put("actor", username);
        placeholders.put("username", username);
        placeholders.put("timestamp", Instant.now().toString());

        sendNotification(bearer, userId, username, "INAPP", "SUBCATEGORY_CREATED_INAPP", placeholders, projectType);

        log.info("✅ Created ProductSubCategory id={} name={} by={}",
                saved.getSubCategoryId(), saved.getSubCategoryName(), username);
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE SUBCATEGORY
    // ============================================================
    @Transactional
    public ProductSubCategory update(HttpHeaders headers, Long id, SubCategoryRequest request) {
        if (request == null || request.getSubCategory() == null)
            throw new IllegalArgumentException("Request or subCategory cannot be null");

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return repo.findById(id).map(existing -> {
            String newName = request.getSubCategory().getSubCategoryName() != null 
                    ? request.getSubCategory().getSubCategoryName().trim() : null;

            if (!StringUtils.hasText(newName))
                throw new IllegalArgumentException("Subcategory name must not be blank");

            // ✅ Check uniqueness per category (case-insensitive)
            Long categoryId = existing.getCategory() != null ? existing.getCategory().getCategoryId() : null;
            boolean isDuplicate = false;
            if (categoryId != null) {
                isDuplicate = !existing.getSubCategoryName().equalsIgnoreCase(newName)
                        && repo.existsBySubCategoryNameIgnoreCaseAndCategory_CategoryId(newName, categoryId);
            } else {
                isDuplicate = !existing.getSubCategoryName().equalsIgnoreCase(newName)
                        && repo.existsBySubCategoryNameIgnoreCase(newName);
            }
            
            if (isDuplicate)
                throw new IllegalArgumentException("Subcategory already exists: " + newName);

            String oldName = existing.getSubCategoryName();
            existing.setSubCategoryName(newName);
            existing.setUpdatedBy(username);

            ProductSubCategory saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("subCategoryId", saved.getSubCategoryId());
            placeholders.put("oldName", oldName);
            placeholders.put("newName", newName);
            placeholders.put("subCategoryName", newName);
            placeholders.put("actor", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", Instant.now().toString());

            String bearer = extractBearerToken(headers);
            sendNotification(bearer, userId, username, "INAPP", "SUBCATEGORY_UPDATED_INAPP", placeholders, projectType);

            log.info("✏️ Updated SubCategory id={} newName={} by={}", id, newName, username);
            return saved;
        }).orElseThrow(() -> new IllegalArgumentException("Subcategory not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, SubCategoryRequest request) {
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(sub -> {
            sub.setActive(false);
            sub.setUpdatedBy(username);
            ProductSubCategory saved = repo.save(sub);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("subCategoryId", saved.getSubCategoryId());
            placeholders.put("subCategoryName", saved.getSubCategoryName());
            placeholders.put("actor", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", Instant.now().toString());

            String bearer = extractBearerToken(headers);
            sendNotification(bearer, userId, username, "INAPP", "SUBCATEGORY_DELETED_INAPP", placeholders, projectType);

            log.info("🗑️ SubCategory soft-deleted id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST + FIND
    // ============================================================
    public List<ProductSubCategory> list() {
        return repo.findAll().stream()
                .filter(s -> s.getActive() == null || s.getActive())
                .toList();
    }

    public Optional<ProductSubCategory> find(Long id) {
        return repo.findById(id)
                .filter(s -> s.getActive() == null || s.getActive());
    }

    // ============================================================
    // 📦 BULK UPLOAD SUBCATEGORIES (NEW - using BulkSubCategoryRequest)
    // ============================================================
    @Transactional
    public BulkUploadResponse<ProductSubCategoryDto> bulkCreate(HttpHeaders headers, BulkSubCategoryRequest bulkRequest) {
        BulkUploadResponse<ProductSubCategoryDto> response = new BulkUploadResponse<>();
        
        if (bulkRequest == null || bulkRequest.getSubCategories() == null) {
            throw new IllegalArgumentException("Bulk request cannot be null");
        }

        List<BulkSubCategoryRequest.SimpleSubCategoryDto> items = bulkRequest.getSubCategories();
        response.setTotalCount(items.size());

        String username = bulkRequest.getUsername();
        Long userId = bulkRequest.getUserId();
        String projectType = Optional.ofNullable(bulkRequest.getProjectType()).orElse("ASSET_SERVICE");

        for (int i = 0; i < items.size(); i++) {
            try {
                BulkSubCategoryRequest.SimpleSubCategoryDto item = items.get(i);

                // ✅ VALIDATION: Required field
                if (item.getSubCategoryName() == null || item.getSubCategoryName().trim().isEmpty()) {
                    response.addFailure(i, "SubCategory name is required");
                    continue;
                }

                String subCategoryName = item.getSubCategoryName().trim();

                // ✅ VALIDATION: Name length (max 200 chars per entity constraint)
                if (subCategoryName.length() > 200) {
                    response.addFailure(i, "SubCategory name exceeds maximum length of 200 characters");
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

                // ✅ VALIDATION: Foreign key - Category must exist (resolve first for duplicate check)
                ProductCategory category = null;
                if (item.getCategoryId() != null) {
                    // Prioritize ID over name
                    category = categoryRepo.findById(item.getCategoryId())
                            .filter(c -> c.getActive() == null || c.getActive())
                            .orElse(null);
                    if (category == null) {
                        response.addFailure(i, "Category not found with id: " + item.getCategoryId());
                        continue;
                    }
                } else if (item.getCategoryName() != null && !item.getCategoryName().trim().isEmpty()) {
                    // Fallback to name lookup
                    category = categoryRepo.findByCategoryNameIgnoreCase(item.getCategoryName().trim())
                            .filter(c -> c.getActive() == null || c.getActive())
                            .orElse(null);
                    if (category == null) {
                        response.addFailure(i, "Category not found with name: " + item.getCategoryName().trim());
                        continue;
                    }
                }
                // Note: Category is optional per business rules, but if provided, it must exist

                // ✅ VALIDATION: Name uniqueness per category (case-insensitive)
                // Subcategories can have same name in different categories
                // Check after category is resolved
                boolean isDuplicate = false;
                if (category != null) {
                    isDuplicate = repo.existsBySubCategoryNameIgnoreCaseAndCategory_CategoryId(subCategoryName, category.getCategoryId());
                } else {
                    // If no category, check global uniqueness (fallback)
                    isDuplicate = repo.existsBySubCategoryNameIgnoreCase(subCategoryName);
                }
                
                if (isDuplicate) {
                    String errorMsg = category != null 
                            ? "SubCategory with name '" + subCategoryName + "' already exists in this category"
                            : "SubCategory with name '" + subCategoryName + "' already exists";
                    response.addFailure(i, errorMsg);
                    continue;
                }

                // CREATE: Primary key is auto-generated
                SubCategoryRequest createReq = new SubCategoryRequest();
                createReq.setUserId(userId);
                createReq.setUsername(username);
                createReq.setProjectType(projectType);

                ProductSubCategory subCategory = new ProductSubCategory();
                subCategory.setSubCategoryName(subCategoryName);
                // Only set description if provided (optional field)
                if (description != null) {
                    subCategory.setDescription(description);
                }
                // Only set category if provided (optional field)
                if (category != null) {
                    subCategory.setCategory(category);
                }

                createReq.setSubCategory(subCategory);
                ProductSubCategory created = create(headers, createReq);
                // Convert entity to DTO to include all optional fields in JSON response
                ProductSubCategoryDto result = ProductSubCategoryMapper.toDto(created);
                response.addSuccess(i, result);
                log.debug("✅ Created subcategory name={}", subCategoryName);

            } catch (Exception e) {
                log.error("❌ Bulk subcategory failed at index {}: {}", i, e.getMessage());
                response.addFailure(i, e.getMessage());
            }
        }

        log.info("📦 Bulk subcategory upload: {}/{} success",
                response.getSuccessCount(), response.getTotalCount());
        return response;
    }

    // ============================================================
    // 🔐 TOKEN EXTRACTOR
    // ============================================================
    private String extractBearerToken(HttpHeaders headers) {
        String authorization = headers.getFirst("Authorization");
        if (authorization == null || authorization.isBlank()) {
            throw new RuntimeException("❌ Missing Authorization header");
        }
        return authorization.startsWith("Bearer ") ? authorization : "Bearer " + authorization;
    }

    // ============================================================
    // 🔔 NOTIFICATION HELPER
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
                    bearer, userId, username, null, null,
                    channel, templateCode, placeholders, projectType);
        } catch (Exception e) {
            log.error("⚠️ Notification ({}) failed: {}", templateCode, e.getMessage());
        }
    }
}


