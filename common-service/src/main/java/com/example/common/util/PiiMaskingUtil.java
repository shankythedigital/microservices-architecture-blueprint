package com.example.common.util;

/**
 * =======================================================================
 * 🔐 PII Masking Utility - DPDPA Compliance
 * =======================================================================
 * 
 * Utility class for masking Personally Identifiable Information (PII)
 * before sending data to frontend, ensuring DPDPA compliance.
 * 
 * Masking patterns:
 * - Email: Shows first character and domain, masks middle part
 * - Mobile: Shows first 5 digits, masks rest
 * - Username: Shows first character, masks rest
 * - User ID (if email): Same as email masking
 * 
 * =======================================================================
 */
public class PiiMaskingUtil {

    private static final String MASK_CHAR = "*";
    private static final int EMAIL_VISIBLE_START = 1;
    private static final int EMAIL_VISIBLE_END = 2;
    private static final int MOBILE_VISIBLE_DIGITS = 5;
    private static final int USERNAME_VISIBLE_START = 1;

    /**
     * Masks an email address
     * Example: "john.doe@example.com" → "j***@e***.com"
     * 
     * @param email The email address to mask
     * @return Masked email address, or null if input is null/empty
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }

        email = email.trim();
        int atIndex = email.indexOf('@');
        
        if (atIndex <= 0) {
            // Invalid email format, mask entire string
            return maskString(email, 1, 0);
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex + 1);
        
        // Mask local part: show first character
        String maskedLocal = maskString(localPart, EMAIL_VISIBLE_START, 0);
        
        // Mask domain part: show first 2 characters of domain name
        int dotIndex = domainPart.indexOf('.');
        if (dotIndex > 0) {
            String domainName = domainPart.substring(0, dotIndex);
            String domainExt = domainPart.substring(dotIndex);
            String maskedDomainName = maskString(domainName, EMAIL_VISIBLE_END, 0);
            return maskedLocal + "@" + maskedDomainName + domainExt;
        } else {
            // No dot in domain, mask all but first 2 chars
            String maskedDomain = maskString(domainPart, EMAIL_VISIBLE_END, 0);
            return maskedLocal + "@" + maskedDomain;
        }
    }

    /**
     * Masks a mobile number
     * Example: "9876543210" → "98765*****"
     * 
     * @param mobile The mobile number to mask
     * @return Masked mobile number, or null if input is null/empty
     */
    public static String maskMobile(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            return mobile;
        }

        mobile = mobile.trim();
        
        // Remove any non-digit characters for masking
        String digitsOnly = mobile.replaceAll("[^0-9]", "");
        
        if (digitsOnly.length() <= MOBILE_VISIBLE_DIGITS) {
            // Too short, mask all but first character
            return maskString(mobile, 1, 0);
        }

        // Show first 5 digits, mask the rest
        String visible = digitsOnly.substring(0, MOBILE_VISIBLE_DIGITS);
        String masked = MASK_CHAR.repeat(Math.max(0, digitsOnly.length() - MOBILE_VISIBLE_DIGITS));
        
        // Preserve original format if it had separators
        if (mobile.length() != digitsOnly.length()) {
            // Had separators, try to preserve format
            return visible + masked;
        }
        
        return visible + masked;
    }

    /**
     * Masks a username
     * Example: "john_doe" → "j***_doe" or "j***" for short usernames
     * 
     * @param username The username to mask
     * @return Masked username, or null if input is null/empty
     */
    public static String maskUsername(String username) {
        if (username == null || username.isBlank()) {
            return username;
        }

        username = username.trim();
        
        if (username.length() <= 3) {
            // Very short username, mask all but first character
            return maskString(username, USERNAME_VISIBLE_START, 0);
        }

        // Show first character, mask middle, show last 3 characters if username is long enough
        if (username.length() > 6) {
            int visibleEnd = 3;
            return maskString(username, USERNAME_VISIBLE_START, visibleEnd);
        } else {
            // Short username, just show first character
            return maskString(username, USERNAME_VISIBLE_START, 0);
        }
    }

    /**
     * Masks a user ID if it appears to be an email, otherwise masks as username
     * 
     * @param userId The user ID to mask
     * @return Masked user ID
     */
    public static String maskUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return userId;
        }

        userId = userId.trim();
        
        // Check if it's an email format
        if (userId.contains("@")) {
            return maskEmail(userId);
        }
        
        // Otherwise mask as username
        return maskUsername(userId);
    }

    /**
     * Generic string masking utility
     * 
     * @param input The string to mask
     * @param visibleStart Number of characters to show at the start
     * @param visibleEnd Number of characters to show at the end (0 = don't show end)
     * @return Masked string
     */
    private static String maskString(String input, int visibleStart, int visibleEnd) {
        if (input == null || input.isBlank()) {
            return input;
        }

        int length = input.length();
        
        if (length <= visibleStart) {
            // Too short, return all masked except first char
            return input.charAt(0) + MASK_CHAR.repeat(Math.max(0, length - 1));
        }

        if (visibleEnd > 0 && length > visibleStart + visibleEnd) {
            // Show start and end
            String start = input.substring(0, visibleStart);
            String end = input.substring(length - visibleEnd);
            int maskLength = length - visibleStart - visibleEnd;
            return start + MASK_CHAR.repeat(maskLength) + end;
        } else {
            // Show only start
            String start = input.substring(0, visibleStart);
            int maskLength = length - visibleStart;
            return start + MASK_CHAR.repeat(maskLength);
        }
    }

    /**
     * Conditionally masks PII data based on user role/permissions
     * For now, always masks. Can be extended to check user permissions.
     * 
     * @param value The PII value to mask
     * @param type The type of PII (EMAIL, MOBILE, USERNAME, USER_ID)
     * @return Masked value
     */
    public static String maskPii(String value, PiiType type) {
        if (value == null || value.isBlank()) {
            return value;
        }

        return switch (type) {
            case EMAIL -> maskEmail(value);
            case MOBILE -> maskMobile(value);
            case USERNAME -> maskUsername(value);
            case USER_ID -> maskUserId(value);
        };
    }

    /**
     * Enum for PII types
     */
    public enum PiiType {
        EMAIL,
        MOBILE,
        USERNAME,
        USER_ID
    }
}

