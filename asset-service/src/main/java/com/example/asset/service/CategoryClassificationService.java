package com.example.asset.service;

import com.example.asset.client.LlmClient;
import com.example.asset.dto.CategoryClassifyResponse;
import com.example.asset.dto.ProductBarcodeLookupResult;
import com.example.asset.entity.OcrLearnedPattern;
import com.example.asset.entity.ProductCategory;
import com.example.asset.entity.ProductSubCategory;
import com.example.asset.repository.OcrLearnedPatternRepository;
import com.example.asset.repository.ProductCategoryRepository;
import com.example.asset.repository.ProductSubCategoryRepository;
import com.example.asset.util.ByteArrayMultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sourceforge.tess4j.TesseractException;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Classify product into category when missing.
 * Uses: 1) Keyword mapping, 2) LLM (if enabled).
 */
@Service
public class CategoryClassificationService {

    private static final Logger log = LoggerFactory.getLogger(CategoryClassificationService.class);

    private final LlmClient llmClient;
    private final OcrService ocrService;
    private final QrBarcodeImageService qrBarcodeImageService;
    private final ProductBarcodeLookupService productBarcodeLookupService;
    private final ProductCategoryRepository categoryRepository;
    private final ProductSubCategoryRepository subCategoryRepository;
    private final OcrLearnedPatternRepository ocrLearnedPatternRepository;
    private final ObjectMapper objectMapper;

    /** Zoom scales for multi-scale OCR (1x, 1.5x, 2x) to improve logo/name reading */
    private static final double[] OCR_ZOOM_SCALES = {1.0, 1.5, 2.0};

    // Keyword -> (Category, Subcategory)
    private static final Map<Pattern, String[]> KEYWORD_MAP = new LinkedHashMap<>();

    static {
        // IT Equipment
        add("laptop|notebook|ultrabook", "IT Equipment", "Laptop");
        add("desktop|pc|workstation", "IT Equipment", "Desktop");
        add("monitor|display|screen", "IT Equipment", "Monitor");
        add("printer|scanner|multifunction", "IT Equipment", "Printer");
        add("keyboard|mouse|webcam", "IT Equipment", "Peripheral");
        add("server|nas|storage", "IT Equipment", "Server");
        add("router|switch|network", "IT Equipment", "Network");
        add("tablet|ipad", "IT Equipment", "Tablet");
        add("phone|smartphone|mobile", "IT Equipment", "Mobile");
        add("headset|headphone|earphone", "IT Equipment", "Audio");
        // Electronics
        add("tv|television", "Electronics", "TV");
        add("camera|camcorder", "Electronics", "Camera");
        add("speaker|soundbar", "Electronics", "Audio");
        // Furniture
        add("chair|seat", "Furniture", "Chair");
        add("desk|table", "Furniture", "Desk");
        add("cabinet|shelf", "Furniture", "Cabinet");
        // Vehicles
        add("car|vehicle|automobile", "Vehicles", "Car");
        add("forklift|truck", "Vehicles", "Industrial");
        // Machinery
        add("machine|equipment|tool", "Machinery", "Equipment");
        add("generator|motor", "Machinery", "Power");
        // Medical
        add("medical|hospital|diagnostic", "Medical Devices", "Diagnostic");
        // Food & Beverages (from product barcodes)
        add("water|sparkling|beverage|drink", "Food & Beverages", "Beverages");
        add("food|snack|cereal", "Food & Beverages", "Food");
    }

    private static void add(String regex, String category, String subcategory) {
        KEYWORD_MAP.put(Pattern.compile(regex, Pattern.CASE_INSENSITIVE), new String[]{category, subcategory});
    }

