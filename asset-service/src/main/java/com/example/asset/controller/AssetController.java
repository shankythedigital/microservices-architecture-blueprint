package com.example.asset.controller;

import com.example.asset.dto.AssetRequest;
import com.example.asset.dto.AssetResponseDTO;
import com.example.asset.dto.BulkAssetRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.CompleteAssetCreationRequest;
import com.example.asset.entity.AssetMaster;
import com.example.asset.service.AssetCrudService;
import com.example.asset.service.ExcelParsingService;
import com.example.asset.util.ByteArrayMultipartFile;
import com.example.asset.service.DocumentTypeMasterService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;
import com.example.common.util.ResponseWrapper;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * ✅ AssetController
 * Central REST controller for Asset CRUD operations.
 * Accepts @RequestBody AssetRequest (includes userId, username, projectType).
 * Extracts Bearer token from Authorization header.
 */
@RestController
@RequestMapping("/api/asset/v1/assets")
public class AssetController {

    private static final Logger log = LoggerFactory.getLogger(AssetController.class);
    private final AssetCrudService assetService;
    private final DocumentTypeMasterService documentTypeMasterService;
    private final ExcelParsingService excelParsingService;

    public AssetController(AssetCrudService assetService, DocumentTypeMasterService documentTypeMasterService, ExcelParsingService excelParsingService) {
        this.assetService = assetService;
        this.documentTypeMasterService = documentTypeMasterService;
        this.excelParsingService = excelParsingService;
    }

