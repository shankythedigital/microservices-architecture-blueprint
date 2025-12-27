package com.example.asset.enums;

/**
 * ✅ Status Enum
 * Defines all supported status values in the asset management system.
 * Includes statuses for Assets, AMC, Warranty, and general statuses.
 */
public enum Status {
    
    // ============================================================
    // ASSET STATUSES
    // ============================================================
    ASSET_AVAILABLE("ASSET_AVAILABLE", "Asset is available for assignment", "ASSET"),
    ASSET_ASSIGNED("ASSET_ASSIGNED", "Asset is assigned to a user", "ASSET"),
    ASSET_SOLD("ASSET_SOLD", "Asset has been sold", "ASSET"),
    ASSET_DAMAGED("ASSET_DAMAGED", "Asset is damaged", "ASSET"),
    ASSET_LOST("ASSET_LOST", "Asset is lost", "ASSET"),
    ASSET_UNDER_MAINTENANCE("ASSET_UNDER_MAINTENANCE", "Asset is under maintenance", "ASSET"),
    ASSET_RETIRED("ASSET_RETIRED", "Asset is retired", "ASSET"),
    
    // ============================================================
    // AMC STATUSES
    // ============================================================
    AMC_ACTIVE("AMC_ACTIVE", "AMC is active and valid", "AMC"),
    AMC_EXPIRED("AMC_EXPIRED", "AMC has expired", "AMC"),
    AMC_PENDING("AMC_PENDING", "AMC is pending activation", "AMC"),
    AMC_RENEWED("AMC_RENEWED", "AMC has been renewed", "AMC"),
    AMC_CANCELLED("AMC_CANCELLED", "AMC has been cancelled", "AMC"),
    
    // ============================================================
    // WARRANTY STATUSES
    // ============================================================
    WARRANTY_ACTIVE("WARRANTY_ACTIVE", "Warranty is active and valid", "WARRANTY"),
    WARRANTY_EXPIRED("WARRANTY_EXPIRED", "Warranty has expired", "WARRANTY"),
    WARRANTY_PENDING("WARRANTY_PENDING", "Warranty is pending activation", "WARRANTY"),
    WARRANTY_CLAIMED("WARRANTY_CLAIMED", "Warranty claim has been made", "WARRANTY"),
    WARRANTY_VOID("WARRANTY_VOID", "Warranty is void", "WARRANTY"),
    
    // ============================================================
    // GENERAL STATUSES
    // ============================================================
    ACTIVE("ACTIVE", "Entity is active", "GENERAL"),
    INACTIVE("INACTIVE", "Entity is inactive", "GENERAL"),
    PENDING("PENDING", "Entity is pending", "GENERAL"),
    APPROVED("APPROVED", "Entity is approved", "GENERAL"),
    REJECTED("REJECTED", "Entity is rejected", "GENERAL"),
    COMPLETED("COMPLETED", "Process is completed", "GENERAL"),
    IN_PROGRESS("IN_PROGRESS", "Process is in progress", "GENERAL"),
    CANCELLED("CANCELLED", "Process is cancelled", "GENERAL");
    
    private final String code;
    private final String description;
    private final String category; // ASSET, AMC, WARRANTY, GENERAL
    
    Status(String code, String description, String category) {
        this.code = code;
        this.description = description;
        this.category = category;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCategory() {
        return category;
    }
    
    /**
     * Get Status by code (case-insensitive)
     */
    public static Status fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (Status status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
    
    /**
     * Check if a code is a valid status
     */
    public static boolean isValid(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Get all statuses for a specific category
     */
    public static Status[] getByCategory(String category) {
        if (category == null || category.isBlank()) {
            return new Status[0];
        }
        return java.util.Arrays.stream(values())
                .filter(s -> s.category.equalsIgnoreCase(category.trim()))
                .toArray(Status[]::new);
    }
}
