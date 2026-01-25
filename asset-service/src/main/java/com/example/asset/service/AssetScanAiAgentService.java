package com.example.asset.service;

import com.example.asset.dto.AssetScanCreateRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ✅ AssetScanAiAgentService
 * AI Agent service that intelligently analyzes scanned QR/barcode data
 * and extracts structured information for asset creation/update.
 * 
 * This agent can:
 * - Parse JSON data from QR codes
 * - Extract structured information from text patterns
 * - Identify warranty, AMC, and other related data
 * - Map data to appropriate asset tables
 */
@Service
public class AssetScanAiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AssetScanAiAgentService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Date patterns for parsing
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    // ============================================================
    // 🤖 ANALYZE AND EXTRACT DATA FROM SCAN
    // ============================================================
    public AssetScanCreateRequest analyzeAndExtract(String scanValue, String scanType, 
                                                    Long userId, String username) {
        log.info("🤖 AI Agent analyzing scan value: '{}'", scanValue);
        
        AssetScanCreateRequest request = new AssetScanCreateRequest();
        request.setScanValue(scanValue);
        request.setScanType(scanType != null ? scanType : "AUTO");
        request.setUserId(userId);
        request.setUsername(username);
        
        // Try to parse as JSON first
        Map<String, Object> parsedData = tryParseJson(scanValue);
        if (parsedData != null && !parsedData.isEmpty()) {
            log.debug("✅ Parsed as JSON data");
            extractFromJson(parsedData, request);
            request.setRawData(parsedData);
        } else {
            // Try pattern-based extraction
            log.debug("🔍 Attempting pattern-based extraction");
            extractFromPatterns(scanValue, request);
        }
        
        // Post-process: validate and enrich data
        enrichAndValidate(request);
        
        log.info("🤖 AI Agent extraction complete. Asset: {}, Warranty: {}, AMC: {}", 
                request.getAssetNameUdv() != null ? "YES" : "NO",
                request.getWarranty() != null ? "YES" : "NO",
                request.getAmc() != null ? "YES" : "NO");
        
        return request;
    }

    // ============================================================
    // 📄 PARSE JSON FROM SCAN VALUE
    // ============================================================
    private Map<String, Object> tryParseJson(String scanValue) {
        if (!StringUtils.hasText(scanValue)) {
            return null;
        }
        
        String trimmed = scanValue.trim();
        
        // Check if it looks like JSON
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return null;
        }
        
        try {
            return objectMapper.readValue(trimmed, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.debug("⚠️ Not valid JSON: {}", e.getMessage());
            return null;
        }
    }

    // ============================================================
    // 📊 EXTRACT FROM JSON DATA
    // ============================================================
    private void extractFromJson(Map<String, Object> data, AssetScanCreateRequest request) {
        // Extract asset basic info
        request.setAssetNameUdv(getStringValue(data, "assetName", "assetNameUdv", "name", "title", "asset"));
        request.setSerialNumber(getStringValue(data, "serialNumber", "serial", "serialNo", "sn"));
        request.setAssetStatus(getStringValue(data, "assetStatus", "status", "state", "assetStatus"));
        request.setPurchaseDate(parseDate(getStringValue(data, "purchaseDate", "purchase_date", "purchased", "purchaseDate")));
        
        // Extract IDs (if provided directly)
        request.setCategoryId(getLongValue(data, "categoryId", "category_id", "categoryId"));
        request.setSubCategoryId(getLongValue(data, "subCategoryId", "sub_category_id", "subCategoryId"));
        request.setMakeId(getLongValue(data, "makeId", "make_id", "makeId"));
        request.setModelId(getLongValue(data, "modelId", "model_id", "modelId"));
        
        // Extract Names (for AI agent to resolve to IDs later)
        request.setCategoryName(getStringValue(data, "categoryName", "category_name", "category", "cat"));
        request.setSubCategoryName(getStringValue(data, "subCategoryName", "sub_category_name", "subCategory", "subcategory", "subCat"));
        request.setMakeName(getStringValue(data, "makeName", "make_name", "make", "brand", "manufacturer"));
        request.setModelName(getStringValue(data, "modelName", "model_name", "model", "modelNo", "modelNumber"));
        
        // If names are provided but IDs are not, prioritize names (will be resolved later)
        if (request.getCategoryName() != null && request.getCategoryId() == null) {
            // Name will be resolved to ID in the service layer
        }
        if (request.getSubCategoryName() != null && request.getSubCategoryId() == null) {
            // Name will be resolved to ID in the service layer
        }
        if (request.getMakeName() != null && request.getMakeId() == null) {
            // Name will be resolved to ID in the service layer
        }
        if (request.getModelName() != null && request.getModelId() == null) {
            // Name will be resolved to ID in the service layer
        }
        
        // Extract warranty data
        Object warrantyObj = data.get("warranty");
        if (warrantyObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> warrantyMap = (Map<String, Object>) warrantyObj;
            AssetScanCreateRequest.WarrantyData warranty = new AssetScanCreateRequest.WarrantyData();
            warranty.setWarrantyStatus(getStringValue(warrantyMap, "status", "warrantyStatus"));
            warranty.setWarrantyProvider(getStringValue(warrantyMap, "provider", "warrantyProvider"));
            warranty.setWarrantyTerms(getStringValue(warrantyMap, "terms", "warrantyTerms"));
            warranty.setStartDate(parseDate(getStringValue(warrantyMap, "startDate", "start", "from")));
            warranty.setEndDate(parseDate(getStringValue(warrantyMap, "endDate", "end", "to", "expiry")));
            warranty.setComponentId(getLongValue(warrantyMap, "componentId", "component_id"));
            warranty.setDocumentId(getLongValue(warrantyMap, "documentId", "document_id"));
            request.setWarranty(warranty);
        }
        
        // Extract AMC data
        Object amcObj = data.get("amc");
        if (amcObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> amcMap = (Map<String, Object>) amcObj;
            AssetScanCreateRequest.AmcData amc = new AssetScanCreateRequest.AmcData();
            amc.setAmcStatus(getStringValue(amcMap, "status", "amcStatus"));
            amc.setStartDate(parseDate(getStringValue(amcMap, "startDate", "start", "from")));
            amc.setEndDate(parseDate(getStringValue(amcMap, "endDate", "end", "to", "expiry")));
            amc.setComponentId(getLongValue(amcMap, "componentId", "component_id"));
            amc.setDocumentId(getLongValue(amcMap, "documentId", "document_id"));
            request.setAmc(amc);
        }
        
        // Extract user assignment
        request.setTargetUserId(getLongValue(data, "targetUserId", "target_user_id", "assignedTo", "userId"));
        request.setTargetUsername(getStringValue(data, "targetUsername", "target_username", "assignedToUser"));
        
        // Extract components
        Object componentsObj = data.get("components");
        if (componentsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> componentsList = (List<Object>) componentsObj;
            List<Long> componentIds = new ArrayList<>();
            for (Object comp : componentsList) {
                if (comp instanceof Number) {
                    componentIds.add(((Number) comp).longValue());
                } else if (comp instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> compMap = (Map<String, Object>) comp;
                    Long compId = getLongValue(compMap, "id", "componentId", "component_id");
                    if (compId != null) {
                        componentIds.add(compId);
                    }
                }
            }
            if (!componentIds.isEmpty()) {
                request.setComponentIds(componentIds);
            }
        }
    }

    // ============================================================
    // 🔍 EXTRACT FROM PATTERNS (for non-JSON data)
    // ============================================================
    private void extractFromPatterns(String scanValue, AssetScanCreateRequest request) {
        // Pattern 1: Simple identifier (could be asset name or serial)
        if (scanValue.length() > 3 && scanValue.length() < 100) {
            // Check if it looks like a serial number (alphanumeric)
            if (Pattern.matches("^[A-Z0-9-]+$", scanValue.toUpperCase())) {
                request.setSerialNumber(scanValue);
            } else {
                // Could be asset name
                request.setAssetNameUdv(scanValue);
            }
        }
        
        // Pattern 2: Structured text with delimiters
        // Example: "ASSET-12345|SERIAL-ABC123|CATEGORY-Electronics|SUBCATEGORY-Laptops|MAKE-Dell|MODEL-XPS15|STATUS-ACTIVE|PURCHASE-2024-01-01"
        if (scanValue.contains("|") || scanValue.contains(";") || scanValue.contains(",")) {
            String[] parts = scanValue.split("[|;,]");
            for (String part : parts) {
                String[] keyValue = part.split("[:=]", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().toLowerCase();
                    String value = keyValue[1].trim();
                    
                    switch (key) {
                        case "asset":
                        case "assetname":
                        case "name":
                            request.setAssetNameUdv(value);
                            break;
                        case "serial":
                        case "serialnumber":
                        case "sn":
                            request.setSerialNumber(value);
                            break;
                        case "category":
                        case "categoryname":
                        case "cat":
                            request.setCategoryName(value);
                            break;
                        case "subcategory":
                        case "subcategoryname":
                        case "subcat":
                        case "sub_category":
                            request.setSubCategoryName(value);
                            break;
                        case "make":
                        case "makename":
                        case "brand":
                        case "manufacturer":
                            request.setMakeName(value);
                            break;
                        case "model":
                        case "modelname":
                        case "modelno":
                        case "modelnumber":
                            request.setModelName(value);
                            break;
                        case "status":
                        case "assetstatus":
                        case "state":
                            request.setAssetStatus(value);
                            break;
                        case "purchase":
                        case "purchasedate":
                        case "purchase_date":
                        case "purchased":
                            request.setPurchaseDate(parseDate(value));
                            break;
                        case "warrantystart":
                        case "warrantyfrom":
                        case "warranty_start":
                            if (request.getWarranty() == null) {
                                request.setWarranty(new AssetScanCreateRequest.WarrantyData());
                            }
                            request.getWarranty().setStartDate(parseDate(value));
                            break;
                        case "warrantyend":
                        case "warrantyto":
                        case "warrantyexpiry":
                        case "warranty_end":
                            if (request.getWarranty() == null) {
                                request.setWarranty(new AssetScanCreateRequest.WarrantyData());
                            }
                            request.getWarranty().setEndDate(parseDate(value));
                            break;
                    }
                }
            }
        }
        
        // Pattern 3: Try to extract from common text patterns
        // Example: "Dell XPS 15 Laptop - Serial: ABC123 - Category: Electronics"
        extractFromTextPatterns(scanValue, request);
    }
    
    // ============================================================
    // 📝 EXTRACT FROM TEXT PATTERNS (AI-like extraction)
    // ============================================================
    private void extractFromTextPatterns(String scanValue, AssetScanCreateRequest request) {
        // Extract serial number patterns
        // Patterns: "SN: ABC123", "Serial: XYZ789", "S/N: DEF456"
        Pattern serialPattern = Pattern.compile("(?:serial|sn|s/n)[\\s:]*([A-Z0-9-]+)", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher serialMatcher = serialPattern.matcher(scanValue);
        if (serialMatcher.find() && request.getSerialNumber() == null) {
            request.setSerialNumber(serialMatcher.group(1).trim());
        }
        
        // Extract category patterns
        // Patterns: "Category: Electronics", "Cat: Laptops"
        Pattern categoryPattern = Pattern.compile("(?:category|cat)[\\s:]*([A-Za-z\\s]+)", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher categoryMatcher = categoryPattern.matcher(scanValue);
        if (categoryMatcher.find() && request.getCategoryName() == null) {
            request.setCategoryName(categoryMatcher.group(1).trim());
        }
        
        // Extract make/brand patterns
        // Patterns: "Make: Dell", "Brand: Apple", "Manufacturer: HP"
        Pattern makePattern = Pattern.compile("(?:make|brand|manufacturer)[\\s:]*([A-Za-z0-9\\s]+)", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher makeMatcher = makePattern.matcher(scanValue);
        if (makeMatcher.find() && request.getMakeName() == null) {
            request.setMakeName(makeMatcher.group(1).trim());
        }
        
        // Extract model patterns
        // Patterns: "Model: XPS 15", "Model No: iPhone 15 Pro"
        Pattern modelPattern = Pattern.compile("(?:model|model\\s*no|model\\s*number)[\\s:]*([A-Za-z0-9\\s-]+)", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher modelMatcher = modelPattern.matcher(scanValue);
        if (modelMatcher.find() && request.getModelName() == null) {
            request.setModelName(modelMatcher.group(1).trim());
        }
        
        // Extract status patterns
        // Patterns: "Status: ACTIVE", "State: INACTIVE"
        Pattern statusPattern = Pattern.compile("(?:status|state)[\\s:]*([A-Za-z]+)", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher statusMatcher = statusPattern.matcher(scanValue);
        if (statusMatcher.find() && request.getAssetStatus() == null) {
            request.setAssetStatus(statusMatcher.group(1).trim().toUpperCase());
        }
        
        // Extract date patterns
        // Patterns: "Purchase: 2024-01-01", "Date: 01/01/2024"
        Pattern datePattern = Pattern.compile("(?:purchase|purchased|date)[\\s:]*([0-9]{4}[-/][0-9]{1,2}[-/][0-9]{1,2}|[0-9]{1,2}[-/][0-9]{1,2}[-/][0-9]{4})", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher dateMatcher = datePattern.matcher(scanValue);
        if (dateMatcher.find() && request.getPurchaseDate() == null) {
            request.setPurchaseDate(parseDate(dateMatcher.group(1).trim()));
        }
    }

    // ============================================================
    // ✨ ENRICH AND VALIDATE
    // ============================================================
    private void enrichAndValidate(AssetScanCreateRequest request) {
        // If no asset name but we have serial number, use serial as name
        if (!StringUtils.hasText(request.getAssetNameUdv()) && 
            StringUtils.hasText(request.getSerialNumber())) {
            request.setAssetNameUdv("Asset-" + request.getSerialNumber());
        }
        
        // If we have warranty dates but no status, set default
        if (request.getWarranty() != null) {
            AssetScanCreateRequest.WarrantyData warranty = request.getWarranty();
            if (warranty.getStartDate() != null && warranty.getEndDate() != null) {
                if (!StringUtils.hasText(warranty.getWarrantyStatus())) {
                    warranty.setWarrantyStatus("ACTIVE");
                }
            }
        }
        
        // If we have AMC dates but no status, set default
        if (request.getAmc() != null) {
            AssetScanCreateRequest.AmcData amc = request.getAmc();
            if (amc.getStartDate() != null && amc.getEndDate() != null) {
                if (!StringUtils.hasText(amc.getAmcStatus())) {
                    amc.setAmcStatus("ACTIVE");
                }
            }
        }
        
        // Set default asset status if not provided
        if (!StringUtils.hasText(request.getAssetStatus())) {
            request.setAssetStatus("ACTIVE");
        }
    }

    // ============================================================
    // 🧩 HELPER METHODS
    // ============================================================
    private String getStringValue(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null) {
                return value.toString().trim();
            }
        }
        return null;
    }
    
    private Long getLongValue(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null) {
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                } else {
                    try {
                        return Long.parseLong(value.toString().trim());
                    } catch (NumberFormatException e) {
                        // Continue to next key
                    }
                }
            }
        }
        return null;
    }
    
    private LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        
        String trimmed = dateStr.trim();
        
        // Try each formatter
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException e) {
                // Continue to next formatter
            }
        }
        
        log.warn("⚠️ Could not parse date: {}", dateStr);
        return null;
    }
}

