
package com.example.asset.controller;

import com.example.asset.dto.BulkSubCategoryRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.SubCategoryRequest;
import com.example.asset.util.ByteArrayMultipartFile;
import com.example.asset.service.DocumentTypeMasterService;
import com.example.common.jackson.JacksonObjectMappers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;
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
    private final DocumentTypeMasterService documentTypeMasterService;
    private final ExcelParsingService excelParsingService;

    public SubCategoryController(SubCategoryService service, DocumentTypeMasterService documentTypeMasterService, ExcelParsingService excelParsingService) {
        this.service = service;
        this.documentTypeMasterService = documentTypeMasterService;
        this.excelParsingService = excelParsingService;
    }

    // ============================================================
    // 🟢 CREATE SUBCATEGORY (JSON body - document via Document API separately)
    // ============================================================
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ResponseWrapper<ProductSubCategory>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody SubCategoryRequest request) {
        try {
            ProductSubCategory created = service.create(headers, request, null, null, false);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Subcategory created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to create subcategory: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    @PostMapping(path = "/with-document", consumes = "application/json")
    public ResponseEntity<ResponseWrapper<ProductSubCategory>> createWithDocument(
            @RequestHeader HttpHeaders headers,
            @RequestBody Map<String, Object> body) {
        try {
            ObjectMapper mapper = JacksonObjectMappers.standard();
            SubCategoryRequest request = mapper.convertValue(body.get("request"), SubCategoryRequest.class);
            String document = (String) body.get("document");
            String docType = (String) body.get("docType");
            if (document == null || document.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ document is required (base64)", null));
            if (docType == null || docType.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ docType is required", null));
            documentTypeMasterService.validate(docType);
            byte[] bytes = Base64.getDecoder().decode(document);
            MultipartFile multipartFile = new ByteArrayMultipartFile(bytes, "document", "document." + docType);
            ProductSubCategory created = service.create(headers, request, multipartFile, docType.trim(), false);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Subcategory created successfully with document", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to create subcategory with document: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
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

    @PutMapping(path = "/{id}/with-document", consumes = "application/json")
    public ResponseEntity<ResponseWrapper<ProductSubCategory>> updateWithDocument(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            ObjectMapper mapper = JacksonObjectMappers.standard();
            SubCategoryRequest request = mapper.convertValue(body.get("request"), SubCategoryRequest.class);
            String document = (String) body.get("document");
            String docType = (String) body.get("docType");
            if (document == null || document.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ document is required (base64)", null));
            if (docType == null || docType.isBlank())
                return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ docType is required", null));
            documentTypeMasterService.validate(docType);
            byte[] bytes = Base64.getDecoder().decode(document);
            MultipartFile multipartFile = new ByteArrayMultipartFile(bytes, "document", "document." + docType);
            ProductSubCategory updated = service.updateWithDocument(headers, id, request, multipartFile, docType.trim());
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✏️ Subcategory updated successfully with document", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to update subcategory with document: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
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

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for a subcategory (accessible to all authenticated users)
     * PUT /api/asset/v1/subcategories/{id}/favourite
     */
    @PutMapping("/{id}/favourite")
    public ResponseEntity<ResponseWrapper<ProductSubCategoryDto>> updateFavourite(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isFavourite", defaultValue = "true") Boolean isFavourite) {
        try {
            ProductSubCategoryDto updated = service.updateFavourite(headers, id, isFavourite);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Subcategory favourite updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update subcategory favourite: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Toggle most like status for a subcategory (accessible to all authenticated users)
     * PUT /api/asset/v1/subcategories/{id}/most-like
     */
    @PutMapping("/{id}/most-like")
    public ResponseEntity<ResponseWrapper<ProductSubCategoryDto>> updateMostLike(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam(value = "isMostLike", defaultValue = "true") Boolean isMostLike) {
        try {
            ProductSubCategoryDto updated = service.updateMostLike(headers, id, isMostLike);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "⭐ Subcategory most like updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update subcategory most like: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }

    /**
     * Update sequence order for a subcategory (admin only)
     * PUT /api/asset/v1/subcategories/{id}/sequence-order
     */
    @PutMapping("/{id}/sequence-order")
    public ResponseEntity<ResponseWrapper<ProductSubCategoryDto>> updateSequenceOrder(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestParam("sequenceOrder") Integer sequenceOrder) {
        try {
            ProductSubCategoryDto updated = service.updateSequenceOrder(headers, id, sequenceOrder);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "📊 Subcategory sequence order updated successfully", updated)
            );
        } catch (Exception e) {
            log.error("❌ Failed to update subcategory sequence order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Error: " + e.getMessage(), null));
        }
    }
}



