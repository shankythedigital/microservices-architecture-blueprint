# Warranty & AMC Controllers - Postman Collection Guide

## Overview

This Postman collection provides comprehensive API testing for the `AssetWarrantyController` and `AssetAmcController` which handle warranty and AMC (Annual Maintenance Contract) operations for assets and components.

## Files

1. **Collection File**: `Warranty_AMC_Controllers.postman_collection.json`
2. **Environment File**: `Warranty_AMC_Environment.postman_environment.json`

## Controllers

### AssetWarrantyController
**Base Path**: `/api/asset/v1/warranty`

### AssetAmcController
**Base Path**: `/api/asset/v1/amc`

## Warranty Endpoints

### 1. Create Warranty
**POST** `/api/asset/v1/warranty`

Create a new warranty record for an asset or component.

**Request Body:**
```json
{
  "userId": {{userId}},
  "username": "{{username}}",
  "projectType": "{{projectType}}",
  "assetId": {{assetId}},
  "componentId": {{componentId}},
  "warrantyStatus": "{{warrantyStatus}}",
  "warrantyProvider": "{{warrantyProvider}}",
  "warrantyTerms": "{{warrantyTerms}}",
  "startDate": "{{startDate}}",
  "endDate": "{{endDate}}",
  "documentId": {{documentId}},
  "docType": "{{docType}}"
}
```

**Required Fields:**
- `userId` - User ID
- `username` - Username
- `assetId` - Asset ID
- `warrantyStatus` - Warranty status
- `startDate` - Start date (format: yyyy-MM-dd)
- `endDate` - End date (format: yyyy-MM-dd)

**Optional Fields:**
- `projectType` - Project type
- `componentId` - Component ID
- `warrantyProvider` - Warranty provider name
- `warrantyTerms` - Warranty terms description
- `documentId` - Document ID for warranty document
- `docType` - Document type

### 2. Update Warranty
**PUT** `/api/asset/v1/warranty/{warrantyId}`

Update an existing warranty record.

**Path Parameters:**
- `warrantyId` - Warranty ID (required)

**Request Body:** Same as Create Warranty (all fields optional for update)

### 3. Delete Warranty
**DELETE** `/api/asset/v1/warranty/{warrantyId}`

Soft delete a warranty record.

**Path Parameters:**
- `warrantyId` - Warranty ID (required)

**Request Body:**
```json
{
  "userId": {{userId}},
  "username": "{{username}}",
  "projectType": "{{projectType}}"
}
```

### 4. List All Warranties
**GET** `/api/asset/v1/warranty`

Get all warranty records.

**Response:** Returns a list of all warranty records.

### 5. Get Warranty by ID
**GET** `/api/asset/v1/warranty/{warrantyId}`

Get a specific warranty record by ID.

**Path Parameters:**
- `warrantyId` - Warranty ID (required)

**Response:** Returns warranty details if found, 404 if not found.

## AMC Endpoints

### 1. Create AMC
**POST** `/api/asset/v1/amc`

Create a new AMC (Annual Maintenance Contract) record for an asset or component.

**Request Body:**
```json
{
  "userId": {{userId}},
  "username": "{{username}}",
  "projectType": "{{projectType}}",
  "assetId": {{assetId}},
  "componentId": {{componentId}},
  "amcStatus": "{{amcStatus}}",
  "startDate": "{{startDate}}",
  "endDate": "{{endDate}}",
  "documentId": {{documentId}},
  "docType": "{{docType}}"
}
```

**Optional Fields:** All fields are optional for AMC creation.

### 2. Update AMC
**PUT** `/api/asset/v1/amc/{amcId}`

Update an existing AMC record.

**Path Parameters:**
- `amcId` - AMC ID (required)

**Request Body:** Same as Create AMC (all fields optional for update)

### 3. Delete AMC
**DELETE** `/api/asset/v1/amc/{amcId}`

Soft delete an AMC record.

**Path Parameters:**
- `amcId` - AMC ID (required)

**Request Body:**
```json
{
  "userId": {{userId}},
  "username": "{{username}}",
  "projectType": "{{projectType}}"
}
```

### 4. List All AMCs
**GET** `/api/asset/v1/amc`

Get all AMC records.

**Response:** Returns a list of all AMC records.

### 5. Get AMC by ID
**GET** `/api/asset/v1/amc/{amcId}`

Get a specific AMC record by ID.

**Path Parameters:**
- `amcId` - AMC ID (required)

**Response:** Returns AMC details if found, 404 if not found.

## Environment Variables

### Required Variables

- **assetbaseUrl**: Base URL for Asset Service (default: `http://localhost:8083`)
- **accessToken**: JWT Bearer token from auth-service login endpoint

### User Context Variables

- **userId**: User ID for operations (default: `1`)
- **username**: Username for operations (default: `admin`)
- **projectType**: Project type for operations (default: `ASSET_SERVICE`)

### Entity ID Variables

Update these after creating entities:

- **assetId**: Asset ID for linking warranty/AMC (default: `1`)
- **componentId**: Component ID for linking warranty/AMC (default: `1`)
- **warrantyId**: Warranty ID for operations (default: `1`)
- **amcId**: AMC ID for operations (default: `1`)

### Warranty-Specific Variables

- **warrantyStatus**: Warranty status (default: `ACTIVE`)
  - Common values: `ACTIVE`, `EXPIRED`, `PENDING`, `CANCELLED`
- **warrantyProvider**: Warranty provider name (default: `Dell Inc`)
- **warrantyTerms**: Warranty terms description (default: `3 years standard warranty`)

