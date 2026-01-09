# Postman Collections Guide
## Complete Setup with Environment Variables and Examples

**Version:** 1.0.0  
**Date:** 2024-01-15

---

## Overview

This guide provides step-by-step instructions for setting up and using all Postman collections with their environment variables and example requests/responses.

---

## Quick Start

### 1. Import Collections

1. Open Postman
2. Click **Import** (top left)
3. Import the following collections:
   - `auth-service/docs/postman/Auth_Service_API.postman_collection.json`
   - `asset-service/docs/postman/Asset_Service_API.postman_collection.json`
   - `asset-service/docs/postman/Master_Data_API.postman_collection.json`
   - `asset-service/docs/postman/Complete_Asset_Creation_API.postman_collection.json`
   - `notification-service/docs/postman/Notification_Service_API.postman_collection.json`

### 2. Import Environments

1. In Postman, click **Environments** (left sidebar)
2. Click **Import**
3. Import the following environments:
   - `auth-service/docs/postman/Auth_Service_Environment.postman_environment.json`
   - `asset-service/docs/postman/Asset_Service_Environment.postman_environment.json`
   - `notification-service/docs/postman/Notification_Service_Environment.postman_environment.json`

### 3. Select Environment

1. Click the environment dropdown (top right)
2. Select **"Auth Service - Local"** to start

---

## Auth Service Collection

### Environment Variables

**Environment:** `Auth Service - Local`

| Variable | Default Value | Description |
|----------|--------------|-------------|
| `authbaseUrl` | `http://localhost:8080` | Base URL for Auth Service |
| `accessToken` | *(empty)* | JWT token (auto-populated after login) |
| `refreshToken` | *(empty)* | Refresh token (auto-populated after login) |
| `projectType` | `ASSET_SERVICE` | Project type code |
| `userId` | `1` | User ID for operations |
| `username` | `john.doe` | Username for login/registration |
| `password` | `SecurePass123!` | Password for login |
| `email` | `john.doe@example.com` | Email address |
| `mobile` | `9876543210` | Mobile number |
| `countryCode` | `+91` | Country code |
| `pincode` | `400001` | Postal code |
| `city` | `Mumbai` | City name |
| `state` | `Maharashtra` | State name |
| `country` | `India` | Country name |
| `otp` | *(empty)* | OTP code |
| `mpin` | `1234` | MPIN |
| `language` | `en` | Language code |

### Example Workflow

1. **Register User**
   - Use: `1. Registration > Register User (Complete)`
   - Update variables if needed
   - Execute request
   - Response: `{"message": "User registered successfully", "username": "john.doe"}`

2. **Login**
   - Use: `2. Login > Login with Password`
   - Execute request
   - Copy `accessToken` and `refreshToken` from response
   - Manually set in environment (or use Postman scripts to auto-set)

3. **Get User Profile**
   - Use: `5. User Profile > Get Current User Profile`
   - Token is automatically used from environment
   - Execute request

### Example Requests

#### Register User (Complete)
```json
POST {{authbaseUrl}}/api/auth/register
Content-Type: application/json

{
  "username": "{{username}}",
  "password": "{{password}}",
  "email": "{{email}}",
  "mobile": "{{mobile}}",
  "countryCode": "{{countryCode}}",
  "projectType": "{{projectType}}",
  "pincode": "{{pincode}}",
  "city": "{{city}}",
  "state": "{{state}}",
  "country": "{{country}}",
  "acceptTc": true
}
```

**Example Response:**
```json
{
  "message": "User registered successfully",
  "username": "john.doe"
}
```

#### Login with Password
```json
POST {{authbaseUrl}}/api/auth/login
Content-Type: application/json

{
  "loginType": "PASSWORD",
  "username": "{{username}}",
  "password": "{{password}}"
}
```

**Example Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": 1,
  "username": "john.doe",
  "roles": ["USER"]
}
```

---

## Asset Service Collection

### Environment Variables

**Environment:** `Asset Service - Local`

| Variable | Default Value | Description |
|----------|--------------|-------------|
| `assetbaseUrl` | `http://localhost:8083` | Base URL for Asset Service |
| `accessToken` | *(empty)* | JWT token from auth-service |
| `userId` | `1` | Current user ID |
| `username` | `admin` | Current username |
| `projectType` | `ASSET_SERVICE` | Project type |
| `assetId` | `1` | Asset ID |
| `categoryId` | `1` | Category ID |
| `subCategoryId` | `1` | Sub-category ID |
| `makeId` | `1` | Make ID |
| `modelId` | `1` | Model ID |
| `vendorId` | `1` | Vendor ID |
| `outletId` | `1` | Outlet ID |
| `componentId` | `1` | Component ID |
| `warrantyId` | `1` | Warranty ID |
| `amcId` | `1` | AMC ID |
| `documentId` | `1` | Document ID |
| `targetUserId` | `2` | Target user ID for linking |
| `targetUsername` | `user1` | Target username |
| `entityType` | `ASSET` | Entity type |
| `entityId` | `1` | Entity ID |

