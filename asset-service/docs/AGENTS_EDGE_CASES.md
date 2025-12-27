# Agents Edge Cases Documentation

## Overview
This document describes the edge cases handled by the three main agents:
1. **MasterDataAgentService** - Master data management
2. **UserAssetLinkAgentService** - User-asset linking
3. **AuditAgentService** - Audit logging

## MasterDataAgentService Edge Cases

### Category Operations
- ✅ **Null/Empty Name**: Throws `IllegalArgumentException` if category name is null or empty
- ✅ **Duplicate Name**: Prevents duplicate category names (case-insensitive)
- ✅ **Category Not Found**: Validates category exists before update/delete
- ✅ **Has SubCategories**: Prevents deletion if category has associated subcategories

### SubCategory Operations
- ✅ **Null/Empty Name**: Validates subcategory name is not null/empty
- ✅ **Category Not Found**: Validates parent category exists
- ✅ **Duplicate in Category**: Prevents duplicate subcategory names within same category
- ✅ **Has Makes**: Prevents deletion if subcategory has associated makes

### Make Operations
- ✅ **Null/Empty Name**: Validates make name is not null/empty
- ✅ **SubCategory Not Found**: Validates parent subcategory exists
- ✅ **Duplicate in SubCategory**: Prevents duplicate make names within same subcategory
- ✅ **Has Models**: Prevents deletion if make has associated models

### Model Operations
- ✅ **Null/Empty Name**: Validates model name is not null/empty
- ✅ **Make Not Found**: Validates parent make exists
- ✅ **Duplicate in Make**: Prevents duplicate model names within same make
- ✅ **Has Assets**: Prevents deletion if model has associated assets

### Vendor/Outlet/Component Operations
- ✅ **Null/Empty Name**: Validates names are not null/empty
- ✅ **Duplicate Names**: Prevents duplicate vendor/outlet names (case-insensitive)

### Bulk Operations
- ✅ **Partial Failures**: Continues processing even if some items fail
- ✅ **Error Aggregation**: Collects all errors and returns summary
- ✅ **Transaction Safety**: Uses `@Transactional` for atomicity

## UserAssetLinkAgentService Edge Cases

### Link Operations
- ✅ **Null IDs**: Validates asset/component ID and user ID are not null
- ✅ **Null Username**: Validates username is not null/empty
- ✅ **Entity Not Found**: Validates asset/component exists before linking
- ✅ **Already Linked to Same User**: Returns existing link instead of error (idempotent)
- ✅ **Already Linked to Different User**: Throws `IllegalStateException` to prevent conflicts
- ✅ **Compliance Check**: Validates compliance before linking (non-blocking warning)

### Delink Operations
- ✅ **Null IDs**: Validates IDs are not null
- ✅ **Link Not Found**: Validates active link exists before delinking
- ✅ **Soft Delete**: Uses soft delete to maintain history

### Query Operations
- ✅ **Null User ID**: Validates user ID is not null
- ✅ **Empty Results**: Returns empty list instead of null
- ✅ **History Tracking**: Maintains complete assignment history

### Bulk Operations
- ✅ **Partial Failures**: Continues processing remaining items
- ✅ **Error Collection**: Aggregates errors for reporting
- ✅ **Idempotency**: Handles duplicate links gracefully

## AuditAgentService Edge Cases

### Logging Operations
- ✅ **Null Username**: Defaults to "SYSTEM" if username is null
- ✅ **Null Event Message**: Defaults to "Unknown event" if message is null
- ✅ **Null Request**: Handles missing request information gracefully
- ✅ **Proxy Headers**: Extracts IP from X-Forwarded-For, X-Real-IP headers
- ✅ **Multiple IPs**: Handles comma-separated IP addresses in headers

### Query Operations
- ✅ **Null Username**: Returns empty list for null username searches
- ✅ **Null Entity Type**: Returns empty list for null entity type searches
- ✅ **Invalid Date Range**: Validates start date is before end date
- ✅ **Null Dates**: Throws exception for null date parameters
- ✅ **Invalid Limit**: Defaults to 100, max 1000 for performance

### Search Operations
- ✅ **Null/Empty Keyword**: Returns empty list for null/empty searches
- ✅ **Case-Insensitive**: Performs case-insensitive search across multiple fields

### Cleanup Operations
- ✅ **Invalid Days**: Validates days to keep is not negative
- ✅ **Too Aggressive**: Warns if cleanup is too aggressive (< 30 days)
- ✅ **Soft Delete**: Uses soft delete to maintain audit trail

## Common Edge Cases Handled

### Null Safety
- All methods validate null parameters
- Return empty collections instead of null
- Use Optional for nullable returns

### Duplicate Prevention
- Case-insensitive duplicate checks
- Context-aware uniqueness (within parent entity)
- Clear error messages for duplicates

### Relationship Integrity
- Prevents deletion of entities with dependencies
- Validates parent entities exist
- Maintains referential integrity

### Transaction Management
- Uses `@Transactional` for atomic operations
- Handles partial failures in bulk operations
- Maintains data consistency

### Performance
- Limits query results to prevent memory issues
- Uses efficient repository methods
- Implements pagination where needed

### Error Handling
- Descriptive error messages
- Proper exception types (IllegalArgumentException, IllegalStateException)
- Logging for debugging

## Best Practices

1. **Always validate input**: Check for null, empty, and invalid values
2. **Check relationships**: Validate parent entities exist before operations
3. **Prevent duplicates**: Use case-insensitive checks with context
4. **Maintain history**: Use soft deletes for audit trails
5. **Handle partial failures**: Continue processing in bulk operations
6. **Log appropriately**: Use appropriate log levels (info, warn, error)
7. **Return meaningful errors**: Provide clear error messages to users
8. **Idempotent operations**: Make operations safe to retry