### AMC-Specific Variables

- **amcStatus**: AMC status (default: `ACTIVE`)
  - Common values: `ACTIVE`, `EXPIRED`, `PENDING`, `CANCELLED`

### Date Variables

- **startDate**: Start date in format yyyy-MM-dd (default: `2024-01-01`)
- **endDate**: End date in format yyyy-MM-dd (default: `2027-01-01`)

### Document Variables

- **documentId**: Document ID for warranty/AMC document (default: `1`)
- **docType**: Document type (default: `PDF`)
  - Common values: `PDF`, `IMAGE`, `RECEIPT`, `AGREEMENT`

## Setup Instructions

1. **Import Collection and Environment**:
   - Import `Warranty_AMC_Controllers.postman_collection.json`
   - Import `Warranty_AMC_Environment.postman_environment.json`
   - Select environment: **Warranty & AMC Controllers - Environment**

2. **Configure Environment Variables**:
   - Set `accessToken` after logging in via auth-service
   - Create assets/components first, then update `assetId` and `componentId`
   - Update `warrantyId` and `amcId` after creating warranty/AMC records
   - Adjust date values (`startDate`, `endDate`) as needed

3. **Test Workflow**:
   - First, create assets/components using AssetController/ComponentController
   - Update environment variables with created entity IDs
   - Create warranty/AMC records
   - Update warranty/AMC records
   - Query warranty/AMC records
   - Delete warranty/AMC records (soft delete)

## Request Body Examples

### Create Warranty
```json
{
  "userId": 1,
  "username": "admin",
  "projectType": "ASSET_SERVICE",
  "assetId": 101,
  "componentId": 501,
  "warrantyStatus": "ACTIVE",
  "warrantyProvider": "Dell Inc",
  "warrantyTerms": "3 years standard warranty covering hardware defects",
  "startDate": "2024-01-01",
  "endDate": "2027-01-01",
  "documentId": 1001,
  "docType": "PDF"
}
```

### Create AMC
```json
{
  "userId": 1,
  "username": "admin",
  "projectType": "ASSET_SERVICE",
  "assetId": 101,
  "componentId": 501,
  "amcStatus": "ACTIVE",
  "startDate": "2024-01-01",
  "endDate": "2025-01-01",
  "documentId": 1002,
  "docType": "PDF"
}
```

### Update Warranty
```json
{
  "userId": 1,
  "username": "admin",
  "warrantyStatus": "EXPIRED",
  "warrantyProvider": "Dell Inc Updated",
  "warrantyTerms": "Extended warranty terms"
}
```

### Delete Warranty/AMC
```json
{
  "userId": 1,
  "username": "admin",
  "projectType": "ASSET_SERVICE"
}
```

## Common Issues

### 401 Unauthorized
- Check `accessToken` is set correctly
- Ensure token is not expired
- Format: `Bearer <token>`

### 400 Bad Request
- Verify required fields are provided (for warranty: userId, username, assetId, warrantyStatus, startDate, endDate)
- Check date format is `yyyy-MM-dd`
- Ensure asset/component IDs exist

### 404 Not Found
- Verify warranty/AMC IDs exist
- Check warranty/AMC IDs in environment variables

### 500 Internal Server Error
- Check application logs for detailed error messages
- Verify asset/component relationships exist
- Ensure dates are valid (endDate should be after startDate)

## Date Format

All date fields must be in the format: **yyyy-MM-dd**

**Examples:**
- `2024-01-01` - January 1, 2024
- `2024-12-31` - December 31, 2024
- `2027-01-01` - January 1, 2027

## Status Values

### Warranty Status
- `ACTIVE` - Warranty is currently active
- `EXPIRED` - Warranty has expired
- `PENDING` - Warranty is pending activation
- `CANCELLED` - Warranty has been cancelled

### AMC Status
- `ACTIVE` - AMC is currently active
- `EXPIRED` - AMC has expired
- `PENDING` - AMC is pending activation
- `CANCELLED` - AMC has been cancelled

## Document Management

**Note:** Document uploads are handled separately via `DocumentController`. The `documentId` and `docType` fields in warranty/AMC requests are optional and used to link existing documents.

To upload documents:
1. Use `DocumentController` to upload warranty/AMC documents
2. Get the `documentId` from the upload response
3. Use the `documentId` when creating/updating warranty/AMC records

## Testing Workflow

1. **Setup**:
   - Create assets/components using respective controllers
   - Update environment variables with entity IDs

2. **Create Operations**:
   - Create warranty records using `/warranty` POST endpoint
   - Create AMC records using `/amc` POST endpoint
   - Update environment variables with created IDs

3. **Query Operations**:
   - List all warranties using `/warranty` GET endpoint
   - List all AMCs using `/amc` GET endpoint
   - Get specific warranty/AMC by ID

4. **Update Operations**:
   - Update warranty records using `/warranty/{id}` PUT endpoint
   - Update AMC records using `/amc/{id}` PUT endpoint

5. **Delete Operations**:
   - Soft delete warranty records using `/warranty/{id}` DELETE endpoint
   - Soft delete AMC records using `/amc/{id}` DELETE endpoint

## Support

For issues or questions:
1. Check controller code:
   - `asset-service/src/main/java/com/example/asset/controller/AssetWarrantyController.java`
   - `asset-service/src/main/java/com/example/asset/controller/AssetAmcController.java`
2. Review DTOs:
   - `AssetWarrantyRequest`
   - `AssetAmcRequest`
3. Check service implementations
4. Review application logs

