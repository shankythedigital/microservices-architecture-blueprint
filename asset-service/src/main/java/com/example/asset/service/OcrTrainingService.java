package com.example.asset.service;

import com.example.asset.dto.ProductOcrResponse;
import com.example.asset.entity.OcrLearnedPattern;
import com.example.asset.entity.OcrModelMetadata;
import com.example.asset.entity.OcrTrainingData;
import com.example.asset.repository.OcrLearnedPatternRepository;
import com.example.asset.repository.OcrModelMetadataRepository;
import com.example.asset.repository.OcrTrainingDataRepository;
import com.example.common.jackson.JacksonObjectMappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ✅ OcrTrainingService
 * Service for learning from OCR training data and improving extraction patterns.
 * Analyzes corrections, learns patterns, and updates model metadata.
 */
@Service
@Transactional
public class OcrTrainingService {

    private static final Logger log = LoggerFactory.getLogger(OcrTrainingService.class);
    private final ObjectMapper objectMapper = JacksonObjectMappers.standard();

    private final OcrTrainingDataRepository trainingDataRepo;
    private final OcrLearnedPatternRepository patternRepo;
    private final OcrModelMetadataRepository modelMetadataRepo;

    public OcrTrainingService(
            OcrTrainingDataRepository trainingDataRepo,
            OcrLearnedPatternRepository patternRepo,
            OcrModelMetadataRepository modelMetadataRepo) {
        this.trainingDataRepo = trainingDataRepo;
        this.patternRepo = patternRepo;
        this.modelMetadataRepo = modelMetadataRepo;
    }

    // ============================================================
    // 💾 SAVE TRAINING DATA
    // ============================================================
    public OcrTrainingData saveTrainingData(
            String ocrText,
            ProductOcrResponse.ExtractedProductInfo extractedInfo,
            ProductOcrResponse response,
            Long userId,
            String username,
            Long subCategoryId,
            byte[] imageBytes) {
        
        OcrTrainingData trainingData = new OcrTrainingData();
        trainingData.setOriginalOcrText(ocrText);
        trainingData.setExtractedMake(extractedInfo.getMakeName());
        trainingData.setExtractedModel(extractedInfo.getModelName());
        trainingData.setExtractedSerial(extractedInfo.getSerialNumber());
        trainingData.setConfidenceScore(response.getConfidence());
        trainingData.setUserId(userId);
        trainingData.setUsername(username);
        trainingData.setSubCategoryId(subCategoryId);
        trainingData.setMakeId(response.getMake() != null ? response.getMake().getMakeId() : null);
        trainingData.setModelId(response.getModel() != null ? response.getModel().getModelId() : null);
        trainingData.setCreatedBy(username);
        trainingData.setUpdatedBy(username);

        // Generate image hash for deduplication
        if (imageBytes != null) {
            trainingData.setImageHash(calculateImageHash(imageBytes));
        }

        // Store extraction pattern as JSON
        try {
            Map<String, Object> pattern = new HashMap<>();
            pattern.put("extractionMethod", "PATTERN_MATCHING");
            pattern.put("confidence", response.getConfidence());
            pattern.put("timestamp", LocalDateTime.now().toString());
            trainingData.setExtractionPattern(objectMapper.writeValueAsString(pattern));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize extraction pattern: {}", e.getMessage());
        }

        // Store image features
        try {
            Map<String, Object> features = new HashMap<>();
            if (imageBytes != null) {
                features.put("size", imageBytes.length);
            }
            trainingData.setImageFeatures(objectMapper.writeValueAsString(features));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize image features: {}", e.getMessage());
        }

        OcrTrainingData saved = trainingDataRepo.save(trainingData);
        log.info("✅ Training data saved: trainingId={}, userId={}", saved.getTrainingId(), userId);
        return saved;
    }

    // ============================================================
    // ✏️ SAVE CORRECTION (User feedback)
    // ============================================================
    public OcrTrainingData saveCorrection(
            Long trainingId,
            String correctedMake,
            String correctedModel,
            String correctedSerial,
            String username) {
        
        return trainingDataRepo.findById(trainingId).map(trainingData -> {
            trainingData.setCorrectedMake(correctedMake);
            trainingData.setCorrectedModel(correctedModel);
            trainingData.setCorrectedSerial(correctedSerial);
            trainingData.setIsCorrected(true);
            trainingData.setUpdatedBy(username);
            
            OcrTrainingData saved = trainingDataRepo.save(trainingData);
            log.info("✅ Correction saved: trainingId={}", trainingId);
            
            // Trigger pattern learning from this correction
            learnFromCorrection(saved);
            
            return saved;
        }).orElseThrow(() -> new RuntimeException("Training data not found: " + trainingId));
    }

