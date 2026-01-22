package com.example.asset.service;

import com.example.asset.entity.ComplianceRule;
import com.example.asset.repository.ComplianceRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ✅ ComplianceRuleService
 * Service for managing compliance rules (CRUD operations).
 */
@Service
public class ComplianceRuleService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceRuleService.class);
    private final ComplianceRuleRepository ruleRepo;
    private final ComplianceMasterCacheService cacheService;

    public ComplianceRuleService(ComplianceRuleRepository ruleRepo,
                                ComplianceMasterCacheService cacheService) {
        this.ruleRepo = ruleRepo;
        this.cacheService = cacheService;
    }

    // ============================================================
    // 📋 LIST ALL RULES
    // ============================================================
    public List<ComplianceRule> listAll() {
        return ruleRepo.findAllByActiveTrue();
    }

    // ============================================================
    // 📋 LIST RULES BY ENTITY TYPE
    // ============================================================
    public List<ComplianceRule> listByEntityType(String entityType) {
        return ruleRepo.findAllByEntityTypeIgnoreCaseAndActiveTrue(entityType);
    }

    // ============================================================
    // 🔍 FIND RULE BY ID
    // ============================================================
    public Optional<ComplianceRule> findById(Long ruleId) {
        return ruleRepo.findById(ruleId);
    }

    // ============================================================
    // 🔍 FIND RULE BY CODE
    // ============================================================
    public Optional<ComplianceRule> findByCode(String ruleCode, String entityType) {
        return ruleRepo.findByRuleCodeIgnoreCaseAndEntityTypeIgnoreCase(ruleCode, entityType);
    }

    // ============================================================
    // ➕ CREATE RULE
    // ============================================================
    @Transactional
    public ComplianceRule create(ComplianceRule rule, String createdBy) {
        // Check if rule code already exists
        if (ruleRepo.findByRuleCodeIgnoreCaseAndEntityTypeIgnoreCase(
                rule.getRuleCode(), rule.getEntityType()).isPresent()) {
            throw new IllegalArgumentException("Rule with code '" + rule.getRuleCode() + 
                    "' already exists for entity type '" + rule.getEntityType() + "'");
        }

        rule.setCreatedBy(createdBy);
        rule.setUpdatedBy(createdBy);
        rule.setActive(true);
        
        ComplianceRule saved = ruleRepo.save(rule);
        log.info("✅ Compliance rule created: {} ({})", saved.getRuleCode(), saved.getRuleName());
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE RULE
    // ============================================================
    @Transactional
    public ComplianceRule update(Long ruleId, ComplianceRule updatedRule, String updatedBy) {
        return ruleRepo.findById(ruleId)
                .map(rule -> {
                    // Check if rule code is being changed and conflicts
                    if (!rule.getRuleCode().equalsIgnoreCase(updatedRule.getRuleCode()) ||
                        !rule.getEntityType().equalsIgnoreCase(updatedRule.getEntityType())) {
                        if (ruleRepo.findByRuleCodeIgnoreCaseAndEntityTypeIgnoreCase(
                                updatedRule.getRuleCode(), updatedRule.getEntityType()).isPresent()) {
                            throw new IllegalArgumentException("Rule with code '" + updatedRule.getRuleCode() + 
                                    "' already exists for entity type '" + updatedRule.getEntityType() + "'");
                        }
                    }

                    rule.setRuleCode(updatedRule.getRuleCode());
                    rule.setRuleName(updatedRule.getRuleName());
                    rule.setDescription(updatedRule.getDescription());
                    rule.setEntityType(updatedRule.getEntityType());
                    rule.setRuleType(updatedRule.getRuleType());
                    rule.setSeverity(updatedRule.getSeverity());
                    rule.setRuleExpression(updatedRule.getRuleExpression());
                    rule.setErrorMessage(updatedRule.getErrorMessage());
                    rule.setBlocksOperation(updatedRule.getBlocksOperation());
                    rule.setPriority(updatedRule.getPriority());
                    rule.setUpdatedBy(updatedBy);

                    ComplianceRule saved = ruleRepo.save(rule);
                    log.info("✅ Compliance rule updated: {} ({})", saved.getRuleCode(), saved.getRuleName());
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("Compliance rule not found: " + ruleId));
    }

    // ============================================================
    // 🗑️ DELETE RULE (Soft Delete)
    // ============================================================
    @Transactional
    public void delete(Long ruleId, String deletedBy) {
        ruleRepo.findById(ruleId)
                .ifPresentOrElse(
                        rule -> {
                            rule.setActive(false);
                            rule.setUpdatedBy(deletedBy);
                            ruleRepo.save(rule);
                            log.info("✅ Compliance rule deleted: {} ({})", rule.getRuleCode(), rule.getRuleName());
                        },
                        () -> {
                            throw new RuntimeException("Compliance rule not found: " + ruleId);
                        });
    }

    // ============================================================
    // 🧩 HELPER: CREATE DEFAULT RULES
    // ============================================================
    @Transactional
    public void initializeDefaultRules(String createdBy) {
        // Asset Name Required Rule
        if (ruleRepo.findByRuleCodeIgnoreCaseAndEntityTypeIgnoreCase("ASSET_NAME_REQUIRED", "ASSET").isEmpty()) {
            ComplianceRule rule = new ComplianceRule();
            rule.setRuleCode("ASSET_NAME_REQUIRED");
            rule.setRuleName("Asset Name Required");
            rule.setDescription("Asset name must not be empty");
            rule.setEntityType("ASSET");
            rule.setRuleType(cacheService.getRuleTypeByCode("REQUIRED_FIELD"));
            rule.setSeverity(cacheService.getSeverityByCode("CRITICAL"));
            rule.setRuleExpression("{\"field\": \"assetNameUdv\"}");
            rule.setErrorMessage("Asset name is required");
            rule.setBlocksOperation(true);
            rule.setPriority(1);
            create(rule, createdBy);
        }

        // Asset Name Unique Rule
        if (ruleRepo.findByRuleCodeIgnoreCaseAndEntityTypeIgnoreCase("ASSET_NAME_UNIQUE", "ASSET").isEmpty()) {
            ComplianceRule rule = new ComplianceRule();
            rule.setRuleCode("ASSET_NAME_UNIQUE");
            rule.setRuleName("Asset Name Unique");
            rule.setDescription("Asset name must be unique");
            rule.setEntityType("ASSET");
            rule.setRuleType(cacheService.getRuleTypeByCode("UNIQUE_FIELD"));
            rule.setSeverity(cacheService.getSeverityByCode("HIGH"));
            rule.setRuleExpression("{\"field\": \"assetNameUdv\"}");
            rule.setErrorMessage("Asset name must be unique");
            rule.setBlocksOperation(true);
            rule.setPriority(2);
            create(rule, createdBy);
        }

        // Warranty Expiry Check
        if (ruleRepo.findByRuleCodeIgnoreCaseAndEntityTypeIgnoreCase("WARRANTY_EXPIRY_CHECK", "WARRANTY").isEmpty()) {
            ComplianceRule rule = new ComplianceRule();
            rule.setRuleCode("WARRANTY_EXPIRY_CHECK");
            rule.setRuleName("Warranty Expiry Check");
            rule.setDescription("Warranty must not be expired");
            rule.setEntityType("WARRANTY");
            rule.setRuleType(cacheService.getRuleTypeByCode("WARRANTY_EXPIRY"));
            rule.setSeverity(cacheService.getSeverityByCode("MEDIUM"));
            rule.setRuleExpression("{}");
            rule.setErrorMessage("Warranty has expired");
            rule.setBlocksOperation(false);
            rule.setPriority(10);
            create(rule, createdBy);
        }

        // AMC Renewal Check
        if (ruleRepo.findByRuleCodeIgnoreCaseAndEntityTypeIgnoreCase("AMC_RENEWAL_CHECK", "AMC").isEmpty()) {
            ComplianceRule rule = new ComplianceRule();
            rule.setRuleCode("AMC_RENEWAL_CHECK");
            rule.setRuleName("AMC Renewal Check");
            rule.setDescription("AMC must be renewed before expiry");
            rule.setEntityType("AMC");
            rule.setRuleType(cacheService.getRuleTypeByCode("AMC_RENEWAL"));
            rule.setSeverity(cacheService.getSeverityByCode("MEDIUM"));
            rule.setRuleExpression("{}");
            rule.setErrorMessage("AMC has expired and needs renewal");
            rule.setBlocksOperation(false);
            rule.setPriority(10);
            create(rule, createdBy);
        }

        log.info("✅ Default compliance rules initialized");
    }
}

