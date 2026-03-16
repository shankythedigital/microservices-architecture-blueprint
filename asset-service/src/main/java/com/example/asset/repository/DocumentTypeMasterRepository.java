package com.example.asset.repository;

import com.example.asset.entity.DocumentTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ✅ DocumentTypeMasterRepository
 * Repository for DocumentTypeMaster entity.
 * Provides methods to query document types by code and active status.
 */
@Repository
public interface DocumentTypeMasterRepository extends JpaRepository<DocumentTypeMaster, Long> {

    /**
     * Find document type by code (case-insensitive)
     */
    Optional<DocumentTypeMaster> findByCodeIgnoreCase(String code);

    /**
     * Check if document type exists by code (case-insensitive)
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Find all active document types
     */
    List<DocumentTypeMaster> findAllByActiveTrue();

    /**
     * Find document type by code and active status
     */
    Optional<DocumentTypeMaster> findByCodeIgnoreCaseAndActiveTrue(String code);
}
