package com.example.asset.service;

import com.example.asset.dto.ProductScanPreviewResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ✅ ProductScanPreviewService
 * Service for previewing product information from mobile camera photos.
 * Extracts product details without saving to database.
 */
@Service
public class ProductScanPreviewService {

    private static final Logger log = LoggerFactory.getLogger(ProductScanPreviewService.class);

    private final OcrService ocrService;

    public ProductScanPreviewService(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    // ============================================================
    // 📸 SCAN PRODUCT FROM IMAGE (PREVIEW ONLY)
    // ============================================================
    public ProductScanPreviewResponse scanProductPreview(MultipartFile imageFile) {
        long startTime = System.currentTimeMillis();
        ProductScanPreviewResponse response = new ProductScanPreviewResponse();
        ProductScanPreviewResponse.ExtractedProductInfo productInfo = 
            new ProductScanPreviewResponse.ExtractedProductInfo();

        try {
            // 1. Extract text from image using OCR
            String extractedText;
            try {
                log.debug("📸 Starting OCR extraction for image: {}", imageFile.getOriginalFilename());
                extractedText = ocrService.extractText(imageFile);
                log.debug("✅ OCR extraction completed. Text length: {}", 
                        extractedText != null ? extractedText.length() : 0);
            } catch (NoClassDefFoundError e) {
                log.error("❌ NoClassDefFoundError during OCR: {}", e.getMessage(), e);
                String osName = System.getProperty("os.name").toLowerCase();
                String fixCmd = osName.contains("mac") ? "brew reinstall tesseract" :
                               osName.contains("win") ? "Reinstall Tesseract from https://github.com/UB-Mannheim/tesseract/wiki" :
                               "sudo apt-get install --reinstall tesseract-ocr";
                
                response.setStatus("ERROR");
                response.setMessage(
                    "Tesseract native library cannot be loaded.\n" +
                    "The Java bindings cannot find the Tesseract native library.\n\n" +
                    "Try: " + fixCmd + "\n" +
                    "After reinstalling, restart the application.\n\n" +
                    "Error: " + e.getMessage()
                );
                response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                return response;
            } catch (UnsatisfiedLinkError e) {
                log.error("❌ UnsatisfiedLinkError during OCR: {}", e.getMessage(), e);
                String osName = System.getProperty("os.name").toLowerCase();
                String fixCmd = osName.contains("mac") ? "brew reinstall tesseract" :
                               osName.contains("win") ? "Reinstall Tesseract from https://github.com/UB-Mannheim/tesseract/wiki" :
                               "sudo apt-get install --reinstall tesseract-ocr";
                
                response.setStatus("ERROR");
                response.setMessage(
                    "Tesseract native library cannot be linked.\n" +
                    "Please ensure Tesseract is properly installed.\n\n" +
                    "Try: " + fixCmd + "\n" +
                    "After reinstalling, restart the application.\n\n" +
                    "Error: " + e.getMessage()
                );
                response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                return response;
            } catch (net.sourceforge.tess4j.TesseractException e) {
                log.error("❌ TesseractException during OCR: {}", e.getMessage(), e);
                // Use the detailed error message from OcrService (includes installation instructions)
                response.setStatus("ERROR");
                String errorMessage = e.getMessage();
                // If the message doesn't contain installation instructions, add a generic one
                if (errorMessage == null || (!errorMessage.contains("Installation") && !errorMessage.contains("brew") && !errorMessage.contains("apt-get"))) {
                    errorMessage = "OCR extraction failed: " + errorMessage + 
                        "\n\nPlease ensure Tesseract OCR is installed. See: asset-service/docs/TESSERACT_INSTALLATION.md";
                }
                response.setMessage(errorMessage);
                response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                return response;
            } catch (java.io.IOException e) {
                log.error("❌ IOException during OCR: {}", e.getMessage(), e);
                response.setStatus("ERROR");
                response.setMessage("Failed to process image: " + e.getMessage());
                response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                return response;
            } catch (Exception e) {
                log.error("❌ Unexpected error during OCR: {}", e.getMessage(), e);
                response.setStatus("ERROR");
                response.setMessage("Unexpected error during image processing: " + e.getMessage());
                response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                return response;
            }
            
            if (extractedText == null || !StringUtils.hasText(extractedText)) {
                response.setStatus("ERROR");
                response.setMessage("No text could be extracted from the image. Please ensure the image is clear and contains readable text.");
                response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                return response;
            }

            productInfo.setExtractedText(extractedText);
            String normalizedText = extractedText.toUpperCase();

            // 2. Extract product information using AI agent patterns
            extractProductInfo(extractedText, normalizedText, productInfo);

            // 3. Calculate confidence score
            double confidence = calculateConfidence(productInfo);
            response.setConfidence(confidence);
            response.setProductInfo(productInfo);
            response.setStatus("SUCCESS");
            response.setMessage("Product information extracted successfully");
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);

            log.info("✅ Product scan preview completed in {}ms. Confidence: {}", 
                    response.getProcessingTimeMs(), confidence);

        } catch (Exception e) {
            log.error("❌ Product scan preview failed: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setMessage("Failed to scan product: " + e.getMessage());
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        }

        return response;
    }

    // ============================================================
    // 🏷️ EXTRACT PRODUCT INFORMATION
    // ============================================================
    private void extractProductInfo(String text, String normalizedText,
                                   ProductScanPreviewResponse.ExtractedProductInfo info) {
        
        // Extract Make/Brand
        String make = extractMake(text, normalizedText);
        info.setMakeName(make);
        info.setBrand(make);
        info.setManufacturer(make);

        // Extract Model
        String model = extractModel(text, normalizedText, make);
        info.setModelName(model);

        // Extract Serial Number
        String serial = extractSerialNumber(text);
        info.setSerialNumber(serial);

        // Extract Category
        String category = extractCategory(text, normalizedText);
        info.setCategoryName(category);

        // Extract SubCategory
        String subCategory = extractSubCategory(text, normalizedText, category);
        info.setSubCategoryName(subCategory);

        // Extract Description
        String description = extractDescription(text);
        info.setDescription(description);

        // Extract Components
        List<String> components = extractComponents(text, normalizedText);
        info.setComponentNames(components);
    }

    // ============================================================
    // 🔍 EXTRACT MAKE/BRAND
    // ============================================================
    private String extractMake(String text, String normalizedText) {
        // Common brand patterns
        Pattern[] patterns = {
            Pattern.compile("(?:brand|make|manufacturer)[\\s:]+([A-Z][A-Za-z0-9\\s&]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(Dell|HP|Lenovo|Apple|Samsung|LG|Sony|Panasonic|Canon|Nikon|Microsoft|Asus|Acer|Toshiba|IBM|Cisco|Juniper|NetApp|EMC|HPE|Oracle|Intel|AMD|NVIDIA|Western Digital|Seagate|Kingston|Crucial|Corsair|Logitech|Razer|SteelSeries|HyperX|Bose|JBL|Sennheiser|Audio-Technica|Shure|Yamaha|Pioneer|Denon|Marantz|Onkyo|Klipsch|Polk|Bowers & Wilkins|Bang & Olufsen|Bang & Olufsen|Bang & Olufsen)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^([A-Z][A-Za-z0-9\\s&]+)(?:\\s+(?:Ltd|Limited|Inc|Corporation|Corp|LLC|Pvt|Private|Technologies|Tech|Systems|Solutions))?", Pattern.MULTILINE)
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String make = matcher.group(1).trim();
                if (make.length() > 1 && make.length() < 100) {
                    return make;
                }
            }
        }

        return null;
    }

