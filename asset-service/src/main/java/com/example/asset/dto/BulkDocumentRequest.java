package com.example.asset.dto;

import java.util.List;

/**
 * ✅ BulkDocumentRequest DTO
 * Request wrapper for bulk document upload operations.
 * Supports both JSON bulk upload and Excel file parsing.
 * 
 * <p>Documents can be linked to various entities:
 * ASSET, COMPONENT, AMC, WARRANTY, CATEGORY, SUBCATEGORY, MAKE, MODEL, OUTLET, VENDOR</p>
 */
public class BulkDocumentRequest {

    private Long userId;
    private String username;
    private String projectType;

    private List<SimpleDocumentDto> documents;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public List<SimpleDocumentDto> getDocuments() { return documents; }
    public void setDocuments(List<SimpleDocumentDto> documents) { this.documents = documents; }

    /**
     * ✅ SimpleDocumentDto
     * Simplified DTO for bulk document operations.
     * Contains document metadata and entity linkage information.
     */
    public static class SimpleDocumentDto {
        private Long documentId; // Primary key from Excel (optional)
        
        // Entity linkage (required)
        private String entityType; // ASSET, COMPONENT, AMC, WARRANTY, CATEGORY, SUBCATEGORY, MAKE, MODEL, OUTLET, VENDOR
        private Long entityId; // ID of the entity this document is linked to
        
        // Document metadata
        private String fileName; // File name (required)
        private String filePath; // File path (required - can be relative or absolute)
        private String docType; // Document type (optional - e.g., IMAGE, PDF, RECEIPT, AGREEMENT)

        // Getters and Setters
        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }

        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }

        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public String getDocType() { return docType; }
        public void setDocType(String docType) { this.docType = docType; }
    }
}

