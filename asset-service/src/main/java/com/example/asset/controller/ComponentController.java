
package com.example.asset.controller;

import com.example.asset.dto.BulkComponentRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.ComponentDto;
import com.example.asset.dto.ComponentRequest;
import com.example.asset.entity.AssetComponent;
import com.example.asset.mapper.ComponentMapper;
import com.example.asset.service.ComponentService;
import com.example.asset.service.ExcelParsingService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ✅ ComponentController
 * - Handles CRUD via @RequestBody (ComponentRequest)
 * - Extracts Bearer token from Authorization header
 * - Delegates logic + notifications to ComponentService
 */
@RestController
@RequestMapping("/api/asset/v1/components")
public class ComponentController {

    private static final Logger log = LoggerFactory.getLogger(ComponentController.class);
    private final ComponentService componentService;
    private final ExcelParsingService excelParsingService;

    public ComponentController(ComponentService componentService, ExcelParsingService excelParsingService) {
        this.componentService = componentService;
        this.excelParsingService = excelParsingService;
    }

    // ============================================================
    // 🟢 CREATE COMPONENT
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<ComponentDto>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody ComponentRequest request) {
        try {
            AssetComponent created = componentService.create(headers, request);
            // Convert entity to DTO to include all optional fields in JSON response
            ComponentDto result = ComponentMapper.toDto(created);
            log.info("✅ Component created successfully by user={} id={}", request.getUsername(), created.getComponentId());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Component created successfully", result));
        } catch (Exception e) {
            log.error("❌ Failed to create component: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE COMPONENT
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetComponent>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody ComponentRequest request) {
        try {
            AssetComponent updated = componentService.update(headers, id, request);
            log.info("✏️ Component updated successfully by user={} id={}", request.getUsername(), id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Component updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update component: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE COMPONENT
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody ComponentRequest request) {
        try {
            componentService.softDelete(headers, id, request);
            log.info("🗑️ Component soft-deleted successfully by user={} id={}", request.getUsername(), id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Component deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete component: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST COMPONENTS
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<AssetComponent>>> list() {
        List<AssetComponent> components = componentService.list();
        log.info("📋 Fetched {} active components", components.size());
        return ResponseEntity.ok(new ResponseWrapper<>(true, "📋 Components fetched successfully", components));
    }

    // ============================================================
    // 🔍 GET COMPONENT BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetComponent>> find(@PathVariable Long id) {
        return componentService.find(id)
                .map(c -> ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Component found", c)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "❌ Component not found", null)));
    }

    // ============================================================
    // 📦 BULK UPLOAD COMPONENTS
    // ============================================================
    @PostMapping("/bulk")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<ComponentDto>>> bulkCreate(
            @RequestHeader HttpHeaders headers,
            @RequestBody BulkComponentRequest request) {
        try {
            if (request.getComponents() == null || request.getComponents().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Component list cannot be empty", null));
            }
    
            BulkUploadResponse<ComponentDto> result =
                    componentService.bulkCreate(headers, request);
    
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
    // 📊 EXCEL BULK UPLOAD COMPONENTS
    // ============================================================
    @PostMapping("/bulk/excel")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<ComponentDto>>> bulkUploadFromExcel(
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
    
            List<BulkComponentRequest.SimpleComponentDto> rows =
                    excelParsingService.parseComponentsSimple(file);
    
            if (rows.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "No valid component data in Excel", null));
            }
    
            BulkComponentRequest request = new BulkComponentRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setComponents(rows);
    
            BulkUploadResponse<ComponentDto> result =
                    componentService.bulkCreate(headers, request);
    
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
     * Toggle favourite status for a component (accessible to all authenticated users)
     * PUT /api/asset/v1/components/{id}/favourite
     */
    @PutMapping("/{id}/favourite")
    public ResponseEntity<ResponseWrapper<ComponentDto>> updateFavourite(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isFavourite", defaultValue = "true") Boolean isFavourite) {
        try {
            ComponentDto updated = componentService.updateFavourite(headers, id, isFavourite);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Component favourite updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update component favourite: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Toggle most like status for a component (accessible to all authenticated users)
     * PUT /api/asset/v1/components/{id}/most-like
     */
    @PutMapping("/{id}/most-like")
    public ResponseEntity<ResponseWrapper<ComponentDto>> updateMostLike(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isMostLike", defaultValue = "true") Boolean isMostLike) {
        try {
            ComponentDto updated = componentService.updateMostLike(headers, id, isMostLike);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Component most like updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update component most like: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Update sequence order for a component (admin only)
     * PUT /api/asset/v1/components/{id}/sequence-order
     */
    @PutMapping("/{id}/sequence-order")
    public ResponseEntity<ResponseWrapper<ComponentDto>> updateSequenceOrder(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam("sequenceOrder") Integer sequenceOrder) {
        try {
            ComponentDto updated = componentService.updateSequenceOrder(headers, id, sequenceOrder);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📊 Component sequence order updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update component sequence order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }
}

