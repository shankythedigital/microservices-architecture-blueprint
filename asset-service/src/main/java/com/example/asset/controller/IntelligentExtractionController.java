package com.example.asset.controller;

import com.example.asset.dto.IntelligentExtractionRequest;
import com.example.asset.dto.IntelligentExtractionResponse;
import com.example.asset.dto.ProductScanPreviewResponse;
import com.example.asset.service.IntelligentDocumentExtractionService;
import com.example.asset.service.OcrService;
import com.example.asset.service.ProductScanPreviewService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * ✅ IntelligentExtractionController
 * REST controller for intelligent document extraction that reads various file formats
 * and extracts comprehensive asset information including category, subcategory, make, model,
 * components, warranty, AMC, and more.
 * 
 * This controller handles different document types, naming conventions, and formats intelligently.
 */
@RestController
@RequestMapping("/api/asset/v1/intelligent-extraction")
public class IntelligentExtractionController {

    private static final Logger log = LoggerFactory.getLogger(IntelligentExtractionController.class);

    private final IntelligentDocumentExtractionService extractionService;
    private final OcrService ocrService;
    private final ProductScanPreviewService scanPreviewService;

    public IntelligentExtractionController(
            IntelligentDocumentExtractionService extractionService,
            OcrService ocrService,
            ProductScanPreviewService scanPreviewService) {
        this.extractionService = extractionService;
        this.ocrService = ocrService;
        this.scanPreviewService = scanPreviewService;
    }

    // ============================================================
    // 🤖 INTELLIGENT DOCUMENT EXTRACTION
    // ============================================================
    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseWrapper<IntelligentExtractionResponse>> extractComprehensiveInfo(
            @RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "projectType", required = false, defaultValue = "ASSET_SERVICE") String projectType,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "autoCreateEntities", required = false, defaultValue = "true") Boolean autoCreateEntities,
            @RequestParam(value = "extractWarranty", required = false, defaultValue = "true") Boolean extractWarranty,
            @RequestParam(value = "extractAmc", required = false, defaultValue = "true") Boolean extractAmc,
            @RequestParam(value = "extractComponents", required = false, defaultValue = "true") Boolean extractComponents,
            @RequestParam(value = "existingAssetId", required = false) Long existingAssetId) {

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ File is required", null));
            }

            if (!ocrService.isValidFile(file)) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, 
                                "❌ Unsupported file format. Supported: Images (JPG, PNG, GIF), PDF, Word (DOC, DOCX), Excel (XLS, XLSX), PowerPoint (PPT, PPTX)", 
                                null));
            }

            // Prepare request
            IntelligentExtractionRequest request = new IntelligentExtractionRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setDocumentType(documentType);
            request.setAutoCreateEntities(autoCreateEntities);
            request.setExtractWarranty(extractWarranty);
            request.setExtractAmc(extractAmc);
            request.setExtractComponents(extractComponents);
            request.setExistingAssetId(existingAssetId);

            // Perform intelligent extraction
            IntelligentExtractionResponse response = extractionService.extractComprehensiveInfo(
                    headers, file, request);

            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    response.getStatus().equals("SUCCESS") ? 
                        "✅ Comprehensive asset information extracted and stored successfully" :
                        response.getMessage(),
                    response
            ));

        } catch (Exception e) {
            log.error("❌ Intelligent extraction failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, 
                            "❌ Intelligent extraction failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📸 SCAN PRODUCT PREVIEW (MOBILE CAMERA - NO DATA SAVED)
    // ============================================================
    /**
     * 📸 Scan product from mobile camera photo - Preview only (no data saved)
     * 
     * This endpoint is designed for mobile apps to quickly scan product labels,
     * barcodes, or product information sheets and get a preview of extracted data
     * without saving anything to the database.
     * 
     * Use case: User takes a photo of a product label/spec sheet with mobile camera,
     * system extracts product information (category, subcategory, make, model, etc.)
     * and displays it for review before saving.
     * 
     * @param imageFile Image file from mobile camera (JPG, PNG, etc.)
     * @return ProductScanPreviewResponse with extracted product information
     */
    @PostMapping(value = "/scan-preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseWrapper<ProductScanPreviewResponse>> scanProductPreview(
            @RequestParam("file") MultipartFile imageFile) {
        
        log.info("📸 Product scan preview request received. File: {}, Size: {} bytes", 
                imageFile.getOriginalFilename(), imageFile.getSize());

        try {
            // Validate file
            if (imageFile == null || imageFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ Image file is required", null));
            }

            // Validate image format
            String contentType = imageFile.getContentType();
            if (contentType == null || 
                (!contentType.startsWith("image/") && !contentType.equals("application/octet-stream"))) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, 
                                "❌ Invalid file type. Please upload an image file (JPG, PNG, etc.)", null));
            }

            // Scan product and get preview (no data saved)
            ProductScanPreviewResponse response = scanPreviewService.scanProductPreview(imageFile);

            if ("SUCCESS".equals(response.getStatus())) {
                log.info("✅ Product scan preview completed successfully. Confidence: {}, Processing time: {}ms", 
                        response.getConfidence(), response.getProcessingTimeMs());
                return ResponseEntity.ok(new ResponseWrapper<>(
                        true,
                        "✅ Product information extracted successfully (Preview only - no data saved)",
                        response
                ));
            } else {
                log.warn("⚠️ Product scan preview completed with errors: {}", response.getMessage());
                return ResponseEntity.ok(new ResponseWrapper<>(
                        false,
                        response.getMessage(),
                        response
                ));
            }

        } catch (Exception e) {
            log.error("❌ Product scan preview failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, 
                            "❌ Failed to scan product: " + e.getMessage(), null));
        }
    }
}

