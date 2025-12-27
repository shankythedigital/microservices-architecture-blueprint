# Compliance Agent API - Postman Collection

## Overview
This Postman collection provides comprehensive API testing for the Compliance Agent system, including validation, status checks, violations management, reports, metrics, and rule management.

## Files
- **Compliance_Agent_API.postman_collection.json** - Main Postman collection with all endpoints
- **Compliance_Agent_Environment.postman_environment.json** - Environment variables for local development

## Setup Instructions

### 1. Import Collection
1. Open Postman
2. Click **Import** button
3. Select `Compliance_Agent_API.postman_collection.json`
4. Click **Import**

### 2. Import Environment
1. Click **Environments** in left sidebar
2. Click **Import**
3. Select `Compliance_Agent_Environment.postman_environment.json`
4. Click **Import**
5. Select the imported environment from the dropdown (top right)

### 3. Configure Variables

Update the following variables in the environment:

| Variable | Description | Example Value |
|----------|-------------|---------------|
| `baseUrl` | Base URL of the asset service | `http://localhost:8080` |
| `authToken` | JWT authentication token | `your-jwt-token-here` |
| `entityType` | Entity type for testing | `ASSET`, `VENDOR`, `WARRANTY`, etc. |
| `entityId` | Entity ID for testing | `1` |
| `ruleId` | Compliance rule ID | `1` |
| `violationId` | Violation ID | `1` |
| `username` | Username for audit trail | `admin` |

## Collection Structure

### 1. Compliance Validation
- **Validate Entity** - Validate entity with request body
- **Validate Entity by ID** - Validate entity by type and ID
- **Bulk Validation** - Validate multiple entities at once

### 2. Compliance Status
- **Get Compliance Status** - Get current compliance status of an entity

### 3. Compliance Violations
- **Get Violations** - Get all violations for an entity
- **Resolve Violation** - Mark a violation as resolved
- **Get Violations Summary** - Get summary of all violations

### 4. Compliance Reports
- **Generate Compliance Report** - Generate detailed compliance report

### 5. Compliance Metrics
- **Get Overall Metrics** - Get system-wide compliance metrics
- **Get Metrics by Entity Type** - Get metrics for specific entity type

### 6. Compliance Rules
- **List All Rules** - Get all compliance rules
- **List Rules by Entity Type** - Get rules for specific entity type
- **Get Rule by ID** - Get specific rule details
- **Create Rule** - Create new compliance rule
- **Update Rule** - Update existing rule
- **Delete Rule** - Soft delete a rule
- **Get Rule Templates** - Get pre-defined rule templates
- **Initialize Default Rules** - Initialize default system rules

## Example Requests

### Validate Asset
```json
POST /api/asset/v1/compliance/validate
{
  "entityType": "ASSET",
  "entityId": 1,
  "autoResolve": false
}
```

### Create Compliance Rule
```json
POST /api/asset/v1/compliance/rules
{
  "ruleCode": "VENDOR_EMAIL_FORMAT",
  "ruleName": "Vendor Email Format Validation",
  "description": "Vendor email must be in valid format",
  "entityType": "VENDOR",
  "ruleType": {
    "code": "FORMAT_VALIDATION"
  },
  "severity": {
    "code": "MEDIUM"
  },
  "ruleExpression": "{\"field\": \"email\", \"pattern\": \"^[A-Za-z0-9+_.-]+@(.+)$\"}",
  "errorMessage": "Vendor email must be in valid format",
  "blocksOperation": false,
  "priority": 50
}
```

### Resolve Violation
```
POST /api/asset/v1/compliance/violations/1/resolve?resolvedBy=admin&notes=Fixed%20the%20issue
```

## Testing Workflow

1. **Initialize System**
   - Run "Initialize Default Rules" to set up default compliance rules

2. **Create Test Rule**
   - Use "Create Rule" to add a custom compliance rule

3. **Validate Entity**
   - Use "Validate Entity" to check compliance of an entity

4. **Check Status**
   - Use "Get Compliance Status" to see current compliance status

5. **View Violations**
   - Use "Get Violations" to see any compliance violations

6. **Resolve Violations**
   - Use "Resolve Violation" to mark violations as resolved

7. **View Metrics**
   - Use "Get Overall Metrics" to see system-wide compliance statistics

## Notes

- All requests require `Authorization` header with Bearer token
- Replace `{{variableName}}` with actual values or configure in environment
- Entity types: `ASSET`, `VENDOR`, `OUTLET`, `WARRANTY`, `AMC`, `CATEGORY`, `SUBCATEGORY`, `MAKE`, `MODEL`, `COMPONENT`
- Rule types: `REQUIRED_FIELD`, `UNIQUE_FIELD`, `FORMAT_VALIDATION`, `RANGE_VALIDATION`, `LENGTH_VALIDATION`, `REFERENCE_INTEGRITY`, `DATE_VALIDATION`, `WARRANTY_EXPIRY`, `AMC_RENEWAL`, etc.
- Severity levels: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO`
- Status values: `COMPLIANT`, `NON_COMPLIANT`, `PENDING`, `EXEMPTED`, `UNDER_REVIEW`

## Troubleshooting

### 401 Unauthorized
- Check that `authToken` is valid and not expired
- Ensure token is properly formatted in Authorization header

### 404 Not Found
- Verify `baseUrl` is correct
- Check that entity IDs exist in the database

### 400 Bad Request
- Validate request body matches expected format
- Check that rule types and severities exist in master data

### 500 Internal Server Error
- Check server logs for detailed error messages
- Ensure master data is initialized (run Initialize Default Rules)
