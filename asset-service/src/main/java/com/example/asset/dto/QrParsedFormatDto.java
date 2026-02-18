package com.example.asset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ✅ QrParsedFormatDto
 * Standard format for parsed QR codes (WIFI, vCard, MeCard, mailto, tel, geo, bitcoin).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrParsedFormatDto {

    private String rawValue;
    private String source = "QR_SCAN";
    private boolean assetManagementEntity = false;
    private String detectedFormat;
    private String description;

    // WIFI
    private String ssid;
    private String security;  // WPA, WEP, nopass
    private String password;

    // Contact (vCard, MeCard, mailto, tel)
    private String name;
    private String email;
    private String phone;
    private String organization;

    // Geo
    private Double latitude;
    private Double longitude;
    private String query;

    // Bitcoin
    private String address;
    private String amount;

    public QrParsedFormatDto() {}

    public QrParsedFormatDto(String rawValue, String detectedFormat) {
        this.rawValue = rawValue;
        this.detectedFormat = detectedFormat;
        this.description = "Parsed QR format. Not part of asset management entity master.";
    }

    public String getRawValue() { return rawValue; }
    public void setRawValue(String rawValue) { this.rawValue = rawValue; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public boolean isAssetManagementEntity() { return assetManagementEntity; }
    public void setAssetManagementEntity(boolean v) { this.assetManagementEntity = v; }
    public String getDetectedFormat() { return detectedFormat; }
    public void setDetectedFormat(String detectedFormat) { this.detectedFormat = detectedFormat; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSsid() { return ssid; }
    public void setSsid(String ssid) { this.ssid = ssid; }
    public String getSecurity() { return security; }
    public void setSecurity(String security) { this.security = security; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
}
