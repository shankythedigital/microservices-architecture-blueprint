package com.example.asset.enums;

/**
 * ✅ ComplianceSeverity Enum
 * Defines severity levels for compliance violations.
 */
public enum ComplianceSeverity {
    
    CRITICAL("CRITICAL", "Critical violation - operation must be blocked"),
    HIGH("HIGH", "High severity - operation should be blocked"),
    MEDIUM("MEDIUM", "Medium severity - warning issued"),
    LOW("LOW", "Low severity - informational only"),
    INFO("INFO", "Informational - no action required");
    
    private final String code;
    private final String description;
    
    ComplianceSeverity(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ComplianceSeverity fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (ComplianceSeverity severity : values()) {
            if (severity.code.equalsIgnoreCase(code.trim())) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Unknown compliance severity: " + code);
    }
}
