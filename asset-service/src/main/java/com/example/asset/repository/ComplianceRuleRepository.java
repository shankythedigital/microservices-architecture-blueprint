package com.example.asset.repository;

import com.example.asset.entity.ComplianceRule;
import com.example.asset.enums.ComplianceRuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ✅ ComplianceRuleRepository
 * Repository for ComplianceRule entity.
 */
@Repository
public interface ComplianceRuleRepository extends JpaRepository<ComplianceRule, Long> {

    /**
     * Find rule by code and entity type
     */
    Optional<ComplianceRule> findByRuleCodeIgnoreCaseAndEntityTypeIgnoreCase(String ruleCode, String entityType);

    /**
     * Find all active rules
     */
    List<ComplianceRule> findAllByActiveTrue();

    /**
     * Find all rules for a specific entity type
     */
    List<ComplianceRule> findAllByEntityTypeIgnoreCase(String entityType);

    /**
     * Find all active rules for a specific entity type
     */
    List<ComplianceRule> findAllByEntityTypeIgnoreCaseAndActiveTrue(String entityType);

    /**
     * Find all rules by type
     */
    List<ComplianceRule> findAllByRuleType(ComplianceRuleType ruleType);

    /**
     * Find all active rules by type
     */
    List<ComplianceRule> findAllByRuleTypeAndActiveTrue(ComplianceRuleType ruleType);

    /**
     * Find rules that block operations
     */
    List<ComplianceRule> findAllByBlocksOperationTrueAndActiveTrue();

    /**
     * Find rules ordered by priority (ascending)
     */
    List<ComplianceRule> findAllByEntityTypeIgnoreCaseAndActiveTrueOrderByPriorityAsc(String entityType);
}