    public CategoryClassificationService(LlmClient llmClient, OcrService ocrService,
                                         QrBarcodeImageService qrBarcodeImageService,
                                         ProductBarcodeLookupService productBarcodeLookupService,
                                         ProductCategoryRepository categoryRepository,
                                         ProductSubCategoryRepository subCategoryRepository,
                                         OcrLearnedPatternRepository ocrLearnedPatternRepository,
                                         ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.ocrService = ocrService;
        this.qrBarcodeImageService = qrBarcodeImageService;
        this.productBarcodeLookupService = productBarcodeLookupService;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.ocrLearnedPatternRepository = ocrLearnedPatternRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Classify product name/description into category and subcategory.
     * Returns null if classification fails.
     */
    public CategoryResult classify(String productName, String brand, String existingCategory) {
        if (StringUtils.hasText(existingCategory)) {
            return new CategoryResult(existingCategory, null, "SOURCE");
        }

        String text = buildSearchText(productName, brand);
        if (!StringUtils.hasText(text)) return null;

        // 1. Keyword mapping
        CategoryResult kw = classifyByKeyword(text);
        if (kw != null) {
            log.debug("Category from keyword: {} -> {}", text, kw.category);
            return kw;
        }

        // 2. LLM (if available)
        CategoryResult llm = classifyByLlm(text);
        if (llm != null) {
            log.debug("Category from LLM: {} -> {}", text, llm.category);
            return llm;
        }

        return new CategoryResult("General", "Other", "DEFAULT");
    }

    /**
     * Classify product from photo or scanned image.
     * Reads logo/brand name and product name from image via OCR, then matches against category/subcategory master.
     * Uses: 1) Barcode lookup (if barcode found), 2) OCR text + brand/product extraction + keyword/LLM classification.
     * Returns CategoryClassifyResponse with category, subcategory, master IDs when matched, and extracted brand/product.
     */
    public CategoryClassifyResponse classifyFromImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return buildResponseWithMasterMatch("General", "Other", "DEFAULT", null, null, null);
        }

        String extractedText = null;
        String brandName = null;
        String productName = null;

        // 1. Try barcode/QR decode first
        Optional<String> barcodeOpt = qrBarcodeImageService.decodeFromImage(image);
        if (barcodeOpt.isPresent()) {
            String barcodeValue = barcodeOpt.get().trim();
            extractedText = barcodeValue;

            // If it's a product barcode (8-14 digits), try external lookup
            if (productBarcodeLookupService.isProductBarcode(barcodeValue)) {
                Optional<ProductBarcodeLookupResult> lookup = productBarcodeLookupService.lookup(barcodeValue);
                if (lookup.isPresent()) {
                    ProductBarcodeLookupResult r = lookup.get();
                    if (StringUtils.hasText(r.getCategory())) {
                        log.debug("Category from barcode lookup: {} -> {}", barcodeValue, r.getCategory());
                        brandName = r.getBrand();
                        productName = r.getProductName();
                        return buildResponseWithMasterMatch(
                                r.getCategory(),
                                r.getSubcategory() != null ? r.getSubcategory() : "Other",
                                "BARCODE_LOOKUP",
                                extractedText, brandName, productName
                        );
                    }
                }
            }

            // Barcode/QR decoded but no product lookup - use decoded value for classification
            CategoryResult cr = classify(barcodeValue, null, null);
            if (cr != null) {
                return buildResponseWithMasterMatch(
                        cr.category,
                        cr.subcategory != null ? cr.subcategory : "Other",
                        cr.source,
                        extractedText, brandName, productName
                );
            }
        }

        // 2. OCR: multi-scale (zoom in/out) for better logo and name reading
        try {
            if (ocrService.isTesseractAvailable()) {
                extractedText = extractTextWithMultiScaleZoom(image);
                if (StringUtils.hasText(extractedText)) {
                    // Extract brand/logo and product name from OCR text for better classification
                    BrandProductExtract extract = extractBrandAndProductFromText(extractedText);
                    if (extract != null) {
                        brandName = extract.brand;
                        productName = extract.product;
                    }
                    String searchText = buildSearchText(productName != null ? productName : extractedText, brandName);
                    // 2a. Try learned patterns first (trained data)
                    CategoryResult cr = classifyByLearnedPattern(extractedText, searchText);
                    if (cr == null) {
                        cr = classify(searchText, brandName, null);
                    }
                    if (cr == null) {
                        cr = classify(extractedText, null, null);
                    }
                    if (cr != null) {
                        return buildResponseWithMasterMatch(
                                cr.category,
                                cr.subcategory != null ? cr.subcategory : "Other",
                                "OCR_" + cr.source,
                                extractedText, brandName, productName
                        );
                    }
                }
            }
        } catch (IOException | TesseractException e) {
            log.warn("OCR extraction failed: {}", e.getMessage());
        }

