

package com.example.asset.repository;

import com.example.asset.entity.AssetDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetDocumentRepository extends JpaRepository<AssetDocument, Long> {

    // ============================================================
    // 🔹 Existing Methods (DO NOT REMOVE — required for upload logic)
    // ============================================================

    boolean existsById(Long documentId);

    Optional<AssetDocument> findByEntityTypeIgnoreCaseAndEntityIdAndActiveTrue(
            String entityType, Long entityId);

    Optional<AssetDocument> findByEntityTypeIgnoreCaseAndEntityIdAndDocTypeIgnoreCaseAndActiveTrue(
            String entityType, Long entityId, String docType);

    List<AssetDocument> findAllByEntityTypeIgnoreCaseAndEntityIdAndActiveTrue(
            String entityType, Long entityId);

    List<AssetDocument> findAllByEntityTypeIgnoreCaseAndEntityId(
            String entityType, Long entityId);

    Optional<AssetDocument> findTopByEntityTypeIgnoreCaseAndEntityIdOrderByUploadedDateDesc(
            String entityType, Long entityId);

    Optional<AssetDocument> findTopByEntityTypeIgnoreCaseAndEntityIdAndActiveTrueOrderByUploadedDateDesc(
            String entityType, Long entityId);

    List<AssetDocument> findAllByEntityTypeIgnoreCaseAndEntityIdAndActiveFalse(
            String entityType, Long entityId);

    /**
     * Check if an active document already exists for the same entity and doc type.
     * Used to prevent duplicate document entry per (entityType, entityId, docType).
     * Only use when docType is not null/blank.
     */
    boolean existsByEntityTypeIgnoreCaseAndEntityIdAndDocTypeIgnoreCaseAndActiveTrue(
            String entityType, Long entityId, String docType);

    // ============================================================
    // 🔹 Additional Methods Required for Unified Validation System
    // ============================================================

    /**
     * Check if a document exists by document ID.
     * Needed for ensureEntityExists("DOCUMENT").
     */
    default boolean existsByDocumentId(Long documentId) {
        return existsById(documentId);
    }

    /**
     * Fetch document by document ID.
     * Needed for indirect user linkage logic.
     */
    default Optional<AssetDocument> findByDocumentId(Long documentId) {
        return findById(documentId);
    }


    // ============================================================
    // 🔹 SME Requirement: Detect if Document is Linked to Any User
    //
    // RULE:
    //     Document → Asset → AssetUserLink
    //
    // No inline JPQL allowed → must use repository chaining.
    // ============================================================
    default boolean existsByDocumentIdAndUserAssigned(
            Long documentId,
            AssetMasterRepository assetRepo,
            AssetUserLinkRepository linkRepo
    ) {

        Optional<AssetDocument> docOp = findById(documentId);

        if (docOp.isEmpty()) {
            return false;
        }

        // Document → Asset
        AssetDocument doc = docOp.get();
        if (doc.getAsset() == null || doc.getAsset().getAssetId() == null) {
            return false;
        }

        Long assetId = doc.getAsset().getAssetId();

        // Asset → AssetUserLink
        return linkRepo.existsByAssetIdAndActiveTrue(assetId);
    }

    /**
     * This overload is intentionally blocked.
     * MUST use version with repositories injected.
     */
    default boolean existsByDocumentIdAndUserAssigned(Long documentId) {
        throw new UnsupportedOperationException("""
            ❌ Use existsByDocumentIdAndUserAssigned(documentId, assetRepo, linkRepo)
            because default methods cannot auto-inject Spring beans.
        """);
    }
}


