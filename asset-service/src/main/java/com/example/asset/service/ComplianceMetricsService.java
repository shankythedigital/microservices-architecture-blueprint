package com.example.asset.service;

import com.example.asset.dto.ComplianceMetrics;
import com.example.asset.entity.ComplianceViolation;
import com.example.asset.entity.ComplianceSeverityMaster;
import com.example.asset.service.ComplianceMasterCacheService;
import com.example.asset.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ✅ ComplianceMetricsService
 * Service for generating compliance metrics and statistics.
 */
@Service
public class ComplianceMetricsService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceMetricsService.class);

    private final ComplianceViolationRepository violationRepo;
    private final AssetMasterRepository assetRepo;
    private final AssetWarrantyRepository warrantyRepo;
    private final AssetAmcRepository amcRepo;
    private final VendorRepository vendorRepo;
    private final PurchaseOutletRepository outletRepo;
    private final ComplianceMasterCacheService cacheService;

    public ComplianceMetricsService(
            ComplianceViolationRepository violationRepo,
            AssetMasterRepository assetRepo,
            AssetWarrantyRepository warrantyRepo,
            AssetAmcRepository amcRepo,
            VendorRepository vendorRepo,
            PurchaseOutletRepository outletRepo,
            ComplianceMasterCacheService cacheService) {
        this.violationRepo = violationRepo;
        this.assetRepo = assetRepo;
        this.warrantyRepo = warrantyRepo;
        this.amcRepo = amcRepo;
        this.vendorRepo = vendorRepo;
        this.outletRepo = outletRepo;
        this.cacheService = cacheService;
    }

    // ============================================================
    // 📊 GENERATE OVERALL METRICS
    // ============================================================
    public ComplianceMetrics generateOverallMetrics() {
        ComplianceMetrics metrics = new ComplianceMetrics();
        
        // Count total entities
        long totalAssets = assetRepo.count();
        long totalWarranties = warrantyRepo.count();
        long totalAmcs = amcRepo.count();
        long totalVendors = vendorRepo.count();
        long totalOutlets = outletRepo.count();
        metrics.setTotalEntities(totalAssets + totalWarranties + totalAmcs + totalVendors + totalOutlets);
        
        // Get all unresolved violations
        List<ComplianceViolation> unresolvedViolations = violationRepo.findAllByStatus_IsResolvedFalseAndActiveTrue();
        
        metrics.setTotalViolations(unresolvedViolations.size());
        metrics.setUnresolvedViolations(unresolvedViolations.size());
        
        // Get severity masters from cache
        ComplianceSeverityMaster critical = cacheService.getSeverityByCode("CRITICAL");
        ComplianceSeverityMaster high = cacheService.getSeverityByCode("HIGH");
        ComplianceSeverityMaster medium = cacheService.getSeverityByCode("MEDIUM");
        ComplianceSeverityMaster low = cacheService.getSeverityByCode("LOW");
        
        // Count by severity
        metrics.setCriticalViolations(countBySeverity(unresolvedViolations, critical));
        metrics.setHighViolations(countBySeverity(unresolvedViolations, high));
        metrics.setMediumViolations(countBySeverity(unresolvedViolations, medium));
        metrics.setLowViolations(countBySeverity(unresolvedViolations, low));
        
        // Count violations by entity type
        Map<String, Long> byEntityType = unresolvedViolations.stream()
                .collect(Collectors.groupingBy(
                        ComplianceViolation::getEntityType,
                        Collectors.counting()));
        metrics.setViolationsByEntityType(byEntityType);
        
        // Count violations by severity
        Map<String, Long> bySeverity = new HashMap<>();
        bySeverity.put("CRITICAL", metrics.getCriticalViolations());
        bySeverity.put("HIGH", metrics.getHighViolations());
        bySeverity.put("MEDIUM", metrics.getMediumViolations());
        bySeverity.put("LOW", metrics.getLowViolations());
        metrics.setViolationsBySeverity(bySeverity);
        
        // Calculate compliant entities (entities without violations)
        long entitiesWithViolations = byEntityType.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        metrics.setCompliantEntities(metrics.getTotalEntities() - entitiesWithViolations);
        metrics.setNonCompliantEntities(entitiesWithViolations);
        
        log.info("📊 Compliance metrics generated: {} total entities, {} violations", 
                metrics.getTotalEntities(), metrics.getTotalViolations());
        
        return metrics;
    }

    // ============================================================
    // 📊 GENERATE METRICS BY ENTITY TYPE
    // ============================================================
    public ComplianceMetrics generateMetricsByEntityType(String entityType) {
        ComplianceMetrics metrics = new ComplianceMetrics();
        
        // Count entities of this type
        long totalCount = countEntitiesByType(entityType);
        metrics.setTotalEntities(totalCount);
        
        // Get all violations for this entity type
        List<ComplianceViolation> allViolations = violationRepo.findAll().stream()
                .filter(v -> v.getEntityType() != null && v.getEntityType().equalsIgnoreCase(entityType))
                .filter(v -> v.getStatus() != null && 
                            (v.getStatus().getIsResolved() == null || !v.getStatus().getIsResolved()))
                .filter(v -> v.getActive() != null && v.getActive())
                .collect(Collectors.toList());
        
        long uniqueEntitiesWithViolations = allViolations.stream()
                .map(v -> v.getEntityType() + ":" + v.getEntityId())
                .distinct()
                .count();
        
        metrics.setTotalViolations(allViolations.size());
        metrics.setUnresolvedViolations(allViolations.size());
        metrics.setNonCompliantEntities(uniqueEntitiesWithViolations);
        metrics.setCompliantEntities(totalCount - uniqueEntitiesWithViolations);
        
        // Get severity masters from cache
        ComplianceSeverityMaster critical = cacheService.getSeverityByCode("CRITICAL");
        ComplianceSeverityMaster high = cacheService.getSeverityByCode("HIGH");
        ComplianceSeverityMaster medium = cacheService.getSeverityByCode("MEDIUM");
        ComplianceSeverityMaster low = cacheService.getSeverityByCode("LOW");
        
        metrics.setCriticalViolations(countBySeverity(allViolations, critical));
        metrics.setHighViolations(countBySeverity(allViolations, high));
        metrics.setMediumViolations(countBySeverity(allViolations, medium));
        metrics.setLowViolations(countBySeverity(allViolations, low));
        
        return metrics;
    }

    // ============================================================
    // 📊 GET VIOLATIONS SUMMARY
    // ============================================================
    public Map<String, Object> getViolationsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        List<ComplianceViolation> allViolations = violationRepo.findAllByStatus_IsResolvedFalseAndActiveTrue();
        
        // Get severity masters from cache
        ComplianceSeverityMaster critical = cacheService.getSeverityByCode("CRITICAL");
        ComplianceSeverityMaster high = cacheService.getSeverityByCode("HIGH");
        ComplianceSeverityMaster medium = cacheService.getSeverityByCode("MEDIUM");
        ComplianceSeverityMaster low = cacheService.getSeverityByCode("LOW");
        
        summary.put("total", allViolations.size());
        summary.put("critical", countBySeverity(allViolations, critical));
        summary.put("high", countBySeverity(allViolations, high));
        summary.put("medium", countBySeverity(allViolations, medium));
        summary.put("low", countBySeverity(allViolations, low));
        
        // Recent violations (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long recentViolations = allViolations.stream()
                .filter(v -> v.getDetectedAt() != null && v.getDetectedAt().isAfter(sevenDaysAgo))
                .count();
        summary.put("recentViolations", recentViolations);
        
        return summary;
    }

    // ============================================================
    // 🧩 HELPER METHODS
    // ============================================================
    private long countBySeverity(List<ComplianceViolation> violations, ComplianceSeverityMaster severity) {
        if (severity == null) return 0;
        return violations.stream()
                .filter(v -> v.getSeverity() != null && v.getSeverity().equals(severity))
                .count();
    }

    private long countEntitiesByType(String entityType) {
        switch (entityType.toUpperCase()) {
            case "ASSET": return assetRepo.count();
            case "WARRANTY": return warrantyRepo.count();
            case "AMC": return amcRepo.count();
            case "VENDOR": return vendorRepo.count();
            case "OUTLET": return outletRepo.count();
            default: return 0;
        }
    }
}

