package com.example.authservice.dto;

import com.example.common.util.PiiMaskingUtil;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 🔐 DPDPA Compliance: All PII fields are automatically masked when accessed
 */
public class UserDetailDto {
    public Long userId;
    
    // 🔐 DPDPA Compliance: Masked PII fields
    private String username;
    private String email;
    private String mobile;
    private String employeeId;
    
    public LocalDateTime loginDate;
    public LocalDateTime lastLoginDate;
    public Integer failedAttempts;
    public Boolean accountLocked;
    
    // Internal storage for unmasked values
    private transient String usernameUnmasked;
    private transient String emailUnmasked;
    private transient String mobileUnmasked;
    private transient String employeeIdUnmasked;
    
    // Getters with masking
    public String getUsername() {
        return usernameUnmasked != null ? PiiMaskingUtil.maskUsername(usernameUnmasked) : 
               (username != null ? PiiMaskingUtil.maskUsername(username) : null);
    }
    
    public void setUsername(String username) {
        this.usernameUnmasked = username;
        this.username = username;
    }
    
    public String getEmail() {
        return emailUnmasked != null ? PiiMaskingUtil.maskEmail(emailUnmasked) : 
               (email != null ? PiiMaskingUtil.maskEmail(email) : null);
    }
    
    public void setEmail(String email) {
        this.emailUnmasked = email;
        this.email = email;
    }
    
    public String getMobile() {
        return mobileUnmasked != null ? PiiMaskingUtil.maskMobile(mobileUnmasked) : 
               (mobile != null ? PiiMaskingUtil.maskMobile(mobile) : null);
    }
    
    public void setMobile(String mobile) {
        this.mobileUnmasked = mobile;
        this.mobile = mobile;
    }
    
    public String getEmployeeId() {
        return employeeIdUnmasked != null ? PiiMaskingUtil.maskUsername(employeeIdUnmasked) : 
               (employeeId != null ? PiiMaskingUtil.maskUsername(employeeId) : null);
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeIdUnmasked = employeeId;
        this.employeeId = employeeId;
    }
}
