package com.example.asset.exception;

import com.example.asset.entity.ComplianceViolation;
import java.util.List;

/**
 * ✅ ComplianceException
 * Custom exception for compliance violations.
 * Contains list of violations that caused the exception.
 */
public class ComplianceException extends RuntimeException {
    
    private final List<ComplianceViolation> violations;
    private final boolean blocking;
    
    public ComplianceException(String message, List<ComplianceViolation> violations, boolean blocking) {
        super(message);
        this.violations = violations;
        this.blocking = blocking;
    }
    
    public ComplianceException(String message, List<ComplianceViolation> violations) {
        this(message, violations, false);
    }
    
    public List<ComplianceViolation> getViolations() {
        return violations;
    }
    
    public boolean isBlocking() {
        return blocking;
    }
}
