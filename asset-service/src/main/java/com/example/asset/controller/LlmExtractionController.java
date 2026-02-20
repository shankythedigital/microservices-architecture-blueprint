package com.example.asset.controller;

import com.example.asset.config.LlmProperties;
import com.example.asset.dto.LlmAssetExtractionResult;
import com.example.asset.service.DocumentLlmAgentService;
import com.example.asset.service.OcrService;
import com.example.common.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * LLM and agentic AI document extraction: upload a document, scan it, extract asset data via LLM, return JSON.
 */
@RestController
@RequestMapping("/api/asset/v1/llm-extraction")
public class LlmExtractionController {

    private static final Logger log = LoggerFactory.getLogger(LlmExtractionController.class);

    private final OcrService ocrService;
    private final DocumentLlmAgentService llmAgentService;
    private final LlmProperties llmProperties;

    public LlmExtractionController(OcrService ocrService,
                                   DocumentLlmAgentService llmAgentService,
                                   LlmProperties llmProperties) {
        this.ocrService = ocrService;
        this.llmAgentService = llmAgentService;
        this.llmProperties = llmProperties;
    }

    @Operation(summary = "Extract asset data from document using LLM",
            description = "Upload a document (image, PDF, Word, Excel, PowerPoint). The document is scanned (OCR/text extraction), " +
                    "then an LLM agent extracts structured asset data. Response is returned in JSON format. " +
                    "Configure app.llm.api-url and app.llm.api-key (or OPENAI_API_KEY) for LLM; when disabled, returns empty extraction.")
    @ApiResponse(responseCode = "200", description = "Extraction result in JSON",
            content = @Content(schema = @Schema(implementation = LlmAssetExtractionResult.class)))
    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<LlmAssetExtractionResult>> extract(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false) String documentType) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "File is required", null));
        }

        if (!ocrService.isValidFile(file)) {
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false,
                            "Unsupported format. Use: Images (JPG, PNG, GIF), PDF, Word (DOC, DOCX), Excel (XLS, XLSX), PowerPoint (PPT, PPTX)",
                            null));
        }

        try {
            String extractedText = ocrService.extractText(file);
            if (extractedText == null || extractedText.isBlank()) {
                return ResponseEntity.ok(new ResponseWrapper<>(false, "No text could be extracted from the document", emptyResult(file.getOriginalFilename())));
            }

            String detectedType = documentType != null && !documentType.isBlank()
                    ? documentType.toUpperCase()
                    : llmAgentService.detectDocumentType(extractedText);

            LlmAssetExtractionResult result = llmAgentService.extractFromText(extractedText, detectedType);
            result.setExtractedTextPreview(truncate(extractedText, 2000));

            return ResponseEntity.ok(new ResponseWrapper<>(
                    llmProperties.isEnabled(),
                    "Asset data extracted; response in JSON format.",
                    result
            ));
        } catch (Exception e) {
            log.error("LLM extraction failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "Extraction failed: " + e.getMessage(), null));
        }
    }

    private static LlmAssetExtractionResult emptyResult(String filename) {
        LlmAssetExtractionResult r = new LlmAssetExtractionResult();
        r.setDocumentType("UNKNOWN");
        r.setExtractionMethod("LLM_AGENT");
        r.setConfidence(0.0);
        return r;
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
