# Swagger/OpenAPI Documentation Summary

This document provides a detailed overview of the Swagger/OpenAPI specifications for all four microservices in the Asset Lifecycle Management platform.

## Postman Collection (Latest)

**Consolidated collection and environment** with all services and Swagger examples:

| File | Location |
|------|----------|
| **Collection** | `Deployment/Postman Collection/Microservices_Complete_API_Collection.postman_collection.json` |
| **Environment** | `Deployment/Postman Collection/Microservices_Consolidated_Environment.postman_environment.json` |

See [POSTMAN_AND_SWAGGER_GUIDE.md](POSTMAN_AND_SWAGGER_GUIDE.md) for import instructions and Swagger examples.

---

## 1. Asset Service API

**File:** `asset-service/docs/swagger/openapi.yaml`  
**Base URL:** `http://localhost:7072`  
**Version:** 1.0.0

### Overview
Comprehensive asset lifecycle management including CRUD operations, master data, user-asset linking, compliance, documents, warranty, AMC, and audit logging.

### API Tags & Endpoints

| Tag | Endpoints | Description |
|-----|-----------|-------------|
| **Asset Scanning** | POST/GET `/api/asset/v1/scan`, POST `/api/asset/v1/scan/save`, POST/GET `/api/asset/v1/scan/qr`, POST `/api/asset/v1/scan/qr/image` | QR/barcode scan for mobile, asset lookup by ID/name/serial, scan & create, image upload |
| **Assets** | POST/GET `/api/asset/v1/assets`, GET/PUT/DELETE `/api/asset/v1/assets/{id}` | Asset CRUD, pagination, soft delete |
| **Master Data** | Categories, Subcategories, Makes, Models | Full CRUD + bulk upload + Excel upload |
| **Vendors** | `/api/asset/v1/vendors` | CRUD, bulk create, Excel upload |
| **Outlets** | `/api/asset/v1/outlets` | Purchase outlet management |
| **Components** | `/api/asset/v1/components` | Asset component management |
| **User Links** | `/api/asset/v1/userlinks/link`, `delink`, `multi-link`, `multi-delink`, `user/{userId}/assets` | Link/delink entities to users |
| **Compliance** | `/api/asset/v1/compliance/check`, `status/{entityType}/{entityId}`, `violations`, `metrics` | Compliance validation & metrics |
| **Documents** | POST `/api/asset/v1/documents`, GET/DELETE `/{id}`, GET `entity/{entityType}/{entityId}` | Document upload, retrieval by entity |
| **Warranty** | POST/GET/PUT `/api/asset/v1/warranty`, `/{id}` | Warranty management |
| **AMC** | POST/GET/PUT `/api/asset/v1/amc`, `/{id}` | Annual Maintenance Contract |
| **Status** | GET `/api/asset/v1/statuses`, `active`, `category/{category}`, `code/{code}` | Status master data |
| **Entity Types** | GET `/api/asset/v1/entity-types`, `active`, `code/{code}` | Entity type master data |
| **Audit** | GET `/api/asset/v1/audit`, `username/{username}`, `date-range`, `statistics` | Audit logs & statistics |

### Key Schemas
- **AssetScanRequest**: scanValue, scanType (QR/BARCODE/AUTO) - for mobile QR/barcode scanner
- **AssetScanResponse**: assetId, assetNameUdv, serialNumber, categoryName, subCategoryName, makeName, modelName, matchedBy
- **AssetRequest**: categoryId, subCategoryId, makeId, modelId, assetNameUdv, userId, username, projectType
- **AssetMaster**: assetId, assetNameUdv, category, subCategory, make, model
- **ComplianceCheckRequest/Result**: entityType, entityId, compliant, violations, metrics
- **AssetUserUniversalLinkRequest**: entityType (ASSET, COMPONENT, MODEL, MAKE, AMC, WARRANTY, DOCUMENT), entityId, targetUserId, targetUsername

### Security
- **bearerAuth** (JWT)

---

## 2. Auth Service API

**File:** `auth-service/docs/swagger/openapi.yaml`  
**Base URL:** `http://localhost:8080`  
**Version:** 1.0.0

### Overview
Authentication and authorization with multiple methods: password, OTP, MPIN, RSA, WebAuthn/Passkey, and auth code. Includes user registration, credential management, and project types.

### API Tags & Endpoints

