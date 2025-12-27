# Compliance Agent Integration Guide

## Overview
The Compliance Agent provides automated validation and compliance checking for all entities in the asset management system. This guide explains how to integrate compliance validation into existing services.

## Integration Methods

### 1. Using ComplianceValidationHelper (Recommended)

The `ComplianceValidationHelper` utility provides easy-to-use methods for validating entities:

```java
@Autowired
private ComplianceValidationHelper complianceHelper;

// In your create method
public AssetMaster create(HttpHeaders headers, AssetRequest request) {
    AssetMaster asset = request.getAsset();
    
    // Validate before saving
    complianceHelper.validateBeforeCreate("ASSET", asset);
    
    // If validation passes, save the entity
    return assetRepo.save(asset);
}

// In your update method
public AssetMaster update(HttpHeaders headers, Long id, AssetRequest request) {
    AssetMaster asset = request.getAsset();
    
    // Validate before updating
    complianceHelper.validateBeforeUpdate("ASSET", id, asset);
    
    // If validation passes, update the entity
    return assetRepo.save(asset);
}
```

### 2. Manual Validation

For more control, you can use `ComplianceAgentService` directly:

```java
@Autowired
private ComplianceAgentService complianceService;

public AssetMaster create(HttpHeaders headers, AssetRequest request) {
    AssetMaster asset = request.getAsset();
    
    // Validate entity
    ComplianceCheckResult result = complianceService.validateEntity("ASSET", null, asset);
    
    if (!result.isCompliant() && result.isHasBlockingViolations()) {
        throw new ComplianceException(
            "Asset failed compliance validation",
            result.getViolations(),
            true);
    }
    
    return assetRepo.save(asset);
}
```

### 3. Post-Save Validation

You can also validate after saving to track violations:

```java
public AssetMaster create(HttpHeaders headers, AssetRequest request) {
    AssetMaster asset = request.getAsset();
    AssetMaster saved = assetRepo.save(asset);
    
    // Validate after save (non-blocking)
    ComplianceCheckResult result = complianceService.validateEntityById("ASSET", saved.getAssetId());
    
    if (!result.isCompliant()) {
        log.warn("⚠️ Asset {} has compliance violations: {}", 
                saved.getAssetId(), result.getViolations().size());
    }
    
    return saved;
}
```

## Compliance Rules

### Default Rules
The system automatically initializes default compliance rules:
- **ASSET_NAME_REQUIRED**: Asset name must not be empty (CRITICAL, blocks operation)
- **ASSET_NAME_UNIQUE**: Asset name must be unique (HIGH, blocks operation)
- **WARRANTY_EXPIRY_CHECK**: Warranty must not be expired (MEDIUM, non-blocking)
- **AMC_RENEWAL_CHECK**: AMC must be renewed before expiry (MEDIUM, non-blocking)

### Creating Custom Rules

Use the Compliance Rule API to create custom rules:

```bash
POST /api/asset/v1/compliance/rules
{
  "ruleCode": "VENDOR_EMAIL_FORMAT",
  "ruleName": "Vendor Email Format Validation",
  "description": "Vendor email must be in valid format",
  "entityType": "VENDOR",
  "ruleType": "FORMAT_VALIDATION",
  "severity": "MEDIUM",
  "ruleExpression": "{\"field\": \"email\", \"pattern\": \"^[A-Za-z0-9+_.-]+@(.+)$\"}",
  "errorMessage": "Vendor email must be in valid format",
  "blocksOperation": false,
  "priority": 50
}
```

## Rule Expression Format

Rules use JSON expressions to define validation logic:

### Required Field
```json
{
  "field": "assetNameUdv"
}
```

### Unique Field
```json
{
  "field": "vendorName"
}
```

### Format Validation
```json
{
  "field": "email",
  "pattern": "^[A-Za-z0-9+_.-]+@(.+)$"
}
```

### Range Validation
```json
{
  "field": "price",
  "min": 0,
  "max": 1000000
}
```

### Length Validation
```json
{
  "field": "description",
  "minLength": 10,
  "maxLength": 1000
}
```

