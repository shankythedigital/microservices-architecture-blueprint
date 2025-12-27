package com.example.asset.mapper;

import com.example.asset.dto.VendorDto;
import com.example.asset.entity.VendorMaster;

/**
 * ✅ VendorMapper
 * Utility class for converting between {@link VendorMaster} entities
 * and {@link VendorDto} data transfer objects.
 * <p>
 * Provides bi-directional mapping with null-safety and includes all optional fields.
 */
public final class VendorMapper {

    // Prevent instantiation
    private VendorMapper() {}

    // ============================================================
    // 🔄 ENTITY → DTO
    // ============================================================
    /**
     * Converts a {@link VendorMaster} entity to a {@link VendorDto}.
     * Includes all optional fields (contactPerson, email, mobile, address).
     *
     * @param entity the entity to convert
     * @return the corresponding DTO, or {@code null} if input is null
     */
    public static VendorDto toDto(VendorMaster entity) {
        if (entity == null) return null;

        VendorDto dto = new VendorDto();
        dto.setVendorId(entity.getVendorId());
        dto.setVendorName(entity.getVendorName());
        dto.setContactPerson(entity.getContactPerson()); // Optional field
        dto.setEmail(entity.getEmail()); // Optional field
        dto.setMobile(entity.getMobile()); // Optional field
        dto.setAddress(entity.getAddress()); // Optional field
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
     * Converts a {@link VendorDto} to a {@link VendorMaster} entity.
     *
     * @param dto the DTO to convert
     * @return the corresponding entity, or {@code null} if input is null
     */
    public static VendorMaster toEntity(VendorDto dto) {
        if (dto == null) return null;

        VendorMaster entity = new VendorMaster();
        entity.setVendorId(dto.getVendorId());
        entity.setVendorName(trim(dto.getVendorName()));
        entity.setContactPerson(trim(dto.getContactPerson())); // Optional field
        entity.setEmail(trim(dto.getEmail())); // Optional field
        entity.setMobile(trim(dto.getMobile())); // Optional field
        entity.setAddress(trim(dto.getAddress())); // Optional field
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

