package com.example.asset.service;

import com.example.asset.entity.AssetUserLink;
import com.example.asset.entity.AssetMaster;
import com.example.asset.entity.AssetComponent;
import com.example.asset.repository.*;
import com.example.asset.util.ComplianceValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ UserAssetLinkAgentService
 * Comprehensive agent for managing user-asset links.
 * Handles linking, delinking, validation, and edge cases.
 */
@Service
public class UserAssetLinkAgentService {

    private static final Logger log = LoggerFactory.getLogger(UserAssetLinkAgentService.class);

    private final AssetUserLinkRepository linkRepo;
    private final AssetMasterRepository assetRepo;
    private final AssetComponentRepository componentRepo;
    private final ComplianceValidationHelper complianceHelper;

    public UserAssetLinkAgentService(
            AssetUserLinkRepository linkRepo,
            AssetMasterRepository assetRepo,
            AssetComponentRepository componentRepo,
            ComplianceValidationHelper complianceHelper) {
        this.linkRepo = linkRepo;
        this.assetRepo = assetRepo;
        this.componentRepo = componentRepo;
        this.complianceHelper = complianceHelper;
    }

    // ============================================================
    // 🔗 LINK ASSET TO USER
    // ============================================================
    @Transactional
    public AssetUserLink linkAssetToUser(Long assetId, Long userId, String username, 
                                        String email, String mobile, String createdBy) {
        // Edge case: Null asset ID
        if (assetId == null) {
            throw new IllegalArgumentException("❌ Asset ID cannot be null");
        }

        // Edge case: Null user ID
        if (userId == null) {
            throw new IllegalArgumentException("❌ User ID cannot be null");
        }

        // Edge case: Null username
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Username cannot be null or empty");
        }

        // Edge case: Asset not found
        AssetMaster asset = assetRepo.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Asset not found: " + assetId));

        // Edge case: Asset already linked to this user (active link)
        Optional<AssetUserLink> existingLink = linkRepo.findByAssetIdAndUserIdAndActiveTrue(assetId, userId);
        if (existingLink.isPresent()) {
            AssetUserLink link = existingLink.get();
            log.warn("⚠️ Asset {} already linked to user {} (Link ID: {})", assetId, userId, link.getLinkId());
            return link; // Return existing link instead of throwing
        }

        // Edge case: Asset already linked to different user
        if (linkRepo.existsByAssetIdAndActiveTrue(assetId)) {
            List<AssetUserLink> activeLinks = linkRepo.findByAssetIdAndActiveTrue(assetId);
            if (!activeLinks.isEmpty()) {
                AssetUserLink existing = activeLinks.get(0);
                log.warn("⚠️ Asset {} already linked to user {} (Link ID: {})", 
                        assetId, existing.getUserId(), existing.getLinkId());
                // Option 1: Throw exception
                throw new IllegalStateException("❌ Asset " + assetId + 
                        " is already linked to user " + existing.getUserId());
                // Option 2: Auto-delink previous and create new (uncomment if needed)
                // delinkAssetFromUser(assetId, existing.getUserId(), createdBy);
            }
        }

        // Compliance check before linking
        try {
            complianceHelper.validateBeforeUpdate("ASSET", assetId, asset);
        } catch (Exception e) {
            log.warn("⚠️ Compliance check failed for asset {}: {}", assetId, e.getMessage());
            // Continue with linking but log the warning
        }

        // Create new link
        AssetUserLink link = new AssetUserLink();
        link.setAssetId(assetId);
        link.setUserId(userId);
        link.setUsername(username.trim());
        link.setEmail(email != null ? email.trim() : null);
        link.setMobile(mobile != null ? mobile.trim() : null);
        link.setAssignedDate(LocalDateTime.now());
        link.setCreatedBy(createdBy);
        link.setUpdatedBy(createdBy);
        link.setActive(true);

