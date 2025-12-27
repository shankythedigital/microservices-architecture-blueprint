
package com.example.asset.mapper;

import com.example.asset.dto.CategoryDto;
import com.example.asset.entity.ProductCategory;

/**
 * ✅ CategoryMapper
 * Utility class for converting between {@link ProductCategory} entities
 * and {@link CategoryDto} data transfer objects.
 * <p>
 * Provides bi-directional mapping with null-safety and optional normalization.
 */
public final class CategoryMapper {

    // Prevent instantiation
    private CategoryMapper() {}

    // ============================================================
    // 🔄 ENTITY → DTO
    // ============================================================
    /**
     * Converts a {@link ProductCategory} entity to a {@link CategoryDto}.
     *
     * @param entity the entity to convert
     * @return the corresponding DTO, or {@code null} if input is null
     */
    public static CategoryDto toDto(ProductCategory entity) {
        if (entity == null) return null;

        CategoryDto dto = new CategoryDto();
        dto.setCategoryId(entity.getCategoryId());
        dto.setCategoryName(entity.getCategoryName());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    // ============================================================
    // 🔄 DTO → ENTITY
    // ============================================================
    /**
     * Converts a {@link CategoryDto} to a {@link ProductCategory} entity.
     *
     * @param dto the DTO to convert
     * @return the corresponding entity, or {@code null} if input is null
     */
    public static ProductCategory toEntity(CategoryDto dto) {
        if (dto == null) return null;

        ProductCategory entity = new ProductCategory();
        entity.setCategoryId(dto.getCategoryId());
        entity.setCategoryName(trim(dto.getCategoryName()));
        entity.setDescription(trim(dto.getDescription()));
        entity.setActive(dto.getActive());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }

    // ============================================================
    // ✏️ PARTIAL UPDATE SUPPORT
    // ============================================================
    /**
     * Copies non-null values from DTO to an existing entity.
     * Useful for PATCH-like or partial updates.
     *
     * @param dto    the source DTO
     * @param entity the target entity to update
     */
    public static void copyNonNullToEntity(CategoryDto dto, ProductCategory entity) {
        if (dto == null || entity == null) return;

        if (dto.getCategoryName() != null)
            entity.setCategoryName(trim(dto.getCategoryName()));
        if (dto.getDescription() != null)
            entity.setDescription(trim(dto.getDescription()));
        if (dto.getActive() != null)
            entity.setActive(dto.getActive());
        if (dto.getUpdatedBy() != null)
            entity.setUpdatedBy(dto.getUpdatedBy());
        if (dto.getUpdatedAt() != null)
            entity.setUpdatedAt(dto.getUpdatedAt());
    }

    // ============================================================
    // 🧩 Helper: Trim strings safely
    // ============================================================
    private static String trim(String value) {
        return (value != null) ? value.trim() : null;
    }
}


