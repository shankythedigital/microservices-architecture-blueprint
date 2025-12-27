#!/usr/bin/env bash
set -euo pipefail

### ====================================================================
###  ASSET MASTER SCHEMA SETUP FOR MYSQL
### ====================================================================
### Creates:
###   - asset_master_schema (MySQL)
###   - All master tables
###   - Staging tables
###   - Inserts into entity_type_master
###   - Generates Java Entities + Repositories for master schema
### ====================================================================

ROOT_DIR="$(pwd)/asset-management-service"
JAVA_SRC="$ROOT_DIR/src/main/java/com/example/asset"
MASTER_ENTITY="$JAVA_SRC/entity/assetmaster"
MASTER_REPO="$JAVA_SRC/repository/master"
DB_DIR="$ROOT_DIR/db"

echo "Creating directories..."
mkdir -p "$MASTER_ENTITY"
mkdir -p "$MASTER_REPO"
mkdir -p "$DB_DIR"

### ====================================================================
### 1. MySQL SCHEMA CREATION
### ====================================================================
cat > "$DB_DIR/schema_asset_master.sql" <<'EOF'
CREATE SCHEMA IF NOT EXISTS asset_master_schema;

USE asset_master_schema;

-- ============================
-- ENTITY TYPE MASTER
-- ============================
CREATE TABLE IF NOT EXISTS entity_type_master (
    entity_type_id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    active TINYINT DEFAULT 1,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NULL
);

-- ============================
-- CATEGORY MASTER
-- ============================
CREATE TABLE IF NOT EXISTS category_master (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    active TINYINT DEFAULT 1,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NULL
);

-- ============================
-- SUB-CATEGORY MASTER
-- ============================
CREATE TABLE IF NOT EXISTS subcategory_master (
    subcategory_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    active TINYINT DEFAULT 1,

    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NULL,

    FOREIGN KEY (category_id) REFERENCES category_master(category_id)
);

-- ============================
-- MAKE MASTER
-- ============================
CREATE TABLE IF NOT EXISTS product_make_master (
    make_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    make_name VARCHAR(255) UNIQUE NOT NULL,
    active TINYINT DEFAULT 1,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NULL
);

-- ============================
-- MODEL MASTER
-- ============================
CREATE TABLE IF NOT EXISTS product_model_master (
    model_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    make_id BIGINT NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    active TINYINT DEFAULT 1,

    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NULL,

    FOREIGN KEY (make_id) REFERENCES product_make_master(make_id)
);

-- ============================
-- OUTLET MASTER
-- ============================
CREATE TABLE IF NOT EXISTS outlet_master (
    outlet_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    outlet_name VARCHAR(255) NOT NULL,
    address TEXT,
    contact VARCHAR(100),
    active TINYINT DEFAULT 1
);

-- ============================
-- VENDOR MASTER
-- ============================
CREATE TABLE IF NOT EXISTS vendor_master (
    vendor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_name VARCHAR(255) NOT NULL,
    address TEXT,
    contact VARCHAR(100),
    active TINYINT DEFAULT 1
);

-- ============================
-- ASSET MASTER
-- ============================
CREATE TABLE IF NOT EXISTS asset_master (
    asset_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_name_udv VARCHAR(255) NOT NULL,
    category_id BIGINT,
    subcategory_id BIGINT,
    make_id BIGINT,
    model_id BIGINT,
    outlet_id BIGINT,
    vendor_id BIGINT,
    asset_status VARCHAR(100),
    active TINYINT DEFAULT 1,

    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NULL,

    FOREIGN KEY (category_id) REFERENCES category_master(category_id),
    FOREIGN KEY (subcategory_id) REFERENCES subcategory_master(subcategory_id),
    FOREIGN KEY (make_id) REFERENCES product_make_master(make_id),
    FOREIGN KEY (model_id) REFERENCES product_model_master(model_id),
    FOREIGN KEY (outlet_id) REFERENCES outlet_master(outlet_id),
    FOREIGN KEY (vendor_id) REFERENCES vendor_master(vendor_id)
);

-- ============================
-- COMPONENT MASTER
-- ============================
CREATE TABLE IF NOT EXISTS component_master (
    component_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT,
    component_name VARCHAR(255),
    serial_no VARCHAR(255),
    active TINYINT DEFAULT 1,

    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NULL,

    FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id)
);

