package com.example.authservice.repository;

import com.example.authservice.model.TermsAndConditions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TermsAndConditions entity
 */
@Repository
public interface TermsAndConditionsRepository extends JpaRepository<TermsAndConditions, Long> {

    /**
     * Find active T&C by project type
     */
    Optional<TermsAndConditions> findByProjectTypeAndIsActiveTrue(String projectType);

    /**
     * Find T&C by project type and version
     */
    Optional<TermsAndConditions> findByProjectTypeAndVersion(String projectType, String version);

    /**
     * Find all active T&C for a project type
     */
    List<TermsAndConditions> findByProjectTypeAndIsActiveTrueOrderByDisplayOrderAsc(String projectType);

    /**
     * Find active T&C by project type and language
     */
    Optional<TermsAndConditions> findByProjectTypeAndLanguageAndIsActiveTrue(String projectType, String language);

    /**
     * Find all T&C for a project type (all versions)
     */
    List<TermsAndConditions> findByProjectTypeOrderByVersionDesc(String projectType);

    /**
     * Find global/default active T&C (where projectType is null)
     */
    Optional<TermsAndConditions> findByProjectTypeIsNullAndIsActiveTrue();

    /**
     * Find global/default active T&C by language
     */
    Optional<TermsAndConditions> findByProjectTypeIsNullAndLanguageAndIsActiveTrue(String language);

    /**
     * Check if T&C exists for project type and version
     */
    boolean existsByProjectTypeAndVersion(String projectType, String version);
}

