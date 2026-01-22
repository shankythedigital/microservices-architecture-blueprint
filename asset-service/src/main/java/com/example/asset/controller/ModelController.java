
package com.example.asset.controller;

import com.example.asset.dto.BulkModelRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.ModelDto;
import com.example.asset.dto.ModelRequest;
import com.example.asset.service.ExcelParsingService;
import com.example.asset.service.ModelService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ✅ ModelController
 * Handles CRUD endpoints for ProductModel using DTOs.
 */
@RestController
@RequestMapping("/api/asset/v1/models")
public class ModelController {

    private static final Logger log = LoggerFactory.getLogger(ModelController.class);
    private final ModelService modelService;
    private final ExcelParsingService excelParsingService;

    public ModelController(ModelService modelService, ExcelParsingService excelParsingService) {
        this.modelService = modelService;
        this.excelParsingService = excelParsingService;
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<ModelDto>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody ModelRequest request) {
        try {
            ModelDto dto = modelService.create(headers, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Model created successfully", dto));
        } catch (Exception e) {
            log.error("❌ Model create failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ModelDto>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody ModelRequest request) {
        try {
            ModelDto dto = modelService.update(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Model updated successfully", dto));
        } catch (Exception e) {
            log.error("❌ Model update failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody ModelRequest request) {
        try {
            modelService.softDelete(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Model deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Model delete failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ModelDto>>> list() {
        List<ModelDto> models = modelService.list();
        return ResponseEntity.ok(new ResponseWrapper<>(true, "📋 Models fetched successfully", models));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ModelDto>> find(@PathVariable Long id) {
        return modelService.find(id)
                .map(dto -> ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Model found", dto)))
                .orElse(ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "❌ Model not found", null)));
    }

    // ============================================================
    // 📦 BULK UPLOAD MODELS
    // ============================================================
    @PostMapping("/bulk")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<ModelDto>>> bulkCreate(
            @RequestHeader HttpHeaders headers,
            @RequestBody BulkModelRequest request) {
        try {
            if (request.getModels() == null || request.getModels().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Model list cannot be empty", null));
            }
    
            BulkUploadResponse<ModelDto> result =
                    modelService.bulkCreate(headers, request);
    
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    String.format("Bulk upload completed: %d/%d successful",
                            result.getSuccessCount(), result.getTotalCount()),
                    result
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📊 EXCEL BULK UPLOAD MODELS
    // ============================================================
    @PostMapping("/bulk/excel")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<ModelDto>>> bulkUploadFromExcel(
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
    
            List<BulkModelRequest.SimpleModelDto> rows =
                    excelParsingService.parseModelsSimple(file);
    
            if (rows.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "No valid model data in Excel", null));
            }
    
            BulkModelRequest request = new BulkModelRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setModels(rows);
    
            BulkUploadResponse<ModelDto> result =
                    modelService.bulkCreate(headers, request);
    
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    String.format("Excel upload completed: %d/%d successful",
                            result.getSuccessCount(), result.getTotalCount()),
                    result
            ));
    
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for a model (accessible to all authenticated users)
     * PUT /api/asset/v1/models/{id}/favourite
     */
    @PutMapping("/{id}/favourite")
    public ResponseEntity<ResponseWrapper<ModelDto>> updateFavourite(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isFavourite", defaultValue = "true") Boolean isFavourite) {
        try {
            ModelDto updated = modelService.updateFavourite(headers, id, isFavourite);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Model favourite updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update model favourite: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Toggle most like status for a model (accessible to all authenticated users)
     * PUT /api/asset/v1/models/{id}/most-like
     */
    @PutMapping("/{id}/most-like")
    public ResponseEntity<ResponseWrapper<ModelDto>> updateMostLike(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isMostLike", defaultValue = "true") Boolean isMostLike) {
        try {
            ModelDto updated = modelService.updateMostLike(headers, id, isMostLike);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Model most like updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update model most like: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Update sequence order for a model (admin only)
     * PUT /api/asset/v1/models/{id}/sequence-order
     */
    @PutMapping("/{id}/sequence-order")
    public ResponseEntity<ResponseWrapper<ModelDto>> updateSequenceOrder(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam("sequenceOrder") Integer sequenceOrder) {
        try {
            ModelDto updated = modelService.updateSequenceOrder(headers, id, sequenceOrder);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📊 Model sequence order updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update model sequence order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }
}


