# Postman Collection & Swagger/OpenAPI Guide

## Overview

This guide covers the consolidated Postman collection and Swagger/OpenAPI specifications for all microservices.

## Postman Collection

**Location:** `Deployment/Postman Collection/`

| File | Description |
|------|--------------|
| `Microservices_Complete_API_Collection.postman_collection.json` | Consolidated API collection for all services (188 requests) |
| `Microservices_Consolidated_Environment.postman_environment.json` | Environment variables for all services |
| `1. Auth_Service_API.postman_collection.json` | Auth Service only (29 requests) |
| `2. Asset Service - Complete API Collection.postman_collection.json` | Asset Service only (99 requests) |
| `3. Notification_Service_API.postman_collection.json` | Notification Service only (11 requests) |
| `4. Helpdesk_Service_Complete_API_Collection.postman_collection.json` | Helpdesk Service only (49 requests) |

**Environments:**

| File | Description |
|------|-------------|
| `Auth Service - Local.postman_environment.json` | Auth Service (localhost:8080) |
| `Auth_Service_Environment.postman_environment.json` | Auth Service (same as Local) |
| `Asset Service - Consolidated Environment.postman_environment.json` | Asset Service (localhost:7072) |
| `Notification Service - Local.postman_environment.json` | Notification Service (localhost:7071) |
| `Helpdesk Service - Local.postman_environment.json` | Helpdesk Service (localhost:7074) |

### Import Instructions

1. Open Postman
2. **Import Collection:** File → Import → Select `Microservices_Complete_API_Collection.postman_collection.json`
3. **Import Environment:** File → Import → Select `Microservices_Consolidated_Environment.postman_environment.json`
4. Select "Microservices - Consolidated Environment" from the environment dropdown (top right)

### Quick Start

1. **Login first:** Run `1. Auth Service` → `Login (Password)` to get JWT token
2. **Set token:** Copy `accessToken` from response → Environment → `bearerToken` → Save
3. All other requests will automatically use the token

### Services in Collection

| Folder | Base URL (Local) | Port |
|--------|------------------|------|
| 1. Auth Service | http://localhost:8080 | 8080 |
| 2. Notification Service | http://localhost:7071 | 7071 |
| 3. Asset Service | http://localhost:7072 | 7072 |
| 4. Helpdesk Service | http://localhost:7074 | 7074 |

---

## Swagger/OpenAPI Specifications

All OpenAPI specs include request/response examples from the Swagger definitions.

| Service | OpenAPI File | Swagger UI (when service running) |
|---------|--------------|----------------------------------|
| **Auth Service** | `auth-service/docs/swagger/auth_openapi.yaml` | http://localhost:8080/swagger-ui.html |
| **Notification Service** | `notification-service/docs/swagger/notification_openapi.yaml` | http://localhost:7071/swagger-ui.html |
| **Asset Service** | `asset-service/docs/swagger/asset_openapi.yaml` | http://localhost:7072/swagger-ui.html |
| **Helpdesk Service** | `helpdesk-service/docs/swagger/helpdesk_openapi.yaml` | http://localhost:7074/swagger-ui.html |

### OpenAPI Spec Paths (Relative to Project Root)

```
asset-service/docs/swagger/asset_openapi.yaml
auth-service/docs/swagger/auth_openapi.yaml
notification-service/docs/swagger/notification_openapi.yaml
helpdesk-service/docs/swagger/helpdesk_openapi.yaml
```

---

## Swagger Examples by Service

### Auth Service (auth_openapi.yaml)

**Register Request Example:**
```json
{
  "username": "john.doe",
  "password": "SecurePass123!",
  "email": "john.doe@example.com",
  "mobile": "+1234567890",
  "projectType": "ECOM"
}
```

**Login Request Example (PASSWORD):**
```json
{
  "loginType": "PASSWORD",
  "username": "john.doe",
  "password": "SecurePass123!"
}
```

