# User Link Controller - Postman Collection Guide

## Overview

This Postman collection provides comprehensive API testing for the `UserLinkController` which handles universal entity linking operations between users and various entities (ASSET, COMPONENT, MODEL, MAKE, AMC, WARRANTY, DOCUMENT).

## Files

1. **Collection File**: `UserLinkController.postman_collection.json`
2. **Environment File**: `UserLinkController_Environment.postman_environment.json`

## Controller Endpoints

**Base Path**: `/api/asset/v1/userlinks`

### 1. Universal Link Operations

#### POST `/link`
Link any entity type to a user using a universal endpoint.

**Supported Entity Types:**
- `ASSET` - Link an asset to a user
- `COMPONENT` - Link a component to a user
- `MODEL` - Link a model to a user
- `MAKE` - Link a make to a user
- `AMC` - Link an AMC to a user
- `WARRANTY` - Link a warranty to a user
- `DOCUMENT` - Link a document to a user

**Request Body:**
```json
{
  "userId": {{userId}},
  "username": "{{username}}",
  "entityType": "ASSET",
  "entityId": {{assetId}},
  "targetUserId": {{targetUserId}},
  "targetUsername": "{{targetUsername}}"
}
```

### 2. Universal Delink Operations

#### POST `/delink`
Delink any entity type from a user using a universal endpoint.

**Request Body:**
```json
{
  "userId": {{userId}},
  "username": "{{username}}",
  "entityType": "ASSET",
  "entityId": {{assetId}},
  "targetUserId": {{targetUserId}},
  "targetUsername": "{{targetUsername}}"
}
```

### 3. Multi-Entity Link Operations

#### POST `/link-multiple`
Link a user to multiple entities in a single request.

**Two Request Formats Supported:**

**Format 1: Typed Fields (Recommended)**
```json
{
  "userId": {{userId}},
  "username": "{{username}}",
  "targetUserId": {{targetUserId}},
  "targetUsername": "{{targetUsername}}",
  "assetId": {{assetId}},
  "componentId": {{componentId}},
  "modelId": {{modelId}},
  "makeId": {{makeId}},
  "amcId": {{amcId}},
  "warrantyId": {{warrantyId}},
  "documentId": {{documentId}}
}
```

**Format 2: String Format (Backward Compatible)**
```json
{
  "userId": {{userId}},
  "username": "{{username}}",
  "targetUserId": {{targetUserId}},
  "targetUsername": "{{targetUsername}}",
  "entityLinks": [
    "ASSET:{{assetId}}",
    "COMPONENT:{{componentId}}",
    "MODEL:{{modelId}}",
    "MAKE:{{makeId}}",
    "AMC:{{amcId}}",
    "WARRANTY:{{warrantyId}}",
    "DOCUMENT:{{documentId}}"
  ]
}
```

### 4. Multi-Entity Delink Operations

#### POST `/delink-multiple`
Delink a user from multiple entities in a single request.

**Two Request Formats Supported:**

**Format 1: Typed Fields (Recommended)**
```json
{
  "userId": {{userId}},
  "username": "{{username}}",
  "targetUserId": {{targetUserId}},
  "targetUsername": "{{targetUsername}}",
  "assetId": {{assetId}},
  "componentId": {{componentId}},
  "modelId": {{modelId}},
  "makeId": {{makeId}},
  "amcId": {{amcId}},
  "warrantyId": {{warrantyId}},
  "documentId": {{documentId}}
}
```

**Format 2: String Format (Backward Compatible)**
```json
{
  "userId": {{userId}},
  "username": "{{username}}",
  "targetUserId": {{targetUserId}},
  "targetUsername": "{{targetUsername}}",
  "entityLinks": [
    "ASSET:{{assetId}}",
    "COMPONENT:{{componentId}}",
    "MODEL:{{modelId}}",
    "MAKE:{{makeId}}",
    "AMC:{{amcId}}",
    "WARRANTY:{{warrantyId}}",
    "DOCUMENT:{{documentId}}"
  ]
}
```

### 5. Query Operations

#### GET `/assigned-assets?targetUserId={targetUserId}`
Get all assets assigned to a specific user.

**Query Parameters:**
- `targetUserId` (required): Target user ID

#### GET `/asset?assetId={assetId}&componentId={componentId}`
Get link information for a single asset or component.

**Query Parameters:**
- `assetId` (optional): Asset ID
- `componentId` (optional): Component ID

**Note:** At least one of `assetId` or `componentId` must be provided.

