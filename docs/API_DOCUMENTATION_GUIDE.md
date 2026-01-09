# API Documentation Guide
## Complete Swagger/OpenAPI and Postman Collections

**Version:** 1.0.0  
**Date:** 2024-01-15

---

## Overview

This guide provides information about the Swagger/OpenAPI documentation and Postman collections available for all three microservices in the Asset Lifecycle Management System.

---

## Swagger/OpenAPI Documentation

### Accessing Swagger UI

When services are running, access Swagger UI at:

1. **Auth Service**
   - URL: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/api-docs`
   - OpenAPI YAML: `auth-service/docs/swagger/openapi.yaml`

2. **Asset Service**
   - URL: `http://localhost:8083/swagger-ui.html`
   - API Docs: `http://localhost:8083/api-docs`
   - OpenAPI YAML: `asset-service/docs/swagger/openapi.yaml`

3. **Notification Service**
   - URL: `http://localhost:8082/swagger-ui.html`
   - API Docs: `http://localhost:8082/api-docs`
   - OpenAPI YAML: `notification-service/docs/swagger/openapi.yaml`

### OpenAPI Specification Files

All OpenAPI 3.0 specification files are located in:
- `auth-service/docs/swagger/openapi.yaml`
- `asset-service/docs/swagger/openapi.yaml`
- `notification-service/docs/swagger/openapi.yaml`

### Viewing OpenAPI Files

