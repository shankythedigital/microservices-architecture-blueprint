package com.example.asset.controller;

import com.example.asset.entity.ComplianceRule;
import com.example.common.util.ResponseWrapper;
import com.example.asset.service.ComplianceRuleService;
import com.example.asset.service.ComplianceRuleTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * ✅ ComplianceRuleController
 * REST endpoints for managing compliance rules.
 */
@RestController
@RequestMapping("/api/asset/v1/compliance/rules")
public class ComplianceRuleController {

    private static final Logger log = LoggerFactory.getLogger(ComplianceRuleController.class);
    private final ComplianceRuleService ruleService;
    private final ComplianceRuleTemplateService templateService;

    public ComplianceRuleController(ComplianceRuleService ruleService,
                                   ComplianceRuleTemplateService templateService) {
        this.ruleService = ruleService;
        this.templateService = templateService;
    }

    // ============================================================
    // 📋 LIST ALL RULES
    // ============================================================
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ComplianceRule>>> listAll(@RequestHeader HttpHeaders headers) {
        try {
            List<ComplianceRule> rules = ruleService.listAll();
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Compliance rules fetched successfully",
                    rules));
        } catch (Exception e) {
            log.error("❌ Failed to fetch compliance rules: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 LIST RULES BY ENTITY TYPE
    // ============================================================
    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<ResponseWrapper<List<ComplianceRule>>> listByEntityType(
            @RequestHeader HttpHeaders headers,
            @PathVariable String entityType) {
        try {
            List<ComplianceRule> rules = ruleService.listByEntityType(entityType);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Compliance rules fetched for entity type: " + entityType,
                    rules));
        } catch (Exception e) {
            log.error("❌ Failed to fetch compliance rules: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔍 FIND RULE BY ID
    // ============================================================
    @GetMapping("/{ruleId}")
    public ResponseEntity<ResponseWrapper<ComplianceRule>> findById(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long ruleId) {
        try {
            Optional<ComplianceRule> rule = ruleService.findById(ruleId);
            if (rule.isPresent()) {
                return ResponseEntity.ok(new ResponseWrapper<>(
                        true,
                        "Compliance rule fetched",
                        rule.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Failed to fetch compliance rule: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ➕ CREATE RULE
    // ============================================================
    @PostMapping
    public ResponseEntity<ResponseWrapper<ComplianceRule>> create(
            @RequestHeader HttpHeaders headers,
            @RequestBody ComplianceRule rule,
            @RequestParam(value = "createdBy", defaultValue = "SYSTEM") String createdBy) {
        try {
            ComplianceRule created = ruleService.create(rule, createdBy);
            log.info("✅ Compliance rule created: {}", created.getRuleCode());
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Compliance rule created successfully",
                    created));
        } catch (Exception e) {
            log.error("❌ Failed to create compliance rule: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // ✏️ UPDATE RULE
    // ============================================================
    @PutMapping("/{ruleId}")
    public ResponseEntity<ResponseWrapper<ComplianceRule>> update(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long ruleId,
            @RequestBody ComplianceRule rule,
            @RequestParam(value = "updatedBy", defaultValue = "SYSTEM") String updatedBy) {
        try {
            ComplianceRule updated = ruleService.update(ruleId, rule, updatedBy);
            log.info("✅ Compliance rule updated: {}", updated.getRuleCode());
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Compliance rule updated successfully",
                    updated));
        } catch (Exception e) {
            log.error("❌ Failed to update compliance rule: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🗑️ DELETE RULE
    // ============================================================
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<ResponseWrapper<Void>> delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long ruleId,
            @RequestParam(value = "deletedBy", defaultValue = "SYSTEM") String deletedBy) {
        try {
            ruleService.delete(ruleId, deletedBy);
            log.info("✅ Compliance rule deleted: {}", ruleId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Compliance rule deleted successfully",
                    null));
        } catch (Exception e) {
            log.error("❌ Failed to delete compliance rule: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔄 INITIALIZE DEFAULT RULES
    // ============================================================
    @PostMapping("/initialize")
    public ResponseEntity<ResponseWrapper<String>> initializeDefaultRules(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "createdBy", defaultValue = "SYSTEM") String createdBy) {
        try {
            ruleService.initializeDefaultRules(createdBy);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Default compliance rules initialized successfully",
                    "OK"));
        } catch (Exception e) {
            log.error("❌ Failed to initialize default rules: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📋 GET RULE TEMPLATES
    // ============================================================
    @GetMapping("/templates")
    public ResponseEntity<ResponseWrapper<List<ComplianceRule>>> getTemplates(
            @RequestHeader HttpHeaders headers) {
        try {
            List<ComplianceRule> templates = templateService.getAvailableTemplates();
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Compliance rule templates retrieved",
                    templates));
        } catch (Exception e) {
            log.error("❌ Failed to get rule templates: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }
}