| Tag | Endpoints | Description |
|-----|-----------|-------------|
| **Authentication** | POST `/api/auth/register`, `adminregister`, `login`, `refresh` | User/admin registration, login, token refresh |
| **Authentication** | POST `/api/auth/otp/send` | Send OTP via SMS or Email |
| **Authentication** | POST `/api/auth/contact/change/request`, `contact/change/confirm` | Change email/mobile with OTP |
| **Authentication** | POST `/api/auth/password/change`, `password/forgot` | Password management |
| **Credentials** | POST `/api/auth/credential/register` | Register RSA, WebAuthn, MPIN credentials |
| **Credentials** | GET `/api/auth/credential/rsa/challenge/{userId}` | Get RSA challenge |
| **Credentials** | POST `/api/auth/credential/rsa/verify` | Verify RSA signature |
| **Credentials** | GET `/api/auth/credential/webauthn/challenge/{userId}` | Get WebAuthn challenge |
| **Credentials** | POST `/api/auth/credential/webauthn/verify` | Verify WebAuthn response |
| **Credentials** | POST `/api/auth/mpin/register`, `mpin/verify`, `mpin/reset/request`, `mpin/reset/confirm` | MPIN lifecycle |
| **Users** | GET `/api/users/me`, `/api/users/{id}` | Current user profile, user by ID |
| **Admin** | GET `/api/admin/users` | List all users |
| **Admin** | GET `/api/admin/audit/logs`, `logs/paged`, `logs/csv`, `logs/excel` | Audit logs with filters, pagination, export |
| **Project Types** | GET/POST `/api/auth/v1/project-types`, GET/PUT/DELETE `/{projectTypeId}`, GET `code/{code}`, `validate/{code}` | Project type CRUD |

### Login Types
- **PASSWORD**: username + password
- **OTP**: username + otp
- **MPIN**: userId + mpin
- **RSA**: userId + challenge + signature
- **PASSKEY**: userId + credentialId + signature
- **AUTHCODE**: userId + authCode

### Key Schemas
- **RegisterRequest**: username, password, email, mobile, projectType
- **LoginRequest**: loginType, username, password, otp, mpin, rsaChallenge, signature, credentialId, authCode
- **AuthResponse**: accessToken, refreshToken, tokenType, expiresIn, userId, username, roles
- **UserDto**: userId, username, email, mobile, projectType, enabled, roles, lastLoginDate
- **ProjectType**: projectTypeId, code, name, description, displayOrder, active

### Security
- **bearerAuth** (JWT) for protected endpoints

---

## 3. Notification Service API

**File:** `notification-service/docs/swagger/openapi.yaml`  
**Base URL:** `http://localhost:7071`  
**Version:** 1.0.0

### Overview
Multi-channel notification service supporting SMS, Email, WhatsApp, and In-App notifications with template-based content and dynamic variable substitution.

### API Tags & Endpoints

| Tag | Endpoints | Description |
|-----|-----------|-------------|
| **Notifications** | POST `/api/notifications` | Send notification (async, queued) |

### Notification Channels
- **SMS**: Phone number recipient
- **EMAIL**: Email address recipient
- **WHATSAPP**: Phone number recipient
- **INAPP**: User ID or username recipient

### Key Schemas
- **NotificationRequest**:
  - **channel** (required): SMS | EMAIL | WHATSAPP | INAPP
  - **templateCode** (required): Template identifier
  - **recipient** (required): Phone, email, or user ID
  - **variables**: Key-value for template placeholders (e.g., `{{otp}}`, `{{name}}`)
  - **priority**: LOW | NORMAL | HIGH | URGENT (default: NORMAL)
  - **metadata**: Additional metadata (source, eventType, etc.)

### Example Payloads
- **SMS**: `{ "channel": "SMS", "templateCode": "OTP_SMS", "recipient": "+1234567890", "variables": { "otp": "123456", "expiry": "3" } }`
- **Email**: `{ "channel": "EMAIL", "templateCode": "WELCOME_EMAIL", "recipient": "user@example.com", "variables": { "name": "John Doe", "activationLink": "..." } }`
- **WhatsApp**: `{ "channel": "WHATSAPP", "templateCode": "ORDER_CONFIRMATION", "recipient": "+1234567890", "variables": { "orderId": "ORD-12345", "amount": "99.99" } }`
- **In-App**: `{ "channel": "INAPP", "templateCode": "ASSIGNMENT_NOTIFICATION", "recipient": "user123", "variables": { "assetName": "Laptop-001", "assignedBy": "Admin" } }`

### Responses
- **202**: Notification accepted and queued
- **400**: Bad request (invalid channel, missing fields)
- **401**: Unauthorized
- **500**: Internal server error

### Security
- **bearerAuth** (JWT)

---

## 4. Helpdesk Service API

**File:** `helpdesk-service/docs/swagger/openapi.yaml` *(newly created)*  
**Base URL:** `http://localhost:7074`  
**Version:** 1.0.0

### Overview
Helpdesk and support management with issues, queries, FAQs, escalation matrix, SLA tracking, service knowledge base, and AI chatbot.

### API Tags & Endpoints

