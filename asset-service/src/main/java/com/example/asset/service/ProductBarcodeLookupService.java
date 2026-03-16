package com.example.asset.service;

import com.example.asset.dto.ProductBarcodeLookupResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Lookup standard product barcodes (EAN, UPC, GTIN) in public databases.
 * - OpenFoodFacts: https://world.openfoodfacts.org/api/v0/product/{barcode}.json
 * - UPC Item DB: https://api.upcitemdb.com/prod/trial/lookup?upc={barcode}
 */
@Service
public class ProductBarcodeLookupService {

    private static final Logger log = LoggerFactory.getLogger(ProductBarcodeLookupService.class);

    private static final String OPENFOODFACTS_URL = "https://world.openfoodfacts.org/api/v0/product/%s.json";
    private static final String UPCITEMDB_URL = "https://api.upcitemdb.com/prod/trial/lookup?upc=%s";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ProductBarcodeLookupService(
            @Qualifier("productLookupRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Check if value looks like a product barcode (8-14 digits).
     */
    public boolean isProductBarcode(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        return value.trim().matches("\\d{8,14}");
    }

    /**
     * Lookup product in OpenFoodFacts first, then UPC Item DB.
     */
    public Optional<ProductBarcodeLookupResult> lookup(String barcode) {
        if (!isProductBarcode(barcode)) return Optional.empty();

        String trimmed = barcode.trim();
        log.debug("Looking up product barcode: {}", trimmed);

        // 1. Try OpenFoodFacts
        Optional<ProductBarcodeLookupResult> result = lookupOpenFoodFacts(trimmed);
        if (result.isPresent()) return result;

        // 2. Try UPC Item DB
        result = lookupUpcItemDb(trimmed);
        if (result.isPresent()) return result;

        log.debug("Product not found in OpenFoodFacts or UPC Item DB: {}", trimmed);
        return Optional.empty();
    }

    private Optional<ProductBarcodeLookupResult> lookupOpenFoodFacts(String barcode) {
        try {
            String url = String.format(OPENFOODFACTS_URL, barcode);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                int status = root.path("status").asInt(0);
                if (status == 1) {
                    JsonNode product = root.path("product");
                    if (!product.isMissingNode()) {
                        String productName = getText(product, "product_name", "product_name_en");
                        String categories = getText(product, "categories");
                        String[] catParts = categories != null ? categories.split(",") : new String[0];
                        String category = catParts.length > 0 ? catParts[0].trim() : null;
                        String subcategory = catParts.length > 1 ? catParts[1].trim() : null;
                        String brand = getText(product, "brands");

                        ProductBarcodeLookupResult r = new ProductBarcodeLookupResult();
                        r.setBarcode(barcode);
                        r.setProductName(productName != null ? productName : "Unknown Product");
                        r.setCategory(category);
                        r.setSubcategory(subcategory);
                        r.setBrand(brand);
                        r.setSource("OpenFoodFacts");
                        log.info("Product found in OpenFoodFacts: {}", r.getProductName());
                        return Optional.of(r);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("OpenFoodFacts lookup failed for {}: {}", barcode, e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<ProductBarcodeLookupResult> lookupUpcItemDb(String barcode) {
        try {
            // UPC Item DB expects UPC format (often 12 digits); pad if needed
            String upcParam = barcode.length() == 13 && barcode.startsWith("0")
                    ? barcode.substring(1) : barcode;

            String url = String.format(UPCITEMDB_URL, upcParam);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String code = root.path("code").asText("");
                if ("OK".equals(code)) {
                    JsonNode items = root.path("items");
                    if (items.isArray() && items.size() > 0) {
                        JsonNode item = items.get(0);
                        String title = getText(item, "title");
                        String category = getText(item, "category");
                        String brand = getText(item, "brand");
                        String model = getText(item, "model");

                        String catPart = category;
                        String subcatPart = null;
                        if (category != null && category.contains(" > ")) {
                            String[] parts = category.split(" > ");
                            catPart = parts.length > 0 ? parts[0].trim() : category;
                            subcatPart = parts.length > 1 ? parts[1].trim() : null;
                        }

                        ProductBarcodeLookupResult r = new ProductBarcodeLookupResult();
                        r.setBarcode(barcode);
                        r.setProductName(title != null ? title : "Unknown Product");
                        r.setCategory(catPart);
                        r.setSubcategory(subcatPart);
                        r.setBrand(brand);
                        r.setModel(model);
                        r.setSource("UPCItemDB");
                        log.info("Product found in UPC Item DB: {}", r.getProductName());
                        return Optional.of(r);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("UPC Item DB lookup failed for {}: {}", barcode, e.getMessage());
        }
        return Optional.empty();
    }

    private String getText(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode n = node.path(key);
            if (!n.isMissingNode() && n.isTextual()) {
                String v = n.asText().trim();
                if (!v.isEmpty()) return v;
            }
        }
        return null;
    }
}