### Reference Integrity
```json
{
  "field": "categoryId",
  "referencedEntityType": "CATEGORY"
}
```

### Date Validation
```json
{
  "field": "purchaseDate",
  "format": "yyyy-MM-dd",
  "minDate": "2020-01-01",
  "maxDate": "2030-12-31"
}
```

## API Endpoints

### Validation
- `POST /api/asset/v1/compliance/validate` - Validate entity
- `GET /api/asset/v1/compliance/validate/{entityType}/{entityId}` - Validate by ID
- `GET /api/asset/v1/compliance/status/{entityType}/{entityId}` - Get compliance status
- `GET /api/asset/v1/compliance/violations/{entityType}/{entityId}` - Get violations
- `POST /api/asset/v1/compliance/violations/{violationId}/resolve` - Resolve violation
- `GET /api/asset/v1/compliance/report/{entityType}/{entityId}` - Generate report
- `POST /api/asset/v1/compliance/validate/bulk/{entityType}` - Bulk validation

### Metrics
- `GET /api/asset/v1/compliance/metrics` - Overall compliance metrics
- `GET /api/asset/v1/compliance/metrics/{entityType}` - Metrics by entity type
- `GET /api/asset/v1/compliance/violations/summary` - Violations summary

### Rule Management
- `GET /api/asset/v1/compliance/rules` - List all rules
- `GET /api/asset/v1/compliance/rules/entity-type/{entityType}` - List by entity type
- `GET /api/asset/v1/compliance/rules/{ruleId}` - Get rule by ID
- `POST /api/asset/v1/compliance/rules` - Create rule
- `PUT /api/asset/v1/compliance/rules/{ruleId}` - Update rule
- `DELETE /api/asset/v1/compliance/rules/{ruleId}` - Delete rule
- `POST /api/asset/v1/compliance/rules/initialize` - Initialize default rules

## Best Practices

1. **Validate Before Save**: Always validate entities before saving to prevent invalid data
2. **Handle Blocking Violations**: Check for blocking violations and prevent save operations
3. **Log Non-Blocking Violations**: Log non-blocking violations for audit purposes
4. **Resolve Violations**: Regularly review and resolve compliance violations
5. **Monitor Metrics**: Use compliance metrics to track overall system health
6. **Custom Rules**: Create custom rules specific to your business requirements
7. **Priority Order**: Set appropriate priorities for rules (lower number = higher priority)

## Example: Full Integration

```java
@Service
public class VendorService {
    
    @Autowired
    private ComplianceValidationHelper complianceHelper;
    
    @Transactional
    public VendorMaster create(HttpHeaders headers, VendorRequest request) {
        VendorMaster vendor = request.getVendor();
        
        // Existing validations
        if (!StringUtils.hasText(vendor.getVendorName())) {
            throw new RuntimeException("Vendor name cannot be blank");
        }
        
        // Compliance validation (will throw ComplianceException if blocking violations)
        complianceHelper.validateBeforeCreate("VENDOR", vendor);
        
        // Save entity
        vendor.setCreatedBy(request.getUsername());
        VendorMaster saved = repo.save(vendor);
        
        // Post-save validation (non-blocking, for tracking)
        ComplianceCheckResult result = complianceHelper.validateAndGetResult(
            "VENDOR", saved.getVendorId(), saved);
        
        if (!result.isCompliant()) {
            log.warn("⚠️ Vendor {} has {} compliance violations", 
                    saved.getVendorId(), result.getViolations().size());
        }
        
        return saved;
    }
}
```

## Troubleshooting

### Violations Not Being Detected
- Ensure compliance rules are active and properly configured
- Check that entity type matches rule entity type (case-insensitive)
- Verify rule expressions are valid JSON

### Blocking Violations Not Preventing Save
- Ensure `blocksOperation` is set to `true` in the rule
- Check that validation is called before save operation
- Verify exception handling is in place

### Performance Issues
- Use bulk validation for multiple entities
- Consider caching compliance rules
- Optimize rule expressions for better performance
