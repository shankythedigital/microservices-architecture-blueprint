
package com.example.asset.controller;

import com.example.asset.dto.AssetAmcDto;
import com.example.asset.dto.AssetAmcRequest;
import com.example.asset.service.AssetAmcService;
import com.example.common.util.ResponseWrapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ AssetAmcController (REST Only)
 * Handles all AMC (Annual Maintenance Contract) operations:
 * create, update, delete, list, and getById.
 * Document uploads are now managed via DocumentController.
 */
@RestController
@RequestMapping("/api/asset/v1/amc")
public class AssetAmcController {

    private static final Logger log = LoggerFactory.getLogger(AssetAmcController.class);
    private final AssetAmcService assetAmcService;

    public AssetAmcController(AssetAmcService assetAmcService) {
        this.assetAmcService = assetAmcService;
    }

    // ============================================================
    // 🟢 CREATE AMC
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<AssetAmcDto>> create(
            @RequestHeader HttpHeaders headers,
            @Valid @RequestBody AssetAmcRequest request) {

        try {
            AssetAmcDto created = assetAmcService.create(headers, request, null);
            log.info("✅ AMC created successfully by user={} for assetId={}", 
                    request.getUsername(), request.getAssetId());

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✅ AMC created successfully", created));

        } catch (Exception e) {
            log.error("❌ AMC creation failed for user={} : {}", 
                    request.getUsername(), e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ AMC creation failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE AMC
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetAmcDto>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @Valid @RequestBody AssetAmcRequest request) {

        try {
            AssetAmcDto updated = assetAmcService.update(headers, id, request, null);
            log.info("✏️ AMC updated successfully by user={} for amcId={}", 
                    request.getUsername(), id);

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✏️ AMC updated successfully", updated));

        } catch (Exception e) {
            log.error("❌ AMC update failed for amcId={} : {}", id, e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ AMC update failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE AMC
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody AssetAmcRequest request) {

        try {
            assetAmcService.softDelete(headers, id, request);
            log.info("🗑️ AMC soft-deleted successfully by user={} amcId={}", 
                    request.getUsername(), id);

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "🗑️ AMC deleted successfully", null));

        } catch (Exception e) {
            log.error("❌ AMC delete failed for amcId={} : {}", id, e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ AMC deletion failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST ALL AMC RECORDS
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<AssetAmcDto>>> list() {
        try {
            List<AssetAmcDto> list = assetAmcService.list();
            log.info("📋 Retrieved {} AMC records", list.size());

            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📋 AMC list fetched successfully", list));

        } catch (Exception e) {
            log.error("❌ Failed to fetch AMC list: {}", e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Failed to fetch AMC list: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 FIND AMC BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetAmcDto>> find(@PathVariable Long id) {
        try {
            return assetAmcService.find(id)
                    .map(amc -> {
                        log.info("🔍 AMC found successfully (ID={})", id);
                        return ResponseEntity.ok(
                                new ResponseWrapper<>(true, "🔍 AMC found successfully", amc));
                    })
                    .orElseGet(() -> {
                        log.warn("⚠️ AMC not found (ID={})", id);
                        return ResponseEntity.status(404)
                                .body(new ResponseWrapper<>(false, "⚠️ AMC not found", null));
                    });

        } catch (Exception e) {
            log.error("❌ Failed to fetch AMC (ID={}): {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Failed to fetch AMC: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for an AMC (accessible to all authenticated users)
     * PUT /api/asset/v1/amc/{id}/favourite
     */
    @PutMapping("/{id}/favourite")
    public ResponseEntity<ResponseWrapper<AssetAmcDto>> updateFavourite(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isFavourite", defaultValue = "true") Boolean isFavourite) {
        try {
            AssetAmcDto updated = assetAmcService.updateFavourite(headers, id, isFavourite);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ AMC favourite updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update AMC favourite: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Toggle most like status for an AMC (accessible to all authenticated users)
     * PUT /api/asset/v1/amc/{id}/most-like
     */
    @PutMapping("/{id}/most-like")
    public ResponseEntity<ResponseWrapper<AssetAmcDto>> updateMostLike(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isMostLike", defaultValue = "true") Boolean isMostLike) {
        try {
            AssetAmcDto updated = assetAmcService.updateMostLike(headers, id, isMostLike);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ AMC most like updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update AMC most like: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Update sequence order for an AMC (admin only)
     * PUT /api/asset/v1/amc/{id}/sequence-order
     */
    @PutMapping("/{id}/sequence-order")
    public ResponseEntity<ResponseWrapper<AssetAmcDto>> updateSequenceOrder(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam("sequenceOrder") Integer sequenceOrder) {
        try {
            AssetAmcDto updated = assetAmcService.updateSequenceOrder(headers, id, sequenceOrder);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📊 AMC sequence order updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update AMC sequence order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }
}

