package com.example.asset.dto;

import com.example.asset.enums.ComplianceRuleType;
import com.example.asset.enums.ComplianceSeverity;

/**
 * ✅ ComplianceRuleRequest DTO
 * Request DTO for creating/updating compliance rules.
 */
public class ComplianceRuleRequest {
    
    private String ruleCode;
    private String ruleName;
    private String description;
    private String entityType;
    private ComplianceRuleType ruleType;
    private ComplianceSeverity severity;
    private String ruleExpression;
    private String errorMessage;
    private Boolean blocksOperation;
    private Integer priority;
    
    public ComplianceRuleRequest() {}
    
    // Getters and Setters
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public ComplianceRuleType getRuleType() { return ruleType; }
    public void setRuleType(ComplianceRuleType ruleType) { this.ruleType = ruleType; }
    
    public ComplianceSeverity getSeverity() { return severity; }
    public void setSeverity(ComplianceSeverity severity) { this.severity = severity; }
    
    public String getRuleExpression() { return ruleExpression; }
    public void setRuleExpression(String ruleExpression) { this.ruleExpression = ruleExpression; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Boolean getBlocksOperation() { return blocksOperation; }
    public void setBlocksOperation(Boolean blocksOperation) { this.blocksOperation = blocksOperation; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
}