    // ============================================================
    // 📱 EXTRACT MODEL
    // ============================================================
    private String extractModel(String text, String normalizedText, String make) {
        Pattern[] patterns = {
            Pattern.compile("(?:model|model\\s*no|model\\s*number|part\\s*no|part\\s*number)[\\s:]+([A-Z0-9-]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b([A-Z]{1,3}[0-9]{3,}[A-Z0-9-]*)\\b"), // Model numbers like XPS13, ThinkPad X1, etc.
            Pattern.compile("(?:series|version|ver\\.?)[\\s:]+([A-Z0-9-]+)", Pattern.CASE_INSENSITIVE)
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String model = matcher.group(1).trim();
                if (model.length() > 2 && model.length() < 100) {
                    return model;
                }
            }
        }

        return null;
    }

    // ============================================================
    // 🔢 EXTRACT SERIAL NUMBER
    // ============================================================
    private String extractSerialNumber(String text) {
        Pattern[] patterns = {
            Pattern.compile("(?:serial|s\\/n|sn|serial\\s*no|serial\\s*number)[\\s:]+([A-Z0-9-]{5,})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b([A-Z]{2,}[0-9]{6,}[A-Z0-9-]*)\\b"), // Serial number patterns
            Pattern.compile("S\\/N[\\s:]+([A-Z0-9-]+)", Pattern.CASE_INSENSITIVE)
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String serial = matcher.group(1).trim();
                if (serial.length() > 4 && serial.length() < 100) {
                    return serial;
                }
            }
        }

        return null;
    }

    // ============================================================
    // 📂 EXTRACT CATEGORY
    // ============================================================
    private String extractCategory(String text, String normalizedText) {
        // Common product categories
        String[] categories = {
            "Laptop", "Desktop", "Server", "Workstation", "Tablet", "Smartphone", "Monitor",
            "Printer", "Scanner", "Projector", "Keyboard", "Mouse", "Headset", "Speaker",
            "Camera", "Router", "Switch", "Firewall", "Storage", "NAS", "UPS", "Battery",
            "Charger", "Adapter", "Cable", "Dongle", "Hub", "Dock", "Stand", "Mount"
        };

        for (String category : categories) {
            if (normalizedText.contains(category.toUpperCase())) {
                return category;
            }
        }

        // Try pattern matching
        Pattern categoryPattern = Pattern.compile(
            "(?:category|type|product\\s*type)[\\s:]+([A-Z][A-Za-z\\s]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = categoryPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    // ============================================================
    // 📁 EXTRACT SUBCATEGORY
    // ============================================================
    private String extractSubCategory(String text, String normalizedText, String category) {
        // Common subcategories based on category
        if (category != null && category.equalsIgnoreCase("Laptop")) {
            String[] subCategories = {"Gaming", "Business", "Ultrabook", "Workstation", "Chromebook"};
            for (String subCat : subCategories) {
                if (normalizedText.contains(subCat.toUpperCase())) {
                    return subCat;
                }
            }
        }

        // Try pattern matching
        Pattern subCategoryPattern = Pattern.compile(
            "(?:subcategory|sub\\s*category|variant|series)[\\s:]+([A-Z][A-Za-z\\s]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = subCategoryPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    // ============================================================
    // 📝 EXTRACT DESCRIPTION
    // ============================================================
    private String extractDescription(String text) {
        // Try to find description or specifications section
        Pattern descPattern = Pattern.compile(
            "(?:description|specifications|specs|features|details)[\\s:]+([A-Za-z0-9\\s.,-]{20,200})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = descPattern.matcher(text);
        if (matcher.find()) {
            String desc = matcher.group(1).trim();
            if (desc.length() > 20 && desc.length() < 500) {
                return desc;
            }
        }

        // If no specific description found, return first meaningful sentence
        String[] sentences = text.split("[.!?]");
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.length() > 30 && sentence.length() < 200) {
                return sentence;
            }
        }

        return null;
    }

    // ============================================================
    // 🔧 EXTRACT COMPONENTS
    // ============================================================
    private List<String> extractComponents(String text, String normalizedText) {
        List<String> components = new ArrayList<>();

        // Common component patterns
        String[] componentKeywords = {
            "RAM", "Memory", "Storage", "HDD", "SSD", "Processor", "CPU", "GPU", "Graphics",
            "Display", "Screen", "Battery", "Keyboard", "Touchpad", "Webcam", "Microphone",
            "Speaker", "Port", "USB", "HDMI", "Ethernet", "WiFi", "Bluetooth", "NFC"
        };

        for (String keyword : componentKeywords) {
            Pattern pattern = Pattern.compile(
                "\\b" + keyword + "[\\s:]+([A-Z0-9\\s]+)",
                Pattern.CASE_INSENSITIVE
            );
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String component = keyword + ": " + matcher.group(1).trim();
                if (component.length() < 100) {
                    components.add(component);
                }
            }
        }

        return components.isEmpty() ? null : components;
    }

    // ============================================================
    // 📊 CALCULATE CONFIDENCE
    // ============================================================
    private double calculateConfidence(ProductScanPreviewResponse.ExtractedProductInfo info) {
        double confidence = 0.0;
        
        if (StringUtils.hasText(info.getMakeName())) confidence += 0.25;
        if (StringUtils.hasText(info.getModelName())) confidence += 0.25;
        if (StringUtils.hasText(info.getSerialNumber())) confidence += 0.20;
        if (StringUtils.hasText(info.getCategoryName())) confidence += 0.15;
        if (StringUtils.hasText(info.getSubCategoryName())) confidence += 0.10;
        if (info.getComponentNames() != null && !info.getComponentNames().isEmpty()) confidence += 0.05;
        
        return Math.min(confidence, 1.0);
    }
}

