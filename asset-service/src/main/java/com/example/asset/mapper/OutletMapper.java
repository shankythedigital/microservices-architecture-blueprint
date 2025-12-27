package com.example.asset.mapper;

import com.example.asset.dto.OutletDto;
import com.example.asset.entity.PurchaseOutlet;

/**
 * ✅ OutletMapper
 * Utility class for converting between {@link PurchaseOutlet} entities
 * and {@link OutletDto} data transfer objects.
 * <p>
 * Provides bi-directional mapping with null-safety and includes all optional fields.
 */
public final class OutletMapper {

    // Prevent instantiation
    private OutletMapper() {}

    // ============================================================
    // 🔄 ENTITY → DTO
    // ============================================================
    /**
     * Converts a {@link PurchaseOutlet} entity to an {@link OutletDto}.
     * Includes all optional fields (outletAddress, contactInfo, vendorId, vendorName).
     *
     * @param entity the entity to convert
     * @return the corresponding DTO, or {@code null} if input is null
     */
    public static OutletDto toDto(PurchaseOutlet entity) {
        if (entity == null) return null;

        OutletDto dto = new OutletDto();
        dto.setOutletId(entity.getOutletId());
        dto.setOutletName(entity.getOutletName());
        dto.setOutletAddress(entity.getOutletAddress()); // Optional field
        dto.setContactInfo(entity.getContactInfo()); // Optional field
        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Include optional foreign key fields
        if (entity.getVendor() != null) {
            dto.setVendorId(entity.getVendor().getVendorId());
            dto.setVendorName(entity.getVendor().getVendorName());
        }

        return dto;
    }

    // ============================================================
    // 🔄 DTO → ENTITY
    // ============================================================
    /**
     * Converts an {@link OutletDto} to a {@link PurchaseOutlet} entity.
     *
     * @param dto the DTO to convert
     * @return the corresponding entity, or {@code null} if input is null
     */
    public static PurchaseOutlet toEntity(OutletDto dto) {
        if (dto == null) return null;

        PurchaseOutlet entity = new PurchaseOutlet();
        entity.setOutletId(dto.getOutletId());
        entity.setOutletName(trim(dto.getOutletName()));
        entity.setOutletAddress(trim(dto.getOutletAddress())); // Optional field
        entity.setContactInfo(trim(dto.getContactInfo())); // Optional field
        entity.setActive(dto.getActive());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        // Note: Vendor relationship should be set separately via repository lookup

        return entity;
    }

    // ============================================================
    // 🧩 Helper: Trim strings safely
    // ============================================================
    private static String trim(String value) {
        return (value != null) ? value.trim() : null;
    }
}

