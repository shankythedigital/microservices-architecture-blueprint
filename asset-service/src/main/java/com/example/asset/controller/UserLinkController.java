


package com.example.asset.controller;

import com.example.asset.dto.AssetUserUniversalLinkRequest;
import com.example.asset.dto.AssetUserMultiLinkRequest;
import com.example.asset.dto.AssetUserMultiDelinkRequest;
import com.example.asset.service.UserLinkService;
import com.example.asset.service.ValidationService;
import com.example.common.util.ResponseWrapper;

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

    public UserLinkController(UserLinkService linkService,
                              ValidationService validationService) {
        this.linkService = linkService;
        this.validationService = validationService;
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

    private String extractBearer(HttpHeaders headers) {
        String token = headers.getFirst("Authorization");
        if (token == null) throw new IllegalStateException("Missing Authorization header");
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}


