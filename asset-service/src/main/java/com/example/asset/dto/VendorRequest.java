
package com.example.asset.dto;

import com.example.asset.entity.VendorMaster;

/**
 * ✅ VendorRequest DTO
 * Wrapper for vendor requests with user info for auditing + notification.
 */
public class VendorRequest {

    private Long userId;
    private String username;
    private String projectType;
    private VendorMaster vendor;

    public VendorRequest() {}

    public VendorRequest(Long userId, String username, String projectType, VendorMaster vendor) {
        this.userId = userId;
        this.username = username;
        this.projectType = projectType;
        this.vendor = vendor;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public VendorMaster getVendor() { return vendor; }
    public void setVendor(VendorMaster vendor) { this.vendor = vendor; }
}


