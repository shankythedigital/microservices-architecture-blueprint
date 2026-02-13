package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

/**
 * ✅ OcrModelMetadata Entity
 * Stores ML model metadata, parameters, and training statistics.
 * Tracks model versions and performance metrics.
 */
@Entity
@Table(name = "ocr_model_metadata")
public class OcrModelMetadata extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Long modelId;

    @Column(name = "model_version", length = 50, nullable = false)
    private String modelVersion; // e.g., "1.0.0", "2.1.3"

    @Column(name = "model_type", length = 50, nullable = false)
    private String modelType; // PATTERN_MATCHING, REGEX, ML_CLASSIFIER, etc.

    @Column(name = "model_parameters", columnDefinition = "TEXT")
    private String modelParameters; // JSON of model parameters

    @Column(name = "training_samples_count")
    private Integer trainingSamplesCount = 0;

    @Column(name = "validation_samples_count")
    private Integer validationSamplesCount = 0;

    @Column(name = "accuracy_score")
    private Double accuracyScore; // Overall accuracy (0-1)

    @Column(name = "precision_score")
    private Double precisionScore; // Precision metric

    @Column(name = "recall_score")
    private Double recallScore; // Recall metric

    @Column(name = "f1_score")
    private Double f1Score; // F1 score

    @Column(name = "is_active")
    private Boolean isActive = false; // Only one active model at a time

    @Column(name = "trained_at")
    private java.time.LocalDateTime trainedAt;

    @Column(name = "trained_by", length = 255)
    private String trainedBy;

    @Column(name = "training_duration_seconds")
    private Long trainingDurationSeconds;

    @Column(name = "model_file_path", length = 512)
    private String modelFilePath; // Path to serialized model file (if applicable)

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Training notes and observations

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public OcrModelMetadata() {}

    // ============================================================
    // ✅ Getters and Setters
    // ============================================================
    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getModelParameters() {
        return modelParameters;
    }

    public void setModelParameters(String modelParameters) {
        this.modelParameters = modelParameters;
    }

    public Integer getTrainingSamplesCount() {
        return trainingSamplesCount;
    }

    public void setTrainingSamplesCount(Integer trainingSamplesCount) {
        this.trainingSamplesCount = trainingSamplesCount;
    }

    public Integer getValidationSamplesCount() {
        return validationSamplesCount;
    }

    public void setValidationSamplesCount(Integer validationSamplesCount) {
        this.validationSamplesCount = validationSamplesCount;
    }

    public Double getAccuracyScore() {
        return accuracyScore;
    }

    public void setAccuracyScore(Double accuracyScore) {
        this.accuracyScore = accuracyScore;
    }

    public Double getPrecisionScore() {
        return precisionScore;
    }

    public void setPrecisionScore(Double precisionScore) {
        this.precisionScore = precisionScore;
    }

    public Double getRecallScore() {
        return recallScore;
    }

    public void setRecallScore(Double recallScore) {
        this.recallScore = recallScore;
    }

    public Double getF1Score() {
        return f1Score;
    }

    public void setF1Score(Double f1Score) {
        this.f1Score = f1Score;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public java.time.LocalDateTime getTrainedAt() {
        return trainedAt;
    }

    public void setTrainedAt(java.time.LocalDateTime trainedAt) {
        this.trainedAt = trainedAt;
    }

    public String getTrainedBy() {
        return trainedBy;
    }

    public void setTrainedBy(String trainedBy) {
        this.trainedBy = trainedBy;
    }

    public Long getTrainingDurationSeconds() {
        return trainingDurationSeconds;
    }

    public void setTrainingDurationSeconds(Long trainingDurationSeconds) {
        this.trainingDurationSeconds = trainingDurationSeconds;
    }

    public String getModelFilePath() {
        return modelFilePath;
    }

    public void setModelFilePath(String modelFilePath) {
        this.modelFilePath = modelFilePath;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

