


package com.example.asset.controller;

import com.example.asset.dto.AssetUserUniversalLinkRequest;
import com.example.asset.dto.AssetUserMultiLinkRequest;
import com.example.asset.dto.AssetUserMultiDelinkRequest;
import com.example.asset.service.UserLinkService;
import com.example.asset.service.ValidationService;
import com.example.common.security.JwtVerifier;
import com.example.common.util.ResponseWrapper;
import io.jsonwebtoken.Claims;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asset/v1/userlinks")
public class UserLinkController {

    private static final Logger log = LoggerFactory.getLogger(UserLinkController.class);

    private final UserLinkService linkService;
    private final ValidationService validationService;
    private final JwtVerifier jwtVerifier;

    public UserLinkController(UserLinkService linkService,
                              ValidationService validationService,
                              JwtVerifier jwtVerifier) {
        this.linkService = linkService;
        this.validationService = validationService;
        this.jwtVerifier = jwtVerifier;
    }

    // ================================
    // LINK
    // ================================
    @PostMapping("/link")
    public ResponseEntity<ResponseWrapper<String>> linkEntity(
            @RequestHeader HttpHeaders headers,
            @RequestBody AssetUserUniversalLinkRequest request) {

        log.info("📌 Unified Link Request: {}", request);
        try {
            String token = extractBearer(headers);

            // Validation (SME Logic)
            validationService.validateLinkRequest(request);

            String msg = linkService.linkEntity(
                    token,
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getTargetUserId(),
                    request.getTargetUsername(),
                    request.getUserId(),
                    request.getUsername()
            );

            return ResponseEntity.ok(new ResponseWrapper<>(true, msg, null));

        } catch (Exception e) {
            log.error("❌ Link Failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ================================
    // DELINK
    // ================================
    @PostMapping("/delink")
    public ResponseEntity<ResponseWrapper<String>> delinkEntity(
            @RequestHeader HttpHeaders headers,
            @RequestBody AssetUserUniversalLinkRequest request) {

        log.info("📌 Unified Delink Request: {}", request);
        try {
            String token = extractBearer(headers);

            validationService.ensureEntityLinked(
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getTargetUserId()
            );

            String msg = linkService.delinkEntity(
                    token,
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getTargetUserId(),
                    request.getTargetUsername(),
                    request.getUserId(),
                    request.getUsername()
            );

            return ResponseEntity.ok(new ResponseWrapper<>(true, msg, null));

        } catch (Exception e) {
            log.error("❌ Delink Failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }


    // ========================================================================
    // 6️⃣ LINK MULTIPLE ENTITIES IN ONE REQUEST
    // ========================================================================
    @PostMapping("/link-multiple")
    @Operation(summary = "Link a user to multiple entities (ASSET, COMPONENT, MODEL, MAKE, AMC, WARRANTY, DOCUMENT) in one request")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> linkMultipleEntities(
            @RequestHeader HttpHeaders headers,
            @RequestBody AssetUserMultiLinkRequest request) {

        log.info("📌 MULTI-LINK REQUEST: {}", request);

        try {
            String token = extractBearer(headers);

            Map<String, Object> result =
                    linkService.linkMultipleEntities(token, request, validationService);

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true,
                            "Multi-entity link process completed", result));

        } catch (Exception e) {
            log.error("❌ Multi-link operation failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }


    // ========================================================================
// 7️⃣ DELINK MULTIPLE ENTITIES (ASSET, COMPONENT, MODEL, MAKE, AMC, WARRANTY, DOCUMENT)
// ========================================================================
@PostMapping("/delink-multiple")
@Operation(summary = "Delink a user from multiple entities (ASSET, COMPONENT, MODEL, MAKE, AMC, WARRANTY, DOCUMENT) in one request")
public ResponseEntity<ResponseWrapper<Map<String, Object>>> delinkMultipleEntities(
        @RequestHeader HttpHeaders headers,
        @RequestBody AssetUserMultiDelinkRequest request) {

    log.info("📌 MULTI-DELINK REQUEST: {}", request);

    try {
        String token = extractBearer(headers);

        Map<String, Object> result =
                linkService.delinkMultipleEntities(token, request, validationService);

        return ResponseEntity.ok(
                new ResponseWrapper<>(true,
                        "Multi-entity delink process completed", result));

    } catch (Exception e) {
        log.error("❌ Multi-delink operation failed: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
                .body(new ResponseWrapper<>(false, e.getMessage(), null));
    }
}


    // ================================
    // GET ALL ASSIGNED ITEMS
    // ================================
    @GetMapping("/assigned-assets")
    public ResponseEntity<ResponseWrapper<List<Map<String, Object>>>> getAssignedAssets(
            @RequestHeader HttpHeaders headers,
            @RequestParam Long targetUserId) {

        extractBearer(headers);

        List<Map<String, Object>> result = linkService.getAssetsAssignedToUser(targetUserId);

        return ResponseEntity.ok(new ResponseWrapper<>(true, "Assigned items fetched", result));
    }


    // ================================
    // GET SINGLE ITEM
    // ================================
    @GetMapping("/asset")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getSingleAsset(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) Long componentId) {

        extractBearer(headers);

        Map<String, Object> result = linkService.getSingleAsset(assetId, componentId);

        return ResponseEntity.ok(new ResponseWrapper<>(true, "Fetched", result));
    }

    // ================================
    // GET USERS BY SUBCATEGORY
    // ================================
    @GetMapping("/by-subcategory")
    public ResponseEntity<ResponseWrapper<List<Map<String, Object>>>> getBySubCategory(
            @RequestHeader HttpHeaders headers,
            @RequestParam Long subCategoryId) {

        String token = extractBearer(headers);

        List<Map<String, Object>> result = linkService.getUsersBySubCategory(token, subCategoryId);

        return ResponseEntity.ok(new ResponseWrapper<>(true, "Users fetched", result));
    }

    // ================================
    // GET ALL MASTER DATA IN DETAIL
    // ================================
    @GetMapping("/master-data/all")
    @Operation(summary = "Get comprehensive master data including users, assets, components, warranties, AMCs, makes, models, categories, sub-categories, vendors, outlets, and statuses. Optionally filter by userId.")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getAllMasterDataInDetail(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) Long userId) {

        log.info("📊 Comprehensive Master Data Request{}", userId != null ? " for userId: " + userId : "");
        try {
            String token = extractBearer(headers);
            
            // Extract login user info from token for audit
            Long loginUserId = null;
            String loginUsername = null;
            try {
                String tokenValue = token.startsWith("Bearer ") ? token.substring(7) : token;
                Claims claims = jwtVerifier.parseToken(tokenValue).getBody();
                String userIdStr = claims.getSubject();
                if (userIdStr != null) {
                    try {
                        loginUserId = Long.parseLong(userIdStr);
                    } catch (NumberFormatException e) {
                        log.warn("⚠️ Could not parse userId from token: {}", userIdStr);
                    }
                }
                Object usernameObj = claims.get("username");
                if (usernameObj == null) {
                    usernameObj = claims.get("preferred_username");
                }
                if (usernameObj != null) {
                    loginUsername = usernameObj.toString();
                }
            } catch (Exception e) {
                log.warn("⚠️ Could not extract user info from token: {}", e.getMessage());
            }

            Map<String, Object> result = linkService.getAllMasterDataInDetailByUserId(userId, loginUserId, loginUsername);

            String message = userId != null ? 
                    "Master data retrieved successfully for user ID: " + userId : 
                    "All master data retrieved successfully";
            
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, message, result));

        } catch (Exception e) {
            log.error("❌ Failed to retrieve master data{}: {}", 
                    userId != null ? " for userId: " + userId : "", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ================================
    // NEED YOUR ATTENTION API
    // ================================
    @GetMapping("/need-your-attention")
    @Operation(summary = "Get comprehensive 'Need Your Attention' data including all entities in detail", 
               description = "Returns all features: users, assets, components, warranties, AMCs, makes, models, categories, sub-categories, vendors, outlets, statuses, and attention indicators (expiring warranties, expiring AMCs, assets without warranty/AMC, unassigned assets)")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getNeedYourAttention(
            @RequestHeader HttpHeaders headers) {

        log.info("📊 Need Your Attention API Request");
        try {
            String token = extractBearer(headers);
            
            // Extract login user info from token for audit
            Long loginUserId = null;
            String loginUsername = null;
            try {
                String tokenValue = token.startsWith("Bearer ") ? token.substring(7) : token;
                Claims claims = jwtVerifier.parseToken(tokenValue).getBody();
                String userIdStr = claims.getSubject();
                if (userIdStr != null) {
                    try {
                        loginUserId = Long.parseLong(userIdStr);
                    } catch (NumberFormatException e) {
                        log.warn("⚠️ Could not parse userId from token: {}", userIdStr);
                    }
                }
                Object usernameObj = claims.get("username");
                if (usernameObj == null) {
                    usernameObj = claims.get("preferred_username");
                }
                if (usernameObj != null) {
                    loginUsername = usernameObj.toString();
                }
            } catch (Exception e) {
                log.warn("⚠️ Could not extract user info from token: {}", e.getMessage());
            }

            Map<String, Object> result = linkService.getNeedYourAttentionData(loginUserId, loginUsername);
            
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "Need Your Attention data retrieved successfully", result));

        } catch (Exception e) {
            log.error("❌ Failed to retrieve Need Your Attention data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    private String extractBearer(HttpHeaders headers) {
        String token = headers.getFirst("Authorization");
        if (token == null) throw new IllegalStateException("Missing Authorization header");
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}


