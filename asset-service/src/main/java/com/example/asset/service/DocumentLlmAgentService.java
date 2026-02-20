package com.example.asset.service;

import com.example.asset.client.LlmClient;
import com.example.asset.config.LlmProperties;
import com.example.asset.dto.LlmAssetExtractionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Agentic AI service: uses an LLM to extract structured asset data from document text.
 * "Trains" the extraction by prompting the model to follow a strict JSON schema so that
 * the system learns to produce consistent asset data from any uploaded document.
 */
@Service
public class DocumentLlmAgentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentLlmAgentService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int TEXT_PREVIEW_MAX_LEN = 2000;

    private final LlmClient llmClient;
    private final LlmProperties llmProperties;

    public DocumentLlmAgentService(LlmClient llmClient, LlmProperties llmProperties) {
        this.llmClient = llmClient;
        this.llmProperties = llmProperties;
    }

    private static final java.util.Map<String, java.util.List<String>> DOC_TYPE_KEYWORDS = java.util.Map.of(
            "INVOICE", java.util.List.of("invoice", "bill", "receipt", "purchase", "order"),
            "WARRANTY_CARD", java.util.List.of("warranty", "guarantee", "warranty card"),
            "AMC_DOCUMENT", java.util.List.of("amc", "annual maintenance", "maintenance contract", "service contract"),
            "SPEC_SHEET", java.util.List.of("specification", "spec sheet", "technical spec"),
            "MANUAL", java.util.List.of("manual", "user guide", "instruction", "handbook")
    );

    /**
     * Detect document type from raw text (keyword-based). Can be overridden by caller.
     */
    public String detectDocumentType(String documentText) {
        if (documentText == null || documentText.isBlank()) return "UNKNOWN";
        String lower = documentText.toLowerCase();
        for (java.util.Map.Entry<String, java.util.List<String>> e : DOC_TYPE_KEYWORDS.entrySet()) {
            for (String kw : e.getValue()) {
                if (lower.contains(kw)) return e.getKey();
            }
        }
        return "UNKNOWN";
    }

    /**
     * Extract asset data from document text using the LLM agent.
     * Returns a structured JSON-friendly result for display and optional persistence.
     */
    public LlmAssetExtractionResult extractFromText(String documentText, String detectedDocumentType) {
        long start = System.currentTimeMillis();
        LlmAssetExtractionResult result = new LlmAssetExtractionResult();
        result.setDocumentType(detectedDocumentType != null ? detectedDocumentType : "UNKNOWN");
        result.setExtractedTextPreview(truncate(documentText, TEXT_PREVIEW_MAX_LEN));
        result.setExtractionMethod("LLM_AGENT");

        if (!llmProperties.isEnabled()) {
            result.setConfidence(0.0);
            result.setProcessingTimeMs(System.currentTimeMillis() - start);
            log.info("LLM extraction skipped (disabled). Returning empty result.");
            return result;
        }

        String prompt = buildExtractionPrompt(documentText, detectedDocumentType);
        String rawResponse = llmClient.complete(prompt);

        if (rawResponse == null || rawResponse.isBlank()) {
            result.setConfidence(0.0);
            result.setProcessingTimeMs(System.currentTimeMillis() - start);
            return result;
        }

        String jsonStr = stripJsonFromResponse(rawResponse);
        try {
            mapJsonToResult(jsonStr, result);
            result.setConfidence(computeConfidence(result));
        } catch (Exception e) {
            log.warn("Failed to parse LLM JSON, returning raw preview: {}", e.getMessage());
            result.setConfidence(0.0);
        }

        result.setProcessingTimeMs(System.currentTimeMillis() - start);
        return result;
    }

    private static String buildExtractionPrompt(String documentText, String documentType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Extract all asset-related information from the following document text. ");
        if (documentType != null && !"UNKNOWN".equals(documentType)) {
            sb.append("Document type: ").append(documentType).append(". ");
        }
        sb.append("Return ONLY a single JSON object with these keys (use null for missing values):\n");
        sb.append("{\n");
        sb.append("  \"documentType\": \"string or null\",\n");
        sb.append("  \"assetName\": \"string\", \"serialNumber\": \"string\", \"categoryName\": \"string\", \"subCategoryName\": \"string\",\n");
        sb.append("  \"makeName\": \"string\", \"modelName\": \"string\", \"brand\": \"string\", \"description\": \"string\", \"assetStatus\": \"string\",\n");
        sb.append("  \"purchaseDate\": \"string (YYYY-MM-DD or as in document)\", \"purchasePrice\": \"string\", \"invoiceNumber\": \"string\", \"invoiceDate\": \"string\",\n");
        sb.append("  \"billNumber\": \"string\", \"billDate\": \"string\", \"poNumber\": \"string\", \"quantity\": \"string\", \"unitPrice\": \"string\", \"totalAmount\": \"string\", \"currency\": \"string\",\n");
        sb.append("  \"vendorName\": \"string\", \"outletName\": \"string\", \"vendorGstin\": \"string\", \"paymentMethod\": \"string\", \"paymentStatus\": \"string\",\n");
        sb.append("  \"warranty\": { \"warrantyStatus\": \"string\", \"warrantyProvider\": \"string\", \"startDate\": \"string\", \"endDate\": \"string\", \"duration\": \"string\", \"terms\": \"string\" } or null,\n");
        sb.append("  \"amc\": { \"amcStatus\": \"string\", \"provider\": \"string\", \"startDate\": \"string\", \"endDate\": \"string\", \"duration\": \"string\" } or null,\n");
        sb.append("  \"componentNames\": [ \"string\" ] or null\n");
        sb.append("}\n\n");
        sb.append("Document text:\n---\n");
        sb.append(truncate(documentText, 12000));
        sb.append("\n---");
        return sb.toString();
    }

    private static String stripJsonFromResponse(String raw) {
        String s = raw.trim();
        if (s.startsWith("```json")) {
            s = s.substring(7).trim();
        } else if (s.startsWith("```")) {
            s = s.substring(3).trim();
        }
        if (s.endsWith("```")) {
            s = s.substring(0, s.length() - 3).trim();
        }
        return s;
    }

    private static void mapJsonToResult(String jsonStr, LlmAssetExtractionResult result) throws Exception {
        JsonNode root = MAPPER.readTree(jsonStr);

        result.setDocumentType(getText(root, "documentType"));
        result.setAssetName(getText(root, "assetName"));
        result.setSerialNumber(getText(root, "serialNumber"));
        result.setCategoryName(getText(root, "categoryName"));
        result.setSubCategoryName(getText(root, "subCategoryName"));
        result.setMakeName(getText(root, "makeName"));
        result.setModelName(getText(root, "modelName"));
        result.setBrand(getText(root, "brand"));
        result.setDescription(getText(root, "description"));
        result.setAssetStatus(getText(root, "assetStatus"));
        result.setPurchaseDate(getText(root, "purchaseDate"));
        result.setPurchasePrice(getText(root, "purchasePrice"));
        result.setInvoiceNumber(getText(root, "invoiceNumber"));
        result.setInvoiceDate(getText(root, "invoiceDate"));
        result.setBillNumber(getText(root, "billNumber"));
        result.setBillDate(getText(root, "billDate"));
        result.setPoNumber(getText(root, "poNumber"));
        result.setQuantity(getText(root, "quantity"));
        result.setUnitPrice(getText(root, "unitPrice"));
        result.setTotalAmount(getText(root, "totalAmount"));
        result.setCurrency(getText(root, "currency"));
        result.setVendorName(getText(root, "vendorName"));
        result.setOutletName(getText(root, "outletName"));
        result.setVendorGstin(getText(root, "vendorGstin"));
        result.setPaymentMethod(getText(root, "paymentMethod"));
        result.setPaymentStatus(getText(root, "paymentStatus"));

        JsonNode warranty = root.path("warranty");
        if (warranty.isObject()) {
            LlmAssetExtractionResult.WarrantyBlock w = new LlmAssetExtractionResult.WarrantyBlock();
            w.setWarrantyStatus(getText(warranty, "warrantyStatus"));
            w.setWarrantyProvider(getText(warranty, "warrantyProvider"));
            w.setStartDate(getText(warranty, "startDate"));
            w.setEndDate(getText(warranty, "endDate"));
            w.setDuration(getText(warranty, "duration"));
            w.setTerms(getText(warranty, "terms"));
            result.setWarranty(w);
        }

        JsonNode amc = root.path("amc");
        if (amc.isObject()) {
            LlmAssetExtractionResult.AmcBlock a = new LlmAssetExtractionResult.AmcBlock();
            a.setAmcStatus(getText(amc, "amcStatus"));
            a.setProvider(getText(amc, "provider"));
            a.setStartDate(getText(amc, "startDate"));
            a.setEndDate(getText(amc, "endDate"));
            a.setDuration(getText(amc, "duration"));
            result.setAmc(a);
        }

        JsonNode comp = root.path("componentNames");
        if (comp.isArray()) {
            List<String> list = new ArrayList<>();
            comp.forEach(n -> list.add(n.asText(null)));
            result.setComponentNames(list);
        }
    }

    private static String getText(JsonNode node, String key) {
        JsonNode v = node.path(key);
        if (v.isMissingNode() || v.isNull()) return null;
        String s = v.asText(null);
        return (s == null || s.isBlank() || "null".equalsIgnoreCase(s)) ? null : s.trim();
    }

    private static double computeConfidence(LlmAssetExtractionResult r) {
        double c = 0.0;
        if (r.getAssetName() != null) c += 0.2;
        if (r.getMakeName() != null) c += 0.15;
        if (r.getModelName() != null) c += 0.15;
        if (r.getSerialNumber() != null) c += 0.1;
        if (r.getCategoryName() != null) c += 0.1;
        if (r.getWarranty() != null) c += 0.1;
        if (r.getAmc() != null) c += 0.1;
        if (r.getVendorName() != null) c += 0.05;
        if (r.getInvoiceNumber() != null || r.getBillNumber() != null) c += 0.05;
        return Math.min(c, 1.0);
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return null;
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }
}