    // ============================================================
    // 🧠 LEARN FROM CORRECTIONS
    // ============================================================
    private void learnFromCorrection(OcrTrainingData correction) {
        log.info("🧠 Learning from correction: trainingId={}", correction.getTrainingId());
        
        String ocrText = correction.getOriginalOcrText();
        if (ocrText == null) return;

        // Learn make pattern
        if (correction.getCorrectedMake() != null && !correction.getCorrectedMake().equals(correction.getExtractedMake())) {
            learnPattern("MAKE", ocrText, correction.getCorrectedMake(), correction.getExtractedMake(), 
                        correction.getSubCategoryId(), correction.getMakeId());
        }

        // Learn model pattern
        if (correction.getCorrectedModel() != null && !correction.getCorrectedModel().equals(correction.getExtractedModel())) {
            learnPattern("MODEL", ocrText, correction.getCorrectedModel(), correction.getExtractedModel(),
                        correction.getSubCategoryId(), correction.getMakeId());
        }

        // Learn serial pattern
        if (correction.getCorrectedSerial() != null && !correction.getCorrectedSerial().equals(correction.getExtractedSerial())) {
            learnPattern("SERIAL", ocrText, correction.getCorrectedSerial(), correction.getExtractedSerial(),
                        correction.getSubCategoryId(), null);
        }
    }

    // ============================================================
    // 📚 LEARN PATTERN FROM TEXT
    // ============================================================
    private void learnPattern(String patternType, String ocrText, String correctValue, 
                              String incorrectValue, Long subCategoryId, Long makeId) {
        // Find context around the correct value
        String contextBefore = extractContextBefore(ocrText, correctValue);
        String contextAfter = extractContextAfter(ocrText, correctValue);
        
        // Generate regex pattern
        String regexPattern = generateRegexPattern(ocrText, correctValue, contextBefore, contextAfter);
        
        // Check if similar pattern exists
        Optional<OcrLearnedPattern> existingPattern = patternRepo
            .findByPatternTypeAndPatternRegexAndSubCategoryId(patternType, regexPattern, subCategoryId);
        
        if (existingPattern.isPresent()) {
            // Update existing pattern
            OcrLearnedPattern pattern = existingPattern.get();
            pattern.setUsageCount(pattern.getUsageCount() + 1);
            pattern.setSuccessCount(pattern.getSuccessCount() + 1);
            pattern.setLastUsedAt(LocalDateTime.now());
            // Increase confidence weight
            pattern.setConfidenceWeight(Math.min(pattern.getConfidenceWeight() + 0.1, 5.0));
            patternRepo.save(pattern);
            log.debug("✅ Updated existing pattern: patternId={}, type={}", pattern.getPatternId(), patternType);
        } else {
            // Create new pattern
            OcrLearnedPattern pattern = new OcrLearnedPattern();
            pattern.setPatternType(patternType);
            pattern.setPatternRegex(regexPattern);
            pattern.setContextBefore(contextBefore);
            pattern.setContextAfter(contextAfter);
            pattern.setSubCategoryId(subCategoryId);
            pattern.setMakeId(makeId);
            pattern.setUsageCount(1);
            pattern.setSuccessCount(1);
            pattern.setConfidenceWeight(1.0);
            pattern.setIsActive(true);
            pattern.setLastUsedAt(LocalDateTime.now());
            pattern.setCreatedBy("SYSTEM");
            pattern.setUpdatedBy("SYSTEM");
            
            // Extract keywords
            try {
                List<String> keywords = extractKeywords(ocrText, correctValue);
                pattern.setPatternKeywords(objectMapper.writeValueAsString(keywords));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize keywords: {}", e.getMessage());
            }
            
            patternRepo.save(pattern);
            log.info("✅ Learned new pattern: patternId={}, type={}, regex={}", 
                    pattern.getPatternId(), patternType, regexPattern);
        }
    }

    // ============================================================
    // 🔍 EXTRACT CONTEXT
    // ============================================================
    private String extractContextBefore(String text, String value) {
        int index = text.indexOf(value);
        if (index <= 0) return "";
        int start = Math.max(0, index - 50);
        return text.substring(start, index).trim();
    }

    private String extractContextAfter(String text, String value) {
        int index = text.indexOf(value);
        if (index < 0) return "";
        int end = Math.min(text.length(), index + value.length() + 50);
        return text.substring(index + value.length(), end).trim();
    }

    // ============================================================
    // 📝 GENERATE REGEX PATTERN
    // ============================================================
    private String generateRegexPattern(String text, String value, String contextBefore, String contextAfter) {
        // Escape special regex characters in value
        String escapedValue = Pattern.quote(value);
        
        // Build pattern with context
        StringBuilder regex = new StringBuilder();
        if (contextBefore != null && !contextBefore.isEmpty()) {
            regex.append("(?:").append(Pattern.quote(contextBefore)).append(")");
        }
        regex.append("(").append(escapedValue).append(")");
        if (contextAfter != null && !contextAfter.isEmpty()) {
            regex.append("(?:").append(Pattern.quote(contextAfter)).append(")");
        }
        
        return regex.toString();
    }

