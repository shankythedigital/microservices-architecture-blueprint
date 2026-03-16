package com.example.asset.entity;

import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ✅ DocumentTypeMaster Entity
 * Master table for document type values used across the asset management system.
 * Stores allowed file extension types (pdf, doc, docx, jpg, png, etc.) with descriptions.
 * Extends BaseEntity for audit fields: createdBy, createdAt, updatedBy, updatedAt, active.
 */
@Entity
@Table(
    name = "document_type_master",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"code"})
    }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentTypeMaster extends BaseEntity {

    // ============================================================
    // 🔑 Primary Key
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_type_id")
    private Long documentTypeId;

    // ============================================================
    // 📦 Core Fields
    // ============================================================
    /**
     * Unique code for the document type (e.g., pdf, doc, docx, jpg)
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Human-readable description of the document type
     */
    @Column(name = "description", length = 255)
    private String description;

    // ============================================================
    // 🧾 Constructors
    // ============================================================
    public DocumentTypeMaster() {
    }

    public DocumentTypeMaster(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // ============================================================
    // 🧾 Getters and Setters
    // ============================================================
    public Long getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Long documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ============================================================
    // 🧠 toString
    // ============================================================
    @Override
    public String toString() {
        return "DocumentTypeMaster{" +
                "documentTypeId=" + documentTypeId +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", active=" + getActive() +
                '}';
    }
}
