package com.example.asset.service;

import com.example.asset.entity.*;
import com.example.asset.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * ✅ MasterDataAgentService
 * Comprehensive agent for managing all master data entities.
 * Handles CRUD operations, validation, relationships, and edge cases.
 */
@Service
public class MasterDataAgentService {

    private static final Logger log = LoggerFactory.getLogger(MasterDataAgentService.class);

    // Repositories
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final VendorRepository vendorRepo;
    private final PurchaseOutletRepository outletRepo;
    private final AssetComponentRepository componentRepo;
    private final AssetMasterRepository assetRepo;

    public MasterDataAgentService(
            ProductCategoryRepository categoryRepo,
            ProductSubCategoryRepository subCategoryRepo,
            ProductMakeRepository makeRepo,
            ProductModelRepository modelRepo,
            VendorRepository vendorRepo,
            PurchaseOutletRepository outletRepo,
            AssetComponentRepository componentRepo,
            AssetMasterRepository assetRepo) {
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.vendorRepo = vendorRepo;
        this.outletRepo = outletRepo;
        this.componentRepo = componentRepo;
        this.assetRepo = assetRepo;
    }

    // ============================================================
    // 📋 CATEGORY OPERATIONS
    // ============================================================
    @Transactional
    public ProductCategory createCategory(String categoryName, String createdBy) {
        // Edge case: Null/empty name
        if (!StringUtils.hasText(categoryName)) {
            throw new IllegalArgumentException("❌ Category name cannot be null or empty");
        }

        // Edge case: Duplicate name (case-insensitive)
        if (categoryRepo.existsByCategoryNameIgnoreCase(categoryName.trim())) {
            throw new IllegalArgumentException("❌ Category with name '" + categoryName + "' already exists");
        }

        ProductCategory category = new ProductCategory();
        category.setCategoryName(categoryName.trim());
        category.setCreatedBy(createdBy);
        category.setUpdatedBy(createdBy);
        category.setActive(true);

        ProductCategory saved = categoryRepo.save(category);
        log.info("✅ Category created: {} (ID: {})", saved.getCategoryName(), saved.getCategoryId());
        return saved;
    }

    @Transactional
    public ProductCategory updateCategory(Long categoryId, String newName, String updatedBy) {
        // Edge case: Category not found
        ProductCategory category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Category not found: " + categoryId));

        // Edge case: Null/empty name
        if (!StringUtils.hasText(newName)) {
            throw new IllegalArgumentException("❌ Category name cannot be null or empty");
        }

        // Edge case: Duplicate name (excluding current category)
        String trimmedName = newName.trim();
        if (!category.getCategoryName().equalsIgnoreCase(trimmedName) &&
            categoryRepo.existsByCategoryNameIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("❌ Category with name '" + trimmedName + "' already exists");
        }

        category.setCategoryName(trimmedName);
        category.setUpdatedBy(updatedBy);
        ProductCategory saved = categoryRepo.save(category);
        log.info("✅ Category updated: {} (ID: {})", saved.getCategoryName(), saved.getCategoryId());
        return saved;
    }

    @Transactional
    public void deleteCategory(Long categoryId, String deletedBy) {
        // Edge case: Category not found
        ProductCategory category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Category not found: " + categoryId));

        // Edge case: Category has subcategories
        List<ProductSubCategory> subCategories = subCategoryRepo.findByCategory_CategoryId(categoryId);
        if (!subCategories.isEmpty()) {
            throw new IllegalStateException("❌ Cannot delete category: " + subCategories.size() + 
                    " subcategories are associated with it");
        }

        // Soft delete
        category.setActive(false);
        category.setUpdatedBy(deletedBy);
        categoryRepo.save(category);
        log.info("✅ Category deleted: {} (ID: {})", category.getCategoryName(), categoryId);
    }

    // ============================================================
    // 📋 SUBCATEGORY OPERATIONS
    // ============================================================
    @Transactional
    public ProductSubCategory createSubCategory(String subCategoryName, Long categoryId, String createdBy) {
        // Edge case: Null/empty name
        if (!StringUtils.hasText(subCategoryName)) {
            throw new IllegalArgumentException("❌ SubCategory name cannot be null or empty");
        }

        // Edge case: Category not found
        ProductCategory category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Category not found: " + categoryId));

        // Edge case: Duplicate name within same category
        if (subCategoryRepo.existsBySubCategoryNameIgnoreCaseAndCategory_CategoryId(
                subCategoryName.trim(), categoryId)) {
            throw new IllegalArgumentException("❌ SubCategory with name '" + subCategoryName + 
                    "' already exists in this category");
        }

        ProductSubCategory subCategory = new ProductSubCategory();
        subCategory.setSubCategoryName(subCategoryName.trim());
        subCategory.setCategory(category);
        subCategory.setCreatedBy(createdBy);
        subCategory.setUpdatedBy(createdBy);
        subCategory.setActive(true);

        ProductSubCategory saved = subCategoryRepo.save(subCategory);
        log.info("✅ SubCategory created: {} (ID: {})", saved.getSubCategoryName(), saved.getSubCategoryId());
        return saved;
    }

