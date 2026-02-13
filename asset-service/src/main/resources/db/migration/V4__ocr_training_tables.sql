-- V4__ocr_training_tables.sql
-- OCR Training and Learning Tables
-- These tables store training data and learned patterns without modifying existing table structures

-- ============================
-- OCR TRAINING DATA
-- ============================
CREATE TABLE IF NOT EXISTS ocr_training_data (
  training_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  image_hash VARCHAR(64),
  original_ocr_text TEXT,
  extracted_make VARCHAR(255),
  extracted_model VARCHAR(255),
  extracted_serial VARCHAR(255),
  corrected_make VARCHAR(255),
  corrected_model VARCHAR(255),
  corrected_serial VARCHAR(255),
  confidence_score DOUBLE,
  user_id BIGINT,
  username VARCHAR(255),
  is_corrected BOOLEAN DEFAULT FALSE,
  sub_category_id BIGINT,
  make_id BIGINT,
  model_id BIGINT,
  extraction_pattern TEXT,
  image_features TEXT,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  INDEX idx_image_hash (image_hash),
  INDEX idx_user_id (user_id),
  INDEX idx_sub_category_id (sub_category_id),
  INDEX idx_make_id (make_id),
  INDEX idx_model_id (model_id),
  INDEX idx_is_corrected (is_corrected),
  INDEX idx_created_at (created_at)
);

-- ============================
-- OCR LEARNED PATTERNS
-- ============================
CREATE TABLE IF NOT EXISTS ocr_learned_pattern (
  pattern_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  pattern_type VARCHAR(50) NOT NULL,
  pattern_regex TEXT,
  pattern_keywords TEXT,
  context_before VARCHAR(100),
  context_after VARCHAR(100),
  confidence_weight DOUBLE DEFAULT 1.0,
  usage_count INT DEFAULT 0,
  success_count INT DEFAULT 0,
  sub_category_id BIGINT,
  make_id BIGINT,
  is_active BOOLEAN DEFAULT TRUE,
  last_used_at DATETIME,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  INDEX idx_pattern_type (pattern_type),
  INDEX idx_sub_category_id (sub_category_id),
  INDEX idx_make_id (make_id),
  INDEX idx_is_active (is_active),
  INDEX idx_confidence_weight (confidence_weight)
);

-- ============================
-- OCR MODEL METADATA
-- ============================
CREATE TABLE IF NOT EXISTS ocr_model_metadata (
  model_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  model_version VARCHAR(50) NOT NULL,
  model_type VARCHAR(50) NOT NULL,
  model_parameters TEXT,
  training_samples_count INT DEFAULT 0,
  validation_samples_count INT DEFAULT 0,
  accuracy_score DOUBLE,
  precision_score DOUBLE,
  recall_score DOUBLE,
  f1_score DOUBLE,
  is_active BOOLEAN DEFAULT FALSE,
  trained_at DATETIME,
  trained_by VARCHAR(255),
  training_duration_seconds BIGINT,
  model_file_path VARCHAR(512),
  notes TEXT,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  INDEX idx_model_version (model_version),
  INDEX idx_model_type (model_type),
  INDEX idx_is_active (is_active),
  INDEX idx_trained_at (trained_at)
);

