package com.example.asset.service;

import com.example.asset.entity.*;
import com.example.asset.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ✅ ComplianceMasterDataService
 * Service for initializing and managing compliance master data.
 */
@Service
public class ComplianceMasterDataService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceMasterDataService.class);

    private final ComplianceRuleTypeMasterRepository ruleTypeRepo;
    private final ComplianceSeverityMasterRepository severityRepo;
    private final ComplianceStatusMasterRepository statusRepo;
    private final ComplianceMasterCacheService cacheService;

    public ComplianceMasterDataService(
            ComplianceRuleTypeMasterRepository ruleTypeRepo,
            ComplianceSeverityMasterRepository severityRepo,
            ComplianceStatusMasterRepository statusRepo,
            ComplianceMasterCacheService cacheService) {
        this.ruleTypeRepo = ruleTypeRepo;
        this.severityRepo = severityRepo;
        this.statusRepo = statusRepo;
        this.cacheService = cacheService;
    }

    // ============================================================
    // 🔄 INITIALIZE MASTER DATA
    // ============================================================
    @Transactional
    public void initializeMasterData(String createdBy) {
        log.info("🔄 Initializing compliance master data...");

        initializeRuleTypes(createdBy);
        initializeSeverities(createdBy);
        initializeStatuses(createdBy);

        // Refresh cache after initialization
        cacheService.refreshCache();

        log.info("✅ Compliance master data initialized");
    }

    // ============================================================
    // 📋 INITIALIZE RULE TYPES
    // ============================================================
    private void initializeRuleTypes(String createdBy) {
        if (ruleTypeRepo.count() > 0) {
            log.info("⏭️ Rule types already exist, skipping initialization");
            return;
        }

        // DATA VALIDATION RULES
        createRuleTypeIfNotExists("REQUIRED_FIELD", "Required Field", 
                "Field must not be empty", "DATA_VALIDATION", 1, createdBy);
        createRuleTypeIfNotExists("UNIQUE_FIELD", "Unique Field", 
                "Field value must be unique", "DATA_VALIDATION", 2, createdBy);
        createRuleTypeIfNotExists("FORMAT_VALIDATION", "Format Validation", 
                "Field must match specified format", "DATA_VALIDATION", 3, createdBy);
        createRuleTypeIfNotExists("RANGE_VALIDATION", "Range Validation", 
                "Field value must be within specified range", "DATA_VALIDATION", 4, createdBy);
        createRuleTypeIfNotExists("LENGTH_VALIDATION", "Length Validation", 
                "Field length must be within specified limits", "DATA_VALIDATION", 5, createdBy);

        // BUSINESS RULES
        createRuleTypeIfNotExists("REFERENCE_INTEGRITY", "Reference Integrity", 
                "Referenced entity must exist", "BUSINESS_RULES", 10, createdBy);
        createRuleTypeIfNotExists("STATUS_TRANSITION", "Status Transition", 
                "Status transition must be valid", "BUSINESS_RULES", 11, createdBy);
        createRuleTypeIfNotExists("DATE_VALIDATION", "Date Validation", 
                "Date must be valid and within allowed range", "BUSINESS_RULES", 12, createdBy);
        createRuleTypeIfNotExists("RELATIONSHIP_VALIDATION", "Relationship Validation", 
                "Entity relationships must be valid", "BUSINESS_RULES", 13, createdBy);

        // COMPLIANCE RULES
        createRuleTypeIfNotExists("WARRANTY_EXPIRY", "Warranty Expiry", 
                "Warranty must not be expired", "COMPLIANCE_RULES", 20, createdBy);
        createRuleTypeIfNotExists("AMC_RENEWAL", "AMC Renewal", 
                "AMC must be renewed before expiry", "COMPLIANCE_RULES", 21, createdBy);
        createRuleTypeIfNotExists("ASSET_ASSIGNMENT", "Asset Assignment", 
                "Asset assignment must comply with policies", "COMPLIANCE_RULES", 22, createdBy);
        createRuleTypeIfNotExists("DOCUMENT_REQUIRED", "Document Required", 
                "Required documents must be attached", "COMPLIANCE_RULES", 23, createdBy);
        createRuleTypeIfNotExists("AUDIT_TRAIL", "Audit Trail", 
                "Audit trail must be maintained", "COMPLIANCE_RULES", 24, createdBy);

        // SECURITY RULES
        createRuleTypeIfNotExists("AUTHORIZATION", "Authorization", 
                "User must be authorized for operation", "SECURITY_RULES", 30, createdBy);
        createRuleTypeIfNotExists("DATA_ACCESS", "Data Access", 
                "User must have access to data", "SECURITY_RULES", 31, createdBy);
        createRuleTypeIfNotExists("OPERATION_PERMISSION", "Operation Permission", 
                "User must have permission for operation", "SECURITY_RULES", 32, createdBy);

        log.info("✅ Rule types initialized");
    }

    // ============================================================
    // 📋 INITIALIZE SEVERITIES
    // ============================================================
    private void initializeSeverities(String createdBy) {
        if (severityRepo.count() > 0) {
            log.info("⏭️ Severities already exist, skipping initialization");
            return;
        }

        createSeverityIfNotExists("CRITICAL", "Critical", 
                "Critical violation - operation must be blocked", 1, true, createdBy);
        createSeverityIfNotExists("HIGH", "High", 
                "High severity - operation should be blocked", 2, true, createdBy);
        createSeverityIfNotExists("MEDIUM", "Medium", 
                "Medium severity - warning issued", 3, false, createdBy);
        createSeverityIfNotExists("LOW", "Low", 
                "Low severity - informational only", 4, false, createdBy);
        createSeverityIfNotExists("INFO", "Info", 
                "Informational - no action required", 5, false, createdBy);

        log.info("✅ Severities initialized");
    }

    // ============================================================
    // 📋 INITIALIZE STATUSES
    // ============================================================
    private void initializeStatuses(String createdBy) {
        if (statusRepo.count() > 0) {
            log.info("⏭️ Statuses already exist, skipping initialization");
            return;
        }

        createStatusIfNotExists("COMPLIANT", "Compliant", 
                "Entity is compliant with all rules", true, createdBy);
        createStatusIfNotExists("NON_COMPLIANT", "Non-Compliant", 
                "Entity has compliance violations", false, createdBy);
        createStatusIfNotExists("PENDING", "Pending", 
                "Compliance check is pending", false, createdBy);
        createStatusIfNotExists("EXEMPTED", "Exempted", 
                "Entity is exempted from compliance check", true, createdBy);
        createStatusIfNotExists("UNDER_REVIEW", "Under Review", 
                "Compliance is under review", false, createdBy);

        log.info("✅ Statuses initialized");
    }

    // ============================================================
    // 🧩 HELPER METHODS
    // ============================================================
    private void createRuleTypeIfNotExists(String code, String name, String description, 
                                           String category, Integer priority, String createdBy) {
        if (!ruleTypeRepo.existsByCodeIgnoreCase(code)) {
            ComplianceRuleTypeMaster ruleType = new ComplianceRuleTypeMaster(code, name, description, category);
            ruleType.setPriority(priority);
            ruleType.setCreatedBy(createdBy);
            ruleType.setUpdatedBy(createdBy);
            ruleType.setActive(true);
            ruleTypeRepo.save(ruleType);
        }
    }

    private void createSeverityIfNotExists(String code, String name, String description, 
                                           Integer level, Boolean blocksOperation, String createdBy) {
        if (!severityRepo.existsByCodeIgnoreCase(code)) {
            ComplianceSeverityMaster severity = new ComplianceSeverityMaster(
                    code, name, description, level, blocksOperation);
            severity.setCreatedBy(createdBy);
            severity.setUpdatedBy(createdBy);
            severity.setActive(true);
            severityRepo.save(severity);
        }
    }

    private void createStatusIfNotExists(String code, String name, String description, 
                                        Boolean isResolved, String createdBy) {
        if (!statusRepo.existsByCodeIgnoreCase(code)) {
            ComplianceStatusMaster status = new ComplianceStatusMaster(code, name, description, isResolved);
            status.setCreatedBy(createdBy);
            status.setUpdatedBy(createdBy);
            status.setActive(true);
            statusRepo.save(status);
        }
    }
}
