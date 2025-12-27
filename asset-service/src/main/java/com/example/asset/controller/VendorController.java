
package com.example.asset.controller;

import com.example.asset.dto.BulkVendorRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.VendorDto;
import com.example.asset.dto.VendorRequest;
import com.example.asset.entity.VendorMaster;
import com.example.asset.mapper.VendorMapper;
import com.example.asset.service.ExcelParsingService;
import com.example.asset.service.VendorService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ✅ VendorController
 * Handles vendor CRUD operations with token-secured header authentication.
 */
@RestController
@RequestMapping("/api/asset/v1/vendors")
public class VendorController {

    private static final Logger log = LoggerFactory.getLogger(VendorController.class);

    private final VendorService vendorService;
    private final ExcelParsingService excelParsingService;

    public VendorController(VendorService vendorService, ExcelParsingService excelParsingService) {
        this.vendorService = vendorService;
        this.excelParsingService = excelParsingService;
    }

    // ============================================================
    // 🟢 CREATE VENDOR
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<VendorDto>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody VendorRequest request) {
        try {
            VendorMaster created = vendorService.create(headers, request);
            // Convert entity to DTO to include all optional fields in JSON response
            VendorDto result = VendorMapper.toDto(created);
            log.info("✅ Vendor created successfully: {}", created.getVendorName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Vendor created successfully", result));
        } catch (Exception e) {
            log.error("❌ Failed to create vendor: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE VENDOR
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<VendorMaster>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody VendorRequest request) {
        try {
            VendorMaster updated = vendorService.update(headers, id, request);
            log.info("✏️ Vendor updated successfully: id={} name={}", id, updated.getVendorName());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Vendor updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update vendor: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE VENDOR
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody VendorRequest request) {
        try {
            vendorService.softDelete(headers, id, request);
            log.info("🗑️ Vendor deleted (soft): id={}", id);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Vendor deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete vendor: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST ALL VENDORS
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<VendorMaster>>> list() {
        List<VendorMaster> vendors = vendorService.list();
        return ResponseEntity.ok(new ResponseWrapper<>(true, "Fetched vendor list successfully", vendors));
    }

    // ============================================================
    // 🔍 GET BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<VendorMaster>> find(@PathVariable Long id) {
        return vendorService.find(id)
                .map(v -> ResponseEntity.ok(new ResponseWrapper<>(true, "Vendor found", v)))
                .orElse(ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "Vendor not found", null)));
    }

    // ============================================================
    // 📦 BULK UPLOAD VENDORS
    // ============================================================
    @PostMapping("/bulk")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<VendorDto>>> bulkCreate(
            @RequestHeader HttpHeaders headers,
            @RequestBody BulkVendorRequest request) {
        try {
            if (request.getVendors() == null || request.getVendors().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Vendor list cannot be empty", null));
            }
    
            BulkUploadResponse<VendorDto> result =
                    vendorService.bulkCreate(headers, request);
    
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
    // 📊 EXCEL BULK UPLOAD VENDORS
    // ============================================================
    @PostMapping("/bulk/excel")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<VendorDto>>> bulkUploadFromExcel(
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
    
            List<BulkVendorRequest.SimpleVendorDto> rows =
                    excelParsingService.parseVendorsSimple(file);
    
            if (rows.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "No valid vendor data in Excel", null));
            }
    
            BulkVendorRequest request = new BulkVendorRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setVendors(rows);
    
            BulkUploadResponse<VendorDto> result =
                    vendorService.bulkCreate(headers, request);
    
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

