package com.example.asset.repository;

import com.example.asset.entity.AssetUserLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface AssetUserLinkRepository extends JpaRepository<AssetUserLink, Long> {

    // ACTIVE link lookup
    List<AssetUserLink> findByActiveTrue();

    List<AssetUserLink> findByAssetIdAndActiveTrue(Long assetId);

    List<AssetUserLink> findByComponentIdAndActiveTrue(Long componentId);

    boolean existsByAssetIdAndActiveTrue(Long assetId);

    boolean existsByComponentIdAndActiveTrue(Long componentId);

    Optional<AssetUserLink> findByAssetIdAndUserIdAndActiveTrue(Long assetId, Long userId);

    Optional<AssetUserLink> findByComponentIdAndUserIdAndActiveTrue(Long componentId, Long userId);

    List<AssetUserLink> findByUserIdAndActiveTrue(Long userId);

    Optional<AssetUserLink> findFirstByAssetId(Long assetId);

    Optional<AssetUserLink> findFirstByComponentId(Long componentId);

    boolean existsByAssetIdAndUserIdAndActiveTrue(Long assetId, Long userId);

    boolean existsByComponentIdAndUserIdAndActiveTrue(Long componentId, Long userId);
    
    // History queries (all links, including inactive)
    List<AssetUserLink> findByAssetId(Long assetId);
    
    List<AssetUserLink> findByUserId(Long userId);

    // =====================================================
    // 🔥 MOST IMPORTANT: Preserve existing method signature
    //    but return ALL ACTIVE LINKS (filtering in service)
    // =====================================================
    default List<AssetUserLink> findBySubCategoryId(Long subCategoryId) {
        return findByActiveTrue();
    }
}


