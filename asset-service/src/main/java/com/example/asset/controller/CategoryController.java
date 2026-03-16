
package com.example.asset.controller;

import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.CategoryClassifyResponse;
import com.example.asset.dto.CategoryDto;
import com.example.asset.dto.CategoryRequest;
import com.example.asset.dto.BulkCategoryRequest;
import com.example.asset.service.CategoryClassificationService;
import com.example.asset.service.CategoryService;
import com.example.asset.util.ByteArrayMultipartFile;
import com.example.asset.service.DocumentTypeMasterService;
import com.example.asset.service.ExcelParsingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;
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
    private final DocumentTypeMasterService documentTypeMasterService;
    private final ExcelParsingService excelParsingService;
    private final CategoryClassificationService categoryClassificationService;

    public CategoryController(CategoryService service, DocumentTypeMasterService documentTypeMasterService,
                             ExcelParsingService excelParsingService,
                             CategoryClassificationService categoryClassificationService) {
        this.service = service;
        this.documentTypeMasterService = documentTypeMasterService;
        this.excelParsingService = excelParsingService;
        this.categoryClassificationService = categoryClassificationService;
    }

    // ============================================================
    // 🟢 CREATE CATEGORY (JSON body - document via Document API separately)
    // ============================================================
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ResponseWrapper<CategoryDto>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody CategoryRequest request) {
        try {
            CategoryDto created = service.create(headers, request, null, null, false);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✅ Category created successfully", created)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to create category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🟢 CREATE CATEGORY WITH DOCUMENT (raw JSON: request, document base64, docType)
    // ============================================================
    @PostMapping(path = "/with-document", consumes = "application/json")
    public ResponseEntity<ResponseWrapper<CategoryDto>> createWithDocument(
            @RequestHeader HttpHeaders headers,
            @RequestBody Map<String, Object> body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CategoryRequest request = mapper.convertValue(body.get("request"), CategoryRequest.class);
            String document = (String) body.get("document");
            String docType = (String) body.get("docType");
            if (document == null || document.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ document is required (base64)", null));
            if (docType == null || docType.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ docType is required", null));
            documentTypeMasterService.validate(docType);
            byte[] bytes = Base64.getDecoder().decode(document);
            MultipartFile multipartFile = new ByteArrayMultipartFile(bytes, "document", "document." + docType);
            CategoryDto created = service.create(headers, request, multipartFile, docType.trim(), false);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Category created successfully with document", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to create category with document: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
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
    // ✏️ UPDATE CATEGORY WITH DOCUMENT (raw JSON: request, document base64, docType)
    // ============================================================
    @PutMapping(path = "/{id}/with-document", consumes = "application/json")
    public ResponseEntity<ResponseWrapper<CategoryDto>> updateWithDocument(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CategoryRequest request = mapper.convertValue(body.get("request"), CategoryRequest.class);
            String document = (String) body.get("document");
            String docType = (String) body.get("docType");
            if (document == null || document.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ document is required (base64)", null));
            if (docType == null || docType.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ docType is required", null));
            documentTypeMasterService.validate(docType);
            byte[] bytes = Base64.getDecoder().decode(document);
            MultipartFile multipartFile = new ByteArrayMultipartFile(bytes, "document", "document." + docType);
            CategoryDto updated = service.updateWithDocument(headers, id, request, multipartFile, docType.trim());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Category updated successfully with document", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to update category with document: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
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
    // 📷 CLASSIFY CATEGORY FROM PHOTO/SCAN IMAGE
    // ============================================================
    /**
     * Upload a photo or scanned image from mobile (multipart) to get suggested category and subcategory.
     * Uses barcode lookup (if barcode present), OCR text extraction, and AI classification.
     * Supports: JPEG, PNG, GIF, BMP, TIFF.
     */
    @PostMapping(value = "/classify-image", consumes = "multipart/form-data")
    public ResponseEntity<ResponseWrapper<CategoryClassifyResponse>> classifyFromImage(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ Image file cannot be empty", null));
            }
            CategoryClassifyResponse result = categoryClassificationService.classifyFromImage(file);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✅ Category classified from image", result)
            );
        } catch (Exception e) {
            log.error("❌ Failed to classify category from image: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Classify category from photo/scan sent as base64 (JSON body).
     * Mobile-friendly alternative when multipart is not convenient.
     * Body: { "document": "base64EncodedImage", "contentType": "image/jpeg" }
     */
    @PostMapping(value = "/classify-image", consumes = "application/json")
    public ResponseEntity<ResponseWrapper<CategoryClassifyResponse>> classifyFromImageBase64(
            @RequestBody Map<String, Object> body) {
        try {
            String document = (String) body.get("document");
            String contentType = body.get("contentType") != null ? body.get("contentType").toString() : "image/jpeg";
            if (document == null || document.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ document (base64) is required", null));
            }
            byte[] bytes = Base64.getDecoder().decode(document);
            String ext = contentType.contains("png") ? "png" : contentType.contains("gif") ? "gif" : "jpg";
            MultipartFile multipartFile = new ByteArrayMultipartFile(bytes, "document", "image." + ext);
            CategoryClassifyResponse result = categoryClassificationService.classifyFromImage(multipartFile);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "✅ Category classified from image", result)
            );
        } catch (Exception e) {
            log.error("❌ Failed to classify category from image (base64): {}", e.getMessage(), e);
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

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for a category (accessible to all authenticated users)
     * PUT /api/asset/v1/categories/{id}/favourite
     */
    @PutMapping("/{id}/favourite")
    public ResponseEntity<ResponseWrapper<CategoryDto>> updateFavourite(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isFavourite", defaultValue = "true") Boolean isFavourite) {
        try {
            CategoryDto updated = service.updateFavourite(headers, id, isFavourite);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Category favourite updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update category favourite: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Toggle most like status for a category (accessible to all authenticated users)
     * PUT /api/asset/v1/categories/{id}/most-like
     */
    @PutMapping("/{id}/most-like")
    public ResponseEntity<ResponseWrapper<CategoryDto>> updateMostLike(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isMostLike", defaultValue = "true") Boolean isMostLike) {
        try {
            CategoryDto updated = service.updateMostLike(headers, id, isMostLike);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Category most like updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update category most like: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Update sequence order for a category (admin only)
     * PUT /api/asset/v1/categories/{id}/sequence-order
     */
    @PutMapping("/{id}/sequence-order")
    public ResponseEntity<ResponseWrapper<CategoryDto>> updateSequenceOrder(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam("sequenceOrder") Integer sequenceOrder) {
        try {
            CategoryDto updated = service.updateSequenceOrder(headers, id, sequenceOrder);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📊 Category sequence order updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update category sequence order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }
}


