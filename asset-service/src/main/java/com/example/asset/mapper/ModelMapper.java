
package com.example.asset.mapper;

import com.example.asset.dto.ModelDto;
import com.example.asset.entity.ProductModel;

/**
 * ✅ ModelMapper
 * Converts between ProductModel entity and ModelDto.
 */
public class ModelMapper {

    public static ModelDto toDto(ProductModel entity) {
        if (entity == null) return null;

        ModelDto dto = new ModelDto();
        dto.setModelId(entity.getModelId());
        dto.setModelName(entity.getModelName());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getMake() != null) {
            dto.setMakeId(entity.getMake().getMakeId());
            dto.setMakeName(entity.getMake().getMakeName());
        }

        return dto;
    }
}

