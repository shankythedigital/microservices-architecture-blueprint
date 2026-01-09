# Technical Functional Document
## Microservices Architecture - Asset Lifecycle Management System

**Version:** 1.0.0  
**Date:** 2024-01-15  
**Author:** Development Team

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [System Overview](#system-overview)
3. [Architecture](#architecture)
4. [Service Details](#service-details)
   - [Auth Service](#auth-service)
   - [Asset Service](#asset-service)
   - [Notification Service](#notification-service)
5. [API Documentation](#api-documentation)
6. [Data Models](#data-models)
7. [Security](#security)
8. [Integration Points](#integration-points)
9. [Deployment](#deployment)
10. [Testing](#testing)
11. [Troubleshooting](#troubleshooting)

---

## Executive Summary

This document provides comprehensive technical and functional documentation for the Asset Lifecycle Management System, a microservices-based application designed to manage assets, users, authentication, and notifications across multiple project types.

### Key Features

- **Multi-tenant Architecture**: Supports multiple project types (ASSET_SERVICE, ECOM, etc.)
- **Comprehensive Asset Management**: Full lifecycle management from creation to retirement
- **Flexible Authentication**: Multiple authentication methods (Password, OTP, MPIN, RSA, WebAuthn)
- **Multi-channel Notifications**: SMS, Email, WhatsApp, and In-App notifications
- **Compliance Management**: Built-in compliance checking and validation
- **Audit Trail**: Complete audit logging for all operations
- **Master Data Management**: Centralized management of categories, makes, models, vendors, etc.

---

## System Overview

### Purpose

The Asset Lifecycle Management System is designed to:
- Manage assets throughout their entire lifecycle
- Track asset assignments to users
- Manage warranties and AMCs (Annual Maintenance Contracts)
- Provide comprehensive audit trails
- Support multiple authentication methods
- Send notifications across multiple channels
- Ensure compliance with business rules

### Technology Stack

- **Backend Framework**: Spring Boot 3.x
- **Database**: MySQL 8.0
- **Authentication**: JWT (JSON Web Tokens)
- **API Documentation**: OpenAPI 3.0 / Swagger
- **Build Tool**: Maven
- **Java Version**: 17+

### System Components

1. **Auth Service** (Port 8080/8081)
   - User registration and authentication
   - Credential management (RSA, WebAuthn, MPIN)
   - Terms and Conditions management
   - Project type management
   - Audit logging

2. **Asset Service** (Port 8082/8083)
   - Asset CRUD operations
   - Master data management
   - User-Asset linking
   - Warranty and AMC management
   - Document management
   - Compliance checking

3. **Notification Service** (Port 8081/8082)
   - Multi-channel notification delivery
   - Template-based notifications
   - Asynchronous processing

---

## Architecture

### Microservices Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Auth Service  │     │  Asset Service  │     │Notification Svc │
│   (Port 8080)   │     │   (Port 8083)   │     │   (Port 8082)   │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         └───────────────────────┴───────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │   MySQL Database         │
                    │   (Port 3306)            │
                    └──────────────────────────┘
```

### Service Communication

- **Synchronous**: REST APIs with JWT authentication
- **Asynchronous**: Notification queue for async processing
- **Database**: Each service has its own database schema

### Data Flow

1. **User Registration Flow**:
   ```
   Client → Auth Service → Database
   Auth Service → Notification Service (Welcome Email/SMS)
   ```

2. **Asset Creation Flow**:
   ```
   Client → Asset Service → Database
   Asset Service → Notification Service (Assignment Notification)
   ```

3. **Authentication Flow**:
   ```
   Client → Auth Service → JWT Token Generation
   Client → Other Services (with JWT Token)
   ```

---

## Service Details

### Auth Service

#### Overview

The Auth Service handles all authentication and authorization operations, user management, and Terms & Conditions management.

#### Base URL
- **Local**: `http://localhost:8080`
- **Production**: `https://api.example.com/auth-service`

#### Key Features

1. **User Registration**
   - Standard user registration
   - Admin user registration
   - Mobile number validation by country code
   - Terms & Conditions acceptance validation
   - User detail storage (pincode, city, state, country)

2. **Authentication Methods**
   - **Password**: Username and password
   - **OTP**: One-Time Password via SMS/Email
   - **MPIN**: Mobile PIN authentication
   - **RSA**: RSA signature-based authentication
   - **WebAuthn/Passkey**: FIDO2/WebAuthn authentication
   - **Auth Code**: Authorization code-based authentication

3. **Credential Management**
   - RSA credential registration and verification
   - WebAuthn credential registration and verification
   - MPIN registration, verification, and reset

4. **Terms & Conditions**
   - Version management
   - Project-specific T&C
   - Multi-language support
   - Active version tracking

5. **User Management**
   - User profile retrieval
   - Contact change (email/mobile) with OTP verification
   - Password change and reset

6. **Admin Operations**
   - User listing
   - Audit log retrieval and export
   - Project type management

#### Database Schema

- `user_master`: User accounts
- `user_detail_master`: User details (encrypted)
- `otp_master`: OTP storage
- `credential_master`: RSA/WebAuthn credentials
- `mpin_master`: MPIN storage
- `terms_and_conditions`: T&C content
- `project_type_master`: Project types
- `audit_log`: Audit trail

#### API Endpoints

See [Auth Service API Documentation](#auth-service-api-documentation) for complete endpoint list.

---

### Asset Service

#### Overview

The Asset Service manages the complete asset lifecycle, including asset creation, master data management, user-asset linking, warranty/AMC management, and compliance checking.

#### Base URL
- **Local**: `http://localhost:8083`
- **Production**: `https://api.example.com/asset-service`

#### Key Features

1. **Asset Management**
   - Asset CRUD operations
   - Bulk asset creation (JSON and Excel)
   - Complete asset creation (one-go API)
   - Asset status management
   - Serial number and purchase date tracking

2. **Master Data Management**
   - Categories and Sub-categories
   - Makes and Models
   - Vendors and Outlets
   - Components
   - Status master
   - Entity types

3. **User-Asset Linking**
   - Link/delink assets to users
   - Multi-entity linking
   - Link via related entities (Make, Model, Category, etc.)
   - User asset retrieval

4. **Warranty & AMC Management**
   - Warranty creation and management
   - AMC creation and management
   - Expiry tracking
   - Document association

5. **Document Management**
   - Document upload
   - Entity-document association
   - Document retrieval and download

6. **Compliance Management**
   - Compliance rule definition
   - Compliance checking
   - Violation tracking
   - Compliance metrics

7. **Comprehensive Data APIs**
   - Master data retrieval (all entities)
   - Need Your Attention API (dashboard data)
   - User-specific data filtering

#### Database Schema

- `asset_master`: Assets
- `asset_user_link`: User-asset assignments
- `asset_component`: Components
- `asset_warranty`: Warranties
- `asset_amc`: AMCs
- `asset_document`: Documents
- `product_category`: Categories
- `product_sub_category`: Sub-categories
- `product_make`: Makes
- `product_model`: Models
- `vendor_master`: Vendors
- `purchase_outlet`: Outlets
- `status_master`: Status codes
- `entity_type_master`: Entity types
- `compliance_rule`: Compliance rules
- `compliance_violation`: Violations
- `audit_log`: Audit trail

#### API Endpoints

See [Asset Service API Documentation](#asset-service-api-documentation) for complete endpoint list.

---

### Notification Service

#### Overview

The Notification Service provides multi-channel notification capabilities with template-based content and asynchronous processing.

#### Base URL
- **Local**: `http://localhost:8082`
- **Production**: `https://api.example.com/notification-service`

#### Key Features

1. **Multi-channel Support**
   - SMS notifications
   - Email notifications
   - WhatsApp notifications
   - In-App notifications

2. **Template-based Notifications**
   - Template code-based content
   - Dynamic variable substitution
   - Multi-language support

3. **Asynchronous Processing**
   - Queue-based processing
   - Priority levels (LOW, NORMAL, HIGH, URGENT)
   - Retry mechanism

4. **Metadata Support**
   - Source tracking
   - Event type tracking
   - Custom metadata

#### Database Schema

- `notification_queue`: Notification queue
- `notification_log`: Notification history

#### API Endpoints

See [Notification Service API Documentation](#notification-service-api-documentation) for complete endpoint list.

---

## API Documentation

### Authentication

All protected endpoints require JWT Bearer token authentication:

```http
Authorization: Bearer <accessToken>
```

### Common Response Format

Most endpoints return responses in the following format:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

### HTTP Status Codes

- `200 OK`: Success
- `201 Created`: Resource created
- `202 Accepted`: Request accepted (async processing)
- `400 Bad Request`: Invalid request
- `401 Unauthorized`: Missing or invalid token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

---

### Auth Service API Documentation

#### Authentication Endpoints

##### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john.doe",
  "password": "SecurePass123!",
  "email": "john.doe@example.com",
  "mobile": "+1234567890",
  "countryCode": "+1",
  "projectType": "ASSET_SERVICE",
  "pincode": "12345",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "acceptTc": true
}
```

##### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "loginType": "PASSWORD",
  "username": "john.doe",
  "password": "SecurePass123!"
}
```

##### Refresh Token
```http
POST /api/auth/refresh?refreshToken=<token>
```

##### Send OTP
```http
POST /api/auth/otp/send
Content-Type: application/json

{
  "userId": 123,
  "purpose": "LOGIN",
  "channel": "SMS"
}
```

#### Credential Management

##### Register RSA Credential
```http
POST /api/auth/credential/register
Content-Type: application/json

{
  "userId": 123,
  "type": "RSA",
  "credentialId": "rsa-key-1",
  "publicKey": "-----BEGIN PUBLIC KEY-----..."
}
```

##### Get RSA Challenge
```http
GET /api/auth/credential/rsa/challenge/{userId}
```

##### Verify RSA Signature
```http
POST /api/auth/credential/rsa/verify
Content-Type: application/json

{
  "userId": 123,
  "challenge": "random-challenge-string",
  "signature": "base64-signature"
}
```

#### MPIN Management

##### Register/Reset MPIN
```http
POST /api/auth/mpin/register
Content-Type: application/json

{
  "userId": 123,
  "mpin": "1234",
  "deviceInfo": "Device-Info"
}
```

##### Verify MPIN
```http
POST /api/auth/mpin/verify
Content-Type: application/json

{
  "userId": 123,
  "mpin": "1234",
  "deviceInfo": "Device-Info"
}
```

#### Terms & Conditions

##### Get Active T&C
```http
GET /api/auth/terms-and-conditions?projectType=ASSET_SERVICE&language=en
```

##### Get T&C by Version
```http
GET /api/auth/terms-and-conditions/version?projectType=ASSET_SERVICE&version=1.0
```

##### Create T&C (Admin)
```http
POST /api/auth/terms-and-conditions
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "projectType": "ASSET_SERVICE",
  "version": "1.0",
  "title": "Terms and Conditions",
  "content": "Full T&C content...",
  "summary": "Summary...",
  "language": "en"
}
```

#### User Management

##### Get Current User Profile
```http
GET /api/users/me
Authorization: Bearer <token>
```

##### Change Password
```http
POST /api/auth/password/change
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 123,
  "currentPassword": "OldPass123!",
  "newPassword": "NewPass123!"
}
```

---

### Asset Service API Documentation

#### Asset Management

##### Create Asset
```http
POST /api/asset/v1/assets
Authorization: Bearer <token>
Content-Type: application/json

{
  "assetNameUdv": "Laptop-001",
  "modelId": 1,
  "categoryId": 1,
  "subCategoryId": 1,
  "makeId": 1,
  "serialNumber": "SN123456",
  "purchaseDate": "2024-01-15"
}
```

##### Complete Asset Creation (One-Go)
```http
POST /api/asset/v1/assets/complete
Authorization: Bearer <token>
Content-Type: multipart/form-data

userId: 123
username: john.doe
assetNameUdv: Laptop-001
modelId: 1
serialNumber: SN123456
warrantyStartDate: 2024-01-15
warrantyEndDate: 2025-01-15
targetUserId: 456
purchaseInvoice: <file>
```

##### Get All Assets
```http
GET /api/asset/v1/assets?page=0&size=20
Authorization: Bearer <token>
```

##### Get Asset by ID
```http
GET /api/asset/v1/assets/{id}
Authorization: Bearer <token>
```

#### Master Data Management

##### Get All Categories
```http
GET /api/asset/v1/categories
```

##### Create Category
```http
POST /api/asset/v1/categories
Authorization: Bearer <token>
Content-Type: application/json

{
  "categoryName": "Electronics",
  "description": "Electronic devices"
}
```

##### Bulk Upload Categories (Excel)
```http
POST /api/asset/v1/categories/upload-excel
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <excel-file>
```

#### User-Asset Linking

##### Link Entity to User
```http
POST /api/asset/v1/userlinks/link
Authorization: Bearer <token>
Content-Type: application/json

{
  "entityType": "ASSET",
  "entityId": 1,
  "targetUserId": 123,
  "targetUsername": "john.doe"
}
```

##### Link Multiple Entities
```http
POST /api/asset/v1/userlinks/multi-link
Authorization: Bearer <token>
Content-Type: application/json

{
  "entityType": "ASSET",
  "entityIds": [1, 2, 3],
  "targetUserId": 123,
  "targetUsername": "john.doe"
}
```

##### Get Assets Assigned to User
```http
GET /api/asset/v1/userlinks/user/{userId}/assets
Authorization: Bearer <token>
```

#### Comprehensive Data APIs

##### Get All Master Data
```http
GET /api/asset/v1/userlinks/master-data/all
Authorization: Bearer <token>
```

##### Get Master Data by User ID
```http
GET /api/asset/v1/userlinks/master-data/all?userId=123
Authorization: Bearer <token>
```

##### Need Your Attention API
```http
GET /api/asset/v1/userlinks/need-your-attention
Authorization: Bearer <token>
```

#### Warranty Management

##### Create Warranty
```http
POST /api/asset/v1/warranty
Authorization: Bearer <token>
Content-Type: application/json

{
  "assetId": 1,
  "warrantyStartDate": "2024-01-15",
  "warrantyEndDate": "2025-01-15",
  "warrantyProvider": "Manufacturer",
  "warrantyStatus": "ACTIVE"
}
```

#### AMC Management

##### Create AMC
```http
POST /api/asset/v1/amc
Authorization: Bearer <token>
Content-Type: application/json

{
  "assetId": 1,
  "startDate": "2024-01-15",
  "endDate": "2025-01-15",
  "amcStatus": "ACTIVE"
}
```

#### Document Management

##### Upload Document
```http
POST /api/asset/v1/documents
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <file>
entityType: ASSET
entityId: 1
documentType: PURCHASE_INVOICE
```

---

### Notification Service API Documentation

#### Send Notification

##### SMS Notification
```http
POST /api/notifications
Authorization: Bearer <token>
Content-Type: application/json

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

##### Email Notification
```http
POST /api/notifications
Authorization: Bearer <token>
Content-Type: application/json

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

##### WhatsApp Notification
```http
POST /api/notifications
Authorization: Bearer <token>
Content-Type: application/json

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

##### In-App Notification
```http
POST /api/notifications
Authorization: Bearer <token>
Content-Type: application/json

{
  "channel": "INAPP",
  "templateCode": "ASSIGNMENT_NOTIFICATION",
  "recipient": "user123",
  "variables": {
    "assetName": "Laptop-001",
    "assignedBy": "Admin"
  },
  "priority": "HIGH"
}
```

---

## Data Models

### User Model

```json
{
  "userId": 123,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "mobile": "+1234567890",
  "countryCode": "+1",
  "projectType": "ASSET_SERVICE",
  "pincode": "12345",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "acceptTc": true,
  "enabled": true,
  "roles": ["USER"],
  "lastLoginDate": "2024-01-15T10:30:00Z"
}
```

### Asset Model

```json
{
  "assetId": 1,
  "assetNameUdv": "Laptop-001",
  "serialNumber": "SN123456",
  "purchaseDate": "2024-01-15",
  "assetStatus": "ACTIVE",
  "category": {
    "categoryId": 1,
    "categoryName": "Electronics"
  },
  "subCategory": {
    "subCategoryId": 1,
    "subCategoryName": "Laptops"
  },
  "make": {
    "makeId": 1,
    "makeName": "Dell"
  },
  "model": {
    "modelId": 1,
    "modelName": "XPS 13"
  },
  "active": true,
  "createdBy": "admin",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### Warranty Model

```json
{
  "warrantyId": 1,
  "assetId": 1,
  "warrantyStartDate": "2024-01-15",
  "warrantyEndDate": "2025-01-15",
  "warrantyProvider": "Manufacturer",
  "warrantyStatus": "ACTIVE",
  "warrantyTerms": "Standard warranty terms",
  "active": true
}
```

---

## Security

### Authentication

- **JWT Tokens**: Access tokens and refresh tokens
- **Token Expiry**: Configurable (default: 15 minutes for access, 14 days for refresh)
- **Token Storage**: Secure HTTP-only cookies or client-side storage

### Authorization

- **Role-based Access Control (RBAC)**: USER, ADMIN roles
- **Resource-level Authorization**: Users can only access their own resources (unless admin)

### Data Encryption

- **Sensitive Fields**: User details (pincode, city, state, country) are encrypted at rest
- **Encryption Algorithm**: AES-256
- **Key Management**: HMAC-based key derivation

### API Security

- **HTTPS**: All production APIs use HTTPS
- **CORS**: Configured for specific origins
- **Rate Limiting**: Implemented for authentication endpoints
- **Input Validation**: All inputs are validated and sanitized

---

## Integration Points

### Service-to-Service Communication

1. **Asset Service → Notification Service**
   - Asset assignment notifications
   - Warranty expiry notifications
   - AMC expiry notifications

2. **Auth Service → Notification Service**
   - Welcome emails/SMS
   - OTP delivery
   - Password reset notifications

### External Integrations

1. **SMS Gateway**: For SMS notifications
2. **Email Service**: For email notifications
3. **WhatsApp Business API**: For WhatsApp notifications

---

## Deployment

### Prerequisites

- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Environment Variables

#### Auth Service
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/authdb
spring.datasource.username=root
spring.datasource.password=password
jwt.secret=your-secret-key
jwt.public-key-path=classpath:keys/jwt-public.pem
```

#### Asset Service
```properties
server.port=8083
spring.datasource.url=jdbc:mysql://localhost:3306/assetdb
spring.datasource.username=root
spring.datasource.password=password
auth.service.url=http://localhost:8080
notification.service.url=http://localhost:8082
```

#### Notification Service
```properties
server.port=8082
spring.datasource.url=jdbc:mysql://localhost:3306/notificationdb
spring.datasource.username=root
spring.datasource.password=password
auth.service.url=http://localhost:8080
```

### Build and Run

```bash
# Build all services
mvn clean install

# Run Auth Service
cd auth-service
mvn spring-boot:run

# Run Asset Service
cd asset-service
mvn spring-boot:run

# Run Notification Service
cd notification-service
mvn spring-boot:run
```

### Docker Deployment

```bash
# Build Docker images
docker build -t auth-service:latest ./auth-service
docker build -t asset-service:latest ./asset-service
docker build -t notification-service:latest ./notification-service

# Run with Docker Compose
docker-compose up -d
```

---

## Testing

### API Testing

Use the provided Postman collections:
- `auth-service/docs/postman/Auth_Service_API.postman_collection.json`
- `asset-service/docs/postman/Asset_Service_API.postman_collection.json`
- `notification-service/docs/postman/Notification_Service_API.postman_collection.json`

### Unit Testing

```bash
# Run unit tests
mvn test

# Run with coverage
mvn test jacoco:report
```

### Integration Testing

```bash
# Run integration tests
mvn verify
```

---

## Troubleshooting

### Common Issues

1. **Authentication Failures**
   - Verify JWT token is valid and not expired
   - Check token format: `Bearer <token>`
   - Verify user has required permissions

2. **Database Connection Issues**
   - Verify database is running
   - Check connection string and credentials
   - Verify database schema is created

3. **Notification Failures**
   - Check notification service is running
   - Verify template codes exist
   - Check external service integrations (SMS/Email gateway)

4. **File Upload Issues**
   - Verify file size limits
   - Check file storage directory permissions
   - Verify multipart configuration

### Logs

Logs are available at:
- Auth Service: `auth-service/logs/application.log`
- Asset Service: `asset-service/logs/application.log`
- Notification Service: `notification-service/logs/application.log`

### Health Checks

```http
GET /actuator/health
```

---

## Appendix

### Swagger/OpenAPI Documentation

- **Auth Service**: `http://localhost:8080/swagger-ui.html`
- **Asset Service**: `http://localhost:8083/swagger-ui.html`
- **Notification Service**: `http://localhost:8082/swagger-ui.html`

### Postman Collections

All Postman collections are available in:
- `auth-service/docs/postman/`
- `asset-service/docs/postman/`
- `notification-service/docs/postman/`

### Additional Resources

- API Documentation: See OpenAPI YAML files in `docs/swagger/`
- Database Schema: See migration files in `*/src/main/resources/db/migration/`
- Configuration: See `application.yml` files in each service

---

**Document Version:** 1.0.0  
**Last Updated:** 2024-01-15  
**Maintained By:** Development Team