        return buildResponseWithMasterMatch("General", "Other", "DEFAULT", extractedText, brandName, productName);
    }

    /**
     * Extract text from image using multi-scale zoom (1x, 1.5x, 2x) for better logo and name reading.
     * Tries each scale and returns the result with the most extracted text.
     */
    private String extractTextWithMultiScaleZoom(MultipartFile image) throws IOException, TesseractException {
        byte[] bytes = image.getBytes();
        BufferedImage original = ImageIO.read(new java.io.ByteArrayInputStream(bytes));
        if (original == null) return ocrService.extractText(image);

        String bestText = "";
        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            contentType = "image/png";
        }

        for (double scale : OCR_ZOOM_SCALES) {
            try {
                int w = original.getWidth();
                int h = original.getHeight();
                int newW = (int) (w * scale);
                int newH = (int) (h * scale);
                if (newW < 50 || newH < 50 || newW > 4000 || newH > 4000) continue;

                BufferedImage scaled = Scalr.resize(original, Scalr.Method.QUALITY, newW, newH);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(scaled, "png", baos);
                MultipartFile scaledFile = new ByteArrayMultipartFile(baos.toByteArray(), "image", "image.png", contentType);
                String text = ocrService.extractText(scaledFile);
                if (StringUtils.hasText(text) && text.trim().length() > bestText.length()) {
                    bestText = text.trim();
                    log.debug("Multi-scale OCR: scale {} produced {} chars", scale, bestText.length());
                    if (bestText.length() > 80) break; // Good enough, skip remaining scales
                }
            } catch (Exception e) {
                log.debug("OCR at scale {} failed: {}", scale, e.getMessage());
            }
        }
        return StringUtils.hasText(bestText) ? bestText : ocrService.extractText(image);
    }

    /**
     * Classify using OcrLearnedPattern (trained patterns for CATEGORY type).
     */
    private CategoryResult classifyByLearnedPattern(String ocrText, String searchText) {
        List<OcrLearnedPattern> patterns = ocrLearnedPatternRepository.findByPatternTypeAndIsActiveTrue("CATEGORY");
        if (patterns.isEmpty()) return null;

        String textToMatch = (searchText != null ? searchText + " " : "") + (ocrText != null ? ocrText : "");
        if (!StringUtils.hasText(textToMatch)) return null;

        String lower = textToMatch.toLowerCase();
        for (OcrLearnedPattern p : patterns) {
            if (p.getPatternRegex() != null && !p.getPatternRegex().isBlank()) {
                try {
                    if (Pattern.compile(p.getPatternRegex(), Pattern.CASE_INSENSITIVE).matcher(textToMatch).find()) {
                        String[] catSub = parseCategoryFromPattern(p);
                        if (catSub != null) {
                            log.debug("Category from learned pattern: {} -> {}", p.getPatternId(), catSub[0]);
                            return new CategoryResult(catSub[0], catSub.length > 1 ? catSub[1] : null, "LEARNED_PATTERN");
                        }
                    }
                } catch (Exception e) {
                    log.debug("Pattern regex invalid: {}", e.getMessage());
                }
            }
            if (StringUtils.hasText(p.getPatternKeywords())) {
                try {
                    List<String> keywords = objectMapper.readValue(p.getPatternKeywords(), new TypeReference<List<String>>() {});
                    if (keywords != null && keywords.stream().anyMatch(k -> lower.contains(k.toLowerCase().trim()))) {
                        String[] catSub = parseCategoryFromPattern(p);
                        if (catSub != null) {
                            log.debug("Category from learned keywords: {} -> {}", p.getPatternId(), catSub[0]);
                            return new CategoryResult(catSub[0], catSub.length > 1 ? catSub[1] : null, "LEARNED_PATTERN");
                        }
                    }
                } catch (Exception e) {
                    log.debug("Pattern keywords parse failed: {}", e.getMessage());
                }
            }
        }
        return null;
    }

    /** Parse category/subcategory from pattern (stored in context or subCategoryId lookup). */
    private String[] parseCategoryFromPattern(OcrLearnedPattern p) {
        Long subId = p.getSubCategoryId();
        if (subId != null) {
            Optional<ProductSubCategory> sub = subCategoryRepository.findById(subId);
            if (sub.isPresent()) {
                ProductSubCategory s = sub.get();
                String cat = s.getCategory() != null ? s.getCategory().getCategoryName() : null;
                if (cat != null) {
                    return new String[]{cat, s.getSubCategoryName()};
                }
            }
        }
        return null;
    }

    /**
     * Extract brand/logo name and product name from OCR text using LLM.
     */
    private BrandProductExtract extractBrandAndProductFromText(String ocrText) {
        if (!StringUtils.hasText(ocrText) || ocrText.length() > 2000) return null;
        try {
            String prompt = "From this text extracted from a product image (label, packaging, or receipt), extract the brand/logo name and product name. " +
                    "Reply with ONLY two lines: Brand: <name>\nProduct: <name>. Use the most prominent brand and product. If unsure, leave blank.";
            String response = llmClient.complete(prompt + "\n\nText:\n" + ocrText);
            if (StringUtils.hasText(response)) {
                String brand = null, product = null;
                for (String line : response.split("\n")) {
                    if (line.toLowerCase().startsWith("brand:")) {
                        brand = line.substring(6).trim();
                        if (brand.isEmpty()) brand = null;
                    } else if (line.toLowerCase().startsWith("product:")) {
                        product = line.substring(8).trim();
                        if (product.isEmpty()) product = null;
                    }
                }
                if (brand != null || product != null) {
                    return new BrandProductExtract(brand, product);
                }
            }
        } catch (Exception e) {
            log.debug("Brand/product extraction failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Match suggested category/subcategory against master and build full response.
     */
    private CategoryClassifyResponse buildResponseWithMasterMatch(String suggestedCategory, String suggestedSubcategory,
                                                                 String source, String extractedText,
                                                                 String extractedBrandName, String extractedProductName) {
        CategoryClassifyResponse resp = new CategoryClassifyResponse(
                suggestedCategory, suggestedSubcategory, source, extractedText
        );
        resp.setExtractedBrandName(extractedBrandName);
        resp.setExtractedProductName(extractedProductName);

        // Match against category/subcategory master
        String catSearch = StringUtils.hasText(suggestedCategory) ? suggestedCategory.trim() : "General";
        Optional<ProductCategory> catOpt = categoryRepository.findByCategoryNameIgnoreCase(catSearch);
        if (catOpt.isEmpty()) {
            // Fuzzy: try contains match from all categories
            List<ProductCategory> allCats = categoryRepository.findAll();
            final String catSearchFinal = catSearch;
            catOpt = allCats.stream()
                    .filter(c -> c.getCategoryName() != null &&
                            (c.getCategoryName().equalsIgnoreCase(catSearchFinal) ||
                                    c.getCategoryName().toLowerCase().contains(catSearchFinal.toLowerCase()) ||
                                    catSearchFinal.toLowerCase().contains(c.getCategoryName().toLowerCase())))
                    .findFirst();
        }
        if (catOpt.isPresent()) {
            ProductCategory cat = catOpt.get();
            String subName = StringUtils.hasText(suggestedSubcategory) ? suggestedSubcategory.trim() : "Other";
            Optional<ProductSubCategory> subOpt = subCategoryRepository
                    .findByCategory_CategoryIdAndSubCategoryNameIgnoreCase(cat.getCategoryId(), subName);
            if (subOpt.isEmpty()) {
                // Fuzzy: try subcategories under this category
                List<ProductSubCategory> subs = subCategoryRepository.findByCategory_CategoryId(cat.getCategoryId());
                final String subSearch = subName;
                subOpt = subs.stream()
                        .filter(s -> s.getSubCategoryName() != null &&
                                (s.getSubCategoryName().equalsIgnoreCase(subSearch) ||
                                        s.getSubCategoryName().toLowerCase().contains(subSearch.toLowerCase()) ||
                                        subSearch.toLowerCase().contains(s.getSubCategoryName().toLowerCase())))
                        .findFirst();
            }
            if (subOpt.isPresent()) {
                ProductSubCategory sub = subOpt.get();
                resp.setCategoryId(cat.getCategoryId());
                resp.setSubCategoryId(sub.getSubCategoryId());
                resp.setMatchedCategoryName(cat.getCategoryName());
                resp.setMatchedSubCategoryName(sub.getSubCategoryName());
                resp.setMatchedFromMaster(true);
                log.debug("Matched master: categoryId={}, subCategoryId={}", cat.getCategoryId(), sub.getSubCategoryId());
            } else {
                resp.setCategoryId(cat.getCategoryId());
                resp.setMatchedCategoryName(cat.getCategoryName());
                resp.setMatchedFromMaster(false);
            }
        } else {
            resp.setMatchedFromMaster(false);
        }
        return resp;
    }

    private static class BrandProductExtract {
        final String brand;
        final String product;

        BrandProductExtract(String brand, String product) {
            this.brand = brand;
            this.product = product;
        }
    }

    private String buildSearchText(String productName, String brand) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(productName)) sb.append(productName).append(" ");
        if (StringUtils.hasText(brand)) sb.append(brand);
        return sb.toString().trim();
    }

    private CategoryResult classifyByKeyword(String text) {
        for (Map.Entry<Pattern, String[]> e : KEYWORD_MAP.entrySet()) {
            if (e.getKey().matcher(text).find()) {
                return new CategoryResult(e.getValue()[0], e.getValue()[1], "KEYWORD");
            }
        }
        return null;
    }

    /**
     * Classify using LLM trained with master category/subcategory list.
     * Ensures LLM returns names that match our database for accurate master matching.
     */
    private CategoryResult classifyByLlm(String text) {
        try {
            // Fetch master categories and subcategories to train the LLM
            List<ProductCategory> categories = categoryRepository.findAll();
            Map<Long, List<String>> categoryToSubs = new LinkedHashMap<>();
            for (ProductCategory c : categories) {
                List<ProductSubCategory> subs = subCategoryRepository.findByCategory_CategoryId(c.getCategoryId());
                List<String> subNames = subs.stream()
                        .map(ProductSubCategory::getSubCategoryName)
                        .filter(StringUtils::hasText)
                        .toList();
                categoryToSubs.put(c.getCategoryId(), subNames);
            }

            StringBuilder masterList = new StringBuilder();
            for (ProductCategory c : categories) {
                if (c.getCategoryName() == null) continue;
                masterList.append("- ").append(c.getCategoryName());
                List<String> subs = categoryToSubs.getOrDefault(c.getCategoryId(), List.of());
                if (!subs.isEmpty()) {
                    masterList.append(" (subcategories: ").append(String.join(", ", subs)).append(")");
                }
                masterList.append("\n");
            }
            if (masterList.length() == 0) {
                masterList.append("IT Equipment, Electronics, Furniture, Machinery, Medical Devices, Vehicles, Tools, Food & Beverages");
            }

            String prompt = "You are an asset classification expert. Classify this product into EXACTLY one category and subcategory from the master list below. " +
                    "Reply with ONLY: CategoryName,SubCategoryName (comma-separated, no extra text). " +
                    "Use the EXACT names from the list. If no good match, pick the closest.\n\n" +
                    "MASTER LIST:\n" + masterList + "\n" +
                    "Product/Brand/Text from image: " + text;
            String response = llmClient.complete(prompt);
            if (StringUtils.hasText(response)) {
                String[] parts = response.trim().split("[,;]");
                if (parts.length >= 1) {
                    String cat = parts[0].trim();
                    String sub = parts.length >= 2 ? parts[1].trim() : null;
                    if (!cat.isEmpty()) {
                        return new CategoryResult(cat, sub, "LLM");
                    }
                }
            }
        } catch (Exception e) {
            log.debug("LLM classification failed: {}", e.getMessage());
        }
        return null;
    }

    public static class CategoryResult {
        public final String category;
        public final String subcategory;
        public final String source;

        public CategoryResult(String category, String subcategory, String source) {
            this.category = category;
            this.subcategory = subcategory;
            this.source = source;
        }
    }
}
