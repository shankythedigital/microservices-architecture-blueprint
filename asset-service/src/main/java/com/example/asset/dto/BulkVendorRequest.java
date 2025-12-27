package com.example.asset.dto;

import java.util.List;

public class BulkVendorRequest {

    private Long userId;
    private String username;
    private String projectType;

    private List<SimpleVendorDto> vendors;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public List<SimpleVendorDto> getVendors() { return vendors; }
    public void setVendors(List<SimpleVendorDto> vendors) { this.vendors = vendors; }

    public static class SimpleVendorDto {
        private Long vendorId; // Primary key from Excel
        private String vendorName;
        private String contactPerson;
        private String email;
        private String mobile;
        private String address;

        public Long getVendorId() { return vendorId; }
        public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }

        public String getContactPerson() { return contactPerson; }
        public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
}

