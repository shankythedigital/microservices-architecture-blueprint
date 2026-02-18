package com.example.authservice.dto;

import com.example.common.util.PiiMaskingUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * ✅ UserDto
 * Safe data transfer object for exposing user information.
 * 🔐 DPDPA Compliance: All PII fields are automatically masked when serialized to JSON.
 * Used for:
 *  - GET /users/me
 *  - GET /users/{id}
 *  - GET /admin/users
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long userId;             // System-generated user ID
    private String username;         // Decrypted username (masked in getter)
    private String email;            // Decrypted email (masked in getter)
    private String mobile;           // Decrypted mobile (masked in getter)
    private String projectType;      // ECOM / ASSET / etc.
    private String pincode;
    private String city;
    private String state;
    private String country;
    private String countryCode;
    private String address1;
    private String address2;
    private String address3;
    private Boolean enabled;         // Account active?
    private Set<String> roles;       // ROLE_USER / ROLE_ADMIN
    private LocalDateTime lastLoginDate; // Last login timestamp
    
    // Internal storage for unmasked values (used internally, not serialized)
    private transient String usernameUnmasked;
    private transient String emailUnmasked;
    private transient String mobileUnmasked;

    public UserDto() {}

    public UserDto(Long userId, String username, String email, String mobile,
                   String projectType, Boolean enabled, Set<String> roles, LocalDateTime lastLoginDate) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.mobile = mobile;
        this.projectType = projectType;
        this.enabled = enabled;
        this.roles = roles;
        this.lastLoginDate = lastLoginDate;
    }

    // -------------------------
    // Getters & Setters
    // -------------------------
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    /**
     * 🔐 DPDPA Compliance: Returns masked username for frontend display
     * Example: "john_doe" → "j***_doe"
     */
    public String getUsername() {
        if (usernameUnmasked != null) {
            return PiiMaskingUtil.maskUsername(usernameUnmasked);
        }
        return username != null ? PiiMaskingUtil.maskUsername(username) : null;
    }
    
    /**
     * Sets username (stores unmasked value internally)
     */
    public void setUsername(String username) {
        this.usernameUnmasked = username;
        this.username = username;
    }

    /**
     * 🔐 DPDPA Compliance: Returns masked email for frontend display
     * Example: "john.doe@example.com" → "j***@e***.com"
     */
    public String getEmail() {
        if (emailUnmasked != null) {
            return PiiMaskingUtil.maskEmail(emailUnmasked);
        }
        return email != null ? PiiMaskingUtil.maskEmail(email) : null;
    }
    
    /**
     * Sets email (stores unmasked value internally)
     */
    public void setEmail(String email) {
        this.emailUnmasked = email;
        this.email = email;
    }

    /**
     * 🔐 DPDPA Compliance: Returns masked mobile for frontend display
     * Example: "9876543210" → "98765*****"
     */
    public String getMobile() {
        if (mobileUnmasked != null) {
            return PiiMaskingUtil.maskMobile(mobileUnmasked);
        }
        return mobile != null ? PiiMaskingUtil.maskMobile(mobile) : null;
    }
    
    /**
     * Sets mobile (stores unmasked value internally)
     */
    public void setMobile(String mobile) {
        this.mobileUnmasked = mobile;
        this.mobile = mobile;
    }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getAddress1() { return address1; }
    public void setAddress1(String address1) { this.address1 = address1; }
    public String getAddress2() { return address2; }
    public void setAddress2(String address2) { this.address2 = address2; }
    public String getAddress3() { return address3; }
    public void setAddress3(String address3) { this.address3 = address3; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public LocalDateTime getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    // -------------------------
    // Builder Pattern (Optional)
    // -------------------------
    public static UserDtoBuilder builder() {
        return new UserDtoBuilder();
    }

    public static class UserDtoBuilder {
        private final UserDto dto = new UserDto();

        public UserDtoBuilder userId(Long id) { dto.setUserId(id); return this; }
        public UserDtoBuilder username(String v) { dto.setUsername(v); return this; }
        public UserDtoBuilder email(String v) { dto.setEmail(v); return this; }
        public UserDtoBuilder mobile(String v) { dto.setMobile(v); return this; }
        public UserDtoBuilder projectType(String v) { dto.setProjectType(v); return this; }
        public UserDtoBuilder pincode(String v) { dto.setPincode(v); return this; }
        public UserDtoBuilder city(String v) { dto.setCity(v); return this; }
        public UserDtoBuilder state(String v) { dto.setState(v); return this; }
        public UserDtoBuilder country(String v) { dto.setCountry(v); return this; }
        public UserDtoBuilder countryCode(String v) { dto.setCountryCode(v); return this; }
        public UserDtoBuilder address1(String v) { dto.setAddress1(v); return this; }
        public UserDtoBuilder address2(String v) { dto.setAddress2(v); return this; }
        public UserDtoBuilder address3(String v) { dto.setAddress3(v); return this; }
        public UserDtoBuilder enabled(Boolean v) { dto.setEnabled(v); return this; }
        public UserDtoBuilder roles(Set<String> v) { dto.setRoles(v); return this; }
        public UserDtoBuilder lastLoginDate(LocalDateTime v) { dto.setLastLoginDate(v); return this; }

        public UserDto build() { return dto; }
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", projectType='" + projectType + '\'' +
                ", enabled=" + enabled +
                ", roles=" + roles +
                ", lastLoginDate=" + lastLoginDate +
                '}';
    }
}


