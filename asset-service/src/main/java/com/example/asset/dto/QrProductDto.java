package com.example.asset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ✅ QrProductDto
 * Standard format for product/GTIN barcodes (EAN-13, UPC-A, EAN-8, UPC-E).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrProductDto {

    private String rawValue;
    private String source = "QR_SCAN";
    private boolean assetManagementEntity = false;
    private String detectedFormat;  // EAN-13, UPC-A, EAN-8, UPC-E, GTIN
    private Integer digitCount;
    private String description;

    public QrProductDto(String rawValue, String detectedFormat, int digitCount) {
        this.rawValue = rawValue;
        this.detectedFormat = detectedFormat;
        this.digitCount = digitCount;
        this.description = "Product barcode (GTIN). Not part of asset management entity master.";
    }

    public String getRawValue() { return rawValue; }
    public void setRawValue(String rawValue) { this.rawValue = rawValue; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public boolean isAssetManagementEntity() { return assetManagementEntity; }
    public void setAssetManagementEntity(boolean v) { this.assetManagementEntity = v; }
    public String getDetectedFormat() { return detectedFormat; }
    public void setDetectedFormat(String detectedFormat) { this.detectedFormat = detectedFormat; }
    public Integer getDigitCount() { return digitCount; }
    public void setDigitCount(Integer digitCount) { this.digitCount = digitCount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
