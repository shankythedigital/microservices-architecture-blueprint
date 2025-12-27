package com.example.asset.service;

import com.example.asset.dto.BulkVendorRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.VendorDto;
import com.example.asset.dto.VendorRequest;
import com.example.asset.entity.VendorMaster;
import com.example.asset.mapper.VendorMapper;
import com.example.asset.repository.VendorRepository;
import com.example.common.service.SafeNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * ✅ VendorService
 * Handles vendor CRUD operations with token-secured header authentication
 * and SafeNotificationHelper for reliable async notifications.
 */
@Service
public class VendorService {

    private static final Logger log = LoggerFactory.getLogger(VendorService.class);

    private final VendorRepository repo;
    private final SafeNotificationHelper safeNotificationHelper;

    public VendorService(VendorRepository repo,
                         SafeNotificationHelper safeNotificationHelper) {
        this.repo = repo;
        this.safeNotificationHelper = safeNotificationHelper;
    }

    // ============================================================
    // 🟢 CREATE VENDOR
    // ============================================================
    @Transactional
    public VendorMaster create(HttpHeaders headers, VendorRequest request) {
        validateRequest(headers, request);

        VendorMaster vendor = request.getVendor();
        String username = request.getUsername();
        String bearer = extractBearer(headers);
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        // ✅ Name validation and trimming
        String vendorName = vendor.getVendorName() != null ? vendor.getVendorName().trim() : null;
        if (!StringUtils.hasText(vendorName)) {
            throw new RuntimeException("Vendor name cannot be blank");
        }
        
        vendor.setVendorName(vendorName);

        // ✅ Uniqueness check (case-insensitive - already correct)
        if (repo.existsByVendorNameIgnoreCase(vendorName)) {
            throw new RuntimeException("❌ Vendor with name '" + vendorName + "' already exists");
        }

        vendor.setCreatedBy(username);
        vendor.setUpdatedBy(username);
        VendorMaster saved = repo.save(vendor);

        // 📩 Notification placeholders
        Map<String, Object> placeholders = Map.of(
                "vendorId", saved.getVendorId(),
                "vendorName", saved.getVendorName(),
                "createdBy", username,
                "timestamp", new Date().toString(),
                "username", username
        );

        sendNotification(bearer, request.getUserId(), username, placeholders,
                "VENDOR_CREATED_INAPP", projectType);

        log.info("✅ Vendor created successfully: {}", saved.getVendorName());
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE VENDOR
    // ============================================================
    @Transactional
    public VendorMaster update(HttpHeaders headers, Long id, VendorRequest request) {
        validateRequest(headers, request);

        String username = request.getUsername();
        String bearer = extractBearer(headers);
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        return repo.findById(id).map(existing -> {
            String newName = request.getVendor().getVendorName();

            if (!StringUtils.hasText(newName)) {
                throw new RuntimeException("Vendor name cannot be blank");
            }

            if (!existing.getVendorName().equalsIgnoreCase(newName)
                    && repo.existsByVendorNameIgnoreCase(newName)) {
                throw new RuntimeException("❌ Vendor with name '" + newName + "' already exists");
            }

            String oldName = existing.getVendorName();
            existing.setVendorName(newName);
            existing.setContactPerson(request.getVendor().getContactPerson());
            existing.setEmail(request.getVendor().getEmail());
            existing.setMobile(request.getVendor().getMobile());
            existing.setAddress(request.getVendor().getAddress());
            existing.setUpdatedBy(username);

            VendorMaster saved = repo.save(existing);

            Map<String, Object> placeholders = Map.of(
                    "vendorId", saved.getVendorId(),
                    "oldName", oldName,
                    "newName", newName,
                    "vendorName", newName,
                    "updatedBy", username,
                    "timestamp", new Date().toString(),
                "username", username
            );

            sendNotification(bearer, request.getUserId(), username, placeholders,
                    "VENDOR_UPDATED_INAPP", projectType);

            log.info("✏️ Vendor updated successfully: id={} name={}", id, newName);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE VENDOR
    // ============================================================
    @Transactional
    public void softDelete(HttpHeaders headers, Long id, VendorRequest request) {
        validateRequest(headers, request);

        String username = request.getUsername();
        String bearer = extractBearer(headers);
        String projectType = Optional.ofNullable(request.getProjectType()).orElse("ASSET_SERVICE");

        repo.findById(id).ifPresent(vendor -> {
            vendor.setActive(false);
            vendor.setUpdatedBy(username);
            repo.save(vendor);

            Map<String, Object> placeholders = Map.of(
                    "vendorId", vendor.getVendorId(),
                    "vendorName", vendor.getVendorName(),
                    "deletedBy", username,
                    "timestamp", new Date().toString(),
                "username", username
            );

            sendNotification(bearer, request.getUserId(), username, placeholders,
                    "VENDOR_DELETED_INAPP", projectType);

            log.info("🗑️ Vendor deleted (soft): {}", vendor.getVendorName());
        });
    }

    // ============================================================
    // 📋 LIST / FIND
    // ============================================================
    public List<VendorMaster> list() {
        return repo.findAll().stream()
                .filter(v -> v.getActive() == null || v.getActive())
                .toList();
    }

    public Optional<VendorMaster> find(Long id) {
        return repo.findById(id)
                .filter(v -> v.getActive() == null || v.getActive());
    }

    // ============================================================
    // 📦 BULK UPLOAD VENDORS (NEW - using BulkVendorRequest)
    // ============================================================
    @Transactional
    public BulkUploadResponse<VendorDto> bulkCreate(HttpHeaders headers, BulkVendorRequest bulkRequest) {
        BulkUploadResponse<VendorDto> response = new BulkUploadResponse<>();
        
        if (bulkRequest == null || bulkRequest.getVendors() == null) {
            throw new IllegalArgumentException("Bulk request cannot be null");
        }

        List<BulkVendorRequest.SimpleVendorDto> items = bulkRequest.getVendors();
        response.setTotalCount(items.size());

        String username = bulkRequest.getUsername();
        Long userId = bulkRequest.getUserId();
        String projectType = Optional.ofNullable(bulkRequest.getProjectType()).orElse("ASSET_SERVICE");

        for (int i = 0; i < items.size(); i++) {
            try {
                BulkVendorRequest.SimpleVendorDto item = items.get(i);

                // ✅ VALIDATION: Required field
                if (item.getVendorName() == null || item.getVendorName().trim().isEmpty()) {
                    response.addFailure(i, "Vendor name is required");
                    continue;
                }

                String vendorName = item.getVendorName().trim();

                // ✅ VALIDATION: Name length (max 150 chars per entity constraint)
                if (vendorName.length() > 150) {
                    response.addFailure(i, "Vendor name exceeds maximum length of 150 characters");
                    continue;
                }

                // ✅ VALIDATION: Name uniqueness (checked in create method, but pre-check for better error message)
                if (repo.existsByVendorNameIgnoreCase(vendorName)) {
                    response.addFailure(i, "Vendor with name '" + vendorName + "' already exists");
                    continue;
                }

                // ✅ VALIDATION: Contact person length (max 150 chars per entity constraint)
                String contactPerson = null;
                if (item.getContactPerson() != null && !item.getContactPerson().trim().isEmpty()) {
                    contactPerson = item.getContactPerson().trim();
                    if (contactPerson.length() > 150) {
                        response.addFailure(i, "Contact person name exceeds maximum length of 150 characters");
                        continue;
                    }
                }

                // ✅ VALIDATION: Email format and length (max 100 chars per entity constraint)
                String email = null;
                if (item.getEmail() != null && !item.getEmail().trim().isEmpty()) {
                    email = item.getEmail().trim();
                    if (email.length() > 100) {
                        response.addFailure(i, "Email exceeds maximum length of 100 characters");
                        continue;
                    }
                    // Basic email format validation
                    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                        response.addFailure(i, "Invalid email format: " + email);
                        continue;
                    }
                }

                // ✅ VALIDATION: Mobile length and format (max 15 chars per entity constraint)
                String mobile = null;
                if (item.getMobile() != null && !item.getMobile().trim().isEmpty()) {
                    mobile = item.getMobile().trim();
                    if (mobile.length() > 15) {
                        response.addFailure(i, "Mobile number exceeds maximum length of 15 characters");
                        continue;
                    }
                    // Basic mobile format validation (digits only, optionally with +, -, spaces)
                    if (!mobile.matches("^[+]?[0-9\\s\\-()]{7,15}$")) {
                        response.addFailure(i, "Invalid mobile number format: " + mobile);
                        continue;
                    }
                }

                // ✅ VALIDATION: Address length (max 255 chars per entity constraint)
                String address = null;
                if (item.getAddress() != null && !item.getAddress().trim().isEmpty()) {
                    address = item.getAddress().trim();
                    if (address.length() > 255) {
                        response.addFailure(i, "Address exceeds maximum length of 255 characters");
                        continue;
                    }
                }

                // CREATE: Primary key is auto-generated
                VendorRequest createReq = new VendorRequest();
                createReq.setUserId(userId);
                createReq.setUsername(username);
                createReq.setProjectType(projectType);

                VendorMaster vendor = new VendorMaster();
                vendor.setVendorName(vendorName);
                // Only set optional fields if provided
                if (contactPerson != null) {
                    vendor.setContactPerson(contactPerson);
                }
                if (email != null) {
                    vendor.setEmail(email);
                }
                if (mobile != null) {
                    vendor.setMobile(mobile);
                }
                if (address != null) {
                    vendor.setAddress(address);
                }

                createReq.setVendor(vendor);
                VendorMaster created = create(headers, createReq);
                // Convert entity to DTO to include all optional fields in JSON response
                VendorDto result = VendorMapper.toDto(created);
                response.addSuccess(i, result);
                log.debug("✅ Created vendor name={}", vendorName);

            } catch (Exception e) {
                log.error("❌ Bulk vendor failed at index {}: {}", i, e.getMessage());
                response.addFailure(i, e.getMessage());
            }
        }

        log.info("📦 Bulk vendor upload: {}/{} success",
                response.getSuccessCount(), response.getTotalCount());
        return response;
    }

    // ============================================================
    // 🧩 PRIVATE HELPERS
    // ============================================================
    private void validateRequest(HttpHeaders headers, VendorRequest request) {
        if (headers == null || headers.getFirst("Authorization") == null) {
            throw new RuntimeException("❌ Missing Authorization header");
        }
        // if (request == null || request.getVendor() == null) {
        //     throw new RuntimeException("❌ Invalid request or missing vendor data");
        // }
    }

    private String extractBearer(HttpHeaders headers) {
        String token = headers.getFirst("Authorization");
        if (token == null || token.isBlank()) {
            throw new RuntimeException("❌ Missing or invalid Authorization header");
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    private void sendNotification(String bearer, Long userId, String username,
                                  Map<String, Object> placeholders, String templateCode, String projectType) {
        try {
            safeNotificationHelper.safeNotifyAsync(
                    bearer,
                    userId,
                    username,
                    username + "@example.com",
                    "9999999999",
                    "INAPP",
                    templateCode,
                    placeholders,
                    projectType
            );
            log.info("📩 Notification [{}] sent for user={}", templateCode, username);
        } catch (Exception e) {
            log.error("⚠️ Failed to send {} notification: {}", templateCode, e.getMessage());
        }
    }
}

