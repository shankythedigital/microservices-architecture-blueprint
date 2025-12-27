# Asset Service - Postman Collection

## 📋 Overview

Complete Postman collection for Asset Management Service with all controllers, environment variables, and request body examples.

## 📁 Files

1. **Asset_Service_API.postman_collection.json** - Complete API collection
2. **Asset_Service_Environment.postman_environment.json** - Environment variables

## 🚀 Setup

### 1. Import Collection

1. Open Postman
2. Click **Import** button
3. Select `Asset_Service_API.postman_collection.json`
4. Collection will be imported with all folders and requests

### 2. Import Environment

1. Click **Environments** in left sidebar
2. Click **Import**
3. Select `Asset_Service_Environment.postman_environment.json`
4. Select the environment from dropdown

### 3. Configure Environment Variables

Update the following variables in the environment:

| Variable | Description | Example Value |
|----------|-------------|---------------|
| `baseUrl` | Asset service base URL | `http://localhost:8083` |
| `authToken` | JWT authentication token | `Bearer token from auth service` |
| `userId` | Current user ID | `1` |
| `username` | Current username | `admin` |
| `projectType` | Project type | `ASSET_SERVICE` |
| `assetId` | Asset ID for testing | `1` |
| `categoryId` | Category ID | `1` |
| `subCategoryId` | SubCategory ID | `1` |
| `makeId` | Make ID | `1` |
| `modelId` | Model ID | `1` |
| `vendorId` | Vendor ID | `1` |
| `outletId` | Outlet ID | `1` |
| `componentId` | Component ID | `1` |
| `warrantyId` | Warranty ID | `1` |
| `amcId` | AMC ID | `1` |
| `documentId` | Document ID | `1` |
| `targetUserId` | Target user ID for linking | `2` |
| `targetUsername` | Target username | `user1` |
| `entityType` | Entity type code | `ASSET` |
| `entityId` | Entity ID | `1` |
| `ruleId` | Compliance rule ID | `1` |
| `violationId` | Violation ID | `1` |
| `searchKeyword` | Search keyword | `laptop` |

## 📂 Collection Structure

The collection is organized into 19 folders:

### 1. Assets
- Create Asset
- Get Asset by ID
- Update Asset
- Delete Asset
- Search Assets

### 2. Categories
- Create Category
- List All Categories
- Get Category by ID
- Update Category
- Delete Category
- Bulk Create Categories
- Bulk Upload Categories (Excel)

### 3. SubCategories
- Create SubCategory
- List All SubCategories
- Get SubCategory by ID
- Update SubCategory
- Delete SubCategory
- Bulk Create SubCategories
- Bulk Upload SubCategories (Excel)

### 4. Makes
- Create Make
- List All Makes
- Get Make by ID
- Update Make
- Delete Make
- Bulk Create Makes
- Bulk Upload Makes (Excel)

### 5. Models
- Create Model
- List All Models
- Get Model by ID
- Update Model
- Delete Model
- Bulk Create Models
- Bulk Upload Models (Excel)

### 6. Vendors
- Create Vendor
- List All Vendors
- Get Vendor by ID
- Update Vendor
- Delete Vendor
- Bulk Create Vendors
- Bulk Upload Vendors (Excel)

### 7. Outlets
- Create Outlet
- List All Outlets
- Get Outlet by ID
- Update Outlet
- Delete Outlet
- Bulk Create Outlets
- Bulk Upload Outlets (Excel)

### 8. Components
- Create Component
- List All Components
- Get Component by ID
- Update Component
- Delete Component
- Bulk Create Components
- Bulk Upload Components (Excel)

### 9. User Links
- Link Asset to User
- Delink Asset from User
- Link Multiple Assets
- Delink Multiple Assets
- Get Assigned Assets
- Get Asset Link Info
- Get Users by SubCategory

### 10. Warranty
- Create Warranty
- List All Warranties
- Get Warranty by ID
- Update Warranty
- Delete Warranty

### 11. AMC
- Create AMC
- List All AMCs
- Get AMC by ID
- Update AMC
- Delete AMC

### 12. Documents
- Upload Document
- Get Document by ID
- Download Document
- Delete Document

### 13. Compliance
- Validate Entity
- Validate Entity by ID
- Get Compliance Status
- Get Violations
- Resolve Violation
- Bulk Validate
- Get Compliance Metrics
- Get Metrics by Entity Type

