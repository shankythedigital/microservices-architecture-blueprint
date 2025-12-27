# Postman Collection Guide - Asset Service v2.5

## Overview

This Postman collection provides comprehensive API testing for the Asset Management Service with detailed environment and body variables.

## Files

1. **Collection File**: `docs/M3 v2.5 Asset Service - Complete API Collection.postman_collection.json`
2. **Environment File**: `asset-service/docs/postman/Asset_Service_Environment.postman_environment.json`

## Setup Instructions

### 1. Import Collection and Environment

1. Open Postman
2. Click **Import** button
3. Import both files:
   - `M3 v2.5 Asset Service - Complete API Collection.postman_collection.json`
   - `Asset_Service_Environment.postman_environment.json`
4. Select the environment: **Asset Service - Local**

### 2. Configure Environment Variables

Update the following variables in the environment:

#### Required Variables

- **assetbaseUrl**: Base URL for Asset Service (default: `http://localhost:8083`)
- **accessToken**: JWT Bearer token from auth-service login endpoint

#### User Context Variables

- **userId**: Current user ID (default: `1`)
- **username**: Current username (default: `admin`)
- **projectType**: Project type for notifications (default: `ASSET_SERVICE`)

#### Entity ID Variables

Update these after creating entities:

- **categoryId**: Category ID
- **subCategoryId**: SubCategory ID
- **makeId**: Make ID
- **modelId**: Model ID
- **vendorId**: Vendor ID
- **outletId**: Outlet ID
- **componentId**: Component ID
- **assetId**: Asset ID
- **warrantyId**: Warranty ID
- **amcId**: AMC ID
- **documentId**: Document ID

#### Operation-Specific Variables

- **targetUserId**: Target user ID for linking operations (default: `2`)
- **targetUsername**: Target username for linking operations (default: `user1`)
- **entityType**: Entity type for compliance/document operations (default: `ASSET`)
- **entityId**: Entity ID for compliance/document operations (default: `1`)
- **searchKeyword**: Search keyword for asset search (default: `laptop`)
- **filename**: File name for download operations (default: `example.pdf`)

### 3. Get Access Token

1. Use auth-service login endpoint to get JWT token
2. Copy the token
3. Update `accessToken` variable in environment: `Bearer <your-token>`

## Collection Structure

The collection is organized into the following folders:

### 1. Assets
- Create, Update, Delete, Get Asset
- Search Assets (with pagination)
- Bulk Create Assets (JSON)
- Bulk Upload Assets (Excel)

### 2. Categories
- CRUD operations
- Bulk Create Categories
- Bulk Upload Categories (Excel)

### 3. SubCategories
- CRUD operations
- Bulk Create SubCategories
- Bulk Upload SubCategories (Excel)

### 4. Makes
- CRUD operations
- Bulk Create Makes
- Bulk Upload Makes (Excel)

### 5. Models
- CRUD operations
- Bulk Create Models
- Bulk Upload Models (Excel)

### 6. Components
- CRUD operations
- Bulk Create Components
- Bulk Upload Components (Excel)

### 7. Vendors
- CRUD operations
- Bulk Create Vendors
- Bulk Upload Vendors (Excel)

### 8. Outlets
- CRUD operations
- Bulk Create Outlets
- Bulk Upload Outlets (Excel)

### 9. Documents
- Upload Document (multipart/form-data)
- Get Document Details
- Download Document
- Delete Document
- Bulk Create Documents (JSON)
- Bulk Upload Documents (Excel)

### 10. Warranty
- Create, Update, Delete, List, Get Warranty

### 11. AMC
- Create, Update, Delete, List, Get AMC

### 12. Compliance
- Validate Entity
- Get Compliance Status
- Get Violations
- Resolve Violation
- Generate Compliance Report
- Bulk Validation
- Compliance Metrics
- Violations Summary

### 13. Compliance Rules
- List All Rules
- List Rules by Entity Type
- Get Rule by ID
- Create, Update, Delete Rule
- Initialize Default Rules
- Get Rule Templates