| Tag | Endpoints | Description |
|-----|-----------|-------------|
| **Issue Management** | POST/GET `/api/helpdesk/issues`, GET/PATCH `/{id}`, PATCH `/{id}/assign`, POST `/{id}/resolve`, PATCH `/{id}/close` | Create, list, update status, assign, resolve, close |
| **Issue Management** | GET `status/{status}`, `service/{service}`, `my-issues` | Filter by status, service, or current user |
| **Query Management** | POST/GET `/api/helpdesk/queries`, GET `/{id}`, POST `/{id}/answer`, PATCH `/{id}/close` | Create, list, answer, close queries |
| **Query Management** | GET `status/{status}`, `service/{service}`, `my-queries` | Filter queries |
| **FAQ Management** | POST/GET `/api/helpdesk/faqs`, GET/PUT/DELETE `/{id}` | FAQ CRUD |
| **FAQ Management** | GET `service/{service}`, `category/{category}`, `search`, `service/{service}/search` | Filter & search FAQs |
| **FAQ Management** | POST `/{id}/helpful`, PUT `/{id}/favourite`, `/{id}/most-like`, `/{id}/sequence-order` | Helpful count, favourite, most-like, sequence |
| **Escalation Matrix** | POST/GET `/api/helpdesk/escalation-matrix`, GET/PUT/DELETE `/{id}` | Escalation matrix CRUD |
| **Escalation Matrix** | GET `service/{service}`, `service/{service}/priority/{priority}` | Filter by service and priority |
| **Issue Escalation** | POST `/api/helpdesk/escalations/issue/{issueId}` | Manual escalation |
| **Issue Escalation** | POST `issue/{issueId}/auto-escalate` | Trigger auto-escalation |
| **Issue Escalation** | GET `issue/{issueId}` | Escalation history |
| **SLA Tracking** | GET `/api/helpdesk/sla/issue/{issueId}` | SLA tracking for issue |
| **SLA Tracking** | GET `/api/helpdesk/sla/breaches` | All SLA breaches |
| **SLA Tracking** | POST `issue/{issueId}/first-response` | Record first response |
| **Service Knowledge** | POST/GET `/api/helpdesk/knowledge`, GET/PUT/DELETE `/{id}` | Knowledge base CRUD |
| **Service Knowledge** | GET `service/{service}`, `service/{service}/search` | Filter & search |
| **Chatbot** | POST `/api/helpdesk/chatbot/message` | Send message, get response |
| **Chatbot** | GET `/api/helpdesk/chatbot/session/{sessionId}` | Conversation history |

### Enums
- **IssueStatus**: OPEN, IN_PROGRESS, RESOLVED, CLOSED, REOPENED
- **QueryStatus**: PENDING, ANSWERED, CLOSED
- **RelatedService**: AUTH_SERVICE, NOTIFICATION_SERVICE, ASSET_SERVICE, HELPDESK_SERVICE, UPCOMING_PROJECT
- **IssuePriority**: LOW, MEDIUM, HIGH, CRITICAL
- **SupportLevel**: L1, L2, L3

### Key Schemas
- **IssueRequest**: title, description, priority, relatedService
- **IssueResponse**: id, title, description, status, priority, reportedBy (masked), assignedTo (masked), support levels, SLA tracking, etc.
- **IssueResolutionRequest**: resolution
- **QueryRequest**: question, relatedService
- **QueryResponse**: id, question, answer, status, askedBy (masked), answeredBy (masked)
- **FAQRequest**: question, answer, relatedService, category
- **FAQResponse**: id, question, answer, viewCount, helpfulCount, sequenceOrder, isFavourite, isMostLike
- **EscalationMatrixRequest**: relatedService, priority, supportLevel, initialAssignmentLevel, escalateToLevel, escalationTimeMinutes, responseTimeMinutes, resolutionTimeMinutes
- **IssueEscalationRequest**: toLevel, escalationReason
- **SLATrackingResponse**: responseTimeMinutes, resolutionTimeMinutes, responseSLAMet, resolutionSLAMet, breach timestamps
- **ServiceKnowledgeRequest**: service, topic, content, category, apiEndpoints, commonIssues, troubleshootingSteps
- **ChatbotMessageRequest**: message, sessionId (optional)
- **ChatbotMessageResponse**: sessionId, response, conversationHistory, timestamp

### DPDPA Compliance
PII fields (reportedBy, assignedTo, askedBy, answeredBy, escalatedBy) are automatically masked in responses.

### Security
- **bearerAuth** (JWT) for all endpoints

---

## File Locations Summary

| Service | Swagger File Path |
|--------|-------------------|
| Asset Service | `asset-service/docs/swagger/openapi.yaml` |
| Auth Service | `auth-service/docs/swagger/openapi.yaml` |
| Notification Service | `notification-service/docs/swagger/openapi.yaml` |
| Helpdesk Service | `helpdesk-service/docs/swagger/openapi.yaml` |

## Viewing Swagger UI

When services are running, Swagger UI is typically available at:
- **Asset Service**: `http://localhost:7072/swagger-ui.html`
- **Auth Service**: `http://localhost:8080/swagger-ui.html`
- **Notification Service**: `http://localhost:7071/swagger-ui.html`
- **Helpdesk Service**: `http://localhost:7074/swagger-ui.html` (SpringDoc configured)

## OpenAPI Docs (JSON)

- **Asset Service**: `http://localhost:7072/v3/api-docs`
- **Auth Service**: `http://localhost:8080/v3/api-docs`
- **Notification Service**: `http://localhost:7071/v3/api-docs`
- **Helpdesk Service**: `http://localhost:7074/api-docs`
