package com.example.asset.mapper;

import com.example.asset.dto.ComponentDto;
import com.example.asset.entity.AssetComponent;

/**
 * ✅ ComponentMapper
 * Utility class for converting between {@link AssetComponent} entities
 * and {@link ComponentDto} data transfer objects.
 * <p>
 * Provides bi-directional mapping with null-safety and includes all optional fields.
 */
public final class ComponentMapper {

    // Prevent instantiation
    private ComponentMapper() {}

    // ============================================================
    // 🔄 ENTITY → DTO
    // ============================================================
    /**
     * Converts an {@link AssetComponent} entity to a {@link ComponentDto}.
     * Includes all optional fields (description).
     *
     * @param entity the entity to convert
     * @return the corresponding DTO, or {@code null} if input is null
     */
    public static ComponentDto toDto(AssetComponent entity) {
        if (entity == null) return null;

        ComponentDto dto = new ComponentDto();
        dto.setComponentId(entity.getComponentId());
        dto.setComponentName(entity.getComponentName());
        dto.setDescription(entity.getDescription()); // Optional field
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
     * Converts a {@link ComponentDto} to an {@link AssetComponent} entity.
     *
     * @param dto the DTO to convert
     * @return the corresponding entity, or {@code null} if input is null
     */
    public static AssetComponent toEntity(ComponentDto dto) {
        if (dto == null) return null;

        AssetComponent entity = new AssetComponent();
        entity.setComponentId(dto.getComponentId());
        entity.setComponentName(trim(dto.getComponentName()));
        entity.setDescription(trim(dto.getDescription())); // Optional field
        entity.setActive(dto.getActive());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }

    // ============================================================
    // 🧩 Helper: Trim strings safely
    // ============================================================
    private static String trim(String value) {
        return (value != null) ? value.trim() : null;
    }
}