### 14. Compliance Rules
- List All Rules
- Get Rules by Entity Type
- Get Rule by ID
- Create Rule
- Update Rule
- Delete Rule

### 15. Entity Types
- List All Entity Types
- List Active Entity Types
- Get Entity Type by Code
- Get Entity Type by ID
- Initialize Entity Types

### 16. Status
- List All Statuses
- List Active Statuses
- Get Statuses by Category
- Get Status by Code
- Get Status by ID
- Initialize Statuses

### 17. Master Data Agent
- Create Category (Agent)
- Create SubCategory (Agent)
- Create Make (Agent)
- Create Model (Agent)
- Create Vendor (Agent)
- Create Outlet (Agent)
- Get Master Data Summary

### 18. User Asset Link Agent
- Link Asset
- Link Component
- Get User Assets
- Get Asset Link History
- Bulk Link Assets

### 19. Audit Agent
- Create Audit Log
- List Audit Logs
- Get Logs by Username
- Get Logs by Entity Type
- Get Logs by Date Range
- Get Audit Statistics

## 🔐 Authentication

All requests require JWT authentication. The token is automatically included via the `{{authToken}}` variable in the Authorization header.

### Getting a Token

1. Use Auth Service to login:
   ```
   POST http://localhost:8081/api/auth/login
   ```
2. Copy the `accessToken` from response
3. Update `authToken` variable in environment

## 📝 Request Body Variables

All request bodies use environment variables for:
- `{{userId}}` - Current user ID
- `{{username}}` - Current username
- `{{projectType}}` - Project type (usually `ASSET_SERVICE`)
- Entity IDs (e.g., `{{assetId}}`, `{{categoryId}}`)

### Example Request Body

```json
{
  "userId": "{{userId}}",
  "username": "{{username}}",
  "projectType": "{{projectType}}",
  "asset": {
    "assetNameUdv": "ASSET-001",
    "assetStatus": "AVAILABLE",
    "category": {
      "categoryId": "{{categoryId}}"
    }
  }
}
```

## 📤 File Uploads

Excel upload endpoints use `form-data` format:
- `file` - Excel file (select file in Postman)
- `userId` - User ID
- `username` - Username
- `projectType` - Project type

## 🔄 Using Variables

### Dynamic Values

Postman supports dynamic values:
- `{{$randomInt}}` - Random integer
- `{{$timestamp}}` - Current timestamp
- `{{$guid}}` - Random GUID

### Example with Dynamic Value

```json
{
  "asset": {
    "assetNameUdv": "ASSET-{{$randomInt}}"
  }
}
```

## 🧪 Testing Workflow

### 1. Setup Master Data
1. Create Category
2. Create SubCategory (linked to category)
3. Create Make (linked to subcategory)
4. Create Model (linked to make)
5. Create Vendor
6. Create Outlet (linked to vendor)

### 2. Create Asset
1. Use created master data IDs
2. Create Asset with proper relationships

### 3. Link Asset to User
1. Use created asset ID
2. Link to target user

### 4. Upload Document
1. Select file in form-data
2. Link to asset

### 5. Check Compliance
1. Validate asset
2. Check violations
3. Resolve if needed

## 📊 Response Format

All responses follow this format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

## 🐛 Troubleshooting

### 401 Unauthorized
- Check `authToken` is valid
- Token may have expired - get new token from auth service

### 404 Not Found
- Verify `baseUrl` is correct
- Check entity IDs exist in database

### 400 Bad Request
- Verify request body format
- Check required fields are present
- Validate data types match expected format

### 500 Internal Server Error
- Check server logs
- Verify database connection
- Check entity relationships are valid

## 📚 Additional Resources

- **Swagger UI**: http://localhost:8083/swagger-ui.html
- **API Docs**: http://localhost:8083/api-docs
- **Service Review**: See `REVIEW_REVISED.md`

## ✅ Collection Features

- ✅ All 19 controllers covered
- ✅ 100+ API endpoints
- ✅ Environment variables for easy configuration
- ✅ Request body examples with variables
- ✅ Proper authentication headers
- ✅ File upload support
- ✅ Organized by feature/controller

---

**Last Updated:** 2025-12-11  
**Collection Version:** 1.0

