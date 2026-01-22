package com.example.asset.service;

import com.example.asset.entity.ComplianceRule;
import com.example.asset.service.ComplianceMasterCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ✅ ComplianceRuleTemplateService
 * Provides pre-defined rule templates for common validation scenarios.
 * Helps users quickly create compliance rules without writing JSON expressions manually.
 */
@Service
public class ComplianceRuleTemplateService {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ComplianceRuleTemplateService.class);
    private final ComplianceMasterCacheService cacheService;

    public ComplianceRuleTemplateService(ComplianceMasterCacheService cacheService) {
        this.cacheService = cacheService;
    }

    // ============================================================
    // 📋 GET AVAILABLE TEMPLATES
    // ============================================================
    public List<ComplianceRule> getAvailableTemplates() {
        List<ComplianceRule> templates = new ArrayList<>();
        
        // Required Field Templates
        templates.add(createRequiredFieldTemplate("ASSET", "assetNameUdv", "Asset Name"));
        templates.add(createRequiredFieldTemplate("VENDOR", "vendorName", "Vendor Name"));
        templates.add(createRequiredFieldTemplate("OUTLET", "outletName", "Outlet Name"));
        
        // Unique Field Templates
        templates.add(createUniqueFieldTemplate("ASSET", "assetNameUdv", "Asset Name"));
        templates.add(createUniqueFieldTemplate("VENDOR", "vendorName", "Vendor Name"));
        templates.add(createUniqueFieldTemplate("OUTLET", "outletName", "Outlet Name"));
        
        // Format Validation Templates
        templates.add(createEmailFormatTemplate("VENDOR", "email"));
        templates.add(createPhoneFormatTemplate("VENDOR", "contactNumber"));
        
        // Length Validation Templates
        templates.add(createLengthTemplate("ASSET", "assetNameUdv", 3, 255, "Asset Name"));
        templates.add(createLengthTemplate("VENDOR", "vendorName", 2, 100, "Vendor Name"));
        
        // Date Validation Templates
        templates.add(createDateRangeTemplate("ASSET", "purchaseDate", "Purchase Date"));
        
        return templates;
    }

    // ============================================================
    // 🧩 TEMPLATE CREATION HELPERS
    // ============================================================
    private ComplianceRule createRequiredFieldTemplate(String entityType, String fieldName, String displayName) {
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleCode(entityType + "_" + fieldName.toUpperCase() + "_REQUIRED");
        rule.setRuleName(displayName + " Required");
        rule.setDescription(displayName + " must not be empty");
        rule.setEntityType(entityType);
        rule.setRuleType(cacheService.getRuleTypeByCode("REQUIRED_FIELD"));
        rule.setSeverity(cacheService.getSeverityByCode("CRITICAL"));
        rule.setRuleExpression(String.format("{\"field\": \"%s\"}", fieldName));
        rule.setErrorMessage(displayName + " is required");
        rule.setBlocksOperation(true);
        rule.setPriority(1);
        return rule;
    }

    private ComplianceRule createUniqueFieldTemplate(String entityType, String fieldName, String displayName) {
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleCode(entityType + "_" + fieldName.toUpperCase() + "_UNIQUE");
        rule.setRuleName(displayName + " Unique");
        rule.setDescription(displayName + " must be unique");
        rule.setEntityType(entityType);
        rule.setRuleType(cacheService.getRuleTypeByCode("UNIQUE_FIELD"));
        rule.setSeverity(cacheService.getSeverityByCode("HIGH"));
        rule.setRuleExpression(String.format("{\"field\": \"%s\"}", fieldName));
        rule.setErrorMessage(displayName + " must be unique");
        rule.setBlocksOperation(true);
        rule.setPriority(2);
        return rule;
    }

    private ComplianceRule createEmailFormatTemplate(String entityType, String fieldName) {
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleCode(entityType + "_EMAIL_FORMAT");
        rule.setRuleName("Email Format Validation");
        rule.setDescription("Email must be in valid format");
        rule.setEntityType(entityType);
        rule.setRuleType(cacheService.getRuleTypeByCode("FORMAT_VALIDATION"));
        rule.setSeverity(cacheService.getSeverityByCode("MEDIUM"));
        rule.setRuleExpression(String.format(
            "{\"field\": \"%s\", \"pattern\": \"^[A-Za-z0-9+_.-]+@(.+)$\"}", fieldName));
        rule.setErrorMessage("Email must be in valid format");
        rule.setBlocksOperation(false);
        rule.setPriority(50);
        return rule;
    }

    private ComplianceRule createPhoneFormatTemplate(String entityType, String fieldName) {
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleCode(entityType + "_PHONE_FORMAT");
        rule.setRuleName("Phone Format Validation");
        rule.setDescription("Phone number must be in valid format");
        rule.setEntityType(entityType);
        rule.setRuleType(cacheService.getRuleTypeByCode("FORMAT_VALIDATION"));
        rule.setSeverity(cacheService.getSeverityByCode("MEDIUM"));
        rule.setRuleExpression(String.format(
            "{\"field\": \"%s\", \"pattern\": \"^[+]?[0-9]{10,15}$\"}", fieldName));
        rule.setErrorMessage("Phone number must be in valid format (10-15 digits)");
        rule.setBlocksOperation(false);
        rule.setPriority(50);
        return rule;
    }

    private ComplianceRule createLengthTemplate(String entityType, String fieldName, 
                                                int minLength, int maxLength, String displayName) {
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleCode(entityType + "_" + fieldName.toUpperCase() + "_LENGTH");
        rule.setRuleName(displayName + " Length Validation");
        rule.setDescription(displayName + " length must be between " + minLength + " and " + maxLength);
        rule.setEntityType(entityType);
        rule.setRuleType(cacheService.getRuleTypeByCode("LENGTH_VALIDATION"));
        rule.setSeverity(cacheService.getSeverityByCode("MEDIUM"));
        rule.setRuleExpression(String.format(
            "{\"field\": \"%s\", \"minLength\": %d, \"maxLength\": %d}", 
            fieldName, minLength, maxLength));
        rule.setErrorMessage(displayName + " length must be between " + minLength + " and " + maxLength);
        rule.setBlocksOperation(false);
        rule.setPriority(30);
        return rule;
    }

    private ComplianceRule createDateRangeTemplate(String entityType, String fieldName, String displayName) {
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleCode(entityType + "_" + fieldName.toUpperCase() + "_DATE_RANGE");
        rule.setRuleName(displayName + " Date Range Validation");
        rule.setDescription(displayName + " must be a valid date within allowed range");
        rule.setEntityType(entityType);
        rule.setRuleType(cacheService.getRuleTypeByCode("DATE_VALIDATION"));
        rule.setSeverity(cacheService.getSeverityByCode("MEDIUM"));
        rule.setRuleExpression(String.format(
            "{\"field\": \"%s\", \"format\": \"yyyy-MM-dd\", \"minDate\": \"2020-01-01\", \"maxDate\": \"2030-12-31\"}", 
            fieldName));
        rule.setErrorMessage(displayName + " must be a valid date between 2020-01-01 and 2030-12-31");
        rule.setBlocksOperation(false);
        rule.setPriority(40);
        return rule;
    }
}

