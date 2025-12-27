package com.example.asset.repository;

import com.example.asset.entity.EntityTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * ✅ EntityTypeMasterRepository
 * Repository for EntityTypeMaster entity.
 * Provides methods to query entity types by code, active status, etc.
 */
@Repository
public interface EntityTypeMasterRepository extends JpaRepository<EntityTypeMaster, Integer> {

    /**
     * Find entity type by code (case-insensitive)
     */
    Optional<EntityTypeMaster> findByCodeIgnoreCase(String code);

    /**
     * Check if entity type exists by code (case-insensitive)
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Find all active entity types
     */
    List<EntityTypeMaster> findAllByActiveTrue();

    /**
     * Find entity type by code and active status
     */
    Optional<EntityTypeMaster> findByCodeIgnoreCaseAndActiveTrue(String code);
}

