package com.example.asset.service;

import com.example.asset.dto.ComplianceCheckResult;
import com.example.asset.entity.*;
import com.example.asset.repository.*;
import com.example.asset.service.ValidationRuleEngine.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ ComplianceAgentService
 * Main service for validation and compliance checking.
 * Validates entities against compliance rules and tracks violations.
 */
@Service
public class ComplianceAgentService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceAgentService.class);

    private final ComplianceRuleRepository ruleRepo;
    private final ComplianceViolationRepository violationRepo;
    private final ValidationRuleEngine ruleEngine;
    private final AssetMasterRepository assetRepo;
    private final AssetWarrantyRepository warrantyRepo;
    private final AssetAmcRepository amcRepo;
    private final VendorRepository vendorRepo;
    private final PurchaseOutletRepository outletRepo;
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final AssetComponentRepository componentRepo;
    private final ComplianceMasterCacheService cacheService;

    public ComplianceAgentService(
            ComplianceRuleRepository ruleRepo,
            ComplianceViolationRepository violationRepo,
            ValidationRuleEngine ruleEngine,
            AssetMasterRepository assetRepo,
            AssetWarrantyRepository warrantyRepo,
            AssetAmcRepository amcRepo,
            VendorRepository vendorRepo,
            PurchaseOutletRepository outletRepo,
            ProductCategoryRepository categoryRepo,
            ProductSubCategoryRepository subCategoryRepo,
            ProductMakeRepository makeRepo,
            ProductModelRepository modelRepo,
            AssetComponentRepository componentRepo,
            ComplianceMasterCacheService cacheService) {
        this.ruleRepo = ruleRepo;
        this.violationRepo = violationRepo;
        this.ruleEngine = ruleEngine;
        this.assetRepo = assetRepo;
        this.warrantyRepo = warrantyRepo;
        this.amcRepo = amcRepo;
        this.vendorRepo = vendorRepo;
        this.outletRepo = outletRepo;
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.componentRepo = componentRepo;
        this.cacheService = cacheService;
    }

    // ============================================================
    // 🔍 VALIDATE ENTITY
    // ============================================================
    @Transactional
    public ComplianceCheckResult validateEntity(String entityType, Long entityId, Object entity) {
        ComplianceCheckResult result = new ComplianceCheckResult();
        result.setEntityType(entityType);
        result.setEntityId(entityId);
        result.setCheckedAt(LocalDateTime.now());
        result.setViolations(new ArrayList<>());

        try {
            // Get all active rules for this entity type
            List<ComplianceRule> rules = ruleRepo.findAllByEntityTypeIgnoreCaseAndActiveTrueOrderByPriorityAsc(entityType);
            
            if (rules.isEmpty()) {
                result.setCompliant(true);
                result.setMessage("No compliance rules defined for entity type: " + entityType);
                return result;
            }

            List<ComplianceViolation> violations = new ArrayList<>();
            boolean hasBlockingViolations = false;

            // Validate against each rule
            for (ComplianceRule rule : rules) {
                ValidationResult validationResult = ruleEngine.validate(rule, entityType, entityId, entity);
                
                if (!validationResult.isValid()) {
                    ComplianceViolation violation = createViolation(rule, entityType, entityId, validationResult);
                    violations.add(violation);
                    
                    if (rule.getBlocksOperation()) {
                        hasBlockingViolations = true;
                    }
                }
            }

            // Save violations
            if (!violations.isEmpty()) {
                violationRepo.saveAll(violations);
                result.setViolations(violations);
            }

            result.setCompliant(violations.isEmpty());
            result.setHasBlockingViolations(hasBlockingViolations);
            result.setMessage(violations.isEmpty() ? 
                "Entity is compliant with all rules" : 
                "Found " + violations.size() + " compliance violation(s)");

        } catch (Exception e) {
            log.error("❌ Error validating entity {} {}: {}", entityType, entityId, e.getMessage(), e);
            result.setCompliant(false);
            result.setMessage("Validation error: " + e.getMessage());
        }

        return result;
    }

    // ============================================================
    // 🔍 VALIDATE ENTITY BY ID
    // ============================================================
    @Transactional
    public ComplianceCheckResult validateEntityById(String entityType, Long entityId) {
        Object entity = fetchEntity(entityType, entityId);
        if (entity == null) {
            ComplianceCheckResult result = new ComplianceCheckResult();
            result.setEntityType(entityType);
            result.setEntityId(entityId);
            result.setCompliant(false);
            result.setMessage("Entity not found: " + entityType + " with ID " + entityId);
            return result;
        }
        return validateEntity(entityType, entityId, entity);
    }

    // ============================================================
    // 📋 GET COMPLIANCE STATUS
    // ============================================================
    public ComplianceStatusMaster getComplianceStatus(String entityType, Long entityId) {
        boolean hasUnresolvedViolations = violationRepo.existsByEntityTypeIgnoreCaseAndEntityIdAndStatus_IsResolvedFalseAndActiveTrue(
                entityType, entityId);
        
        if (!hasUnresolvedViolations) {
            return cacheService.getCompliantStatus();
        }

        // Check for critical violations
        ComplianceSeverityMaster criticalSeverity = cacheService.getSeverityByCode("CRITICAL");
        ComplianceSeverityMaster highSeverity = cacheService.getSeverityByCode("HIGH");
        
        List<ComplianceViolation> criticalViolations = violationRepo.findAllByEntityTypeIgnoreCaseAndEntityIdAndActiveTrue(
                entityType, entityId).stream()
                .filter(v -> v.getSeverity() != null && 
                            (v.getSeverity().equals(criticalSeverity) || v.getSeverity().equals(highSeverity)))
                .filter(v -> v.getStatus() != null && 
                            (v.getStatus().getIsResolved() == null || !v.getStatus().getIsResolved()))
                .collect(Collectors.toList());

        return criticalViolations.isEmpty() ? 
                cacheService.getNonCompliantStatus() : 
                cacheService.getNonCompliantStatus();
    }

    // ============================================================
    // 📋 GET VIOLATIONS
    // ============================================================
    public List<ComplianceViolation> getViolations(String entityType, Long entityId, boolean unresolvedOnly) {
        if (unresolvedOnly) {
            return violationRepo.findAllByEntityTypeIgnoreCaseAndEntityIdAndStatus_IsResolvedFalseAndActiveTrue(
                    entityType, entityId);
        }
        return violationRepo.findAllByEntityTypeIgnoreCaseAndEntityIdAndActiveTrue(entityType, entityId);
    }

    // ============================================================
    // ✅ RESOLVE VIOLATION
    // ============================================================
    @Transactional
    public ComplianceViolation resolveViolation(Long violationId, String resolvedBy, String notes) {
        return violationRepo.findById(violationId)
                .map(violation -> {
                    ComplianceStatusMaster compliantStatus = cacheService.getCompliantStatus();
                    violation.resolve(resolvedBy, notes, compliantStatus);
                    violation.setUpdatedBy(resolvedBy);
                    return violationRepo.save(violation);
                })
                .orElseThrow(() -> new RuntimeException("Violation not found: " + violationId));
    }

    // ============================================================
    // 📊 COMPLIANCE REPORT
    // ============================================================
    public Map<String, Object> generateComplianceReport(String entityType, Long entityId) {
        Map<String, Object> report = new HashMap<>();
        
        ComplianceStatusMaster status = getComplianceStatus(entityType, entityId);
        List<ComplianceViolation> violations = getViolations(entityType, entityId, true);
        
        ComplianceSeverityMaster critical = cacheService.getSeverityByCode("CRITICAL");
        ComplianceSeverityMaster high = cacheService.getSeverityByCode("HIGH");
        ComplianceSeverityMaster medium = cacheService.getSeverityByCode("MEDIUM");
        ComplianceSeverityMaster low = cacheService.getSeverityByCode("LOW");
        
        report.put("entityType", entityType);
        report.put("entityId", entityId);
        report.put("status", status != null ? status.getCode() : "UNKNOWN");
        report.put("totalViolations", violations.size());
        report.put("criticalViolations", violations.stream()
                .filter(v -> v.getSeverity() != null && v.getSeverity().equals(critical)).count());
        report.put("highViolations", violations.stream()
                .filter(v -> v.getSeverity() != null && v.getSeverity().equals(high)).count());
        report.put("mediumViolations", violations.stream()
                .filter(v -> v.getSeverity() != null && v.getSeverity().equals(medium)).count());
        report.put("lowViolations", violations.stream()
                .filter(v -> v.getSeverity() != null && v.getSeverity().equals(low)).count());
        report.put("violations", violations);
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    // ============================================================
    // 🔄 BULK VALIDATION
    // ============================================================
    @Transactional
    public List<ComplianceCheckResult> validateBulk(String entityType, List<Long> entityIds) {
        List<ComplianceCheckResult> results = new ArrayList<>();
        
        for (Long entityId : entityIds) {
            try {
                ComplianceCheckResult result = validateEntityById(entityType, entityId);
                results.add(result);
            } catch (Exception e) {
                log.error("❌ Error validating {} {}: {}", entityType, entityId, e.getMessage());
                ComplianceCheckResult errorResult = new ComplianceCheckResult();
                errorResult.setEntityType(entityType);
                errorResult.setEntityId(entityId);
                errorResult.setCompliant(false);
                errorResult.setMessage("Validation error: " + e.getMessage());
                results.add(errorResult);
            }
        }
        
        return results;
    }

    // ============================================================
    // 🧩 HELPER METHODS
    // ============================================================
    private ComplianceViolation createViolation(ComplianceRule rule, String entityType, 
                                                Long entityId, ValidationResult validationResult) {
        ComplianceViolation violation = new ComplianceViolation();
        violation.setRule(rule);
        violation.setEntityType(entityType);
        violation.setEntityId(entityId);
        violation.setSeverity(rule.getSeverity());
        violation.setStatus(cacheService.getNonCompliantStatus());
        violation.setViolationMessage(validationResult.getErrorMessage());
        violation.setViolatedField(validationResult.getViolatedField());
        violation.setExpectedValue(validationResult.getExpectedValue());
        violation.setActualValue(validationResult.getActualValue());
        violation.setDetectedAt(LocalDateTime.now());
        violation.setActive(true);
        violation.setCreatedBy("SYSTEM");
        return violation;
    }

    private Object fetchEntity(String entityType, Long entityId) {
        if (entityId == null) return null;
        
        switch (entityType.toUpperCase()) {
            case "ASSET":
                return assetRepo.findById(entityId).orElse(null);
            case "WARRANTY":
                return warrantyRepo.findById(entityId).orElse(null);
            case "AMC":
                return amcRepo.findById(entityId).orElse(null);
            case "VENDOR":
                return vendorRepo.findById(entityId).orElse(null);
            case "OUTLET":
                return outletRepo.findById(entityId).orElse(null);
            case "CATEGORY":
                return categoryRepo.findById(entityId).orElse(null);
            case "SUBCATEGORY":
                return subCategoryRepo.findById(entityId).orElse(null);
            case "MAKE":
                return makeRepo.findById(entityId).orElse(null);
            case "MODEL":
                return modelRepo.findById(entityId).orElse(null);
            case "COMPONENT":
                return componentRepo.findById(entityId).orElse(null);
            default:
                log.warn("⚠️ Unknown entity type: {}", entityType);
                return null;
        }
    }
}

