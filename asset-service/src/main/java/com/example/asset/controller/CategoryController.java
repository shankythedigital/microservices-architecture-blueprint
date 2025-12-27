
package com.example.asset.controller;

import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.CategoryDto;
import com.example.asset.dto.CategoryRequest;
import com.example.asset.dto.BulkCategoryRequest;
import com.example.asset.service.CategoryService;
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
 * ✅ CategoryController
 * Handles CRUD endpoints for ProductCategory.
 * Uses DTO responses to ensure clean JSON serialization.
 */
@RestController
@RequestMapping("/api/asset/v1/categories")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService service;
    private final ExcelParsingService excelParsingService;

    public CategoryController(CategoryService service, ExcelParsingService excelParsingService) {
        this.service = service;
        this.excelParsingService = excelParsingService;
    }

    // ============================================================
    // 🟢 CREATE CATEGORY
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<CategoryDto>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody CategoryRequest request) {
        try {
            CategoryDto created = service.create(headers, request);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✅ Category created successfully", created)
            );
        } catch (Exception e) {
            log.error("❌ Failed to create category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE CATEGORY
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<CategoryDto>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        try {
            CategoryDto updated = service.update(headers, id, request);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✏️ Category updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE CATEGORY
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        try {
            service.softDelete(headers, id, request);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "🗑️ Category deleted successfully", null)
            );
        } catch (Exception e) {
            log.error("❌ Failed to delete category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST ALL CATEGORIES
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<CategoryDto>>> list() {
        try {
            List<CategoryDto> categories = service.list();
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📋 Categories fetched successfully", categories)
            );
        } catch (Exception e) {
            log.error("❌ Failed to list categories: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 GET CATEGORY BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<CategoryDto>> getById(@PathVariable Long id) {
        try {
            return service.find(id)
                    .map(dto -> ResponseEntity.ok(
                            new ResponseWrapper<>(true, "✅ Category found", dto)))
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body(new ResponseWrapper<>(false, "❌ Category not found", null)));
        } catch (Exception e) {
            log.error("❌ Failed to fetch category by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📦 BULK UPLOAD CATEGORIES
    // ============================================================
    @PostMapping("/bulk")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<CategoryDto>>> bulkCreate(
            @RequestHeader HttpHeaders headers,
            @RequestBody BulkCategoryRequest request) {
        try {
            if (request.getCategories() == null || request.getCategories().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Category list cannot be empty", null));
            }
    
            BulkUploadResponse<CategoryDto> result =
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
    // 📊 EXCEL BULK UPLOAD CATEGORIES
    // ============================================================
    @PostMapping("/bulk/excel")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<CategoryDto>>> bulkUploadFromExcel(
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
    
            List<BulkCategoryRequest.SimpleCategoryDto> rows =
                    excelParsingService.parseCategoriesSimple(file);
    
            if (rows.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "No valid category data in Excel", null));
            }
    
            BulkCategoryRequest request = new BulkCategoryRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setCategories(rows);
    
            BulkUploadResponse<CategoryDto> result =
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


