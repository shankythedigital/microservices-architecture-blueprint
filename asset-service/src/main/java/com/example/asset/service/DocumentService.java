
package com.example.asset.service;

import com.example.asset.dto.BulkDocumentRequest;
import com.example.asset.dto.BulkUploadResponse;
import com.example.asset.dto.DocumentRequest;
import com.example.asset.entity.AssetDocument;
import com.example.asset.entity.AssetMaster;
import com.example.asset.repository.AssetDocumentRepository;
import com.example.asset.repository.AssetMasterRepository;
import com.example.asset.repository.ProductCategoryRepository;
import com.example.asset.repository.ProductSubCategoryRepository;
import com.example.asset.repository.ProductMakeRepository;
import com.example.asset.repository.ProductModelRepository;
import com.example.asset.repository.AssetComponentRepository;
import com.example.asset.repository.PurchaseOutletRepository;
import com.example.asset.repository.VendorRepository;
import com.example.asset.repository.AssetWarrantyRepository;
import com.example.asset.repository.AssetAmcRepository;
import com.example.common.util.FileStorageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final AssetDocumentRepository repo;
    private final AssetMasterRepository assetRepo;
    private final FileStorageUtil fileStorageUtil;
    
    // Repositories for entity validation
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final AssetComponentRepository componentRepo;
    private final PurchaseOutletRepository outletRepo;
    private final VendorRepository vendorRepo;
    private final AssetWarrantyRepository warrantyRepo;
    private final AssetAmcRepository amcRepo;

    public DocumentService(AssetDocumentRepository repo,
                           AssetMasterRepository assetRepo,
                           FileStorageUtil fileStorageUtil,
                           ProductCategoryRepository categoryRepo,
                           ProductSubCategoryRepository subCategoryRepo,
                           ProductMakeRepository makeRepo,
                           ProductModelRepository modelRepo,
                           AssetComponentRepository componentRepo,
                           PurchaseOutletRepository outletRepo,
                           VendorRepository vendorRepo,
                           AssetWarrantyRepository warrantyRepo,
                           AssetAmcRepository amcRepo) {
        this.repo = repo;
        this.assetRepo = assetRepo;
        this.fileStorageUtil = fileStorageUtil;
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.componentRepo = componentRepo;
        this.outletRepo = outletRepo;
        this.vendorRepo = vendorRepo;
        this.warrantyRepo = warrantyRepo;
        this.amcRepo = amcRepo;
    }

    // ============================================================
    // 🟢 UPLOAD DOCUMENT
    // ============================================================
    public AssetDocument upload(HttpHeaders headers, MultipartFile file, DocumentRequest request) {
        log.info("📤 Upload request: entityType={} entityId={}", request.getEntityType(), request.getEntityId());

        try {
            // 1️⃣ Store file on disk
            String filePath = fileStorageUtil.storeFile(file, request.getEntityType());

            // 2️⃣ Create new document entity
            AssetDocument doc = new AssetDocument();
            doc.setFileName(file.getOriginalFilename());
            doc.setFilePath(filePath);
            doc.setEntityType(request.getEntityType());
            doc.setEntityId(request.getEntityId());
            doc.setDocType(request.getDocType());
            doc.setUserId(request.getUserId());
            doc.setUsername(request.getUsername());
            doc.setProjectType(request.getProjectType());
            doc.setUploadedDate(LocalDateTime.now());
            doc.setActive(true);
            doc.setCreatedBy(request.getUsername());
            doc.setCreatedAt(LocalDateTime.now());
            doc.setUpdatedAt(LocalDateTime.now());

            // 3️⃣ Handle linking and previous deactivation
            linkDocumentToEntity(doc, request);

            // 4️⃣ Save new document
            AssetDocument saved = repo.save(doc);
            log.info("✅ Document uploaded successfully (ID={}, entityType={}, entityId={})",
                    saved.getDocumentId(), request.getEntityType(), request.getEntityId());
            return saved;

        } catch (Exception e) {
            log.error("❌ Failed to upload document for entityType={} ID={}: {}", 
                      request.getEntityType(), request.getEntityId(), e.getMessage(), e);
            throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 🔍 FIND DOCUMENT BY ID
    // ============================================================
    @Transactional(readOnly = true)
    public AssetDocument findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("❌ Document not found for ID: " + id));
    }

    // ============================================================
    // ❌ SOFT DELETE DOCUMENT
    // ============================================================
    public void softDelete(HttpHeaders headers, Long id, DocumentRequest request) {
        AssetDocument doc = findById(id);
        doc.setActive(false);
        doc.setUpdatedBy(request.getUsername());
        doc.setUpdatedAt(LocalDateTime.now());
        repo.save(doc);
        log.info("🗑️ Soft-deleted document ID={} by user={}", id, request.getUsername());
    }

    // ============================================================
    // 🔗 LINK DOCUMENT TO ENTITY (Deactivate older active)
    // ============================================================
    private void linkDocumentToEntity(AssetDocument doc, DocumentRequest request) {
        String type = request.getEntityType().toUpperCase();
        Long id = request.getEntityId();

        log.info("🔗 Linking document to entityType={} entityId={}", type, id);

        // 1️⃣ Find active existing doc
        Optional<AssetDocument> existingOpt = repo.findByEntityTypeIgnoreCaseAndEntityIdAndActiveTrue(type, id);

        if (existingOpt.isPresent()) {
            AssetDocument existing = existingOpt.get();
            existing.setActive(false);
            existing.setUpdatedBy(request.getUsername());
            existing.setUpdatedAt(LocalDateTime.now());
            repo.save(existing);
            log.info("🗑️ Deactivated previous document ID={} for {} ID={}", existing.getDocumentId(), type, id);
        }

        // 2️⃣ Link to actual entity (if exists)
        switch (type) {
            case "ASSET" -> {
                AssetMaster asset = assetRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("❌ Asset not found for ID: " + id));
                doc.setAsset(asset);
            }
            case "COMPONENT", "AMC", "WARRANTY", "CATEGORY", "SUBCATEGORY", "MAKE", "MODEL", "OUTLET", "VENDOR" -> {
                doc.setEntityType(type);
                doc.setEntityId(id);
            }
            default -> throw new IllegalArgumentException("❌ Unsupported entityType: " + type);
        }

        doc.setUpdatedAt(LocalDateTime.now());
        doc.setUpdatedBy(request.getUsername());
    }

    // ============================================================
    // 📦 BULK UPLOAD DOCUMENTS (NEW - using BulkDocumentRequest)
    // ============================================================
    @Transactional
    public BulkUploadResponse<AssetDocument> bulkCreate(HttpHeaders headers, BulkDocumentRequest bulkRequest) {
        BulkUploadResponse<AssetDocument> response = new BulkUploadResponse<>();
        
        if (bulkRequest == null || bulkRequest.getDocuments() == null) {
            throw new IllegalArgumentException("Bulk request cannot be null");
        }

        List<BulkDocumentRequest.SimpleDocumentDto> items = bulkRequest.getDocuments();
        response.setTotalCount(items.size());

        String username = bulkRequest.getUsername();
        Long userId = bulkRequest.getUserId();
        String projectType = Optional.ofNullable(bulkRequest.getProjectType()).orElse("ASSET_SERVICE");

        for (int i = 0; i < items.size(); i++) {
            try {
                BulkDocumentRequest.SimpleDocumentDto item = items.get(i);

                // ✅ VALIDATION: Required fields
                if (item.getEntityType() == null || item.getEntityType().trim().isEmpty()) {
                    response.addFailure(i, "Entity type is required");
                    continue;
                }

                if (item.getEntityId() == null) {
                    response.addFailure(i, "Entity ID is required");
                    continue;
                }

                if (item.getFileName() == null || item.getFileName().trim().isEmpty()) {
                    response.addFailure(i, "File name is required");
                    continue;
                }

                if (item.getFilePath() == null || item.getFilePath().trim().isEmpty()) {
                    response.addFailure(i, "File path is required");
                    continue;
                }

                String entityType = item.getEntityType().trim().toUpperCase();
                Long entityId = item.getEntityId();
                String fileName = item.getFileName().trim();
                String filePath = item.getFilePath().trim();

                // ✅ VALIDATION: File path exists
                Path path = Paths.get(filePath);
                if (!Files.exists(path)) {
                    response.addFailure(i, "File does not exist at path: " + filePath);
                    continue;
                }

                // ✅ VALIDATION: Entity exists and is active
                if (!validateEntityExists(entityType, entityId)) {
                    response.addFailure(i, String.format("Entity not found or inactive: %s with ID %d", entityType, entityId));
                    continue;
                }

                // ✅ VALIDATION: File name length (max 255 chars per entity constraint)
                if (fileName.length() > 255) {
                    response.addFailure(i, "File name exceeds maximum length of 255 characters");
                    continue;
                }

                // ✅ VALIDATION: File path length (max 500 chars per entity constraint)
                if (filePath.length() > 500) {
                    response.addFailure(i, "File path exceeds maximum length of 500 characters");
                    continue;
                }

                // CREATE: Primary key is auto-generated
                // Directly save entity without calling upload() to avoid per-item notifications
                AssetDocument doc = new AssetDocument();
                doc.setEntityType(entityType);
                doc.setEntityId(entityId);
                doc.setFileName(fileName);
                doc.setFilePath(filePath);
                doc.setDocType(item.getDocType() != null ? item.getDocType().trim() : null);
                doc.setUserId(userId);
                doc.setUsername(username);
                doc.setProjectType(projectType);
                doc.setUploadedDate(LocalDateTime.now());
                doc.setActive(true);
                doc.setCreatedBy(username);
                doc.setCreatedAt(LocalDateTime.now());
                doc.setUpdatedAt(LocalDateTime.now());
                doc.setUpdatedBy(username);

                // Link document to entity
                linkDocumentToEntityBulk(doc, entityType, entityId, username);

                AssetDocument created = repo.save(doc);
                
                response.addSuccess(i, created);
                log.debug("✅ Created document fileName={} for {} ID={}", fileName, entityType, entityId);

            } catch (Exception e) {
                log.error("❌ Bulk document failed at index {}: {}", i, e.getMessage());
                response.addFailure(i, e.getMessage());
            }
        }

        log.info("📦 Bulk document upload: {}/{} success",
                response.getSuccessCount(), response.getTotalCount());
        return response;
    }

    // ============================================================
    // 🔍 VALIDATE ENTITY EXISTS
    // ============================================================
    private boolean validateEntityExists(String entityType, Long entityId) {
        try {
            switch (entityType.toUpperCase()) {
                case "ASSET" -> {
                    return assetRepo.findById(entityId)
                            .filter(e -> e.getActive() == null || e.getActive())
                            .isPresent();
                }
                case "COMPONENT" -> {
                    return componentRepo.findById(entityId)
                            .filter(e -> e.getActive() == null || e.getActive())
                            .isPresent();
                }
                case "CATEGORY" -> {
                    return categoryRepo.findById(entityId)
                            .filter(e -> e.getActive() == null || e.getActive())
                            .isPresent();
                }
                case "SUBCATEGORY" -> {
                    return subCategoryRepo.findById(entityId)
                            .filter(e -> e.getActive() == null || e.getActive())
                            .isPresent();
                }
                case "MAKE" -> {
                    return makeRepo.findById(entityId)
                            .filter(e -> e.getActive() == null || e.getActive())
                            .isPresent();
                }
                case "MODEL" -> {
                    return modelRepo.findById(entityId)
                            .filter(e -> e.getActive() == null || e.getActive())
                            .isPresent();
                }
                case "OUTLET" -> {
                    return outletRepo.findById(entityId)
                            .filter(e -> e.getActive() == null || e.getActive())
                            .isPresent();
                }
                case "VENDOR" -> {
                    return vendorRepo.findById(entityId)
                            .filter(e -> e.getActive() == null || e.getActive())
                            .isPresent();
                }
                case "WARRANTY" -> {
                    return warrantyRepo.findById(entityId)
                            .filter(e -> e.getActive() == null || e.getActive())
                            .isPresent();
                }
                case "AMC" -> {
                    return amcRepo.findById(entityId)
                            .filter(e -> e.getActive() == null || e.getActive())
                            .isPresent();
                }
                default -> {
                    log.warn("⚠️ Unknown entity type: {}", entityType);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("❌ Error validating entity {} ID {}: {}", entityType, entityId, e.getMessage());
            return false;
        }
    }

    // ============================================================
    // 🔗 LINK DOCUMENT TO ENTITY (Bulk version - without deactivating existing)
    // ============================================================
    private void linkDocumentToEntityBulk(AssetDocument doc, String entityType, Long entityId, String username) {
        String type = entityType.toUpperCase();

        log.debug("🔗 Linking document to entityType={} entityId={}", type, entityId);

        // Link to actual entity (if exists)
        switch (type) {
            case "ASSET" -> {
                AssetMaster asset = assetRepo.findById(entityId)
                        .orElse(null);
                if (asset != null) {
                    doc.setAsset(asset);
                }
            }
            case "COMPONENT", "AMC", "WARRANTY", "CATEGORY", "SUBCATEGORY", "MAKE", "MODEL", "OUTLET", "VENDOR" -> {
                doc.setEntityType(type);
                doc.setEntityId(entityId);
            }
            default -> throw new IllegalArgumentException("❌ Unsupported entityType: " + type);
        }

        doc.setUpdatedAt(LocalDateTime.now());
        doc.setUpdatedBy(username);
    }
}

