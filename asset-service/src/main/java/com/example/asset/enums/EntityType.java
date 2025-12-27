package com.example.asset.enums;

/**
 * ✅ EntityType Enum
 * Defines all supported entity types in the asset management system.
 * Used for polymorphic relationships and document linking.
 */
public enum EntityType {
    
    ASSET("ASSET", "Asset entity"),
    COMPONENT("COMPONENT", "Component"),
    MAKE("MAKE", "Product Make"),
    MODEL("MODEL", "Product Model"),
    AMC("AMC", "Annual Maintenance Contract"),
    WARRANTY("WARRANTY", "Warranty"),
    DOCUMENT("DOCUMENT", "Document"),
    CATEGORY("CATEGORY", "Product Category"),
    SUBCATEGORY("SUBCATEGORY", "Product Subcategory"),
    OUTLET("OUTLET", "Purchase Outlet"),
    VENDOR("VENDOR", "Vendor");
    
    private final String code;
    private final String description;
    
    EntityType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get EntityType by code (case-insensitive)
     */
    public static EntityType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (EntityType type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown entity type code: " + code);
    }
    
    /**
     * Check if a code is a valid entity type
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
}
