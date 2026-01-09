package com.example.authservice.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * MobileValidationUtil
 * Utility class for validating mobile numbers based on country codes.
 * Supports common country codes with their respective mobile number formats.
 */
public class MobileValidationUtil {

    // Country code to mobile pattern mapping
    private static final Map<String, Pattern> COUNTRY_PATTERNS = new HashMap<>();
    
    static {
        // India (+91): 10 digits starting with 6-9
        COUNTRY_PATTERNS.put("+91", Pattern.compile("^[6-9]\\d{9}$"));
        
        // USA/Canada (+1): 10 digits
        COUNTRY_PATTERNS.put("+1", Pattern.compile("^\\d{10}$"));
        
        // UK (+44): 10-11 digits (excluding leading 0)
        COUNTRY_PATTERNS.put("+44", Pattern.compile("^[1-9]\\d{9,10}$"));
        
        // Australia (+61): 9 digits (excluding leading 0)
        COUNTRY_PATTERNS.put("+61", Pattern.compile("^[1-9]\\d{8}$"));
        
        // Germany (+49): 10-11 digits (excluding leading 0)
        COUNTRY_PATTERNS.put("+49", Pattern.compile("^[1-9]\\d{9,10}$"));
        
        // France (+33): 9 digits (excluding leading 0)
        COUNTRY_PATTERNS.put("+33", Pattern.compile("^[1-9]\\d{8}$"));
        
        // China (+86): 11 digits starting with 1
        COUNTRY_PATTERNS.put("+86", Pattern.compile("^1\\d{10}$"));
        
        // Japan (+81): 10-11 digits (excluding leading 0)
        COUNTRY_PATTERNS.put("+81", Pattern.compile("^[1-9]\\d{9,10}$"));
        
        // Brazil (+55): 10-11 digits (excluding leading 0)
        COUNTRY_PATTERNS.put("+55", Pattern.compile("^[1-9]\\d{9,10}$"));
        
        // UAE (+971): 9 digits (excluding leading 0)
        COUNTRY_PATTERNS.put("+971", Pattern.compile("^[1-9]\\d{8}$"));
        
        // Singapore (+65): 8 digits
        COUNTRY_PATTERNS.put("+65", Pattern.compile("^\\d{8}$"));
        
        // Default pattern for countries not explicitly listed: 7-15 digits
        COUNTRY_PATTERNS.put("DEFAULT", Pattern.compile("^\\d{7,15}$"));
    }

    /**
     * Validates mobile number format based on country code
     * 
     * @param mobile Mobile number (without country code)
     * @param countryCode Country code (e.g., "+91", "+1")
     * @return true if valid, false otherwise
     */
    public static boolean isValidMobile(String mobile, String countryCode) {
        if (mobile == null || mobile.isBlank() || countryCode == null || countryCode.isBlank()) {
            return false;
        }
        
        // Remove any spaces, dashes, or parentheses
        String cleanedMobile = mobile.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Check if mobile contains only digits
        if (!cleanedMobile.matches("^\\d+$")) {
            return false;
        }
        
        // Get pattern for country code or use default
        Pattern pattern = COUNTRY_PATTERNS.getOrDefault(countryCode.toUpperCase(), COUNTRY_PATTERNS.get("DEFAULT"));
        
        return pattern.matcher(cleanedMobile).matches();
    }

    /**
     * Validates mobile number and returns error message if invalid
     * 
     * @param mobile Mobile number (without country code)
     * @param countryCode Country code (e.g., "+91", "+1")
     * @return Error message if invalid, null if valid
     */
    public static String validateMobileWithMessage(String mobile, String countryCode) {
        if (mobile == null || mobile.isBlank()) {
            return "Mobile number is required";
        }
        
        if (countryCode == null || countryCode.isBlank()) {
            return "Country code is required";
        }
        
        if (!isValidMobile(mobile, countryCode)) {
            return String.format("Invalid mobile number format for country code %s. Please enter a valid mobile number.", countryCode);
        }
        
        return null; // Valid
    }

    /**
     * Normalizes mobile number by removing spaces, dashes, and parentheses
     * 
     * @param mobile Mobile number
     * @return Normalized mobile number
     */
    public static String normalizeMobile(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            return mobile;
        }
        return mobile.replaceAll("[\\s\\-\\(\\)]", "");
    }

    /**
     * Formats mobile number with country code
     * 
     * @param mobile Mobile number
     * @param countryCode Country code
     * @return Formatted mobile number (e.g., "+91 9876543210")
     */
    public static String formatMobile(String mobile, String countryCode) {
        if (mobile == null || countryCode == null) {
            return mobile;
        }
        String normalized = normalizeMobile(mobile);
        return countryCode + " " + normalized;
    }
}

