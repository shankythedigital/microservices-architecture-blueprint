package com.example.asset.service;

import com.example.asset.dto.*;
import com.example.asset.entity.*;
import com.example.asset.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ✅ IntelligentDocumentExtractionService
 * Advanced AI-powered service that intelligently extracts comprehensive asset information
 * from various document formats, handling different naming conventions and structures.
 * 
 * Supports:
 * - Invoices, Purchase Orders, Receipts
 * - Warranty Cards, Warranty Certificates
 * - AMC Documents, Service Contracts
 * - Product Spec Sheets, Manuals
 * - Different naming conventions and formats
 */
@Service
@Transactional
public class IntelligentDocumentExtractionService {

    private static final Logger log = LoggerFactory.getLogger(IntelligentDocumentExtractionService.class);

    private final OcrService ocrService;
    @SuppressWarnings("unused")
    private final ProductOcrAiAgentService ocrAiAgentService; // Reserved for future use
    private final CategoryService categoryService;
    private final SubCategoryService subCategoryService;
    private final MakeService makeService;
    private final ModelService modelService;
    private final ComponentService componentService;
    private final AssetWarrantyService warrantyService;
    private final AssetAmcService amcService;
    private final AssetCrudService assetService;
    private final DocumentService documentService;
    private final VendorService vendorService;
    private final OutletService outletService;
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final AssetComponentRepository componentRepo;
    private final VendorRepository vendorRepo;
    private final PurchaseOutletRepository outletRepo;
    private final AssetMasterRepository assetRepo;
    private final AssetWarrantyRepository warrantyRepo;
    private final AssetAmcRepository amcRepo;
    private final AssetPurchaseInfoRepository purchaseInfoRepo;
    private final OcrTrainingService trainingService;

    // Document type patterns
    private static final Map<String, List<String>> DOCUMENT_TYPE_KEYWORDS = Map.of(
        "INVOICE", List.of("invoice", "bill", "receipt", "purchase", "order"),
        "WARRANTY_CARD", List.of("warranty", "guarantee", "warranty card", "warranty certificate"),
        "AMC_DOCUMENT", List.of("amc", "annual maintenance", "maintenance contract", "service contract", "service agreement"),
        "SPEC_SHEET", List.of("specification", "spec sheet", "technical specification", "product spec"),
        "MANUAL", List.of("manual", "user guide", "instruction", "handbook")
    );

    // Common component names
    private static final Set<String> COMMON_COMPONENTS = Set.of(
        "RAM", "Hard Drive", "SSD", "Processor", "CPU", "GPU", "Graphics Card",
        "Motherboard", "Power Supply", "Battery", "Keyboard", "Mouse", "Monitor",
        "Screen", "Display", "Camera", "Speaker", "Microphone", "Charger", "Adapter"
    );

    public IntelligentDocumentExtractionService(
            OcrService ocrService,
            ProductOcrAiAgentService ocrAiAgentService,
            CategoryService categoryService,
            SubCategoryService subCategoryService,
            MakeService makeService,
            ModelService modelService,
            ComponentService componentService,
            AssetWarrantyService warrantyService,
            AssetAmcService amcService,
            AssetCrudService assetService,
            DocumentService documentService,
            VendorService vendorService,
            OutletService outletService,
            ProductCategoryRepository categoryRepo,
            ProductSubCategoryRepository subCategoryRepo,
            ProductMakeRepository makeRepo,
            ProductModelRepository modelRepo,
            AssetComponentRepository componentRepo,
            VendorRepository vendorRepo,
            PurchaseOutletRepository outletRepo,
            AssetMasterRepository assetRepo,
            AssetWarrantyRepository warrantyRepo,
            AssetAmcRepository amcRepo,
            AssetPurchaseInfoRepository purchaseInfoRepo,
            OcrTrainingService trainingService) {
        this.ocrService = ocrService;
        this.ocrAiAgentService = ocrAiAgentService;
        this.categoryService = categoryService;
        this.subCategoryService = subCategoryService;
        this.makeService = makeService;
        this.modelService = modelService;
        this.componentService = componentService;
        this.warrantyService = warrantyService;
        this.amcService = amcService;
        this.assetService = assetService;
        this.documentService = documentService;
        this.vendorService = vendorService;
        this.outletService = outletService;
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.componentRepo = componentRepo;
        this.vendorRepo = vendorRepo;
        this.outletRepo = outletRepo;
        this.assetRepo = assetRepo;
        this.warrantyRepo = warrantyRepo;
        this.amcRepo = amcRepo;
        this.purchaseInfoRepo = purchaseInfoRepo;
        this.trainingService = trainingService;
    }