    // ============================================================
    // 🔑 EXTRACT KEYWORDS
    // ============================================================
    private List<String> extractKeywords(String text, String value) {
        List<String> keywords = new ArrayList<>();
        
        // Extract words around the value
        int index = text.indexOf(value);
        if (index >= 0) {
            String before = text.substring(Math.max(0, index - 100), index);
            String after = text.substring(Math.min(text.length(), index + value.length()), 
                                         Math.min(text.length(), index + value.length() + 100));
            
            // Extract capitalized words (likely brand/model names)
            Pattern wordPattern = Pattern.compile("\\b([A-Z][a-z]+)\\b");
            Matcher matcher = wordPattern.matcher(before + " " + after);
            while (matcher.find() && keywords.size() < 10) {
                String word = matcher.group(1);
                if (word.length() > 2 && !keywords.contains(word)) {
                    keywords.add(word);
                }
            }
        }
        
        return keywords;
    }

    // ============================================================
    // 🎯 GET LEARNED PATTERNS FOR EXTRACTION
    // ============================================================
    public List<OcrLearnedPattern> getActivePatterns(String patternType, Long subCategoryId, Long makeId) {
        List<OcrLearnedPattern> patterns = new ArrayList<>();
        
        // Get patterns specific to make and subcategory
        if (makeId != null && subCategoryId != null) {
            patterns.addAll(patternRepo.findByPatternTypeAndMakeIdAndIsActiveTrue(patternType, makeId));
        }
        
        // Get patterns specific to subcategory
        if (subCategoryId != null) {
            patterns.addAll(patternRepo.findByPatternTypeAndSubCategoryIdAndIsActiveTrue(patternType, subCategoryId));
        }
        
        // Get general patterns
        patterns.addAll(patternRepo.findByPatternTypeAndIsActiveTrue(patternType));
        
        // Remove duplicates and sort by confidence weight
        return patterns.stream()
            .collect(Collectors.toMap(
                OcrLearnedPattern::getPatternId,
                p -> p,
                (p1, p2) -> p1.getConfidenceWeight() > p2.getConfidenceWeight() ? p1 : p2
            ))
            .values()
            .stream()
            .sorted((p1, p2) -> Double.compare(p2.getConfidenceWeight(), p1.getConfidenceWeight()))
            .collect(Collectors.toList());
    }

    // ============================================================
    // 🏋️ TRAIN MODEL (Batch learning from all corrections)
    // ============================================================
    public OcrModelMetadata trainModel(String username) {
        log.info("🏋️ Starting model training...");
        long startTime = System.currentTimeMillis();
        
        List<OcrTrainingData> corrections = trainingDataRepo.findByIsCorrectedTrue();
        log.info("📊 Training on {} correction samples", corrections.size());
        
        if (corrections.isEmpty()) {
            throw new RuntimeException("No training data available. Need at least one correction.");
        }

        // Learn from all corrections
        int learnedCount = 0;
        for (OcrTrainingData correction : corrections) {
            try {
                learnFromCorrection(correction);
                learnedCount++;
            } catch (Exception e) {
                log.warn("⚠️ Failed to learn from correction {}: {}", correction.getTrainingId(), e.getMessage());
            }
        }

        // Calculate metrics
        long correctedSamples = trainingDataRepo.countByIsCorrectedTrue();
        double accuracy = correctedSamples > 0 ? (double) learnedCount / correctedSamples : 0.0;

        // Create model metadata
        OcrModelMetadata model = new OcrModelMetadata();
        model.setModelVersion(generateModelVersion());
        model.setModelType("PATTERN_MATCHING");
        model.setTrainingSamplesCount(corrections.size());
        model.setValidationSamplesCount(0);
        model.setAccuracyScore(accuracy);
        model.setIsActive(true);
        model.setTrainedAt(LocalDateTime.now());
        model.setTrainedBy(username);
        model.setTrainingDurationSeconds((System.currentTimeMillis() - startTime) / 1000);
        model.setCreatedBy(username);
        model.setUpdatedBy(username);

        // Deactivate previous active model
        modelMetadataRepo.findByIsActiveTrue().ifPresent(prevModel -> {
            prevModel.setIsActive(false);
            modelMetadataRepo.save(prevModel);
        });

        OcrModelMetadata saved = modelMetadataRepo.save(model);
        log.info("✅ Model training completed: modelId={}, version={}, accuracy={}", 
                saved.getModelId(), saved.getModelVersion(), accuracy);
        
        return saved;
    }

    // ============================================================
    // 🔢 GENERATE MODEL VERSION
    // ============================================================
    private String generateModelVersion() {
        long count = modelMetadataRepo.count();
        int major = (int) (count / 100) + 1;
        int minor = (int) (count % 100);
        return String.format("%d.%d.0", major, minor);
    }

    // ============================================================
    // 🔐 CALCULATE IMAGE HASH
    // ============================================================
    private String calculateImageHash(byte[] imageBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(imageBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("❌ SHA-256 algorithm not available", e);
            return UUID.randomUUID().toString();
        }
    }
}

