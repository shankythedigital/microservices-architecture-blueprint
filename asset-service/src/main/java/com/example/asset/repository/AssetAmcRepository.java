

package com.example.asset.repository;

import com.example.asset.entity.AssetAmc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetAmcRepository extends JpaRepository<AssetAmc, Long> {

    // ========================================================================
    // 🔹 EXISTING METHODS (Required by other services)
    // ========================================================================

    /**
     * Fetch all AMC records where active = true or null.
     */
    List<AssetAmc> findByActiveTrueOrActiveIsNull();

    /**
     * Check if an AMC is active for the given Asset.
     */
    boolean existsByAsset_AssetIdAndActiveTrue(Long assetId);


    // ========================================================================
    // 🔹 NEW METHODS REQUIRED FOR UNIFIED LINK VALIDATION
    // ========================================================================

    /**
     * Check if an AMC exists by ID.
     * Used by ValidationService ensureEntityExists()
     */
    boolean existsByAmcId(Long amcId);

    /**
     * Fetch AMC by AMC ID.
     * Required for validation of indirect linkage.
     */
    Optional<AssetAmc> findByAmcId(Long amcId);

    /**
     * SME Requirement:
     * Determine if AMC is already assigned to a user.
     *
     * There is NO direct user-AMC table.
     * So we check using AssetUserLink:
     *   AMC → Asset → AssetUserLink
     *
     * NO JPQL ALLOWED → Use default method.
     */
    default boolean existsByAmcIdAndUserAssigned(
            Long amcId,
            AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo) {

        // 1️⃣ Get AMC record
        Optional<AssetAmc> amcOp = findByAmcId(amcId);
        if (amcOp.isEmpty()) return false;

        Long assetId = amcOp.get().getAsset().getAssetId();
        if (assetId == null) return false;

        // 2️⃣ Check if asset is linked to any user
        return linkRepo.existsByAssetIdAndActiveTrue(assetId);
    }

    /**
     * SME convenience method used in ValidationServiceImpl:
     * Uses the default method above with injected repos.
     */
    default boolean existsByAmcIdAndUserAssigned(Long amcId) {
        throw new UnsupportedOperationException("""
            ❌ You must call existsByAmcIdAndUserAssigned(amcId, assetRepo, linkRepo)
            because Spring cannot inject repositories into a default interface method.
        """);
    }
}


