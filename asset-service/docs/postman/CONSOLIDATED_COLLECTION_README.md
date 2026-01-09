# Asset Service - Consolidated Postman Collection

## 📦 Overview

This is a **complete consolidated Postman collection** that combines all Asset Service API collections into a single, comprehensive collection organized by **controller structure** with a unified environment file.

## 📁 Files

1. **`Asset_Service_Consolidated.postman_collection.json`** (212KB)
   - Complete consolidated collection organized by 20 controllers
   - 20 folders, 159 API requests
   - 28 collection variables (global variables)

2. **`Asset_Service_Consolidated_Environment.postman_environment.json`** (6.2KB)
   - Unified environment with all variables
   - 39 environment variables, logically sorted
   - Default values and descriptions included

3. **`CONSOLIDATED_COLLECTION_README.md`** (this file)
   - Complete documentation and usage guide

## 🎯 Controller-Based Organization

The collection is organized by **20 controllers** in the following order:

| # | Controller | Base Path | Requests | Description |
|---|-----------|-----------|----------|-------------|
| 1 | **AssetController** | `/api/asset/v1/assets` | 9 | Asset CRUD, Search, Bulk, Complete Creation |
| 2 | **CategoryController** | `/api/asset/v1/categories` | 7 | Category management |
| 3 | **SubCategoryController** | `/api/asset/v1/subcategories` | 7 | SubCategory management |
| 4 | **MakeController** | `/api/asset/v1/makes` | 7 | Make management |
| 5 | **ModelController** | `/api/asset/v1/models` | 7 | Model management |
| 6 | **VendorController** | `/api/asset/v1/vendors` | 7 | Vendor management |
| 7 | **OutletController** | `/api/asset/v1/outlets` | 7 | Outlet management |
| 8 | **ComponentController** | `/api/asset/v1/components` | 7 | Component management |
| 9 | **UserLinkController** | `/api/asset/v1/userlinks` | 27 | User linking, Master Data, Need Your Attention |
| 10 | **AssetWarrantyController** | `/api/asset/v1/warranty` | 5 | Warranty operations |
| 11 | **AssetAmcController** | `/api/asset/v1/amc` | 5 | AMC operations |
| 12 | **DocumentController** | `/api/asset/v1/documents` | 7 | Document upload/management |
| 13 | **FileDownloadController** | `/api/asset/v1/files` | 1 | File download by filename |
| 14 | **ComplianceController** | `/api/asset/v1/compliance` | 10 | Compliance validation, status, violations, reports, metrics |
| 15 | **ComplianceRuleController** | `/api/asset/v1/compliance/rules` | 14 | Compliance rules management |
| 16 | **EntityTypeController** | `/api/asset/v1/entity-types` | 5 | Entity type management |
| 17 | **StatusController** | `/api/asset/v1/statuses` | 6 | Status management |
| 18 | **MasterDataAgentController** | `/api/asset/v1/masters` | 7 | Master data agent operations |
| 19 | **UserAssetLinkAgentController** | `/api/asset/v1/user-asset-links` | 5 | User asset link agent operations |
| 20 | **AuditAgentController** | `/api/asset/v1/audit` | 9 | Audit logging and tracking |

**Total: 20 Controllers, 159 API Requests**

## 🔑 Environment Variables

### Base Configuration
- `assetbaseUrl` - Base URL (default: `http://localhost:8083`)
- `accessToken` - JWT Bearer token from auth-service (secret)

### User Context
- `userId` - Current user ID (default: `1`)
- `username` - Current username (default: `admin`)
- `projectType` - Project type (default: `ASSET_SERVICE`)

### Entity IDs
- `assetId`, `categoryId`, `subCategoryId`, `makeId`, `modelId`
- `vendorId`, `outletId`, `componentId`
- `warrantyId`, `amcId`, `documentId`
- `targetUserId`, `targetUsername`
- `entityId`, `entityTypeId`, `ruleId`, `violationId`, `statusId`

