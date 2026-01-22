
package com.example.asset.service;

import com.example.asset.dto.BulkOutletRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.OutletDto;
import com.example.asset.dto.OutletRequest;
import com.example.asset.entity.PurchaseOutlet;
import com.example.asset.entity.VendorMaster;
import com.example.asset.mapper.OutletMapper;
import com.example.asset.repository.PurchaseOutletRepository;
import com.example.asset.repository.VendorRepository;
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
 * ✅ OutletService
 * Handles CRUD for PurchaseOutlet with token validation via @RequestHeader.
 * Sends notifications via SafeNotificationHelper to creator and admins.
 */
@Service
public class OutletService {

    private static final Logger log = LoggerFactory.getLogger(OutletService.class);

    private final PurchaseOutletRepository repo;
    private final VendorRepository vendorRepo;
    private final SafeNotificationHelper safeNotificationHelper;
    private final AdminClient adminClient;

    public OutletService(PurchaseOutletRepository repo,
                         VendorRepository vendorRepo,
                         SafeNotificationHelper safeNotificationHelper,
                         AdminClient adminClient) {
        this.repo = repo;
        this.vendorRepo = vendorRepo;
        this.safeNotificationHelper = safeNotificationHelper;
        this.adminClient = adminClient;
    }

    // ============================================================
    // 🟢 CREATE OUTLET
    // ============================================================
    @Transactional
    public PurchaseOutlet create(HttpHeaders headers, OutletRequest request) {
        validateAuthorization(headers);

        if (request == null || request.getOutlet() == null)
            throw new IllegalArgumentException("Request or outlet cannot be null");

        PurchaseOutlet outlet = request.getOutlet();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        // ✅ Validate outlet name
        String outletName = outlet.getOutletName() != null ? outlet.getOutletName().trim() : null;
        if (!StringUtils.hasText(outletName))
            throw new RuntimeException("Outlet name cannot be blank");
        
        outlet.setOutletName(outletName);

        // ✅ Uniqueness check (case-insensitive to prevent duplicates like "Store A" and "store a")
        if (repo.existsByOutletNameIgnoreCase(outletName))
            throw new RuntimeException("❌ Outlet with name '" + outletName + "' already exists");

        // ✅ Validate and set vendor if provided
        if (outlet.getVendor() != null && outlet.getVendor().getVendorId() != null) {
            VendorMaster vendor = vendorRepo.findById(outlet.getVendor().getVendorId())
                    .filter(v -> v.getActive() == null || v.getActive())
                    .orElse(null);
            if (vendor == null) {
                throw new RuntimeException("❌ Vendor not found with id: " + outlet.getVendor().getVendorId());
            }
            outlet.setVendor(vendor);
        }

        outlet.setCreatedBy(username);
        outlet.setUpdatedBy(username);
        PurchaseOutlet saved = repo.save(outlet);

        Map<String, Object> placeholders = new LinkedHashMap<>();
        placeholders.put("outletId", saved.getOutletId());
        placeholders.put("outletName", saved.getOutletName());
        placeholders.put("createdBy", username);
        placeholders.put("username", username);
        placeholders.put("timestamp", new Date().toString());

        sendNotifications(bearer, userId, username, placeholders, projectType,
                "OUTLET_CREATED", "Outlet created successfully");

        notifyAdmins(bearer, projectType, placeholders, "OUTLET_CREATED_ADMIN", username);

        log.info("✅ Outlet created successfully: id={} name={} by={}",
                saved.getOutletId(), saved.getOutletName(), username);

        return saved;
    }

