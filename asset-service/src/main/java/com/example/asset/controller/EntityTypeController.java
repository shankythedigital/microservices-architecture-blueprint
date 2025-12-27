package com.example.asset.controller;

import com.example.asset.entity.EntityTypeMaster;
import com.example.asset.service.EntityTypeService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * ✅ EntityTypeController
 * REST controller for EntityType operations.
 * Provides endpoints to list and query entity types.
 */
@RestController
@RequestMapping("/api/asset/v1/entity-types")
public class EntityTypeController {

    private static final Logger log = LoggerFactory.getLogger(EntityTypeController.class);

    private final EntityTypeService entityTypeService;

    public EntityTypeController(EntityTypeService entityTypeService) {
        this.entityTypeService = entityTypeService;
    }

    // ============================================================
    // 📋 LIST ALL ENTITY TYPES
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<EntityTypeMaster>>> listAll() {
        try {
            List<EntityTypeMaster> entityTypes = entityTypeService.listAll();
            log.info("📋 Retrieved {} entity types", entityTypes.size());
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "Entity types fetched successfully", entityTypes));
        } catch (Exception e) {
            log.error("❌ Failed to fetch entity types: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST ACTIVE ENTITY TYPES
    // ============================================================
    @GetMapping("/active")
    public ResponseEntity<ResponseWrapper<List<EntityTypeMaster>>> listActive() {
        try {
            List<EntityTypeMaster> entityTypes = entityTypeService.listActive();
            log.info("📋 Retrieved {} active entity types", entityTypes.size());
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "Active entity types fetched successfully", entityTypes));
        } catch (Exception e) {
            log.error("❌ Failed to fetch active entity types: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 FIND BY CODE
    // ============================================================
    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<EntityTypeMaster>> findByCode(@PathVariable String code) {
        try {
            Optional<EntityTypeMaster> entityType = entityTypeService.findByCode(code);
            if (entityType.isPresent()) {
                log.info("🔍 Found entity type: {}", code);
                return ResponseEntity.ok(
                        new ResponseWrapper<>(true, "Entity type found", entityType.get()));
            } else {
                log.warn("⚠️ Entity type not found: {}", code);
                return ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "Entity type not found: " + code, null));
            }
        } catch (Exception e) {
            log.error("❌ Failed to fetch entity type by code: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 FIND BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<EntityTypeMaster>> findById(@PathVariable Integer id) {
        try {
            Optional<EntityTypeMaster> entityType = entityTypeService.findById(id);
            if (entityType.isPresent()) {
                log.info("🔍 Found entity type: id={}", id);
                return ResponseEntity.ok(
                        new ResponseWrapper<>(true, "Entity type found", entityType.get()));
            } else {
                log.warn("⚠️ Entity type not found: id={}", id);
                return ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "Entity type not found: " + id, null));
            }
        } catch (Exception e) {
            log.error("❌ Failed to fetch entity type by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✅ VALIDATE ENTITY TYPE
    // ============================================================
    @GetMapping("/validate/{code}")
    public ResponseEntity<ResponseWrapper<Boolean>> validate(@PathVariable String code) {
        try {
            boolean isValid = entityTypeService.isValid(code);
            log.info("✅ Validation result for {}: {}", code, isValid);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "Validation completed", isValid));
        } catch (Exception e) {
            log.error("❌ Failed to validate entity type: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔄 INITIALIZE ENTITY TYPES (Admin/System Endpoint)
    // ============================================================
    @PostMapping("/initialize")
    public ResponseEntity<ResponseWrapper<String>> initialize() {
        try {
            entityTypeService.initializeEntityTypes();
            log.info("✅ Entity types initialized successfully");
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "Entity types initialized successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to initialize entity types: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }
}
