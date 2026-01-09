package com.example.authservice.controller;

import com.example.authservice.model.TermsAndConditions;
import com.example.authservice.service.TermsAndConditionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Terms and Conditions API
 * Provides endpoints for apps to retrieve T&C content during registration
 */
@RestController
@RequestMapping("/api/auth/terms-and-conditions")
public class TermsAndConditionsController {

    private static final Logger log = LoggerFactory.getLogger(TermsAndConditionsController.class);

    @Autowired
    private TermsAndConditionsService tcService;

    /**
     * Get active Terms and Conditions for display during registration
     * 
     * @param projectType Project type code (e.g., "ASSET_SERVICE") - optional
     * @param language Language code (e.g., "en", "hi") - optional, default: "en"
     * @return Terms and Conditions content
     */
    @GetMapping
    public ResponseEntity<?> getTermsAndConditions(
            @RequestParam(value = "projectType", required = false) String projectType,
            @RequestParam(value = "language", required = false, defaultValue = "en") String language) {
        
        log.info("📄 [GET] /terms-and-conditions - projectType: {}, language: {}", projectType, language);
        
        try {
            TermsAndConditions tc = tcService.getActiveTermsAndConditions(projectType, language);
            
            if (tc == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Terms and Conditions not found");
                errorResponse.put("projectType", projectType != null ? projectType : "global");
                errorResponse.put("language", language);
                return ResponseEntity.status(404).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("tcId", tc.getTcId());
            response.put("projectType", tc.getProjectType());
            response.put("version", tc.getVersion());
            response.put("title", tc.getTitle());
            response.put("content", tc.getContent());
            response.put("summary", tc.getSummary());
            response.put("language", tc.getLanguage());
            response.put("effectiveDate", tc.getEffectiveDate());
            response.put("lastUpdated", tc.getUpdatedAt());

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to fetch Terms and Conditions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch Terms and Conditions: " + e.getMessage()));
        }
    }

    /**
     * Get Terms and Conditions by specific version
     * 
     * @param projectType Project type code
     * @param version Version number
     * @return Terms and Conditions content
     */
    @GetMapping("/version")
    public ResponseEntity<?> getTermsAndConditionsByVersion(
            @RequestParam("projectType") String projectType,
            @RequestParam("version") String version) {
        
        log.info("📄 [GET] /terms-and-conditions/version - projectType: {}, version: {}", projectType, version);
        
        try {
            TermsAndConditions tc = tcService.getTermsAndConditionsByVersion(projectType, version);
            
            if (tc == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Terms and Conditions not found for version: " + version));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("tcId", tc.getTcId());
            response.put("projectType", tc.getProjectType());
            response.put("version", tc.getVersion());
            response.put("title", tc.getTitle());
            response.put("content", tc.getContent());
            response.put("summary", tc.getSummary());
            response.put("language", tc.getLanguage());
            response.put("isActive", tc.getIsActive());
            response.put("effectiveDate", tc.getEffectiveDate());
            response.put("lastUpdated", tc.getUpdatedAt());

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to fetch Terms and Conditions by version: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch Terms and Conditions: " + e.getMessage()));
        }
    }

    /**
     * Get all versions of Terms and Conditions for a project type
     * (Admin endpoint)
     * 
     * @param projectType Project type code
     * @return List of all T&C versions
     */
    @GetMapping("/versions")
    public ResponseEntity<?> getAllVersions(@RequestParam("projectType") String projectType) {
        
        log.info("📄 [GET] /terms-and-conditions/versions - projectType: {}", projectType);
        
        try {
            List<TermsAndConditions> versions = tcService.getAllVersions(projectType);
            return ResponseEntity.ok(versions);
            
        } catch (Exception e) {
            log.error("❌ Failed to fetch T&C versions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch T&C versions: " + e.getMessage()));
        }
    }

    /**
     * Create or update Terms and Conditions
     * (Admin endpoint - should be secured)
     * 
     * @param tc TermsAndConditions entity
     * @return Created/Updated Terms and Conditions
     */
    @PostMapping
    public ResponseEntity<?> createTermsAndConditions(@RequestBody TermsAndConditions tc) {
        
        log.info("💾 [POST] /terms-and-conditions - projectType: {}, version: {}", 
                tc.getProjectType(), tc.getVersion());
        
        try {
            // Validate required fields
            if (tc.getVersion() == null || tc.getVersion().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Version is required"));
            }
            if (tc.getTitle() == null || tc.getTitle().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Title is required"));
            }
            if (tc.getContent() == null || tc.getContent().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Content is required"));
            }

            // Check if version already exists
            if (tc.getProjectType() != null && 
                tcService.getTermsAndConditionsByVersion(tc.getProjectType(), tc.getVersion()) != null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", 
                                "Terms and Conditions already exists for projectType: " + 
                                tc.getProjectType() + ", version: " + tc.getVersion()));
            }

            TermsAndConditions saved = tcService.saveTermsAndConditions(tc);
            return ResponseEntity.ok(saved);
            
        } catch (Exception e) {
            log.error("❌ Failed to create Terms and Conditions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create Terms and Conditions: " + e.getMessage()));
        }
    }

    /**
     * Activate a Terms and Conditions version
     * (Admin endpoint - should be secured)
     * 
     * @param tcId T&C ID
     * @return Activated Terms and Conditions
     */
    @PutMapping("/{tcId}/activate")
    public ResponseEntity<?> activateTermsAndConditions(@PathVariable Long tcId) {
        
        log.info("✅ [PUT] /terms-and-conditions/{}/activate", tcId);
        
        try {
            TermsAndConditions activated = tcService.activateTermsAndConditions(tcId);
            return ResponseEntity.ok(activated);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Failed to activate Terms and Conditions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to activate Terms and Conditions: " + e.getMessage()));
        }
    }

    /**
     * Deactivate a Terms and Conditions version
     * (Admin endpoint - should be secured)
     * 
     * @param tcId T&C ID
     * @return Deactivated Terms and Conditions
     */
    @PutMapping("/{tcId}/deactivate")
    public ResponseEntity<?> deactivateTermsAndConditions(@PathVariable Long tcId) {
        
        log.info("🔒 [PUT] /terms-and-conditions/{}/deactivate", tcId);
        
        try {
            TermsAndConditions deactivated = tcService.deactivateTermsAndConditions(tcId);
            return ResponseEntity.ok(deactivated);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Failed to deactivate Terms and Conditions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to deactivate Terms and Conditions: " + e.getMessage()));
        }
    }
}

