package com.example.asset.enums;

/**
 * ✅ ComplianceRuleType Enum
 * Defines types of compliance rules that can be enforced.
 */
public enum ComplianceRuleType {
    
    // ============================================================
    // DATA VALIDATION RULES
    // ============================================================
    REQUIRED_FIELD("REQUIRED_FIELD", "Field must not be empty"),
    UNIQUE_FIELD("UNIQUE_FIELD", "Field value must be unique"),
    FORMAT_VALIDATION("FORMAT_VALIDATION", "Field must match specified format"),
    RANGE_VALIDATION("RANGE_VALIDATION", "Field value must be within specified range"),
    LENGTH_VALIDATION("LENGTH_VALIDATION", "Field length must be within specified limits"),
    
    // ============================================================
    // BUSINESS RULES
    // ============================================================
    REFERENCE_INTEGRITY("REFERENCE_INTEGRITY", "Referenced entity must exist"),
    STATUS_TRANSITION("STATUS_TRANSITION", "Status transition must be valid"),
    DATE_VALIDATION("DATE_VALIDATION", "Date must be valid and within allowed range"),
    RELATIONSHIP_VALIDATION("RELATIONSHIP_VALIDATION", "Entity relationships must be valid"),
    
    // ============================================================
    // COMPLIANCE RULES
    // ============================================================
    WARRANTY_EXPIRY("WARRANTY_EXPIRY", "Warranty must not be expired"),
    AMC_RENEWAL("AMC_RENEWAL", "AMC must be renewed before expiry"),
    ASSET_ASSIGNMENT("ASSET_ASSIGNMENT", "Asset assignment must comply with policies"),
    DOCUMENT_REQUIRED("DOCUMENT_REQUIRED", "Required documents must be attached"),
    AUDIT_TRAIL("AUDIT_TRAIL", "Audit trail must be maintained"),
    
    // ============================================================
    // SECURITY RULES
    // ============================================================
    AUTHORIZATION("AUTHORIZATION", "User must be authorized for operation"),
    DATA_ACCESS("DATA_ACCESS", "User must have access to data"),
    OPERATION_PERMISSION("OPERATION_PERMISSION", "User must have permission for operation");
    
    private final String code;
    private final String description;
    
    ComplianceRuleType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ComplianceRuleType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (ComplianceRuleType type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown compliance rule type: " + code);
    }
}
