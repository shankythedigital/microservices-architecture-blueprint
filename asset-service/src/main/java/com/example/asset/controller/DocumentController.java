

package com.example.asset.controller;

import com.example.asset.dto.BulkDocumentRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.DocumentRequest;
import com.example.asset.entity.AssetDocument;
import com.example.asset.service.DocumentService;
import com.example.asset.service.ExcelParsingService;
import com.example.common.util.ResponseWrapper;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@RestController
@RequestMapping("/api/asset/v1/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;
    private final ExcelParsingService excelParsingService;

    public DocumentController(DocumentService documentService, ExcelParsingService excelParsingService) {
        this.documentService = documentService;
        this.excelParsingService = excelParsingService;
    }

    // ============================================================
    // 🟢 UNIVERSAL UPLOAD ENDPOINT
    // ============================================================
    @PostMapping("/upload")
    public ResponseEntity<ResponseWrapper<AssetDocument>> upload(
            @RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId,
            @RequestParam("userId") Long userId,
            @RequestParam("username") String username,
            @RequestParam(value = "projectType", required = false) String projectType,
            @RequestParam(value = "docType", required = false) String docType) {

        try {
            DocumentRequest req = new DocumentRequest();
            req.setEntityType(entityType);
            req.setEntityId(entityId);
            req.setUserId(userId);
            req.setUsername(username);
            req.setProjectType(projectType);
            req.setDocType(docType);

            AssetDocument saved = documentService.upload(headers, file, req);
            log.info("✅ {} document uploaded successfully by user={} for {} ID={}", docType, username, entityType, entityId);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "✅ Document uploaded successfully", saved));

        } catch (Exception e) {
            log.error("❌ Upload failed for entityType={} ID={}: {}", entityType, entityId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Upload failed: " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ SOFT DELETE DOCUMENT
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long id,
            @RequestBody DocumentRequest request) {
        try {
            documentService.softDelete(headers, id, request);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "🗑️ Document deleted successfully", null));
        } catch (Exception e) {
            log.error("❌ Delete failed for document ID={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Delete failed: " + e.getMessage(), null));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AssetDocument>> getDocumentDetails(@PathVariable Long id) {
        try {
            AssetDocument document = documentService.findById(id);
            if (document == null) {
                return ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "⚠️ Document not found", null));
            }

            return ResponseEntity.ok(new ResponseWrapper<>(true, "📄 Document details fetched successfully", document));

        } catch (Exception e) {
            log.error("❌ Failed to fetch document details for ID={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ Failed to fetch document details: " + e.getMessage(), null));
        }
    }


    // ============================================================
    // ⬇️ DOWNLOAD DOCUMENT (Actual File)
    // ============================================================
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            AssetDocument document = documentService.findById(id);
            if (document == null || document.getFilePath() == null) {
                return ResponseEntity.status(404).build();
            }

            Path filePath = Paths.get(document.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(404).build();
            }

            org.springframework.core.io.Resource fileResource =
                    new org.springframework.core.io.PathResource(filePath);

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            log.info("⬇️ File download initiated for documentId={}, file={}", id, filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                    .body(fileResource);

        } catch (Exception e) {
            log.error("❌ Download failed for document ID={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================
    // 📦 BULK UPLOAD DOCUMENTS (with file uploads)
    // ============================================================
    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<AssetDocument>>> bulkCreate(
            @RequestHeader HttpHeaders headers,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "request", required = false) String requestJson,
            @RequestPart(value = "request", required = false) BulkDocumentRequest requestPart,
            @RequestParam("userId") Long userId,
            @RequestParam("username") String username,
            @RequestParam(value = "projectType", required = false, defaultValue = "ASSET_SERVICE") String projectType) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Files cannot be empty", null));
            }

            BulkDocumentRequest request = requestPart;
            
            // If requestPart is null, try to parse from JSON string
            if (request == null && requestJson != null && !requestJson.trim().isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = 
                        new com.fasterxml.jackson.databind.ObjectMapper();
                    request = objectMapper.readValue(requestJson, BulkDocumentRequest.class);
                } catch (Exception e) {
                    log.error("❌ Failed to parse request JSON: {}", e.getMessage());
                    return ResponseEntity.badRequest()
                            .body(new ResponseWrapper<>(false, "Invalid request JSON format: " + e.getMessage(), null));
                }
            }

            if (request == null || request.getDocuments() == null || request.getDocuments().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Document list cannot be empty. Provide 'request' as JSON part or parameter.", null));
            }

            // Set user context
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);

            // Validate file count matches document count
            if (files.length != request.getDocuments().size()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, 
                            String.format("Number of files (%d) must match number of documents (%d)", 
                                files.length, request.getDocuments().size()), null));
            }

            BulkUploadResponse<AssetDocument> result =
                    documentService.bulkCreateWithFiles(headers, files, request);

            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    String.format("Bulk upload completed: %d/%d successful",
                            result.getSuccessCount(), result.getTotalCount()),
                    result
            ));
        } catch (Exception e) {
            log.error("❌ Failed to bulk create documents: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📦 BULK UPLOAD DOCUMENTS (with file paths - for existing files)
    // ============================================================
    @PostMapping(value = "/bulk/paths", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<AssetDocument>>> bulkCreateWithPaths(
            @RequestHeader HttpHeaders headers,
            @RequestBody BulkDocumentRequest request) {
        try {
            if (request.getDocuments() == null || request.getDocuments().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Document list cannot be empty", null));
            }

            BulkUploadResponse<AssetDocument> result =
                    documentService.bulkCreate(headers, request);

            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    String.format("Bulk upload completed: %d/%d successful",
                            result.getSuccessCount(), result.getTotalCount()),
                    result
            ));
        } catch (Exception e) {
            log.error("❌ Failed to bulk create documents: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📊 EXCEL BULK UPLOAD DOCUMENTS
    // ============================================================
    @PostMapping("/bulk/excel")
    public ResponseEntity<ResponseWrapper<BulkUploadResponse<AssetDocument>>> bulkUploadFromExcel(
            @RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("username") String username,
            @RequestParam(value = "projectType", required = false, defaultValue = "ASSET_SERVICE") String projectType) {
    
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "Excel file cannot be empty", null));
            }
    
            List<BulkDocumentRequest.SimpleDocumentDto> rows =
                    excelParsingService.parseDocumentsSimple(file);
    
            if (rows.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "No valid document data in Excel", null));
            }
    
            BulkDocumentRequest request = new BulkDocumentRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setProjectType(projectType);
            request.setDocuments(rows);
    
            BulkUploadResponse<AssetDocument> result =
                    documentService.bulkCreate(headers, request);
    
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    String.format("Excel upload completed: %d/%d successful",
                            result.getSuccessCount(), result.getTotalCount()),
                    result
            ));
    
        } catch (Exception e) {
            log.error("❌ Failed to bulk upload documents from Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

}