    // ============================================================
    // 🟢 CREATE ASSET (JSON body - document via Document API separately)
    // ============================================================
    /**
     * Create asset with JSON body.
     * Document upload via POST /api/asset/v1/documents/upload after creation.
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ResponseWrapper<AssetMaster>> create(
            @RequestHeader HttpHeaders headers,
            @Valid @RequestBody AssetRequest request) {
        log.info("📥 [POST] /assets - Creating asset for userId={} username={}",
                request.getUserId(), request.getUsername());
        try {
            AssetMaster created = assetService.create(headers, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Asset created successfully", created));
        } catch (Exception e) {
            log.error("❌ Failed to create asset: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    @PostMapping(path = "/with-document", consumes = "application/json")
    public ResponseEntity<ResponseWrapper<AssetMaster>> createWithDocument(
            @RequestHeader HttpHeaders headers,
            @RequestBody Map<String, Object> body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            AssetRequest request = mapper.convertValue(body.get("request"), AssetRequest.class);
            String document = (String) body.get("document");
            String docType = (String) body.get("docType");
            if (document == null || document.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ document is required (base64)", null));
            if (docType == null || docType.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ docType is required", null));
            documentTypeMasterService.validate(docType);
            byte[] bytes = Base64.getDecoder().decode(document);
            MultipartFile multipartFile = new ByteArrayMultipartFile(bytes, "document", "document." + docType);
            AssetMaster created = assetService.create(headers, request, multipartFile, docType.trim());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Asset created successfully with document", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to create asset with document: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE ASSET
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetMaster>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest request) {
        log.info("✏️ [PUT] /assets/{} - Updating by userId={} username={}", id, request.getUserId(), request.getUsername());
        try {
            AssetMaster updated = assetService.update(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Asset updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update asset: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    @PutMapping(path = "/{id}/with-document", consumes = "application/json")
    public ResponseEntity<ResponseWrapper<AssetMaster>> updateWithDocument(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            AssetRequest request = mapper.convertValue(body.get("request"), AssetRequest.class);
            String document = (String) body.get("document");
            String docType = (String) body.get("docType");
            if (document == null || document.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ document is required (base64)", null));
            if (docType == null || docType.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ docType is required", null));
            documentTypeMasterService.validate(docType);
            byte[] bytes = Base64.getDecoder().decode(document);
            MultipartFile multipartFile = new ByteArrayMultipartFile(bytes, "document", "document." + docType);
            AssetMaster updated = assetService.updateWithDocument(headers, id, request, multipartFile, docType.trim());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Asset updated successfully with document", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to update asset with document: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ DELETE (SOFT)
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest request) {
        log.info("🗑️ [DELETE] /assets/{} - Deleting by userId={} username={}",
                id, request.getUserId(), request.getUsername());
        try {
            assetService.softDelete(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Asset deleted successfully (soft delete)", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete asset: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 GET ASSET BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetResponseDTO>> getAssetById(@PathVariable Long id) {
        try {
            log.info("🔍 [GET] /assets/{} - Fetching asset", id);
            return assetService.get(id)
                .map(asset -> ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✅ Asset fetched successfully", asset)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseWrapper<>(false, "❌ Asset not found", null)));
        } catch (Exception e) {
            log.error("❌ Failed to fetch asset by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 SEARCH ASSETS
    // ============================================================
    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<Page<AssetResponseDTO>>> searchAssets(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("🔍 [GET] /assets/search - Searching with keyword={}, page={}, size={}", keyword, page, size);
            
            // Convert to Pageable
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            
            // Use keyword for search - delegate to service
            Page<AssetResponseDTO> result = assetService.searchByKeyword(keyword, pageable);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, 
                    "✅ Assets fetched successfully", 
                    result));
        } catch (Exception e) {
            log.error("❌ Failed to search assets: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📦 BULK UPLOAD ASSETS
    // ============================================================
    @PostMapping("/bulk")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<AssetResponseDTO>>> bulkCreate(
            @RequestHeader HttpHeaders headers,
            @RequestBody BulkAssetRequest request) {
        try {
            if (request.getAssets() == null || request.getAssets().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Asset list cannot be empty", null));
            }

            BulkUploadResponse<AssetResponseDTO> result =
                    assetService.bulkCreate(headers, request);

            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    String.format("Bulk upload completed: %d/%d successful",
                            result.getSuccessCount(), result.getTotalCount()),
                    result
            ));
        } catch (Exception e) {
            log.error("❌ Failed to bulk create assets: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📊 EXCEL BULK UPLOAD ASSETS
    // ============================================================
    @PostMapping("/bulk/excel")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<AssetResponseDTO>>> bulkUploadFromExcel(
            @RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("username") String username,
            @RequestParam(value = "projectType", required = false, defaultValue = "ASSET_SERVICE") String projectType) {
    
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Excel file cannot be empty", null));
            }
    
            List<BulkAssetRequest.SimpleAssetDto> rows =
                    excelParsingService.parseAssetsSimple(file);
    
            if (rows.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "No valid asset data in Excel", null));
            }
    
            BulkAssetRequest request = new BulkAssetRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setAssets(rows);
    
            BulkUploadResponse<AssetResponseDTO> result =
                    assetService.bulkCreate(headers, request);
    
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    String.format("Excel upload completed: %d/%d successful",
                            result.getSuccessCount(), result.getTotalCount()),
                    result
            ));
    
        } catch (Exception e) {
            log.error("❌ Failed to bulk upload assets from Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🚀 COMPLETE ASSET CREATION (All-in-One)
    // ============================================================
    /**
     * Create asset with all related information in one request:
     * - Asset basic info (Title/UI Name, Model Number, Serial Number)
     * - Warranty information (Purchase/Installation Date, Limited Warranty)
     * - Purchase Invoice document upload
     * - User assignment (Added to)
     * 
     * This endpoint combines multiple operations into a single transaction.
     * Accepts all parameters as separate form-data fields.
     */
    @PostMapping(value = "/complete", consumes = "multipart/form-data")
    @Operation(summary = "Create asset with all related information in one request", 
               description = "Creates asset, warranty, uploads document, and assigns to user in a single transaction. Accepts all parameters as separate form-data fields.")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> createCompleteAsset(
            @RequestHeader HttpHeaders headers,
            // User Context
            @RequestParam("userId") Long userId,
            @RequestParam("username") String username,
            @RequestParam(value = "projectType", required = false, defaultValue = "ASSET_SERVICE") String projectType,
            // Asset Basic Information
            @RequestParam("assetNameUdv") String assetNameUdv,
            @RequestParam("modelId") Long modelId,
            @RequestParam(value = "serialNumber", required = false) String serialNumber,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "subCategoryId", required = false) Long subCategoryId,
            @RequestParam(value = "makeId", required = false) Long makeId,
            @RequestParam(value = "assetStatus", required = false) String assetStatus,
            // Warranty Information
            @RequestParam("warrantyStartDate") String warrantyStartDate,
            @RequestParam("warrantyEndDate") String warrantyEndDate,
            @RequestParam(value = "warrantyProvider", required = false) String warrantyProvider,
            @RequestParam(value = "warrantyStatus", required = false) String warrantyStatus,
            @RequestParam(value = "warrantyTerms", required = false) String warrantyTerms,
            // User Assignment
            @RequestParam("targetUserId") Long targetUserId,
            @RequestParam(value = "targetUsername", required = false) String targetUsername,
            // Document Upload (required - stored in AssetDocument with docType)
            @RequestParam("document") MultipartFile document,
            @RequestParam("docType") String docType) {
        
        log.info("🚀 [POST] /assets/complete - Creating complete asset: name={}, modelId={}, targetUserId={}, docType={}",
                assetNameUdv, modelId, targetUserId, docType);
        
        try {
            if (document == null || document.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ Document upload is required", null));
            }
            documentTypeMasterService.validate(docType);
            // Build CompleteAssetCreationRequest from parameters
            CompleteAssetCreationRequest request = new CompleteAssetCreationRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setAssetNameUdv(assetNameUdv);
            request.setModelId(modelId);
            request.setSerialNumber(serialNumber);
            request.setCategoryId(categoryId);
            request.setSubCategoryId(subCategoryId);
            request.setMakeId(makeId);
            request.setAssetStatus(assetStatus);
            request.setWarrantyStartDate(java.time.LocalDate.parse(warrantyStartDate));
            request.setWarrantyEndDate(java.time.LocalDate.parse(warrantyEndDate));
            request.setWarrantyProvider(warrantyProvider);
            request.setWarrantyStatus(warrantyStatus);
            request.setWarrantyTerms(warrantyTerms);
            request.setTargetUserId(targetUserId);
            request.setTargetUsername(targetUsername);
            
            Map<String, Object> result = assetService.createCompleteAsset(headers, request, document, docType.trim());
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "✅ Asset created successfully with warranty, document, and user assignment",
                    result
            ));
            
        } catch (Exception e) {
            log.error("❌ Failed to create complete asset: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for an asset (accessible to all authenticated users)
     * PUT /api/asset/v1/assets/{id}/favourite
     */
    @PutMapping("/{id}/favourite")
    public ResponseEntity<ResponseWrapper<AssetMaster>> updateFavourite(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isFavourite", defaultValue = "true") Boolean isFavourite) {
        try {
            AssetMaster updated = assetService.updateFavourite(headers, id, isFavourite);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Asset favourite updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update asset favourite: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Toggle most like status for an asset (accessible to all authenticated users)
     * PUT /api/asset/v1/assets/{id}/most-like
     */
    @PutMapping("/{id}/most-like")
    public ResponseEntity<ResponseWrapper<AssetMaster>> updateMostLike(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isMostLike", defaultValue = "true") Boolean isMostLike) {
        try {
            AssetMaster updated = assetService.updateMostLike(headers, id, isMostLike);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Asset most like updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update asset most like: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Update sequence order for an asset (admin only)
     * PUT /api/asset/v1/assets/{id}/sequence-order
     */
    @PutMapping("/{id}/sequence-order")
    public ResponseEntity<ResponseWrapper<AssetMaster>> updateSequenceOrder(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam("sequenceOrder") Integer sequenceOrder) {
        try {
            AssetMaster updated = assetService.updateSequenceOrder(headers, id, sequenceOrder);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📊 Asset sequence order updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update asset sequence order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }
}



