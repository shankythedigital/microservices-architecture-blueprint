
package com.example.asset.mapper;

import com.example.asset.dto.AssetWarrantyDto;
import com.example.asset.entity.AssetWarranty;
import com.example.asset.entity.AssetDocument;
import com.example.asset.entity.AssetMaster;

/**
 * ✅ AssetWarrantyMapper
 * Handles mapping between AssetWarranty entity and DTO.
 */
public class AssetWarrantyMapper {

    // ============================================================
    // 🔁 ENTITY → DTO
    // ============================================================
    public static AssetWarrantyDto toDto(AssetWarranty entity) {
        if (entity == null) return null;

        AssetWarrantyDto dto = new AssetWarrantyDto();

        dto.setWarrantyId(entity.getWarrantyId());
        dto.setWarrantyStatus(entity.getWarrantyStatus());
        dto.setWarrantyProvider(entity.getWarrantyProvider());
        dto.setWarrantyTerms(entity.getWarrantyTerms());
        dto.setStartDate(entity.getWarrantyStartDate());
        dto.setEndDate(entity.getWarrantyEndDate());
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setComponentId(entity.getComponentId());

        // ✅ Include assetId if asset is linked
        if (entity.getAsset() != null) {
            dto.setAssetId(entity.getAsset().getAssetId());
        }

        // ✅ Include documentId safely (even if lazy)
        if (entity.getDocument() != null) {
            try {
                dto.setDocumentId(entity.getDocument().getDocumentId());
            } catch (Exception e) {
                dto.setDocumentId(null);
            }
        } else if (entity.getDocumentId() != null) {
            dto.setDocumentId(entity.getDocumentId());
        }

        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        // dto.setCreatedAt(entity.getCreatedAt());
        // dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    // ============================================================
    // 🔁 DTO → ENTITY
    // ============================================================
    public static AssetWarranty toEntity(AssetWarrantyDto dto) {
        if (dto == null) return null;

        AssetWarranty entity = new AssetWarranty();

        entity.setWarrantyId(dto.getWarrantyId());
        entity.setWarrantyStatus(dto.getWarrantyStatus());
        entity.setWarrantyProvider(dto.getWarrantyProvider());
        entity.setWarrantyTerms(dto.getWarrantyTerms());
        entity.setWarrantyStartDate(dto.getStartDate());
        entity.setWarrantyEndDate(dto.getEndDate());
        entity.setUserId(dto.getUserId());
        entity.setUsername(dto.getUsername());
        entity.setComponentId(dto.getComponentId());
        entity.setActive(dto.getActive());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());

        // ✅ Map asset if assetId is provided
        if (dto.getAssetId() != null) {
            AssetMaster asset = new AssetMaster();
            asset.setAssetId(dto.getAssetId());
            entity.setAsset(asset);
        }

        // ✅ Map document if documentId is provided
        if (dto.getDocumentId() != null) {
            AssetDocument doc = new AssetDocument();
            doc.setDocumentId(dto.getDocumentId());
            entity.setDocument(doc);
        }

        return entity;
    }
}