#### GET `/by-subcategory?subCategoryId={subCategoryId}`
Get all users linked to assets in a specific subcategory.

**Query Parameters:**
- `subCategoryId` (required): SubCategory ID

## Environment Variables

### Required Variables

- **assetbaseUrl**: Base URL for Asset Service (default: `http://localhost:8083`)
- **accessToken**: JWT Bearer token from auth-service login endpoint

### User Context Variables

- **userId**: Caller user ID (default: `1`)
- **username**: Caller username (default: `admin`)
- **targetUserId**: Target user ID for linking operations (default: `2`)
- **targetUsername**: Target username for linking operations (default: `user1`)

### Entity ID Variables

Update these after creating entities:

- **assetId**: Asset ID (default: `1`)
- **componentId**: Component ID (default: `1`)
- **modelId**: Model ID (default: `1`)
- **makeId**: Make ID (default: `1`)
- **amcId**: AMC ID (default: `1`)
- **warrantyId**: Warranty ID (default: `1`)
- **documentId**: Document ID (default: `1`)
- **subCategoryId**: SubCategory ID (default: `1`)

### Operation-Specific Variables

- **entityType**: Entity type for universal operations (default: `ASSET`)
- **entityId**: Entity ID for universal operations (default: `1`)

## Setup Instructions

1. **Import Collection and Environment**:
   - Import `UserLinkController.postman_collection.json`
   - Import `UserLinkController_Environment.postman_environment.json`
   - Select environment: **User Link Controller - Environment**

2. **Configure Environment Variables**:
   - Set `accessToken` after logging in via auth-service
   - Update entity IDs (`assetId`, `componentId`, etc.) after creating entities
   - Update `targetUserId` and `targetUsername` as needed

3. **Test Workflow**:
   - First, create entities (assets, components, etc.) using respective controllers
   - Update environment variables with created entity IDs
   - Test link operations
   - Test query operations to verify links
   - Test delink operations

## Request Body Examples

### Link Asset to User
```json
{
  "userId": 1,
  "username": "admin",
  "entityType": "ASSET",
  "entityId": 101,
  "targetUserId": 2,
  "targetUsername": "user1"
}
```

### Link Multiple Entities (Typed Fields)
```json
{
  "userId": 1,
  "username": "admin",
  "targetUserId": 2,
  "targetUsername": "user1",
  "assetId": 101,
  "componentId": 501,
  "modelId": 201
}
```

### Link Multiple Entities (String Format)
```json
{
  "userId": 1,
  "username": "admin",
  "targetUserId": 2,
  "targetUsername": "user1",
  "entityLinks": [
    "ASSET:101",
    "COMPONENT:501",
    "MODEL:201"
  ]
}
```

## Common Issues

### 401 Unauthorized
- Check `accessToken` is set correctly
- Ensure token is not expired
- Format: `Bearer <token>`

### 400 Bad Request
- Verify entity IDs exist
- Check entity type is valid (ASSET, COMPONENT, MODEL, MAKE, AMC, WARRANTY, DOCUMENT)
- Ensure target user exists

### 404 Not Found
- Verify entity IDs exist
- Check entity IDs in environment variables

## Entity Type Reference

| Entity Type | Description | Example ID Variable |
|------------|-------------|-------------------|
| ASSET | Asset entity | `{{assetId}}` |
| COMPONENT | Component entity | `{{componentId}}` |
| MODEL | Model entity | `{{modelId}}` |
| MAKE | Make entity | `{{makeId}}` |
| AMC | AMC entity | `{{amcId}}` |
| WARRANTY | Warranty entity | `{{warrantyId}}` |
| DOCUMENT | Document entity | `{{documentId}}` |

## Testing Workflow

1. **Setup**:
   - Create entities (assets, components, etc.)
   - Update environment variables with entity IDs

2. **Link Operations**:
   - Link individual entities using `/link`
   - Link multiple entities using `/link-multiple`

3. **Query Operations**:
   - Get assigned assets using `/assigned-assets`
   - Get single asset link info using `/asset`
   - Get users by subcategory using `/by-subcategory`

4. **Delink Operations**:
   - Delink individual entities using `/delink`
   - Delink multiple entities using `/delink-multiple`

## Support

For issues or questions:
1. Check controller code: `asset-service/src/main/java/com/example/asset/controller/UserLinkController.java`
2. Review DTOs: `AssetUserUniversalLinkRequest`, `AssetUserMultiLinkRequest`, `AssetUserMultiDelinkRequest`
3. Check service implementations
4. Review application logs

