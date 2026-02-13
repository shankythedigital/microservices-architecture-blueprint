package com.example.asset.entity;

import com.example.common.converter.JpaAttributeEncryptor;
import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

/**
 * ✅ OcrTrainingData Entity
 * Stores OCR extraction results and user corrections for training purposes.
 * This table accumulates training data without modifying existing table structures.
 * 🔐 DPDPA Compliance: All PII data (username) is encrypted at rest.
 */
@Entity
@Table(name = "ocr_training_data")
public class OcrTrainingData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_id")
    private Long trainingId;

    @Column(name = "image_hash", length = 64)
    private String imageHash; // SHA-256 hash of image for deduplication

    @Column(name = "original_ocr_text", columnDefinition = "TEXT")
    private String originalOcrText;

    @Column(name = "extracted_make", length = 255)
    private String extractedMake;

    @Column(name = "extracted_model", length = 255)
    private String extractedModel;

    @Column(name = "extracted_serial", length = 255)
    private String extractedSerial;

    @Column(name = "corrected_make", length = 255)
    private String correctedMake; // User correction

    @Column(name = "corrected_model", length = 255)
    private String correctedModel; // User correction

    @Column(name = "corrected_serial", length = 255)
    private String correctedSerial; // User correction

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "user_id")
    private Long userId;

    // 🔐 DPDPA Compliance: Encrypted PII data
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "username_enc", columnDefinition = "TEXT")
    private String username;

    @Column(name = "is_corrected")
    private Boolean isCorrected = false; // Whether user provided corrections

    @Column(name = "sub_category_id")
    private Long subCategoryId; // Context for better pattern learning

    @Column(name = "make_id")
    private Long makeId; // Resolved make ID

    @Column(name = "model_id")
    private Long modelId; // Resolved model ID

    @Column(name = "extraction_pattern", columnDefinition = "TEXT")
    private String extractionPattern; // JSON pattern of how data was extracted

    @Column(name = "image_features", columnDefinition = "TEXT")
    private String imageFeatures; // JSON of image characteristics (size, format, etc.)

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public OcrTrainingData() {}

    // ============================================================
    // ✅ Getters and Setters
    // ============================================================
    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long trainingId) {
        this.trainingId = trainingId;
    }

    public String getImageHash() {
        return imageHash;
    }

    public void setImageHash(String imageHash) {
        this.imageHash = imageHash;
    }

    public String getOriginalOcrText() {
        return originalOcrText;
    }

    public void setOriginalOcrText(String originalOcrText) {
        this.originalOcrText = originalOcrText;
    }

    public String getExtractedMake() {
        return extractedMake;
    }

    public void setExtractedMake(String extractedMake) {
        this.extractedMake = extractedMake;
    }

    public String getExtractedModel() {
        return extractedModel;
    }

    public void setExtractedModel(String extractedModel) {
        this.extractedModel = extractedModel;
    }

    public String getExtractedSerial() {
        return extractedSerial;
    }

    public void setExtractedSerial(String extractedSerial) {
        this.extractedSerial = extractedSerial;
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

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
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

    public Boolean getIsCorrected() {
        return isCorrected;
    }

    public void setIsCorrected(Boolean isCorrected) {
        this.isCorrected = isCorrected;
    }

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public Long getMakeId() {
        return makeId;
    }

    public void setMakeId(Long makeId) {
        this.makeId = makeId;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getExtractionPattern() {
        return extractionPattern;
    }

    public void setExtractionPattern(String extractionPattern) {
        this.extractionPattern = extractionPattern;
    }

    public String getImageFeatures() {
        return imageFeatures;
    }

    public void setImageFeatures(String imageFeatures) {
        this.imageFeatures = imageFeatures;
    }
}

