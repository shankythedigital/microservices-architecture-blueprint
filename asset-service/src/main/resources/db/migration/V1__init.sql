-- V1__init.sql
-- Full schema for asset-service

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