**Auth Response Example:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 1,
  "username": "john.doe",
  "roles": ["USER"]
}
```

---

### Notification Service (notification_openapi.yaml)

**SMS Example:**
```json
{
  "channel": "SMS",
  "templateCode": "OTP_SMS",
  "recipient": "+1234567890",
  "variables": {
    "otp": "123456",
    "expiry": "3"
  }
}
```

**Email Example:**
```json
{
  "channel": "EMAIL",
  "templateCode": "WELCOME_EMAIL",
  "recipient": "user@example.com",
  "variables": {
    "name": "John Doe",
    "activationLink": "https://example.com/activate"
  }
}
```

**WhatsApp Example:**
```json
{
  "channel": "WHATSAPP",
  "templateCode": "ORDER_CONFIRMATION",
  "recipient": "+1234567890",
  "variables": {
    "orderId": "ORD-12345",
    "amount": "99.99"
  }
}
```

---

### Asset Service (asset_openapi.yaml)

**AssetScanRequest Example:**
```json
{
  "scanValue": "AST-LAP-001",
  "scanType": "AUTO"
}
```

**AssetRequest Example:**
```json
{
  "categoryId": 1,
  "subCategoryId": 1,
  "makeId": 1,
  "modelId": 1,
  "assetNameUdv": "AST-LAP-001",
  "serialNumber": "SN123456"
}
```

**AssetScanResponse Example:**
```json
{
  "success": true,
  "message": "Asset found",
  "data": {
    "assetId": 1,
    "assetNameUdv": "AST-LAP-001",
    "serialNumber": "SN123456",
    "assetStatus": "ACTIVE",
    "categoryName": "Electronics",
    "makeName": "Dell",
    "modelName": "XPS 15",
    "matchedBy": "ASSET_NAME_UDV"
  }
}
```

---

### Helpdesk Service (helpdesk_openapi.yaml)

**IssueRequest Example:**
```json
{
  "title": "Network connectivity issue",
  "description": "Unable to connect to network",
  "priority": "HIGH",
  "relatedService": "ASSET_SERVICE"
}
```

**QueryRequest Example:**
```json
{
  "question": "How do I create an asset?",
  "relatedService": "ASSET_SERVICE"
}
```

**FAQRequest Example:**
```json
{
  "question": "How do I reset my password?",
  "answer": "Click on forgot password link",
  "relatedService": "ASSET_SERVICE",
  "category": "Authentication"
}
```

**EscalationMatrixRequest Example:**
```json
{
  "relatedService": "ASSET_SERVICE",
  "priority": "HIGH",
  "supportLevel": "L1",
  "initialAssignmentLevel": "L1",
  "responseTimeMinutes": 30,
  "resolutionTimeMinutes": 240
}
```

---

## Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| authbaseUrl | http://localhost:8080 | Auth Service URL |
| assetbaseUrl | http://localhost:7072 | Asset Service URL |
| notificationbaseUrl | http://localhost:7071 | Notification Service URL |
| helpdeskbaseUrl | http://localhost:7074 | Helpdesk Service URL |
| bearerToken | (empty) | JWT from login - set after Login |
| accessToken | (empty) | Same as bearerToken |
| refreshToken | (empty) | For token refresh |
| userId | 1 | Current user ID |
| username | john.doe | Username |
| password | SecurePass123! | Password |
| projectType | ASSET_SERVICE | Project type |
| scanValue | AST-LAP-001 | QR/Barcode for asset scan |
| issueId, queryId, faqId | 1 | Helpdesk entity IDs |
| channel | SMS | Notification channel |
| templateCode | OTP_SMS | Notification template |
| recipient | +919876543210 | Notification recipient |

---

## Regenerating the Collection

To regenerate the Postman collection and environment:

```bash
python3 generate_consolidated_postman.py
```

This updates:
- `Deployment/Postman Collection/Microservices_Complete_API_Collection.postman_collection.json`
- `Deployment/Postman Collection/Microservices_Consolidated_Environment.postman_environment.json`
