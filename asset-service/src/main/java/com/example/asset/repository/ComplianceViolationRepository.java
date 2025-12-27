package com.example.asset.repository;

import com.example.asset.entity.ComplianceViolation;
import com.example.asset.entity.ComplianceSeverityMaster;
import com.example.asset.entity.ComplianceStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ✅ ComplianceViolationRepository
 * Repository for ComplianceViolation entity.
 */
@Repository
public interface ComplianceViolationRepository extends JpaRepository<ComplianceViolation, Long> {

    /**
     * Find violations by entity type and ID
     */
    List<ComplianceViolation> findAllByEntityTypeIgnoreCaseAndEntityId(String entityType, Long entityId);

    /**
     * Find active violations by entity type and ID
     */
    List<ComplianceViolation> findAllByEntityTypeIgnoreCaseAndEntityIdAndActiveTrue(String entityType, Long entityId);

    /**
     * Find unresolved violations (status is not resolved)
     */
    List<ComplianceViolation> findAllByStatus_IsResolvedFalseAndActiveTrue();

    /**
     * Find violations by status
     */
    List<ComplianceViolation> findAllByStatusAndActiveTrue(ComplianceStatusMaster status);

    /**
     * Find violations by severity
     */
    List<ComplianceViolation> findAllBySeverityAndActiveTrue(ComplianceSeverityMaster severity);

    /**
     * Find critical violations
     */
    List<ComplianceViolation> findAllBySeverityAndStatus_IsResolvedFalseAndActiveTrue(
            ComplianceSeverityMaster severity);

    /**
     * Find violations detected within a time range
     */
    List<ComplianceViolation> findAllByDetectedAtBetweenAndActiveTrue(
            LocalDateTime start, LocalDateTime end);

    /**
     * Count violations by entity
     */
    long countByEntityTypeIgnoreCaseAndEntityIdAndActiveTrue(String entityType, Long entityId);

    /**
     * Check if entity has any unresolved violations
     */
    boolean existsByEntityTypeIgnoreCaseAndEntityIdAndStatus_IsResolvedFalseAndActiveTrue(
            String entityType, Long entityId);
    
    /**
     * Find violations by entity type, ID, that are not resolved
     */
    List<ComplianceViolation> findAllByEntityTypeIgnoreCaseAndEntityIdAndStatus_IsResolvedFalseAndActiveTrue(
            String entityType, Long entityId);
}

