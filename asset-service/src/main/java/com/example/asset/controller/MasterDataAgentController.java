package com.example.asset.controller;

import com.example.asset.dto.MasterDataAgentRequest;
import com.example.asset.entity.*;
import com.example.asset.service.MasterDataAgentService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ✅ MasterDataAgentController
 * REST endpoints for master data management.
 */
@RestController
@RequestMapping("/api/asset/v1/masters")
public class MasterDataAgentController {

    private static final Logger log = LoggerFactory.getLogger(MasterDataAgentController.class);
    private final MasterDataAgentService masterService;

    public MasterDataAgentController(MasterDataAgentService masterService) {
        this.masterService = masterService;
    }

    // ============================================================
    // 📋 CATEGORY OPERATIONS
    // ============================================================
    @PostMapping("/categories")
    public ResponseEntity<ResponseWrapper<ProductCategory>> createCategory(
            @RequestHeader HttpHeaders headers,
            @RequestBody MasterDataAgentRequest request) {
        try {
            String categoryName = request.getCategoryName();
            String createdBy = request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM";
            ProductCategory category = masterService.createCategory(categoryName, createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Category created successfully", category));
        } catch (Exception e) {
            log.error("❌ Failed to create category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<ResponseWrapper<ProductCategory>> updateCategory(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long categoryId,
            @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "updatedBy", defaultValue = "SYSTEM") String updatedBy) {
        try {
            ProductCategory category = masterService.updateCategory(categoryId, categoryName, updatedBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Category updated successfully", category));
        } catch (Exception e) {
            log.error("❌ Failed to update category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<ResponseWrapper<Void>> deleteCategory(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long categoryId,
            @RequestParam(value = "deletedBy", defaultValue = "SYSTEM") String deletedBy) {
        try {
            masterService.deleteCategory(categoryId, deletedBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Category deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 SUBCATEGORY OPERATIONS
    // ============================================================
    @PostMapping("/subcategories")
    public ResponseEntity<ResponseWrapper<ProductSubCategory>> createSubCategory(
            @RequestHeader HttpHeaders headers,
            @RequestBody MasterDataAgentRequest request) {
        try {
            String subCategoryName = request.getSubCategoryName();
            Long categoryId = request.getCategoryId();
            String createdBy = request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM";
            ProductSubCategory subCategory = masterService.createSubCategory(
                    subCategoryName, categoryId, createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "SubCategory created successfully", subCategory));
        } catch (Exception e) {
            log.error("❌ Failed to create subcategory: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/subcategories/{subCategoryId}")
    public ResponseEntity<ResponseWrapper<Void>> deleteSubCategory(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long subCategoryId,
            @RequestParam(value = "deletedBy", defaultValue = "SYSTEM") String deletedBy) {
        try {
            masterService.deleteSubCategory(subCategoryId, deletedBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "SubCategory deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete subcategory: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 MAKE OPERATIONS
    // ============================================================
    @PostMapping("/makes")
    public ResponseEntity<ResponseWrapper<ProductMake>> createMake(
            @RequestHeader HttpHeaders headers,
            @RequestBody MasterDataAgentRequest request) {
        try {
            String makeName = request.getMakeName();
            Long subCategoryId = request.getSubCategoryId();
            String createdBy = request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM";
            ProductMake make = masterService.createMake(makeName, subCategoryId, createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Make created successfully", make));
        } catch (Exception e) {
            log.error("❌ Failed to create make: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/makes/{makeId}")
    public ResponseEntity<ResponseWrapper<Void>> deleteMake(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long makeId,
            @RequestParam(value = "deletedBy", defaultValue = "SYSTEM") String deletedBy) {
        try {
            masterService.deleteMake(makeId, deletedBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Make deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete make: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 MODEL OPERATIONS
    // ============================================================
    @PostMapping("/models")
    public ResponseEntity<ResponseWrapper<ProductModel>> createModel(
            @RequestHeader HttpHeaders headers,
            @RequestBody MasterDataAgentRequest request) {
        try {
            String modelName = request.getModelName();
            Long makeId = request.getMakeId();
            String createdBy = request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM";
            ProductModel model = masterService.createModel(modelName, makeId, createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Model created successfully", model));
        } catch (Exception e) {
            log.error("❌ Failed to create model: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/models/{modelId}")
    public ResponseEntity<ResponseWrapper<Void>> deleteModel(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long modelId,
            @RequestParam(value = "deletedBy", defaultValue = "SYSTEM") String deletedBy) {
        try {
            masterService.deleteModel(modelId, deletedBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Model deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to delete model: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 VENDOR OPERATIONS
    // ============================================================
    @PostMapping("/vendors")
    public ResponseEntity<ResponseWrapper<VendorMaster>> createVendor(
            @RequestHeader HttpHeaders headers,
            @RequestBody MasterDataAgentRequest request) {
        try {
            String vendorName = request.getVendorName();
            String createdBy = request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM";
            VendorMaster vendor = masterService.createVendor(vendorName, createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Vendor created successfully", vendor));
        } catch (Exception e) {
            log.error("❌ Failed to create vendor: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 OUTLET OPERATIONS
    // ============================================================
    @PostMapping("/outlets")
    public ResponseEntity<ResponseWrapper<PurchaseOutlet>> createOutlet(
            @RequestHeader HttpHeaders headers,
            @RequestBody MasterDataAgentRequest request) {
        try {
            String outletName = request.getOutletName();
            String createdBy = request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM";
            PurchaseOutlet outlet = masterService.createOutlet(outletName, createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Outlet created successfully", outlet));
        } catch (Exception e) {
            log.error("❌ Failed to create outlet: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 COMPONENT OPERATIONS
    // ============================================================
    @PostMapping("/components")
    public ResponseEntity<ResponseWrapper<AssetComponent>> createComponent(
            @RequestHeader HttpHeaders headers,
            @RequestParam("componentName") String componentName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "createdBy", defaultValue = "SYSTEM") String createdBy) {
        try {
            AssetComponent component = masterService.createComponent(
                    componentName, description, createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Component created successfully", component));
        } catch (Exception e) {
            log.error("❌ Failed to create component: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 BULK OPERATIONS
    // ============================================================
    @PostMapping("/categories/bulk")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> bulkCreateCategories(
            @RequestHeader HttpHeaders headers,
            @RequestBody List<String> categoryNames,
            @RequestParam(value = "createdBy", defaultValue = "SYSTEM") String createdBy) {
        try {
            Map<String, Object> result = masterService.bulkCreateCategories(categoryNames, createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Bulk category creation completed", result));
        } catch (Exception e) {
            log.error("❌ Failed to bulk create categories: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 VALIDATION & SUMMARY
    // ============================================================
    @GetMapping("/validate/category/{categoryId}")
    public ResponseEntity<ResponseWrapper<Boolean>> validateCategory(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long categoryId) {
        try {
            boolean exists = masterService.validateCategoryExists(categoryId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Validation completed", exists));
        } catch (Exception e) {
            log.error("❌ Validation failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getMasterDataSummary(
            @RequestHeader HttpHeaders headers) {
        try {
            Map<String, Object> summary = masterService.getMasterDataSummary();
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true, "Master data summary retrieved", summary));
        } catch (Exception e) {
            log.error("❌ Failed to get summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }
}
