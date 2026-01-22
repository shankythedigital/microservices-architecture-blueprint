package com.example.asset.dto;

/**
 * ✅ AssetScanRequest
 * Request DTO for scanning QR codes or barcodes to identify assets.
 */
public class AssetScanRequest {
    
    private String scanValue; // QR code or barcode value
    private String scanType;  // Optional: "QR", "BARCODE", or "AUTO" (default)
    
    public AssetScanRequest() {
    }
    
    public AssetScanRequest(String scanValue) {
        this.scanValue = scanValue;
        this.scanType = "AUTO";
    }
    
    public AssetScanRequest(String scanValue, String scanType) {
        this.scanValue = scanValue;
        this.scanType = scanType;
    }
    
    public String getScanValue() {
        return scanValue;
    }
    
    public void setScanValue(String scanValue) {
        this.scanValue = scanValue;
    }
    
    public String getScanType() {
        return scanType;
    }
    
    public void setScanType(String scanType) {
        this.scanType = scanType;
    }
    
    @Override
    public String toString() {
        return "AssetScanRequest{" +
                "scanValue='" + scanValue + '\'' +
                ", scanType='" + scanType + '\'' +
                '}';
    }
}