### Example Workflow

1. **Get Access Token**
   - First, login via Auth Service
   - Copy `accessToken`
   - Set in Asset Service environment

2. **Create Category**
   - Use: `Master Data > Categories > Create Category`
   - Execute request

3. **Create Asset**
   - Use: `Assets > Create Asset`
   - Update variables as needed
   - Execute request

4. **Link Asset to User**
   - Use: `User Links > Link Entity to User`
   - Execute request

### Example Requests

#### Create Asset
```json
POST {{assetbaseUrl}}/api/asset/v1/assets
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "assetNameUdv": "Laptop-001",
  "modelId": {{modelId}},
  "categoryId": {{categoryId}},
  "subCategoryId": {{subCategoryId}},
  "makeId": {{makeId}},
  "serialNumber": "SN123456",
  "purchaseDate": "2024-01-15"
}
```

**Example Response:**
```json
{
  "success": true,
  "message": "Asset created successfully",
  "data": {
    "assetId": 1,
    "assetNameUdv": "Laptop-001",
    "serialNumber": "SN123456",
    "purchaseDate": "2024-01-15",
    "assetStatus": "ACTIVE"
  }
}
```

#### Complete Asset Creation (One-Go)
```
POST {{assetbaseUrl}}/api/asset/v1/assets/complete
Authorization: Bearer {{accessToken}}
Content-Type: multipart/form-data

userId: {{userId}}
username: {{username}}
assetNameUdv: Laptop-001
modelId: {{modelId}}
serialNumber: SN123456
warrantyStartDate: 2024-01-15
warrantyEndDate: 2025-01-15
targetUserId: {{targetUserId}}
purchaseInvoice: <file>
```

**Example Response:**
```json
{
  "success": true,
  "message": "✅ Asset created successfully with warranty, document, and user assignment",
  "data": {
    "assetId": 1,
    "warrantyId": 1,
    "documentId": 1,
    "userLinkId": 1,
    "assetNameUdv": "Laptop-001",
    "serialNumber": "SN123456"
  }
}
```

#### Need Your Attention API
```
GET {{assetbaseUrl}}/api/asset/v1/userlinks/need-your-attention
Authorization: Bearer {{accessToken}}
```

**Example Response:**
```json
{
  "success": true,
  "message": "Need Your Attention data retrieved successfully",
  "data": {
    "users": [...],
    "assets": [...],
    "components": [...],
    "warranties": [...],
    "amcs": [...],
    "summary": {
      "totalUsers": 10,
      "totalAssets": 50,
      "totalComponents": 20
    },
    "attention": {
      "expiringWarranties": [...],
      "expiringWarrantiesCount": 5,
      "expiringAmcs": [...],
      "expiringAmcsCount": 3,
      "assetsWithoutWarranty": [...],
      "assetsWithoutWarrantyCount": 10,
      "unassignedAssets": [...],
      "unassignedAssetsCount": 12
    }
  }
}
```

---

## Notification Service Collection

### Environment Variables

**Environment:** `Notification Service - Local`

| Variable | Default Value | Description |
|----------|--------------|-------------|
| `notificationbaseUrl` | `http://localhost:8082` | Base URL for Notification Service |
| `accessToken` | *(empty)* | JWT token from auth-service |
| `templateCode` | `OTP_SMS` | Template code |
| `recipient` | `+919876543210` | Recipient address |
| `mobile` | `+919876543210` | Mobile number |
| `email` | `user@example.com` | Email address |
| `userId` | `123` | User ID for in-app |

### Example Workflow

1. **Get Access Token**
   - Login via Auth Service
   - Copy `accessToken`
   - Set in Notification Service environment

2. **Send SMS Notification**
   - Use: `1. SMS Notifications > Send SMS - OTP`
   - Update variables
   - Execute request

3. **Send Email Notification**
   - Use: `2. Email Notifications > Send Email - Welcome`
   - Execute request

### Example Requests

#### Send SMS - OTP
```json
POST {{notificationbaseUrl}}/api/notifications
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "channel": "SMS",
  "templateCode": "OTP_SMS",
  "recipient": "{{mobile}}",
  "variables": {
    "otp": "123456",
    "expiry": "3"
  },
  "priority": "HIGH"
}
```