        AssetUserLink saved = linkRepo.save(link);
        log.info("✅ Asset {} linked to user {} (Link ID: {})", assetId, userId, saved.getLinkId());
        return saved;
    }

    // ============================================================
    // 🔗 LINK COMPONENT TO USER
    // ============================================================
    @Transactional
    public AssetUserLink linkComponentToUser(Long componentId, Long userId, String username,
                                             String email, String mobile, String createdBy) {
        // Edge case: Null component ID
        if (componentId == null) {
            throw new IllegalArgumentException("❌ Component ID cannot be null");
        }

        // Edge case: Null user ID
        if (userId == null) {
            throw new IllegalArgumentException("❌ User ID cannot be null");
        }

        // Edge case: Null username
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Username cannot be null or empty");
        }

        // Edge case: Component not found
        if (!componentRepo.existsById(componentId)) {
            throw new IllegalArgumentException("❌ Component not found: " + componentId);
        }

        // Edge case: Component already linked to this user
        Optional<AssetUserLink> existingLink = linkRepo.findByComponentIdAndUserIdAndActiveTrue(componentId, userId);
        if (existingLink.isPresent()) {
            AssetUserLink link = existingLink.get();
            log.warn("⚠️ Component {} already linked to user {} (Link ID: {})", componentId, userId, link.getLinkId());
            return link;
        }

        // Edge case: Component already linked to different user
        if (linkRepo.existsByComponentIdAndActiveTrue(componentId)) {
            List<AssetUserLink> activeLinks = linkRepo.findByComponentIdAndActiveTrue(componentId);
            if (!activeLinks.isEmpty()) {
                AssetUserLink existing = activeLinks.get(0);
                throw new IllegalStateException("❌ Component " + componentId + 
                        " is already linked to user " + existing.getUserId());
            }
        }

        // Create new link
        AssetUserLink link = new AssetUserLink();
        link.setComponentId(componentId);
        link.setUserId(userId);
        link.setUsername(username.trim());
        link.setEmail(email != null ? email.trim() : null);
        link.setMobile(mobile != null ? mobile.trim() : null);
        link.setAssignedDate(LocalDateTime.now());
        link.setCreatedBy(createdBy);
        link.setUpdatedBy(createdBy);
        link.setActive(true);

        AssetUserLink saved = linkRepo.save(link);
        log.info("✅ Component {} linked to user {} (Link ID: {})", componentId, userId, saved.getLinkId());
        return saved;
    }

    // ============================================================
    // 🔓 DELINK ASSET FROM USER
    // ============================================================
    @Transactional
    public void delinkAssetFromUser(Long assetId, Long userId, String updatedBy) {
        // Edge case: Null IDs
        if (assetId == null || userId == null) {
            throw new IllegalArgumentException("❌ Asset ID and User ID cannot be null");
        }

        // Edge case: Link not found
        AssetUserLink link = linkRepo.findByAssetIdAndUserIdAndActiveTrue(assetId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "❌ Active link not found for asset " + assetId + " and user " + userId));

        // Soft delete
        link.setActive(false);
        link.setUnassignedDate(LocalDateTime.now());
        link.setUpdatedBy(updatedBy);
        linkRepo.save(link);

        log.info("✅ Asset {} delinked from user {} (Link ID: {})", assetId, userId, link.getLinkId());
    }

    // ============================================================
    // 🔓 DELINK COMPONENT FROM USER
    // ============================================================
    @Transactional
    public void delinkComponentFromUser(Long componentId, Long userId, String updatedBy) {
        // Edge case: Null IDs
        if (componentId == null || userId == null) {
            throw new IllegalArgumentException("❌ Component ID and User ID cannot be null");
        }

        // Edge case: Link not found
        AssetUserLink link = linkRepo.findByComponentIdAndUserIdAndActiveTrue(componentId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "❌ Active link not found for component " + componentId + " and user " + userId));

        // Soft delete
        link.setActive(false);
        link.setUnassignedDate(LocalDateTime.now());
        link.setUpdatedBy(updatedBy);
        linkRepo.save(link);

        log.info("✅ Component {} delinked from user {} (Link ID: {})", componentId, userId, link.getLinkId());
    }

    // ============================================================
    // 📋 GET USER ASSETS
    // ============================================================
    public List<AssetMaster> getAssetsAssignedToUser(Long userId) {
        // Edge case: Null user ID
        if (userId == null) {
            throw new IllegalArgumentException("❌ User ID cannot be null");
        }

        List<AssetUserLink> links = linkRepo.findByUserIdAndActiveTrue(userId);
        List<Long> assetIds = links.stream()
                .filter(link -> link.getAssetId() != null)
                .map(AssetUserLink::getAssetId)
                .distinct()
                .collect(Collectors.toList());

        if (assetIds.isEmpty()) {
            return Collections.emptyList();
        }

        return assetRepo.findAllById(assetIds);
    }

    // ============================================================
    // 📋 GET USER COMPONENTS
    // ============================================================
    public List<AssetComponent> getComponentsAssignedToUser(Long userId) {
        // Edge case: Null user ID
        if (userId == null) {
            throw new IllegalArgumentException("❌ User ID cannot be null");
        }

        List<AssetUserLink> links = linkRepo.findByUserIdAndActiveTrue(userId);
        List<Long> componentIds = links.stream()
                .filter(link -> link.getComponentId() != null)
                .map(AssetUserLink::getComponentId)
                .distinct()
                .collect(Collectors.toList());

        if (componentIds.isEmpty()) {
            return Collections.emptyList();
        }

        return componentRepo.findAllById(componentIds);
    }

    // ============================================================
    // 📋 GET ASSET ASSIGNMENT HISTORY
    // ============================================================
    public List<AssetUserLink> getAssetAssignmentHistory(Long assetId) {
        // Edge case: Null asset ID
        if (assetId == null) {
            throw new IllegalArgumentException("❌ Asset ID cannot be null");
        }

        List<AssetUserLink> links = linkRepo.findByAssetId(assetId);
        return links != null ? links : Collections.emptyList();
    }

    // ============================================================
    // 📋 GET USER ASSIGNMENT HISTORY
    // ============================================================
    public List<AssetUserLink> getUserAssignmentHistory(Long userId) {
        // Edge case: Null user ID
        if (userId == null) {
            throw new IllegalArgumentException("❌ User ID cannot be null");
        }

        List<AssetUserLink> links = linkRepo.findByUserId(userId);
        return links != null ? links : Collections.emptyList();
    }

    // ============================================================
    // 📋 BULK OPERATIONS
    // ============================================================
    @Transactional
    public Map<String, Object> bulkLinkAssetsToUser(List<Long> assetIds, Long userId, 
                                                    String username, String createdBy) {
        Map<String, Object> result = new HashMap<>();
        List<AssetUserLink> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < assetIds.size(); i++) {
            Long assetId = assetIds.get(i);
            try {
                AssetUserLink link = linkAssetToUser(assetId, userId, username, null, null, createdBy);
                created.add(link);
            } catch (Exception e) {
                errors.add("Asset ID " + assetId + " (Index " + i + "): " + e.getMessage());
                log.warn("⚠️ Failed to link asset {} to user {}: {}", assetId, userId, e.getMessage());
            }
        }

        result.put("total", assetIds.size());
        result.put("linked", created.size());
        result.put("failed", errors.size());
        result.put("links", created);
        result.put("errors", errors);

        log.info("📦 Bulk asset linking: {}/{} successful", created.size(), assetIds.size());
        return result;
    }

    // ============================================================
    // 📋 VALIDATION & CHECKS
    // ============================================================
    public boolean isAssetLinkedToUser(Long assetId, Long userId) {
        if (assetId == null || userId == null) {
            return false;
        }
        return linkRepo.existsByAssetIdAndUserIdAndActiveTrue(assetId, userId);
    }

    public boolean isComponentLinkedToUser(Long componentId, Long userId) {
        if (componentId == null || userId == null) {
            return false;
        }
        return linkRepo.existsByComponentIdAndUserIdAndActiveTrue(componentId, userId);
    }

    public Optional<AssetUserLink> getActiveLink(Long assetId, Long userId) {
        if (assetId == null || userId == null) {
            return Optional.empty();
        }
        return linkRepo.findByAssetIdAndUserIdAndActiveTrue(assetId, userId);
    }

    public Map<String, Object> getLinkStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long totalLinks = linkRepo.count();
        long activeLinks = linkRepo.findByActiveTrue().size();
        long inactiveLinks = totalLinks - activeLinks;

        stats.put("totalLinks", totalLinks);
        stats.put("activeLinks", activeLinks);
        stats.put("inactiveLinks", inactiveLinks);

        // Count by user
        Map<Long, Long> linksByUser = linkRepo.findByActiveTrue().stream()
                .collect(Collectors.groupingBy(AssetUserLink::getUserId, Collectors.counting()));
        stats.put("uniqueUsers", linksByUser.size());
        stats.put("linksByUser", linksByUser);

        return stats;
    }
}

