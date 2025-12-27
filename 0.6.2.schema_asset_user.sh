#!/usr/bin/env bash
set -euo pipefail

### ====================================================================
###  ASSET USER SCHEMA SETUP FOR MYSQL
### ====================================================================

ROOT_DIR="$(pwd)/asset-management-service"
JAVA_SRC="$ROOT_DIR/src/main/java/com/example/asset"
USER_ENTITY="$JAVA_SRC/entity/user"
USER_REPO="$JAVA_SRC/repository/user"
DB_DIR="$ROOT_DIR/db"

echo "Creating directories..."
mkdir -p "$USER_ENTITY"
mkdir -p "$USER_REPO"
mkdir -p "$DB_DIR"

### ====================================================================
### 1. MySQL asset_user_schema
### ====================================================================

cat > "$DB_DIR/schema_asset_user.sql" <<'EOF'
CREATE SCHEMA IF NOT EXISTS asset_user_schema;

USE asset_user_schema;

-- ===============================
-- ASSET USER LINK
-- ===============================
CREATE TABLE IF NOT EXISTS asset_user_link (
    link_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT,
    component_id BIGINT,
    user_id BIGINT NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(150),
    mobile VARCHAR(50),
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unassigned_date TIMESTAMP NULL,
    active TINYINT DEFAULT 1,

    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NULL,

    FOREIGN KEY (asset_id) REFERENCES asset_master_schema.asset_master(asset_id),
    FOREIGN KEY (component_id) REFERENCES asset_master_schema.component_master(component_id)
);

-- ===============================
-- ASSET AMC
-- ===============================
CREATE TABLE IF NOT EXISTS asset_amc (
    amc_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    amc_provider VARCHAR(255),
    start_date DATE,
    end_date DATE,
    active TINYINT DEFAULT 1,

    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (asset_id) REFERENCES asset_master_schema.asset_master(asset_id)
);

-- ===============================
-- ASSET WARRANTY
-- ===============================
CREATE TABLE IF NOT EXISTS asset_warranty (
    warranty_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    vendor VARCHAR(255),
    warranty_start DATE,
    warranty_end DATE,
    active TINYINT DEFAULT 1,

    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (asset_id) REFERENCES asset_master_schema.asset_master(asset_id)
);

-- ===============================
-- ASSET DOCUMENT
-- ===============================
CREATE TABLE IF NOT EXISTS asset_document (
    document_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type_id INT NOT NULL,
    entity_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    uploaded_by VARCHAR(255),
    uploaded_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active TINYINT DEFAULT 1,

    FOREIGN KEY (entity_type_id) REFERENCES asset_master_schema.entity_type_master(entity_type_id)
);
EOF

echo "Asset USER schema created ✔"

### ====================================================================
### 2. USER ENTITIES (JAVA)
### ====================================================================

cat > "$USER_ENTITY/AssetUserLink.java" <<EOF
package com.example.asset.entity.user;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_user_link", schema = "asset_user_schema")
public class AssetUserLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkId;

    private Long assetId;
    private Long componentId;

    private Long userId;
    private String username;
    private String email;
    private String mobile;

    private LocalDateTime assignedDate;
    private LocalDateTime unassignedDate;

    public Long getLinkId() { return linkId; }
    public void setLinkId(Long linkId) { this.linkId = linkId; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public LocalDateTime getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }

    public LocalDateTime getUnassignedDate() { return unassignedDate; }
    public void setUnassignedDate(LocalDateTime unassignedDate) { this.unassignedDate = unassignedDate; }
}
EOF

cat > "$USER_ENTITY/AssetAmc.java" <<EOF
package com.example.asset.entity.user;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
...

@Entity
@Table(name = "asset_amc", schema = "asset_user_schema")
public class AssetAmc extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long amcId;

    private Long assetId;
    private String amcProvider;
    private LocalDate startDate;
    private LocalDate endDate;
}
EOF

cat > "$USER_ENTITY/AssetWarranty.java" <<EOF
package com.example.asset.entity.user;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "asset_warranty", schema = "asset_user_schema")
public class AssetWarranty extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long warrantyId;

    private Long assetId;
    private String vendor;
    private LocalDate warrantyStart;
    private LocalDate warrantyEnd;
}
EOF

cat > "$USER_ENTITY/AssetDocument.java" <<EOF
package com.example.asset.entity.user;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "asset_document", schema = "asset_user_schema")
public class AssetDocument extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    private Integer entityTypeId;
    private Long entityId;
    private String fileName;
    private String filePath;
    private String uploadedBy;
}
EOF

### ====================================================================
### 3. USER REPOSITORIES
### ====================================================================

cat > "$USER_REPO/AssetUserLinkRepository.java" <<EOF
package com.example.asset.repository.user;

import com.example.asset.entity.user.AssetUserLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AssetUserLinkRepository extends JpaRepository<AssetUserLink, Long> {

    List<AssetUserLink> findByActiveTrue();
    List<AssetUserLink> findByUserIdAndActiveTrue(Long userId);

    boolean existsByAssetIdAndActiveTrue(Long assetId);
    boolean existsByComponentIdAndActiveTrue(Long componentId);

    Optional<AssetUserLink> findByAssetIdAndUserIdAndActiveTrue(Long assetId, Long userId);
    Optional<AssetUserLink> findByComponentIdAndUserIdAndActiveTrue(Long componentId, Long userId);

    Optional<AssetUserLink> findFirstByAssetId(Long assetId);
    Optional<AssetUserLink> findFirstByComponentId(Long componentId);

    default List<AssetUserLink> findBySubCategoryId(Long subCategoryId) {
        return findByActiveTrue();
    }
}
EOF

cat > "$USER_REPO/AssetAmcRepository.java" <<EOF
package com.example.asset.repository.user;

import com.example.asset.entity.user.AssetAmc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetAmcRepository extends JpaRepository<AssetAmc, Long> {
    boolean existsByAssetIdAndActiveTrue(Long assetId);
}
EOF

cat > "$USER_REPO/AssetWarrantyRepository.java" <<EOF
package com.example.asset.repository.user;

import com.example.asset.entity.user.AssetWarranty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetWarrantyRepository extends JpaRepository<AssetWarranty, Long> {
    boolean existsByAssetIdAndActiveTrue(Long assetId);
}
EOF

cat > "$USER_REPO/AssetDocumentRepository.java" <<EOF
package com.example.asset.repository.user;

import com.example.asset.entity.user.AssetDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AssetDocumentRepository extends JpaRepository<AssetDocument, Long> {

    Optional<AssetDocument> findByEntityTypeIdAndEntityIdAndActiveTrue(Integer entityTypeId, Long entityId);

    List<AssetDocument> findAllByEntityTypeIdAndEntityIdAndActiveTrue(Integer entityTypeId, Long entityId);
}
EOF

echo "User schema + entities + repositories generated ✔"