-- ============================
-- STAGING TABLES (for OTHER selections)
-- ============================
CREATE TABLE IF NOT EXISTS make_staging (
    staging_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    make_name VARCHAR(255),
    requested_by VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS model_staging (
    staging_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_name VARCHAR(255),
    make_name VARCHAR(255),
    requested_by VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS asset_staging (
    staging_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_name_udv VARCHAR(255),
    category_name VARCHAR(255),
    subcategory_name VARCHAR(255),
    make_name VARCHAR(255),
    model_name VARCHAR(255),
    requested_by VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS component_staging (
    staging_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    component_name VARCHAR(255),
    asset_name_udv VARCHAR(255),
    serial_no VARCHAR(255),
    requested_by VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING'
);

-- Seed entity types (polymorphic linkage)
INSERT INTO entity_type_master (code, description)
VALUES
 ('ASSET','Asset entity'),
 ('COMPONENT','Component'),
 ('MAKE','Product Make'),
 ('MODEL','Product Model'),
 ('AMC','AMC'),
 ('WARRANTY','Warranty'),
 ('DOCUMENT','Document')
ON DUPLICATE KEY UPDATE code=code;

EOF

echo "Asset master schema created at $DB_DIR/schema_asset_master.sql"

### ====================================================================
### 2. GENERATE MASTER ENTITIES (JAVA)
### ====================================================================

cat > "$MASTER_ENTITY/EntityTypeMaster.java" <<EOF
package com.example.asset.entity.assetmaster;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "entity_type_master", schema = "asset_master_schema")
public class EntityTypeMaster extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer entityTypeId;

    private String code;
    private String description;

    public Integer getEntityTypeId() { return entityTypeId; }
    public void setEntityTypeId(Integer entityTypeId) { this.entityTypeId = entityTypeId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
EOF

cat > "$MASTER_ENTITY/CategoryMaster.java" <<EOF
package com.example.asset.entity.assetmaster;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "category_master", schema = "asset_master_schema")
public class CategoryMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
EOF

cat > "$MASTER_ENTITY/SubCategoryMaster.java" <<EOF
package com.example.asset.entity.assetmaster;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "subcategory_master", schema = "asset_master_schema")
public class SubCategoryMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subcategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryMaster category;

    @Column(nullable = false)
    private String name;

    public Long getSubcategoryId() { return subcategoryId; }
    public void setSubcategoryId(Long subcategoryId) { this.subcategoryId = subcategoryId; }

    public CategoryMaster getCategory() { return category; }
    public void setCategory(CategoryMaster category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
EOF

cat > "$MASTER_ENTITY/ProductMake.java" <<EOF
package com.example.asset.entity.assetmaster;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product_make_master", schema = "asset_master_schema")
public class ProductMake extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long makeId;

    @Column(nullable = false, unique = true)
    private String makeName;

    public Long getMakeId() { return makeId; }
    public void setMakeId(Long makeId) { this.makeId = makeId; }

    public String getMakeName() { return makeName; }
    public void setMakeName(String makeName) { this.makeName = makeName; }
}
EOF

cat > "$MASTER_ENTITY/ProductModel.java" <<EOF
package com.example.asset.entity.assetmaster;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product_model_master", schema = "asset_master_schema")
public class ProductModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "make_id")
    private ProductMake make;

    @Column(nullable = false)
    private String modelName;

    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }

    public ProductMake getMake() { return make; }
    public void setMake(ProductMake make) { this.make = make; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
}
EOF

cat > "$MASTER_ENTITY/AssetMaster.java" <<EOF
package com.example.asset.entity.assetmaster;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "asset_master", schema = "asset_master_schema")
public class AssetMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    @Column(nullable = false)
    private String assetNameUdv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryMaster category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private SubCategoryMaster subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "make_id")
    private ProductMake make;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private ProductModel model;

    private String assetStatus;

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetNameUdv() { return assetNameUdv; }
    public void setAssetNameUdv(String assetNameUdv) { this.assetNameUdv = assetNameUdv; }

    public CategoryMaster getCategory() { return category; }
    public void setCategory(CategoryMaster category) { this.category = category; }

    public SubCategoryMaster getSubCategory() { return subCategory; }
    public void setSubCategory(SubCategoryMaster subCategory) { this.subCategory = subCategory; }

    public ProductMake getMake() { return make; }
    public void setMake(ProductMake make) { this.make = make; }

    public ProductModel getModel() { return model; }
    public void setModel(ProductModel model) { this.model = model; }

    public String getAssetStatus() { return assetStatus; }
    public void setAssetStatus(String assetStatus) { this.assetStatus = assetStatus; }
}
EOF

cat > "$MASTER_ENTITY/ComponentMaster.java" <<EOF
package com.example.asset.entity.assetmaster;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "component_master", schema = "asset_master_schema")
public class ComponentMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long componentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private AssetMaster asset;

    @Column(nullable = false)
    private String componentName;

    private String serialNo;

    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }

    public AssetMaster getAsset() { return asset; }
    public void setAsset(AssetMaster asset) { this.asset = asset; }

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }

    public String getSerialNo() { return serialNo; }
    public void setSerialNo(String serialNo) { this.serialNo = serialNo; }
}
EOF

### ====================================================================
### 3. MASTER REPOSITORIES
### ====================================================================

cat > "$MASTER_REPO/ProductMakeRepository.java" <<EOF
package com.example.asset.repository.master;

import com.example.asset.entity.assetmaster.ProductMake;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMakeRepository extends JpaRepository<ProductMake, Long> {
    boolean existsByMakeNameIgnoreCase(String makeName);
}
EOF

cat > "$MASTER_REPO/ProductModelRepository.java" <<EOF
package com.example.asset.repository.master;

import com.example.asset.entity.assetmaster.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductModelRepository extends JpaRepository<ProductModel, Long> {
    boolean existsByModelNameIgnoreCaseAndMake_MakeId(String modelName, Long makeId);
}
EOF

cat > "$MASTER_REPO/CategoryMasterRepository.java" <<EOF
package com.example.asset.repository.master;

import com.example.asset.entity.assetmaster.CategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryMasterRepository extends JpaRepository<CategoryMaster, Long> {
    boolean existsByNameIgnoreCase(String name);
}
EOF

cat > "$MASTER_REPO/SubCategoryMasterRepository.java" <<EOF
package com.example.asset.repository.master;

import com.example.asset.entity.assetmaster.SubCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubCategoryMasterRepository extends JpaRepository<SubCategoryMaster, Long> { }
EOF

cat > "$MASTER_REPO/AssetMasterRepository.java" <<EOF
package com.example.asset.repository.master;

import com.example.asset.entity.assetmaster.AssetMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetMasterRepository extends JpaRepository<AssetMaster, Long> {
    boolean existsByAssetNameUdv(String assetNameUdv);
}
EOF

cat > "$MASTER_REPO/ComponentMasterRepository.java" <<EOF
package com.example.asset.repository.master;

import com.example.asset.entity.assetmaster.ComponentMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComponentMasterRepository extends JpaRepository<ComponentMaster, Long> { }
EOF

echo "Master schema + entities + repositories GENERATED ✔"
