
package com.example.asset.repository;

import com.example.asset.entity.AssetWarranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetWarrantyRepository extends JpaRepository<AssetWarranty, Long> {

    // ========================================================================
    // 🔹 EXISTING METHODS (MUST NOT BE REMOVED)
    // ========================================================================

    /**
     * Fetch active warranties (active = true or null).
     */
    List<AssetWarranty> findByActiveTrueOrActiveIsNull();

    /**
     * Check if an active warranty exists for a given asset.
     */
    boolean existsByAsset_AssetIdAndActiveTrue(Long assetId);


    // ========================================================================
    // 🔹 NEW METHODS REQUIRED FOR UNIFIED LINK VALIDATION
    // ========================================================================

    /**
     * Check if warranty exists by ID.
     * Needed for ensureEntityExists("WARRANTY").
     */
    boolean existsByWarrantyId(Long warrantyId);

    /**
     * Fetch warranty by Warranty ID.
     * Used for validating indirect linkage.
     */
    Optional<AssetWarranty> findByWarrantyId(Long warrantyId);

    /**
     * SME Requirement:
     * Determine if a WARRANTY is already assigned to a user.
     *
     * Indirect mapping:
     *
     *   WARRANTY → ASSET → ASSET_USER_LINK
     *
     * ❗NO inline JPQL allowed → Must use repository chaining.
     */
    default boolean existsByWarrantyIdAndUserAssigned(
            Long warrantyId,
            AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo) {

        // 1️⃣ Get Warranty
        Optional<AssetWarranty> warrantyOp = findByWarrantyId(warrantyId);
        if (warrantyOp.isEmpty()) return false;

        Long assetId = warrantyOp.get().getAsset().getAssetId();
        if (assetId == null) return false;

        // 2️⃣ Lookup user link for this asset
        return linkRepo.existsByAssetIdAndActiveTrue(assetId);
    }

    /**
     * This overload MUST NOT be called directly.
     * Exists only for compatibility with ValidationService signatures.
     */
    default boolean existsByWarrantyIdAndUserAssigned(Long warrantyId) {
        throw new UnsupportedOperationException("""
            ❌ Call existsByWarrantyIdAndUserAssigned(warrantyId, assetRepo, linkRepo)
            — Spring Data cannot autowire repositories inside default methods.
        """);
    }
}

