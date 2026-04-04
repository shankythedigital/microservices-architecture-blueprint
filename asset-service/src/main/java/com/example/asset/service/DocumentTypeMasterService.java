package com.example.asset.service;

import com.example.asset.entity.DocumentTypeMaster;
import com.example.asset.repository.DocumentTypeMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * ✅ DocumentTypeMasterService
 * Service for managing document type master values.
 * Validates document types against the document_type_master table (DB-backed).
 */
@Service
public class DocumentTypeMasterService {

    private static final Logger log = LoggerFactory.getLogger(DocumentTypeMasterService.class);

    /**
     * Logical doc type for user appliance photos (distinct from file-format codes like jpeg/png).
     * Valid even before {@code document_type_master} row exists; migration V9/V10 still adds the row.
     */
    public static final String CODE_ASSET_PHOTO = "asset_photo";

    private static final Set<String> BUILTIN_ACTIVE_CODES = Set.of(CODE_ASSET_PHOTO);

    private final DocumentTypeMasterRepository repository;

    public DocumentTypeMasterService(DocumentTypeMasterRepository repository) {
        this.repository = repository;
    }

    // ============================================================
    // ✅ VALIDATION (replaces DocumentTypeValidator)
    // ============================================================

    /**
     * Validates that the given docType exists and is active in document_type_master (case-insensitive).
     */
    public boolean isValid(String docType) {
        if (docType == null || docType.isBlank()) {
            return false;
        }
        String n = normalize(docType);
        if (BUILTIN_ACTIVE_CODES.contains(n)) {
            return true;
        }
        return repository.findByCodeIgnoreCaseAndActiveTrue(n).isPresent();
    }

    /**
     * Validates docType and throws IllegalArgumentException if invalid.
     */
    public void validate(String docType) {
        if (docType == null || docType.isBlank()) {
            throw new IllegalArgumentException("❌ Document type (docType) is required");
        }
        if (!isValid(docType)) {
            Set<String> allowed = getAllowedTypes();
            throw new IllegalArgumentException(
                    "❌ Invalid document type: '" + docType + "'. Allowed types: " + String.join(", ", allowed));
        }
    }

    /**
     * Returns the normalized (lowercase) docType for storage.
     */
    public String normalize(String docType) {
        return docType != null ? docType.trim().toLowerCase() : null;
    }

    /**
     * Returns the set of allowed document type codes (active only).
     */
    public Set<String> getAllowedTypes() {
        Set<String> codes = repository.findAllByActiveTrue().stream()
                .map(DocumentTypeMaster::getCode)
                .collect(Collectors.toCollection(TreeSet::new));
        codes.addAll(BUILTIN_ACTIVE_CODES);
        return codes;
    }

    // ============================================================
    // 📋 LIST ALL DOCUMENT TYPES
    // ============================================================
    public List<DocumentTypeMaster> listAll() {
        List<DocumentTypeMaster> list = repository.findAll();
        log.debug("Listed {} document types", list.size());
        return list;
    }

    // ============================================================
    // 📋 LIST ACTIVE DOCUMENT TYPES
    // ============================================================
    public List<DocumentTypeMaster> listActive() {
        return repository.findAllByActiveTrue();
    }

    // ============================================================
    // 🔍 FIND BY CODE
    // ============================================================
    public Optional<DocumentTypeMaster> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return repository.findByCodeIgnoreCase(code.trim());
    }

    // ============================================================
    // 🔍 FIND BY ID
    // ============================================================
    public Optional<DocumentTypeMaster> findById(Long id) {
        return id != null ? repository.findById(id) : Optional.empty();
    }

    // ============================================================
    // ✅ VALIDATE (check if code exists in DB)
    // ============================================================
    public boolean existsByCode(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return repository.existsByCodeIgnoreCase(code.trim());
    }

    // ============================================================
    // ✅ VALIDATE ACTIVE (check if code exists and is active)
    // ============================================================
    public boolean existsByCodeAndActive(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return repository.findByCodeIgnoreCaseAndActiveTrue(code.trim()).isPresent();
    }
}
