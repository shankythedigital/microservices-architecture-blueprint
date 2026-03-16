package com.example.asset.controller;

import com.example.asset.entity.DocumentTypeMaster;
import com.example.asset.service.DocumentTypeMasterService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * ✅ DocumentTypeMasterController
 * REST controller for Document Type Master operations.
 * Provides endpoints to list and query document type values from the database.
 * Validation uses DocumentTypeMaster (DB-backed document_type_master table).
 */
@RestController
@RequestMapping("/api/asset/v1/document-types")
public class DocumentTypeMasterController {

    private static final Logger log = LoggerFactory.getLogger(DocumentTypeMasterController.class);

    private final DocumentTypeMasterService documentTypeMasterService;

    public DocumentTypeMasterController(DocumentTypeMasterService documentTypeMasterService) {
        this.documentTypeMasterService = documentTypeMasterService;
    }

    // ============================================================
    // 📋 LIST ALL DOCUMENT TYPES
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<DocumentTypeMaster>>> listAll() {
        try {
            List<DocumentTypeMaster> types = documentTypeMasterService.listAll();
            log.info("📋 Retrieved {} document types", types.size());
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "Document types fetched successfully", types));
        } catch (Exception e) {
            log.error("❌ Failed to fetch document types: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST ACTIVE DOCUMENT TYPES
    // ============================================================
    @GetMapping("/active")
    public ResponseEntity<ResponseWrapper<List<DocumentTypeMaster>>> listActive() {
        try {
            List<DocumentTypeMaster> types = documentTypeMasterService.listActive();
            log.info("📋 Retrieved {} active document types", types.size());
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "Active document types fetched successfully", types));
        } catch (Exception e) {
            log.error("❌ Failed to fetch active document types: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 FIND BY CODE
    // ============================================================
    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<DocumentTypeMaster>> findByCode(@PathVariable String code) {
        try {
            Optional<DocumentTypeMaster> docType = documentTypeMasterService.findByCode(code);
            if (docType.isPresent()) {
                return ResponseEntity.ok(
                        new ResponseWrapper<>(true, "Document type fetched successfully", docType.get()));
            }
            return ResponseEntity.status(404)
                    .body(new ResponseWrapper<>(false, "Document type not found for code: " + code, null));
        } catch (Exception e) {
            log.error("❌ Failed to fetch document type by code: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 FIND BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<DocumentTypeMaster>> findById(@PathVariable Long id) {
        try {
            Optional<DocumentTypeMaster> docType = documentTypeMasterService.findById(id);
            if (docType.isPresent()) {
                return ResponseEntity.ok(
                        new ResponseWrapper<>(true, "Document type fetched successfully", docType.get()));
            }
            return ResponseEntity.status(404)
                    .body(new ResponseWrapper<>(false, "Document type not found for id: " + id, null));
        } catch (Exception e) {
            log.error("❌ Failed to fetch document type by id: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }
}
