
package com.example.asset.controller;

import com.example.asset.dto.BulkMakeRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.MakeDto;
import com.example.asset.dto.MakeRequest;
import com.example.asset.entity.ProductMake;
import com.example.asset.mapper.MakeMapper;
import com.example.asset.service.ExcelParsingService;
import com.example.asset.service.MakeService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ✅ MakeController
 * Handles ProductMake CRUD endpoints with token-based authentication.
 */
@RestController
@RequestMapping("/api/asset/v1/makes")
public class MakeController {

    private static final Logger log = LoggerFactory.getLogger(MakeController.class);

    private final MakeService makeService;
    private final ExcelParsingService excelParsingService;

    public MakeController(MakeService makeService, ExcelParsingService excelParsingService) {
        this.makeService = makeService;
        this.excelParsingService = excelParsingService;
    }

    // ============================================================
    // 🟢 CREATE
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<MakeDto>> create(@RequestHeader HttpHeaders headers,
                                                               @RequestBody MakeRequest request) {
        try {
            ProductMake created = makeService.create(headers, request);
            // Convert entity to DTO to include all optional fields in JSON response
            MakeDto result = MakeMapper.toDto(created);
            log.info("✅ Make created successfully: {}", created.getMakeName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Make created successfully", result));
        } catch (Exception e) {
            log.error("❌ Failed to create make: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductMake>> update(@RequestHeader HttpHeaders headers,
                                                               @PathVariable Long id,
                                                               @RequestBody MakeRequest request) {
        try {
            ProductMake updated = makeService.update(headers, id, request);
            log.info("✏️ Make updated successfully: {}", updated.getMakeName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Make updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update make: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ DELETE (SOFT)
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(@RequestHeader HttpHeaders headers,
                                                        @PathVariable Long id,
                                                        @RequestBody MakeRequest request) {
        try {
            makeService.softDelete(headers, id, request);
            log.info("🗑️ Make deleted successfully: {}", id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Make deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete make: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ProductMake>>> list() {
        List<ProductMake> makes = makeService.list();
        return ResponseEntity.ok(new ResponseWrapper<>(true, "Fetched all makes successfully", makes));
    }

    // ============================================================
    // 🔍 FIND BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductMake>> find(@PathVariable Long id) {
        return makeService.find(id)
                .map(make -> ResponseEntity.ok(new ResponseWrapper<>(true, "Make found successfully", make)))
                .orElse(ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "Make not found", null)));
    }

    // ============================================================
    // 📦 BULK UPLOAD MAKES
    // ============================================================
    @PostMapping("/bulk")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<MakeDto>>> bulkCreate(
            @RequestHeader HttpHeaders headers,
            @RequestBody BulkMakeRequest request) {
        try {
            if (request.getMakes() == null || request.getMakes().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Make list cannot be empty", null));
            }
    
            BulkUploadResponse<MakeDto> result =
                    makeService.bulkCreate(headers, request);
    
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
    // 📊 EXCEL BULK UPLOAD MAKES
    // ============================================================
    @PostMapping("/bulk/excel")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<MakeDto>>> bulkUploadFromExcel(
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
    
            List<BulkMakeRequest.SimpleMakeDto> rows =
                    excelParsingService.parseMakesSimple(file);
    
            if (rows.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "No valid make data in Excel", null));
            }
    
            BulkMakeRequest request = new BulkMakeRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setMakes(rows);
    
            BulkUploadResponse<MakeDto> result =
                    makeService.bulkCreate(headers, request);
    
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
     * Toggle favourite status for a make (accessible to all authenticated users)
     * PUT /api/asset/v1/makes/{id}/favourite
     */
    @PutMapping("/{id}/favourite")
    public ResponseEntity<ResponseWrapper<MakeDto>> updateFavourite(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isFavourite", defaultValue = "true") Boolean isFavourite) {
        try {
            MakeDto updated = makeService.updateFavourite(headers, id, isFavourite);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Make favourite updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update make favourite: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Toggle most like status for a make (accessible to all authenticated users)
     * PUT /api/asset/v1/makes/{id}/most-like
     */
    @PutMapping("/{id}/most-like")
    public ResponseEntity<ResponseWrapper<MakeDto>> updateMostLike(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isMostLike", defaultValue = "true") Boolean isMostLike) {
        try {
            MakeDto updated = makeService.updateMostLike(headers, id, isMostLike);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Make most like updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update make most like: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Update sequence order for a make (admin only)
     * PUT /api/asset/v1/makes/{id}/sequence-order
     */
    @PutMapping("/{id}/sequence-order")
    public ResponseEntity<ResponseWrapper<MakeDto>> updateSequenceOrder(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam("sequenceOrder") Integer sequenceOrder) {
        try {
            MakeDto updated = makeService.updateSequenceOrder(headers, id, sequenceOrder);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📊 Make sequence order updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update make sequence order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }
}


