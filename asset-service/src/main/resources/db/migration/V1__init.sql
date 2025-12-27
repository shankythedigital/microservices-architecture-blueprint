-- V1__init.sql
-- Full schema for asset-service

-- ============================
-- ENTITY TYPE MASTER
-- ============================
CREATE TABLE IF NOT EXISTS entity_type_master (
  entity_type_id INT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

-- ============================
-- STATUS MASTER
-- ============================
CREATE TABLE IF NOT EXISTS status_master (
  status_id INT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255),
  category VARCHAR(50) NOT NULL,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS product_category (
  category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  category_name VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS product_sub_category (
  sub_category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  category_id BIGINT,
  sub_category_name VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (category_id) REFERENCES product_category(category_id)
);

CREATE TABLE IF NOT EXISTS product_make (
  make_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sub_category_id BIGINT,
  make_name VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (sub_category_id) REFERENCES product_sub_category(sub_category_id)
);

CREATE TABLE IF NOT EXISTS product_model (
  model_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  make_id BIGINT,
  model_name VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (make_id) REFERENCES product_make(make_id)
);

CREATE TABLE IF NOT EXISTS purchase_outlet (
  outlet_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  outlet_name VARCHAR(255),
  outlet_address TEXT,
  contact_info VARCHAR(255),
  outlet_type VARCHAR(50),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);


CREATE TABLE IF NOT EXISTS asset_master (
  asset_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  asset_name_udv VARCHAR(255) NOT NULL,
  category_id BIGINT,
  sub_category_id BIGINT,
  make_id BIGINT,
  model_id BIGINT,
  make_udv VARCHAR(255),
  model_udv VARCHAR(255),
  purchase_mode VARCHAR(50),
  purchase_outlet_id BIGINT,
  purchase_outlet_udv VARCHAR(255),
  purchase_outlet_address_udv TEXT,
  purchase_date DATE,
  asset_status VARCHAR(50),
  sold_on_date DATE,
  sales_channel_name VARCHAR(255),
  created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (category_id) REFERENCES product_category(category_id),
  FOREIGN KEY (sub_category_id) REFERENCES product_sub_category(sub_category_id),
  FOREIGN KEY (make_id) REFERENCES product_make(make_id),
  FOREIGN KEY (model_id) REFERENCES product_model(model_id),
  FOREIGN KEY (purchase_outlet_id) REFERENCES purchase_outlet(outlet_id)
);


CREATE TABLE IF NOT EXISTS asset_component_master (
  component_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  component_name VARCHAR(255),
  description TEXT,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS asset_component_link (
  asset_id BIGINT,
  component_id BIGINT,
  PRIMARY KEY (asset_id, component_id),
  FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id),
  FOREIGN KEY (component_id) REFERENCES asset_component_master(component_id)
);


CREATE TABLE IF NOT EXISTS asset_user_link (
  link_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  asset_id BIGINT,
  user_id VARCHAR(100) NOT NULL,
  username VARCHAR(255),
  assigned_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id)
);

CREATE TABLE IF NOT EXISTS asset_warranty (
  warranty_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  asset_id BIGINT,
  component_id BIGINT,
  warranty_type VARCHAR(50),
  start_date DATE,
  end_date DATE,
  document_path VARCHAR(512),
  user_id VARCHAR(100),
  username VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id)
);

CREATE TABLE IF NOT EXISTS asset_amc (
  amc_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  asset_id BIGINT,
  component_id BIGINT,
  start_date DATE,
  end_date DATE,
  amc_status VARCHAR(50),
  document_path VARCHAR(512),
  user_id VARCHAR(100),
  username VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id)
);

CREATE TABLE IF NOT EXISTS asset_document (
  document_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  asset_id BIGINT,
  component_id BIGINT,
  doc_type VARCHAR(255),
  file_path VARCHAR(512),
  uploaded_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  user_id VARCHAR(100),
  username VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id)
);

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ip_address VARCHAR(100),
  user_agent VARCHAR(1000),
  url VARCHAR(1000),
  http_method VARCHAR(20),
  username VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

-- ============================
-- COMPLIANCE RULE TYPE MASTER
-- ============================
CREATE TABLE IF NOT EXISTS compliance_rule_type_master (
  rule_type_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(1000),
  category VARCHAR(50),
  priority INT DEFAULT 100,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

-- ============================
-- COMPLIANCE SEVERITY MASTER
-- ============================
CREATE TABLE IF NOT EXISTS compliance_severity_master (
  severity_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  level INT NOT NULL,
  blocks_operation BOOLEAN DEFAULT FALSE,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

-- ============================
-- COMPLIANCE STATUS MASTER
-- ============================
CREATE TABLE IF NOT EXISTS compliance_status_master (
  status_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  is_resolved BOOLEAN DEFAULT FALSE,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

-- ============================
-- COMPLIANCE RULE
-- ============================
CREATE TABLE IF NOT EXISTS compliance_rule (
  rule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rule_code VARCHAR(100) NOT NULL,
  rule_name VARCHAR(255) NOT NULL,
  description VARCHAR(1000),
  entity_type VARCHAR(50) NOT NULL,
  rule_type_id BIGINT NOT NULL,
  severity_id BIGINT NOT NULL,
  rule_expression TEXT,
  error_message VARCHAR(500),
  blocks_operation BOOLEAN DEFAULT FALSE,
  priority INT DEFAULT 100,
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  UNIQUE KEY uk_rule_code_entity_type (rule_code, entity_type),
  FOREIGN KEY (rule_type_id) REFERENCES compliance_rule_type_master(rule_type_id),
  FOREIGN KEY (severity_id) REFERENCES compliance_severity_master(severity_id)
);

-- ============================
-- COMPLIANCE VIOLATION
-- ============================
CREATE TABLE IF NOT EXISTS compliance_violation (
  violation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rule_id BIGINT NOT NULL,
  entity_type VARCHAR(50) NOT NULL,
  entity_id BIGINT NOT NULL,
  severity_id BIGINT NOT NULL,
  status_id BIGINT NOT NULL,
  violation_message VARCHAR(1000),
  violated_field VARCHAR(100),
  expected_value VARCHAR(500),
  actual_value VARCHAR(500),
  detected_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at DATETIME,
  resolved_by VARCHAR(255),
  resolution_notes VARCHAR(1000),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (rule_id) REFERENCES compliance_rule(rule_id),
  FOREIGN KEY (severity_id) REFERENCES compliance_severity_master(severity_id),
  FOREIGN KEY (status_id) REFERENCES compliance_status_master(status_id),
  INDEX idx_entity_type_id (entity_type, entity_id),
  INDEX idx_status_id (status_id),
  INDEX idx_severity_id (severity_id),
  INDEX idx_detected_at (detected_at)
);
