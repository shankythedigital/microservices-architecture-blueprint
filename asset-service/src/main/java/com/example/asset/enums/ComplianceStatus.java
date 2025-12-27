package com.example.asset.enums;

/**
 * ✅ ComplianceStatus Enum
 * Defines status of compliance checks.
 */
public enum ComplianceStatus {
    
    COMPLIANT("COMPLIANT", "Entity is compliant with all rules"),
    NON_COMPLIANT("NON_COMPLIANT", "Entity has compliance violations"),
    PENDING("PENDING", "Compliance check is pending"),
    EXEMPTED("EXEMPTED", "Entity is exempted from compliance check"),
    UNDER_REVIEW("UNDER_REVIEW", "Compliance is under review");
    
    private final String code;
    private final String description;
    
    ComplianceStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ComplianceStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (ComplianceStatus status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown compliance status: " + code);
    }
}