#### Option 1: Swagger Editor (Online)
1. Go to [Swagger Editor](https://editor.swagger.io/)
2. Copy the contents of any `openapi.yaml` file
3. Paste into the editor
4. View interactive documentation

#### Option 2: Swagger UI (Local)
```bash
# Install Swagger UI (if not installed)
npm install -g swagger-ui-serve

# View Auth Service
swagger-ui-serve auth-service/docs/swagger/openapi.yaml

# View Asset Service
swagger-ui-serve asset-service/docs/swagger/openapi.yaml

# View Notification Service
swagger-ui-serve notification-service/docs/swagger/openapi.yaml
```

#### Option 3: Postman Import
1. Open Postman
2. Click **Import**
3. Select the `openapi.yaml` file
4. Postman will automatically generate a collection with all endpoints

---

## Postman Collections

### Collection Locations

1. **Auth Service**
   - Collection: `auth-service/docs/postman/Auth_Service_API.postman_collection.json`
   - Environment: Create environment with variables (see below)

2. **Asset Service**
   - Collection: `asset-service/docs/postman/Asset_Service_API.postman_collection.json`
   - Additional Collections:
     - `Master_Data_API.postman_collection.json`
     - `Complete_Asset_Creation_API.postman_collection.json`
     - `UserLinkController.postman_collection.json`
     - `Warranty_AMC_Controllers.postman_collection.json`
     - `Compliance_Agent_API.postman_collection.json`
   - Environment: `asset-service/docs/postman/Asset_Service_Environment.postman_environment.json`

3. **Notification Service**
   - Collection: `notification-service/docs/postman/Notification_Service_API.postman_collection.json` (to be created)
   - Environment: Create environment with variables (see below)

### Importing Postman Collections

1. **Open Postman**
2. Click **Import** button (top left)
3. Select **File** tab
4. Choose the collection JSON file
5. Click **Import**

### Postman Environment Variables

#### Auth Service Environment

Create a new environment in Postman with these variables:

```json
{
  "authbaseUrl": "http://localhost:8080",
  "accessToken": "",
  "refreshToken": "",
  "projectType": "ASSET_SERVICE",
  "userId": "",
  "username": ""
}
```

#### Asset Service Environment

Import the existing environment file:
- `asset-service/docs/postman/Asset_Service_Environment.postman_environment.json`

Or create manually with:

```json
{
  "assetbaseUrl": "http://localhost:8083",
  "accessToken": "",
  "userId": "123",
  "username": "john.doe",
  "projectType": "ASSET_SERVICE",
  "modelId": "1",
  "categoryId": "1",
  "subCategoryId": "1",
  "makeId": "1",
  "targetUserId": "456",
  "targetUsername": "jane.doe"
}
```

#### Notification Service Environment

Create a new environment with:

```json
{
  "notificationbaseUrl": "http://localhost:8082",
  "accessToken": "",
  "templateCode": "OTP_SMS",
  "recipient": "+1234567890"
}
```

### Using Postman Collections

1. **Set Environment**
   - Select the appropriate environment from the dropdown (top right)
   - Ensure all variables are set

2. **Get Access Token**
   - Use the "Login" request in Auth Service collection
   - Copy the `accessToken` from response
   - Set it in the environment variable `accessToken`

3. **Make Requests**
   - All protected endpoints automatically use the `accessToken` from environment
   - Update variables as needed for different requests

---

## Auth Service API

### Collection Structure

The Auth Service Postman collection includes:

1. **Registration**
   - Register User (Complete)
   - Register User (Minimal)
   - Register User (USA)
   - Register Admin User

2. **Login**
   - Login with Password
   - Login with OTP
   - Login with MPIN
   - Login with RSA
   - Login with Passkey
   - Login with Auth Code

3. **OTP Management**
   - Send OTP (SMS)
   - Send OTP (Email)
   - Verify OTP

4. **Token Management**
   - Refresh Token

5. **Credential Management**
   - Register RSA Credential
   - Get RSA Challenge
   - Verify RSA Signature
   - Register WebAuthn Credential
   - Get WebAuthn Challenge
   - Verify WebAuthn Response

6. **MPIN Management**
   - Register/Reset MPIN
   - Verify MPIN
   - Request MPIN Reset
   - Confirm MPIN Reset

7. **Contact Change**
   - Request Email Change
   - Confirm Email Change
   - Request Mobile Change
   - Confirm Mobile Change

8. **Password Management**
   - Change Password
   - Forgot Password

9. **User Profile**
   - Get Current User Profile
   - Get User by ID

10. **Terms and Conditions**
    - Get Terms and Conditions (For App)
    - Get Terms and Conditions (Global)
    - Get Terms and Conditions by Version
    - Get All T&C Versions (Admin)
    - Create Terms and Conditions (Admin)
    - Activate T&C Version (Admin)
    - Deactivate T&C Version (Admin)

11. **Project Types**
    - Get All Active Project Types
    - Get Project Type by ID
    - Get Project Type by Code
    - Create Project Type
    - Update Project Type
    - Delete Project Type
    - Validate Project Type Code

12. **Admin Operations**
    - List All Users
    - Get Audit Logs
    - Get Paginated Audit Logs
    - Export Audit Logs (CSV)
    - Export Audit Logs (Excel)

### Key Features

- **Mobile Validation**: Mobile numbers are validated based on country code
- **T&C Acceptance**: Registration requires Terms & Conditions acceptance
- **Multiple Auth Methods**: Support for 6 different authentication methods
- **Address Fields**: Registration includes pincode, city, state, country

---

## Asset Service API

### Collection Structure

The Asset Service has multiple Postman collections:

#### 1. Asset Service API (Main Collection)

Includes:
- Asset CRUD operations
- Master data management (Categories, Makes, Models, etc.)
- Vendor and Outlet management
- Component management
- Document management
- Warranty and AMC management
- Compliance operations
- Audit operations

#### 2. Master Data API Collection

Includes:
- Get All Master Data
- Get Master Data by User ID
- Need Your Attention API

#### 3. Complete Asset Creation API Collection

Includes:
- Create Complete Asset (With Document)
- Create Complete Asset (Without Document)
- Minimal Required Fields Example
- Full Example

#### 4. User Link Controller Collection

Includes:
- Link Entity to User
- Delink Entity from User
- Link Multiple Entities
- Delink Multiple Entities
- Get Assets Assigned to User
- Get Users by SubCategory

#### 5. Warranty & AMC Controllers Collection

Includes:
- Warranty CRUD operations
- AMC CRUD operations
- Get Warranties by Asset
- Get AMCs by Asset

#### 6. Compliance Agent API Collection

Includes:
- Run Compliance Check
- Get Compliance Status
- Get Compliance Violations
- Get Compliance Metrics

### Key Features

- **Complete Asset Creation**: One-go API to create asset with warranty, document, and user assignment
- **Bulk Operations**: Bulk upload via JSON and Excel
- **Master Data**: Comprehensive master data management
- **User Linking**: Flexible entity-to-user linking
- **Compliance**: Built-in compliance checking
- **Need Your Attention**: Dashboard API with attention indicators

---

## Notification Service API

### Collection Structure

The Notification Service collection includes:

1. **SMS Notifications**
   - Send SMS with Template
   - Send SMS with Custom Message

2. **Email Notifications**
   - Send Email with Template
   - Send Email with Custom Content

3. **WhatsApp Notifications**
   - Send WhatsApp with Template
   - Send WhatsApp with Custom Message

4. **In-App Notifications**
   - Send In-App Notification
   - Send High Priority Notification

### Key Features

- **Multi-channel**: Support for SMS, Email, WhatsApp, and In-App
- **Template-based**: Template code-based notifications with variable substitution
- **Priority Levels**: LOW, NORMAL, HIGH, URGENT
- **Asynchronous**: Queue-based processing

---

## API Testing Workflow

### Step 1: Start Services

```bash
# Start Auth Service
cd auth-service && mvn spring-boot:run

# Start Asset Service
cd asset-service && mvn spring-boot:run

# Start Notification Service
cd notification-service && mvn spring-boot:run
```

### Step 2: Register a User

1. Open Auth Service Postman collection
2. Use "Register User (Complete)" request
3. Set environment variables:
   - `authbaseUrl`: `http://localhost:8080`
   - `projectType`: `ASSET_SERVICE`
4. Execute request
5. Note the username for login

### Step 3: Login

1. Use "Login with Password" request
2. Set username and password from registration
3. Execute request
4. Copy `accessToken` from response
5. Set `accessToken` in environment variables

### Step 4: Test Protected Endpoints

1. All protected endpoints will automatically use the `accessToken`
2. Test various endpoints as needed
3. If token expires, use "Refresh Token" request

### Step 5: Test Asset Service

1. Import Asset Service collections
2. Set `assetbaseUrl` and `accessToken` in environment
3. Test asset creation, linking, etc.

### Step 6: Test Notification Service

1. Import Notification Service collection
2. Set `notificationbaseUrl` and `accessToken`
3. Test sending notifications

---

## Common Issues and Solutions

### Issue: 401 Unauthorized

**Solution:**
- Verify `accessToken` is set in environment
- Check token hasn't expired (default: 15 minutes)
- Use "Refresh Token" to get new access token

### Issue: 400 Bad Request

**Solution:**
- Check request body format
- Verify all required fields are present
- Check field validation rules

### Issue: Connection Refused

**Solution:**
- Verify service is running
- Check port numbers match environment variables
- Verify firewall settings

### Issue: Mobile Validation Error

**Solution:**
- Ensure `countryCode` matches mobile number format
- Check supported country codes
- Verify mobile number format (spaces/dashes are normalized)

---

## Additional Resources

### Documentation Files

- **Technical Functional Document**: `docs/TECHNICAL_FUNCTIONAL_DOCUMENT.md`
- **Swagger README**: `docs/swagger/README.md`
- **Integration Guide**: `docs/swagger/INTEGRATION_GUIDE.md`

### Code Documentation

- **JavaDoc**: Available in source code
- **API Annotations**: See `@Operation` annotations in controllers

### Support

For issues or questions:
- Check service logs
- Review OpenAPI specifications
- Consult technical functional document
- Contact development team

---

## Version History

- **v1.0.0** (2024-01-15): Initial comprehensive documentation

---

**Last Updated:** 2024-01-15  
**Maintained By:** Development Team