    @Transactional
    public void deleteSubCategory(Long subCategoryId, String deletedBy) {
        // Edge case: SubCategory not found
        ProductSubCategory subCategory = subCategoryRepo.findById(subCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("❌ SubCategory not found: " + subCategoryId));

        // Edge case: SubCategory has makes
        List<ProductMake> makes = makeRepo.findBySubCategory_SubCategoryId(subCategoryId);
        if (!makes.isEmpty()) {
            throw new IllegalStateException("❌ Cannot delete subcategory: " + makes.size() + 
                    " makes are associated with it");
        }

        // Soft delete
        subCategory.setActive(false);
        subCategory.setUpdatedBy(deletedBy);
        subCategoryRepo.save(subCategory);
        log.info("✅ SubCategory deleted: {} (ID: {})", subCategory.getSubCategoryName(), subCategoryId);
    }

    // ============================================================
    // 📋 MAKE OPERATIONS
    // ============================================================
    @Transactional
    public ProductMake createMake(String makeName, Long subCategoryId, String createdBy) {
        // Edge case: Null/empty name
        if (!StringUtils.hasText(makeName)) {
            throw new IllegalArgumentException("❌ Make name cannot be null or empty");
        }

        // Edge case: SubCategory not found
        ProductSubCategory subCategory = subCategoryRepo.findById(subCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("❌ SubCategory not found: " + subCategoryId));

        // Edge case: Duplicate make within same subcategory
        if (makeRepo.findByMakeNameIgnoreCaseAndSubCategory(makeName.trim(), subCategory).isPresent()) {
            throw new IllegalArgumentException("❌ Make with name '" + makeName + 
                    "' already exists in this subcategory");
        }

        ProductMake make = new ProductMake();
        make.setMakeName(makeName.trim());
        make.setSubCategory(subCategory);
        make.setCreatedBy(createdBy);
        make.setUpdatedBy(createdBy);
        make.setActive(true);

        ProductMake saved = makeRepo.save(make);
        log.info("✅ Make created: {} (ID: {})", saved.getMakeName(), saved.getMakeId());
        return saved;
    }

