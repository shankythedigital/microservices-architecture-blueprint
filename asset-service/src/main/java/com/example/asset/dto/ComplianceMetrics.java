package com.example.asset.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ✅ ComplianceMetrics DTO
 * Metrics and statistics for compliance monitoring.
 */
public class ComplianceMetrics {
    
    private long totalEntities;
    private long compliantEntities;
    private long nonCompliantEntities;
    private long totalViolations;
    private long criticalViolations;
    private long highViolations;
    private long mediumViolations;
    private long lowViolations;
    private long unresolvedViolations;
    private Map<String, Long> violationsByEntityType;
    private Map<String, Long> violationsBySeverity;
    private LocalDateTime generatedAt;
    
    public ComplianceMetrics() {
        this.generatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public long getTotalEntities() { return totalEntities; }
    public void setTotalEntities(long totalEntities) { this.totalEntities = totalEntities; }
    
    public long getCompliantEntities() { return compliantEntities; }
    public void setCompliantEntities(long compliantEntities) { this.compliantEntities = compliantEntities; }
    
    public long getNonCompliantEntities() { return nonCompliantEntities; }
    public void setNonCompliantEntities(long nonCompliantEntities) { this.nonCompliantEntities = nonCompliantEntities; }
    
    public long getTotalViolations() { return totalViolations; }
    public void setTotalViolations(long totalViolations) { this.totalViolations = totalViolations; }
    
    public long getCriticalViolations() { return criticalViolations; }
    public void setCriticalViolations(long criticalViolations) { this.criticalViolations = criticalViolations; }
    
    public long getHighViolations() { return highViolations; }
    public void setHighViolations(long highViolations) { this.highViolations = highViolations; }
    
    public long getMediumViolations() { return mediumViolations; }
    public void setMediumViolations(long mediumViolations) { this.mediumViolations = mediumViolations; }
    
    public long getLowViolations() { return lowViolations; }
    public void setLowViolations(long lowViolations) { this.lowViolations = lowViolations; }
    
    public long getUnresolvedViolations() { return unresolvedViolations; }
    public void setUnresolvedViolations(long unresolvedViolations) { this.unresolvedViolations = unresolvedViolations; }
    
    public Map<String, Long> getViolationsByEntityType() { return violationsByEntityType; }
    public void setViolationsByEntityType(Map<String, Long> violationsByEntityType) { 
        this.violationsByEntityType = violationsByEntityType; 
    }
    
    public Map<String, Long> getViolationsBySeverity() { return violationsBySeverity; }
    public void setViolationsBySeverity(Map<String, Long> violationsBySeverity) { 
        this.violationsBySeverity = violationsBySeverity; 
    }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public double getComplianceRate() {
        if (totalEntities == 0) return 100.0;
        return (double) compliantEntities / totalEntities * 100.0;
    }
}
