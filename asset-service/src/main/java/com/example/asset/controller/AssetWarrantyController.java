
package com.example.asset.controller;

import com.example.asset.dto.AssetWarrantyDto;
import com.example.asset.dto.AssetWarrantyRequest;
import com.example.asset.service.AssetWarrantyService;
import com.example.asset.util.ByteArrayMultipartFile;
import com.example.asset.service.DocumentTypeMasterService;
import org.springframework.web.multipart.MultipartFile;
import com.example.common.jackson.JacksonObjectMappers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;
import com.example.common.util.ResponseWrapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ AssetWarrantyController (JSON-only)
 * Handles Warranty creation, updates, deletions, and retrieval.
 * Document uploads are handled separately via DocumentController.
 */
@RestController
@RequestMapping("/api/asset/v1/warranty")
public class AssetWarrantyController {

    private static final Logger log = LoggerFactory.getLogger(AssetWarrantyController.class);
    private final AssetWarrantyService warrantyService;
    private final DocumentTypeMasterService documentTypeMasterService;

    public AssetWarrantyController(AssetWarrantyService warrantyService, DocumentTypeMasterService documentTypeMasterService) {
        this.warrantyService = warrantyService;
        this.documentTypeMasterService = documentTypeMasterService;
    }

    // ============================================================
    // 🟢 CREATE WARRANTY (JSON only)
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<AssetWarrantyDto>> create(
            @RequestHeader HttpHeaders headers,
            @Valid @RequestBody AssetWarrantyRequest request) {
        try {
            AssetWarrantyDto created = warrantyService.create(headers, request, null);
            log.info("✅ Warranty created successfully by user={} for assetId={}",
                    request.getUsername(), request.getAssetId());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Warranty created successfully", created));
        } catch (Exception e) {
            log.error("❌ Warranty creation failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Warranty creation failed: " + e.getMessage(), null));
        }
    }

    @PostMapping(path = "/with-document", consumes = "application/json")
    public ResponseEntity<ResponseWrapper<AssetWarrantyDto>> createWithDocument(
            @RequestHeader HttpHeaders headers,
            @RequestBody Map<String, Object> body) {
        try {
            ObjectMapper mapper = JacksonObjectMappers.standard();
            AssetWarrantyRequest request = mapper.convertValue(body.get("request"), AssetWarrantyRequest.class);
            String document = (String) body.get("document");
            String docType = (String) body.get("docType");
            if (document == null || document.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ document is required (base64)", null));
            if (docType == null || docType.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ docType is required", null));
            documentTypeMasterService.validate(docType);
            request.setDocType(docType.trim());
            byte[] bytes = Base64.getDecoder().decode(document);
            MultipartFile multipartFile = new ByteArrayMultipartFile(bytes, "document", "document." + docType);
            AssetWarrantyDto created = warrantyService.create(headers, request, multipartFile);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Warranty created successfully with document", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Warranty creation with document failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE WARRANTY (JSON only)
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetWarrantyDto>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @Valid @RequestBody AssetWarrantyRequest request) {
        try {
            AssetWarrantyDto updated = warrantyService.update(headers, id, request, null);
            log.info("✏️ Warranty updated successfully by user={} for warrantyId={}",
                    request.getUsername(), id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Warranty updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Warranty update failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Warranty update failed: " + e.getMessage(), null));
        }
    }

    @PutMapping(path = "/{id}/with-document", consumes = "application/json")
    public ResponseEntity<ResponseWrapper<AssetWarrantyDto>> updateWithDocument(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            ObjectMapper mapper = JacksonObjectMappers.standard();
            AssetWarrantyRequest request = mapper.convertValue(body.get("request"), AssetWarrantyRequest.class);
            String document = (String) body.get("document");
            String docType = (String) body.get("docType");
            if (document == null || document.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ document is required (base64)", null));
            if (docType == null || docType.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ docType is required", null));
            documentTypeMasterService.validate(docType);
            request.setDocType(docType.trim());
            byte[] bytes = Base64.getDecoder().decode(document);
            MultipartFile multipartFile = new ByteArrayMultipartFile(bytes, "document", "document." + docType);
            AssetWarrantyDto updated = warrantyService.update(headers, id, request, multipartFile);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Warranty updated successfully with document", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Warranty update with document failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ DELETE WARRANTY (soft delete)
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody AssetWarrantyRequest request) {
        try {
            warrantyService.softDelete(headers, id, request);
            log.info("🗑️ Warranty deleted successfully by user={} warrantyId={}", request.getUsername(), id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Warranty deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete Warranty: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Warranty deletion failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST + FIND
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<AssetWarrantyDto>>> list() {
        try {
            List<AssetWarrantyDto> list = warrantyService.list();
            log.info("📋 Fetched {} Warranty records", list.size());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "📋 Warranty list fetched successfully", list));
        } catch (Exception e) {
            log.error("❌ Failed to fetch Warranty list: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Failed to fetch Warranty list: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetWarrantyDto>> find(@PathVariable Long id) {
        try {
            return warrantyService.find(id)
                    .map(warranty -> ResponseEntity.ok(new ResponseWrapper<>(true, "🔍 Warranty found successfully", warranty)))
                    .orElse(ResponseEntity.status(404)
                            .body(new ResponseWrapper<>(false, "⚠️ Warranty not found", null)));
        } catch (Exception e) {
            log.error("❌ Failed to fetch Warranty: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Failed to fetch Warranty: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for a warranty (accessible to all authenticated users)
     * PUT /api/asset/v1/warranty/{id}/favourite
     */
    @PutMapping("/{id}/favourite")
    public ResponseEntity<ResponseWrapper<AssetWarrantyDto>> updateFavourite(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isFavourite", defaultValue = "true") Boolean isFavourite) {
        try {
            AssetWarrantyDto updated = warrantyService.updateFavourite(headers, id, isFavourite);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Warranty favourite updated successfully", updated)
            );
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("No authenticated user")) {
                log.error("❌ Failed to update warranty favourite: Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseWrapper<>(false, "❌ Authentication required. Please provide a valid JWT token.", null));
            }
            log.error("❌ Failed to update warranty favourite: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to update warranty favourite: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Toggle most like status for a warranty (accessible to all authenticated users)
     * PUT /api/asset/v1/warranty/{id}/most-like
     */
    @PutMapping("/{id}/most-like")
    public ResponseEntity<ResponseWrapper<AssetWarrantyDto>> updateMostLike(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isMostLike", defaultValue = "true") Boolean isMostLike) {
        try {
            AssetWarrantyDto updated = warrantyService.updateMostLike(headers, id, isMostLike);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Warranty most like updated successfully", updated)
            );
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("No authenticated user")) {
                log.error("❌ Failed to update warranty most like: Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseWrapper<>(false, "❌ Authentication required. Please provide a valid JWT token.", null));
            }
            log.error("❌ Failed to update warranty most like: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to update warranty most like: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Update sequence order for a warranty (admin only)
     * PUT /api/asset/v1/warranty/{id}/sequence-order
     */
    @PutMapping("/{id}/sequence-order")
    public ResponseEntity<ResponseWrapper<AssetWarrantyDto>> updateSequenceOrder(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam("sequenceOrder") Integer sequenceOrder) {
        try {
            AssetWarrantyDto updated = warrantyService.updateSequenceOrder(headers, id, sequenceOrder);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📊 Warranty sequence order updated successfully", updated)
            );
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("No authenticated user")) {
                log.error("❌ Failed to update warranty sequence order: Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseWrapper<>(false, "❌ Authentication required. Please provide a valid JWT token.", null));
            }
            if (e.getMessage() != null && e.getMessage().contains("Access denied")) {
                log.error("❌ Failed to update warranty sequence order: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
            }
            log.error("❌ Failed to update warranty sequence order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to update warranty sequence order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }
}


