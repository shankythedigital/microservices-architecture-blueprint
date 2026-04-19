package com.example.asset.controller;

import com.example.asset.dto.DocumentRequest;
import com.example.asset.dto.OcrCorrectionRequest;
import com.example.asset.dto.ProductOcrRequest;
import com.example.asset.dto.ProductOcrResponse;
import com.example.asset.entity.AssetDocument;
import com.example.asset.entity.OcrModelMetadata;
import com.example.asset.entity.OcrTrainingData;
import com.example.asset.service.DocumentService;
import com.example.asset.service.OcrService;
import com.example.asset.service.OcrTrainingService;
import com.example.asset.service.ProductOcrAiAgentService;
import com.example.common.util.ResponseWrapper;
import com.example.asset.service.OcrException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * ✅ ProductOcrController
 * REST controller for OCR-based product extraction from images.
 * 
 * Endpoints:
 * - POST /api/asset/v1/products/ocr/scan - Upload image and extract product info
 * - POST /api/asset/v1/products/ocr/extract - Extract product info from OCR text
 */
@RestController
@RequestMapping("/api/asset/v1/products/ocr")
public class ProductOcrController {

    private static final Logger log = LoggerFactory.getLogger(ProductOcrController.class);

    private final OcrService ocrService;
    private final ProductOcrAiAgentService aiAgentService;
    private final OcrTrainingService trainingService;
    private final DocumentService documentService;

    public ProductOcrController(OcrService ocrService, ProductOcrAiAgentService aiAgentService, 
                               OcrTrainingService trainingService, DocumentService documentService) {
        this.ocrService = ocrService;
        this.aiAgentService = aiAgentService;
        this.trainingService = trainingService;
        this.documentService = documentService;
    }

