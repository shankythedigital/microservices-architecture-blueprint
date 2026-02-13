package com.example.asset.service;

import com.example.asset.dto.CategoryRequest;
import com.example.asset.dto.MakeRequest;
import com.example.asset.dto.ModelRequest;
import com.example.asset.dto.ProductOcrRequest;
import com.example.asset.dto.ProductOcrResponse;
import com.example.asset.dto.SubCategoryRequest;
import com.example.asset.entity.ProductCategory;
import com.example.asset.entity.ProductMake;
import com.example.asset.entity.ProductModel;
import com.example.asset.entity.ProductSubCategory;
import com.example.asset.repository.ProductCategoryRepository;
import com.example.asset.repository.ProductMakeRepository;
import com.example.asset.repository.ProductModelRepository;
import com.example.asset.repository.ProductSubCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ✅ ProductOcrAiAgentService
 * AI Agent service that intelligently analyzes OCR-extracted text
 * and extracts product information (make, model, category, etc.)
 * to create or update product records.
 * 
 * This agent can:
 * - Parse OCR text from product images/labels
 * - Extract structured product information (make, model, category, etc.)
 * - Match against existing products or create new ones
 * - Handle fuzzy matching and disambiguation
 */
@Service
public class ProductOcrAiAgentService {

    private static final Logger log = LoggerFactory.getLogger(ProductOcrAiAgentService.class);

    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final MakeService makeService;
    private final ModelService modelService;
    private final CategoryService categoryService;
    private final SubCategoryService subCategoryService;
    private final OcrTrainingService trainingService;

    // Common product makes/brands for matching
    private static final Set<String> COMMON_MAKES = Set.of(
        "Dell", "HP", "Lenovo", "Apple", "Samsung", "Sony", "LG", "Panasonic",
        "Microsoft", "Asus", "Acer", "Toshiba", "Fujitsu", "IBM", "Cisco",
        "Nokia", "Motorola", "Xiaomi", "OnePlus", "Huawei", "Oppo", "Vivo"
    );

    public ProductOcrAiAgentService(
            ProductMakeRepository makeRepo,
            ProductModelRepository modelRepo,
            ProductCategoryRepository categoryRepo,
            ProductSubCategoryRepository subCategoryRepo,
            MakeService makeService,
            ModelService modelService,
            CategoryService categoryService,
            SubCategoryService subCategoryService,
            OcrTrainingService trainingService) {
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.makeService = makeService;
        this.modelService = modelService;
        this.categoryService = categoryService;
        this.subCategoryService = subCategoryService;
        this.trainingService = trainingService;
    }

    // ============================================================
    // 🤖 ANALYZE OCR TEXT AND EXTRACT PRODUCT INFO
    // ============================================================
    @Transactional
    public ProductOcrResponse analyzeAndExtract(HttpHeaders headers, String ocrText, ProductOcrRequest request) {
        log.info("🤖 AI Agent analyzing OCR text (length: {})", ocrText != null ? ocrText.length() : 0);
        
        ProductOcrResponse response = new ProductOcrResponse();
        response.setExtractedText(ocrText);
        response.setStatus("PROCESSING");
        
        if (!StringUtils.hasText(ocrText)) {
            response.setStatus("ERROR");
            response.setMessage("No text extracted from image");
            return response;
        }

        // Extract product information from OCR text
        ProductOcrResponse.ExtractedProductInfo extractedInfo = extractProductInfo(ocrText);
        response.setExtractedInfo(extractedInfo);

        // ============================================================
        // 📂 RESOLVE OR CREATE CATEGORY
        // ============================================================
        ProductCategory category = resolveOrCreateCategory(headers, extractedInfo, request);
        if (category != null) {
            response.setCategory(category);
            log.info("✅ Category resolved/created: {}", category.getCategoryName());
        }

        // ============================================================
        // 📁 RESOLVE OR CREATE SUBCATEGORY
        // ============================================================
        ProductSubCategory subCategory = resolveOrCreateSubCategory(headers, extractedInfo, category, request);
        if (subCategory != null) {
            response.setSubCategory(subCategory);
            log.info("✅ SubCategory resolved/created: {}", subCategory.getSubCategoryName());
        }

        // ============================================================
        // 🏷️ RESOLVE OR CREATE MAKE
        // ============================================================
        ProductMake make = resolveOrCreateMake(headers, extractedInfo, subCategory, request);
        response.setMake(make);
        response.setMakeCreated(make != null && response.getMakeCreated() != null && response.getMakeCreated());

        // ============================================================
        // 📱 RESOLVE OR CREATE MODEL
        // ============================================================
        if (make != null) {
            ProductModel model = resolveOrCreateModel(headers, extractedInfo, make, request);
            response.setModel(model);
            response.setModelCreated(model != null && response.getModelCreated() != null && response.getModelCreated());
        }

        response.setStatus("SUCCESS");
        response.setMessage("Product information extracted and stored successfully in database");
        response.setConfidence(calculateConfidence(extractedInfo));

        log.info("🤖 AI Agent extraction complete. Make: {}, Model: {}", 
                make != null ? make.getMakeName() : "NONE",
                response.getModel() != null ? response.getModel().getModelName() : "NONE");

        return response;
    }

