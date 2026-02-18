package com.example.asset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ✅ QrUniversalEntityDto
 * Universal standard JSON format for QR codes that are NOT part of asset management.
 * Used when entityType is not available in the entity master.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrUniversalEntityDto {

    /** Raw value extracted from the QR code */
    private String rawValue;
    /** Source of the scan: QR_SCAN */
    private String source = "QR_SCAN";
    /** Whether the data matched an asset management entity */
    private boolean assetManagementEntity = false;
    /** Human-readable description */
    private String description;
    /** Optional: detected format (TEXT, JSON, URL, etc.) */
    private String detectedFormat;

    public QrUniversalEntityDto() {}

    public QrUniversalEntityDto(String rawValue) {
        this.rawValue = rawValue;
        this.description = "QR code data is not part of asset management. Entity type not found in entity master.";
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isAssetManagementEntity() {
        return assetManagementEntity;
    }

    public void setAssetManagementEntity(boolean assetManagementEntity) {
        this.assetManagementEntity = assetManagementEntity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetectedFormat() {
        return detectedFormat;
    }

    public void setDetectedFormat(String detectedFormat) {
        this.detectedFormat = detectedFormat;
    }
}
