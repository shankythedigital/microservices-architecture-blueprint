
package com.example.asset.service;

import com.example.asset.dto.BulkMakeRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.MakeDto;
import com.example.asset.dto.MakeRequest;
import com.example.asset.entity.ProductMake;
import com.example.asset.entity.ProductSubCategory;
import com.example.asset.mapper.MakeMapper;
import com.example.asset.repository.ProductMakeRepository;
import com.example.asset.repository.ProductSubCategoryRepository;
import com.example.common.client.AdminClient;
import com.example.common.client.AssetUserLinkClient;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * ✅ MakeService
 * Token-based CRUD + notifications for ProductMake.
 * Removes UserService dependency, validates Authorization header.
 */
@Service
public class MakeService {

    private static final Logger log = LoggerFactory.getLogger(MakeService.class);

    private final ProductMakeRepository repo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final SafeNotificationHelper safeNotificationHelper;
    private final AdminClient adminClient;
    private final AssetUserLinkClient assetUserLinkClient;

    public MakeService(ProductMakeRepository repo,
                       ProductSubCategoryRepository subCategoryRepo,
                       SafeNotificationHelper safeNotificationHelper,
                       AdminClient adminClient,
                       AssetUserLinkClient assetUserLinkClient) {
        this.repo = repo;
        this.subCategoryRepo = subCategoryRepo;
        this.safeNotificationHelper = safeNotificationHelper;
        this.adminClient = adminClient;
        this.assetUserLinkClient = assetUserLinkClient;
    }

    // ============================================================
    // 🟢 CREATE MAKE
    // ============================================================
    @Transactional
    public ProductMake create(HttpHeaders headers, MakeRequest request) {
        return create(headers, request, false);
    }

