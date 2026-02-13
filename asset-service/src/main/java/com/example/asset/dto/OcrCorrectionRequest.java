package com.example.asset.dto;

/**
 * ✅ OcrCorrectionRequest
 * Request DTO for submitting OCR corrections/feedback.
 */
public class OcrCorrectionRequest {
    
    private Long trainingId;
    private String correctedMake;
    private String correctedModel;
    private String correctedSerial;
    private Long userId;
    private String username;

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public OcrCorrectionRequest() {}

    // ============================================================
    // ✅ Getters and Setters
    // ============================================================
    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public String getCorrectedMake() {
        return correctedMake;
    }

    public void setCorrectedMake(String correctedMake) {
        this.correctedMake = correctedMake;
    }

    public String getCorrectedModel() {
        return correctedModel;
    }

    public void setCorrectedModel(String correctedModel) {
        this.correctedModel = correctedModel;
    }

    public String getCorrectedSerial() {
        return correctedSerial;
    }

    public void setCorrectedSerial(String correctedSerial) {
        this.correctedSerial = correctedSerial;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

