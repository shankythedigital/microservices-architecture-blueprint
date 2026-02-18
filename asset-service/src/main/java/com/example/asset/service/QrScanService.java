package com.example.asset.service;

import com.example.asset.dto.*;
import com.example.asset.mapper.CategoryMapper;
import com.example.asset.mapper.ComponentMapper;
import com.example.asset.mapper.MakeMapper;
import com.example.asset.mapper.OutletMapper;
import com.example.asset.mapper.ProductSubCategoryMapper;
import com.example.asset.mapper.VendorMapper;
import com.example.asset.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Patterns for common QR/barcode formats

/**
 * ✅ QrScanService
 * Universal QR code scan service. Extracts data from QR codes and returns
 * the respective entity in its native JSON format.
 * <p>
 * Supported QR formats:
 * - JSON: {"type":"category","id":5} or {"type":"category","name":"Laptops"}
 * - URL: /api/asset/v1/categories/5 or https://.../categories/5
 * - Plain numeric: Try as ID for each entity type (first match)
 * - Plain text: Try as name for each entity type (first match)
 */
@Service
public class QrScanService {

    private static final Logger log = LoggerFactory.getLogger(QrScanService.class);

    private static final Pattern URL_PATTERN = Pattern.compile(
            "/(categories|subcategories|makes|models|components|warranty|amc|outlets|vendors)/(\\d+)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern WIFI_PATTERN = Pattern.compile(
            "WIFI:T:(WPA|WEP|nopass|WPA2|WPA3);S:([^;]*);P:([^;]*);;", Pattern.CASE_INSENSITIVE);
    private static final Pattern WIFI_NOPASS_PATTERN = Pattern.compile(
            "WIFI:T:nopass;S:([^;]*);;", Pattern.CASE_INSENSITIVE);
    private static final Pattern MAILTO_PATTERN = Pattern.compile("mailto:([^?\\s]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEL_PATTERN = Pattern.compile("tel:([^\\s]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern GEO_PATTERN = Pattern.compile("geo:([+-]?[\\d.]+),([+-]?[\\d.]+)(?:\\?q=([^\\s]+))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern BITCOIN_PATTERN = Pattern.compile("bitcoin:([a-zA-Z0-9]+)(?:\\?amount=([\\d.]+))?", Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper;
    private final CategoryService categoryService;
    private final SubCategoryService subCategoryService;
    private final MakeService makeService;
    private final ModelService modelService;
    private final ComponentService componentService;
    private final AssetWarrantyService assetWarrantyService;
    private final AssetAmcService assetAmcService;
    private final OutletService outletService;
    private final VendorService vendorService;
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final AssetComponentRepository componentRepo;
    private final PurchaseOutletRepository outletRepo;
    private final VendorRepository vendorRepo;

    public QrScanService(ObjectMapper objectMapper,
                         CategoryService categoryService,
                         SubCategoryService subCategoryService,
                         MakeService makeService,
                         ModelService modelService,
                         ComponentService componentService,
                         AssetWarrantyService assetWarrantyService,
                         AssetAmcService assetAmcService,
                         OutletService outletService,
                         VendorService vendorService,
                         ProductCategoryRepository categoryRepo,
                         ProductSubCategoryRepository subCategoryRepo,
                         ProductMakeRepository makeRepo,
                         ProductModelRepository modelRepo,
                         AssetComponentRepository componentRepo,
                         PurchaseOutletRepository outletRepo,
                         VendorRepository vendorRepo) {
        this.objectMapper = objectMapper;
        this.categoryService = categoryService;
        this.subCategoryService = subCategoryService;
        this.makeService = makeService;
        this.modelService = modelService;
        this.componentService = componentService;
        this.assetWarrantyService = assetWarrantyService;
        this.assetAmcService = assetAmcService;
        this.outletService = outletService;
        this.vendorService = vendorService;
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.componentRepo = componentRepo;
        this.outletRepo = outletRepo;
        this.vendorRepo = vendorRepo;
    }

    /**
     * Scan QR code data and return the entity in its respective JSON format.
     */
    @Transactional(readOnly = true)
    public Optional<QrScanResponseDto> scanQr(String qrData) {
        if (!StringUtils.hasText(qrData)) {
            log.warn("⚠️ Empty QR data provided");
            return Optional.empty();
        }

        String trimmed = qrData.trim();
        log.info("📱 Scanning QR data: '{}'", trimmed.length() > 100 ? trimmed.substring(0, 100) + "..." : trimmed);

        // 1. Try JSON format: {"type":"category","id":5} or {"type":"category","name":"Laptops"}
        Optional<QrScanResponseDto> result = parseAndResolveJson(trimmed);
        if (result.isPresent()) return result;

        // 2. Try URL format: /categories/5 or https://.../categories/5
        result = parseAndResolveUrl(trimmed);
        if (result.isPresent()) return result;

        // 3. Try plain numeric - GTIN (12-14 digits) first, then entity ID (avoids conflict with entity IDs)
        if (isNumeric(trimmed)) {
            int len = trimmed.length();
            if (len >= 12 && len <= 14) {
                result = Optional.of(buildProductResponse(trimmed, len));
                if (result.isPresent()) return result;
            }
            Long id = Long.parseLong(trimmed);
            result = resolveById(id);
            if (result.isPresent()) return result;
        }

        // 4. Try plain text as name
        result = resolveByName(trimmed);
        if (result.isPresent()) return result;

        // 5. Try common QR formats (WIFI, vCard, MeCard, mailto, tel, geo, bitcoin)
        result = parseKnownQrFormats(trimmed);
        if (result.isPresent()) return result;

        // 6. Entity not in asset management master - return universal standard format
        log.info("📱 QR data not part of asset management, returning universal format: '{}'",
                trimmed.length() > 50 ? trimmed.substring(0, 50) + "..." : trimmed);
        return Optional.of(buildUniversalResponse(trimmed));
    }

    /**
     * Build universal standard JSON format for QR codes not in asset management entity master.
     */
    private QrScanResponseDto buildUniversalResponse(String rawValue) {
        QrUniversalEntityDto universal = new QrUniversalEntityDto(rawValue);
        universal.setDescription("QR code data is not part of asset management. Entity type not found in entity master.");
        universal.setDetectedFormat(detectFormat(rawValue));
        return new QrScanResponseDto("universal", universal);
    }

    private String detectFormat(String data) {
        if (data == null || data.isEmpty()) return "TEXT";
        String trimmed = data.trim();
        if (trimmed.startsWith("{")) return "JSON";
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.contains("/")) return "URL";
        if (trimmed.matches("^[0-9]+$")) {
            int l = trimmed.length();
            return (l >= 12 && l <= 14) ? "GTIN" : "NUMERIC";
        }
        if (trimmed.startsWith("WIFI:")) return "WIFI";
        if (trimmed.startsWith("BEGIN:VCARD")) return "VCARD";
        if (trimmed.startsWith("MECARD:")) return "MECARD";
        if (trimmed.startsWith("mailto:")) return "EMAIL";
        if (trimmed.startsWith("tel:")) return "PHONE";
        if (trimmed.startsWith("geo:")) return "GEO";
        if (trimmed.startsWith("bitcoin:")) return "BITCOIN";
        return "TEXT";
    }

    /** Product barcode (UPC-A, EAN-13, GTIN-14) - 12-14 digits */
    private QrScanResponseDto buildProductResponse(String rawValue, int digitCount) {
        String format = switch (digitCount) {
            case 12 -> "UPC-A";
            case 13 -> "EAN-13";
            case 14 -> "GTIN-14";
            default -> "GTIN";
        };
        return new QrScanResponseDto("product", new QrProductDto(rawValue, format, digitCount));
    }

    /** Parse common QR formats: WIFI, vCard, MeCard, mailto, tel, geo, bitcoin */
    private Optional<QrScanResponseDto> parseKnownQrFormats(String data) {
        if (data == null || data.isEmpty()) return Optional.empty();
        String trimmed = data.trim();

        // WIFI
        Matcher wifi = WIFI_PATTERN.matcher(trimmed);
        if (wifi.find()) {
            QrParsedFormatDto dto = new QrParsedFormatDto(trimmed, "WIFI");
            dto.setSsid(wifi.group(2));
            dto.setSecurity(wifi.group(1));
            dto.setPassword(wifi.group(3));
            return Optional.of(new QrScanResponseDto("wifi", dto));
        }
        Matcher wifiNopass = WIFI_NOPASS_PATTERN.matcher(trimmed);
        if (wifiNopass.find()) {
            QrParsedFormatDto dto = new QrParsedFormatDto(trimmed, "WIFI");
            dto.setSsid(wifiNopass.group(1));
            dto.setSecurity("nopass");
            return Optional.of(new QrScanResponseDto("wifi", dto));
        }

        // mailto
        Matcher mailto = MAILTO_PATTERN.matcher(trimmed);
        if (mailto.find()) {
            QrParsedFormatDto dto = new QrParsedFormatDto(trimmed, "EMAIL");
            dto.setEmail(mailto.group(1));
            return Optional.of(new QrScanResponseDto("email", dto));
        }

        // tel
        Matcher tel = TEL_PATTERN.matcher(trimmed);
        if (tel.find()) {
            QrParsedFormatDto dto = new QrParsedFormatDto(trimmed, "PHONE");
            dto.setPhone(tel.group(1));
            return Optional.of(new QrScanResponseDto("phone", dto));
        }

        // geo
        Matcher geo = GEO_PATTERN.matcher(trimmed);
        if (geo.find()) {
            QrParsedFormatDto dto = new QrParsedFormatDto(trimmed, "GEO");
            dto.setLatitude(Double.parseDouble(geo.group(1)));
            dto.setLongitude(Double.parseDouble(geo.group(2)));
            if (geo.groupCount() >= 3 && geo.group(3) != null) dto.setQuery(geo.group(3));
            return Optional.of(new QrScanResponseDto("geo", dto));
        }

        // bitcoin
        Matcher btc = BITCOIN_PATTERN.matcher(trimmed);
        if (btc.find()) {
            QrParsedFormatDto dto = new QrParsedFormatDto(trimmed, "BITCOIN");
            dto.setAddress(btc.group(1));
            if (btc.groupCount() >= 2 && btc.group(2) != null) dto.setAmount(btc.group(2));
            return Optional.of(new QrScanResponseDto("bitcoin", dto));
        }

        // vCard
        if (trimmed.toUpperCase().startsWith("BEGIN:VCARD")) {
            QrParsedFormatDto dto = new QrParsedFormatDto(trimmed, "VCARD");
            parseVCardFields(trimmed, dto);
            return Optional.of(new QrScanResponseDto("contact", dto));
        }

        // MeCard
        if (trimmed.toUpperCase().startsWith("MECARD:")) {
            QrParsedFormatDto dto = new QrParsedFormatDto(trimmed, "MECARD");
            parseMeCardFields(trimmed, dto);
            return Optional.of(new QrScanResponseDto("contact", dto));
        }

        return Optional.empty();
    }

    private void parseVCardFields(String vcard, QrParsedFormatDto dto) {
        String[] lines = vcard.split("[\\r\\n]+");
        for (String line : lines) {
            if (line.toUpperCase().startsWith("N:") && line.length() > 2) {
                String n = line.substring(2).trim().replace(";", " ");
                if (!n.isEmpty()) dto.setName(n);
            } else if (line.toUpperCase().startsWith("FN:") && dto.getName() == null && line.length() > 3) {
                dto.setName(line.substring(3).trim());
            } else if (line.toUpperCase().startsWith("TEL") && line.contains(":")) {
                int idx = line.indexOf(':');
                if (idx >= 0 && idx < line.length() - 1) dto.setPhone(line.substring(idx + 1).trim());
            } else if (line.toUpperCase().startsWith("EMAIL") && line.contains(":")) {
                int idx = line.indexOf(':');
                if (idx >= 0 && idx < line.length() - 1) dto.setEmail(line.substring(idx + 1).trim());
            } else if (line.toUpperCase().startsWith("ORG:") && line.length() > 4) {
                dto.setOrganization(line.substring(4).trim());
            }
        }
    }

    private void parseMeCardFields(String mecard, QrParsedFormatDto dto) {
        Pattern n = Pattern.compile("N:([^;]+)", Pattern.CASE_INSENSITIVE);
        Pattern tel = Pattern.compile("TEL:([^;]+)", Pattern.CASE_INSENSITIVE);
        Pattern email = Pattern.compile("EMAIL:([^;]+)", Pattern.CASE_INSENSITIVE);
        Pattern org = Pattern.compile("ORG:([^;]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = n.matcher(mecard);
        if (m.find()) dto.setName(m.group(1).replace(",", " ").trim());
        m = tel.matcher(mecard);
        if (m.find()) dto.setPhone(m.group(1).trim());
        m = email.matcher(mecard);
        if (m.find()) dto.setEmail(m.group(1).trim());
        m = org.matcher(mecard);
        if (m.find()) dto.setOrganization(m.group(1).trim());
    }

    private Optional<QrScanResponseDto> parseAndResolveJson(String data) {
        try {
            if (!data.startsWith("{")) return Optional.empty();
            JsonNode root = objectMapper.readTree(data);
            JsonNode typeNode = root.get("type");
            if (typeNode == null || !typeNode.isTextual()) return Optional.empty();

            String type = typeNode.asText().toLowerCase().trim();
            JsonNode idNode = root.get("id");
            JsonNode nameNode = root.get("name");

            if (idNode != null && idNode.isNumber()) {
                Long id = idNode.asLong();
                return resolveByTypeAndId(type, id);
            }
            if (nameNode != null && nameNode.isTextual()) {
                String name = nameNode.asText().trim();
                return resolveByTypeAndName(type, name);
            }
        } catch (Exception e) {
            log.debug("QR data is not valid JSON: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<QrScanResponseDto> parseAndResolveUrl(String data) {
        Matcher m = URL_PATTERN.matcher(data);
        if (!m.find()) return Optional.empty();

        String typeRaw = m.group(1).toLowerCase();
        long id = Long.parseLong(m.group(2));

        String type = mapUrlSegmentToType(typeRaw);
        return resolveByTypeAndId(type, id);
    }

    private String mapUrlSegmentToType(String segment) {
        return switch (segment.toLowerCase()) {
            case "categories" -> "category";
            case "subcategories" -> "subcategory";
            case "makes" -> "make";
            case "models" -> "model";
            case "components" -> "component";
            case "warranty" -> "warranty";
            case "amc" -> "amc";
            case "outlets" -> "outlet";
            case "vendors" -> "vendor";
            default -> segment;
        };
    }

    private Optional<QrScanResponseDto> resolveById(Long id) {
        // Try each entity type in order; first match wins
        Optional<QrScanResponseDto> r;
        r = fetchCategoryById(id);
        if (r.isPresent()) return r;
        r = fetchSubCategoryById(id);
        if (r.isPresent()) return r;
        r = fetchMakeById(id);
        if (r.isPresent()) return r;
        r = fetchModelById(id);
        if (r.isPresent()) return r;
        r = fetchComponentById(id);
        if (r.isPresent()) return r;
        r = fetchWarrantyById(id);
        if (r.isPresent()) return r;
        r = fetchAmcById(id);
        if (r.isPresent()) return r;
        r = fetchOutletById(id);
        if (r.isPresent()) return r;
        r = fetchVendorById(id);
        return r;
    }

    private Optional<QrScanResponseDto> resolveByName(String name) {
        Optional<QrScanResponseDto> r;
        r = fetchCategoryByName(name);
        if (r.isPresent()) return r;
        r = fetchSubCategoryByName(name);
        if (r.isPresent()) return r;
        r = fetchMakeByName(name);
        if (r.isPresent()) return r;
        r = fetchComponentByName(name);
        if (r.isPresent()) return r;
        r = fetchOutletByName(name);
        if (r.isPresent()) return r;
        r = fetchVendorByName(name);
        return r;
    }

    private Optional<QrScanResponseDto> resolveByTypeAndId(String type, Long id) {
        return switch (type) {
            case "category" -> fetchCategoryById(id);
            case "subcategory", "sub_category" -> fetchSubCategoryById(id);
            case "make" -> fetchMakeById(id);
            case "model" -> fetchModelById(id);
            case "component" -> fetchComponentById(id);
            case "warranty" -> fetchWarrantyById(id);
            case "amc" -> fetchAmcById(id);
            case "outlet" -> fetchOutletById(id);
            case "vendor" -> fetchVendorById(id);
            default -> Optional.empty();
        };
    }

    private Optional<QrScanResponseDto> resolveByTypeAndName(String type, String name) {
        return switch (type) {
            case "category" -> fetchCategoryByName(name);
            case "subcategory", "sub_category" -> fetchSubCategoryByName(name);
            case "make" -> fetchMakeByName(name);
            case "model" -> fetchModelByName(name);
            case "component" -> fetchComponentByName(name);
            case "outlet" -> fetchOutletByName(name);
            case "vendor" -> fetchVendorByName(name);
            default -> Optional.empty();
        };
    }

    private Optional<QrScanResponseDto> fetchCategoryById(Long id) {
        return categoryService.find(id)
                .map(dto -> new QrScanResponseDto("category", dto));
    }

    private Optional<QrScanResponseDto> fetchCategoryByName(String name) {
        return categoryRepo.findByCategoryNameIgnoreCase(name)
                .filter(c -> c.getActive() == null || c.getActive())
                .map(CategoryMapper::toDto)
                .map(dto -> new QrScanResponseDto("category", dto));
    }

    private Optional<QrScanResponseDto> fetchSubCategoryById(Long id) {
        return subCategoryService.find(id)
                .map(ProductSubCategoryMapper::toDto)
                .map(dto -> new QrScanResponseDto("subcategory", dto));
    }

    private Optional<QrScanResponseDto> fetchSubCategoryByName(String name) {
        return subCategoryRepo.findBySubCategoryNameIgnoreCase(name)
                .filter(s -> s.getActive() == null || s.getActive())
                .map(ProductSubCategoryMapper::toDto)
                .map(dto -> new QrScanResponseDto("subcategory", dto));
    }

    private Optional<QrScanResponseDto> fetchMakeById(Long id) {
        return makeService.find(id)
                .map(MakeMapper::toDto)
                .map(dto -> new QrScanResponseDto("make", dto));
    }

    private Optional<QrScanResponseDto> fetchMakeByName(String name) {
        return makeRepo.findByMakeNameIgnoreCase(name)
                .filter(m -> m.getActive() == null || m.getActive())
                .map(MakeMapper::toDto)
                .map(dto -> new QrScanResponseDto("make", dto));
    }

    private Optional<QrScanResponseDto> fetchModelById(Long id) {
        return modelService.find(id)
                .map(dto -> new QrScanResponseDto("model", dto));
    }

    private Optional<QrScanResponseDto> fetchModelByName(String name) {
        return modelRepo.findAll().stream()
                .filter(m -> m.getModelName() != null && m.getModelName().equalsIgnoreCase(name))
                .filter(m -> m.getActive() == null || m.getActive())
                .findFirst()
                .flatMap(m -> modelService.find(m.getModelId()))
                .map(dto -> new QrScanResponseDto("model", dto));
    }

    private Optional<QrScanResponseDto> fetchComponentById(Long id) {
        return componentService.find(id)
                .map(ComponentMapper::toDto)
                .map(dto -> new QrScanResponseDto("component", dto));
    }

    private Optional<QrScanResponseDto> fetchComponentByName(String name) {
        return componentRepo.findByComponentNameIgnoreCase(name)
                .filter(c -> c.getActive() == null || c.getActive())
                .map(ComponentMapper::toDto)
                .map(dto -> new QrScanResponseDto("component", dto));
    }

    private Optional<QrScanResponseDto> fetchWarrantyById(Long id) {
        return assetWarrantyService.find(id)
                .map(dto -> new QrScanResponseDto("warranty", dto));
    }

    private Optional<QrScanResponseDto> fetchAmcById(Long id) {
        return assetAmcService.find(id)
                .map(dto -> new QrScanResponseDto("amc", dto));
    }

    private Optional<QrScanResponseDto> fetchOutletById(Long id) {
        return outletService.find(id)
                .map(OutletMapper::toDto)
                .map(dto -> new QrScanResponseDto("outlet", dto));
    }

    private Optional<QrScanResponseDto> fetchOutletByName(String name) {
        return outletRepo.findByOutletNameIgnoreCase(name)
                .filter(o -> o.getActive() == null || o.getActive())
                .map(OutletMapper::toDto)
                .map(dto -> new QrScanResponseDto("outlet", dto));
    }

    private Optional<QrScanResponseDto> fetchVendorById(Long id) {
        return vendorService.find(id)
                .map(VendorMapper::toDto)
                .map(dto -> new QrScanResponseDto("vendor", dto));
    }

    private Optional<QrScanResponseDto> fetchVendorByName(String name) {
        return vendorRepo.findByVendorNameIgnoreCase(name)
                .filter(v -> v.getActive() == null || v.getActive())
                .map(VendorMapper::toDto)
                .map(dto -> new QrScanResponseDto("vendor", dto));
    }

    private boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Long.parseLong(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