    @Transactional
    public ProductMake create(HttpHeaders headers, MakeRequest request, boolean skipNotifications) {
        validateAuthorization(headers);

        if (request == null || request.getMake() == null)
            throw new IllegalArgumentException("Request or make cannot be null");

        ProductMake make = request.getMake();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        // ✅ Validate make name
        String makeName = make.getMakeName() != null ? make.getMakeName().trim() : null;
        if (!StringUtils.hasText(makeName))
            throw new RuntimeException("Make name cannot be blank");
        
        make.setMakeName(makeName);

        // ✅ Enforce uniqueness per subcategory (case-insensitive, efficient repository check)
        Long makeSubCategoryId = make.getSubCategory() != null ? make.getSubCategory().getSubCategoryId() : null;
        if (makeSubCategoryId != null) {
            if (repo.existsByMakeNameIgnoreCaseAndSubCategory_SubCategoryId(makeName, makeSubCategoryId)) {
                throw new RuntimeException("❌ Make with name '" + makeName + "' already exists in this subcategory");
            }
        } else {
            // If no subcategory, check global uniqueness (fallback)
            if (repo.findByMakeNameIgnoreCase(makeName).isPresent()) {
                throw new RuntimeException("❌ Make with name '" + makeName + "' already exists");
            }
        }

        make.setCreatedBy(username);
        make.setUpdatedBy(username);
        ProductMake saved = repo.save(make);

        if (!skipNotifications) {
            Long savedSubCategoryId = saved.getSubCategory() != null ? saved.getSubCategory().getSubCategoryId() : null;
            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("makeId", saved.getMakeId());
            placeholders.put("makeName", saved.getMakeName());
            placeholders.put("subCategoryId", savedSubCategoryId);
            placeholders.put("createdBy", username);
            placeholders.put("username", username);
            placeholders.put("timestamp", new Date().toString());
            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType, "MAKE_CREATED", "Make created successfully");
            notifyAdmins(bearer, projectType, placeholders, "MAKE_CREATED_ADMIN", username);
            if (savedSubCategoryId != null) {
                notifyLinkedUsers(bearer, savedSubCategoryId, placeholders, "MAKE_CREATED_USER", username, projectType);
            }
            log.info("✅ Make created successfully: id={} name={} by={}", saved.getMakeId(), saved.getMakeName(), username);
        }
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE MAKE
    // ============================================================
    @Transactional
    public ProductMake update(HttpHeaders headers, Long id, MakeRequest request) {
        validateAuthorization(headers);

        if (request == null || request.getMake() == null)
            throw new IllegalArgumentException("Request or make cannot be null");

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        return repo.findById(id).map(existing -> {
            String newName = request.getMake().getMakeName() != null ? request.getMake().getMakeName().trim() : null;
            if (!StringUtils.hasText(newName))
                throw new RuntimeException("Make name cannot be blank");

            // ✅ Duplicate check: case-insensitive, exclude current make (repository-based)
            Long subCategoryId = request.getMake().getSubCategory() != null
                    ? request.getMake().getSubCategory().getSubCategoryId()
                    : null;
            boolean duplicate;
            if (subCategoryId != null) {
                duplicate = repo.existsByMakeNameIgnoreCaseAndSubCategory_SubCategoryIdAndMakeIdNot(newName, subCategoryId, id);
            } else {
                duplicate = repo.existsByMakeNameIgnoreCaseAndMakeIdNot(newName, id);
            }
            if (duplicate)
                throw new RuntimeException(subCategoryId != null
                        ? "❌ Make with name '" + newName + "' already exists in this subcategory"
                        : "❌ Make with name '" + newName + "' already exists");

            existing.setMakeName(newName);
            existing.setSubCategory(request.getMake().getSubCategory());
            existing.setUpdatedBy(username);
            ProductMake saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("makeId", saved.getMakeId());
            placeholders.put("oldName", existing.getMakeName());
            placeholders.put("newName", newName);
            placeholders.put("makeName", newName);
            placeholders.put("updatedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", new Date().toString());

            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType,
                    "MAKE_UPDATED", "Make updated successfully");

            log.info("✏️ Make updated: id={} name={} by={}", id, newName, username);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Make not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE MAKE
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, MakeRequest request) {
        validateAuthorization(headers);

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        repo.findById(id).ifPresent(m -> {
            m.setActive(false);
            m.setUpdatedBy(username);
            repo.save(m);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("makeId", m.getMakeId());
            placeholders.put("makeName", m.getMakeName());
            placeholders.put("deletedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", new Date().toString());

            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType,
                    "MAKE_DELETED", "Make deleted successfully");

            log.info("🗑️ Make soft-deleted id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<ProductMake> list() {
        return repo.findAll().stream()
                .filter(m -> m.getActive() == null || m.getActive())
                .toList();
    }

    public Optional<ProductMake> find(Long id) {
        return repo.findById(id)
                .filter(m -> m.getActive() == null || m.getActive());
    }

    // ============================================================
    // 📦 BULK UPLOAD MAKES (NEW - using BulkMakeRequest)
    // ============================================================
    @Transactional
    public BulkUploadResponse<MakeDto> bulkCreate(HttpHeaders headers, BulkMakeRequest bulkRequest) {
        BulkUploadResponse<MakeDto> response = new BulkUploadResponse<>();
        
        if (bulkRequest == null || bulkRequest.getMakes() == null) {
            throw new IllegalArgumentException("Bulk request cannot be null");
        }

        List<BulkMakeRequest.SimpleMakeDto> items = bulkRequest.getMakes();
        response.setTotalCount(items.size());

        String username = bulkRequest.getUsername();
        Long userId = bulkRequest.getUserId();
        String projectType = Optional.ofNullable(bulkRequest.getProjectType()).orElse("ASSET_SERVICE");

        for (int i = 0; i < items.size(); i++) {
            try {
                BulkMakeRequest.SimpleMakeDto item = items.get(i);

                // ✅ VALIDATION: Required field
                if (item.getMakeName() == null || item.getMakeName().trim().isEmpty()) {
                    response.addSkipped(i, "Make name is required");
                    continue;
                }

                String makeName = item.getMakeName().trim();

                // ✅ VALIDATION: Name length (reasonable max, typically 200 chars)
                if (makeName.length() > 200) {
                    response.addFailure(i, "Make name exceeds maximum length of 200 characters");
                    continue;
                }

                // ✅ VALIDATION: Foreign key - SubCategory must exist if provided
                ProductSubCategory subCategory = null;
                if (item.getSubCategoryId() != null) {
                    // Prioritize ID over name
                    Optional<ProductSubCategory> subCategoryOpt = subCategoryRepo.findById(item.getSubCategoryId())
                            .filter(s -> s.getActive() == null || s.getActive());
                    if (subCategoryOpt.isEmpty()) {
                        response.addFailure(i, "SubCategory not found with id: " + item.getSubCategoryId());
                        continue;
                    }
                    subCategory = subCategoryOpt.get();
                } else if (item.getSubCategoryName() != null && !item.getSubCategoryName().trim().isEmpty()) {
                    // Fallback to name lookup
                    Optional<ProductSubCategory> subCategoryOpt = subCategoryRepo.findBySubCategoryNameIgnoreCase(item.getSubCategoryName().trim())
                            .filter(s -> s.getActive() == null || s.getActive());
                    if (subCategoryOpt.isEmpty()) {
                        response.addFailure(i, "SubCategory not found with name: " + item.getSubCategoryName().trim());
                        continue;
                    }
                    subCategory = subCategoryOpt.get();
                }
                // Note: SubCategory is optional per business rules, but if provided, it must exist

                // ✅ VALIDATION: Uniqueness check (per subcategory if subcategory is provided)
                // Use efficient repository method instead of stream
                if (subCategory != null) {
                    if (repo.existsByMakeNameIgnoreCaseAndSubCategory_SubCategoryId(makeName, subCategory.getSubCategoryId())) {
                        response.addSkipped(i, "Make with name '" + makeName + "' already exists in this subcategory");
                        continue;
                    }
                } else {
                    // If no subcategory, check global uniqueness (fallback)
                    if (repo.findByMakeNameIgnoreCase(makeName).isPresent()) {
                        response.addSkipped(i, "Make with name '" + makeName + "' already exists");
                        continue;
                    }
                }

                // CREATE: Primary key is auto-generated
                MakeRequest createReq = new MakeRequest();
                createReq.setUserId(userId);
                createReq.setUsername(username);
                createReq.setProjectType(projectType);

                ProductMake make = new ProductMake();
                make.setMakeName(makeName);
                // Only set subCategory if provided (optional field)
                if (subCategory != null) {
                    make.setSubCategory(subCategory);
                }

                createReq.setMake(make);
                ProductMake created = create(headers, createReq, true); // skip per-item notifications; summary at end
                // Convert entity to DTO to include all optional fields in JSON response
                MakeDto result = MakeMapper.toDto(created);
                response.addSuccess(i, result);
                log.debug("✅ Created make name={}", makeName);

            } catch (Exception e) {
                log.error("❌ Bulk make failed at index {}: {}", i, e.getMessage());
                response.addFailure(i, e.getMessage());
            }
        }

        // Notify user: Email, SMS, WhatsApp, InApp - single-line summary
        try {
            String bearer = headers.getFirst("Authorization");
            if (bearer != null && !bearer.isBlank() && !bearer.startsWith("Bearer ")) {
                bearer = "Bearer " + bearer;
            }
            if (bearer != null && !bearer.isBlank()) {
                Map<String, Object> placeholders = new LinkedHashMap<>();
                placeholders.put("entityType", "Make");
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
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to send bulk upload notification: {}", e.getMessage());
        }

        log.info("📦 Bulk make upload: {}/{} success",
                response.getSuccessCount(), response.getTotalCount());
        return response;
    }

    // ============================================================
    // 🔒 TOKEN VALIDATION
    // ============================================================
    private void validateAuthorization(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new SecurityException("❌ Missing Authorization header");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new SecurityException("❌ Invalid Authorization header format");
        }
    }

    // ============================================================
    // 🔔 NOTIFICATION HELPERS
    // ============================================================
    private void sendMultiChannelNotification(String bearer,
                                              Long uid,
                                              String username,
                                              Map<String, Object> placeholders,
                                              String projectType,
                                              String templateCode,
                                              String message) {
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

            log.info("📤 Notifications sent (INAPP, EMAIL, SMS) for {} user={}", templateCode, username);
        } catch (Exception e) {
            log.error("⚠️ Failed to send notifications for {}: {}", templateCode, e.getMessage());
        }
    }

    private void notifyAdmins(String bearer,
                              String projectType,
                              Map<String, Object> placeholders,
                              String templateCode,
                              String actorUsername) {
        try {
            List<Map<String, Object>> admins = adminClient.getAdminsByProjectType(projectType);
            if (admins == null || admins.isEmpty()) {
                log.info("⚠️ No admins found for projectType={}", projectType);
                return;
            }

            for (Map<String, Object> admin : admins) {
                Long adminId = admin.get("userId") != null ? Long.valueOf(admin.get("userId").toString()) : 0L;
                String adminUsername = (String) admin.get("username");
                String email = (String) admin.get("email");
                String mobile = (String) admin.get("mobile");

                placeholders.put("triggeredBy", actorUsername);
                placeholders.put("recipientRole", "Admin");

                safeNotificationHelper.safeNotifyAsync(
                        bearer, adminId, adminUsername, email, mobile,
                        "EMAIL", templateCode + "_EMAIL", placeholders, projectType);

                safeNotificationHelper.safeNotifyAsync(
                        bearer, adminId, adminUsername, null, mobile,
                        "INAPP", templateCode, placeholders, projectType);
            }

            log.info("📢 Notified {} admin(s) for {}", admins.size(), templateCode);

        } catch (Exception e) {
            log.error("⚠️ Failed to notify admins for {}: {}", templateCode, e.getMessage());
        }
    }

    private void notifyLinkedUsers(String bearer,
                                   Long subCategoryId,
                                   Map<String, Object> placeholders,
                                   String templateCode,
                                   String actorUsername,
                                   String projectType) {
        try {
            List<Map<String, Object>> users = assetUserLinkClient.getUsersBySubCategory(subCategoryId);
            if (users == null || users.isEmpty()) {
                log.info("⚠️ No linked users found for subCategoryId={}", subCategoryId);
                return;
            }

            for (Map<String, Object> user : users) {
                Long uid = user.get("userId") != null ? Long.valueOf(user.get("userId").toString()) : 0L;
                String username = (String) user.get("username");

                placeholders.put("triggeredBy", actorUsername);
                placeholders.put("recipientRole", "LinkedUser");

                safeNotificationHelper.safeNotifyAsync(
                        bearer, uid, username, null, null,
                        "INAPP", templateCode, placeholders, projectType);
            }

            log.info("📢 Notified {} linked users under subcategory {}", users.size(), subCategoryId);

        } catch (Exception e) {
            log.error("⚠️ Failed to notify linked users for {}: {}", templateCode, e.getMessage());
        }
    }

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for a make (accessible to all authenticated users)
     */
    @Transactional
    public MakeDto updateFavourite(HttpHeaders headers, Long id, Boolean isFavourite) {
        validateAuthorization(headers);
        String bearer = headers.getFirst("Authorization");
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return repo.findById(id).map(existing -> {
            existing.setIsFavourite(isFavourite != null ? isFavourite : false);
            existing.setUpdatedBy(username);
            ProductMake saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("makeId", saved.getMakeId());
            placeholders.put("makeName", saved.getMakeName());
            placeholders.put("isFavourite", saved.getIsFavourite());
            placeholders.put("actor", username);
            placeholders.put("timestamp", new Date().toString());

            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType,
                    "MAKE_FAVOURITE_UPDATED", "Make favourite updated successfully");
            log.info("⭐ Make favourite updated: id={} isFavourite={} by={}", id, isFavourite, username);

            return MakeMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Make not found with id: " + id));
    }

    /**
     * Toggle most like status for a make (accessible to all authenticated users)
     */
    @Transactional
    public MakeDto updateMostLike(HttpHeaders headers, Long id, Boolean isMostLike) {
        validateAuthorization(headers);
        String bearer = headers.getFirst("Authorization");
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return repo.findById(id).map(existing -> {
            existing.setIsMostLike(isMostLike != null ? isMostLike : false);
            existing.setUpdatedBy(username);
            ProductMake saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("makeId", saved.getMakeId());
            placeholders.put("makeName", saved.getMakeName());
            placeholders.put("isMostLike", saved.getIsMostLike());
            placeholders.put("actor", username);
            placeholders.put("timestamp", new Date().toString());

            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType,
                    "MAKE_MOST_LIKE_UPDATED", "Make most like updated successfully");
            log.info("⭐ Make most like updated: id={} isMostLike={} by={}", id, isMostLike, username);

            return MakeMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Make not found with id: " + id));
    }

    /**
     * Update sequence order for a make (admin only)
     */
    @Transactional
    public MakeDto updateSequenceOrder(HttpHeaders headers, Long id, Integer sequenceOrder) {
        // Check if user is admin
        if (!com.example.asset.util.JwtUtil.isAdmin()) {
            throw new RuntimeException("Access denied: Only admins can update sequence order");
        }

        validateAuthorization(headers);
        String bearer = headers.getFirst("Authorization");
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return repo.findById(id).map(existing -> {
            existing.setSequenceOrder(sequenceOrder);
            existing.setUpdatedBy(username);
            ProductMake saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("makeId", saved.getMakeId());
            placeholders.put("makeName", saved.getMakeName());
            placeholders.put("sequenceOrder", saved.getSequenceOrder() != null ? saved.getSequenceOrder() : 0);
            placeholders.put("actor", username);
            placeholders.put("timestamp", new Date().toString());

            sendMultiChannelNotification(bearer, userId, username, placeholders, projectType,
                    "MAKE_SEQUENCE_UPDATED", "Make sequence order updated successfully");
            log.info("📊 Make sequence order updated: id={} sequenceOrder={} by={}", id, sequenceOrder, username);

            return MakeMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Make not found with id: " + id));
    }
}



