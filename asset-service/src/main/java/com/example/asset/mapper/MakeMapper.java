package com.example.asset.mapper;

import com.example.asset.dto.MakeDto;
import com.example.asset.entity.ProductMake;

/**
 * ✅ MakeMapper
 * Utility class for converting between {@link ProductMake} entities
 * and {@link MakeDto} data transfer objects.
 * <p>
 * Provides bi-directional mapping with null-safety and includes all optional fields.
 */
public final class MakeMapper {

    // Prevent instantiation
    private MakeMapper() {}

    // ============================================================
    // 🔄 ENTITY → DTO
    // ============================================================
    /**
     * Converts a {@link ProductMake} entity to a {@link MakeDto}.
     * Includes all optional fields (subCategoryId, subCategoryName).
     *
     * @param entity the entity to convert
     * @return the corresponding DTO, or {@code null} if input is null
     */
    public static MakeDto toDto(ProductMake entity) {
        if (entity == null) return null;

        MakeDto dto = new MakeDto();
        dto.setMakeId(entity.getMakeId());
        dto.setMakeName(entity.getMakeName());
        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Include optional foreign key fields
        if (entity.getSubCategory() != null) {
            dto.setSubCategoryId(entity.getSubCategory().getSubCategoryId());
            dto.setSubCategoryName(entity.getSubCategory().getSubCategoryName());
        }

        return dto;
    }

    // ============================================================
    // 🔄 DTO → ENTITY
    // ============================================================
    /**
     * Converts a {@link MakeDto} to a {@link ProductMake} entity.
     *
     * @param dto the DTO to convert
     * @return the corresponding entity, or {@code null} if input is null
     */
    public static ProductMake toEntity(MakeDto dto) {
        if (dto == null) return null;

        ProductMake entity = new ProductMake();
        entity.setMakeId(dto.getMakeId());
        entity.setMakeName(trim(dto.getMakeName()));
        entity.setActive(dto.getActive());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        // Note: SubCategory relationship should be set separately via repository lookup

        return entity;
    }

    // ============================================================
    // 🧩 Helper: Trim strings safely
    // ============================================================
    private static String trim(String value) {
        return (value != null) ? value.trim() : null;
    }
}