### Entity Types & Codes
- `entityType` - Entity type (ASSET, COMPONENT, etc.)
- `entityTypeCode` - Entity type code
- `statusCategory` - Status category
- `statusCode` - Status code

### Status & Dates
- `warrantyStatus`, `amcStatus` - Status values
- `warrantyProvider`, `warrantyTerms` - Warranty details
- `startDate`, `endDate` - Date values (format: `yyyy-MM-dd`)

### Document & File
- `docType` - Document type (PDF, IMAGE, etc.)
- `filename` - File name for download

### Search & Other
- `searchKeyword` - Search keyword
- `authToken` - Alternative token variable
- `assetId2` - Secondary asset ID

**Total: 39 Environment Variables**

## 📋 Collection Variables (Global)

The collection also includes **28 global variables** that can be used across all requests:

- `assetbaseUrl`, `accessToken` - Base configuration
- `userId`, `username`, `projectType` - User context
- All entity IDs and related variables
- Status, date, and search variables

These are defined at the collection level and can be overridden by environment variables.

## 🚀 Usage

### 1. Import into Postman

**Import Collection:**
1. Open Postman
2. Click **"Import"**
3. Select `Asset_Service_Consolidated.postman_collection.json`
4. Click **"Import"**

**Import Environment:**
1. Click **"Import"**
2. Select `Asset_Service_Consolidated_Environment.postman_environment.json`
3. Click **"Import"**
4. Select the environment in the top-right dropdown: **"Asset Service - Consolidated Environment"**

### 2. Configure Environment

**Set Base URL:**
- Update `assetbaseUrl` to your service URL (default: `http://localhost:8083`)

**Set Access Token:**
1. Login to auth-service using the Auth Service collection
2. Copy the `accessToken` from the response
3. Update the `accessToken` variable in the environment

**Update Entity IDs:**
- Set `assetId`, `categoryId`, `modelId`, etc. based on your test data
- These can be updated as you create entities

### 3. Start Testing

- Navigate through folders organized by controller
- Each folder contains all endpoints for that controller
- Request bodies use environment variables for easy customization
- All requests include Authorization header automatically

## ✨ Features

- ✅ **Complete Coverage** - All 159 endpoints from all 20 controllers
- ✅ **Controller-Based Organization** - Organized exactly as controllers are structured
- ✅ **Environment Variables** - 39 variables for easy configuration
- ✅ **Global Variables** - 28 collection-level variables
- ✅ **Consistent Naming** - All URLs use `{{assetbaseUrl}}`
- ✅ **Comprehensive Examples** - Request bodies with variable placeholders
- ✅ **Proper Sorting** - Environment variables logically sorted
- ✅ **No Duplicates** - All folders properly merged and deduplicated

## 📊 Statistics

- **Total Controllers:** 20
- **Total Folders:** 20
- **Total API Requests:** 159
- **Collection Variables:** 28
- **Environment Variables:** 39
- **File Size:** ~212KB (collection), ~6.2KB (environment)

## 🔧 Maintenance

To regenerate the consolidated collection:

```bash
cd asset-service/docs/postman
python3 consolidate_collections.py
python3 fix_and_enhance_collection.py
python3 final_reorganize.py
python3 add_missing_file_download.py
python3 sort_environment.py
```

This will:
1. Merge all individual collections
2. Fix variable names
3. Add missing endpoints
4. Reorganize by controller structure
5. Add FileDownloadController if missing
6. Sort environment variables logically

## 📝 Notes

- All URLs use `{{assetbaseUrl}}` variable (standardized)
- All requests include `Authorization: Bearer {{accessToken}}` header
- Request bodies use environment variables for dynamic values
- Bulk operations support both JSON and Excel uploads
- Document operations support multipart/form-data
- File download supports both inline view and attachment download

## 🎉 Ready to Use!

The consolidated collection is complete, properly organized by controller structure, and ready for testing all Asset Service APIs!