    // ============================================================
    // 📸 SCAN IMAGE AND EXTRACT PRODUCT INFO
    // ============================================================
    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseWrapper<ProductOcrResponse>> scanImage(
            @RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "projectType", required = false, defaultValue = "ASSET_SERVICE") String projectType,
            @RequestParam(value = "subCategoryId", required = false) Long subCategoryId,
            @RequestParam(value = "subCategoryName", required = false) String subCategoryName,
            @RequestParam(value = "autoCreateMake", required = false, defaultValue = "true") Boolean autoCreateMake,
            @RequestParam(value = "autoCreateModel", required = false, defaultValue = "true") Boolean autoCreateModel) {

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ Image file is required", null));
            }

            if (!ocrService.isValidFile(file)) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, 
                                "❌ Unsupported file format. Supported: Images (JPG, PNG, GIF), PDF, Word (DOC, DOCX), Excel (XLS, XLSX), PowerPoint (PPT, PPTX)", 
                                null));
            }

            // Extract text from image using OCR
            String ocrText;
            try {
                ocrText = ocrService.extractText(file);
                log.info("✅ OCR extraction completed. Extracted {} characters", ocrText.length());
            } catch (OcrException e) {
                log.error("❌ OCR extraction failed: {}", e.getMessage(), e);
                return ResponseEntity.internalServerError()
                        .body(new ResponseWrapper<>(false, 
                                "❌ OCR extraction failed: " + e.getMessage(), null));
            } catch (IOException e) {
                log.error("❌ File processing failed: {}", e.getMessage(), e);
                return ResponseEntity.internalServerError()
                        .body(new ResponseWrapper<>(false, 
                                "❌ File processing failed: " + e.getMessage(), null));
            }

            // Prepare request
            ProductOcrRequest request = new ProductOcrRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setSubCategoryId(subCategoryId);
            request.setSubCategoryName(subCategoryName);
            request.setAutoCreateMake(autoCreateMake);
            request.setAutoCreateModel(autoCreateModel);

            // Analyze OCR text and extract product info
            ProductOcrResponse response = aiAgentService.analyzeAndExtract(headers, ocrText, request);

            // Save training data for learning
            try {
                byte[] imageBytes = file.getBytes();
                trainingService.saveTrainingData(
                    ocrText,
                    response.getExtractedInfo(),
                    response,
                    userId,
                    username,
                    response.getSubCategory() != null ? response.getSubCategory().getSubCategoryId() : request.getSubCategoryId(),
                    imageBytes
                );
            } catch (Exception e) {
                log.warn("⚠️ Failed to save training data: {}", e.getMessage());
                // Don't fail the request if training data save fails
            }

            // Save document to database if entities were created
            try {
                if (response.getModel() != null || response.getMake() != null || 
                    response.getCategory() != null || response.getSubCategory() != null) {
                    DocumentRequest docRequest = new DocumentRequest();
                    docRequest.setUserId(userId);
                    docRequest.setUsername(username);
                    docRequest.setProjectType(projectType);
                    docRequest.setDocType("OCR_PRODUCT_IMAGE");
                    
                    // Link document to the most specific entity created
                    if (response.getModel() != null) {
                        docRequest.setEntityType("PRODUCT_MODEL");
                        docRequest.setEntityId(response.getModel().getModelId());
                    } else if (response.getMake() != null) {
                        docRequest.setEntityType("PRODUCT_MAKE");
                        docRequest.setEntityId(response.getMake().getMakeId());
                    } else if (response.getSubCategory() != null) {
                        docRequest.setEntityType("PRODUCT_SUBCATEGORY");
                        docRequest.setEntityId(response.getSubCategory().getSubCategoryId());
                    } else if (response.getCategory() != null) {
                        docRequest.setEntityType("PRODUCT_CATEGORY");
                        docRequest.setEntityId(response.getCategory().getCategoryId());
                    }
                    
                    AssetDocument savedDoc = documentService.upload(headers, file, docRequest);
                    response.setDocument(savedDoc);
                    log.info("✅ Document saved: documentId={}, entityType={}, entityId={}", 
                            savedDoc.getDocumentId(), docRequest.getEntityType(), docRequest.getEntityId());
                }
            } catch (Exception e) {
                log.warn("⚠️ Failed to save document: {}", e.getMessage());
                // Don't fail the request if document save fails
            }

            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "✅ Product information extracted and stored successfully in database",
                    response
            ));

        } catch (Exception e) {
            log.error("❌ OCR scan failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, 
                            "❌ OCR scan failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📄 EXTRACT PRODUCT INFO FROM OCR TEXT (for pre-extracted text)
    // ============================================================
    @PostMapping(value = "/extract", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<ProductOcrResponse>> extractFromText(
            @RequestHeader HttpHeaders headers,
            @RequestBody ProductOcrRequest request) {

        try {
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ Request body is required", null));
            }

            // This endpoint expects OCR text to be provided separately
            // For now, we'll require it in a separate field or use the scan endpoint
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, 
                            "❌ Use /scan endpoint to upload image, or provide ocrText in request", null));

        } catch (Exception e) {
            log.error("❌ Product extraction failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, 
                            "❌ Product extraction failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📄 EXTRACT PRODUCT INFO FROM OCR TEXT (with text input)
    // ============================================================
    @PostMapping(value = "/extract-text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<ProductOcrResponse>> extractFromTextInput(
            @RequestHeader HttpHeaders headers,
            @RequestParam("ocrText") String ocrText,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "projectType", required = false, defaultValue = "ASSET_SERVICE") String projectType,
            @RequestParam(value = "subCategoryId", required = false) Long subCategoryId,
            @RequestParam(value = "subCategoryName", required = false) String subCategoryName,
            @RequestParam(value = "autoCreateMake", required = false, defaultValue = "true") Boolean autoCreateMake,
            @RequestParam(value = "autoCreateModel", required = false, defaultValue = "true") Boolean autoCreateModel) {

        try {
            if (ocrText == null || ocrText.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ OCR text is required", null));
            }

            // Prepare request
            ProductOcrRequest request = new ProductOcrRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setSubCategoryId(subCategoryId);
            request.setSubCategoryName(subCategoryName);
            request.setAutoCreateMake(autoCreateMake);
            request.setAutoCreateModel(autoCreateModel);

            // Analyze OCR text and extract product info
            ProductOcrResponse response = aiAgentService.analyzeAndExtract(headers, ocrText, request);

            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "✅ Product information extracted successfully",
                    response
            ));

        } catch (Exception e) {
            log.error("❌ Product extraction failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, 
                            "❌ Product extraction failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ SUBMIT CORRECTION (User feedback)
    // ============================================================
    @PostMapping(value = "/correct", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<OcrTrainingData>> submitCorrection(
            @RequestHeader HttpHeaders headers,
            @RequestBody OcrCorrectionRequest correctionRequest) {
        
        try {
            if (correctionRequest == null || correctionRequest.getTrainingId() == null) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ Training ID is required", null));
            }

            OcrTrainingData corrected = trainingService.saveCorrection(
                correctionRequest.getTrainingId(),
                correctionRequest.getCorrectedMake(),
                correctionRequest.getCorrectedModel(),
                correctionRequest.getCorrectedSerial(),
                correctionRequest.getUsername() != null ? correctionRequest.getUsername() : "SYSTEM"
            );

            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "✅ Correction saved successfully. The system will learn from this feedback.",
                    corrected
            ));

        } catch (Exception e) {
            log.error("❌ Failed to save correction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, 
                            "❌ Failed to save correction: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🏋️ TRAIN MODEL (Batch learning from all corrections)
    // ============================================================
    @PostMapping(value = "/train", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<OcrModelMetadata>> trainModel(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "username", required = false, defaultValue = "SYSTEM") String username) {
        
        try {
            OcrModelMetadata model = trainingService.trainModel(username);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    String.format("✅ Model training completed. Version: %s, Accuracy: %.2f%%", 
                            model.getModelVersion(), 
                            model.getAccuracyScore() != null ? model.getAccuracyScore() * 100 : 0),
                    model
            ));

        } catch (Exception e) {
            log.error("❌ Model training failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, 
                            "❌ Model training failed: " + e.getMessage(), null));
        }
    }
}

