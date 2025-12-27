package com.example.asset.repository;

import com.example.asset.entity.StatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ✅ StatusMasterRepository
 * Repository for StatusMaster entity.
 * Provides methods to query statuses by code, category, active status, etc.
 */
@Repository
public interface StatusMasterRepository extends JpaRepository<StatusMaster, Integer> {

    /**
     * Find status by code (case-insensitive)
     */
    Optional<StatusMaster> findByCodeIgnoreCase(String code);

    /**
     * Check if status exists by code (case-insensitive)
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Find all active statuses
     */
    List<StatusMaster> findAllByActiveTrue();

    /**
     * Find status by code and active status
     */
    Optional<StatusMaster> findByCodeIgnoreCaseAndActiveTrue(String code);

    /**
     * Find all statuses by category
     */
    List<StatusMaster> findAllByCategoryIgnoreCase(String category);

    /**
     * Find all active statuses by category
     */
    List<StatusMaster> findAllByCategoryIgnoreCaseAndActiveTrue(String category);
}