    @Transactional
    public void deleteMake(Long makeId, String deletedBy) {
        // Edge case: Make not found
        ProductMake make = makeRepo.findById(makeId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Make not found: " + makeId));

        // Edge case: Make has models
        List<ProductModel> models = modelRepo.findByMake_MakeId(makeId);
        if (!models.isEmpty()) {
            throw new IllegalStateException("❌ Cannot delete make: " + models.size() + 
                    " models are associated with it");
        }

        // Soft delete
        make.setActive(false);
        make.setUpdatedBy(deletedBy);
        makeRepo.save(make);
        log.info("✅ Make deleted: {} (ID: {})", make.getMakeName(), makeId);
    }

    // ============================================================
    // 📋 MODEL OPERATIONS
    // ============================================================
    @Transactional
    public ProductModel createModel(String modelName, Long makeId, String createdBy) {
        // Edge case: Null/empty name
        if (!StringUtils.hasText(modelName)) {
            throw new IllegalArgumentException("❌ Model name cannot be null or empty");
        }

        // Edge case: Make not found
        ProductMake make = makeRepo.findById(makeId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Make not found: " + makeId));

        // Edge case: Duplicate model within same make
        Optional<ProductModel> existingModel = modelRepo.findByModelNameIgnoreCaseAndMake_MakeId(modelName.trim(), makeId);
        if (existingModel.isPresent()) {
            throw new IllegalArgumentException("❌ Model with name '" + modelName + 
                    "' already exists for this make");
        }

        ProductModel model = new ProductModel();
        model.setModelName(modelName.trim());
        model.setMake(make);
        model.setCreatedBy(createdBy);
        model.setUpdatedBy(createdBy);
        model.setActive(true);

        ProductModel saved = modelRepo.save(model);
        log.info("✅ Model created: {} (ID: {})", saved.getModelName(), saved.getModelId());
        return saved;
    }

    @Transactional
    public void deleteModel(Long modelId, String deletedBy) {
        // Edge case: Model not found
        ProductModel model = modelRepo.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Model not found: " + modelId));

        // Edge case: Model has assets
        List<AssetMaster> assets = assetRepo.findByModel_ModelId(modelId);
        if (!assets.isEmpty()) {
            throw new IllegalStateException("❌ Cannot delete model: " + assets.size() + 
                    " assets are using this model");
        }

        // Soft delete
        model.setActive(false);
        model.setUpdatedBy(deletedBy);
        modelRepo.save(model);
        log.info("✅ Model deleted: {} (ID: {})", model.getModelName(), modelId);
    }

    // ============================================================
    // 📋 VENDOR OPERATIONS
    // ============================================================
    @Transactional
    public VendorMaster createVendor(String vendorName, String createdBy) {
        // Edge case: Null/empty name
        if (!StringUtils.hasText(vendorName)) {
            throw new IllegalArgumentException("❌ Vendor name cannot be null or empty");
        }

        // Edge case: Duplicate vendor name
        if (vendorRepo.existsByVendorNameIgnoreCase(vendorName.trim())) {
            throw new IllegalArgumentException("❌ Vendor with name '" + vendorName + "' already exists");
        }

        VendorMaster vendor = new VendorMaster();
        vendor.setVendorName(vendorName.trim());
        vendor.setCreatedBy(createdBy);
        vendor.setUpdatedBy(createdBy);
        vendor.setActive(true);

        VendorMaster saved = vendorRepo.save(vendor);
        log.info("✅ Vendor created: {} (ID: {})", saved.getVendorName(), saved.getVendorId());
        return saved;
    }

    // ============================================================
    // 📋 OUTLET OPERATIONS
    // ============================================================
    @Transactional
    public PurchaseOutlet createOutlet(String outletName, String createdBy) {
        // Edge case: Null/empty name
        if (!StringUtils.hasText(outletName)) {
            throw new IllegalArgumentException("❌ Outlet name cannot be null or empty");
        }

        // Edge case: Duplicate outlet name
        if (outletRepo.existsByOutletName(outletName.trim())) {
            throw new IllegalArgumentException("❌ Outlet with name '" + outletName + "' already exists");
        }

        PurchaseOutlet outlet = new PurchaseOutlet();
        outlet.setOutletName(outletName.trim());
        outlet.setCreatedBy(createdBy);
        outlet.setUpdatedBy(createdBy);
        outlet.setActive(true);

        PurchaseOutlet saved = outletRepo.save(outlet);
        log.info("✅ Outlet created: {} (ID: {})", saved.getOutletName(), saved.getOutletId());
        return saved;
    }

    // ============================================================
    // 📋 COMPONENT OPERATIONS
    // ============================================================
    @Transactional
    public AssetComponent createComponent(String componentName, String description, String createdBy) {
        // Edge case: Null/empty name
        if (!StringUtils.hasText(componentName)) {
            throw new IllegalArgumentException("❌ Component name cannot be null or empty");
        }

        AssetComponent component = new AssetComponent();
        component.setComponentName(componentName.trim());
        component.setDescription(description != null ? description.trim() : null);
        component.setCreatedBy(createdBy);
        component.setUpdatedBy(createdBy);
        component.setActive(true);

        AssetComponent saved = componentRepo.save(component);
        log.info("✅ Component created: {} (ID: {})", saved.getComponentName(), saved.getComponentId());
        return saved;
    }

    // ============================================================
    // 📋 BULK OPERATIONS
    // ============================================================
    @Transactional
    public Map<String, Object> bulkCreateCategories(List<String> categoryNames, String createdBy) {
        Map<String, Object> result = new HashMap<>();
        List<ProductCategory> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < categoryNames.size(); i++) {
            try {
                ProductCategory category = createCategory(categoryNames.get(i), createdBy);
                created.add(category);
            } catch (Exception e) {
                errors.add("Index " + i + " (" + categoryNames.get(i) + "): " + e.getMessage());
                log.warn("⚠️ Failed to create category at index {}: {}", i, e.getMessage());
            }
        }

        result.put("total", categoryNames.size());
        result.put("created", created.size());
        result.put("failed", errors.size());
        result.put("categories", created);
        result.put("errors", errors);

        log.info("📦 Bulk category creation: {}/{} successful", created.size(), categoryNames.size());
        return result;
    }

    // ============================================================
    // 📋 VALIDATION & RELATIONSHIP CHECKS
    // ============================================================
    public boolean validateCategoryExists(Long categoryId) {
        return categoryRepo.findById(categoryId)
                .map(c -> c.getActive() != null && c.getActive())
                .orElse(false);
    }

    public boolean validateSubCategoryExists(Long subCategoryId) {
        return subCategoryRepo.findById(subCategoryId)
                .map(sc -> sc.getActive() != null && sc.getActive())
                .orElse(false);
    }

    public boolean validateMakeExists(Long makeId) {
        return makeRepo.findById(makeId)
                .map(m -> m.getActive() != null && m.getActive())
                .orElse(false);
    }

    public boolean validateModelExists(Long modelId) {
        return modelRepo.findById(modelId)
                .map(m -> m.getActive() != null && m.getActive())
                .orElse(false);
    }

    public Map<String, Object> getMasterDataSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("categories", categoryRepo.count());
        summary.put("subCategories", subCategoryRepo.count());
        summary.put("makes", makeRepo.count());
        summary.put("models", modelRepo.count());
        summary.put("vendors", vendorRepo.count());
        summary.put("outlets", outletRepo.count());
        summary.put("components", componentRepo.count());
        return summary;
    }
}