### 14. Entity Types
- List All Entity Types
- List Active Entity Types
- Find by Code/ID
- Validate Entity Type
- Initialize Entity Types

### 15. Statuses
- List All Statuses
- List Active Statuses
- List by Category
- Find by Code/ID
- Validate Status
- Initialize Statuses

### 16. Master Data Agent
- Category Operations
- SubCategory Operations
- Make Operations
- Model Operations
- Vendor Operations
- Outlet Operations
- Component Operations
- Bulk Operations
- Validation & Summary

### 17. User Asset Link Agent
- Link Asset to User
- Link Component to User
- Delink Asset from User
- Delink Component from User
- Get Assets/Components Assigned to User
- Get Assignment History
- Check Link Status
- Get Link Statistics
- Bulk Link Assets

### 18. Audit Agent
- Log Audit Event (Unified endpoint)
- Get All Audit Logs
- Get Audit Logs by Username
- Get Audit Logs by Entity Type
- Get Audit Logs by Date Range
- Get Recent Audit Logs
- Search Audit Logs
- Get Audit Statistics
- Cleanup Old Audit Logs

### 19. User Links
- Link Entity (Universal)
- Delink Entity (Universal)
- Link Multiple Entities
- Delink Multiple Entities
- Get Assigned Assets
- Get Single Asset
- Get Users by SubCategory

### 20. File Download
- Download or View File

## Request Body Variables

All request bodies use environment variables with `{{variableName}}` syntax. Examples:

```json
{
  "userId": "{{userId}}",
  "username": "{{username}}",
  "projectType": "{{projectType}}",
  "category": {
    "categoryName": "Electronics",
    "description": "Electronic devices"
  }
}
```

## Excel Upload Format

### Categories Excel Format

| category_name | description |
|--------------|-------------|
| Electronics  | Electronic devices |
| Furniture    | Office furniture |

### Assets Excel Format (Row-based Components)

| asset_id | asset_name_udv | asset_status | category_id | category_name | sub_category_id | subcategory_name | make_id | make_name | model_id | model_name | component_id | component_name |
|----------|----------------|--------------|-------------|---------------|-----------------|------------------|---------|-----------|----------|------------|---------------|----------------|
| 1        | LAPTOP-001     | Active       | 1           | Electronics   | 1               | Laptops          | 1       | Dell      | 1        | XPS 13     | 1            | RAM            |
| 1        | LAPTOP-001     | Active       | 1           | Electronics   | 1               | Laptops          | 1       | Dell      | 1        | XPS 13     | 2            | SSD            |

**Note**: Components are stored in separate rows. Multiple rows with the same `asset_id` or `asset_name_udv` are grouped together.

### Documents Excel Format

| document_id | entity_type | entity_id | file_name   | file_path              | doc_type |
|-------------|-------------|-----------|-------------|------------------------|----------|
| 1           | ASSET       | 101       | invoice.pdf | /path/to/invoices/1.pdf | INVOICE  |
| 2           | CATEGORY    | 1         | image.jpg   | /path/to/images/cat1.jpg | IMAGE    |

## Testing Workflow

1. **Setup Master Data**:
   - Create Categories
   - Create SubCategories
   - Create Makes
   - Create Models
   - Create Components
   - Create Vendors
   - Create Outlets

2. **Create Assets**:
   - Create individual assets
   - Or use bulk upload (JSON or Excel)

3. **Link Assets to Users**:
   - Use User Asset Link Agent or User Links endpoints

4. **Upload Documents**:
   - Upload documents linked to assets, categories, vendors, etc.

5. **Compliance Checks**:
   - Validate entities
   - Check compliance status
   - Resolve violations

## Common Issues

### 401 Unauthorized
- Check `accessToken` is set correctly
- Ensure token is not expired
- Format: `Bearer <token>`

### 404 Not Found
- Verify entity IDs exist
- Check entity IDs in environment variables

### 400 Bad Request
- Validate request body format
- Check required fields are provided
- Verify foreign key relationships exist

## Support

For issues or questions:
1. Check controller code in `asset-service/src/main/java/com/example/asset/controller/`
2. Review service implementations
3. Check application logs

