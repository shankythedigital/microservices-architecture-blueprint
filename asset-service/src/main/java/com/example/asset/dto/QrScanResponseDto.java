package com.example.asset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ✅ QrScanResponseDto
 * Response DTO for universal QR scan.
 * Contains entityType and the entity in its native JSON format.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrScanResponseDto {

    private String entityType;  // category, subcategory, make, model, component, warranty, amc, outlet, vendor
    private Object entity;      // The actual entity in its respective JSON format

    public QrScanResponseDto() {}

    public QrScanResponseDto(String entityType, Object entity) {
        this.entityType = entityType;
        this.entity = entity;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }
}
