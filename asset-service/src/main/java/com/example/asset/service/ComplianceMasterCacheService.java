package com.example.asset.service;

import com.example.asset.entity.*;
import com.example.asset.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ✅ ComplianceMasterCacheService
 * Caching service for compliance master data (rule types, severity, status).
 * Provides fast lookup by code with caching support.
 */
@Service
public class ComplianceMasterCacheService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceMasterCacheService.class);

    private final ComplianceRuleTypeMasterRepository ruleTypeRepo;
    private final ComplianceSeverityMasterRepository severityRepo;
    private final ComplianceStatusMasterRepository statusRepo;

    // In-memory cache maps (backup if Spring Cache is not configured)
    private final Map<String, ComplianceRuleTypeMaster> ruleTypeCache = new HashMap<>();
    private final Map<String, ComplianceSeverityMaster> severityCache = new HashMap<>();
    private final Map<String, ComplianceStatusMaster> statusCache = new HashMap<>();

    public ComplianceMasterCacheService(
            ComplianceRuleTypeMasterRepository ruleTypeRepo,
            ComplianceSeverityMasterRepository severityRepo,
            ComplianceStatusMasterRepository statusRepo) {
        this.ruleTypeRepo = ruleTypeRepo;
        this.severityRepo = severityRepo;
        this.statusRepo = statusRepo;
    }

    @PostConstruct
    public void initializeCache() {
        log.info("🔄 Initializing compliance master data cache...");
        refreshCache();
        log.info("✅ Compliance master data cache initialized");
    }

    // ============================================================
    // 🔄 REFRESH CACHE
    // ============================================================
    @CacheEvict(value = {"ruleTypes", "severities", "statuses"}, allEntries = true)
    public void refreshCache() {
        ruleTypeCache.clear();
        severityCache.clear();
        statusCache.clear();

        // Load all active master data
        List<ComplianceRuleTypeMaster> ruleTypes = ruleTypeRepo.findAllByActiveTrue();
        List<ComplianceSeverityMaster> severities = severityRepo.findAllByActiveTrue();
        List<ComplianceStatusMaster> statuses = statusRepo.findAllByActiveTrue();

        // Populate cache maps
        ruleTypes.forEach(rt -> ruleTypeCache.put(rt.getCode().toUpperCase(), rt));
        severities.forEach(s -> severityCache.put(s.getCode().toUpperCase(), s));
        statuses.forEach(st -> statusCache.put(st.getCode().toUpperCase(), st));

        log.info("📦 Cached {} rule types, {} severities, {} statuses",
                ruleTypeCache.size(), severityCache.size(), statusCache.size());
    }

    // ============================================================
    // 🔍 RULE TYPE LOOKUP
    // ============================================================
    @Cacheable(value = "ruleTypes", key = "#code.toUpperCase()")
    public ComplianceRuleTypeMaster getRuleTypeByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        // Try cache first
        ComplianceRuleTypeMaster cached = ruleTypeCache.get(code.toUpperCase());
        if (cached != null) {
            return cached;
        }

        // Fallback to database
        Optional<ComplianceRuleTypeMaster> found = ruleTypeRepo.findByCodeIgnoreCase(code);
        if (found.isPresent()) {
            ComplianceRuleTypeMaster ruleType = found.get();
            ruleTypeCache.put(code.toUpperCase(), ruleType);
            return ruleType;
        }

        log.warn("⚠️ Rule type not found: {}", code);
        return null;
    }

    // ============================================================
    // 🔍 SEVERITY LOOKUP
    // ============================================================
    @Cacheable(value = "severities", key = "#code.toUpperCase()")
    public ComplianceSeverityMaster getSeverityByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        // Try cache first
        ComplianceSeverityMaster cached = severityCache.get(code.toUpperCase());
        if (cached != null) {
            return cached;
        }

        // Fallback to database
        Optional<ComplianceSeverityMaster> found = severityRepo.findByCodeIgnoreCase(code);
        if (found.isPresent()) {
            ComplianceSeverityMaster severity = found.get();
            severityCache.put(code.toUpperCase(), severity);
            return severity;
        }

        log.warn("⚠️ Severity not found: {}", code);
        return null;
    }

    // ============================================================
    // 🔍 STATUS LOOKUP
    // ============================================================
    @Cacheable(value = "statuses", key = "#code.toUpperCase()")
    public ComplianceStatusMaster getStatusByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        // Try cache first
        ComplianceStatusMaster cached = statusCache.get(code.toUpperCase());
        if (cached != null) {
            return cached;
        }

        // Fallback to database
        Optional<ComplianceStatusMaster> found = statusRepo.findByCodeIgnoreCase(code);
        if (found.isPresent()) {
            ComplianceStatusMaster status = found.get();
            statusCache.put(code.toUpperCase(), status);
            return status;
        }

        log.warn("⚠️ Status not found: {}", code);
        return null;
    }

    // ============================================================
    // 📋 GET ALL ACTIVE
    // ============================================================
    @Cacheable(value = "ruleTypes", key = "'all'")
    public List<ComplianceRuleTypeMaster> getAllRuleTypes() {
        return ruleTypeRepo.findAllByActiveTrue();
    }

    @Cacheable(value = "severities", key = "'all'")
    public List<ComplianceSeverityMaster> getAllSeverities() {
        return severityRepo.findAllByActiveTrueOrderByLevelAsc();
    }

    @Cacheable(value = "statuses", key = "'all'")
    public List<ComplianceStatusMaster> getAllStatuses() {
        return statusRepo.findAllByActiveTrue();
    }

    // ============================================================
    // 🧩 HELPER: GET DEFAULT VALUES
    // ============================================================
    public ComplianceSeverityMaster getDefaultSeverity() {
        return getSeverityByCode("MEDIUM");
    }

    public ComplianceStatusMaster getCompliantStatus() {
        return getStatusByCode("COMPLIANT");
    }

    public ComplianceStatusMaster getNonCompliantStatus() {
        return getStatusByCode("NON_COMPLIANT");
    }
}