    // ============================================================
    // 🤖 INTELLIGENT EXTRACTION (Main Entry Point)
    // ============================================================
    @Transactional
    public IntelligentExtractionResponse extractComprehensiveInfo(
            HttpHeaders headers, MultipartFile file, IntelligentExtractionRequest request) {
        
        long startTime = System.currentTimeMillis();
        log.info("🤖 Starting intelligent extraction for file: {} (type: {})", 
                file.getOriginalFilename(), request.getDocumentType());
        
        IntelligentExtractionResponse response = new IntelligentExtractionResponse();
        response.setStatus("PROCESSING");
        
        try {
            // 1. Extract text from file
            String extractedText = ocrService.extractText(file);
            response.setExtractedText(extractedText);
            
            if (!StringUtils.hasText(extractedText)) {
                response.setStatus("ERROR");
                response.setMessage("No text extracted from document");
                return response;
            }

            // 2. Detect document type if not provided
            String documentType = detectDocumentType(extractedText, request.getDocumentType());
            response.setDocumentType(documentType);
            log.info("📄 Detected document type: {}", documentType);

            // 3. Extract comprehensive asset information
            IntelligentExtractionResponse.ExtractedAssetInfo assetInfo = 
                extractComprehensiveAssetInfo(extractedText, documentType);
            response.setAssetInfo(assetInfo);

            // 4. Create/Resolve entities based on extracted info
            createOrResolveEntities(headers, response, request);

            // 4a. Create/Resolve vendor and outlet if applicable
            createOrResolveVendorAndOutlet(headers, response, request);

            // 5. Extract and create warranty if applicable
            if (request.getExtractWarranty() && assetInfo.getWarrantyInfo() != null) {
                createWarranty(headers, response, request);
            }

            // 6. Extract and create AMC if applicable
            if (request.getExtractAmc() && assetInfo.getAmcInfo() != null) {
                createAmc(headers, response, request);
            }

            // 7. Extract and create components if applicable
            if (request.getExtractComponents() && assetInfo.getComponentNames() != null) {
                createComponents(headers, response, request);
            }

            // 8. Create asset if all required info is available
            createAsset(headers, response, request);

            // 8a. Create purchase info if invoice/bill data is available
            createPurchaseInfo(headers, response, request);

            // 9. Save document
            saveDocument(headers, file, response, request);

            // 10. Calculate confidence and metadata
            response.setConfidence(calculateConfidence(assetInfo));
            response.getMetadata().setProcessingTimeMs(System.currentTimeMillis() - startTime);
            response.getMetadata().setDocumentFormat(file.getContentType());
            response.getMetadata().setExtractionMethod("INTELLIGENT_PARSING");

            response.setStatus("SUCCESS");
            response.setMessage("Comprehensive asset information extracted and stored successfully");
            
            log.info("✅ Intelligent extraction completed in {}ms. Asset: {}, Warranty: {}, AMC: {}, Vendor: {}, Outlet: {}", 
                    response.getMetadata().getProcessingTimeMs(),
                    response.getAsset() != null ? "YES" : "NO",
                    response.getWarranty() != null ? "YES" : "NO",
                    response.getAmc() != null ? "YES" : "NO",
                    response.getVendor() != null ? "YES" : "NO",
                    response.getOutlet() != null ? "YES" : "NO");

        } catch (Exception e) {
            log.error("❌ Intelligent extraction failed: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setMessage("Extraction failed: " + e.getMessage());
        }

        return response;
    }

    // ============================================================
    // 📄 DETECT DOCUMENT TYPE
    // ============================================================
    private String detectDocumentType(String text, String providedType) {
        if (StringUtils.hasText(providedType)) {
            return providedType.toUpperCase();
        }

        String normalizedText = text.toLowerCase();
        
        for (Map.Entry<String, List<String>> entry : DOCUMENT_TYPE_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (normalizedText.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        return "UNKNOWN";
    }

    // ============================================================
    // 📊 EXTRACT COMPREHENSIVE ASSET INFO
    // ============================================================
    private IntelligentExtractionResponse.ExtractedAssetInfo extractComprehensiveAssetInfo(
            String text, String documentType) {
        
        IntelligentExtractionResponse.ExtractedAssetInfo info = 
            new IntelligentExtractionResponse.ExtractedAssetInfo();
        
        String normalizedText = text.toUpperCase();
        
        // Extract basic product info (using existing OCR AI agent patterns)
        extractBasicProductInfo(text, normalizedText, info);
        
        // Extract based on document type
        switch (documentType) {
            case "INVOICE":
                extractFromInvoice(text, normalizedText, info);
                break;
            case "WARRANTY_CARD":
                extractFromWarrantyCard(text, normalizedText, info);
                break;
            case "AMC_DOCUMENT":
                extractFromAmcDocument(text, normalizedText, info);
                break;
            case "SPEC_SHEET":
                extractFromSpecSheet(text, normalizedText, info);
                break;
            default:
                extractGenericInfo(text, normalizedText, info);
        }
        
        return info;
    }

    // ============================================================
    // 🏷️ EXTRACT BASIC PRODUCT INFO
    // ============================================================
    private void extractBasicProductInfo(String text, String normalizedText, 
                                        IntelligentExtractionResponse.ExtractedAssetInfo info) {
        // Make/Brand
        String make = extractMake(text, normalizedText);
        info.setMakeName(make);
        info.setBrand(make);
        info.setManufacturer(make);

        // Model
        String model = extractModel(text, normalizedText, make);
        info.setModelName(model);

        // Serial Number
        String serial = extractSerialNumber(text);
        info.setSerialNumber(serial);

        // Category
        String category = extractCategory(text, normalizedText);
        info.setCategoryName(category);

        // SubCategory
        String subCategory = extractSubCategory(text, normalizedText);
        info.setSubCategoryName(subCategory);

        // Asset Name (combine make + model if available)
        if (make != null && model != null) {
            info.setAssetName(make + " " + model);
        } else if (make != null) {
            info.setAssetName(make);
        } else if (model != null) {
            info.setAssetName(model);
        }
    }

    // ============================================================
    // 🧾 EXTRACT FROM INVOICE
    // ============================================================
    private void extractFromInvoice(String text, String normalizedText, 
                                   IntelligentExtractionResponse.ExtractedAssetInfo info) {
        // Invoice Number
        Pattern invoicePattern = Pattern.compile(
            "(?:invoice\\s*(?:no|number|#)?|invoice\\s*id)[\\s:]*([A-Z0-9-]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = invoicePattern.matcher(text);
        if (matcher.find()) {
            info.setInvoiceNumber(matcher.group(1).trim());
        }

        // Bill Number
        Pattern billPattern = Pattern.compile(
            "(?:bill\\s*(?:no|number|#)?|bill\\s*id)[\\s:]*([A-Z0-9-]+)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = billPattern.matcher(text);
        if (matcher.find()) {
            info.setBillNumber(matcher.group(1).trim());
        }

        // PO Number
        Pattern poPattern = Pattern.compile(
            "(?:po\\s*(?:no|number|#)?|purchase\\s*order)[\\s:]*([A-Z0-9-]+)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = poPattern.matcher(text);
        if (matcher.find()) {
            info.setPoNumber(matcher.group(1).trim());
        }

        // GRN Number
        Pattern grnPattern = Pattern.compile(
            "(?:grn\\s*(?:no|number|#)?|goods\\s*receipt)[\\s:]*([A-Z0-9-]+)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = grnPattern.matcher(text);
        if (matcher.find()) {
            info.setGrnNumber(matcher.group(1).trim());
        }

        // Invoice Date
        Pattern datePattern = Pattern.compile(
            "(?:invoice\\s*date|bill\\s*date|date)[\\s:]+([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}|[0-9]{4}[/-][0-9]{1,2}[/-][0-9]{1,2})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = datePattern.matcher(text);
        if (matcher.find()) {
            info.setInvoiceDate(matcher.group(1).trim());
            info.setPurchaseDate(matcher.group(1).trim());
        }

        // Bill Date
        Pattern billDatePattern = Pattern.compile(
            "(?:bill\\s*date)[\\s:]+([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}|[0-9]{4}[/-][0-9]{1,2}[/-][0-9]{1,2})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = billDatePattern.matcher(text);
        if (matcher.find()) {
            info.setBillDate(matcher.group(1).trim());
        }

        // Quantity
        Pattern quantityPattern = Pattern.compile(
            "(?:qty|quantity|qty\\.)[\\s:]*([0-9]+)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = quantityPattern.matcher(text);
        if (matcher.find()) {
            info.setQuantity(matcher.group(1).trim());
        }

        // Unit Price
        Pattern unitPricePattern = Pattern.compile(
            "(?:unit\\s*price|rate|price\\s*per\\s*unit)[\\s:]*([0-9,]+(?:\\.[0-9]{2})?)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = unitPricePattern.matcher(text);
        if (matcher.find()) {
            info.setUnitPrice(matcher.group(1).trim());
        }

        // Purchase Price / Total Amount
        Pattern pricePattern = Pattern.compile(
            "(?:total|amount|price|cost|rs\\.?|inr|usd|\\$|grand\\s*total)[\\s:]*([0-9,]+(?:\\.[0-9]{2})?)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = pricePattern.matcher(text);
        if (matcher.find()) {
            info.setPurchasePrice(matcher.group(1).trim());
            info.setFinalAmount(matcher.group(1).trim());
        }

        // Discount Amount
        Pattern discountAmountPattern = Pattern.compile(
            "(?:discount|disc\\.)[\\s:]*([0-9,]+(?:\\.[0-9]{2})?)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = discountAmountPattern.matcher(text);
        if (matcher.find()) {
            info.setDiscountAmount(matcher.group(1).trim());
        }

        // Discount Percentage
        Pattern discountPercentPattern = Pattern.compile(
            "(?:discount|disc\\.)[\\s:]*([0-9]+(?:\\.[0-9]+)?)[\\s]*%",
            Pattern.CASE_INSENSITIVE
        );
        matcher = discountPercentPattern.matcher(text);
        if (matcher.find()) {
            info.setDiscountPercentage(matcher.group(1).trim());
        }

        // Tax Amount
        Pattern taxAmountPattern = Pattern.compile(
            "(?:tax|gst|vat)[\\s:]*([0-9,]+(?:\\.[0-9]{2})?)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = taxAmountPattern.matcher(text);
        if (matcher.find()) {
            info.setTaxAmount(matcher.group(1).trim());
        }

        // Tax Rate / GST Rate
        Pattern taxRatePattern = Pattern.compile(
            "(?:gst|tax|vat)[\\s:]*([0-9]+(?:\\.[0-9]+)?)[\\s]*%",
            Pattern.CASE_INSENSITIVE
        );
        matcher = taxRatePattern.matcher(text);
        if (matcher.find()) {
            info.setTaxRate(matcher.group(1).trim());
        }

        // CGST Amount
        Pattern cgstAmountPattern = Pattern.compile(
            "(?:cgst)[\\s:]*([0-9,]+(?:\\.[0-9]{2})?)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = cgstAmountPattern.matcher(text);
        if (matcher.find()) {
            info.setCgstAmount(matcher.group(1).trim());
        }

        // SGST Amount
        Pattern sgstAmountPattern = Pattern.compile(
            "(?:sgst)[\\s:]*([0-9,]+(?:\\.[0-9]{2})?)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = sgstAmountPattern.matcher(text);
        if (matcher.find()) {
            info.setSgstAmount(matcher.group(1).trim());
        }

        // IGST Amount
        Pattern igstAmountPattern = Pattern.compile(
            "(?:igst)[\\s:]*([0-9,]+(?:\\.[0-9]{2})?)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = igstAmountPattern.matcher(text);
        if (matcher.find()) {
            info.setIgstAmount(matcher.group(1).trim());
        }

        // CGST Rate
        Pattern cgstRatePattern = Pattern.compile(
            "(?:cgst)[\\s:]*([0-9]+(?:\\.[0-9]+)?)[\\s]*%",
            Pattern.CASE_INSENSITIVE
        );
        matcher = cgstRatePattern.matcher(text);
        if (matcher.find()) {
            info.setCgstRate(matcher.group(1).trim());
        }

        // SGST Rate
        Pattern sgstRatePattern = Pattern.compile(
            "(?:sgst)[\\s:]*([0-9]+(?:\\.[0-9]+)?)[\\s]*%",
            Pattern.CASE_INSENSITIVE
        );
        matcher = sgstRatePattern.matcher(text);
        if (matcher.find()) {
            info.setSgstRate(matcher.group(1).trim());
        }

        // IGST Rate
        Pattern igstRatePattern = Pattern.compile(
            "(?:igst)[\\s:]*([0-9]+(?:\\.[0-9]+)?)[\\s]*%",
            Pattern.CASE_INSENSITIVE
        );
        matcher = igstRatePattern.matcher(text);
        if (matcher.find()) {
            info.setIgstRate(matcher.group(1).trim());
        }

        // HSN Code
        Pattern hsnPattern = Pattern.compile(
            "(?:hsn|hsn\\s*code)[\\s:]*([0-9]{4,8})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = hsnPattern.matcher(text);
        if (matcher.find()) {
            info.setHsnCode(matcher.group(1).trim());
        }

        // SAC Code
        Pattern sacPattern = Pattern.compile(
            "(?:sac|sac\\s*code)[\\s:]*([0-9]{6})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = sacPattern.matcher(text);
        if (matcher.find()) {
            info.setSacCode(matcher.group(1).trim());
        }

        // SKU / Part Number
        Pattern skuPattern = Pattern.compile(
            "(?:sku|part\\s*no|part\\s*number|item\\s*code)[\\s:]*([A-Z0-9-]+)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = skuPattern.matcher(text);
        if (matcher.find()) {
            info.setSku(matcher.group(1).trim());
            info.setPartNumber(matcher.group(1).trim());
        }

        // Batch Number
        Pattern batchPattern = Pattern.compile(
            "(?:batch|batch\\s*no|batch\\s*number|lot)[\\s:]*([A-Z0-9-]+)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = batchPattern.matcher(text);
        if (matcher.find()) {
            info.setBatchNumber(matcher.group(1).trim());
        }

        // Vendor GSTIN
        Pattern gstinPattern = Pattern.compile(
            "(?:gstin|gst\\s*in)[\\s:]*([0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = gstinPattern.matcher(text);
        if (matcher.find()) {
            info.setVendorGstin(matcher.group(1).trim().toUpperCase());
        }

        // Vendor PAN
        Pattern panPattern = Pattern.compile(
            "(?:pan|pan\\s*no)[\\s:]*([A-Z]{5}[0-9]{4}[A-Z]{1})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = panPattern.matcher(text);
        if (matcher.find()) {
            info.setVendorPan(matcher.group(1).trim().toUpperCase());
        }

        // Payment Method
        Pattern paymentMethodPattern = Pattern.compile(
            "(?:payment\\s*method|paid\\s*by|mode\\s*of\\s*payment)[\\s:]+([A-Za-z]+)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = paymentMethodPattern.matcher(text);
        if (matcher.find()) {
            info.setPaymentMethod(matcher.group(1).trim());
        }

        // Payment Status
        if (normalizedText.contains("PAID") || normalizedText.contains("PAYMENT RECEIVED")) {
            info.setPaymentStatus("PAID");
        } else if (normalizedText.contains("PENDING") || normalizedText.contains("DUE")) {
            info.setPaymentStatus("PENDING");
        }

        // Payment Date
        Pattern paymentDatePattern = Pattern.compile(
            "(?:payment\\s*date|paid\\s*on)[\\s:]+([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}|[0-9]{4}[/-][0-9]{1,2}[/-][0-9]{1,2})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = paymentDatePattern.matcher(text);
        if (matcher.find()) {
            info.setPaymentDate(matcher.group(1).trim());
        }

        // Due Date
        Pattern dueDatePattern = Pattern.compile(
            "(?:due\\s*date|payment\\s*due)[\\s:]+([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}|[0-9]{4}[/-][0-9]{1,2}[/-][0-9]{1,2})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = dueDatePattern.matcher(text);
        if (matcher.find()) {
            info.setDueDate(matcher.group(1).trim());
        }

        // Currency
        Pattern currencyPattern = Pattern.compile(
            "(?:currency|curr)[\\s:]+([A-Z]{3})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = currencyPattern.matcher(text);
        if (matcher.find()) {
            info.setCurrency(matcher.group(1).trim().toUpperCase());
        } else if (normalizedText.contains("INR") || normalizedText.contains("RS") || normalizedText.contains("RUPEES")) {
            info.setCurrency("INR");
        } else if (normalizedText.contains("USD") || normalizedText.contains("$")) {
            info.setCurrency("USD");
        }

        // Vendor/Supplier - Enhanced patterns
        Pattern vendorPattern = Pattern.compile(
            "(?:(?:vendor|supplier|seller|dealer|distributor|manufacturer|company|firm|from|sold by|purchased from|bought from)[\\s:]+)([A-Z][A-Za-z0-9\\s&.,-]{2,50})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = vendorPattern.matcher(text);
        if (matcher.find()) {
            String vendorName = matcher.group(1).trim();
            // Clean up common prefixes/suffixes
            vendorName = vendorName.replaceAll("^(?:from|by|via)\\s+", "").trim();
            if (vendorName.length() > 2 && vendorName.length() < 100) {
                info.setVendorName(vendorName);
            }
        }
        
        // Try alternative vendor patterns (company name at top of invoice)
        if (info.getVendorName() == null) {
            Pattern companyPattern = Pattern.compile(
                "^([A-Z][A-Za-z0-9\\s&.,-]{3,50})(?:\\s+(?:Ltd|Limited|Inc|Corporation|Corp|LLC|Pvt|Private))?",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
            );
            matcher = companyPattern.matcher(text);
            if (matcher.find()) {
                String companyName = matcher.group(1).trim();
                if (companyName.length() > 2 && companyName.length() < 100) {
                    info.setVendorName(companyName);
                }
            }
        }

        // Outlet/Store - Enhanced patterns
        Pattern outletPattern = Pattern.compile(
            "(?:(?:outlet|store|shop|branch|location|retailer|showroom|dealer|center|centre)[\\s:]+)([A-Z][A-Za-z0-9\\s.,-]{2,50})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = outletPattern.matcher(text);
        if (matcher.find()) {
            String outletName = matcher.group(1).trim();
            // Clean up common prefixes
            outletName = outletName.replaceAll("^(?:at|from|in)\\s+", "").trim();
            if (outletName.length() > 2 && outletName.length() < 100) {
                info.setOutletName(outletName);
            }
        }
        
        // Try alternative outlet patterns (address-based)
        if (info.getOutletName() == null) {
            Pattern addressPattern = Pattern.compile(
                "(?:address|location|branch)[\\s:]+([A-Z][A-Za-z0-9\\s,.-]{5,80})",
                Pattern.CASE_INSENSITIVE
            );
            matcher = addressPattern.matcher(text);
            if (matcher.find()) {
                String address = matcher.group(1).trim();
                // Extract first meaningful part as outlet name
                String[] parts = address.split("[,;]");
                if (parts.length > 0 && parts[0].trim().length() > 3) {
                    info.setOutletName(parts[0].trim());
                }
            }
        }
    }

    // ============================================================
    // 🛡️ EXTRACT FROM WARRANTY CARD
    // ============================================================
    private void extractFromWarrantyCard(String text, String normalizedText,
                                        IntelligentExtractionResponse.ExtractedAssetInfo info) {
        IntelligentExtractionResponse.WarrantyInfo warrantyInfo = 
            new IntelligentExtractionResponse.WarrantyInfo();
        
        // Warranty Status
        if (normalizedText.contains("ACTIVE") || normalizedText.contains("VALID")) {
            warrantyInfo.setWarrantyStatus("ACTIVE");
        } else if (normalizedText.contains("EXPIRED") || normalizedText.contains("INVALID")) {
            warrantyInfo.setWarrantyStatus("EXPIRED");
        } else {
            warrantyInfo.setWarrantyStatus("ACTIVE"); // Default
        }

        // Warranty Provider
        Pattern providerPattern = Pattern.compile(
            "(?:warranty provider|provider|issued by|warranty by)[\\s:]+([A-Z][A-Za-z0-9\\s&]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = providerPattern.matcher(text);
        if (matcher.find()) {
            warrantyInfo.setWarrantyProvider(matcher.group(1).trim());
        }

        // Warranty Start Date
        Pattern startDatePattern = Pattern.compile(
            "(?:warranty start|start date|from|valid from|effective from)[\\s:]+([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}|[0-9]{4}[/-][0-9]{1,2}[/-][0-9]{1,2})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = startDatePattern.matcher(text);
        if (matcher.find()) {
            warrantyInfo.setStartDate(matcher.group(1).trim());
        }

        // Warranty End Date
        Pattern endDatePattern = Pattern.compile(
            "(?:warranty end|end date|expiry|expires|valid until|valid till)[\\s:]+([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}|[0-9]{4}[/-][0-9]{1,2}[/-][0-9]{1,2})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = endDatePattern.matcher(text);
        if (matcher.find()) {
            warrantyInfo.setEndDate(matcher.group(1).trim());
        }

        // Warranty Duration
        Pattern durationPattern = Pattern.compile(
            "(?:warranty period|duration|valid for)[\\s:]+([0-9]+)[\\s]*(?:year|years|month|months|day|days)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = durationPattern.matcher(text);
        if (matcher.find()) {
            warrantyInfo.setDuration(matcher.group(1).trim() + " " + 
                (normalizedText.contains("YEAR") ? "years" : "months"));
        }

        // Warranty Terms
        Pattern termsPattern = Pattern.compile(
            "(?:terms|conditions|coverage|warranty covers)[\\s:]+([A-Za-z0-9\\s,.-]+)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = termsPattern.matcher(text);
        if (matcher.find()) {
            warrantyInfo.setWarrantyTerms(matcher.group(1).trim());
        }

        info.setWarrantyInfo(warrantyInfo);
    }

    // ============================================================
    // 🔧 EXTRACT FROM AMC DOCUMENT
    // ============================================================
    private void extractFromAmcDocument(String text, String normalizedText,
                                        IntelligentExtractionResponse.ExtractedAssetInfo info) {
        IntelligentExtractionResponse.AmcInfo amcInfo = 
            new IntelligentExtractionResponse.AmcInfo();
        
        // AMC Status
        if (normalizedText.contains("ACTIVE") || normalizedText.contains("VALID")) {
            amcInfo.setAmcStatus("ACTIVE");
        } else if (normalizedText.contains("EXPIRED") || normalizedText.contains("INVALID")) {
            amcInfo.setAmcStatus("EXPIRED");
        } else {
            amcInfo.setAmcStatus("ACTIVE"); // Default
        }

        // AMC Provider
        Pattern providerPattern = Pattern.compile(
            "(?:amc provider|service provider|maintained by|service by)[\\s:]+([A-Z][A-Za-z0-9\\s&]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = providerPattern.matcher(text);
        if (matcher.find()) {
            amcInfo.setProvider(matcher.group(1).trim());
        }

        // AMC Start Date
        Pattern startDatePattern = Pattern.compile(
            "(?:amc start|start date|from|valid from|effective from|contract start)[\\s:]+([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}|[0-9]{4}[/-][0-9]{1,2}[/-][0-9]{1,2})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = startDatePattern.matcher(text);
        if (matcher.find()) {
            amcInfo.setStartDate(matcher.group(1).trim());
        }

        // AMC End Date
        Pattern endDatePattern = Pattern.compile(
            "(?:amc end|end date|expiry|expires|valid until|contract end)[\\s:]+([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}|[0-9]{4}[/-][0-9]{1,2}[/-][0-9]{1,2})",
            Pattern.CASE_INSENSITIVE
        );
        matcher = endDatePattern.matcher(text);
        if (matcher.find()) {
            amcInfo.setEndDate(matcher.group(1).trim());
        }

        // AMC Duration
        Pattern durationPattern = Pattern.compile(
            "(?:amc period|duration|valid for|contract period)[\\s:]+([0-9]+)[\\s]*(?:year|years|month|months)",
            Pattern.CASE_INSENSITIVE
        );
        matcher = durationPattern.matcher(text);
        if (matcher.find()) {
            amcInfo.setDuration(matcher.group(1).trim() + " " + 
                (normalizedText.contains("YEAR") ? "years" : "months"));
        }

        info.setAmcInfo(amcInfo);
    }

    // ============================================================
    // 📋 EXTRACT FROM SPEC SHEET
    // ============================================================
    private void extractFromSpecSheet(String text, String normalizedText,
                                     IntelligentExtractionResponse.ExtractedAssetInfo info) {
        // Extract components from spec sheet
        List<String> components = new ArrayList<>();
        
        for (String component : COMMON_COMPONENTS) {
            Pattern componentPattern = Pattern.compile(
                "\\b" + Pattern.quote(component) + "[\\s:]+([A-Za-z0-9\\s]+)",
                Pattern.CASE_INSENSITIVE
            );
            Matcher matcher = componentPattern.matcher(text);
            if (matcher.find()) {
                components.add(component);
            }
        }

        // Extract description
        Pattern descPattern = Pattern.compile(
            "(?:description|specification|details|overview)[\\s:]+([A-Za-z0-9\\s.,-]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = descPattern.matcher(text);
        if (matcher.find()) {
            String desc = matcher.group(1).trim();
            if (desc.length() > 500) {
                desc = desc.substring(0, 500);
            }
            info.setDescription(desc);
        }

        if (!components.isEmpty()) {
            info.setComponentNames(components);
        }
    }

    // ============================================================
    // 📄 EXTRACT GENERIC INFO
    // ============================================================
    private void extractGenericInfo(String text, String normalizedText,
                                   IntelligentExtractionResponse.ExtractedAssetInfo info) {
        // Try to extract any available information using generic patterns
        extractFromInvoice(text, normalizedText, info);
        extractFromWarrantyCard(text, normalizedText, info);
        extractFromAmcDocument(text, normalizedText, info);
        
        // Extract vendor and outlet from any document type if not already extracted
        if (!StringUtils.hasText(info.getVendorName())) {
            extractVendorGeneric(text, normalizedText, info);
        }
        if (!StringUtils.hasText(info.getOutletName())) {
            extractOutletGeneric(text, normalizedText, info);
        }
    }
    
    // ============================================================
    // 🏢 EXTRACT VENDOR (GENERIC)
    // ============================================================
    private void extractVendorGeneric(String text, String normalizedText,
                                      IntelligentExtractionResponse.ExtractedAssetInfo info) {
        // Try various vendor patterns
        Pattern[] vendorPatterns = {
            Pattern.compile("(?:vendor|supplier|seller|dealer|distributor|manufacturer|company|firm)[\\s:]+([A-Z][A-Za-z0-9\\s&.,-]{2,50})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^([A-Z][A-Za-z0-9\\s&.,-]{3,50})(?:\\s+(?:Ltd|Limited|Inc|Corporation|Corp|LLC|Pvt|Private))?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("(?:from|by|via|purchased from|bought from)[\\s:]+([A-Z][A-Za-z0-9\\s&.,-]{2,50})", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : vendorPatterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String vendorName = matcher.group(1).trim();
                vendorName = vendorName.replaceAll("^(?:from|by|via)\\s+", "").trim();
                if (vendorName.length() > 2 && vendorName.length() < 100) {
                    info.setVendorName(vendorName);
                    break;
                }
            }
        }
    }
    
    // ============================================================
    // 🏬 EXTRACT OUTLET (GENERIC)
    // ============================================================
    private void extractOutletGeneric(String text, String normalizedText,
                                     IntelligentExtractionResponse.ExtractedAssetInfo info) {
        // Try various outlet patterns
        Pattern[] outletPatterns = {
            Pattern.compile("(?:outlet|store|shop|branch|location|retailer|showroom|dealer|center|centre)[\\s:]+([A-Z][A-Za-z0-9\\s.,-]{2,50})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:address|location|branch)[\\s:]+([A-Z][A-Za-z0-9\\s,.-]{5,80})", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : outletPatterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String outletName = matcher.group(1).trim();
                outletName = outletName.replaceAll("^(?:at|from|in)\\s+", "").trim();
                
                // If it's an address, extract first meaningful part
                if (outletName.contains(",")) {
                    String[] parts = outletName.split("[,;]");
                    if (parts.length > 0 && parts[0].trim().length() > 3) {
                        outletName = parts[0].trim();
                    }
                }
                
                if (outletName.length() > 2 && outletName.length() < 100) {
                    info.setOutletName(outletName);
                    break;
                }
            }
        }
    }

    // ============================================================
    // 🔍 EXTRACTION HELPERS (Reuse from ProductOcrAiAgentService patterns)
    // ============================================================
    private String extractMake(String text, String normalizedText) {
        // Use learned patterns first
        String learnedMake = extractUsingLearnedPatterns("MAKE", text, null, null);
        if (learnedMake != null) return learnedMake;

        // Common makes
        Set<String> commonMakes = Set.of(
            "Dell", "HP", "Lenovo", "Apple", "Samsung", "Sony", "LG", "Panasonic",
            "Microsoft", "Asus", "Acer", "Toshiba", "Fujitsu", "IBM", "Cisco"
        );
        
        for (String make : commonMakes) {
            if (normalizedText.contains(make.toUpperCase())) {
                return make;
            }
        }

        // Pattern matching
        Pattern makePattern = Pattern.compile(
            "(?:brand|make|manufacturer|company)[\\s:]+([A-Z][A-Za-z0-9\\s&-]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = makePattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String extractModel(String text, String normalizedText, String make) {
        // Use learned patterns first
        String learnedModel = extractUsingLearnedPatterns("MODEL", text, null, null);
        if (learnedModel != null) return learnedModel;

        Pattern modelPattern = Pattern.compile(
            "(?:model|model\\s*no|model\\s*number|type|variant)[\\s:]+([A-Za-z0-9\\s-]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = modelPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        if (make != null) {
            Pattern afterMakePattern = Pattern.compile(
                Pattern.quote(make) + "[\\s]+([A-Za-z0-9\\s-]{2,30})",
                Pattern.CASE_INSENSITIVE
            );
            matcher = afterMakePattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }

        return null;
    }

    private String extractSerialNumber(String text) {
        Pattern serialPattern = Pattern.compile(
            "(?:serial|s/n|sn|serial\\s*number)[\\s:]+([A-Z0-9-]{4,30})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = serialPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim().toUpperCase();
        }

        Pattern longCodePattern = Pattern.compile("\\b([A-Z0-9]{8,30})\\b");
        matcher = longCodePattern.matcher(text.toUpperCase());
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String extractCategory(String text, String normalizedText) {
        Pattern categoryPattern = Pattern.compile(
            "(?:category|type|class)[\\s:]+([A-Za-z\\s]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = categoryPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        String[] categories = {"Electronics", "Computer", "Laptop", "Desktop", "Phone", "Mobile", 
                              "Tablet", "Monitor", "Printer", "Scanner", "Camera", "TV"};
        for (String cat : categories) {
            if (normalizedText.contains(cat.toUpperCase())) {
                return cat;
            }
        }

        return null;
    }

    private String extractSubCategory(String text, String normalizedText) {
        Pattern subCategoryPattern = Pattern.compile(
            "(?:subcategory|sub\\s*category|subtype)[\\s:]+([A-Za-z\\s]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = subCategoryPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

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
                        String extracted = matcher.group(1);
                        if (extracted != null && !extracted.trim().isEmpty()) {
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

    // ============================================================
    // 🏗️ CREATE OR RESOLVE ENTITIES
    // ============================================================
    private void createOrResolveEntities(HttpHeaders headers, IntelligentExtractionResponse response,
                                        IntelligentExtractionRequest request) {
        IntelligentExtractionResponse.ExtractedAssetInfo info = response.getAssetInfo();
        
        // Category
        if (StringUtils.hasText(info.getCategoryName())) {
            ProductCategory category = resolveOrCreateCategory(headers, info.getCategoryName(), request);
            response.setCategory(category);
            response.setCategoryCreated(category != null && category.getCategoryId() != null);
        }

        // SubCategory
        if (StringUtils.hasText(info.getSubCategoryName())) {
            ProductSubCategory subCategory = resolveOrCreateSubCategory(
                headers, info.getSubCategoryName(), response.getCategory(), request);
            response.setSubCategory(subCategory);
            response.setSubCategoryCreated(subCategory != null && subCategory.getSubCategoryId() != null);
        }

        // Make
        if (StringUtils.hasText(info.getMakeName())) {
            ProductMake make = resolveOrCreateMake(headers, info.getMakeName(), response.getSubCategory(), request);
            response.setMake(make);
            response.setMakeCreated(make != null && make.getMakeId() != null);
        }

        // Model
        if (StringUtils.hasText(info.getModelName()) && response.getMake() != null) {
            ProductModel model = resolveOrCreateModel(headers, info.getModelName(), response.getMake(), request);
            response.setModel(model);
            response.setModelCreated(model != null && model.getModelId() != null);
        }
    }

    // ============================================================
    // 🏷️ RESOLVE OR CREATE CATEGORY
    // ============================================================
    private ProductCategory resolveOrCreateCategory(HttpHeaders headers, String categoryName,
                                                   IntelligentExtractionRequest request) {
        Optional<ProductCategory> existing = categoryRepo.findByCategoryNameIgnoreCase(categoryName)
            .filter(c -> c.getActive() == null || c.getActive());

        if (existing.isPresent()) {
            return existing.get();
        }

        if (request.getAutoCreateEntities()) {
            try {
                CategoryRequest categoryRequest = new CategoryRequest();
                categoryRequest.setUserId(request.getUserId());
                categoryRequest.setUsername(request.getUsername());
                categoryRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
                
                ProductCategory newCategory = new ProductCategory();
                newCategory.setCategoryName(categoryName);
                categoryRequest.setCategory(newCategory);

                com.example.asset.dto.CategoryDto createdDto = categoryService.create(headers, categoryRequest);
                Long categoryId = createdDto.getCategoryId();
                if (categoryId != null) {
                    return categoryRepo.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Failed to retrieve created category"));
                }
            } catch (Exception e) {
                log.error("❌ Failed to create category '{}': {}", categoryName, e.getMessage());
            }
        }

        return null;
    }

    // ============================================================
    // 📁 RESOLVE OR CREATE SUBCATEGORY
    // ============================================================
    private ProductSubCategory resolveOrCreateSubCategory(HttpHeaders headers, String subCategoryName,
                                                         ProductCategory category, IntelligentExtractionRequest request) {
        Optional<ProductSubCategory> existing;
        if (category != null) {
            Long categoryId = category.getCategoryId();
            if (categoryId != null) {
                existing = subCategoryRepo.findBySubCategoryNameIgnoreCase(subCategoryName)
                    .filter(s -> s.getActive() == null || s.getActive())
                    .filter(s -> s.getCategory() != null && categoryId.equals(s.getCategory().getCategoryId()));
            } else {
                existing = subCategoryRepo.findBySubCategoryNameIgnoreCase(subCategoryName)
                    .filter(s -> s.getActive() == null || s.getActive());
            }
        } else {
            existing = subCategoryRepo.findBySubCategoryNameIgnoreCase(subCategoryName)
                .filter(s -> s.getActive() == null || s.getActive());
        }

        if (existing.isPresent()) {
            return existing.get();
        }

        if (request.getAutoCreateEntities()) {
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
                return created;
            } catch (Exception e) {
                log.error("❌ Failed to create subcategory '{}': {}", subCategoryName, e.getMessage());
            }
        }

        return null;
    }

    // ============================================================
    // 🏷️ RESOLVE OR CREATE MAKE
    // ============================================================
    private ProductMake resolveOrCreateMake(HttpHeaders headers, String makeName,
                                           ProductSubCategory subCategory, IntelligentExtractionRequest request) {
        Optional<ProductMake> existing;
        if (subCategory != null) {
            Long subCategoryId = subCategory.getSubCategoryId();
            if (subCategoryId != null) {
                existing = makeRepo.findByMakeNameIgnoreCase(makeName)
                    .filter(m -> m.getActive() == null || m.getActive())
                    .filter(m -> m.getSubCategory() != null && 
                               subCategoryId.equals(m.getSubCategory().getSubCategoryId()));
            } else {
                existing = makeRepo.findByMakeNameIgnoreCase(makeName)
                    .filter(m -> m.getActive() == null || m.getActive());
            }
        } else {
            existing = makeRepo.findByMakeNameIgnoreCase(makeName)
                .filter(m -> m.getActive() == null || m.getActive());
        }

        if (existing.isPresent()) {
            return existing.get();
        }

        if (request.getAutoCreateEntities()) {
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
                return created;
            } catch (Exception e) {
                log.error("❌ Failed to create make '{}': {}", makeName, e.getMessage());
            }
        }

        return null;
    }

    // ============================================================
    // 📱 RESOLVE OR CREATE MODEL
    // ============================================================
    private ProductModel resolveOrCreateModel(HttpHeaders headers, String modelName,
                                            ProductMake make, IntelligentExtractionRequest request) {
        Long makeId = make.getMakeId();
        if (makeId == null) {
            return null;
        }

        Optional<ProductModel> existing = modelRepo
            .findByModelNameIgnoreCaseAndMake_MakeId(modelName, makeId)
            .filter(m -> m.getActive() == null || m.getActive());

        if (existing.isPresent()) {
            return existing.get();
        }

        if (request.getAutoCreateEntities()) {
            try {
                ModelRequest modelRequest = new ModelRequest();
                modelRequest.setUserId(request.getUserId());
                modelRequest.setUsername(request.getUsername());
                modelRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
                
                ProductModel newModel = new ProductModel();
                newModel.setModelName(modelName);
                newModel.setMake(make);
                modelRequest.setModel(newModel);

                com.example.asset.dto.ModelDto createdDto = modelService.create(headers, modelRequest);
                Long modelId = createdDto.getModelId();
                if (modelId != null) {
                    return modelRepo.findById(modelId)
                        .orElseThrow(() -> new RuntimeException("Failed to retrieve created model"));
                }
            } catch (Exception e) {
                log.error("❌ Failed to create model '{}': {}", modelName, e.getMessage());
            }
        }

        return null;
    }

    // ============================================================
    // 🔧 CREATE COMPONENTS
    // ============================================================
    private void createComponents(HttpHeaders headers, IntelligentExtractionResponse response,
                                 IntelligentExtractionRequest request) {
        List<String> componentNames = response.getAssetInfo().getComponentNames();
        if (componentNames == null || componentNames.isEmpty()) {
            return;
        }

        List<AssetComponent> components = new ArrayList<>();
        for (String componentName : componentNames) {
            try {
                AssetComponent component = resolveOrCreateComponent(headers, componentName, request);
                if (component != null) {
                    components.add(component);
                }
            } catch (Exception e) {
                log.warn("⚠️ Failed to create component '{}': {}", componentName, e.getMessage());
            }
        }

        response.setComponents(components);
    }

    private AssetComponent resolveOrCreateComponent(HttpHeaders headers, String componentName,
                                                   IntelligentExtractionRequest request) {
        Optional<AssetComponent> existing = componentRepo.findByComponentNameIgnoreCase(componentName)
            .filter(c -> c.getActive() == null || c.getActive());

        if (existing.isPresent()) {
            return existing.get();
        }

        if (request.getAutoCreateEntities()) {
            try {
                ComponentRequest componentRequest = new ComponentRequest();
                componentRequest.setUserId(request.getUserId());
                componentRequest.setUsername(request.getUsername());
                componentRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
                
                AssetComponent newComponent = new AssetComponent();
                newComponent.setComponentName(componentName);
                componentRequest.setComponent(newComponent);

                AssetComponent created = componentService.create(headers, componentRequest);
                return created;
            } catch (Exception e) {
                log.error("❌ Failed to create component '{}': {}", componentName, e.getMessage());
            }
        }

        return null;
    }

    // ============================================================
    // 🛡️ CREATE WARRANTY
    // ============================================================
    private void createWarranty(HttpHeaders headers, IntelligentExtractionResponse response,
                              IntelligentExtractionRequest request) {
        IntelligentExtractionResponse.WarrantyInfo warrantyInfo = response.getAssetInfo().getWarrantyInfo();
        if (warrantyInfo == null) {
            return;
        }

        // Need asset ID to create warranty
        Long assetId = null;
        if (response.getAsset() != null && response.getAsset().getAssetId() != null) {
            assetId = response.getAsset().getAssetId();
        } else if (request.getExistingAssetId() != null) {
            assetId = request.getExistingAssetId();
        }
        
        if (assetId == null) {
            log.warn("⚠️ Cannot create warranty: Asset ID is required");
            return;
        }

        try {
            AssetWarrantyRequest warrantyRequest = new AssetWarrantyRequest();
            warrantyRequest.setUserId(request.getUserId());
            warrantyRequest.setUsername(request.getUsername());
            warrantyRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
            warrantyRequest.setAssetId(assetId);
            warrantyRequest.setWarrantyStatus(warrantyInfo.getWarrantyStatus() != null ? 
                warrantyInfo.getWarrantyStatus() : "ACTIVE");
            warrantyRequest.setWarrantyProvider(warrantyInfo.getWarrantyProvider());
            warrantyRequest.setWarrantyTerms(warrantyInfo.getWarrantyTerms());
            warrantyRequest.setStartDate(parseDate(warrantyInfo.getStartDate()));
            warrantyRequest.setEndDate(parseDate(warrantyInfo.getEndDate()));

            com.example.asset.dto.AssetWarrantyDto createdDto = warrantyService.create(headers, warrantyRequest, null);
            // Fetch the created warranty entity
            if (createdDto != null && createdDto.getWarrantyId() != null) {
                warrantyRepo.findByWarrantyId(createdDto.getWarrantyId())
                    .ifPresent(warranty -> {
                        response.setWarranty(warranty);
                        response.setWarrantyCreated(true);
                        log.info("✅ Warranty created successfully: warrantyId={}", warranty.getWarrantyId());
                    });
            }
        } catch (Exception e) {
            log.error("❌ Failed to create warranty: {}", e.getMessage());
        }
    }

    // ============================================================
    // 🔧 CREATE AMC
    // ============================================================
    private void createAmc(HttpHeaders headers, IntelligentExtractionResponse response,
                          IntelligentExtractionRequest request) {
        IntelligentExtractionResponse.AmcInfo amcInfo = response.getAssetInfo().getAmcInfo();
        if (amcInfo == null) {
            return;
        }

        // Need asset ID to create AMC
        Long assetId = response.getAsset() != null ? response.getAsset().getAssetId() : request.getExistingAssetId();
        if (assetId == null) {
            log.warn("⚠️ Cannot create AMC: Asset ID is required");
            return;
        }

        try {
            AssetAmcRequest amcRequest = new AssetAmcRequest();
            amcRequest.setUserId(request.getUserId());
            amcRequest.setUsername(request.getUsername());
            amcRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
            amcRequest.setAssetId(assetId);
            amcRequest.setAmcStatus(amcInfo.getAmcStatus() != null ? amcInfo.getAmcStatus() : "ACTIVE");
            amcRequest.setStartDate(parseLocalDate(amcInfo.getStartDate()));
            amcRequest.setEndDate(parseLocalDate(amcInfo.getEndDate()));

            com.example.asset.dto.AssetAmcDto createdDto = amcService.create(headers, amcRequest, null);
            // Fetch the created AMC entity
            if (createdDto != null && createdDto.getAmcId() != null) {
                amcRepo.findByAmcId(createdDto.getAmcId())
                    .ifPresent(amc -> {
                        response.setAmc(amc);
                        response.setAmcCreated(true);
                        log.info("✅ AMC created successfully: amcId={}", amc.getAmcId());
                    });
            }
        } catch (Exception e) {
            log.error("❌ Failed to create AMC: {}", e.getMessage());
        }
    }

    // ============================================================
    // 📦 CREATE ASSET
    // ============================================================
    private void createAsset(HttpHeaders headers, IntelligentExtractionResponse response,
                            IntelligentExtractionRequest request) {
        IntelligentExtractionResponse.ExtractedAssetInfo info = response.getAssetInfo();
        
        // Check if we have minimum required info
        if (!StringUtils.hasText(info.getAssetName()) && !StringUtils.hasText(info.getSerialNumber())) {
            log.warn("⚠️ Cannot create asset: Missing asset name or serial number");
            return;
        }

        // Use existing asset if provided
        Long existingAssetId = request.getExistingAssetId();
        if (existingAssetId != null) {
            Optional<AssetMaster> existingAsset = assetRepo.findById(existingAssetId)
                .filter(a -> a.getActive() == null || a.getActive());
            if (existingAsset.isPresent()) {
                response.setAsset(existingAsset.get());
                return;
            }
        }

        // Check if we have required subCategory (asset creation requires it)
        if (response.getSubCategory() == null || response.getSubCategory().getSubCategoryId() == null) {
            log.warn("⚠️ Cannot create asset: SubCategory is required but not available. " +
                    "Category={}, SubCategory={}", 
                    response.getCategory() != null ? response.getCategory().getCategoryName() : "null",
                    response.getSubCategory() != null ? response.getSubCategory().getSubCategoryName() : "null");
            return;
        }

        try {
            AssetRequest assetRequest = new AssetRequest();
            assetRequest.setUserId(request.getUserId());
            assetRequest.setUsername(request.getUsername());
            assetRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
            
            AssetMaster asset = new AssetMaster();
            asset.setAssetNameUdv(info.getAssetName() != null ? info.getAssetName() : 
                "Asset-" + (info.getSerialNumber() != null ? info.getSerialNumber() : "Unknown"));
            asset.setSerialNumber(info.getSerialNumber());
            asset.setCategory(response.getCategory());
            asset.setSubCategory(response.getSubCategory());
            asset.setMake(response.getMake());
            asset.setModel(response.getModel());
            asset.setPurchaseDate(parseLocalDate(info.getPurchaseDate()));
            
            assetRequest.setAsset(asset);

            AssetMaster created = assetService.create(headers, assetRequest);
            if (created != null && created.getAssetId() != null) {
                response.setAsset(created);
                response.setAssetCreated(true);
                log.info("✅ Asset created successfully: {}", created.getAssetId());
            }
        } catch (Exception e) {
            log.error("❌ Failed to create asset: {}", e.getMessage());
            // Don't throw - allow extraction to continue even if asset creation fails
        }
    }

    // ============================================================
    // 📄 SAVE DOCUMENT
    // ============================================================
    private void saveDocument(HttpHeaders headers, MultipartFile file, IntelligentExtractionResponse response,
                             IntelligentExtractionRequest request) {
        try {
            DocumentRequest docRequest = new DocumentRequest();
            docRequest.setUserId(request.getUserId());
            docRequest.setUsername(request.getUsername());
            docRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
            docRequest.setDocType(request.getDocumentType() != null ? request.getDocumentType() : "EXTRACTED_DOCUMENT");
            
            // Link to most specific entity (use entity types supported by DocumentService)
            if (response.getAsset() != null && response.getAsset().getAssetId() != null) {
                docRequest.setEntityType("ASSET");
                docRequest.setEntityId(response.getAsset().getAssetId());
            } else if (response.getModel() != null && response.getModel().getModelId() != null) {
                docRequest.setEntityType("MODEL");
                docRequest.setEntityId(response.getModel().getModelId());
            } else if (response.getMake() != null && response.getMake().getMakeId() != null) {
                docRequest.setEntityType("MAKE");
                docRequest.setEntityId(response.getMake().getMakeId());
            } else if (response.getSubCategory() != null && response.getSubCategory().getSubCategoryId() != null) {
                docRequest.setEntityType("SUBCATEGORY");
                docRequest.setEntityId(response.getSubCategory().getSubCategoryId());
            } else if (response.getCategory() != null && response.getCategory().getCategoryId() != null) {
                docRequest.setEntityType("CATEGORY");
                docRequest.setEntityId(response.getCategory().getCategoryId());
            } else {
                // No entity to link to, skip document save
                log.warn("⚠️ No entity available to link document to. Skipping document save.");
                return;
            }
            
            AssetDocument savedDoc = documentService.upload(headers, file, docRequest);
            response.setDocument(savedDoc);
            log.info("✅ Document saved: documentId={}, entityType={}, entityId={}", 
                    savedDoc.getDocumentId(), docRequest.getEntityType(), docRequest.getEntityId());
        } catch (Exception e) {
            log.warn("⚠️ Failed to save document: {}. Extraction will continue without document linking.", e.getMessage());
            // Don't throw - allow extraction to complete even if document save fails
        }
    }

    // ============================================================
    // 🏪 CREATE OR RESOLVE VENDOR AND OUTLET
    // ============================================================
    private void createOrResolveVendorAndOutlet(HttpHeaders headers, IntelligentExtractionResponse response,
                                                IntelligentExtractionRequest request) {
        IntelligentExtractionResponse.ExtractedAssetInfo info = response.getAssetInfo();
        
        // Create/Resolve Vendor
        if (StringUtils.hasText(info.getVendorName())) {
            VendorMaster vendor = resolveOrCreateVendor(headers, info.getVendorName(), request);
            response.setVendor(vendor);
            response.setVendorCreated(vendor != null && vendor.getVendorId() != null);
        }
        
        // Create/Resolve Outlet
        if (StringUtils.hasText(info.getOutletName())) {
            PurchaseOutlet outlet = resolveOrCreateOutlet(headers, info.getOutletName(), response.getVendor(), request);
            response.setOutlet(outlet);
            response.setOutletCreated(outlet != null && outlet.getOutletId() != null);
        }
    }

    // ============================================================
    // 🏢 RESOLVE OR CREATE VENDOR
    // ============================================================
    private VendorMaster resolveOrCreateVendor(HttpHeaders headers, String vendorName,
                                               IntelligentExtractionRequest request) {
        Optional<VendorMaster> existing = vendorRepo.findByVendorNameIgnoreCase(vendorName)
            .filter(v -> v.getActive() == null || v.getActive());

        if (existing.isPresent()) {
            return existing.get();
        }

        if (request.getAutoCreateEntities()) {
            try {
                VendorRequest vendorRequest = new VendorRequest();
                vendorRequest.setUserId(request.getUserId());
                vendorRequest.setUsername(request.getUsername());
                vendorRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
                
                VendorMaster newVendor = new VendorMaster();
                newVendor.setVendorName(vendorName);
                
                // Extract additional vendor information from extracted text if available
                // Note: This could be enhanced to extract email, phone, address from the full document text
                vendorRequest.setVendor(newVendor);

                VendorMaster created = vendorService.create(headers, vendorRequest);
                log.info("✅ Vendor created/resolved: vendorId={}, vendorName={}", 
                        created.getVendorId(), created.getVendorName());
                return created;
            } catch (Exception e) {
                log.error("❌ Failed to create vendor '{}': {}", vendorName, e.getMessage());
            }
        }

        return null;
    }

    // ============================================================
    // 🏬 RESOLVE OR CREATE OUTLET
    // ============================================================
    private PurchaseOutlet resolveOrCreateOutlet(HttpHeaders headers, String outletName,
                                                VendorMaster vendor, IntelligentExtractionRequest request) {
        Optional<PurchaseOutlet> existing = outletRepo.findByOutletNameIgnoreCase(outletName)
            .filter(o -> o.getActive() == null || o.getActive());

        if (existing.isPresent()) {
            return existing.get();
        }

        if (request.getAutoCreateEntities()) {
            try {
                OutletRequest outletRequest = new OutletRequest();
                outletRequest.setUserId(request.getUserId());
                outletRequest.setUsername(request.getUsername());
                outletRequest.setProjectType(request.getProjectType() != null ? request.getProjectType() : "ASSET_SERVICE");
                
                PurchaseOutlet newOutlet = new PurchaseOutlet();
                newOutlet.setOutletName(outletName);
                if (vendor != null) {
                    newOutlet.setVendor(vendor);
                }
                // Note: Could extract outlet address, contact info from document text
                outletRequest.setOutlet(newOutlet);

                PurchaseOutlet created = outletService.create(headers, outletRequest);
                log.info("✅ Outlet created/resolved: outletId={}, outletName={}", 
                        created.getOutletId(), created.getOutletName());
                return created;
            } catch (Exception e) {
                log.error("❌ Failed to create outlet '{}': {}", outletName, e.getMessage());
            }
        }

        return null;
    }

    // ============================================================
    // 💰 CREATE PURCHASE INFO
    // ============================================================
    private void createPurchaseInfo(HttpHeaders headers, IntelligentExtractionResponse response,
                                   IntelligentExtractionRequest request) {
        IntelligentExtractionResponse.ExtractedAssetInfo info = response.getAssetInfo();
        
        // Only create if we have invoice/bill information
        if (!StringUtils.hasText(info.getInvoiceNumber()) && !StringUtils.hasText(info.getBillNumber())) {
            return;
        }
        
        // Need asset ID to link purchase info
        final Long assetId;
        if (response.getAsset() != null && response.getAsset().getAssetId() != null) {
            assetId = response.getAsset().getAssetId();
        } else if (request.getExistingAssetId() != null) {
            assetId = request.getExistingAssetId();
        } else {
            log.warn("⚠️ Cannot create purchase info: Asset ID is required");
            return;
        }
        
        try {
            AssetMaster asset = assetRepo.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetId));
            
            AssetPurchaseInfo purchaseInfo = new AssetPurchaseInfo();
            purchaseInfo.setAsset(asset);
            purchaseInfo.setUserId(request.getUserId());
            purchaseInfo.setUsername(request.getUsername());
            purchaseInfo.setCreatedBy(request.getUsername());
            purchaseInfo.setUpdatedBy(request.getUsername());
            
            // Invoice/Bill Information
            if (StringUtils.hasText(info.getInvoiceNumber())) {
                purchaseInfo.setInvoiceNumber(info.getInvoiceNumber());
            }
            if (StringUtils.hasText(info.getBillNumber())) {
                purchaseInfo.setBillNumber(info.getBillNumber());
            }
            if (StringUtils.hasText(info.getInvoiceDate())) {
                purchaseInfo.setInvoiceDate(parseLocalDate(info.getInvoiceDate()));
            }
            if (StringUtils.hasText(info.getBillDate())) {
                purchaseInfo.setBillDate(parseLocalDate(info.getBillDate()));
            }
            if (StringUtils.hasText(info.getPoNumber())) {
                purchaseInfo.setPoNumber(info.getPoNumber());
            }
            if (StringUtils.hasText(info.getGrnNumber())) {
                purchaseInfo.setGrnNumber(info.getGrnNumber());
            }
            
            // Financial Information
            if (StringUtils.hasText(info.getPurchasePrice())) {
                try {
                    purchaseInfo.setPurchasePrice(new java.math.BigDecimal(
                        info.getPurchasePrice().replaceAll(",", "")));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse purchase price: {}", info.getPurchasePrice());
                }
            }
            if (StringUtils.hasText(info.getUnitPrice())) {
                try {
                    purchaseInfo.setUnitPrice(new java.math.BigDecimal(
                        info.getUnitPrice().replaceAll(",", "")));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse unit price: {}", info.getUnitPrice());
                }
            }
            if (StringUtils.hasText(info.getQuantity())) {
                try {
                    purchaseInfo.setQuantity(Integer.parseInt(info.getQuantity()));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse quantity: {}", info.getQuantity());
                }
            }
            if (StringUtils.hasText(info.getFinalAmount())) {
                try {
                    purchaseInfo.setFinalAmount(new java.math.BigDecimal(
                        info.getFinalAmount().replaceAll(",", "")));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse final amount: {}", info.getFinalAmount());
                }
            }
            if (StringUtils.hasText(info.getDiscountAmount())) {
                try {
                    purchaseInfo.setDiscountAmount(new java.math.BigDecimal(
                        info.getDiscountAmount().replaceAll(",", "")));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse discount amount: {}", info.getDiscountAmount());
                }
            }
            if (StringUtils.hasText(info.getDiscountPercentage())) {
                try {
                    purchaseInfo.setDiscountPercentage(new java.math.BigDecimal(
                        info.getDiscountPercentage()));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse discount percentage: {}", info.getDiscountPercentage());
                }
            }
            if (StringUtils.hasText(info.getTaxAmount())) {
                try {
                    purchaseInfo.setTaxAmount(new java.math.BigDecimal(
                        info.getTaxAmount().replaceAll(",", "")));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse tax amount: {}", info.getTaxAmount());
                }
            }
            if (StringUtils.hasText(info.getTaxRate())) {
                try {
                    purchaseInfo.setTaxRate(new java.math.BigDecimal(info.getTaxRate()));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse tax rate: {}", info.getTaxRate());
                }
            }
            if (StringUtils.hasText(info.getCgstAmount())) {
                try {
                    purchaseInfo.setCgstAmount(new java.math.BigDecimal(
                        info.getCgstAmount().replaceAll(",", "")));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse CGST amount: {}", info.getCgstAmount());
                }
            }
            if (StringUtils.hasText(info.getSgstAmount())) {
                try {
                    purchaseInfo.setSgstAmount(new java.math.BigDecimal(
                        info.getSgstAmount().replaceAll(",", "")));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse SGST amount: {}", info.getSgstAmount());
                }
            }
            if (StringUtils.hasText(info.getIgstAmount())) {
                try {
                    purchaseInfo.setIgstAmount(new java.math.BigDecimal(
                        info.getIgstAmount().replaceAll(",", "")));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse IGST amount: {}", info.getIgstAmount());
                }
            }
            if (StringUtils.hasText(info.getCgstRate())) {
                try {
                    purchaseInfo.setCgstRate(new java.math.BigDecimal(info.getCgstRate()));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse CGST rate: {}", info.getCgstRate());
                }
            }
            if (StringUtils.hasText(info.getSgstRate())) {
                try {
                    purchaseInfo.setSgstRate(new java.math.BigDecimal(info.getSgstRate()));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse SGST rate: {}", info.getSgstRate());
                }
            }
            if (StringUtils.hasText(info.getIgstRate())) {
                try {
                    purchaseInfo.setIgstRate(new java.math.BigDecimal(info.getIgstRate()));
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse IGST rate: {}", info.getIgstRate());
                }
            }
            
            // Vendor/Outlet Information
            if (response.getVendor() != null) {
                purchaseInfo.setVendor(response.getVendor());
            }
            if (response.getOutlet() != null) {
                purchaseInfo.setOutlet(response.getOutlet());
            }
            if (StringUtils.hasText(info.getVendorGstin())) {
                purchaseInfo.setVendorGstin(info.getVendorGstin());
            }
            if (StringUtils.hasText(info.getVendorPan())) {
                purchaseInfo.setVendorPan(info.getVendorPan());
            }
            if (StringUtils.hasText(info.getVendorAddress())) {
                purchaseInfo.setVendorAddress(info.getVendorAddress());
            }
            if (StringUtils.hasText(info.getVendorContact())) {
                purchaseInfo.setVendorContact(info.getVendorContact());
            }
            
            // Product Information
            if (StringUtils.hasText(info.getHsnCode())) {
                purchaseInfo.setHsnCode(info.getHsnCode());
            }
            if (StringUtils.hasText(info.getSacCode())) {
                purchaseInfo.setSacCode(info.getSacCode());
            }
            if (StringUtils.hasText(info.getSku())) {
                purchaseInfo.setSku(info.getSku());
            }
            if (StringUtils.hasText(info.getPartNumber())) {
                purchaseInfo.setPartNumber(info.getPartNumber());
            }
            if (StringUtils.hasText(info.getBatchNumber())) {
                purchaseInfo.setBatchNumber(info.getBatchNumber());
            }
            
            // Payment Information
            if (StringUtils.hasText(info.getPaymentMethod())) {
                purchaseInfo.setPaymentMethod(info.getPaymentMethod());
            }
            if (StringUtils.hasText(info.getPaymentStatus())) {
                purchaseInfo.setPaymentStatus(info.getPaymentStatus());
            }
            if (StringUtils.hasText(info.getPaymentDate())) {
                purchaseInfo.setPaymentDate(parseLocalDate(info.getPaymentDate()));
            }
            if (StringUtils.hasText(info.getDueDate())) {
                purchaseInfo.setDueDate(parseLocalDate(info.getDueDate()));
            }
            if (StringUtils.hasText(info.getPaymentTerms())) {
                purchaseInfo.setPaymentTerms(info.getPaymentTerms());
            }
            if (StringUtils.hasText(info.getPaymentReference())) {
                purchaseInfo.setPaymentReference(info.getPaymentReference());
            }
            
            // Delivery Information
            if (StringUtils.hasText(info.getDeliveryDate())) {
                purchaseInfo.setDeliveryDate(parseLocalDate(info.getDeliveryDate()));
            }
            if (StringUtils.hasText(info.getDeliveryAddress())) {
                purchaseInfo.setDeliveryAddress(info.getDeliveryAddress());
            }
            
            // Currency
            if (StringUtils.hasText(info.getCurrency())) {
                purchaseInfo.setCurrency(info.getCurrency());
            } else {
                purchaseInfo.setCurrency("INR"); // Default
            }
            
            AssetPurchaseInfo saved = purchaseInfoRepo.save(purchaseInfo);
            response.setPurchaseInfo(saved);
            log.info("✅ Purchase info created successfully: purchaseInfoId={}, invoiceNumber={}", 
                    saved.getPurchaseInfoId(), saved.getInvoiceNumber());
        } catch (Exception e) {
            log.error("❌ Failed to create purchase info: {}", e.getMessage());
            // Don't throw - allow extraction to complete even if purchase info creation fails
        }
    }

    // ============================================================
    // 📊 CALCULATE CONFIDENCE
    // ============================================================
    private Double calculateConfidence(IntelligentExtractionResponse.ExtractedAssetInfo info) {
        double confidence = 0.0;
        
        if (StringUtils.hasText(info.getMakeName())) confidence += 0.2;
        if (StringUtils.hasText(info.getModelName())) confidence += 0.2;
        if (StringUtils.hasText(info.getSerialNumber())) confidence += 0.15;
        if (StringUtils.hasText(info.getCategoryName())) confidence += 0.1;
        if (StringUtils.hasText(info.getSubCategoryName())) confidence += 0.1;
        if (info.getWarrantyInfo() != null) confidence += 0.1;
        if (info.getAmcInfo() != null) confidence += 0.1;
        if (info.getComponentNames() != null && !info.getComponentNames().isEmpty()) confidence += 0.05;
        if (StringUtils.hasText(info.getVendorName())) confidence += 0.05;
        if (StringUtils.hasText(info.getOutletName())) confidence += 0.05;
        
        return Math.min(confidence, 1.0);
    }

    // ============================================================
    // 🔧 DATE PARSING HELPERS
    // ============================================================
    private String parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        
        // Try to parse and reformat to standard format
        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
        );
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate date = LocalDate.parse(dateStr.trim(), formatter);
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                // Continue to next formatter
            }
        }
        
        return dateStr.trim();
    }

    private LocalDate parseLocalDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        
        String parsed = parseDate(dateStr);
        if (parsed == null) {
            return null;
        }
        
        try {
            return LocalDate.parse(parsed);
        } catch (DateTimeParseException e) {
            log.warn("⚠️ Could not parse date: {}", dateStr);
            return null;
        }
    }
}

