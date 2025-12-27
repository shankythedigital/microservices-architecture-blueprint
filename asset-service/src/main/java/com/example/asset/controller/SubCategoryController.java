
package com.example.asset.controller;

import com.example.asset.dto.BulkSubCategoryRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.SubCategoryRequest;
import com.example.asset.entity.ProductSubCategory;
import com.example.asset.service.ExcelParsingService;
import com.example.asset.service.SubCategoryService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.asset.dto.ProductSubCategoryDto;
import com.example.asset.mapper.ProductSubCategoryMapper;

import java.util.List;

/**
 * ✅ SubCategoryController
 * Handles CRUD operations for subcategories using token-secured notifications.
 * Token is extracted from Authorization header via @RequestHeader HttpHeaders.
 */
@RestController
@RequestMapping("/api/asset/v1/subcategories")
public class SubCategoryController {

    private static final Logger log = LoggerFactory.getLogger(SubCategoryController.class);

    private final SubCategoryService service;
    private final ExcelParsingService excelParsingService;

    public SubCategoryController(SubCategoryService service, ExcelParsingService excelParsingService) {
        this.service = service;
        this.excelParsingService = excelParsingService;
    }

    // ============================================================
    // 🟢 CREATE SUBCATEGORY
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<ProductSubCategory>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody SubCategoryRequest request) {
        try {
            ProductSubCategory created = service.create(headers, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Subcategory created successfully", created));
        } catch (Exception e) {
            log.error("❌ Failed to create subcategory: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE SUBCATEGORY
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductSubCategory>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody SubCategoryRequest request) {
        try {
            ProductSubCategory updated = service.update(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Subcategory updated successfully", updated));
        } catch (Exception e) {
            log.error("❌ Failed to update subcategory: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE SUBCATEGORY
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody SubCategoryRequest request) {
        try {
            service.softDelete(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Subcategory deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete subcategory: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST SUBCATEGORIES
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ProductSubCategory>>> list() {
        List<ProductSubCategory> subCategories = service.list();
        return ResponseEntity.ok(new ResponseWrapper<>(true, "📋 Subcategories fetched successfully", subCategories));
    }

    // ============================================================
    // 🔍 GET SUBCATEGORY BY ID
    // ============================================================
    
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductSubCategoryDto>> getById(@PathVariable Long id) {
        return service.find(id)
                .map(sub -> {
                    ProductSubCategoryDto dto = ProductSubCategoryMapper.toDto(sub);
                    return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Subcategory found", dto));
                })
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "❌ Subcategory not found", null)));
    }

    // ============================================================
    // 📦 BULK UPLOAD SUBCATEGORIES
    // ============================================================
    @PostMapping("/bulk")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<ProductSubCategoryDto>>> bulkCreate(
            @RequestHeader HttpHeaders headers,
            @RequestBody BulkSubCategoryRequest request) {
        try {
            if (request.getSubCategories() == null || request.getSubCategories().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "SubCategory list cannot be empty", null));
            }
    
            BulkUploadResponse<ProductSubCategoryDto> result =
                    service.bulkCreate(headers, request);
    
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
    // 📊 EXCEL BULK UPLOAD SUBCATEGORIES
    // ============================================================
    @PostMapping("/bulk/excel")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<ProductSubCategoryDto>>> bulkUploadFromExcel(
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
    
            List<BulkSubCategoryRequest.SimpleSubCategoryDto> rows =
                    excelParsingService.parseSubCategoriesSimple(file);
    
            if (rows.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "No valid subcategory data in Excel", null));
            }
    
            BulkSubCategoryRequest request = new BulkSubCategoryRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setSubCategories(rows);
    
            BulkUploadResponse<ProductSubCategoryDto> result =
                    service.bulkCreate(headers, request);
    
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