    // ============================================================
    // 📊 EXTRACT PRODUCT INFO FROM OCR TEXT
    // ============================================================
    private ProductOcrResponse.ExtractedProductInfo extractProductInfo(String ocrText) {
        ProductOcrResponse.ExtractedProductInfo info = new ProductOcrResponse.ExtractedProductInfo();
        
        String normalizedText = ocrText.toUpperCase();
        
        // Extract Make/Brand
        String makeName = extractMake(normalizedText, ocrText);
        info.setMakeName(makeName);
        info.setBrand(makeName);
        info.setManufacturer(makeName);

        // Extract Model
        String modelName = extractModel(normalizedText, ocrText, makeName);
        info.setModelName(modelName);

        // Extract Serial Number
        String serialNumber = extractSerialNumber(ocrText);
        info.setSerialNumber(serialNumber);

        // Extract Category/SubCategory (if mentioned)
        String categoryName = extractCategory(normalizedText, ocrText);
        info.setCategoryName(categoryName);
        
        String subCategoryName = extractSubCategory(normalizedText, ocrText);
        info.setSubCategoryName(subCategoryName);

        // Extract Description (first few meaningful lines)
        String description = extractDescription(ocrText);
        info.setDescription(description);

        return info;
    }

    // ============================================================
    // 🏷️ EXTRACT MAKE/BRAND
    // ============================================================
    private String extractMake(String normalizedText, String originalText) {
        // First, try learned patterns (higher priority)
        ProductSubCategory subCategory = resolveSubCategory(new ProductOcrRequest());
        Long subCategoryId = subCategory != null ? subCategory.getSubCategoryId() : null;
        String learnedMake = extractUsingLearnedPatterns("MAKE", originalText, subCategoryId, null);
        if (learnedMake != null) {
            return learnedMake;
        }
        
        // Pattern 1: Look for common makes
        for (String make : COMMON_MAKES) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(make.toUpperCase()) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(originalText).find()) {
                return make;
            }
        }

        // Pattern 2: Look for "Brand:", "Make:", "Manufacturer:" labels
        Pattern brandPattern = Pattern.compile(
            "(?:brand|make|manufacturer|company)[\\s:]+([A-Z][A-Za-z0-9\\s&-]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = brandPattern.matcher(originalText);
        if (matcher.find()) {
            String candidate = matcher.group(1).trim();
            // Clean up common suffixes
            candidate = candidate.replaceAll("\\s+(Inc|LLC|Ltd|Corp|Corporation|Company)$", "");
            return candidate;
        }

        // Pattern 3: First capitalized word (often the brand)
        Pattern firstWordPattern = Pattern.compile("^([A-Z][A-Za-z0-9]+)");
        matcher = firstWordPattern.matcher(originalText.trim());
        if (matcher.find()) {
            String candidate = matcher.group(1);
            if (candidate.length() >= 2 && candidate.length() <= 20) {
                return candidate;
            }
        }

        return null;
    }

    // ============================================================
    // 📱 EXTRACT MODEL
    // ============================================================
    private String extractModel(String normalizedText, String originalText, String makeName) {
        // First, try learned patterns
        ProductSubCategory subCategory = resolveSubCategory(new ProductOcrRequest());
        Long subCategoryId = subCategory != null ? subCategory.getSubCategoryId() : null;
        final Long[] makeIdArray = new Long[1];
        if (makeName != null) {
            makeRepo.findByMakeNameIgnoreCase(makeName)
                .filter(m -> m.getActive() == null || m.getActive())
                .ifPresent(m -> makeIdArray[0] = m.getMakeId());
        }
        String learnedModel = extractUsingLearnedPatterns("MODEL", originalText, subCategoryId, makeIdArray[0]);
        if (learnedModel != null) {
            return learnedModel;
        }
        
        // Pattern 1: Look for "Model:", "Model No:", "Model Number:" labels
        Pattern modelPattern = Pattern.compile(
            "(?:model|model\\s*no|model\\s*number|type|variant)[\\s:]+([A-Za-z0-9\\s-]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = modelPattern.matcher(originalText);
        if (matcher.find()) {
            String candidate = matcher.group(1).trim();
            // Remove common prefixes/suffixes
            candidate = candidate.replaceAll("^(Model|No|Number|Type)[\\s:]+", "");
            return candidate.length() > 0 ? candidate : null;
        }

        // Pattern 2: If make is found, look for text after make name
        if (makeName != null) {
            Pattern afterMakePattern = Pattern.compile(
                Pattern.quote(makeName) + "[\\s]+([A-Za-z0-9\\s-]{2,30})",
                Pattern.CASE_INSENSITIVE
            );
            matcher = afterMakePattern.matcher(originalText);
            if (matcher.find()) {
                String candidate = matcher.group(1).trim();
                // Filter out common non-model words
                if (!candidate.matches("(?i)(Inc|LLC|Ltd|Corp|Corporation|Company|Laptop|Desktop|Phone|Device)")) {
                    return candidate;
                }
            }
        }

        // Pattern 3: Look for alphanumeric codes (e.g., "XPS 15", "iPhone 14 Pro")
        Pattern codePattern = Pattern.compile("([A-Z][A-Za-z0-9]+[\\s-]+[0-9]+[A-Za-z0-9\\s-]*)");
        matcher = codePattern.matcher(originalText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    // ============================================================
    // 🔢 EXTRACT SERIAL NUMBER
    // ============================================================
    private String extractSerialNumber(String text) {
        // Pattern 1: "Serial:", "S/N:", "SN:" labels
        Pattern serialPattern = Pattern.compile(
            "(?:serial|s/n|sn|serial\\s*number)[\\s:]+([A-Z0-9-]{4,30})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = serialPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim().toUpperCase();
        }

        // Pattern 2: Long alphanumeric codes (likely serial numbers)
        Pattern longCodePattern = Pattern.compile("\\b([A-Z0-9]{8,30})\\b");
        matcher = longCodePattern.matcher(text.toUpperCase());
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    // ============================================================
    // 📂 EXTRACT CATEGORY
    // ============================================================
    private String extractCategory(String normalizedText, String originalText) {
        Pattern categoryPattern = Pattern.compile(
            "(?:category|type|class)[\\s:]+([A-Za-z\\s]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = categoryPattern.matcher(originalText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Look for common category keywords
        String[] categories = {"Electronics", "Computer", "Laptop", "Desktop", "Phone", "Mobile", 
                              "Tablet", "Monitor", "Printer", "Scanner", "Camera", "TV", "Television"};
        for (String cat : categories) {
            if (normalizedText.contains(cat.toUpperCase())) {
                return cat;
            }
        }

        return null;
    }

    // ============================================================
    // 📁 EXTRACT SUBCATEGORY
    // ============================================================
    private String extractSubCategory(String normalizedText, String originalText) {
        Pattern subCategoryPattern = Pattern.compile(
            "(?:subcategory|sub\\s*category|subtype)[\\s:]+([A-Za-z\\s]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = subCategoryPattern.matcher(originalText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    // ============================================================
    // 📝 EXTRACT DESCRIPTION
    // ============================================================
    private String extractDescription(String text) {
        // Take first 3-5 meaningful lines (non-empty, not just numbers/symbols)
        String[] lines = text.split("\\r?\\n");
        List<String> meaningfulLines = new ArrayList<>();
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() > 10 && trimmed.matches(".*[A-Za-z]{3,}.*")) {
                meaningfulLines.add(trimmed);
                if (meaningfulLines.size() >= 5) break;
            }
        }
        
        return meaningfulLines.isEmpty() ? null : String.join(" ", meaningfulLines);
    }

    // ============================================================
    // 🔍 RESOLVE OR CREATE MAKE
    // ============================================================
    private ProductMake resolveOrCreateMake(HttpHeaders headers, 
                                            ProductOcrResponse.ExtractedProductInfo info,
                                            ProductSubCategory subCategory,
                                            ProductOcrRequest request) {
        String makeName = info.getMakeName();
        if (!StringUtils.hasText(makeName)) {
            log.warn("⚠️ No make name extracted from OCR text");
            return null;
        }

        // Try to find existing make
        Optional<ProductMake> existingMake;
        if (subCategory != null) {
            existingMake = makeRepo.findByMakeNameIgnoreCase(makeName)
                .filter(m -> m.getActive() == null || m.getActive())
                .filter(m -> m.getSubCategory() != null && 
                            m.getSubCategory().getSubCategoryId().equals(subCategory.getSubCategoryId()));
        } else {
            existingMake = makeRepo.findByMakeNameIgnoreCase(makeName)
                .filter(m -> m.getActive() == null || m.getActive());
        }

        if (existingMake.isPresent()) {
            log.info("✅ Found existing make: {}", makeName);
            return existingMake.get();
        }

        // Create new make if auto-create is enabled
        if (request.getAutoCreateMake()) {
            try {
                MakeRequest makeRequest = new MakeRequest();
                makeRequest.setUserId(request.getUserId());
                makeRequest.setUsername(request.getUsername());
                makeRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
                
                ProductMake newMake = new ProductMake();
                newMake.setMakeName(makeName);
                newMake.setSubCategory(subCategory);
                makeRequest.setMake(newMake);

                ProductMake created = makeService.create(headers, makeRequest);
                log.info("✅ Created new make: {} (id: {})", makeName, created.getMakeId());
                return created;
            } catch (Exception e) {
                log.error("❌ Failed to create make '{}': {}", makeName, e.getMessage());
                return null;
            }
        }

        log.warn("⚠️ Make '{}' not found and auto-create is disabled", makeName);
        return null;
    }

    // ============================================================
    // 📱 RESOLVE OR CREATE MODEL
    // ============================================================
    private ProductModel resolveOrCreateModel(HttpHeaders headers,
                                              ProductOcrResponse.ExtractedProductInfo info,
                                              ProductMake make,
                                              ProductOcrRequest request) {
        String modelName = info.getModelName();
        if (!StringUtils.hasText(modelName)) {
            log.warn("⚠️ No model name extracted from OCR text");
            return null;
        }

        // Try to find existing model
        Long makeId = make.getMakeId();
        if (makeId == null) {
            log.warn("⚠️ Make ID is null, cannot search for model");
            return null;
        }
        
        Optional<ProductModel> existingModel = modelRepo
            .findByModelNameIgnoreCaseAndMake_MakeId(modelName, makeId)
            .filter(m -> m.getActive() == null || m.getActive());

        if (existingModel.isPresent()) {
            log.info("✅ Found existing model: {} for make: {}", modelName, make.getMakeName());
            return existingModel.get();
        }

        // Create new model if auto-create is enabled
        if (request.getAutoCreateModel()) {
            try {
                ModelRequest modelRequest = new ModelRequest();
                modelRequest.setUserId(request.getUserId());
                modelRequest.setUsername(request.getUsername());
                modelRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
                
                ProductModel newModel = new ProductModel();
                newModel.setModelName(modelName);
                newModel.setMake(make);
                newModel.setDescription(info.getDescription());
                modelRequest.setModel(newModel);

                com.example.asset.dto.ModelDto createdDto = modelService.create(headers, modelRequest);
                // Fetch the created model entity
                Long modelId = createdDto.getModelId();
                if (modelId == null) {
                    throw new RuntimeException("Failed to retrieve created model ID");
                }
                ProductModel created = modelRepo.findById(modelId)
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve created model"));
                log.info("✅ Created new model: {} (id: {}) for make: {}", 
                        modelName, created.getModelId(), make.getMakeName());
                return created;
            } catch (Exception e) {
                log.error("❌ Failed to create model '{}': {}", modelName, e.getMessage());
                return null;
            }
        }

        log.warn("⚠️ Model '{}' not found and auto-create is disabled", modelName);
        return null;
    }

    // ============================================================
    // 📂 RESOLVE OR CREATE CATEGORY
    // ============================================================
    private ProductCategory resolveOrCreateCategory(HttpHeaders headers, 
                                                    ProductOcrResponse.ExtractedProductInfo info,
                                                    ProductOcrRequest request) {
        String categoryName = info.getCategoryName();
        if (!StringUtils.hasText(categoryName)) {
            return null;
        }

        // Try to find existing category
        Optional<ProductCategory> existingCategory = categoryRepo.findByCategoryNameIgnoreCase(categoryName)
            .filter(c -> c.getActive() == null || c.getActive());

        if (existingCategory.isPresent()) {
            log.info("✅ Found existing category: {}", categoryName);
            return existingCategory.get();
        }

        // Create new category if auto-create is enabled (default true)
        if (request.getAutoCreateMake() != null && request.getAutoCreateMake()) {
            try {
                CategoryRequest categoryRequest = new CategoryRequest();
                categoryRequest.setUserId(request.getUserId());
                categoryRequest.setUsername(request.getUsername());
                categoryRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
                
                ProductCategory newCategory = new ProductCategory();
                newCategory.setCategoryName(categoryName);
                categoryRequest.setCategory(newCategory);

                com.example.asset.dto.CategoryDto createdDto = categoryService.create(headers, categoryRequest);
                // Fetch the created category entity
                Long categoryId = createdDto.getCategoryId();
                if (categoryId == null) {
                    throw new RuntimeException("Failed to retrieve created category ID");
                }
                ProductCategory created = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve created category"));
                log.info("✅ Created new category: {} (id: {})", categoryName, created.getCategoryId());
                return created;
            } catch (Exception e) {
                log.error("❌ Failed to create category '{}': {}", categoryName, e.getMessage());
                return null;
            }
        }

        log.warn("⚠️ Category '{}' not found and auto-create is disabled", categoryName);
        return null;
    }

    // ============================================================
    // 📁 RESOLVE OR CREATE SUBCATEGORY
    // ============================================================
    private ProductSubCategory resolveOrCreateSubCategory(HttpHeaders headers,
                                                          ProductOcrResponse.ExtractedProductInfo info,
                                                          ProductCategory category,
                                                          ProductOcrRequest request) {
        // First try to resolve from request
        ProductSubCategory subCategory = resolveSubCategory(request);
        if (subCategory != null) {
            return subCategory;
        }

        // Try to extract from OCR info
        String subCategoryName = info.getSubCategoryName();
        if (!StringUtils.hasText(subCategoryName)) {
            return null;
        }

        // Try to find existing subcategory
        if (category != null) {
            Long categoryId = category.getCategoryId();
            if (categoryId != null) {
                // Search by name and filter by category
                Optional<ProductSubCategory> existingSubCategory = subCategoryRepo
                    .findBySubCategoryNameIgnoreCase(subCategoryName)
                    .filter(s -> s.getActive() == null || s.getActive())
                    .filter(s -> s.getCategory() != null && categoryId.equals(s.getCategory().getCategoryId()));

                if (existingSubCategory.isPresent()) {
                    log.info("✅ Found existing subcategory: {} in category: {}", subCategoryName, category.getCategoryName());
                    return existingSubCategory.get();
                }
            }
        } else {
            // Try global search
            Optional<ProductSubCategory> existingSubCategory = subCategoryRepo
                .findBySubCategoryNameIgnoreCase(subCategoryName)
                .filter(s -> s.getActive() == null || s.getActive());

            if (existingSubCategory.isPresent()) {
                log.info("✅ Found existing subcategory: {}", subCategoryName);
                return existingSubCategory.get();
            }
        }

        // Create new subcategory if auto-create is enabled
        if (request.getAutoCreateMake() != null && request.getAutoCreateMake()) {
            try {
                SubCategoryRequest subCategoryRequest = new SubCategoryRequest();
                subCategoryRequest.setUserId(request.getUserId());
                subCategoryRequest.setUsername(request.getUsername());
                subCategoryRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
                
                ProductSubCategory newSubCategory = new ProductSubCategory();
                newSubCategory.setSubCategoryName(subCategoryName);
                if (category != null) {
                    newSubCategory.setCategory(category);
                }
                subCategoryRequest.setSubCategory(newSubCategory);

                ProductSubCategory created = subCategoryService.create(headers, subCategoryRequest);
                log.info("✅ Created new subcategory: {} (id: {})", subCategoryName, created.getSubCategoryId());
                return created;
            } catch (Exception e) {
                log.error("❌ Failed to create subcategory '{}': {}", subCategoryName, e.getMessage());
                return null;
            }
        }

        log.warn("⚠️ SubCategory '{}' not found and auto-create is disabled", subCategoryName);
        return null;
    }

    // ============================================================
    // 📂 RESOLVE SUBCATEGORY (from request)
    // ============================================================
    private ProductSubCategory resolveSubCategory(ProductOcrRequest request) {
        Long subCategoryId = request.getSubCategoryId();
        if (subCategoryId != null) {
            return subCategoryRepo.findById(subCategoryId)
                .filter(s -> s.getActive() == null || s.getActive())
                .orElse(null);
        }
        
        if (StringUtils.hasText(request.getSubCategoryName())) {
            return subCategoryRepo.findBySubCategoryNameIgnoreCase(request.getSubCategoryName().trim())
                .filter(s -> s.getActive() == null || s.getActive())
                .orElse(null);
        }

        return null;
    }

    // ============================================================
    // 📊 CALCULATE CONFIDENCE SCORE
    // ============================================================
    private Double calculateConfidence(ProductOcrResponse.ExtractedProductInfo info) {
        double confidence = 0.0;
        
        if (StringUtils.hasText(info.getMakeName())) confidence += 0.4;
        if (StringUtils.hasText(info.getModelName())) confidence += 0.4;
        if (StringUtils.hasText(info.getSerialNumber())) confidence += 0.1;
        if (StringUtils.hasText(info.getDescription())) confidence += 0.1;
        
        return Math.min(confidence, 1.0);
    }

    // ============================================================
    // 🎯 EXTRACT USING LEARNED PATTERNS
    // ============================================================
    private String extractUsingLearnedPatterns(String patternType, String text, Long subCategoryId, Long makeId) {
        List<com.example.asset.entity.OcrLearnedPattern> patterns = 
            trainingService.getActivePatterns(patternType, subCategoryId, makeId);
        
        for (com.example.asset.entity.OcrLearnedPattern pattern : patterns) {
            try {
                String regex = pattern.getPatternRegex();
                if (regex != null && !regex.isEmpty()) {
                    Pattern compiledPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = compiledPattern.matcher(text);
                    if (matcher.find()) {
                        String extracted = matcher.group(1); // First capture group
                        if (extracted != null && !extracted.trim().isEmpty()) {
                            // Update pattern usage
                            pattern.setUsageCount(pattern.getUsageCount() + 1);
                            pattern.setLastUsedAt(java.time.LocalDateTime.now());
                            // Note: Pattern update should be done in training service, but for now we'll log
                            log.debug("✅ Used learned pattern: patternId={}, type={}, extracted={}", 
                                    pattern.getPatternId(), patternType, extracted);
                            return extracted.trim();
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ Failed to apply learned pattern {}: {}", pattern.getPatternId(), e.getMessage());
            }
        }
        
        return null;
    }
}

