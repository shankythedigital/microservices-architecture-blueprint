package com.example.asset.controller;

import com.example.asset.dto.UserAssetLinkAgentRequest;
import com.example.asset.entity.AssetMaster;
import com.example.asset.entity.AssetComponent;
import com.example.asset.entity.AssetUserLink;
import com.example.asset.service.UserAssetLinkAgentService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ✅ UserAssetLinkAgentController
 * REST endpoints for user-asset linking operations.
 */
@RestController
@RequestMapping("/api/asset/v1/user-asset-links")
public class UserAssetLinkAgentController {

    private static final Logger log = LoggerFactory.getLogger(UserAssetLinkAgentController.class);
    private final UserAssetLinkAgentService linkService;

    public UserAssetLinkAgentController(UserAssetLinkAgentService linkService) {
        this.linkService = linkService;
    }

    // ============================================================
    // 🔗 LINK OPERATIONS
    // ============================================================
    @PostMapping("/link-asset")
    public ResponseEntity<ResponseWrapper<AssetUserLink>> linkAssetToUser(
            @RequestHeader HttpHeaders headers,
            @RequestBody UserAssetLinkAgentRequest request) {
        try {
            Long assetId = request.getAssetId();
            Long userId = request.getUserId();
            String username = request.getUsername();
            String email = request.getEmail();
            String mobile = request.getMobile();
            String createdBy = request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM";
            AssetUserLink link = linkService.linkAssetToUser(
                    assetId, userId, username, email, mobile, createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Asset linked to user successfully", link));
        } catch (Exception e) {
            log.error("❌ Failed to link asset: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @PostMapping("/link-component")
    public ResponseEntity<ResponseWrapper<AssetUserLink>> linkComponentToUser(
            @RequestHeader HttpHeaders headers,
            @RequestBody UserAssetLinkAgentRequest request) {
        try {
            Long componentId = request.getComponentId();
            Long userId = request.getUserId();
            String username = request.getUsername();
            String email = request.getEmail();
            String mobile = request.getMobile();
            String createdBy = request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM";
            AssetUserLink link = linkService.linkComponentToUser(
                    componentId, userId, username, email, mobile, createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Component linked to user successfully", link));
        } catch (Exception e) {
            log.error("❌ Failed to link component: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔓 DELINK OPERATIONS
    // ============================================================
    @PostMapping("/delink-asset")
    public ResponseEntity<ResponseWrapper<Void>> delinkAssetFromUser(
            @RequestHeader HttpHeaders headers,
            @RequestParam("assetId") Long assetId,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "updatedBy", defaultValue = "SYSTEM") String updatedBy) {
        try {
            linkService.delinkAssetFromUser(assetId, userId, updatedBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Asset delinked from user successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delink asset: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @PostMapping("/delink-component")
    public ResponseEntity<ResponseWrapper<Void>> delinkComponentFromUser(
            @RequestHeader HttpHeaders headers,
            @RequestParam("componentId") Long componentId,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "updatedBy", defaultValue = "SYSTEM") String updatedBy) {
        try {
            linkService.delinkComponentFromUser(componentId, userId, updatedBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Component delinked from user successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delink component: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 QUERY OPERATIONS
    // ============================================================
    @GetMapping("/user/{userId}/assets")
    public ResponseEntity<ResponseWrapper<List<AssetMaster>>> getAssetsAssignedToUser(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long userId) {
        try {
            List<AssetMaster> assets = linkService.getAssetsAssignedToUser(userId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Assets retrieved successfully", assets));
        } catch (Exception e) {
            log.error("❌ Failed to get user assets: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @GetMapping("/user/{userId}/components")
    public ResponseEntity<ResponseWrapper<List<AssetComponent>>> getComponentsAssignedToUser(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long userId) {
        try {
            List<AssetComponent> components = linkService.getComponentsAssignedToUser(userId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Components retrieved successfully", components));
        } catch (Exception e) {
            log.error("❌ Failed to get user components: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @GetMapping("/asset/{assetId}/history")
    public ResponseEntity<ResponseWrapper<List<AssetUserLink>>> getAssetAssignmentHistory(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long assetId) {
        try {
            List<AssetUserLink> history = linkService.getAssetAssignmentHistory(assetId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Assignment history retrieved", history));
        } catch (Exception e) {
            log.error("❌ Failed to get assignment history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<ResponseWrapper<List<AssetUserLink>>> getUserAssignmentHistory(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long userId) {
        try {
            List<AssetUserLink> history = linkService.getUserAssignmentHistory(userId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "User assignment history retrieved", history));
        } catch (Exception e) {
            log.error("❌ Failed to get user history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 VALIDATION & STATISTICS
    // ============================================================
    @GetMapping("/check/asset/{assetId}/user/{userId}")
    public ResponseEntity<ResponseWrapper<Boolean>> checkAssetLinked(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long assetId,
            @PathVariable Long userId) {
        try {
            boolean linked = linkService.isAssetLinkedToUser(assetId, userId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Link check completed", linked));
        } catch (Exception e) {
            log.error("❌ Failed to check link: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getLinkStatistics(
            @RequestHeader HttpHeaders headers) {
        try {
            Map<String, Object> stats = linkService.getLinkStatistics();
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Link statistics retrieved", stats));
        } catch (Exception e) {
            log.error("❌ Failed to get statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 BULK OPERATIONS
    // ============================================================
    @PostMapping("/bulk-link-assets")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> bulkLinkAssets(
            @RequestHeader HttpHeaders headers,
            @RequestBody UserAssetLinkAgentRequest request) {
        try {
            List<Long> assetIds = request.getAssetIds();
            Long userId = request.getUserId();
            String username = request.getUsername();
            String createdBy = request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM";
            Map<String, Object> result = linkService.bulkLinkAssetsToUser(
                    assetIds, userId, username, createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Bulk linking completed", result));
        } catch (Exception e) {
            log.error("❌ Failed to bulk link assets: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }
}
