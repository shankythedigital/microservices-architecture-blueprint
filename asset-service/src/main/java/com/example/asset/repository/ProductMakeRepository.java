
package com.example.asset.repository;

import com.example.asset.entity.ProductMake;
import com.example.asset.entity.AssetMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMakeRepository extends JpaRepository<ProductMake, Long> {

    /**
     * Spring Data: get all models under makeId
     */
    List<ProductMake> findByMakeId(Long makeId);

    /**
     * Find make by name (case-insensitive)
     */
    Optional<ProductMake> findByMakeNameIgnoreCase(String makeName);

    /**
     * Find makes by subcategory ID
     */
    List<ProductMake> findBySubCategory_SubCategoryId(Long subCategoryId);

    /**
     * Find make by name and subcategory (case-insensitive)
     */
    Optional<ProductMake> findByMakeNameIgnoreCaseAndSubCategory(String makeName, com.example.asset.entity.ProductSubCategory subCategory);
    
    /**
     * Check if make exists by name and subcategory ID (case-insensitive)
     * Used for efficient duplicate detection per subcategory
     */
    boolean existsByMakeNameIgnoreCaseAndSubCategory_SubCategoryId(String makeName, Long subCategoryId);

    /**
     * Default SME method: check if ANY asset under THIS MAKE
     * is linked to ANY active user.
     *
     * No JPQL, uses existing repositories.
     */
    default boolean userLinked(
            Long makeId,
            AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo) {

        // 1️⃣ Fetch all assets belonging to models under this makeId
        List<AssetMaster> assets = assetRepo.findByModel_Make_MakeId(makeId);

        if (assets.isEmpty()) return false;

        // 2️⃣ Check if ANY of these assets are linked to a user
        for (AssetMaster asset : assets) {
            boolean linked = linkRepo.existsByAssetIdAndActiveTrue(asset.getAssetId());
            if (linked) return true;
        }

        return false;
    }
}


