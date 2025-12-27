
package com.example.asset.mapper;

import com.example.asset.dto.AssetAmcDto;
import com.example.asset.entity.AssetAmc;

/**
 * ✅ AssetAmcMapper
 * Converts between AssetAmc entity and DTO.
 */
public class AssetAmcMapper {

    public static AssetAmcDto toDto(AssetAmc entity) {
        if (entity == null) return null;

        AssetAmcDto dto = new AssetAmcDto();
        dto.setAmcId(entity.getAmcId());
        dto.setAmcStatus(entity.getAmcStatus());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setActive(entity.getActive());
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setComponentId(entity.getComponentId());

        if (entity.getAsset() != null)
            dto.setAssetId(entity.getAsset().getAssetId());

        if (entity.getDocument() != null)
            dto.setDocumentId(entity.getDocument().getDocumentId());

        return dto;
    }
}


