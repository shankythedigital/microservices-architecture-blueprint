package com.example.asset.controller;

import com.example.asset.entity.StatusMaster;
import com.example.asset.service.StatusService;
import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * ✅ StatusController
 * REST controller for Status operations.
 * Provides endpoints to list and query status values.
 */
@RestController
@RequestMapping("/api/asset/v1/statuses")
public class StatusController {

    private static final Logger log = LoggerFactory.getLogger(StatusController.class);

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    // ============================================================
    // 📋 LIST ALL STATUSES
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<StatusMaster>>> listAll() {
        try {
            List<StatusMaster> statuses = statusService.listAll();
            log.info("📋 Retrieved {} statuses", statuses.size());
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "Statuses fetched successfully", statuses));
        } catch (Exception e) {
            log.error("❌ Failed to fetch statuses: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST ACTIVE STATUSES
    // ============================================================
    @GetMapping("/active")
    public ResponseEntity<ResponseWrapper<List<StatusMaster>>> listActive() {
        try {
            List<StatusMaster> statuses = statusService.listActive();
            log.info("📋 Retrieved {} active statuses", statuses.size());
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "Active statuses fetched successfully", statuses));
        } catch (Exception e) {
            log.error("❌ Failed to fetch active statuses: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST STATUSES BY CATEGORY
    // ============================================================
    @GetMapping("/category/{category}")
    public ResponseEntity<ResponseWrapper<List<StatusMaster>>> listByCategory(@PathVariable String category) {
        try {
            List<StatusMaster> statuses = statusService.listByCategory(category);
            log.info("📋 Retrieved {} statuses for category: {}", statuses.size(), category);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, 
                            String.format("Statuses for category '%s' fetched successfully", category), 
                            statuses));
        } catch (Exception e) {
            log.error("❌ Failed to fetch statuses by category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST ACTIVE STATUSES BY CATEGORY
    // ============================================================
    @GetMapping("/category/{category}/active")
    public ResponseEntity<ResponseWrapper<List<StatusMaster>>> listActiveByCategory(@PathVariable String category) {
        try {
            List<StatusMaster> statuses = statusService.listActiveByCategory(category);
            log.info("📋 Retrieved {} active statuses for category: {}", statuses.size(), category);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, 
                            String.format("Active statuses for category '%s' fetched successfully", category), 
                            statuses));
        } catch (Exception e) {
            log.error("❌ Failed to fetch active statuses by category: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 FIND BY CODE
    // ============================================================
    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<StatusMaster>> findByCode(@PathVariable String code) {
        try {
            Optional<StatusMaster> status = statusService.findByCode(code);
            if (status.isPresent()) {
                log.info("🔍 Found status: {}", code);
                return ResponseEntity.ok(
                        new ResponseWrapper<>(true, "Status found", status.get()));
            } else {
                log.warn("⚠️ Status not found: {}", code);
                return ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "Status not found: " + code, null));
            }
        } catch (Exception e) {
            log.error("❌ Failed to fetch status by code: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 FIND BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<StatusMaster>> findById(@PathVariable Integer id) {
        try {
            Optional<StatusMaster> status = statusService.findById(id);
            if (status.isPresent()) {
                log.info("🔍 Found status: id={}", id);
                return ResponseEntity.ok(
                        new ResponseWrapper<>(true, "Status found", status.get()));
            } else {
                log.warn("⚠️ Status not found: id={}", id);
                return ResponseEntity.status(404)
                        .body(new ResponseWrapper<>(false, "Status not found: " + id, null));
            }
        } catch (Exception e) {
            log.error("❌ Failed to fetch status by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✅ VALIDATE STATUS
    // ============================================================
    @GetMapping("/validate/{code}")
    public ResponseEntity<ResponseWrapper<Boolean>> validate(@PathVariable String code) {
        try {
            boolean isValid = statusService.isValid(code);
            log.info("✅ Validation result for {}: {}", code, isValid);
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "Validation completed", isValid));
        } catch (Exception e) {
            log.error("❌ Failed to validate status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔄 INITIALIZE STATUSES (Admin/System Endpoint)
    // ============================================================
    @PostMapping("/initialize")
    public ResponseEntity<ResponseWrapper<String>> initialize() {
        try {
            statusService.initializeStatuses();
            log.info("✅ Statuses initialized successfully");
            return ResponseEntity.ok(
                    new ResponseWrapper<>(true, "Statuses initialized successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to initialize statuses: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }
}
