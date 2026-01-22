
package com.example.asset.repository;

import com.example.asset.entity.AssetMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetMasterRepository extends JpaRepository<AssetMaster, Long> {

    // =============================================================
    // 🔍 Existing Validations (Do Not Remove)
    // =============================================================
    boolean existsByAssetNameUdv(String assetNameUdv);

    Optional<AssetMaster> findByAssetNameUdvIgnoreCase(String assetNameUdv);

    // =============================================================
    // 🔍 SME: Derived Methods for Relationship Traversal
    //     No inline JPQL, only Spring Data conventions
    // =============================================================

    /**
     * MODEL → ASSET mapping
     * Fetch all assets under a specific modelId.
     */
    List<AssetMaster> findByModel_ModelId(Long modelId);

    /**
     * MAKE → MODEL → ASSET mapping
     * Fetch all assets under a given makeId through the model relationship.
     */
    List<AssetMaster> findByModel_Make_MakeId(Long makeId);

    /**
     * CATEGORY → ASSET mapping
     * (Useful for future validation or subcategory fetch)
     */
    List<AssetMaster> findByCategory_CategoryId(Long categoryId);

    /**
     * SUBCATEGORY → ASSET mapping
     * Used already in UserLinkService.getUsersBySubCategory()
     */
    List<AssetMaster> findBySubCategory_SubCategoryId(Long subCategoryId);

    /**
     * COMPONENT → ASSET mapping (reverse lookup)
     * Fetches all assets containing a given component ID.
     *
     * Required for COMPONENT → LINKAGE validation.
     * 
     * This relies on:
     * AssetMaster.components  (ManyToMany)
     */
    List<AssetMaster> findByComponents_ComponentId(Long componentId);

    /**
     * WARRANTY → ASSET mapping
     * Used for unified validation (WARRANTY linked to asset)
     */
    Optional<AssetMaster> findByWarranty_WarrantyId(Long warrantyId);

    /**
     * AMC → ASSET mapping
     */
    Optional<AssetMaster> findByAmc_AmcId(Long amcId);

    /**
     * DOCUMENT → ASSET mapping
     * For DOCUMENT linkage validation
     */
    Optional<AssetMaster> findByDocuments_DocumentId(Long documentId);

    // =============================================================
    // 🔍 SCANNING METHODS (QR Code / Barcode)
    // =============================================================
    
    /**
     * Find asset by serial number (case-insensitive)
     * Used for QR/barcode scanning
     */
    Optional<AssetMaster> findBySerialNumberIgnoreCase(String serialNumber);
}


