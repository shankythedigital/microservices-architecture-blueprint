package com.example.authservice.service;

import com.example.authservice.model.TermsAndConditions;
import com.example.authservice.repository.TermsAndConditionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing Terms and Conditions
 */
@Service
public class TermsAndConditionsService {

    private static final Logger log = LoggerFactory.getLogger(TermsAndConditionsService.class);

    @Autowired
    private TermsAndConditionsRepository tcRepository;

    /**
     * Get active Terms and Conditions for a project type
     * Falls back to global T&C if project-specific not found
     * 
     * @param projectType Project type code (e.g., "ASSET_SERVICE")
     * @param language Language code (e.g., "en", "hi") - optional
     * @return TermsAndConditions or null if not found
     */
    public TermsAndConditions getActiveTermsAndConditions(String projectType, String language) {
        log.info("📄 Fetching active T&C for projectType: {}, language: {}", projectType, language);

        // Try to find project-specific T&C with language
        if (language != null && !language.isBlank()) {
            Optional<TermsAndConditions> projectSpecific = tcRepository
                    .findByProjectTypeAndLanguageAndIsActiveTrue(projectType, language);
            if (projectSpecific.isPresent()) {
                log.info("✅ Found project-specific T&C with language: {}", language);
                return projectSpecific.get();
            }
        }

        // Try to find project-specific T&C (any language)
        Optional<TermsAndConditions> projectSpecific = tcRepository
                .findByProjectTypeAndIsActiveTrue(projectType);
        if (projectSpecific.isPresent()) {
            log.info("✅ Found project-specific T&C");
            return projectSpecific.get();
        }

        // Fallback to global T&C with language
        if (language != null && !language.isBlank()) {
            Optional<TermsAndConditions> global = tcRepository
                    .findByProjectTypeIsNullAndLanguageAndIsActiveTrue(language);
            if (global.isPresent()) {
                log.info("✅ Found global T&C with language: {}", language);
                return global.get();
            }
        }

        // Fallback to global T&C (default)
        Optional<TermsAndConditions> global = tcRepository
                .findByProjectTypeIsNullAndIsActiveTrue();
        if (global.isPresent()) {
            log.info("✅ Found global T&C");
            return global.get();
        }

        log.warn("⚠️ No active T&C found for projectType: {}, language: {}", projectType, language);
        return null;
    }

    /**
     * Get Terms and Conditions by version
     * 
     * @param projectType Project type code
     * @param version Version number
     * @return TermsAndConditions or null if not found
     */
    public TermsAndConditions getTermsAndConditionsByVersion(String projectType, String version) {
        log.info("📄 Fetching T&C for projectType: {}, version: {}", projectType, version);
        return tcRepository.findByProjectTypeAndVersion(projectType, version)
                .orElse(null);
    }

    /**
     * Get all versions of Terms and Conditions for a project type
     * 
     * @param projectType Project type code
     * @return List of TermsAndConditions
     */
    public List<TermsAndConditions> getAllVersions(String projectType) {
        log.info("📄 Fetching all T&C versions for projectType: {}", projectType);
        return tcRepository.findByProjectTypeOrderByVersionDesc(projectType);
    }

    /**
     * Create or update Terms and Conditions
     * 
     * @param tc TermsAndConditions entity
     * @return Saved TermsAndConditions
     */
    @Transactional
    public TermsAndConditions saveTermsAndConditions(TermsAndConditions tc) {
        log.info("💾 Saving T&C: projectType={}, version={}, title={}", 
                tc.getProjectType(), tc.getVersion(), tc.getTitle());

        // Set effective date if not provided
        if (tc.getEffectiveDate() == null) {
            tc.setEffectiveDate(LocalDate.now());
        }

        // Set createdBy if not set
        if (tc.getCreatedBy() == null || tc.getCreatedBy().isBlank()) {
            tc.setCreatedBy("system");
        }

        TermsAndConditions saved = tcRepository.save(tc);
        log.info("✅ T&C saved successfully: tcId={}", saved.getTcId());
        return saved;
    }

    /**
     * Deactivate a Terms and Conditions version
     * 
     * @param tcId T&C ID
     * @return Updated TermsAndConditions
     */
    @Transactional
    public TermsAndConditions deactivateTermsAndConditions(Long tcId) {
        log.info("🔒 Deactivating T&C: tcId={}", tcId);
        TermsAndConditions tc = tcRepository.findById(tcId)
                .orElseThrow(() -> new IllegalArgumentException("T&C not found with ID: " + tcId));
        tc.setIsActive(false);
        tc.setUpdatedBy("system");
        return tcRepository.save(tc);
    }

    /**
     * Activate a Terms and Conditions version
     * (Deactivates other versions for the same project type)
     * 
     * @param tcId T&C ID
     * @return Updated TermsAndConditions
     */
    @Transactional
    public TermsAndConditions activateTermsAndConditions(Long tcId) {
        log.info("✅ Activating T&C: tcId={}", tcId);
        TermsAndConditions tc = tcRepository.findById(tcId)
                .orElseThrow(() -> new IllegalArgumentException("T&C not found with ID: " + tcId));

        // Deactivate other versions for the same project type
        List<TermsAndConditions> otherVersions = tcRepository
                .findByProjectTypeOrderByVersionDesc(tc.getProjectType());
        for (TermsAndConditions other : otherVersions) {
            if (!other.getTcId().equals(tcId)) {
                other.setIsActive(false);
                other.setUpdatedBy("system");
                tcRepository.save(other);
            }
        }

        // Activate this version
        tc.setIsActive(true);
        tc.setUpdatedBy("system");
        return tcRepository.save(tc);
    }
}

