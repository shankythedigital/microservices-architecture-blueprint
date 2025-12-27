package com.example.asset.dto;

import java.util.List;

public class BulkOutletRequest {

    private Long userId;
    private String username;
    private String projectType;

    private List<SimpleOutletDto> outlets;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public List<SimpleOutletDto> getOutlets() { return outlets; }
    public void setOutlets(List<SimpleOutletDto> outlets) { this.outlets = outlets; }

    public static class SimpleOutletDto {
        private Long outletId; // Primary key from Excel
        private String outletName;
        private String outletAddress;
        private String contactInfo;
        private Long vendorId; // Foreign key to VendorMaster (from Excel)

        public Long getOutletId() { return outletId; }
        public void setOutletId(Long outletId) { this.outletId = outletId; }

        public String getOutletName() { return outletName; }
        public void setOutletName(String outletName) { this.outletName = outletName; }

        public String getOutletAddress() { return outletAddress; }
        public void setOutletAddress(String outletAddress) { this.outletAddress = outletAddress; }

        public String getContactInfo() { return contactInfo; }
        public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

        public Long getVendorId() { return vendorId; }
        public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    }
}

