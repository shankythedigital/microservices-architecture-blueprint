
package com.example.asset.repository;

import com.example.asset.entity.ProductModel;
import com.example.asset.entity.AssetMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductModelRepository extends JpaRepository<ProductModel, Long> {

    /**
     * Unique name validation (existing)
     */
    boolean existsByModelNameIgnoreCaseAndMake_MakeId(String modelName, Long makeId);

    /**
     * Derived: find assets under this model
     */
    List<AssetMaster> findByModelId(Long modelId);

    /**
     * Find models by make ID
     */
    List<ProductModel> findByMake_MakeId(Long makeId);

    /**
     * Find model by name and make ID (case-insensitive)
     */
    Optional<ProductModel> findByModelNameIgnoreCaseAndMake_MakeId(String modelName, Long makeId);

    /**
     * SME Validation: Validate if user already linked to ANY asset in this model
     */
    default boolean userLinked(
            Long modelId,
            AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo) {

        // 1️⃣ Fetch all assets belonging to this model
        List<AssetMaster> assets = assetRepo.findByModel_ModelId(modelId);

        if (assets.isEmpty()) return false;

        // 2️⃣ Check if any asset is linked
        return assets.stream()
                .anyMatch(asset ->
                        linkRepo.existsByAssetIdAndActiveTrue(asset.getAssetId())
                );
    }
}