    // ============================================================
    // ✏️ UPDATE OUTLET
    // ============================================================
    @Transactional
    public PurchaseOutlet update(HttpHeaders headers, Long id, OutletRequest request) {
        validateAuthorization(headers);

        if (request == null || request.getOutlet() == null)
            throw new IllegalArgumentException("Request or outlet cannot be null");

        PurchaseOutlet patch = request.getOutlet();
        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        return repo.findById(id).map(existing -> {
            String newName = patch.getOutletName() != null ? patch.getOutletName().trim() : null;

            if (!StringUtils.hasText(newName))
                throw new RuntimeException("Outlet name cannot be blank");

            // ✅ Prevent duplicate name (case-insensitive, efficient repository check)
            if (!existing.getOutletName().equalsIgnoreCase(newName)
                    && repo.existsByOutletNameIgnoreCase(newName)) {
                throw new RuntimeException("❌ Outlet with name '" + newName + "' already exists");
            }

            existing.setOutletName(newName);
            existing.setOutletAddress(patch.getOutletAddress());
            existing.setContactInfo(patch.getContactInfo());
            
            // ✅ Validate and update vendor if provided
            if (patch.getVendor() != null) {
                if (patch.getVendor().getVendorId() != null) {
                    VendorMaster vendor = vendorRepo.findById(patch.getVendor().getVendorId())
                            .filter(v -> v.getActive() == null || v.getActive())
                            .orElse(null);
                    if (vendor == null) {
                        throw new RuntimeException("❌ Vendor not found with id: " + patch.getVendor().getVendorId());
                    }
                    existing.setVendor(vendor);
                } else {
                    // If vendor_id is null, remove vendor relationship
                    existing.setVendor(null);
                }
            }
            // If patch.getVendor() is null, keep existing vendor (no change)

            existing.setUpdatedBy(username);

            PurchaseOutlet saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("outletId", saved.getOutletId());
            placeholders.put("oldName", existing.getOutletName());
            placeholders.put("newName", newName);
            placeholders.put("outletName", newName);
            placeholders.put("updatedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", new Date().toString());

            sendNotifications(bearer, userId, username, placeholders, projectType,
                    "OUTLET_UPDATED", "Outlet updated successfully");

            log.info("✏️ Outlet updated: id={} name={} by={}", id, newName, username);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Outlet not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE OUTLET
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, OutletRequest request) {
        validateAuthorization(headers);

        String username = request.getUsername();
        Long userId = request.getUserId();
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        repo.findById(id).ifPresent(o -> {
            o.setActive(false);
            o.setUpdatedBy(username);
            repo.save(o);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("outletId", o.getOutletId());
            placeholders.put("outletName", o.getOutletName());
            placeholders.put("deletedBy", username);
        placeholders.put("username", username);
            placeholders.put("timestamp", new Date().toString());

            sendNotifications(bearer, userId, username, placeholders, projectType,
                    "OUTLET_DELETED", "Outlet deleted successfully");

            log.info("🗑️ Outlet soft-deleted id={} by={}", id, username);
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<PurchaseOutlet> list() {
        return repo.findAll().stream()
                .filter(o -> o.getActive() == null || o.getActive())
                .toList();
    }

    public Optional<PurchaseOutlet> find(Long id) {
        return repo.findById(id)
                .filter(o -> o.getActive() == null || o.getActive());
    }

    // ============================================================
    // 📦 BULK UPLOAD OUTLETS (NEW - using BulkOutletRequest)
    // ============================================================
    @Transactional
    public BulkUploadResponse<OutletDto> bulkCreate(HttpHeaders headers, BulkOutletRequest bulkRequest) {
        BulkUploadResponse<OutletDto> response = new BulkUploadResponse<>();
        
        if (bulkRequest == null || bulkRequest.getOutlets() == null) {
            throw new IllegalArgumentException("Bulk request cannot be null");
        }

        List<BulkOutletRequest.SimpleOutletDto> items = bulkRequest.getOutlets();
        response.setTotalCount(items.size());

        String username = bulkRequest.getUsername();
        Long userId = bulkRequest.getUserId();
        String projectType = Optional.ofNullable(bulkRequest.getProjectType()).orElse("ASSET_SERVICE");
        String bearer = headers.getFirst("Authorization");

        for (int i = 0; i < items.size(); i++) {
            try {
                BulkOutletRequest.SimpleOutletDto item = items.get(i);

                // ✅ VALIDATION: Required field
                if (item.getOutletName() == null || item.getOutletName().trim().isEmpty()) {
                    response.addFailure(i, "Outlet name is required");
                    continue;
                }

                String outletName = item.getOutletName().trim();

                // ✅ VALIDATION: Name length (max 150 chars per entity constraint)
                if (outletName.length() > 150) {
                    response.addFailure(i, "Outlet name exceeds maximum length of 150 characters");
                    continue;
                }

                // ✅ VALIDATION: Name uniqueness (case-insensitive check to prevent duplicates)
                if (repo.existsByOutletNameIgnoreCase(outletName)) {
                    response.addFailure(i, "Outlet with name '" + outletName + "' already exists");
                    continue;
                }

                // ✅ VALIDATION: Address length (max 255 chars per entity constraint)
                String outletAddress = null;
                if (item.getOutletAddress() != null && !item.getOutletAddress().trim().isEmpty()) {
                    outletAddress = item.getOutletAddress().trim();
                    if (outletAddress.length() > 255) {
                        response.addFailure(i, "Outlet address exceeds maximum length of 255 characters");
                        continue;
                    }
                }

                // ✅ VALIDATION: Contact info length (max 100 chars per entity constraint)
                String contactInfo = null;
                if (item.getContactInfo() != null && !item.getContactInfo().trim().isEmpty()) {
                    contactInfo = item.getContactInfo().trim();
                    if (contactInfo.length() > 100) {
                        response.addFailure(i, "Contact info exceeds maximum length of 100 characters");
                        continue;
                    }
                }

                // ✅ VALIDATION: Foreign key - Vendor must exist if provided
                VendorMaster vendor = null;
                if (item.getVendorId() != null) {
                    vendor = vendorRepo.findById(item.getVendorId())
                            .filter(v -> v.getActive() == null || v.getActive())
                            .orElse(null);
                    if (vendor == null) {
                        response.addFailure(i, "Vendor not found with id: " + item.getVendorId());
                        continue;
                    }
                }
                // Note: Vendor is optional per business rules, but if provided, it must exist

                // CREATE: Primary key is auto-generated
                // Directly save entity without calling create() to avoid per-item notifications
                PurchaseOutlet outlet = new PurchaseOutlet();
                outlet.setOutletName(outletName);
                outlet.setCreatedBy(username);
                outlet.setUpdatedBy(username);
                
                // Only set optional fields if provided
                if (outletAddress != null) {
                    outlet.setOutletAddress(outletAddress);
                }
                if (contactInfo != null) {
                    outlet.setContactInfo(contactInfo);
                }
                if (vendor != null) {
                    outlet.setVendor(vendor);
                }

                PurchaseOutlet created = repo.save(outlet);
                // Convert entity to DTO to include all optional fields in JSON response
                OutletDto result = OutletMapper.toDto(created);
                response.addSuccess(i, result);
                log.debug("✅ Created outlet name={}", outletName);

            } catch (Exception e) {
                log.error("❌ Bulk outlet failed at index {}: {}", i, e.getMessage());
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
                placeholders.put("timestamp", new Date().toString());

                sendNotifications(bearer, userId, username, placeholders, projectType,
                        "OUTLET_BULK_UPLOAD", "Bulk outlet upload completed");
                
                notifyAdmins(bearer, projectType, placeholders, "OUTLET_BULK_UPLOAD_ADMIN", username);
            } catch (Exception e) {
                log.warn("⚠️ Failed to send bulk upload notification: {}", e.getMessage());
            }
        }

        log.info("📦 Bulk outlet upload: {}/{} success",
                response.getSuccessCount(), response.getTotalCount());
        return response;
    }

    // ============================================================
    // 🔐 TOKEN VALIDATION
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
    private void sendNotifications(String bearer,
                                   Long uid,
                                   String username,
                                   Map<String, Object> placeholders,
                                   String projectType,
                                   String templateCode,
                                   String message) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, null, null,
                    "INAPP", templateCode, placeholders, projectType);

            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, username + "@example.com", null,
                    "EMAIL", templateCode + "_EMAIL", placeholders, projectType);

            String mockMobile = "99999999" + (uid != null ? String.valueOf(uid % 100) : "00");
            safeNotificationHelper.safeNotifyAsync(
                    bearer, uid, username, null, mockMobile,
                    "SMS", templateCode + "_SMS", placeholders, projectType);

            log.info("📤 Notifications sent (INAPP, EMAIL, SMS) for {} → {}", templateCode, username);
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

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for an outlet (accessible to all authenticated users)
     */
    @Transactional
    public OutletDto updateFavourite(HttpHeaders headers, Long id, Boolean isFavourite) {
        validateAuthorization(headers);
        String bearer = headers.getFirst("Authorization");
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return repo.findById(id).map(existing -> {
            existing.setIsFavourite(isFavourite != null ? isFavourite : false);
            existing.setUpdatedBy(username);
            PurchaseOutlet saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("outletId", saved.getOutletId());
            placeholders.put("outletName", saved.getOutletName());
            placeholders.put("isFavourite", saved.getIsFavourite());
            placeholders.put("actor", username);
            placeholders.put("timestamp", new Date().toString());

            sendNotifications(bearer, userId, username, placeholders, projectType,
                    "OUTLET_FAVOURITE_UPDATED", "Outlet favourite updated successfully");
            log.info("⭐ Outlet favourite updated: id={} isFavourite={} by={}", id, isFavourite, username);

            return OutletMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Outlet not found with id: " + id));
    }

    /**
     * Toggle most like status for an outlet (accessible to all authenticated users)
     */
    @Transactional
    public OutletDto updateMostLike(HttpHeaders headers, Long id, Boolean isMostLike) {
        validateAuthorization(headers);
        String bearer = headers.getFirst("Authorization");
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        Long userId = Long.parseLong(com.example.asset.util.JwtUtil.getUserIdOrThrow());
        String projectType = "ASSET_SERVICE";

        return repo.findById(id).map(existing -> {
            existing.setIsMostLike(isMostLike != null ? isMostLike : false);
            existing.setUpdatedBy(username);
            PurchaseOutlet saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("outletId", saved.getOutletId());
            placeholders.put("outletName", saved.getOutletName());
            placeholders.put("isMostLike", saved.getIsMostLike());
            placeholders.put("actor", username);
            placeholders.put("timestamp", new Date().toString());

            sendNotifications(bearer, userId, username, placeholders, projectType,
                    "OUTLET_MOST_LIKE_UPDATED", "Outlet most like updated successfully");
            log.info("⭐ Outlet most like updated: id={} isMostLike={} by={}", id, isMostLike, username);

            return OutletMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Outlet not found with id: " + id));
    }

    /**
     * Update sequence order for an outlet (admin only)
     */
    @Transactional
    public OutletDto updateSequenceOrder(HttpHeaders headers, Long id, Integer sequenceOrder) {
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
            PurchaseOutlet saved = repo.save(existing);

            Map<String, Object> placeholders = new LinkedHashMap<>();
            placeholders.put("outletId", saved.getOutletId());
            placeholders.put("outletName", saved.getOutletName());
            placeholders.put("sequenceOrder", saved.getSequenceOrder() != null ? saved.getSequenceOrder() : 0);
            placeholders.put("actor", username);
            placeholders.put("timestamp", new Date().toString());

            sendNotifications(bearer, userId, username, placeholders, projectType,
                    "OUTLET_SEQUENCE_UPDATED", "Outlet sequence order updated successfully");
            log.info("📊 Outlet sequence order updated: id={} sequenceOrder={} by={}", id, sequenceOrder, username);

            return OutletMapper.toDto(saved);
        }).orElseThrow(() -> new IllegalArgumentException("Outlet not found with id: " + id));
    }
}


