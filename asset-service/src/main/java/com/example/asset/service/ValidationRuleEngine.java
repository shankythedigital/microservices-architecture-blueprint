package com.example.asset.service;

import com.example.asset.entity.*;
import com.example.asset.repository.*;
import com.example.asset.service.ComplianceMasterCacheService;
import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ✅ ValidationRuleEngine
 * Executes compliance rules against entities.
 * Supports various rule types and validation logic.
 */
@Component
public class ValidationRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(ValidationRuleEngine.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AssetMasterRepository assetRepo;
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final AssetWarrantyRepository warrantyRepo;
    private final AssetAmcRepository amcRepo;
    private final VendorRepository vendorRepo;
    private final PurchaseOutletRepository outletRepo;
    private final ComplianceMasterCacheService cacheService;

    public ValidationRuleEngine(
            AssetMasterRepository assetRepo,
            ProductCategoryRepository categoryRepo,
            ProductSubCategoryRepository subCategoryRepo,
            ProductMakeRepository makeRepo,
            ProductModelRepository modelRepo,
            AssetWarrantyRepository warrantyRepo,
            AssetAmcRepository amcRepo,
            VendorRepository vendorRepo,
            PurchaseOutletRepository outletRepo,
            ComplianceMasterCacheService cacheService) {
        this.assetRepo = assetRepo;
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.warrantyRepo = warrantyRepo;
        this.amcRepo = amcRepo;
        this.vendorRepo = vendorRepo;
        this.outletRepo = outletRepo;
        this.cacheService = cacheService;
    }

    // ============================================================
    // 🔍 VALIDATE ENTITY AGAINST RULE
    // ============================================================
    public ValidationResult validate(ComplianceRule rule, String entityType, Long entityId, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setEntityType(entityType);
        result.setEntityId(entityId);
        result.setValid(true);

        try {
            String ruleTypeCode = rule.getRuleType() != null ? rule.getRuleType().getCode() : null;
            if (ruleTypeCode == null) {
                log.warn("⚠️ Rule type is null for rule: {}", rule.getRuleCode());
                result.setValid(true);
                return result;
            }

            switch (ruleTypeCode.toUpperCase()) {
                case "REQUIRED_FIELD":
                    result = validateRequiredField(rule, entity);
                    break;
                case "UNIQUE_FIELD":
                    result = validateUniqueField(rule, entityType, entity);
                    break;
                case "FORMAT_VALIDATION":
                    result = validateFormat(rule, entity);
                    break;
                case "RANGE_VALIDATION":
                    result = validateRange(rule, entity);
                    break;
                case "LENGTH_VALIDATION":
                    result = validateLength(rule, entity);
                    break;
                case "REFERENCE_INTEGRITY":
                    result = validateReferenceIntegrity(rule, entity);
                    break;
                case "STATUS_TRANSITION":
                    result = validateStatusTransition(rule, entity);
                    break;
                case "DATE_VALIDATION":
                    result = validateDate(rule, entity);
                    break;
                case "WARRANTY_EXPIRY":
                    result = validateWarrantyExpiry(rule, entity);
                    break;
                case "AMC_RENEWAL":
                    result = validateAmcRenewal(rule, entity);
                    break;
                case "DOCUMENT_REQUIRED":
                    result = validateDocumentRequired(rule, entity);
                    break;
                default:
                    log.warn("⚠️ Unsupported rule type: {}", ruleTypeCode);
                    result.setValid(true); // Unknown rules don't fail by default
            }
        } catch (Exception e) {
            log.error("❌ Error validating rule {}: {}", rule.getRuleCode(), e.getMessage(), e);
            result.setValid(false);
            result.setErrorMessage("Validation error: " + e.getMessage());
        }

        return result;
    }

    // ============================================================
    // 🔍 VALIDATION METHODS
    // ============================================================
    private ValidationResult validateRequiredField(ComplianceRule rule, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setValid(true);

        try {
            Map<String, Object> expression = parseExpression(rule.getRuleExpression());
            String fieldName = (String) expression.get("field");
            
            Object fieldValue = getFieldValue(entity, fieldName);
            boolean isEmpty = fieldValue == null || 
                             (fieldValue instanceof String && ((String) fieldValue).trim().isEmpty());

            if (isEmpty) {
                result.setValid(false);
                result.setViolatedField(fieldName);
                result.setErrorMessage(rule.getErrorMessage() != null ? 
                    rule.getErrorMessage() : 
                    "Field '" + fieldName + "' is required");
            }
        } catch (Exception e) {
            result.setValid(false);
            result.setErrorMessage("Error validating required field: " + e.getMessage());
        }

        return result;
    }

    private ValidationResult validateUniqueField(ComplianceRule rule, String entityType, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setValid(true);

        try {
            Map<String, Object> expression = parseExpression(rule.getRuleExpression());
            String fieldName = (String) expression.get("field");
            Object fieldValue = getFieldValue(entity, fieldName);

            if (fieldValue == null) {
                return result; // Null values are handled by REQUIRED_FIELD
            }

            boolean exists = checkUniqueness(entityType, fieldName, fieldValue, entity);
            if (exists) {
                result.setValid(false);
                result.setViolatedField(fieldName);
                result.setActualValue(String.valueOf(fieldValue));
                result.setErrorMessage(rule.getErrorMessage() != null ? 
                    rule.getErrorMessage() : 
                    "Field '" + fieldName + "' must be unique");
            }
        } catch (Exception e) {
            result.setValid(false);
            result.setErrorMessage("Error validating unique field: " + e.getMessage());
        }

        return result;
    }

    private ValidationResult validateFormat(ComplianceRule rule, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setValid(true);

        try {
            Map<String, Object> expression = parseExpression(rule.getRuleExpression());
            String fieldName = (String) expression.get("field");
            String pattern = (String) expression.get("pattern");
            
            Object fieldValue = getFieldValue(entity, fieldName);
            if (fieldValue == null) {
                return result; // Null values are handled by REQUIRED_FIELD
            }

            String value = String.valueOf(fieldValue);
            if (!Pattern.matches(pattern, value)) {
                result.setValid(false);
                result.setViolatedField(fieldName);
                result.setActualValue(value);
                result.setExpectedValue("Pattern: " + pattern);
                result.setErrorMessage(rule.getErrorMessage() != null ? 
                    rule.getErrorMessage() : 
                    "Field '" + fieldName + "' does not match required format");
            }
        } catch (Exception e) {
            result.setValid(false);
            result.setErrorMessage("Error validating format: " + e.getMessage());
        }

        return result;
    }

    private ValidationResult validateRange(ComplianceRule rule, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setValid(true);

        try {
            Map<String, Object> expression = parseExpression(rule.getRuleExpression());
            String fieldName = (String) expression.get("field");
            Number min = expression.get("min") != null ? 
                Double.valueOf(expression.get("min").toString()) : null;
            Number max = expression.get("max") != null ? 
                Double.valueOf(expression.get("max").toString()) : null;
            
            Object fieldValue = getFieldValue(entity, fieldName);
            if (fieldValue == null) {
                return result;
            }

            double value = Double.parseDouble(String.valueOf(fieldValue));
            if ((min != null && value < min.doubleValue()) || 
                (max != null && value > max.doubleValue())) {
                result.setValid(false);
                result.setViolatedField(fieldName);
                result.setActualValue(String.valueOf(value));
                result.setExpectedValue("Range: " + min + " to " + max);
                result.setErrorMessage(rule.getErrorMessage());
            }
        } catch (Exception e) {
            result.setValid(false);
            result.setErrorMessage("Error validating range: " + e.getMessage());
        }

        return result;
    }

    private ValidationResult validateLength(ComplianceRule rule, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setValid(true);

        try {
            Map<String, Object> expression = parseExpression(rule.getRuleExpression());
            String fieldName = (String) expression.get("field");
            Integer minLength = expression.get("minLength") != null ? 
                Integer.valueOf(expression.get("minLength").toString()) : null;
            Integer maxLength = expression.get("maxLength") != null ? 
                Integer.valueOf(expression.get("maxLength").toString()) : null;
            
            Object fieldValue = getFieldValue(entity, fieldName);
            if (fieldValue == null) {
                return result;
            }

            String value = String.valueOf(fieldValue);
            int length = value.length();
            if ((minLength != null && length < minLength) || 
                (maxLength != null && length > maxLength)) {
                result.setValid(false);
                result.setViolatedField(fieldName);
                result.setActualValue("Length: " + length);
                result.setExpectedValue("Length range: " + minLength + " to " + maxLength);
                result.setErrorMessage(rule.getErrorMessage());
            }
        } catch (Exception e) {
            result.setValid(false);
            result.setErrorMessage("Error validating length: " + e.getMessage());
        }

        return result;
    }

    private ValidationResult validateReferenceIntegrity(ComplianceRule rule, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setValid(true);

        try {
            Map<String, Object> expression = parseExpression(rule.getRuleExpression());
            String fieldName = (String) expression.get("field");
            String referencedEntityType = (String) expression.get("referencedEntityType");
            
            Object fieldValue = getFieldValue(entity, fieldName);
            if (fieldValue == null) {
                return result; // Optional references
            }

            Long refId = null;
            if (fieldValue instanceof Long) {
                refId = (Long) fieldValue;
            } else if (fieldValue instanceof BaseEntity) {
                BaseEntity refEntity = (BaseEntity) fieldValue;
                refId = getEntityId(refEntity);
            }

            if (refId != null && !entityExists(referencedEntityType, refId)) {
                result.setValid(false);
                result.setViolatedField(fieldName);
                result.setActualValue(String.valueOf(refId));
                result.setErrorMessage("Referenced " + referencedEntityType + " with ID " + refId + " does not exist");
            }
        } catch (Exception e) {
            result.setValid(false);
            result.setErrorMessage("Error validating reference: " + e.getMessage());
        }

        return result;
    }

    private ValidationResult validateStatusTransition(ComplianceRule rule, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setValid(true);
        // Implementation for status transition validation
        return result;
    }

    private ValidationResult validateDate(ComplianceRule rule, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setValid(true);

        try {
            Map<String, Object> expression = parseExpression(rule.getRuleExpression());
            String fieldName = (String) expression.get("field");
            String format = (String) expression.getOrDefault("format", "yyyy-MM-dd");
            
            Object fieldValue = getFieldValue(entity, fieldName);
            if (fieldValue == null) {
                return result;
            }

            LocalDate date = null;
            if (fieldValue instanceof LocalDate) {
                date = (LocalDate) fieldValue;
            } else if (fieldValue instanceof String) {
                try {
                    date = LocalDate.parse((String) fieldValue, DateTimeFormatter.ofPattern(format));
                } catch (DateTimeParseException e) {
                    result.setValid(false);
                    result.setViolatedField(fieldName);
                    result.setErrorMessage("Invalid date format. Expected: " + format);
                    return result;
                }
            }

            if (date != null) {
                LocalDate minDate = expression.get("minDate") != null ? 
                    LocalDate.parse(expression.get("minDate").toString()) : null;
                LocalDate maxDate = expression.get("maxDate") != null ? 
                    LocalDate.parse(expression.get("maxDate").toString()) : null;

                if ((minDate != null && date.isBefore(minDate)) || 
                    (maxDate != null && date.isAfter(maxDate))) {
                    result.setValid(false);
                    result.setViolatedField(fieldName);
                    result.setActualValue(date.toString());
                    result.setErrorMessage(rule.getErrorMessage());
                }
            }
        } catch (Exception e) {
            result.setValid(false);
            result.setErrorMessage("Error validating date: " + e.getMessage());
        }

        return result;
    }

    private ValidationResult validateWarrantyExpiry(ComplianceRule rule, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setValid(true);

        if (entity instanceof AssetWarranty) {
            AssetWarranty warranty = (AssetWarranty) entity;
            if (warranty.getWarrantyEndDate() != null && 
                warranty.getWarrantyEndDate().isBefore(LocalDate.now())) {
                result.setValid(false);
                result.setErrorMessage("Warranty has expired");
            }
        }
        return result;
    }

    private ValidationResult validateAmcRenewal(ComplianceRule rule, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setValid(true);

        if (entity instanceof AssetAmc) {
            AssetAmc amc = (AssetAmc) entity;
            if (amc.getEndDate() != null && amc.getEndDate().isBefore(LocalDate.now())) {
                result.setValid(false);
                result.setErrorMessage("AMC has expired and needs renewal");
            }
        }
        return result;
    }

    private ValidationResult validateDocumentRequired(ComplianceRule rule, Object entity) {
        ValidationResult result = new ValidationResult();
        result.setRule(rule);
        result.setValid(true);
        // Implementation for document requirement validation
        return result;
    }

    // ============================================================
    // 🧩 HELPER METHODS
    // ============================================================
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseExpression(String expression) throws JsonProcessingException {
        if (expression == null || expression.trim().isEmpty()) {
            return new HashMap<>();
        }
        return objectMapper.readValue(expression, Map.class);
    }

    private Object getFieldValue(Object entity, String fieldName) {
        try {
            String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            return entity.getClass().getMethod(methodName).invoke(entity);
        } catch (Exception e) {
            log.warn("⚠️ Could not get field '{}' from entity: {}", fieldName, e.getMessage());
            return null;
        }
    }

    private Long getEntityId(BaseEntity entity) {
        try {
            if (entity instanceof AssetMaster) return ((AssetMaster) entity).getAssetId();
            if (entity instanceof ProductCategory) return ((ProductCategory) entity).getCategoryId();
            if (entity instanceof ProductSubCategory) return ((ProductSubCategory) entity).getSubCategoryId();
            if (entity instanceof ProductMake) return ((ProductMake) entity).getMakeId();
            if (entity instanceof ProductModel) return ((ProductModel) entity).getModelId();
            if (entity instanceof AssetWarranty) return ((AssetWarranty) entity).getWarrantyId();
            if (entity instanceof AssetAmc) return ((AssetAmc) entity).getAmcId();
            if (entity instanceof VendorMaster) return ((VendorMaster) entity).getVendorId();
            if (entity instanceof PurchaseOutlet) return ((PurchaseOutlet) entity).getOutletId();
        } catch (Exception e) {
            log.warn("⚠️ Could not get ID from entity: {}", e.getMessage());
        }
        return null;
    }

    private boolean entityExists(String entityType, Long id) {
        if (id == null) return false;
        switch (entityType.toUpperCase()) {
            case "ASSET": return assetRepo.existsById(id);
            case "CATEGORY": return categoryRepo.existsById(id);
            case "SUBCATEGORY": return subCategoryRepo.existsById(id);
            case "MAKE": return makeRepo.existsById(id);
            case "MODEL": return modelRepo.existsById(id);
            case "WARRANTY": return warrantyRepo.existsByWarrantyId(id);
            case "AMC": return amcRepo.existsByAmcId(id);
            case "VENDOR": return vendorRepo.existsById(id);
            case "OUTLET": return outletRepo.existsById(id);
            default: return false;
        }
    }

    private boolean checkUniqueness(String entityType, String fieldName, Object fieldValue, Object entity) {
        switch (entityType.toUpperCase()) {
            case "ASSET":
                if ("assetNameUdv".equals(fieldName)) {
                    return assetRepo.existsByAssetNameUdv(String.valueOf(fieldValue));
                }
                break;
            case "VENDOR":
                if ("vendorName".equals(fieldName)) {
                    return vendorRepo.existsByVendorNameIgnoreCase(String.valueOf(fieldValue));
                }
                break;
            case "OUTLET":
                if ("outletName".equals(fieldName)) {
                    return outletRepo.existsByOutletName(String.valueOf(fieldValue));
                }
                break;
            case "CATEGORY":
                if ("categoryName".equals(fieldName)) {
                    return categoryRepo.existsByCategoryName(String.valueOf(fieldValue));
                }
                break;
            case "SUBCATEGORY":
                if ("subCategoryName".equals(fieldName)) {
                    return subCategoryRepo.existsBySubCategoryName(String.valueOf(fieldValue));
                }
                break;
        }
        return false;
    }

    // ============================================================
    // 📦 ValidationResult Inner Class
    // ============================================================
    public static class ValidationResult {
        private ComplianceRule rule;
        private String entityType;
        private Long entityId;
        private boolean valid;
        private String errorMessage;
        private String violatedField;
        private String expectedValue;
        private String actualValue;

        // Getters and Setters
        public ComplianceRule getRule() { return rule; }
        public void setRule(ComplianceRule rule) { this.rule = rule; }
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getViolatedField() { return violatedField; }
        public void setViolatedField(String violatedField) { this.violatedField = violatedField; }
        public String getExpectedValue() { return expectedValue; }
        public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }
        public String getActualValue() { return actualValue; }
        public void setActualValue(String actualValue) { this.actualValue = actualValue; }
    }
}