**Example Response:**
```
SMS Notification accepted
```
*(Status: 202 Accepted)*

#### Send Email - Welcome
```json
POST {{notificationbaseUrl}}/api/notifications
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "channel": "EMAIL",
  "templateCode": "WELCOME_EMAIL",
  "recipient": "{{email}}",
  "variables": {
    "name": "John Doe",
    "activationLink": "https://example.com/activate?token=abc123"
  },
  "priority": "NORMAL",
  "metadata": {
    "source": "auth-service",
    "eventType": "USER_REGISTERED"
  }
}
```

**Example Response:**
```
EMAIL Notification accepted
```
*(Status: 202 Accepted)*

#### Send In-App - Asset Assignment
```json
POST {{notificationbaseUrl}}/api/notifications
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
  "channel": "INAPP",
  "templateCode": "ASSIGNMENT_NOTIFICATION",
  "recipient": "{{userId}}",
  "variables": {
    "assetName": "Laptop-001",
    "assignedBy": "Admin",
    "assignedDate": "2024-01-15"
  },
  "priority": "HIGH",
  "metadata": {
    "source": "asset-service",
    "eventType": "ASSET_ASSIGNED",
    "assetId": "1"
  }
}
```

**Example Response:**
```
INAPP Notification accepted
```
*(Status: 202 Accepted)*

---

## Global Variables (Collection Level)

All collections include global variables at the collection level:

### Auth Service
- `authbaseUrl`: `http://localhost:8081`
- `accessToken`: *(empty)*
- `refreshToken`: *(empty)*
- `projectType`: `ASSET_SERVICE`

### Asset Service
- `assetbaseUrl`: `http://localhost:8083`
- `accessToken`: *(empty)*
- Various entity IDs

### Notification Service
- `notificationbaseUrl`: `http://localhost:8082`
- `accessToken`: *(empty)*
- `templateCode`: `OTP_SMS`
- `recipient`: `+919876543210`

---

## Auto-Populating Tokens

### Option 1: Manual (Recommended for Testing)

1. Execute login request
2. Copy `accessToken` from response
3. Paste into environment variable `accessToken`

### Option 2: Automatic (Using Postman Scripts)

Add this script to the login request's **Tests** tab:

```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.accessToken) {
        pm.environment.set("accessToken", jsonData.accessToken);
    }
    if (jsonData.refreshToken) {
        pm.environment.set("refreshToken", jsonData.refreshToken);
    }
}
```

---

## Common Patterns

### 1. Testing Complete Flow

1. **Auth Service**: Register → Login → Get Profile
2. **Asset Service**: Create Category → Create Asset → Link to User
3. **Notification Service**: Send notification

### 2. Using Variables

All requests use environment variables:
- `{{authbaseUrl}}` - Base URL
- `{{accessToken}}` - JWT token
- `{{userId}}` - User ID
- `{{assetId}}` - Asset ID
- etc.

### 3. Error Handling

Common error responses:
- **401 Unauthorized**: Token expired or invalid → Refresh token
- **400 Bad Request**: Invalid input → Check request body
- **404 Not Found**: Resource doesn't exist → Check IDs

---

## Environment Setup

### Local Development

Use these environment files:
- `Auth Service - Local`
- `Asset Service - Local`
- `Notification Service - Local`

### Production

Create new environments with:
- Production URLs
- Production tokens
- Production IDs

---

## Tips

1. **Save Responses**: Use Postman's "Save Response" feature to save example responses
2. **Use Folders**: Collections are organized in folders for easy navigation
3. **Set Variables**: Update environment variables as needed for different scenarios
4. **Use Examples**: Each request includes example responses
5. **Test Scripts**: Add test scripts to validate responses automatically

---

## Troubleshooting

### Issue: 401 Unauthorized

**Solution:**
1. Check if `accessToken` is set in environment
2. Verify token hasn't expired
3. Re-login to get new token

### Issue: Variables Not Resolving

**Solution:**
1. Ensure environment is selected (top right dropdown)
2. Check variable names match exactly (case-sensitive)
3. Verify variable is enabled in environment

### Issue: Connection Refused

**Solution:**
1. Verify service is running
2. Check port numbers match environment variables
3. Verify firewall settings

---

## Additional Resources

- **Technical Functional Document**: `docs/TECHNICAL_FUNCTIONAL_DOCUMENT.md`
- **API Documentation Guide**: `docs/API_DOCUMENTATION_GUIDE.md`
- **Swagger UI**: Access at service URLs + `/swagger-ui.html`

---

**Last Updated:** 2024-01-15  
**Version:** 1.0.0

