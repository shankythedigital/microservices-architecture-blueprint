
package com.example.asset.controller;

import com.example.asset.dto.BulkOutletRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.OutletDto;
import com.example.asset.dto.OutletRequest;
import com.example.asset.entity.PurchaseOutlet;
import com.example.asset.mapper.OutletMapper;
import com.example.asset.service.ExcelParsingService;
import com.example.asset.service.OutletService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ✅ OutletController
 * Handles REST endpoints for PurchaseOutlet CRUD operations.
 * Token is validated from @RequestHeader Authorization.
 */
@RestController
@RequestMapping("/api/asset/v1/outlets")
public class OutletController {

    private static final Logger log = LoggerFactory.getLogger(OutletController.class);
    private final OutletService outletService;
    private final ExcelParsingService excelParsingService;

    public OutletController(OutletService outletService, ExcelParsingService excelParsingService) {
        this.outletService = outletService;
        this.excelParsingService = excelParsingService;
    }

    // ============================================================
    // 🟢 CREATE OUTLET
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<OutletDto>> create(@RequestHeader HttpHeaders headers,
                                                                  @RequestBody OutletRequest request) {
        try {
            PurchaseOutlet created = outletService.create(headers, request);
            // Convert entity to DTO to include all optional fields in JSON response
            OutletDto result = OutletMapper.toDto(created);
            log.info("✅ Outlet created successfully: {}", created.getOutletName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Outlet created successfully", result));
        } catch (Exception e) {
            log.error("❌ Failed to create outlet: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE OUTLET
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<PurchaseOutlet>> update(@RequestHeader HttpHeaders headers,
                                                                  @PathVariable Long id,
                                                                  @RequestBody OutletRequest request) {
        try {
            PurchaseOutlet updated = outletService.update(headers, id, request);
            log.info("✏️ Outlet updated successfully: {}", updated.getOutletName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Outlet updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update outlet: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ DELETE OUTLET (SOFT DELETE)
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(@RequestHeader HttpHeaders headers,
                                                        @PathVariable Long id,
                                                        @RequestBody OutletRequest request) {
        try {
            outletService.softDelete(headers, id, request);
            log.info("🗑️ Outlet deleted successfully: {}", id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Outlet deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete outlet: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST OUTLETS
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<PurchaseOutlet>>> list() {
        List<PurchaseOutlet> outlets = outletService.list();
        return ResponseEntity.ok(new ResponseWrapper<>(true, "Fetched all outlets successfully", outlets));
    }

    // ============================================================
    // 🔍 FIND OUTLET BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<PurchaseOutlet>> find(@PathVariable Long id) {
        return outletService.find(id)
                .map(outlet -> ResponseEntity.ok(new ResponseWrapper<>(true, "Outlet found successfully", outlet)))
                .orElse(ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "Outlet not found", null)));
    }

    // ============================================================
    // 📦 BULK UPLOAD OUTLETS
    // ============================================================
    @PostMapping("/bulk")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<OutletDto>>> bulkCreate(
            @RequestHeader HttpHeaders headers,
            @RequestBody BulkOutletRequest request) {
        try {
            if (request.getOutlets() == null || request.getOutlets().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Outlet list cannot be empty", null));
            }
    
            BulkUploadResponse<OutletDto> result =
                    outletService.bulkCreate(headers, request);
    
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
    // 📊 EXCEL BULK UPLOAD OUTLETS
    // ============================================================
    @PostMapping("/bulk/excel")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<OutletDto>>> bulkUploadFromExcel(
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
    
            List<BulkOutletRequest.SimpleOutletDto> rows =
                    excelParsingService.parseOutletsSimple(file);
    
            if (rows.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "No valid outlet data in Excel", null));
            }
    
            BulkOutletRequest request = new BulkOutletRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setOutlets(rows);
    
            BulkUploadResponse<OutletDto> result =
                    outletService.bulkCreate(headers, request);
    
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
}

